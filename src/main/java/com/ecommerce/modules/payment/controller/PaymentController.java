package com.ecommerce.modules.payment.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.payment.dto.PaymentInitResponse;
import com.ecommerce.modules.payment.dto.PaymentRefundRequest;
import com.ecommerce.modules.payment.gateway.PaymentRefundResult;
import com.ecommerce.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Processing", description = "Khởi tạo giao dịch thanh toán online (VNPay, MoMo, ZaloPay, QR) và hoàn tiền")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/initiate")
    @Operation(summary = "Khởi tạo giao dịch thanh toán (Nhận redirect URL sang cổng thanh toán hoặc QR code)")
    public ResponseEntity<ApiResponse<PaymentInitResponse>> initiatePayment(
            @PathVariable Long orderId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.initiatePayment(orderId, idempotencyKey)));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAuthority('PAYMENT_REFUND') or hasRole('SUPER_ADMIN') or hasRole('FINANCE')")
    @Operation(summary = "Finance/Admin: Thực hiện hoàn tiền cho giao dịch thanh toán")
    public ResponseEntity<ApiResponse<PaymentRefundResult>> refundPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentRefundRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.refundPayment(paymentId, request)));
    }
}
