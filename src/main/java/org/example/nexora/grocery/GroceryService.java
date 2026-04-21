package org.example.nexora.grocery;

import org.example.nexora.common.BusinessException;
import org.example.nexora.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroceryService {

    private final GroceryRepository groceryRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public GroceryService(GroceryRepository groceryRepository, 
                          CartRepository cartRepository,
                          UserRepository userRepository) {
        this.groceryRepository = groceryRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    public GroceryItem createItem(GroceryItem item) {
        return groceryRepository.save(item);
    }

    public Page<GroceryItem> getAllItems(Pageable pageable) {
        return groceryRepository.findAll(pageable);
    }

    public Page<GroceryItem> getItemsByStore(Long storeId, Pageable pageable) {
        return groceryRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);
    }

    public Page<GroceryItem> getItemsByCategory(String category, Pageable pageable) {
        return groceryRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
    }

    public Page<GroceryItem> searchItems(String query, Pageable pageable) {
        return groceryRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(query, pageable);
    }

    public GroceryItem getItemById(Long itemId) {
        return groceryRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Grocery item not found"));
    }

    public List<String> getAllCategories() {
        return groceryRepository.findAllCategories();
    }

    public BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    public GroceryItem updateItem(Long id, GroceryItem item) {
        GroceryItem existingItem = getItemById(id);
        
        if (item.getName() != null) existingItem.setName(item.getName());
        if (item.getDescription() != null) existingItem.setDescription(item.getDescription());
        if (item.getPrice() != null) existingItem.setPrice(item.getPrice());
        if (item.getCategory() != null) existingItem.setCategory(item.getCategory());
        if (item.getImageUrl() != null) existingItem.setImageUrl(item.getImageUrl());
        if (item.getIsAvailable() != null) existingItem.setIsAvailable(item.getIsAvailable());
        existingItem.setUpdatedAt(LocalDateTime.now());
        
        return groceryRepository.save(existingItem);
    }

    public void deleteItem(Long id, Long userId) {
        GroceryItem item = getItemById(id);
        groceryRepository.delete(item);
    }

    // Cart operations
    public CartItem addToCart(Long userId, Long itemId, int quantity) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        GroceryItem item = getItemById(itemId);
        
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setItemId(itemId);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(item.getPrice());
        cartItem.setItemName(item.getName());
        cartItem.setItemImage(item.getImageUrl());
        
        return cartRepository.save(cartItem);
    }

    public List<CartItem> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("Cart item not found"));
        
        if (!cartItem.getUserId().equals(userId)) {
            throw new BusinessException("Forbidden", "FORBIDDEN");
        }
        
        cartRepository.delete(cartItem);
    }

    public void clearCart(Long userId) {
        List<CartItem> items = getCartItems(userId);
        cartRepository.deleteAll(items);
    }
}
