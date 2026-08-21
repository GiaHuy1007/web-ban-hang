package com.ecommerce.modules.identity.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.identity.dto.*;
import com.ecommerce.modules.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Authorization", description = "Đăng ký, xác thực OTP, đăng nhập, refresh token, quên mật khẩu")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới (Gửi OTP qua email)")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực mã OTP và kích hoạt tài khoản")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng Email/SĐT và mật khẩu")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Lấy Access Token mới bằng Refresh Token (Token Rotation)")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất (Thu hồi refresh token và đưa access token vào blacklist)")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        authService.logout(authHeader, refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu gửi OTP đặt lại mật khẩu")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String result = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Xác thực OTP và đặt lại mật khẩu mới")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
