package org.example.nexora.food;

import org.example.nexora.common.PaginationResponse;
import org.example.nexora.food.dto.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // Menu Item Endpoints
    @PostMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable Long restaurantId,
            @RequestBody MenuItem menuItem) {
        menuItem.setRestaurantId(restaurantId);
        return ResponseEntity.ok(foodService.addMenuItem(menuItem));
    }

    @PutMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(foodService.updateMenuItem(menuItemId, menuItem, restaurantId));
    }

    @DeleteMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        foodService.deleteMenuItem(menuItemId, restaurantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> getMenuItems(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodService.getMenuItems(restaurantId));
    }

    @GetMapping("/menu/{menuItemId}")
    public ResponseEntity<MenuItem> getMenuItem(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(foodService.getMenuItemById(menuItemId));
    }

    // Order Endpoints
    @PostMapping("/orders")
    public ResponseEntity<FoodOrder> createOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(foodService.createOrder(request.getOrder(), request.getItems()));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<FoodOrder> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(foodService.getOrderById(orderId));
    }

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<PaginationResponse<FoodOrder>> getUserOrders(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<FoodOrder> orders = foodService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(orders));
    }

    @GetMapping("/restaurants/{restaurantId}/orders")
    public ResponseEntity<PaginationResponse<FoodOrder>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            Pageable pageable) {
        Page<FoodOrder> orders = foodService.getRestaurantOrders(restaurantId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(orders));
    }

    @GetMapping("/drivers/{driverId}/orders")
    public ResponseEntity<PaginationResponse<FoodOrder>> getDriverOrders(
            @PathVariable Long driverId,
            Pageable pageable) {
        Page<FoodOrder> orders = foodService.getDriverOrders(driverId, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(orders));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<FoodOrder> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam FoodOrder.OrderStatus status) {
        return ResponseEntity.ok(foodService.updateOrderStatus(orderId, status));
    }

    @PatchMapping("/orders/{orderId}/driver")
    public ResponseEntity<FoodOrder> assignDriver(
            @PathVariable Long orderId,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(foodService.assignDriver(orderId, driverId));
    }

    @PatchMapping("/orders/{orderId}/location")
    public ResponseEntity<FoodOrder> updateDeliveryLocation(
            @PathVariable Long orderId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        return ResponseEntity.ok(foodService.updateDeliveryLocation(orderId, latitude, longitude));
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<FoodOrder> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(foodService.cancelOrder(orderId, userId));
    }

    @GetMapping("/users/{userId}/active-orders")
    public ResponseEntity<List<FoodOrder>> getActiveOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(foodService.getActiveOrders(userId));
    }
}
