package com.ecommerce.modules.payment.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.JsonUtils;
import com.ecommerce.modules.inventory.service.InventoryService;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderItem;
import com.ecommerce.modules.order.entity.OrderStatus;
import com.ecommerce.modules.order.entity.OrderStatusLog;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.order.repository.OrderRepository;
import com.ecommerce.modules.order.repository.OrderStatusLogRepository;
import com.ecommerce.modules.payment.dto.PaymentInitResponse;
import com.ecommerce.modules.payment.dto.PaymentRefundRequest;
import com.ecommerce.modules.payment.dto.PaymentResponse;
import com.ecommerce.modules.payment.entity.Payment;
import com.ecommerce.modules.payment.gateway.*;
import com.ecommerce.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentInitResponse initiatePayment(Long orderId, String idempotencyKey) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng này đã được thanh toán thành công trước đó.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        PaymentGateway gateway = gatewayFactory.getGateway(order.getPaymentMethod());
        PaymentInitResult initResult = gateway.initiatePayment(order, idempotencyKey);

        String finalKey = idempotencyKey;
        Payment payment = paymentRepository.findByIdempotencyKey(finalKey)
                .orElseGet(() -> Payment.builder()
                        .order(order)
                        .paymentMethod(order.getPaymentMethod())
                        .provider(order.getPaymentMethod().name())
                        .transactionId(initResult.getTransactionId())
                        .status(initResult.getStatus())
                        .amount(order.getTotalAmount())
                        .currency("VND")
                        .idempotencyKey(finalKey)
                        .build());

        payment.setStatus(initResult.getStatus());
        payment.setTransactionId(initResult.getTransactionId());
        Payment saved = paymentRepository.save(payment);

        return PaymentInitResponse.builder()
                .paymentId(saved.getId())
                .orderNo(order.getOrderNo())
                .paymentMethod(order.getPaymentMethod())
                .amount(order.getTotalAmount())
                .paymentUrl(initResult.getPaymentUrl())
                .status(saved.getStatus())
                .message(initResult.getMessage())
                .build();
    }

    @Transactional
    public PaymentCallbackResult processWebhook(String provider, Map<String, String> params) {
        log.info("Processing webhook from provider={}: params={}", provider, params);

        PaymentGateway gateway;
        if ("vnpay".equalsIgnoreCase(provider)) {
            gateway = gatewayFactory.getGateway(com.ecommerce.modules.order.entity.PaymentMethod.VNPAY);
        } else if ("momo".equalsIgnoreCase(provider)) {
            gateway = gatewayFactory.getGateway(com.ecommerce.modules.order.entity.PaymentMethod.MOMO);
        } else if ("zalopay".equalsIgnoreCase(provider)) {
            gateway = gatewayFactory.getGateway(com.ecommerce.modules.order.entity.PaymentMethod.ZALOPAY);
        } else {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }

        PaymentCallbackResult result = gateway.handleWebhook(params);
        if (!result.isSignatureValid()) {
            log.warn("Invalid webhook signature from provider={}", provider);
            throw new AppException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        String orderNo = result.getOrderNo();
        if (orderNo == null) {
            return result;
        }

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Idempotency: If already marked PAID, return without double-processing
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("Order {} is already PAID. Webhook ignored (Idempotent OK).", orderNo);
            return result;
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> Payment.builder()
                        .order(order)
                        .paymentMethod(order.getPaymentMethod())
                        .provider(provider.toUpperCase())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .amount(order.getTotalAmount())
                        .currency("VND")
                        .build());

        payment.setTransactionId(result.getTransactionId());
        payment.setRawResponse(result.getRawData());

        if (result.getStatus() == PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.CONFIRMED);

            // Deduct stock permanently
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getVariant() != null) {
                        inventoryService.deductStock(item.getVariant().getId(), item.getQuantity(), order.getOrderNo());
                    }
                }
            }

            statusLogRepository.save(OrderStatusLog.builder()
                    .order(order)
                    .previousStatus(OrderStatus.PENDING_PAYMENT)
                    .newStatus(OrderStatus.CONFIRMED)
                    .reason("Thanh toán online thành công qua " + provider.toUpperCase() + " (Mã GD: " + result.getTransactionId() + ")")
                    .changedBy("SYSTEM_WEBHOOK")
                    .build());

            log.info("Payment SUCCESS for order: orderNo={}, transId={}", orderNo, result.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);

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
                    .reason("Thanh toán online thất bại qua " + provider.toUpperCase())
                    .changedBy("SYSTEM_WEBHOOK")
                    .build());

            log.warn("Payment FAILED for order: orderNo={}", orderNo);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return result;
    }

    @Transactional
    public PaymentRefundResult refundPayment(Long paymentId, PaymentRefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Chỉ có thể hoàn tiền cho giao dịch đã thanh toán thành công.");
        }

        PaymentGateway gateway = gatewayFactory.getGateway(payment.getPaymentMethod());
        PaymentRefundResult refundResult = gateway.refund(payment, request.getAmount(), request.getReason());

        if (refundResult.isSuccess()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);

            statusLogRepository.save(OrderStatusLog.builder()
                    .order(order)
                    .previousStatus(order.getStatus())
                    .newStatus(OrderStatus.REFUNDED)
                    .reason("Hoàn tiền thành công số tiền " + request.getAmount() + " VNĐ. Lý do: " + request.getReason())
                    .changedBy("FINANCE_STAFF")
                    .build());
        }

        return refundResult;
    }
}
