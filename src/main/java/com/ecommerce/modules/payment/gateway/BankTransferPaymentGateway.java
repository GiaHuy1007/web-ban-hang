package com.ecommerce.modules.payment.gateway;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class BankTransferPaymentGateway implements PaymentGateway {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public PaymentInitResult initiatePayment(Order order, String idempotencyKey) {
        // QR Code VietQR standard URL
        String vietQrUrl = "https://img.vietqr.io/image/MB-0901234567-compact2.png?amount="
                + order.getTotalAmount().longValue()
                + "&addInfo=" + order.getOrderNo()
                + "&accountName=CONG%20TY%20DIEN%20TU%20VIET";

        return PaymentInitResult.builder()
                .paymentUrl(vietQrUrl)
                .transactionId("BANK-" + order.getOrderNo())
                .status(PaymentStatus.PENDING)
                .message("Vui lòng quét mã QR VietQR hoặc chuyển khoản với cú pháp: " + order.getOrderNo())
                .build();
    }

    @Override
    public PaymentCallbackResult handleWebhook(Map<String, String> payload) {
        return PaymentCallbackResult.builder()
                .isSignatureValid(true)
                .status(PaymentStatus.PAID)
                .message("Xác nhận chuyển khoản ngân hàng thành công.")
                .build();
    }

    @Override
    public PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason) {
        return PaymentRefundResult.builder()
                .success(true)
                .refundTransactionId("REFUND-BANK-" + System.currentTimeMillis())
                .refundedAmount(amount)
                .message("Đã lập lệnh chuyển khoản hoàn tiền ngân hàng.")
                .build();
    }
}
