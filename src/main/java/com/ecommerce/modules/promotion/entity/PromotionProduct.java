package com.ecommerce.modules.promotion.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.modules.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "promotion_products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_promotion_variant", columnNames = {"promotion_id", "variant_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "promotional_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal promotionalPrice;

    @Column(name = "quantity_limit", nullable = false)
    @Builder.Default
    private Integer quantityLimit = 0;

    @Column(name = "quantity_sold", nullable = false)
    @Builder.Default
    private Integer quantitySold = 0;
}
