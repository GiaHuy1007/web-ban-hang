package com.ecommerce.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRequest {

    @NotBlank(message = "Lý do hủy đơn không được để trống.")
    private String reason;
}
