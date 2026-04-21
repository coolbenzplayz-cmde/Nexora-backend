package org.example.nexora.grocery;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    
    private GroceryItem item;
    private int quantity;
    private Long userId;
    private Long itemId;
    private BigDecimal price;
    private String itemName;
    private String itemImage;

    public CartItem() {
    }

    public CartItem(GroceryItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.itemId = item != null ? item.getId() : null;
        this.price = item != null ? item.getPrice() : null;
        this.itemName = item != null ? item.getName() : null;
        this.itemImage = item != null ? item.getImageUrl() : null;
    }

    public GroceryItem getItem() {
        return item;
    }

    public void setItem(GroceryItem item) {
        this.item = item;
        if (item != null) {
            this.itemId = item.getId();
            this.price = item.getPrice();
            this.itemName = item.getName();
            this.itemImage = item.getImageUrl();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Additional methods needed by GroceryService
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }
}
