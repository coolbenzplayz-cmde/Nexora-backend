package org.example.nexora.marketplace;

import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody Product product,
            @RequestParam Long sellerId) {
        return ResponseEntity.ok(productService.createProduct(product, sellerId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody Product productDetails,
            @RequestParam Long sellerId) {
        return ResponseEntity.ok(productService.updateProduct(productId, productDetails, sellerId));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<Product>> getAllProducts(Pageable pageable) {
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(new PaginationResponse<>(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<PaginationResponse<Product>> getProductsByCategory(
            @PathVariable String category,
            Pageable pageable) {
        Page<Product> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(products));
    }

    @GetMapping("/search")
    public ResponseEntity<PaginationResponse<Product>> searchProducts(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Product> products = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(products));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Product>> getTopRatedProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getTopRatedProducts(pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Product>> getRecentProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getRecentProducts(pageable));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<PaginationResponse<Product>> getProductsBySeller(
            @PathVariable Long sellerId,
            Pageable pageable) {
        Page<Product> products = productService.getProductsBySeller(sellerId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(products));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @RequestParam Long sellerId) {
        productService.deleteProduct(productId, sellerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(productService.updateStock(productId, quantity));
    }
}
