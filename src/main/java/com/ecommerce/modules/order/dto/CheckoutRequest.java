package com.ecommerce.modules.order.dto;

import com.ecommerce.modules.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Địa chỉ nhận hàng không được để trống.")
    private Long addressId;

    @NotNull(message = "Phương thức thanh toán không được để trống.")
    private PaymentMethod paymentMethod;

    private String couponCode;

    private List<Long> cartItemIds;

    private String notes;
}
