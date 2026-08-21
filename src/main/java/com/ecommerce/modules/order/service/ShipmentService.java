package com.ecommerce.modules.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.inventory.entity.Warehouse;
import com.ecommerce.modules.inventory.repository.WarehouseRepository;
import com.ecommerce.modules.order.dto.CreateShipmentRequest;
import com.ecommerce.modules.order.dto.ShipmentResponse;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.Shipment;
import com.ecommerce.modules.order.entity.ShipmentStatus;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderService orderService;

    @Transactional
    public ShipmentResponse createShipment(Long orderId, CreateShipmentRequest request, String adminEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        Shipment shipment = Shipment.builder()
                .order(order)
                .warehouse(warehouse)
                .carrier(request.getCarrier().trim())
                .trackingNo(request.getTrackingNo() != null ? request.getTrackingNo().trim() : null)
                .status(ShipmentStatus.READY_FOR_PICKUP)
                .shippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO)
                .estimatedDeliveryAt(request.getEstimatedDeliveryAt())
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Update Order status to PREPARING / SHIPPING
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        log.info("Shipment created: id={}, orderNo={}, carrier={}", saved.getId(), order.getOrderNo(), saved.getCarrier());
        return ShipmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrder(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy thông tin vận đơn cho đơn hàng này."));
        return ShipmentResponse.from(shipment);
    }
}
