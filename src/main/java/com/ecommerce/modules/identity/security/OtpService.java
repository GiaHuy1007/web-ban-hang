package com.ecommerce.modules.identity.security;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    private static final String OTP_RATE_LIMIT_PREFIX = "otp_rate_limit:";

    @Value("${app.otp.expiration-minutes:5}")
    private long expirationMinutes;

    @Value("${app.otp.max-retry-attempts:5}")
    private int maxRetryAttempts;

    @Value("${app.otp.rate-limit-per-hour:5}")
    private int rateLimitPerHour;

    public String generateOtp(String targetEmailOrPhone) {
        checkRateLimit(targetEmailOrPhone);

        int otpNum = 100000 + RANDOM.nextInt(900000);
        String otp = String.valueOf(otpNum);

        String otpKey = OTP_KEY_PREFIX + targetEmailOrPhone;
        String attemptsKey = OTP_ATTEMPTS_PREFIX + targetEmailOrPhone;

        redisTemplate.opsForValue().set(otpKey, otp, expirationMinutes, TimeUnit.MINUTES);
        redisTemplate.delete(attemptsKey); // reset attempts on new OTP

        log.info("Generated OTP for {}: {} (Expires in {} mins)", targetEmailOrPhone, otp, expirationMinutes);
        return otp;
    }

    public boolean verifyOtp(String targetEmailOrPhone, String inputOtp) {
        String otpKey = OTP_KEY_PREFIX + targetEmailOrPhone;
        String attemptsKey = OTP_ATTEMPTS_PREFIX + targetEmailOrPhone;

        Object savedOtpObj = redisTemplate.opsForValue().get(otpKey);
        if (savedOtpObj == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts > maxRetryAttempts) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            throw new AppException(ErrorCode.OTP_INVALID, "Bạn đã nhập sai quá số lần quy định. Vui lòng yêu cầu mã OTP mới.");
        }

        String savedOtp = savedOtpObj.toString();
        if (!savedOtp.equals(inputOtp)) {
            throw new AppException(ErrorCode.OTP_INVALID, "Mã OTP không chính xác. Số lần thử còn lại: " + (maxRetryAttempts - (attempts == null ? 1 : attempts.intValue())));
        }

        // OTP hợp lệ -> dọn dẹp
        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);
        return true;
    }

    private void checkRateLimit(String target) {
        String rateLimitKey = OTP_RATE_LIMIT_PREFIX + target;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count != null && count == 1) {
            redisTemplate.expire(rateLimitKey, 1, TimeUnit.HOURS);
        } else if (count != null && count > rateLimitPerHour) {
            throw new AppException(ErrorCode.OTP_RATE_LIMIT);
        }
    }
}
