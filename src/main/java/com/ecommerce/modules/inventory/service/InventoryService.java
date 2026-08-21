package com.ecommerce.modules.inventory.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.inventory.dto.InventoryResponse;
import com.ecommerce.modules.inventory.dto.StockAdjustmentRequest;
import com.ecommerce.modules.inventory.dto.StockInboundRequest;
import com.ecommerce.modules.inventory.entity.*;
import com.ecommerce.modules.inventory.repository.InventoryRepository;
import com.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.ecommerce.modules.inventory.repository.ProductSerialRepository;
import com.ecommerce.modules.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository variantRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductSerialRepository serialRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoriesByVariant(Long variantId) {
        return inventoryRepository.findByVariantId(variantId).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getAvailableStock(Long variantId) {
        Integer available = inventoryRepository.getTotalAvailableStock(variantId);
        return available != null ? available : 0;
    }

    /**
     * Reserve stock for an order using Pessimistic Write Lock (SELECT ... FOR UPDATE)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reserveStock(Long variantId, int quantity, String orderNo) {
        List<Inventory> inventories = inventoryRepository.findByVariantIdForUpdate(variantId);

        int totalAvailable = inventories.stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();

        if (totalAvailable < quantity) {
            log.warn("Stock reservation failed: variantId={}, requested={}, available={}", variantId, quantity, totalAvailable);
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Sản phẩm không đủ số lượng tồn kho (yêu cầu: " + quantity + ", còn lại: " + totalAvailable + ").");
        }

        int remainingToReserve = quantity;
        for (Inventory inv : inventories) {
            int availableInWarehouse = inv.getAvailableQuantity();
            if (availableInWarehouse <= 0) continue;

            int reserveFromThis = Math.min(remainingToReserve, availableInWarehouse);
            inv.setQuantityReserved(inv.getQuantityReserved() + reserveFromThis);
            inventoryRepository.save(inv);

            // Log Transaction
            transactionRepository.save(InventoryTransaction.builder()
                    .variantId(variantId)
                    .warehouseId(inv.getWarehouse().getId())
                    .transactionType(TransactionType.RESERVATION)
                    .quantity(reserveFromThis)
                    .referenceId(orderNo)
                    .referenceType("ORDER")
                    .note("Giữ chỗ tồn kho cho đơn hàng " + orderNo)
                    .build());

            remainingToReserve -= reserveFromThis;
            if (remainingToReserve == 0) break;
        }

        log.info("Stock reserved successfully: variantId={}, quantity={}, orderNo={}", variantId, quantity, orderNo);
    }

    /**
     * Release previously reserved stock (e.g. order cancelled or payment timeout)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void releaseStock(Long variantId, int quantity, String orderNo) {
        List<Inventory> inventories = inventoryRepository.findByVariantIdForUpdate(variantId);

        int remainingToRelease = quantity;
        for (Inventory inv : inventories) {
            if (inv.getQuantityReserved() <= 0) continue;

            int releaseFromThis = Math.min(remainingToRelease, inv.getQuantityReserved());
            inv.setQuantityReserved(inv.getQuantityReserved() - releaseFromThis);
            inventoryRepository.save(inv);

            // Log Transaction
            transactionRepository.save(InventoryTransaction.builder()
                    .variantId(variantId)
                    .warehouseId(inv.getWarehouse().getId())
                    .transactionType(TransactionType.RELEASE)
                    .quantity(releaseFromThis)
                    .referenceId(orderNo)
                    .referenceType("ORDER")
                    .note("Giải phóng tồn kho giữ chỗ cho đơn hàng " + orderNo)
                    .build());

            remainingToRelease -= releaseFromThis;
            if (remainingToRelease == 0) break;
        }

        log.info("Stock released: variantId={}, quantity={}, orderNo={}", variantId, quantity, orderNo);
    }

    /**
     * Deduct stock permanently upon successful payment/confirmation
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deductStock(Long variantId, int quantity, String orderNo) {
        List<Inventory> inventories = inventoryRepository.findByVariantIdForUpdate(variantId);

        int remainingToDeduct = quantity;
        for (Inventory inv : inventories) {
            if (inv.getQuantityReserved() <= 0) continue;

            int deductFromThis = Math.min(remainingToDeduct, inv.getQuantityReserved());
            inv.setQuantityOnHand(Math.max(0, inv.getQuantityOnHand() - deductFromThis));
            inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - deductFromThis));
            inventoryRepository.save(inv);

            // Log Transaction
            transactionRepository.save(InventoryTransaction.builder()
                    .variantId(variantId)
                    .warehouseId(inv.getWarehouse().getId())
                    .transactionType(TransactionType.DEDUCTION)
                    .quantity(deductFromThis)
                    .referenceId(orderNo)
                    .referenceType("ORDER")
                    .note("Khấu trừ xuất kho thực tế cho đơn hàng đã thanh toán " + orderNo)
                    .build());

            remainingToDeduct -= deductFromThis;
            if (remainingToDeduct == 0) break;
        }

        log.info("Stock deducted permanently: variantId={}, quantity={}, orderNo={}", variantId, quantity, orderNo);
    }

    /**
     * Admin: Inbound new stock to a warehouse
     */
    @Transactional
    public InventoryResponse inboundStock(StockInboundRequest request) {
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Inventory inventory = inventoryRepository.findByVariantIdAndWarehouseId(variant.getId(), warehouse.getId())
                .orElseGet(() -> Inventory.builder()
                        .variant(variant)
                        .warehouse(warehouse)
                        .quantityOnHand(0)
                        .quantityReserved(0)
                        .build());

        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);

        // Record Serials/IMEI if provided
        if (request.getSerialNumbers() != null && !request.getSerialNumbers().isEmpty()) {
            for (String serial : request.getSerialNumbers()) {
                if (serialRepository.existsBySerialNumber(serial.trim())) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Số serial '" + serial + "' đã tồn tại trong hệ thống.");
                }
                serialRepository.save(ProductSerial.builder()
                        .variant(variant)
                        .warehouse(warehouse)
                        .serialNumber(serial.trim())
                        .status(SerialStatus.IN_STOCK)
                        .build());
            }
        }

        // Log Transaction
        transactionRepository.save(InventoryTransaction.builder()
                .variantId(variant.getId())
                .warehouseId(warehouse.getId())
                .transactionType(TransactionType.INBOUND)
                .quantity(request.getQuantity())
                .note(request.getNote() != null ? request.getNote() : "Nhập hàng vào kho")
                .build());

        log.info("Stock inbounded: variantId={}, warehouseId={}, qty={}", variant.getId(), warehouse.getId(), request.getQuantity());
        return InventoryResponse.from(saved);
    }

    /**
     * Admin: Adjust stock after inventory audit
     */
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Inventory inventory = inventoryRepository.findByVariantIdAndWarehouseId(variant.getId(), warehouse.getId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        int newQuantity = inventory.getQuantityOnHand() + request.getQuantityChange();
        if (newQuantity < inventory.getQuantityReserved()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Số lượng tồn sau điều chỉnh không thể nhỏ hơn số lượng đang giữ chỗ.");
        }

        inventory.setQuantityOnHand(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);

        transactionRepository.save(InventoryTransaction.builder()
                .variantId(variant.getId())
                .warehouseId(warehouse.getId())
                .transactionType(TransactionType.ADJUSTMENT)
                .quantity(request.getQuantityChange())
                .note(request.getReason())
                .build());

        return InventoryResponse.from(saved);
    }
}
