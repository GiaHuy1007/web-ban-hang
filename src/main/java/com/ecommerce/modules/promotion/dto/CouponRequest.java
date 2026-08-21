package com.ecommerce.modules.promotion.dto;

import com.ecommerce.modules.promotion.entity.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {

    @NotBlank(message = "Mã voucher không được để trống.")
    private String code;

    @NotBlank(message = "Tiêu đề không được để trống.")
    private String title;

    private String description;

    @NotNull(message = "Loại giảm giá (PERCENT hoặc FIXED) không được để trống.")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0.")
    private BigDecimal discountValue;

    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    private BigDecimal maxDiscountAmount;

    @Builder.Default
    private Integer usageLimit = 100;

    @Builder.Default
    private Integer usagePerUser = 1;

    @NotNull(message = "Ngày bắt đầu không được để trống.")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống.")
    private LocalDateTime endDate;

    @Builder.Default
    private Boolean isActive = true;
}
