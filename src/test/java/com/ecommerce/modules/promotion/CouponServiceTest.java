package com.ecommerce.modules.promotion;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.promotion.dto.ValidateCouponRequest;
import com.ecommerce.modules.promotion.dto.ValidateCouponResponse;
import com.ecommerce.modules.promotion.entity.Coupon;
import com.ecommerce.modules.promotion.entity.DiscountType;
import com.ecommerce.modules.promotion.repository.CouponRepository;
import com.ecommerce.modules.promotion.repository.CouponUsageRepository;
import com.ecommerce.modules.promotion.service.CouponService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponService couponService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private Coupon percentCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(100L);

        percentCoupon = Coupon.builder()
                .id(1L)
                .code("SALE10")
                .title("Giảm 10% tối đa 500k")
                .discountType(DiscountType.PERCENT)
                .discountValue(BigDecimal.valueOf(10)) // 10%
                .minOrderAmount(BigDecimal.valueOf(1000000)) // Min 1 triệu
                .maxDiscountAmount(BigDecimal.valueOf(500000)) // Max 500k
                .usageLimit(100)
                .usagePerUser(2)
                .usedCount(5)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .isActive(true)
                .build();

        fixedCoupon = Coupon.builder()
                .id(2L)
                .code("GIAM100K")
                .title("Giảm 100k")
                .discountType(DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(100000))
                .minOrderAmount(BigDecimal.valueOf(500000))
                .usageLimit(50)
                .usagePerUser(1)
                .usedCount(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void validateCoupon_PercentDiscount_ShouldCalculateCorrectly() {
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 100L)).thenReturn(0L);

        ValidateCouponRequest request = ValidateCouponRequest.builder()
                .couponCode("SALE10")
                .subtotalAmount(BigDecimal.valueOf(3000000)) // 3 triệu -> 10% = 300k (< 500k max)
                .build();

        ValidateCouponResponse response = couponService.validateCoupon(request);

        assertTrue(response.isValid());
        assertEquals(0, BigDecimal.valueOf(300000.00).compareTo(response.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(2700000.00).compareTo(response.getFinalAmount()));
    }

    @Test
    void validateCoupon_PercentDiscount_ShouldCapAtMaxDiscount() {
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 100L)).thenReturn(0L);

        ValidateCouponRequest request = ValidateCouponRequest.builder()
                .couponCode("SALE10")
                .subtotalAmount(BigDecimal.valueOf(10000000)) // 10 triệu -> 10% = 1 triệu -> capped at 500k
                .build();

        ValidateCouponResponse response = couponService.validateCoupon(request);

        assertTrue(response.isValid());
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(response.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(9500000).compareTo(response.getFinalAmount()));
    }

    @Test
    void validateCoupon_ShouldThrow_WhenMinOrderAmountNotMet() {
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 100L)).thenReturn(0L);

        ValidateCouponRequest request = ValidateCouponRequest.builder()
                .couponCode("SALE10")
                .subtotalAmount(BigDecimal.valueOf(500000)) // < 1 triệu min
                .build();

        AppException ex = assertThrows(AppException.class, () -> couponService.validateCoupon(request));
        assertEquals(ErrorCode.COUPON_MIN_ORDER_NOT_MET, ex.getErrorCode());
    }

    @Test
    void validateCoupon_ShouldThrow_WhenUserUsageLimitReached() {
        when(couponRepository.findByCode("SALE10")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 100L)).thenReturn(2L); // Max is 2

        ValidateCouponRequest request = ValidateCouponRequest.builder()
                .couponCode("SALE10")
                .subtotalAmount(BigDecimal.valueOf(2000000))
                .build();

        AppException ex = assertThrows(AppException.class, () -> couponService.validateCoupon(request));
        assertEquals(ErrorCode.COUPON_USER_LIMIT_REACHED, ex.getErrorCode());
    }
}
