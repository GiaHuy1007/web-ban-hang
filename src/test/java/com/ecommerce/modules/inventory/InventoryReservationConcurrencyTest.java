package com.ecommerce.modules.inventory;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import com.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.ecommerce.modules.inventory.entity.Inventory;
import com.ecommerce.modules.inventory.entity.Warehouse;
import com.ecommerce.modules.inventory.repository.InventoryRepository;
import com.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.ecommerce.modules.inventory.repository.ProductSerialRepository;
import com.ecommerce.modules.inventory.repository.WarehouseRepository;
import com.ecommerce.modules.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationConcurrencyTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private ProductSerialRepository serialRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory sampleInventory;
    private Warehouse sampleWarehouse;
    private ProductVariant sampleVariant;

    @BeforeEach
    void setUp() {
        sampleWarehouse = Warehouse.builder().id(1L).code("WH-01").name("Main Warehouse").build();
        sampleVariant = ProductVariant.builder().id(10L).sku("MACBOOK-M4-SILVER").build();
        sampleInventory = Inventory.builder()
                .id(100L)
                .variant(sampleVariant)
                .warehouse(sampleWarehouse)
                .quantityOnHand(10)
                .quantityReserved(0)
                .build();
    }

    @Test
    void reserveStock_ShouldSucceed_WhenStockIsAvailable() {
        when(inventoryRepository.findByVariantIdForUpdate(10L))
                .thenReturn(Collections.singletonList(sampleInventory));

        inventoryService.reserveStock(10L, 2, "ORD-TEST-001");

        assertEquals(2, sampleInventory.getQuantityReserved());
        assertEquals(8, sampleInventory.getAvailableQuantity());
        verify(inventoryRepository, times(1)).save(sampleInventory);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void reserveStock_ShouldDeductAndReleaseCorrectly() {
        when(inventoryRepository.findByVariantIdForUpdate(10L))
                .thenReturn(Collections.singletonList(sampleInventory));

        // 1. Reserve 3 items
        inventoryService.reserveStock(10L, 3, "ORD-TEST-002");
        assertEquals(3, sampleInventory.getQuantityReserved());

        // 2. Deduct 3 items upon successful payment
        inventoryService.deductStock(10L, 3, "ORD-TEST-002");
        assertEquals(7, sampleInventory.getQuantityOnHand());
        assertEquals(0, sampleInventory.getQuantityReserved());
        assertEquals(7, sampleInventory.getAvailableQuantity());

        // 3. Reserve 2 items and release (cancel)
        inventoryService.reserveStock(10L, 2, "ORD-TEST-003");
        assertEquals(2, sampleInventory.getQuantityReserved());

        inventoryService.releaseStock(10L, 2, "ORD-TEST-003");
        assertEquals(0, sampleInventory.getQuantityReserved());
        assertEquals(7, sampleInventory.getAvailableQuantity());
    }
}
