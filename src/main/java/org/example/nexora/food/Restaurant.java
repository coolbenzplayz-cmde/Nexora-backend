package org.example.nexora.food;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.nexora.common.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Restaurant entity for food delivery
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurants")
public class Restaurant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private String cuisine;

    private String address;

    private String location;

    @Column(name = "delivery_time")
    private Integer deliveryTime;

    @Column(name = "min_order", precision = 10, scale = 2)
    private BigDecimal minOrder;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    private Double rating;

    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status = RestaurantStatus.OPEN;

    public enum RestaurantStatus {
        OPEN,
        CLOSED,
        TEMPORARILY_CLOSED
    }
}