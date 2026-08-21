package com.ecommerce.modules.payment.gateway;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {

    PaymentMethod getPaymentMethod();

    PaymentInitResult initiatePayment(Order order, String idempotencyKey);

    PaymentCallbackResult handleWebhook(Map<String, String> payload);

    PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason);
}
