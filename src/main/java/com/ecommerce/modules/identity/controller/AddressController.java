package com.ecommerce.modules.identity.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.modules.identity.dto.AddressRequest;
import com.ecommerce.modules.identity.dto.AddressResponse;
import com.ecommerce.modules.identity.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/addresses")
@RequiredArgsConstructor
@Tag(name = "Customer Address Book", description = "Quản lý danh sách sổ địa chỉ nhận hàng của khách hàng")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ nhận hàng của người dùng hiện tại")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        return ResponseEntity.ok(ApiResponse.ok(addressService.getUserAddresses()));
    }

    @PostMapping
    @Operation(summary = "Thêm mới địa chỉ nhận hàng")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(addressService.createAddress(request)));
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Cập nhật địa chỉ nhận hàng")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(addressService.updateAddress(addressId, request)));
    }

    @PatchMapping("/{addressId}/default")
    @Operation(summary = "Đặt làm địa chỉ nhận hàng mặc định")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(@PathVariable Long addressId) {
        addressService.setDefaultAddress(addressId);
        return ResponseEntity.ok(ApiResponse.ok("Đã đặt làm địa chỉ mặc định."));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Xóa địa chỉ nhận hàng")
    public ResponseEntity<ApiResponse<String>> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa địa chỉ thành công."));
    }
}
