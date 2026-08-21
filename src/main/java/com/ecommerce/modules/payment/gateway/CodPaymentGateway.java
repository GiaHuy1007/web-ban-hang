package com.ecommerce.modules.payment.gateway;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CodPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentInitResult initiatePayment(Order order, String idempotencyKey) {
        return PaymentInitResult.builder()
                .paymentUrl(null)
                .transactionId("COD-" + order.getOrderNo())
                .status(PaymentStatus.PENDING)
                .message("Thanh toán khi nhận hàng (COD).")
                .build();
    }

    @Override
    public PaymentCallbackResult handleWebhook(Map<String, String> payload) {
        return PaymentCallbackResult.builder()
                .isSignatureValid(true)
                .status(PaymentStatus.PAID)
                .message("Xác nhận đã thanh toán tiền mặt khi giao hàng thành công.")
                .build();
    }

    @Override
    public PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason) {
        return PaymentRefundResult.builder()
                .success(true)
                .refundTransactionId("REFUND-COD-" + System.currentTimeMillis())
                .refundedAmount(amount)
                .message("Hoàn tiền mặt/chuyển khoản cho đơn COD.")
                .build();
    }
}
