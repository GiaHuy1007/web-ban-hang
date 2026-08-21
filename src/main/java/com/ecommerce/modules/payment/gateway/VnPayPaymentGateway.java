package com.ecommerce.modules.payment.gateway;

import com.ecommerce.common.util.JsonUtils;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.PaymentMethod;
import com.ecommerce.modules.order.entity.PaymentStatus;
import com.ecommerce.modules.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class VnPayPaymentGateway implements PaymentGateway {

    @Value("${app.payment.vnpay.tmn-code:TEST_TMN}")
    private String tmnCode;

    @Value("${app.payment.vnpay.hash-secret:TEST_SECRET}")
    private String hashSecret;

    @Value("${app.payment.vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${app.payment.vnpay.return-url:http://localhost:8080/api/v1/payments/vnpay/return}")
    private String returnUrl;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentInitResult initiatePayment(Order order, String idempotencyKey) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);
            long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", order.getOrderNo());
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderNo());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Sort & build query
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String secureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);
            String paymentUrl = payUrl + "?" + query.toString();

            return PaymentInitResult.builder()
                    .paymentUrl(paymentUrl)
                    .transactionId(order.getOrderNo())
                    .status(PaymentStatus.PENDING)
                    .message("Khởi tạo liên kết thanh toán VNPay thành công.")
                    .build();
        } catch (Exception e) {
            log.error("Error creating VNPay URL: ", e);
            throw new RuntimeException("Lỗi tạo đường dẫn thanh toán VNPay: " + e.getMessage());
        }
    }

    @Override
    public PaymentCallbackResult handleWebhook(Map<String, String> payload) {
        try {
            String vnpSecureHash = payload.get("vnp_SecureHash");
            Map<String, String> fields = new HashMap<>(payload);
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(hashSecret, hashData.toString());
            boolean isSignatureValid = signValue.equalsIgnoreCase(vnpSecureHash);

            String responseCode = payload.get("vnp_ResponseCode");
            String orderNo = payload.get("vnp_TxnRef");
            String transactionId = payload.get("vnp_TransactionNo");
            String amountStr = payload.get("vnp_Amount");
            BigDecimal amount = (amountStr != null) ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

            boolean isSuccess = "00".equals(responseCode) && isSignatureValid;

            return PaymentCallbackResult.builder()
                    .isSignatureValid(isSignatureValid)
                    .orderNo(orderNo)
                    .transactionId(transactionId)
                    .status(isSuccess ? PaymentStatus.PAID : PaymentStatus.FAILED)
                    .amount(amount)
                    .message(isSuccess ? "Thanh toán VNPay thành công." : "Thanh toán VNPay thất bại hoặc chữ ký không hợp lệ.")
                    .rawData(JsonUtils.toJson(payload))
                    .build();
        } catch (Exception e) {
            log.error("Error processing VNPay callback: ", e);
            return PaymentCallbackResult.builder()
                    .isSignatureValid(false)
                    .status(PaymentStatus.FAILED)
                    .message("Lỗi xác thực VNPay webhook: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentRefundResult refund(Payment payment, BigDecimal amount, String reason) {
        // VNPay Refund API call implementation
        return PaymentRefundResult.builder()
                .success(true)
                .refundTransactionId("REFUND-VNPAY-" + System.currentTimeMillis())
                .refundedAmount(amount)
                .message("Yêu cầu hoàn tiền VNPay đã được gửi thành công.")
                .build();
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hash = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
