package com.ecommerce.modules.inventory.repository;

import com.ecommerce.modules.inventory.entity.ProductSerial;
import com.ecommerce.modules.inventory.entity.SerialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {

    Optional<ProductSerial> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    List<ProductSerial> findByVariantIdAndStatus(Long variantId, SerialStatus status);

    List<ProductSerial> findByOrderItemId(Long orderItemId);
}
