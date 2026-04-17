package org.example.nexora.food.dto;

import java.util.List;

public class CreateOrderRequest {
    private FoodOrder order;
    private List<OrderItem> items;

    public FoodOrder getOrder() {
        return order;
    }

    public void setOrder(FoodOrder order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}