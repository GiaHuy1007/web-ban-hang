package com.ecommerce.modules.payment.gateway;

import com.ecommerce.common.util.JsonUtils;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ZaloPayPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.ZALOPAY;
    }

    @Override
    public PaymentInitResult initiatePayment(Order order, String idempotencyKey) {
        String payUrl = "https://sandbox.zalopay.com.vn/pay?app_trans_id=" + order.getOrderNo();
        return PaymentInitResult.builder()
                .paymentUrl(payUrl)
                .transactionId("ZALO-" + order.getOrderNo())
                .status(PaymentStatus.PENDING)
                .message("Khởi tạo thanh toán ZaloPay thành công.")
                .build();
    }

    @Override
    public PaymentCallbackResult handleWebhook(Map<String, String> payload) {
        String returnCode = payload.get("return_code");
        String orderNo = payload.get("app_trans_id");
        String transId = payload.get("zp_trans_id");
        boolean isSuccess = "1".equals(returnCode);

        return PaymentCallbackResult.builder()
                .isSignatureValid(true)
                .orderNo(orderNo)
                .transactionId(transId)
                .status(isSuccess ? PaymentStatus.PAID : PaymentStatus.FAILED)
                .amount(BigDecimal.ZERO)
                .message(isSuccess ? "Thanh toán ZaloPay thành công." : "Thanh toán ZaloPay thất bại.")
                .rawData(JsonUtils.toJson(payload))
                .build();
    }

    @Override
    public PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason) {
        return PaymentRefundResult.builder()
                .success(true)
                .refundTransactionId("REFUND-ZALOPAY-" + System.currentTimeMillis())
                .refundedAmount(amount)
                .message("Hoàn tiền ZaloPay thành công.")
                .build();
    }
}
