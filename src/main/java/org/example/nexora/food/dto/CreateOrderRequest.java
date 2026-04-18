package org.example.nexora.food.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private org.example.nexora.food.FoodOrder order;
    private List<org.example.nexora.food.OrderItem> items;
}
