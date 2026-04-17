package org.example.nexora.grocery;

import java.io.Serializable;

public class CartItem implements Serializable {
    
    private GroceryItem item;
    private int quantity;

    public CartItem() {
    }

    public CartItem(GroceryItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public GroceryItem getItem() {
        return item;
    }

    public void setItem(GroceryItem item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
