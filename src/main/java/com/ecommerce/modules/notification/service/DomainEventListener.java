package com.ecommerce.modules.notification.service;

import com.ecommerce.common.event.OrderCancelledEvent;
import com.ecommerce.common.event.OrderCreatedEvent;
import com.ecommerce.common.event.OrderPaidEvent;
import com.ecommerce.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for email={}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail(), event.getFullName());
    }

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent for orderNo={}", event.getOrderNo());
        notificationService.createNotification(
                event.getUserId(),
                "Đặt hàng thành công",
                "Đơn hàng #" + event.getOrderNo() + " với giá trị " + event.getTotalAmount() + " VNĐ đã được tạo thành công.",
                "ORDER_STATUS",
                event.getOrderNo()
        );
        emailService.sendOrderConfirmationEmail(event.getUserEmail(), event.getOrderNo(), event.getTotalAmount());
    }

    @Async
    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Handling OrderPaidEvent for orderNo={}", event.getOrderNo());
        notificationService.createNotification(
                event.getUserId(),
                "Thanh toán thành công",
                "Đơn hàng #" + event.getOrderNo() + " đã được xác nhận thanh toán thành công.",
                "ORDER_STATUS",
                event.getOrderNo()
        );
        emailService.sendPaymentSuccessEmail(event.getUserEmail(), event.getOrderNo(), event.getAmount(), event.getTransactionId());
    }

    @Async
    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Handling OrderCancelledEvent for orderNo={}", event.getOrderNo());
        notificationService.createNotification(
                event.getUserId(),
                "Đơn hàng đã bị hủy",
                "Đơn hàng #" + event.getOrderNo() + " đã được hủy. Lý do: " + event.getReason(),
                "ORDER_STATUS",
                event.getOrderNo()
        );
    }
}
