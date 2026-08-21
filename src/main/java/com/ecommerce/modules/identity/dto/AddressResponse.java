package com.ecommerce.modules.identity.dto;

import com.ecommerce.modules.identity.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String streetAddress;
    private String ward;
    private String district;
    private String city;
    private String fullAddress;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    public static AddressResponse from(UserAddress address) {
        StringBuilder sb = new StringBuilder(address.getStreetAddress());
        if (address.getWard() != null && !address.getWard().isBlank()) {
            sb.append(", ").append(address.getWard());
        }
        sb.append(", ").append(address.getDistrict())
          .append(", ").append(address.getCity());

        return AddressResponse.builder()
                .id(address.getId())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .streetAddress(address.getStreetAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .fullAddress(sb.toString())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .build();
    }
}
