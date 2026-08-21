package com.ecommerce.modules.identity.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.common.util.SecurityUtils;
import com.ecommerce.modules.identity.dto.AddressRequest;
import com.ecommerce.modules.identity.dto.AddressResponse;
import com.ecommerce.modules.identity.entity.User;
import com.ecommerce.modules.identity.entity.UserAddress;
import com.ecommerce.modules.identity.repository.UserAddressRepository;
import com.ecommerce.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(currentUserId).stream()
                .map(AddressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<UserAddress> existingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(currentUserId);
        boolean shouldBeDefault = existingAddresses.isEmpty() || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault && !existingAddresses.isEmpty()) {
            addressRepository.resetDefaultAddressForUser(currentUserId);
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .recipientName(request.getRecipientName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .streetAddress(request.getStreetAddress().trim())
                .ward(request.getWard() != null ? request.getWard().trim() : null)
                .district(request.getDistrict().trim())
                .city(request.getCity().trim())
                .isDefault(shouldBeDefault)
                .build();

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(currentUserId);
            address.setIsDefault(true);
        }

        address.setRecipientName(request.getRecipientName().trim());
        address.setPhoneNumber(request.getPhoneNumber().trim());
        address.setStreetAddress(request.getStreetAddress().trim());
        address.setWard(request.getWard() != null ? request.getWard().trim() : null);
        address.setDistrict(request.getDistrict().trim());
        address.setCity(request.getCity().trim());

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public void setDefaultAddress(Long addressId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        addressRepository.resetDefaultAddressForUser(currentUserId);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        UserAddress address = addressRepository.findByIdAndUserId(addressId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        if (wasDefault) {
            // Set first remaining address as default
            List<UserAddress> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(currentUserId);
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }
}
