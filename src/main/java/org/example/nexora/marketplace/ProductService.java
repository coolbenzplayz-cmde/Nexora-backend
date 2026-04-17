package org.example.nexora.marketplace;

import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Product createProduct(Product product, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException("Seller not found", "SELLER_NOT_FOUND"));
        
        product.setSeller(seller);
        product.setStatus(Product.ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long productId, Product productDetails, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found", "PRODUCT_NOT_FOUND"));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("Not authorized to update this product", "UNAUTHORIZED");
        }

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStockQuantity(productDetails.getStockQuantity());

        return productRepository.save(product);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found", "PRODUCT_NOT_FOUND"));
    }

    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    public List<Product> getTopRatedProducts(Pageable pageable) {
        return productRepository.findTopRatedProducts(pageable);
    }

    public List<Product> getRecentProducts(Pageable pageable) {
        return productRepository.findRecentProducts(pageable);
    }

    public Page<Product> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found", "PRODUCT_NOT_FOUND"));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("Not authorized to delete this product", "UNAUTHORIZED");
        }

        product.setStatus(Product.ProductStatus.DELETED);
        productRepository.save(product);
    }

    @Transactional
    public Product updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found", "PRODUCT_NOT_FOUND"));

        product.setStockQuantity(quantity);
        if (quantity <= 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        }

        return productRepository.save(product);
    }
}
