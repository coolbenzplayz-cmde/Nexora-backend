package org.example.nexora.grocery;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "grocery_items")
public class GroceryItem extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private String storeName;

    @Column
    private Integer stockQuantity;

    @Column
    private String unit;

    @Column
    private Boolean inStock = true;

    @Column
    private Double rating;

    @Column
    private Integer reviewCount;
}
