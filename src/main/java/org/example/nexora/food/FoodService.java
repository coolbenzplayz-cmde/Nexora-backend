package org.example.nexora.food;

import org.example.nexora.common.BusinessException;
import org.example.nexora.common.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FoodService {

    private final FoodOrderRepository foodOrderRepository;
    private final MenuItemRepository menuItemRepository;

    public FoodService(FoodOrderRepository foodOrderRepository, MenuItemRepository menuItemRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // Restaurant Menu Methods
    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(Long menuItemId, MenuItem menuItemDetails, Long restaurantId) {
        return null;
    }

    public void deleteMenuItem(Long menuItemId, Long restaurantId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new BusinessException("Menu item not found"));
        
        if (!menuItem.getRestaurantId().equals(restaurantId)) {
            throw new BusinessException("Not authorized to delete this menu item");
        }
        
        menuItemRepository.delete(menuItem);
    }

    public List<MenuItem> getMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }

    public MenuItem getMenuItemById(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new BusinessException("Menu item not found"));
    }

    // Order Methods
    public FoodOrder createOrder(FoodOrder order, List<OrderItem> items) {
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalAmount(totalAmount);
        order.setStatus(FoodOrder.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        FoodOrder savedOrder = foodOrderRepository.save(order);
        
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
        }
        
        savedOrder.setItems(items);
        
        return savedOrder;
    }

    public FoodOrder getOrderById(Long orderId) {
        return foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
    }

    public Page<FoodOrder> getUserOrders(Long userId, Pageable pageable) {
        return foodOrderRepository.findByUserId(userId, pageable);
    }

    public Page<FoodOrder> getRestaurantOrders(Long restaurantId, Pageable pageable) {
        return foodOrderRepository.findByRestaurantId(restaurantId, pageable);
    }

    public Page<FoodOrder> getDriverOrders(Long driverId, Pageable pageable) {
        return foodOrderRepository.findByDriverId(driverId, pageable);
    }

    public FoodOrder updateOrderStatus(Long orderId, FoodOrder.OrderStatus newStatus) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        return foodOrderRepository.save(order);
    }

    public FoodOrder assignDriver(Long orderId, Long driverId) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        
        order.setDriverId(driverId);
        order.setStatus(FoodOrder.OrderStatus.PICKED_UP);
        order.setUpdatedAt(LocalDateTime.now());
        
        return foodOrderRepository.save(order);
    }

    public FoodOrder updateDeliveryLocation(Long orderId, Double latitude, Double longitude) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        
        order.setDeliveryLatitude(latitude);
        order.setDeliveryLongitude(longitude);
        
        return foodOrderRepository.save(order);
    }

    public FoodOrder cancelOrder(Long orderId, Long userId) {
        FoodOrder order = foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Not authorized to cancel this order");
        }
        
        if (order.getStatus() == FoodOrder.OrderStatus.DELIVERED ||
            order.getStatus() == FoodOrder.OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel order in current status");
        }
        
        order.setStatus(FoodOrder.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        return foodOrderRepository.save(order);
    }

    public List<FoodOrder> getActiveOrders(Long userId) {
        return foodOrderRepository.findByUserIdAndStatus(userId, FoodOrder.OrderStatus.IN_TRANSIT, Pageable.unpaged()).getContent();
    }

    // Statistics
    public long getRestaurantOrderCount(Long restaurantId) {
        return foodOrderRepository.findByRestaurantId(restaurantId, Pageable.unpaged()).getTotalElements();
    }

    public long getUserOrderCount(Long userId) {
        return foodOrderRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
    }

    public FoodOrder createOrder(FoodOrder order, List<OrderItem> items) {
    }
}
