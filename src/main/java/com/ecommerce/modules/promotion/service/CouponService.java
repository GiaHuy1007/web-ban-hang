package com.ecommerce.modules.promotion.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.repository.UserRepository;
import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.promotion.dto.CouponRequest;
import com.ecommerce.modules.promotion.dto.CouponResponse;
import com.ecommerce.modules.promotion.dto.ValidateCouponRequest;
import com.ecommerce.modules.promotion.dto.ValidateCouponResponse;
import com.ecommerce.modules.promotion.entity.Coupon;
import com.ecommerce.modules.promotion.entity.CouponUsage;
import com.ecommerce.modules.promotion.entity.DiscountType;
import com.ecommerce.modules.promotion.repository.CouponRepository;
import com.ecommerce.modules.promotion.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ValidateCouponResponse validateCoupon(ValidateCouponRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String code = request.getCouponCode().trim().toUpperCase();

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getIsActive()) {
            throw new AppException(ErrorCode.COUPON_EXPIRED, "Mã giảm giá này hiện không khả dụng.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new AppException(ErrorCode.COUPON_EXPIRED, "Mã giảm giá đã hết hạn hoặc chưa đến ngày bắt đầu.");
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppException(ErrorCode.COUPON_USAGE_LIMIT_REACHED, "Mã giảm giá đã hết lượt sử dụng.");
        }

        long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
        if (userUsageCount >= coupon.getUsagePerUser()) {
            throw new AppException(ErrorCode.COUPON_USER_LIMIT_REACHED, "Bạn đã sử dụng hết số lần cho phép (" + coupon.getUsagePerUser() + " lần) của mã này.");
        }

        if (request.getSubtotalAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new AppException(ErrorCode.COUPON_MIN_ORDER_NOT_MET, "Đơn hàng tối thiểu để áp dụng mã là " + coupon.getMinOrderAmount() + " VNĐ.");
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            discount = request.getSubtotalAmount()
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue().min(request.getSubtotalAmount());
        }

        BigDecimal finalAmount = request.getSubtotalAmount().subtract(discount).max(BigDecimal.ZERO);

        return ValidateCouponResponse.builder()
                .isValid(true)
                .couponCode(coupon.getCode())
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .message("Áp dụng mã giảm giá thành công. Bạn được giảm " + discount + " VNĐ.")
                .build();
    }

    @Transactional
    public void recordCouponUsage(Coupon coupon, User user, Order order, BigDecimal discountAmount) {
        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountAmount(discountAmount)
                .build();
        couponUsageRepository.save(usage);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream().map(CouponResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (couponRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã voucher '" + code + "' đã tồn tại.");
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usagePerUser(request.getUsagePerUser())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive())
                .build();

        return CouponResponse.from(couponRepository.save(coupon));
    }
}
