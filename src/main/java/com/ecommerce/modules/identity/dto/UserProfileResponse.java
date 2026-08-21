package com.ecommerce.modules.identity.dto;

import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private UserStatus status;
    private Set<String> roles;
    private Set<String> permissions;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        Set<String> permissionNames = user.getRoles().stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roles(roleNames)
                .permissions(permissionNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
