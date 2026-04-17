// API Base URL - update this to match your backend
const API_BASE_URL = '/api';

// Generic fetch wrapper with error handling
async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new Error(error.message || 'API request failed');
  }

  return response.json();
}

// Auth API
export const authApi = {
  login: (email: string, password: string) =>
    fetchApi<{ token: string; user: any }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
    
  register: (username: string, email: string, password: string) =>
    fetchApi<{ token: string; user: any }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    }),
    
  logout: () => 
    fetchApi('/auth/logout', { method: 'POST' }),
};

// Food API
export const foodApi = {
  getRestaurants: () => fetchApi<any[]>('/food/restaurants'),
  
  getMenuItems: (restaurantId: string) => 
    fetchApi<any[]>(`/food/restaurants/${restaurantId}/menu`),
  
  createOrder: (order: any, items: any[]) =>
    fetchApi<any>('/food/orders', {
      method: 'POST',
      body: JSON.stringify({ order, items }),
    }),
  
  getOrders: (userId: string) => 
    fetchApi<any[]>(`/food/users/${userId}/orders`),
  
  getOrder: (orderId: string) => 
    fetchApi<any>(`/food/orders/${orderId}`),
};

// Ride API
export const rideApi = {
  createRideRequest: (rideRequest: any) =>
    fetchApi<any>('/rides', {
      method: 'POST',
      body: JSON.stringify(rideRequest),
    }),
  
  getRides: (userId: string) => 
    fetchApi<any[]>(`/rides/users/${userId}`),
  
  acceptRide: (rideId: string, driverId: string) =>
    fetchApi<any>(`/rides/${rideId}/accept?driverId=${driverId}`, {
      method: 'PATCH',
    }),
  
  updateStatus: (rideId: string, status: string) =>
    fetchApi<any>(`/rides/${rideId}/status?status=${status}`, {
      method: 'PATCH',
    }),
};

// Grocery API
export const groceryApi = {
  getItems: (page = 0, size = 20) => 
    fetchApi<any>(`/grocery/items?page=${page}&size=${size}`),
  
  addToCart: (userId: string, itemId: string, quantity: number) =>
    fetchApi<any>(`/grocery/cart?userId=${userId}&itemId=${itemId}&quantity=${quantity}`, {
      method: 'POST',
    }),
  
  getCart: (userId: string) => 
    fetchApi<any[]>(`/grocery/cart/${userId}`),
};

// Marketplace API
export const marketplaceApi = {
  getProducts: (page = 0, size = 20) => 
    fetchApi<any>(`/marketplace/products?page=${page}&size=${size}`),
  
  getProduct: (productId: string) => 
    fetchApi<any>(`/marketplace/products/${productId}`),
  
  searchProducts: (query: string) => 
    fetchApi<any[]>(`/marketplace/products/search?keyword=${query}`),
};

// Payment API
export const paymentApi = {
  getBalance: (userId: string) => 
    fetchApi<{ balance: number }>(`/payment/balance/${userId}`),
  
  deposit: (userId: string, amount: number, method: string) =>
    fetchApi<any>(`/payment/deposit?userId=${userId}&amount=${amount}&method=${method}`, {
      method: 'POST',
    }),
  
  transfer: (fromUserId: string, toUserId: string, amount: number, description: string) =>
    fetchApi<any>(`/payment/transfer?fromUserId=${fromUserId}&toUserId=${toUserId}&amount=${amount}&description=${description}`, {
      method: 'POST',
    }),
  
  getTransactions: (userId: string) => 
    fetchApi<any[]>(`/payment/transactions/${userId}`),
};

// Notification API
export const notificationApi = {
  getNotifications: (userId: string, page = 0, size = 20) => 
    fetchApi<any>(`/notifications/${userId}?page=${page}&size=${size}`),
  
  markAsRead: (notificationId: string, userId: string) =>
    fetchApi<any>(`/notifications/${notificationId}/read?userId=${userId}`, {
      method: 'PATCH',
    }),
};

// Video API
export const videoApi = {
  getVideos: (page = 0, size = 20) => 
    fetchApi<any>(`/video/videos?page=${page}&size=${size}`),
  
  getVideo: (videoId: string) => 
    fetchApi<any>(`/video/videos/${videoId}`),
  
  getSubscribers: (creatorId: string) => 
    fetchApi<any[]>(`/video/subscribers/${creatorId}`),
};

// Social API
export const socialApi = {
  getPosts: (page = 0, size = 20) => 
    fetchApi<any>(`/social/posts?page=${page}&size=${size}`),
  
  createPost: (content: string, imageUrl?: string) =>
    fetchApi<any>('/social/posts', {
      method: 'POST',
      body: JSON.stringify({ content, imageUrl }),
    }),
  
  likePost: (postId: string) =>
    fetchApi<any>(`/social/posts/${postId}/like`, {
      method: 'POST',
    }),
  
  comment: (postId: string, content: string) =>
    fetchApi<any>(`/social/posts/${postId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
};

// Messaging API
export const messagingApi = {
  getConversations: (userId: string) => 
    fetchApi<any[]>(`/messaging/conversations/${userId}`),
  
  getMessages: (conversationId: string) => 
    fetchApi<any[]>(`/messaging/${conversationId}/messages`),
  
  sendMessage: (conversationId: string, content: string) =>
    fetchApi<any>(`/messaging/${conversationId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
};

// Export all APIs
export default {
  auth: authApi,
  food: foodApi,
  ride: rideApi,
  grocery: groceryApi,
  marketplace: marketplaceApi,
  payment: paymentApi,
  notification: notificationApi,
  video: videoApi,
  social: socialApi,
  messaging: messagingApi,
};