package com.ecommerce.modules.identity.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.modules.identity.dto.UpdateProfileRequest;
import com.ecommerce.modules.identity.dto.UserProfileResponse;
import com.ecommerce.modules.identity.entity.UserStatus;
import com.ecommerce.modules.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User & Profile Management", description = "Quản lý thông tin tài khoản cá nhân và quản trị người dùng")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUserProfile()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(request)));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Danh sách tất cả người dùng hệ thống")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(userService.getAllUsers(pageable))));
    }

    @PatchMapping("/admin/{userId}/status")
    @PreAuthorize("hasAuthority('USER_WRITE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Admin: Cập nhật trạng thái người dùng (ACTIVE, BLOCKED)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateUserStatus(userId, status)));
    }

    @PostMapping("/admin/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Super Admin: Phân quyền vai trò cho nhân sự")
    public ResponseEntity<ApiResponse<UserProfileResponse>> assignRoles(
            @PathVariable Long userId,
            @RequestBody List<String> roleNames) {
        return ResponseEntity.ok(ApiResponse.ok(userService.assignRolesToUser(userId, roleNames)));
    }
}
