package com.ecommerce.modules.promotion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "Tên chiến dịch Flash Sale không được để trống.")
    private String name;

    private String description;
    private String bannerUrl;

    @NotNull(message = "Thời gian bắt đầu không được để trống.")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống.")
    private LocalDateTime endTime;

    @Builder.Default
    private Boolean isActive = true;

    @Valid
    @Builder.Default
    private List<PromotionProductDto> products = new ArrayList<>();
}
