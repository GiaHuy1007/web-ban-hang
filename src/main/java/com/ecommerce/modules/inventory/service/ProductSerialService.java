package com.ecommerce.modules.inventory.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.inventory.dto.SerialResponse;
import com.ecommerce.modules.inventory.entity.ProductSerial;
import com.ecommerce.modules.inventory.repository.ProductSerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSerialService {

    private final ProductSerialRepository serialRepository;

    @Transactional(readOnly = true)
    public SerialResponse getSerialInfo(String serialNumber) {
        ProductSerial serial = serialRepository.findBySerialNumber(serialNumber.trim())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy thông tin Serial/IMEI này."));
        return SerialResponse.from(serial);
    }

    @Transactional(readOnly = true)
    public List<SerialResponse> getSerialsByOrderItem(Long orderItemId) {
        return serialRepository.findByOrderItemId(orderItemId).stream()
                .map(SerialResponse::from)
                .collect(Collectors.toList());
    }
}
