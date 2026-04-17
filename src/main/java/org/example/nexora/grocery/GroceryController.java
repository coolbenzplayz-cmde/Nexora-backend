package org.example.nexora.grocery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grocery")
public class GroceryController {

    @Autowired
    private GroceryService groceryService;

    @GetMapping("/items")
    public ResponseEntity<Page<GroceryItem>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(groceryService.getAllItems(pageable));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<GroceryItem> getItemById(@PathVariable Long id) {
        return groceryService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/items/category/{category}")
    public ResponseEntity<Page<GroceryItem>> getItemsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groceryService.getItemsByCategory(category, pageable));
    }

    @GetMapping("/items/store/{storeName}")
    public ResponseEntity<Page<GroceryItem>> getItemsByStore(
            @PathVariable String storeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groceryService.getItemsByStore(storeName, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GroceryItem>> searchItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groceryService.searchItems(keyword, pageable));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(groceryService.getAllCategories());
    }

    @PostMapping("/items")
    public ResponseEntity<GroceryItem> createItem(@RequestBody GroceryItem item) {
        return ResponseEntity.ok(groceryService.createItem(item));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<GroceryItem> updateItem(
            @PathVariable Long id, 
            @RequestBody GroceryItem item) {
        return ResponseEntity.ok(groceryService.updateItem(id, item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        groceryService.deleteItem(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/total")
    public ResponseEntity<java.math.BigDecimal> calculateCartTotal(@RequestBody List<CartItem> cartItems) {
        return ResponseEntity.ok(groceryService.calculateCartTotal(cartItems));
    }
}
