package com.ecommerce.modules.inventory.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ErrorCode;
import com.ecommerce.modules.inventory.dto.WarehouseRequest;
import com.ecommerce.modules.inventory.dto.WarehouseResponse;
import com.ecommerce.modules.inventory.entity.Warehouse;
import com.ecommerce.modules.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream().map(WarehouseResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return WarehouseResponse.from(warehouse);
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã kho '" + request.getCode() + "' đã tồn tại.");
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .city(request.getCity().trim())
                .isActive(request.getIsActive())
                .build();

        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        warehouse.setName(request.getName().trim());
        warehouse.setAddress(request.getAddress().trim());
        warehouse.setCity(request.getCity().trim());
        warehouse.setIsActive(request.getIsActive());

        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }
}
