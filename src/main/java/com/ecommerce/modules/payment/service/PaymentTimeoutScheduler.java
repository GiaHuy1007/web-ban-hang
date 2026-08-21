package com.ecommerce.modules.payment.service;

import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderItem;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.OrderStatusLog;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.OrderStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final InventoryService inventoryService;

    @Value("${app.inventory.reservation-timeout-minutes:15}")
    private long reservationTimeoutMinutes;

    @Scheduled(fixedDelay = 60000) // Runs every 60 seconds
    @Transactional
    public void cleanupExpiredUnpaidOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(reservationTimeoutMinutes);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, cutoff);

        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired unpaid orders to cancel and release inventory.", expiredOrders.size());
        }

        for (Order order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderRepository.save(order);

                // Release reserved stock
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getVariant() != null) {
                            inventoryService.releaseStock(item.getVariant().getId(), item.getQuantity(), order.getOrderNo());
                        }
                    }
                }

                statusLogRepository.save(OrderStatusLog.builder()
                        .order(order)
                        .previousStatus(OrderStatus.PENDING_PAYMENT)
                        .newStatus(OrderStatus.CANCELLED)
                        .reason("Hết hạn 15 phút chờ thanh toán online. Tự động hủy đơn và giải phóng tồn kho.")
                        .changedBy("PAYMENT_TIMEOUT_JOB")
                        .build());

                log.info("Auto-cancelled expired order: orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("Error auto-cancelling order {}: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }
}
