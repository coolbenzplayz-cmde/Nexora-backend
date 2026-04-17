# Nexora API Documentation

## Table of Contents
1. [Authentication](#authentication)
2. [Users](#users)
3. [Social](#social)
4. [Messaging](#messaging)
5. [Marketplace](#marketplace)
6. [Food Delivery](#food-delivery)
7. [Ride](#ride)
8. [Grocery](#grocery)
9. [Video](#video)
10. [Advertising](#advertising)
11. [AI Assistant](#ai-assistant)
12. [Wallet](#wallet)
13. [Admin](#admin)

## Base URL
```
Production: https://api.nexora.example.com/api/v1
Development: http://localhost:8080/api/v1
```

## Authentication

### Register
```http
POST /auth/register
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "securePassword123",
  "username": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+254712345678"
}

Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "username": "johndoe"
    }
  }
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "securePassword123"
}

Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "username": "johndoe"
    }
  }
}
```

### Refresh Token
```http
POST /auth/refresh
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

## Users

### Get Profile
```http
GET /users/profile
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "avatar": "https://...",
    "bio": "User bio",
    "followers": 100,
    "following": 50
  }
}
```

### Update Profile
```http
PUT /users/profile
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "firstName": "John",
  "lastName": "Doe",
  "bio": "Updated bio",
  "avatar": "https://..."
}

Response (200):
{
  "success": true,
  "data": { ...updated profile }
}
```

### Follow User
```http
POST /users/{userId}/follow
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "message": "Successfully followed user"
}
```

## Social

### Create Post
```http
POST /social/posts
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "content": "Hello world!",
  "mediaUrls": ["https://..."],
  "mediaType": "IMAGE"
}

Response (201):
{
  "success": true,
  "data": {
    "id": "uuid",
    "content": "Hello world!",
    "author": { ... },
    "likes": 0,
    "comments": 0,
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### Get Feed
```http
GET /social/feed?page=0&size=20
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "content": [...posts],
    "totalPages": 10,
    "totalElements": 200
  }
}
```

### Like Post
```http
POST /social/posts/{postId}/like
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "message": "Post liked"
}
```

### Comment on Post
```http
POST /social/posts/{postId}/comments
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "content": "Great post!"
}

Response (201):
{
  "success": true,
  "data": { ...comment }
}
```

## Messaging

### Send Message
```http
POST /messaging/send
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "recipientId": "uuid",
  "content": "Hello!"
}

Response (201):
{
  "success": true,
  "data": { ...message }
}
```

### Get Conversations
```http
GET /messaging/conversations
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": [...conversations]
}
```

### Get Messages
```http
GET /messaging/{conversationId}/messages?page=0&size=50
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "content": [...messages],
    "totalPages": 10
  }
}
```

## Marketplace

### List Products
```http
GET /marketplace/products?page=0&size=20&search=keyword

Response (200):
{
  "success": true,
  "data": {
    "content": [...products]
  }
}
```

### Create Product
```http
POST /marketplace/products
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "title": "iPhone 14 Pro",
  "description": "Like new",
  "price": 80000,
  "category": "ELECTRONICS",
  "images": ["https://..."]
}

Response (201):
{
  "success": true,
  "data": { ...product }
}
```

### Place Order
```http
POST /marketplace/orders
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "productId": "uuid",
  "quantity": 1,
  "shippingAddress": "..."
}

Response (201):
{
  "success": true,
  "data": { ...order }
}
```

## Food Delivery

### Get Restaurants
```http
GET /food/restaurants?page=0&size=20

Response (200):
{
  "success": true,
  "data": {
    "content": [...restaurants]
  }
}
```

### Get Restaurant Menu
```http
GET /food/restaurants/{restaurantId}/menu

Response (200):
{
  "success": true,
  "data": {
    "restaurant": { ... },
    "menuItems": [...]
  }
}
```

### Place Order
```http
POST /food/orders
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "restaurantId": "uuid",
  "items": [
    { "menuItemId": "uuid", "quantity": 2 }
  ],
  "deliveryAddress": "..."
}

Response (201):
{
  "success": true,
  "data": { ...order }
}
```

### Track Order
```http
GET /food/orders/{orderId}/track
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "status": "IN_TRANSIT",
    "estimatedDelivery": "2024-01-01T01:00:00Z",
    "driver": { ... },
    "location": { "lat": -1.2921, "lng": 36.8219 }
  }
}
```

## Ride

### Request Ride
```http
POST /ride/request
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "pickupLocation": { "lat": -1.2921, "lng": 36.8219, "address": "Nairobi" },
  "dropoffLocation": { "lat": -1.3000, "lng": 36.8000, "address": "Westlands" },
  "rideType": "STANDARD"
}

Response (201):
{
  "success": true,
  "data": {
    "rideId": "uuid",
    "status": "SEARCHING",
    "driver": null,
    "estimatedPrice": 500
  }
}
```

### Get Ride Status
```http
GET /ride/{rideId}
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "IN_PROGRESS",
    "driver": { ... },
    "pickupLocation": { ... },
    "dropoffLocation": { ... },
    "currentLocation": { "lat": -1.2950, "lng": 36.8100 }
  }
}
```

### Complete Ride
```http
POST /ride/{rideId}/complete
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "COMPLETED",
    "finalPrice": 450,
    "receipt": "..."
  }
}
```

## Grocery

### Get Stores
```http
GET /grocery/stores?page=0&size=20

Response (200):
{
  "success": true,
  "data": {
    "content": [...stores]
  }
}
```

### Get Cart
```http
GET /grocery/cart
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "items": [
      { "product": { ... }, "quantity": 2, "price": 200 }
    ],
    "total": 400
  }
}
```

### Add to Cart
```http
POST /grocery/cart/add
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "productId": "uuid",
  "quantity": 2
}

Response (200):
{
  "success": true,
  "message": "Added to cart"
}
```

### Checkout
```http
POST /grocery/checkout
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "deliveryAddress": "..."
}

Response (201):
{
  "success": true,
  "data": { ...order }
}
```

## Video

### Upload Video
```http
POST /video/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

Request:
- video: <file>
- title: "My Video"
- description: "Video description"
- tags: ["tag1", "tag2"]

Response (201):
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "PROCESSING",
    "videoUrl": null
  }
}
```

### Get Videos
```http
GET /video?page=0&size=20

Response (200):
{
  "success": true,
  "data": {
    "content": [...videos]
  }
}
```

### Like Video
```http
POST /video/{videoId}/like
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "message": "Video liked"
}
```

### Subscribe
```http
POST /video/subscribe/{creatorId}
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "message": "Subscribed successfully"
}
```

## Advertising

### Create Campaign
```http
POST /advertising/campaigns
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "Summer Sale",
  "budget": 50000,
  "targetAudience": {
    "ageRange": [18, 45],
    "locations": ["Nairobi", "Mombasa"]
  },
  "adCreative": {
    "headline": "Summer Sale!",
    "description": "Up to 50% off",
    "imageUrl": "https://..."
  }
}

Response (201):
{
  "success": true,
  "data": { ...campaign }
}
```

### Get Analytics
```http
GET /advertising/campaigns/{campaignId}/analytics
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "impressions": 10000,
    "clicks": 500,
    "conversions": 50,
    "spend": 25000,
    "ctr": 5.0,
    "cpc": 50
  }
}
```

## AI Assistant

### Chat
```http
POST /ai/chat
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "message": "What's the weather like today?",
  "context": "user"
}

Response (200):
{
  "success": true,
  "data": {
    "response": "The weather today is sunny with a high of 28°C...",
    "suggestions": ["Tell me more", "What's the forecast?"]
  }
}
```

### Smart Reply
```http
POST /ai/smart-reply
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "message": "Are you coming to the party?"
}

Response (200):
{
  "success": true,
  "data": {
    "suggestions": ["Yes, I'll be there!", "Can't make it", "Maybe"]
  }
}
```

## Wallet

### Get Balance
```http
GET /wallet/balance
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "balance": 10000,
    "currency": "KES"
  }
}
```

### Add Funds
```http
POST /wallet/add-funds
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "amount": 5000,
  "paymentMethod": "M_PESA",
  "phone": "+254712345678"
}

Response (200):
{
  "success": true,
  "message": "Payment initiated"
}
```

### Transfer
```http
POST /wallet/transfer
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "recipientId": "uuid",
  "amount": 1000,
  "note": " Lunch"
}

Response (200):
{
  "success": true,
  "message": "Transfer successful"
}
```

### Withdraw
```http
POST /wallet/withdraw
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "amount": 5000,
  "bankAccount": "1234567890",
  "bankName": "Equity Bank"
}

Response (200):
{
  "success": true,
  "message": "Withdrawal initiated"
}
```

### Get Transaction History
```http
GET /wallet/transactions?page=0&size=20
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "content": [...transactions]
  }
}
```

## Admin

### Get All Users (Admin only)
```http
GET /admin/users?page=0&size=20
Authorization: Bearer <admin_token>

Response (200):
{
  "success": true,
  "data": {
    "content": [...users]
  }
}
```

### Get Reports (Admin only)
```http
GET /admin/reports
Authorization: Bearer <admin_token>

Response (200):
{
  "success": true,
  "data": [...reports]
}
```

### Moderate Content (Admin only)
```http
POST /admin/moderate/{contentId}
Authorization: Bearer <admin_token>
Content-Type: application/json

Request:
{
  "action": "REMOVE",
  "reason": "Violates community guidelines"
}

Response (200):
{
  "success": true,
  "message": "Content moderated"
}
```

## Error Responses

All endpoints may return error responses in the following format:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable message"
  }
}
```

Common error codes:
- `UNAUTHORIZED` - Invalid or missing token
- `FORBIDDEN` - Insufficient permissions
- `NOT_FOUND` - Resource not found
- `VALIDATION_ERROR` - Invalid input
- `INTERNAL_ERROR` - Server error
- `RATE_LIMITED` - Too many requests

## Rate Limits
- Authentication: 10 requests/minute
- Read operations: 100 requests/minute
- Write operations: 30 requests/minute

## WebSocket Connections
Real-time features use WebSocket connections at:
```
wss://api.nexora.example.com/ws
```

### Subscribe to Events
```javascript
// Subscribe to messages
socket.emit('subscribe', { channel: 'messages', userId: 'uuid' });

// Subscribe to ride updates
socket.emit('subscribe', { channel: 'ride', rideId: 'uuid' });

// Listen for events
socket.on('message', (data) => { ... });
socket.on('ride_update', (data) => { ... });
```
