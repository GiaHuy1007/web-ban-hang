package com.ecommerce.modules.order.repository;

import com.ecommerce.modules.order.entity.Order;
import com.ecommerce.modules.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i WHERE o.id = :id")
    Optional<Order> findDetailById(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime expiryTime);
}
