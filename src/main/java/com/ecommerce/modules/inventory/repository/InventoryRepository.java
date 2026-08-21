package com.ecommerce.modules.inventory.repository;

import com.ecommerce.modules.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantIdAndWarehouseId(Long variantId, Long warehouseId);

    List<Inventory> findByVariantId(Long variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.variant.id = :variantId AND i.warehouse.id = :warehouseId")
    Optional<Inventory> findByVariantIdAndWarehouseIdForUpdate(@Param("variantId") Long variantId, @Param("warehouseId") Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.variant.id = :variantId ORDER BY (i.quantityOnHand - i.quantityReserved) DESC")
    List<Inventory> findByVariantIdForUpdate(@Param("variantId") Long variantId);

    @Query("SELECT COALESCE(SUM(i.quantityOnHand - i.quantityReserved), 0) FROM Inventory i WHERE i.variant.id = :variantId")
    Integer getTotalAvailableStock(@Param("variantId") Long variantId);
}
