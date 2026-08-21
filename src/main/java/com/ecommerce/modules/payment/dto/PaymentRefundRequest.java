package com.ecommerce.modules.payment.dto;

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
public class PaymentRefundRequest {

    @NotNull(message = "Số tiền hoàn không được để trống.")
    @DecimalMin(value = "1000.0", message = "Số tiền hoàn tối thiểu là 1.000 VNĐ.")
    private BigDecimal amount;

    @NotBlank(message = "Lý do hoàn tiền không được để trống.")
    private String reason;
}
