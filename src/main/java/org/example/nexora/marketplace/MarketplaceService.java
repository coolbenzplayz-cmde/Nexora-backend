package org.example.nexora.marketplace;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive Marketplace/Grocery e-commerce system providing:
 * - Product catalog management
 * - Shopping cart and checkout
 * - Order processing and tracking
 * - Payment integration
 * - Inventory management
 * - Vendor management
 * - Delivery and logistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final VendorRepository vendorRepository;
    private final CartRepository cartRepository;
    private final PaymentService paymentService;

    /**
     * Search products in marketplace
     */
    public ProductSearchResult searchProducts(ProductSearchRequest request) {
        log.info("Searching products with query: {}", request.getQuery());

        ProductSearchResult result = new ProductSearchResult();
        result.setQuery(request.getQuery());
        result.setSearchTimestamp(LocalDateTime.now());

        // Get products from database
        List<Product> products = productRepository.searchProducts(
                request.getQuery(),
                request.getCategory(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getVendorId(),
                request.isAvailableOnly(),
                request.getSortBy(),
                request.getSortOrder(),
                request.getLimit(),
                request.getOffset()
        );

        // Convert to search results
        List<ProductSearchItem> searchItems = products.stream()
                .map(this::convertToSearchItem)
                .collect(Collectors.toList());

        result.setProducts(searchItems);
        result.setTotalCount(products.size());
        result.setHasMore(products.size() >= request.getLimit());

        // Add search suggestions
        List<String> suggestions = generateSearchSuggestions(request.getQuery());
        result.setSuggestions(suggestions);

        // Add filters
        Map<String, List<FilterOption>> filters = generateAvailableFilters(products);
        result.setAvailableFilters(filters);

        return result;
    }

    /**
     * Get product details
     */
    public ProductDetail getProductDetail(Long productId, Long userId) {
        log.info("Getting product detail for: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        ProductDetail detail = new ProductDetail();
        detail.setProduct(product);

        // Get vendor information
        Vendor vendor = vendorRepository.findById(product.getVendorId())
                .orElse(null);
        detail.setVendor(vendor);

        // Get product reviews
        List<ProductReview> reviews = getProductReviews(productId);
        detail.setReviews(reviews);

        // Calculate rating summary
        RatingSummary ratingSummary = calculateRatingSummary(reviews);
        detail.setRatingSummary(ratingSummary);

        // Get related products
        List<Product> relatedProducts = getRelatedProducts(product);
        detail.setRelatedProducts(relatedProducts);

        // Check if user has favorited
        if (userId != null) {
            detail.setFavorited(isProductFavorited(userId, productId));
        }

        // Get inventory status
        InventoryStatus inventoryStatus = getInventoryStatus(productId);
        detail.setInventoryStatus(inventoryStatus);

        return detail;
    }

    /**
     * Add product to cart
     */
    public CartItem addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);

        // Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        // Check availability
        if (!isProductAvailable(request.getProductId(), request.getQuantity())) {
            throw new IllegalStateException("Product not available in requested quantity");
        }

        // Get or create user cart
        Cart cart = getOrCreateCart(userId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item
            cartItem = new CartItem();
            cartItem.setCartId(cart.getId());
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(product.getPrice());
            cartItem.setAddedAt(LocalDateTime.now());
            cart.getItems().add(cartItem);
        }

        // Update cart totals
        updateCartTotals(cart);

        // Save cart
        cartRepository.save(cart);

        return cartItem;
    }

    /**
     * Get user cart
     */
    public Cart getUserCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        
        // Load product details for cart items
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElse(null);
            item.setProduct(product);
        }

        return cart;
    }

    /**
     * Update cart item quantity
     */
    public CartItem updateCartItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cart item not found"));

        // Validate availability
        if (!isProductAvailable(item.getProductId(), quantity)) {
            throw new IllegalStateException("Product not available in requested quantity");
        }

        item.setQuantity(quantity);
        item.setUpdatedAt(LocalDateTime.now());

        // Update cart totals
        updateCartTotals(cart);

        cartRepository.save(cart);

        return item;
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    /**
     * Checkout cart
     */
    public CheckoutResult checkoutCart(Long userId, CheckoutRequest request) {
        log.info("Checking out cart for user {}", userId);

        Cart cart = getOrCreateCart(userId);
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate inventory
        for (CartItem item : cart.getItems()) {
            if (!isProductAvailable(item.getProductId(), item.getQuantity())) {
                throw new IllegalStateException("Product " + item.getProductId() + " not available");
            }
        }

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        // Add order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        // Calculate totals
        BigDecimal shippingFee = calculateShippingFee(request.getShippingAddress(), order);
        BigDecimal tax = calculateTax(subtotal, request.getShippingAddress());
        BigDecimal total = subtotal.add(shippingFee).add(tax);

        order.setShippingFee(shippingFee);
        order.setTax(tax);
        order.setTotal(total);

        // Process payment
        PaymentResult paymentResult = paymentService.processPayment(
                userId,
                total,
                request.getPaymentMethod(),
                "Order payment"
        );

        if (!paymentResult.isSuccess()) {
            throw new IllegalStateException("Payment failed: " + paymentResult.getErrorMessage());
        }

        order.setPaymentId(paymentResult.getPaymentId());
        order.setStatus(OrderStatus.CONFIRMED);

        // Save order
        order = orderRepository.save(order);

        // Reserve inventory
        reserveInventory(order);

        // Clear cart
        clearCart(userId);

        // Send order confirmation
        sendOrderConfirmation(order);

        return CheckoutResult.success(order, paymentResult);
    }

    /**
     * Get user orders
     */
    public List<Order> getUserOrders(Long userId, OrderStatus status, int limit) {
        return orderRepository.findByUserIdAndStatus(userId, status, limit);
    }

    /**
     * Get order details
     */
    public OrderDetail getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("Access denied");
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);

        // Get tracking information
        TrackingInfo tracking = getOrderTracking(orderId);
        detail.setTracking(tracking);

        // Get delivery estimate
        DeliveryEstimate delivery = getDeliveryEstimate(order);
        detail.setDeliveryEstimate(delivery);

        return detail;
    }

    /**
     * Create vendor account
     */
    public Vendor createVendor(VendorRegistrationRequest request) {
        log.info("Creating vendor account for: {}", request.getBusinessName());

        // Check if vendor already exists
        if (vendorRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalStateException("Vendor account already exists");
        }

        Vendor vendor = new Vendor();
        vendor.setUserId(request.getUserId());
        vendor.setBusinessName(request.getBusinessName());
        vendor.setBusinessType(request.getBusinessType());
        vendor.setDescription(request.getDescription());
        vendor.setContactEmail(request.getContactEmail());
        vendor.setContactPhone(request.getContactPhone());
        vendor.setAddress(request.getAddress());
        vendor.setVerificationStatus(VendorVerificationStatus.PENDING);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor.setActive(false);

        return vendorRepository.save(vendor);
    }

    /**
     * Add product for vendor
     */
    public Product addProduct(Long vendorId, AddProductRequest request) {
        log.info("Adding product for vendor: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalStateException("Vendor not found"));

        if (!vendor.isActive()) {
            throw new IllegalStateException("Vendor account not active");
        }

        Product product = new Product();
        product.setVendorId(vendorId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setInventoryCount(request.getInventoryCount());
        product.setImages(request.getImages());
        product.setSpecifications(request.getSpecifications());
        product.setShippingInfo(request.getShippingInfo());
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Get vendor dashboard
     */
    public VendorDashboard getVendorDashboard(Long vendorId) {
        VendorDashboard dashboard = new VendorDashboard();
        dashboard.setVendorId(vendorId);
        dashboard.setGeneratedAt(LocalDateTime.now());

        // Get vendor info
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalStateException("Vendor not found"));
        dashboard.setVendor(vendor);

        // Get sales analytics
        SalesAnalytics sales = getVendorSalesAnalytics(vendorId);
        dashboard.setSalesAnalytics(sales);

        // Get product performance
        List<ProductPerformance> productPerformance = getProductPerformance(vendorId);
        dashboard.setProductPerformance(productPerformance);

        // Get inventory status
        List<InventoryStatus> inventoryStatus = getVendorInventoryStatus(vendorId);
        dashboard.setInventoryStatus(inventoryStatus);

        // Get recent orders
        List<Order> recentOrders = getVendorRecentOrders(vendorId);
        dashboard.setRecentOrders(recentOrders);

        return dashboard;
    }

    // Private helper methods
    private ProductSearchItem convertToSearchItem(Product product) {
        ProductSearchItem item = new ProductSearchItem();
        item.setProductId(product.getId());
        item.setName(product.getName());
        item.setDescription(product.getDescription());
        item.setPrice(product.getPrice());
        item.setCategory(product.getCategory());
        item.setImageUrl(product.getPrimaryImageUrl());
        item.setVendorId(product.getVendorId());
        item.setRating(product.getAverageRating());
        item.setReviewCount(product.getReviewCount());
        item.setInStock(product.getInventoryCount() > 0);
        return item;
    }

    private List<String> generateSearchSuggestions(String query) {
        // Simplified suggestion generation
        return Arrays.asList("electronics", "clothing", "home goods", "books", "sports");
    }

    private Map<String, List<FilterOption>> generateAvailableFilters(List<Product> products) {
        Map<String, List<FilterOption>> filters = new HashMap<>();
        
        // Category filter
        Map<String, Integer> categoryCounts = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingInt(p -> 1)));
        
        List<FilterOption> categoryOptions = categoryCounts.entrySet().stream()
                .map(entry -> new FilterOption(entry.getKey(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        filters.put("category", categoryOptions);
        
        return filters;
    }

    private List<ProductReview> getProductReviews(Long productId) {
        // Simplified - would fetch from database
        List<ProductReview> reviews = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductReview review = new ProductReview();
            review.setRating(4 + (int)(Math.random() * 2));
            review.setComment("Great product!");
            review.setCreatedAt(LocalDateTime.now().minusDays(i));
            reviews.add(review);
        }
        return reviews;
    }

    private RatingSummary calculateRatingSummary(List<ProductReview> reviews) {
        RatingSummary summary = new RatingSummary();
        summary.setTotalReviews(reviews.size());
        
        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(ProductReview::getRating)
                    .average()
                    .orElse(0.0);
            summary.setAverageRating(averageRating);
            
            // Rating distribution
            Map<Integer, Integer> distribution = reviews.stream()
                    .collect(Collectors.groupingBy(ProductReview::getRating, Collectors.summingInt(r -> 1)));
            summary.setRatingDistribution(distribution);
        }
        
        return summary;
    }

    private List<Product> getRelatedProducts(Product product) {
        // Simplified - would find related products based on category
        return productRepository.findByCategory(product.getCategory(), 5);
    }

    private boolean isProductFavorited(Long userId, Long productId) {
        // Simplified - would check favorites table
        return Math.random() > 0.7;
    }

    private InventoryStatus getInventoryStatus(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return new InventoryStatus();
        
        InventoryStatus status = new InventoryStatus();
        status.setAvailable(product.getInventoryCount());
        status.setReserved(product.getReservedCount());
        status.setTotal(product.getInventoryCount() + product.getReservedCount());
        status.setInStock(product.getInventoryCount() > 0);
        status.setLowStock(product.getInventoryCount() < 10);
        
        return status;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setCreatedAt(LocalDateTime.now());
                    cart.setItems(new ArrayList<>());
                    return cartRepository.save(cart);
                });
    }

    private boolean isProductAvailable(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null && product.getInventoryCount() >= quantity;
    }

    private void updateCartTotals(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cart.setSubtotal(subtotal);
        cart.setItemCount(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private BigDecimal calculateShippingFee(Address address, Order order) {
        // Simplified shipping calculation
        return BigDecimal.valueOf(9.99);
    }

    private BigDecimal calculateTax(BigDecimal subtotal, Address address) {
        // Simplified tax calculation (8.25%)
        return subtotal.multiply(BigDecimal.valueOf(0.0825));
    }

    private void reserveInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setInventoryCount(product.getInventoryCount() - item.getQuantity());
                product.setReservedCount(product.getReservedCount() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private void sendOrderConfirmation(Order order) {
        // Simplified - would send email/notification
        log.info("Order confirmation sent for order: {}", order.getId());
    }

    private TrackingInfo getOrderTracking(Long orderId) {
        // Simplified tracking info
        TrackingInfo tracking = new TrackingInfo();
        tracking.setTrackingNumber("TN" + System.currentTimeMillis());
        tracking.setCarrier("UPS");
        tracking.setStatus("IN_TRANSIT");
        tracking.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
        return tracking;
    }

    private DeliveryEstimate getDeliveryEstimate(Order order) {
        DeliveryEstimate estimate = new DeliveryEstimate();
        estimate.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
        estimate.setShippingMethod("Standard");
        estimate.setTrackingAvailable(true);
        return estimate;
    }

    private SalesAnalytics getVendorSalesAnalytics(Long vendorId) {
        SalesAnalytics analytics = new SalesAnalytics();
        analytics.setTotalRevenue(BigDecimal.valueOf(15000));
        analytics.setTotalOrders(150);
        analytics.setAverageOrderValue(BigDecimal.valueOf(100));
        analytics.setMonthlyGrowth(15.5);
        return analytics;
    }

    private List<ProductPerformance> getProductPerformance(Long vendorId) {
        // Simplified product performance
        return new ArrayList<>();
    }

    private List<InventoryStatus> getVendorInventoryStatus(Long vendorId) {
        // Simplified inventory status
        return new ArrayList<>();
    }

    private List<Order> getVendorRecentOrders(Long vendorId) {
        // Simplified recent orders
        return new ArrayList<>();
    }

    // Data classes
    @Data
    public static class ProductSearchResult {
        private String query;
        private LocalDateTime searchTimestamp;
        private List<ProductSearchItem> products;
        private int totalCount;
        private boolean hasMore;
        private List<String> suggestions;
        private Map<String, List<FilterOption>> availableFilters;
    }

    @Data
    public static class ProductSearchItem {
        private Long productId;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String imageUrl;
        private Long vendorId;
        private double rating;
        private int reviewCount;
        private boolean inStock;
    }

    @Data
    public static class FilterOption {
        private String value;
        private String label;
        private int count;
    }

    @Data
    public static class ProductDetail {
        private Product product;
        private Vendor vendor;
        private List<ProductReview> reviews;
        private RatingSummary ratingSummary;
        private List<Product> relatedProducts;
        private boolean favorited;
        private InventoryStatus inventoryStatus;
    }

    @Data
    public static class ProductReview {
        private Long id;
        private Long userId;
        private int rating;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Data
    public static class RatingSummary {
        private double averageRating;
        private int totalReviews;
        private Map<Integer, Integer> ratingDistribution;
    }

    @Data
    public static class InventoryStatus {
        private int available;
        private int reserved;
        private int total;
        private boolean inStock;
        private boolean lowStock;
    }

    @Data
    public static class Cart {
        private Long id;
        private Long userId;
        private List<CartItem> items;
        private BigDecimal subtotal;
        private int itemCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class CartItem {
        private Long id;
        private Long cartId;
        private Long productId;
        private Product product;
        private int quantity;
        private BigDecimal unitPrice;
        private LocalDateTime addedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class CheckoutResult {
        private boolean success;
        private Order order;
        private PaymentResult paymentResult;
        private String errorMessage;

        public static CheckoutResult success(Order order, PaymentResult paymentResult) {
            CheckoutResult result = new CheckoutResult();
            result.setSuccess(true);
            result.setOrder(order);
            result.setPaymentResult(paymentResult);
            return result;
        }

        public static CheckoutResult failure(String errorMessage) {
            CheckoutResult result = new CheckoutResult();
            result.setSuccess(false);
            result.setErrorMessage(errorMessage);
            return result;
        }
    }

    @Data
    public static class Order {
        private Long id;
        private Long userId;
        private OrderStatus status;
        private List<OrderItem> items;
        private Address shippingAddress;
        private Address billingAddress;
        private String paymentMethod;
        private String paymentId;
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal tax;
        private BigDecimal total;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class OrderItem {
        private Long id;
        private Long orderId;
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data
    public static class OrderDetail {
        private Order order;
        private TrackingInfo tracking;
        private DeliveryEstimate deliveryEstimate;
    }

    @Data
    public static class TrackingInfo {
        private String trackingNumber;
        private String carrier;
        private String status;
        private LocalDateTime estimatedDelivery;
        private List<TrackingEvent> events;
    }

    @Data
    public static class TrackingEvent {
        private LocalDateTime timestamp;
        private String location;
        private String description;
    }

    @Data
    public static class DeliveryEstimate {
        private LocalDateTime estimatedDelivery;
        private String shippingMethod;
        private boolean trackingAvailable;
    }

    @Data
    public static class Vendor {
        private Long id;
        private Long userId;
        private String businessName;
        private String businessType;
        private String description;
        private String contactEmail;
        private String contactPhone;
        private Address address;
        private VendorVerificationStatus verificationStatus;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    public static class VendorDashboard {
        private Long vendorId;
        private LocalDateTime generatedAt;
        private Vendor vendor;
        private SalesAnalytics salesAnalytics;
        private List<ProductPerformance> productPerformance;
        private List<InventoryStatus> inventoryStatus;
        private List<Order> recentOrders;
    }

    @Data
    public static class SalesAnalytics {
        private BigDecimal totalRevenue;
        private int totalOrders;
        private BigDecimal averageOrderValue;
        private double monthlyGrowth;
    }

    @Data
    public static class ProductPerformance {
        private Long productId;
        private String productName;
        private int totalSold;
        private BigDecimal totalRevenue;
        private double conversionRate;
    }

    @Data
    public static class PaymentResult {
        private boolean success;
        private String paymentId;
        private String errorMessage;
    }

    @Data
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }

    // Enums
    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
    }

    public enum VendorVerificationStatus {
        PENDING, VERIFIED, REJECTED, SUSPENDED
    }

    // Request classes
    @Data
    public static class ProductSearchRequest {
        private String query;
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Long vendorId;
        private boolean availableOnly = true;
        private String sortBy = "relevance";
        private String sortOrder = "desc";
        private int limit = 20;
        private int offset = 0;
    }

    @Data
    public static class AddToCartRequest {
        private Long productId;
        private int quantity = 1;
    }

    @Data
    public static class CheckoutRequest {
        private Address shippingAddress;
        private Address billingAddress;
        private String paymentMethod;
    }

    @Data
    public static class VendorRegistrationRequest {
        private Long userId;
        private String businessName;
        private String businessType;
        private String description;
        private String contactEmail;
        private String contactPhone;
        private Address address;
    }

    @Data
    public static class AddProductRequest {
        private String name;
        private String description;
        private String category;
        private BigDecimal price;
        private int inventoryCount;
        private List<String> images;
        private Map<String, Object> specifications;
        private ShippingInfo shippingInfo;
    }

    @Data
    public static class ShippingInfo {
        private BigDecimal weight;
        private String dimensions;
        private List<String> shippingMethods;
        private BigDecimal shippingCost;
    }

    // Repository placeholders
    private static class ProductRepository {
        public List<Product> searchProducts(String query, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                          Long vendorId, boolean availableOnly, String sortBy, String sortOrder,
                                          int limit, int offset) { return new ArrayList<>(); }
        public Optional<Product> findById(Long id) { return Optional.empty(); }
        public Product save(Product product) { return product; }
        public List<Product> findByCategory(String category, int limit) { return new ArrayList<>(); }
    }

    private static class OrderRepository {
        public Optional<Order> findById(Long id) { return Optional.empty(); }
        public Order save(Order order) { return order; }
        public List<Order> findByUserIdAndStatus(Long userId, OrderStatus status, int limit) { return new ArrayList<>(); }
    }

    private static class VendorRepository {
        public Optional<Vendor> findByUserId(Long userId) { return Optional.empty(); }
        public Optional<Vendor> findById(Long id) { return Optional.empty(); }
        public Vendor save(Vendor vendor) { return vendor; }
    }

    private static class CartRepository {
        public Optional<Cart> findByUserId(Long userId) { return Optional.empty(); }
        public Cart save(Cart cart) { return cart; }
    }

    private static class PaymentService {
        public PaymentResult processPayment(Long userId, BigDecimal amount, String paymentMethod, String description) {
            PaymentResult result = new PaymentResult();
            result.setSuccess(true);
            result.setPaymentId("PAY_" + System.currentTimeMillis());
            return result;
        }
    }

    // Service instances - duplicates removed
}
