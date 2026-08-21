package com.ecommerce.modules.identity.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.identity.dto.*;
import com.ecommerce.modules.identity.entity.Role;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.entity.UserStatus;
import com.ecommerce.modules.identity.repository.RoleRepository;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.identity.security.JwtTokenProvider;
import com.ecommerce.modules.identity.security.OtpService;
import com.ecommerce.modules.identity.security.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RedisTokenService redisTokenService;
    private final OtpService otpService;

    @Value("${app.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS, "Email này đã được đăng ký trong hệ thống.");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS, "Số điện thoại này đã được đăng ký.");
        }

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Khách hàng").build()));

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING)
                .roles(new HashSet<>(Collections.singletonList(customerRole)))
                .build();

        userRepository.save(user);

        // Generate OTP and send via Email/SMS
        String otp = otpService.generateOtp(user.getEmail());
        log.info("OTP generated for registration: user={}, otp={}", user.getEmail(), otp);
        return "Mã OTP xác thực đã được gửi đến " + user.getEmail() + ". Vui lòng nhập OTP để hoàn tất đăng ký.";
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();
        otpService.verifyOtp(identifier, request.getOtp().trim());

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVATED);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        return generateAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken().trim();
        Long userId = redisTokenService.validateRefreshToken(refreshToken);

        if (userId == null) {
            throw new AppException(ErrorCode.TOKEN_INVALID, "Refresh token không hợp lệ hoặc đã hết hạn.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Token Rotation: revoke old, generate new refresh token
        redisTokenService.revokeRefreshToken(refreshToken);
        String newRefreshToken = redisTokenService.createRefreshToken(user.getId());

        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissionNames = user.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleNames, permissionNames);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .user(UserProfileResponse.from(user))
                .build();
    }

    public void logout(String authHeader, String refreshToken) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            long remainingTtl = tokenProvider.getRemainingExpirationMs(accessToken);
            redisTokenService.blacklistAccessToken(accessToken, remainingTtl);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTokenService.revokeRefreshToken(refreshToken.trim());
        }
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy tài khoản với email này."));

        String otp = otpService.generateOtp(user.getEmail());
        log.info("OTP generated for forgot password: user={}, otp={}", user.getEmail(), otp);
        return "Mã OTP đặt lại mật khẩu đã được gửi đến " + user.getEmail();
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        otpService.verifyOtp(email, request.getOtp().trim());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all existing refresh tokens for security
        redisTokenService.revokeAllUserRefreshTokens(user.getId());

        return "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại với mật khẩu mới.";
    }

    private AuthResponse generateAuthResponse(User user) {
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissionNames = user.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleNames, permissionNames);
        String refreshToken = redisTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(accessTokenExpirationMs)
                .user(UserProfileResponse.from(user))
                .build();
    }
}
