package com.ecommerce.modules.identity;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.identity.dto.LoginRequest;
import com.ecommerce.modules.identity.dto.RegisterRequest;
import com.ecommerce.modules.identity.dto.VerifyOtpRequest;
import com.ecommerce.modules.identity.entity.Role;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.entity.UserStatus;
import com.ecommerce.modules.identity.repository.RoleRepository;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.identity.security.JwtTokenProvider;
import com.ecommerce.modules.identity.security.OtpService;
import com.ecommerce.modules.identity.security.RedisTokenService;
import com.ecommerce.modules.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTokenService redisTokenService;

    @Mock
    private OtpService otpService;

    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    private User sampleUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 900000L);
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, tokenProvider, redisTokenService, otpService);
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 900000L);

        customerRole = Role.builder().id(1L).name("ROLE_CUSTOMER").build();
        sampleUser = User.builder()
                .id(100L)
                .email("test@example.com")
                .fullName("Nguyen Van A")
                .passwordHash("$2a$12$hashedPassword")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Collections.singletonList(customerRole)))
                .build();
    }

    @Test
    void register_ShouldSucceed_WhenEmailNotExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .password("Password123@")
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(otpService.generateOtp(any())).thenReturn("123456");

        String response = authService.register(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(otpService, times(1)).generateOtp(request.getEmail());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Nguyen Van A")
                .password("Password123@")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ErrorCode.USER_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void login_ShouldSucceed_WhenCredentialsValid() {
        LoginRequest request = LoginRequest.builder()
                .username("test@example.com")
                .password("Password123@")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password123@", sampleUser.getPasswordHash())).thenReturn(true);
        when(redisTokenService.createRefreshToken(any())).thenReturn("mock-refresh-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordWrong() {
        LoginRequest request = LoginRequest.builder()
                .username("test@example.com")
                .password("WrongPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", sampleUser.getPasswordHash())).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> authService.login(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }
}
