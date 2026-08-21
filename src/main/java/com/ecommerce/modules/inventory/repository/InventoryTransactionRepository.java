package com.ecommerce.modules.inventory.repository;

import com.ecommerce.modules.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction> findByVariantIdOrderByCreatedAtDesc(Long variantId, Pageable pageable);

    List<InventoryTransaction> findByReferenceId(String referenceId);
}
