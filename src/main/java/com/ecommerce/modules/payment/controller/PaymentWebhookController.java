package com.ecommerce.modules.payment.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.payment.gateway.PaymentCallbackResult;
import com.ecommerce.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
@Tag(name = "Payment Webhooks", description = "Endpoint tiếp nhận callback / IPN từ các cổng thanh toán (Xác thực chữ ký số HMAC-SHA512)")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/{provider}")
    @Operation(summary = "Tiếp nhận webhook thông báo thanh toán (POST)")
    public ResponseEntity<ApiResponse<PaymentCallbackResult>> handlePostWebhook(
            @PathVariable String provider,
            @RequestParam Map<String, String> allParams,
            @RequestBody(required = false) Map<String, String> body) {
        if (body != null) {
            allParams.putAll(body);
        }
        PaymentCallbackResult result = paymentService.processWebhook(provider, allParams);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{provider}")
    @Operation(summary = "Tiếp nhận callback redirect trả lời từ người dùng / cổng thanh toán (GET)")
    public ResponseEntity<ApiResponse<PaymentCallbackResult>> handleGetCallback(
            @PathVariable String provider,
            @RequestParam Map<String, String> allParams) {
        PaymentCallbackResult result = paymentService.processWebhook(provider, allParams);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
