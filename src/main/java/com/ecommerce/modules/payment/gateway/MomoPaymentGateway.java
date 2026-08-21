package com.ecommerce.modules.payment.gateway;

import com.ecommerce.common.util.JsonUtils;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
public class MomoPaymentGateway implements PaymentGateway {

    @Value("${app.payment.momo.partner-code:TEST_MOMO}")
    private String partnerCode;

    @Value("${app.payment.momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.MOMO;
    }

    @Override
    public PaymentInitResult initiatePayment(Order order, String idempotencyKey) {
        String payUrl = "https://test-payment.momo.vn/v2/gateway/pay?partnerCode=" + partnerCode + "&orderId=" + order.getOrderNo();
        return PaymentInitResult.builder()
                .paymentUrl(payUrl)
                .transactionId("MOMO-" + order.getOrderNo())
                .status(PaymentStatus.PENDING)
                .message("Khởi tạo liên kết ví MoMo thành công.")
                .build();
    }

    @Override
    public PaymentCallbackResult handleWebhook(Map<String, String> payload) {
        String resultCode = payload.get("resultCode");
        String orderNo = payload.get("orderId");
        String transId = payload.get("transId");
        String amountStr = payload.get("amount");
        BigDecimal amount = (amountStr != null) ? new BigDecimal(amountStr) : BigDecimal.ZERO;

        boolean isSuccess = "0".equals(resultCode);

        return PaymentCallbackResult.builder()
                .isSignatureValid(true)
                .orderNo(orderNo)
                .transactionId(transId)
                .status(isSuccess ? PaymentStatus.PAID : PaymentStatus.FAILED)
                .amount(amount)
                .message(isSuccess ? "Thanh toán MoMo thành công." : "Thanh toán MoMo thất bại.")
                .rawData(JsonUtils.toJson(payload))
                .build();
    }

    @Override
    public PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason) {
        return PaymentRefundResult.builder()
                .success(true)
                .refundTransactionId("REFUND-MOMO-" + System.currentTimeMillis())
                .refundedAmount(amount)
                .message("Hoàn tiền ví MoMo thành công.")
                .build();
    }
}
