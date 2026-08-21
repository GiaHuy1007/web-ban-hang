package com.ecommerce.modules.promotion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCouponRequest {

    @NotBlank(message = "Mã voucher không được để trống.")
    private String couponCode;

    @NotNull(message = "Tổng giá trị đơn hàng không được để trống.")
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng phải lớn hơn hoặc bằng 0.")
    private BigDecimal subtotalAmount;
}
