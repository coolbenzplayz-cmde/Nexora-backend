import React, { useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Text, View, StyleSheet, TextInput, TouchableOpacity, ScrollView, Image, Alert, KeyboardAvoidingView, Platform } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

// Types
interface User {
  id: string;
  username: string;
  email: string;
  role: string;
}

// Auth Context
const AuthContext = React.createContext<{
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
}>({
  user: null,
  login: () => {},
  logout: () => {},
});

// Mock Data
const mockProducts = [
  { id: '1', name: 'Smartphone Pro', price: 699, imageUrl: 'https://picsum.photos/200/200?random=1', seller: 'TechStore' },
  { id: '2', name: 'Wireless Earbuds', price: 149, imageUrl: 'https://picsum.photos/200/200?random=2', seller: 'AudioWorld' },
  { id: '3', name: 'Laptop Ultra', price: 1299, imageUrl: 'https://picsum.photos/200/200?random=3', seller: 'TechStore' },
  { id: '4', name: 'Smart Watch', price: 299, imageUrl: 'https://picsum.photos/200/200?random=4', seller: 'GadgetHub' },
];

const mockRestaurants = [
  { id: '1', name: 'Burger Palace', cuisine: 'American', rating: 4.5, deliveryTime: '25-35 min', imageUrl: 'https://picsum.photos/200/150?random=5' },
  { id: '2', name: 'Pizza Heaven', cuisine: 'Italian', rating: 4.8, deliveryTime: '30-40 min', imageUrl: 'https://picsum.photos/200/150?random=6' },
  { id: '3', name: 'Sushi Master', cuisine: 'Japanese', rating: 4.7, deliveryTime: '35-45 min', imageUrl: 'https://picsum.photos/200/150?random=7' },
  { id: '4', name: 'Tandoori Grill', cuisine: 'Indian', rating: 4.6, deliveryTime: '20-30 min', imageUrl: 'https://picsum.photos/200/150?random=8' },
];

const mockVideos = [
  { id: '1', title: 'How to use Nexora', thumbnailUrl: 'https://picsum.photos/300/200?random=9', views: 15000, creator: 'Nexora Official', duration: '5:30' },
  { id: '2', title: 'Creator Economy Guide', thumbnailUrl: 'https://picsum.photos/300/200?random=10', views: 8500, creator: 'Tech Influencer', duration: '12:45' },
  { id: '3', title: 'Maximizing Your Earnings', thumbnailUrl: 'https://picsum.photos/300/200?random=11', views: 12000, creator: 'Business Pro', duration: '8:20' },
];

const mockWallet = {
  balance: 1250.00,
  transactions: [
    { id: '1', amount: 500, type: 'deposit', date: '2026-04-15' },
    { id: '2', amount: -120, type: 'purchase', date: '2026-04-14' },
    { id: '3', amount: 850, type: 'deposit', date: '2026-04-10' },
  ],
};

// Login Screen
function LoginScreen({ navigation }: any) {
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const { login } = React.useContext(AuthContext);

  const handleLogin = () => {
    if (!email || !password) {
      Alert.alert('Error', 'Please fill in all fields');
      return;
    }
    // Demo login
    login({ id: '1', username: username || 'demo', email, role: 'USER' });
  };

  const theme = {
    primary: '#0284c7',
    background: '#f8fafc',
    surface: '#ffffff',
    text: '#1e293b',
    textSecondary: '#64748b',
    border: '#e2e8f0',
  };

  return (
    <KeyboardAvoidingView 
      style={[styles.container, { backgroundColor: '#0f172a' }]}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Logo */}
        <View style={styles.logoContainer}>
          <View style={[styles.logo, { backgroundColor: theme.primary }]}>
            <Text style={styles.logoText}>N</Text>
          </View>
          <Text style={styles.appName}>Nexora</Text>
          <Text style={styles.tagline}>
            {isRegister ? 'Create your account' : 'Welcome back'}
          </Text>
        </View>

        {/* Form */}
        <View style={[styles.formCard, { backgroundColor: 'rgba(30, 41, 59, 0.8)' }]}>
          {isRegister && (
            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>Username</Text>
              <TextInput
                style={[styles.input, { backgroundColor: '#334155', color: '#fff', borderColor: '#475569' }]}
                value={username}
                onChangeText={setUsername}
                placeholder="johndoe"
                placeholderTextColor="#94a3b8"
              />
            </View>
          )}
          
          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Email</Text>
            <TextInput
              style={[styles.input, { backgroundColor: '#334155', color: '#fff', borderColor: '#475569' }]}
              value={email}
              onChangeText={setEmail}
              placeholder="you@example.com"
              placeholderTextColor="#94a3b8"
              keyboardType="email-address"
              autoCapitalize="none"
            />
          </View>

          <View style={styles.inputContainer}>
            <Text style={styles.inputLabel}>Password</Text>
            <View style={{ position: 'relative' }}>
              <TextInput
                style={[styles.input, { backgroundColor: '#334155', color: '#fff', borderColor: '#475569', paddingRight: 50 }]}
                value={password}
                onChangeText={setPassword}
                placeholder="••••••••"
                placeholderTextColor="#94a3b8"
                secureTextEntry={!showPassword}
              />
              <TouchableOpacity 
                style={styles.eyeButton}
                onPress={() => setShowPassword(!showPassword)}
              >
                <Icon name={showPassword ? 'visibility-off' : 'visibility'} size={24} color="#94a3b8" />
              </TouchableOpacity>
            </View>
          </View>

          {!isRegister && (
            <TouchableOpacity>
              <Text style={[styles.forgotPassword, { color: theme.primary }]}>Forgot password?</Text>
            </TouchableOpacity>
          )}

          <TouchableOpacity 
            style={[styles.button, { backgroundColor: theme.primary }]}
            onPress={handleLogin}
          >
            <Text style={styles.buttonText}>
              {isRegister ? 'Create Account' : 'Sign In'}
            </Text>
          </TouchableOpacity>

          {/* Divider */}
          <View style={styles.divider}>
            <View style={[styles.dividerLine, { borderColor: '#475569' }]} />
            <Text style={[styles.dividerText, { color: '#94a3b8' }]}>Or continue with</Text>
            <View style={[styles.dividerLine, { borderColor: '#475569' }]} />
          </View>

          {/* Social Buttons */}
          <View style={styles.socialButtons}>
            <TouchableOpacity style={[styles.socialButton, { backgroundColor: '#334155' }]}>
              <Icon name="smartphone" size={20} color="#fff" />
            </TouchableOpacity>
            <TouchableOpacity style={[styles.socialButton, { backgroundColor: '#334155' }]}>
              <Icon name="code" size={20} color="#fff" />
            </TouchableOpacity>
          </View>

          <Text style={[styles.switchText, { color: '#94a3b8' }]}>
            {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
            <Text 
              style={{ color: theme.primary, fontWeight: '600' }}
              onPress={() => setIsRegister(!isRegister)}
            >
              {isRegister ? 'Sign In' : 'Register'}
            </Text>
          </Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

// Home Screen
function HomeScreen({ navigation }: any) {
  return (
    <ScrollView style={styles.screen}>
      <View style={[styles.header, { backgroundColor: '#0284c7' }]}>
        <Text style={styles.headerTitle}>Welcome to Nexora</Text>
        <Text style={styles.headerSubtitle}>Your all-in-one super app</Text>
      </View>

      <View style={styles.gridContainer}>
        {[
          { name: 'Social', icon: 'people', color: '#8b5cf6', screen: 'Social' },
          { name: 'Market', icon: 'store', color: '#3b82f6', screen: 'Marketplace' },
          { name: 'Food', icon: 'restaurant', color: '#f97316', screen: 'Food' },
          { name: 'Ride', icon: 'directions-car', color: '#22c55e', screen: 'Ride' },
          { name: 'Video', icon: 'play-circle', color: '#ec4899', screen: 'Video' },
          { name: 'Wallet', icon: 'account-balance-wallet', color: '#eab308', screen: 'Wallet' },
        ].map((item, index) => (
          <TouchableOpacity 
            key={index}
            style={styles.gridItem}
            onPress={() => navigation.navigate(item.screen)}
          >
            <View style={[styles.gridIcon, { backgroundColor: item.color }]}>
              <Icon name={item.icon} size={24} color="#fff" />
            </View>
            <Text style={styles.gridLabel}>{item.name}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </ScrollView>
  );
}

// Marketplace Screen
function MarketplaceScreen() {
  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Marketplace</Text>
      <View style={styles.searchContainer}>
        <Icon name="search" size={20} color="#64748b" />
        <TextInput 
          style={styles.searchInput}
          placeholder="Search products..."
          placeholderTextColor="#94a3b8"
        />
      </View>
      <View style={styles.productGrid}>
        {mockProducts.map((product) => (
          <View key={product.id} style={styles.productCard}>
            <Image source={{ uri: product.imageUrl }} style={styles.productImage} />
            <View style={styles.productInfo}>
              <Text style={styles.productName}>{product.name}</Text>
              <Text style={styles.productSeller}>{product.seller}</Text>
              <Text style={styles.productPrice}>${product.price}</Text>
              <TouchableOpacity style={styles.addButton}>
                <Text style={styles.addButtonText}>Add to Cart</Text>
              </TouchableOpacity>
            </View>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

// Food Delivery Screen
function FoodDeliveryScreen() {
  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Food Delivery</Text>
      <TextInput 
        style={styles.addressInput}
        placeholder="Enter delivery address..."
        placeholderTextColor="#94a3b8"
      />
      <Text style={styles.sectionTitle}>Popular Restaurants</Text>
      {mockRestaurants.map((restaurant) => (
        <View key={restaurant.id} style={styles.restaurantCard}>
          <Image source={{ uri: restaurant.imageUrl }} style={styles.restaurantImage} />
          <View style={styles.restaurantInfo}>
            <Text style={styles.restaurantName}>{restaurant.name}</Text>
            <Text style={styles.restaurantCuisine}>{restaurant.cuisine}</Text>
            <View style={styles.restaurantMeta}>
              <Icon name="star" size={16} color="#fbbf24" />
              <Text style={styles.rating}>{restaurant.rating}</Text>
              <Text style={styles.deliveryTime}>• {restaurant.deliveryTime}</Text>
            </View>
          </View>
        </View>
      ))}
    </ScrollView>
  );
}

// Ride Screen
function RideScreen() {
  const [pickup, setPickup] = useState('');
  const [dropoff, setDropoff] = useState('');

  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Ride</Text>
      <View style={styles.rideCard}>
        <View style={styles.locationInput}>
          <Icon name="radio-button-checked" size={20} color="#22c55e" />
          <TextInput 
            style={styles.locationInputField}
            value={pickup}
            onChangeText={setPickup}
            placeholder="Pickup location"
            placeholderTextColor="#94a3b8"
          />
        </View>
        <View style={styles.locationInput}>
          <Icon name="radio-button-checked" size={20} color="#ef4444" />
          <TextInput 
            style={styles.locationInputField}
            value={dropoff}
            onChangeText={setDropoff}
            placeholder="Drop-off location"
            placeholderTextColor="#94a3b8"
          />
        </View>
        <TouchableOpacity style={styles.requestButton}>
          <Text style={styles.requestButtonText}>Request Ride</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.sectionTitle}>Ride Options</Text>
      <View style={styles.rideOptions}>
        {['Economy', 'Comfort', 'Premium'].map((type) => (
          <View key={type} style={styles.rideOption}>
            <Icon name="directions-car" size={32} color="#64748b" />
            <Text style={styles.rideType}>{type}</Text>
            <Text style={styles.ridePrice}>${(Math.random() * 20 + 10).toFixed(2)}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

// Video Screen
function VideoScreen() {
  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Video</Text>
      {mockVideos.map((video) => (
        <View key={video.id} style={styles.videoCard}>
          <Image source={{ uri: video.thumbnailUrl }} style={styles.videoThumbnail} />
          <View style={styles.videoInfo}>
            <Text style={styles.videoTitle}>{video.title}</Text>
            <Text style={styles.videoCreator}>{video.creator}</Text>
            <View style={styles.videoMeta}>
              <Text style={styles.videoViews}>{(video.views / 1000).toFixed(1)}K views</Text>
              <Text style={styles.videoDuration}>{video.duration}</Text>
            </View>
          </View>
        </View>
      ))}
    </ScrollView>
  );
}

// Wallet Screen
function WalletScreen() {
  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Wallet</Text>
      <View style={[styles.walletCard, { backgroundColor: '#0284c7' }]}>
        <Text style={styles.walletLabel}>Available Balance</Text>
        <Text style={styles.walletBalance}>${mockWallet.balance.toFixed(2)}</Text>
        <View style={styles.walletButtons}>
          <TouchableOpacity style={styles.walletButton}>
            <Text style={styles.walletButtonText}>Add Money</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.walletButton}>
            <Text style={styles.walletButtonText}>Send</Text>
          </TouchableOpacity>
        </View>
      </View>

      <Text style={styles.sectionTitle}>Recent Transactions</Text>
      {mockWallet.transactions.map((tx) => (
        <View key={tx.id} style={styles.transaction}>
          <View>
            <Text style={styles.txType}>{tx.type}</Text>
            <Text style={styles.txDate}>{tx.date}</Text>
          </View>
          <Text style={[styles.txAmount, { color: tx.amount > 0 ? '#22c55e' : '#ef4444' }]}>
            {tx.amount > 0 ? '+' : ''}${tx.amount}
          </Text>
        </View>
      ))}
    </ScrollView>
  );
}

// Messages Screen
function MessagesScreen() {
  const messages = [
    { id: '1', name: 'John Doe', lastMessage: 'Hey, how are you?', time: '2 min ago', unread: true },
    { id: '2', name: 'Jane Smith', lastMessage: 'See you tomorrow!', time: '1 hour ago', unread: false },
    { id: '3', name: 'Tech Store', lastMessage: 'Your order has been shipped', time: '3 hours ago', unread: true },
  ];

  return (
    <ScrollView style={styles.screen}>
      <Text style={styles.pageTitle}>Messages</Text>
      {messages.map((msg) => (
        <TouchableOpacity key={msg.id} style={styles.messageItem}>
          <View style={[styles.avatar, msg.unread && styles.unreadAvatar]}>
            <Text style={styles.avatarText}>{msg.name[0]}</Text>
          </View>
          <View style={styles.messageContent}>
            <Text style={[styles.messageName, msg.unread && styles.unreadText]}>{msg.name}</Text>
            <Text style={styles.messageText}>{msg.lastMessage}</Text>
          </View>
          <Text style={styles.messageTime}>{msg.time}</Text>
        </TouchableOpacity>
      ))}
    </ScrollView>
  );
}

// Profile Screen
function ProfileScreen({ navigation }: any) {
  const { user, logout } = React.useContext(AuthContext);

  return (
    <ScrollView style={styles.screen}>
      <View style={styles.profileHeader}>
        <View style={[styles.profileAvatar, { backgroundColor: '#0284c7' }]}>
          <Text style={styles.profileAvatarText}>{user?.username?.[0]?.toUpperCase() || 'U'}</Text>
        </View>
        <Text style={styles.profileName}>{user?.username || 'User'}</Text>
        <Text style={styles.profileEmail}>{user?.email || 'user@example.com'}</Text>
      </View>

      <View style={styles.profileMenu}>
        {[
          { name: 'Edit Profile', icon: 'edit' },
          { name: 'Settings', icon: 'settings' },
          { name: 'Notifications', icon: 'notifications' },
          { name: 'Help & Support', icon: 'help' },
        ].map((item, index) => (
          <TouchableOpacity key={index} style={styles.menuItem}>
            <Icon name={item.icon} size={24} color="#64748b" />
            <Text style={styles.menuText}>{item.name}</Text>
            <Icon name="chevron-right" size={24} color="#94a3b8" />
          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity 
        style={styles.logoutButton}
        onPress={() => {
          logout();
        }}
      >
        <Icon name="logout" size={24} color="#ef4444" />
        <Text style={styles.logoutText}>Log Out</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

// Tab Navigator
const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: string = 'home';
          if (route.name === 'HomeTab') iconName = 'home';
          else if (route.name === 'Messages') iconName = 'chat';
          else if (route.name === 'Market') iconName = 'store';
          else if (route.name === 'Food') iconName = 'restaurant';
          else if (route.name === 'Ride') iconName = 'directions-car';
          else if (route.name === 'Wallet') iconName = 'account-balance-wallet';
          return <Icon name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#0284c7',
        tabBarInactiveTintColor: 'gray',
        headerShown: false,
        tabBarStyle: {
          backgroundColor: '#fff',
          borderTopColor: '#e2e8f0',
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
      })}
    >
      <Tab.Screen name="HomeTab" component={HomeStackNavigator} options={{ title: 'Home' }} />
      <Tab.Screen name="Messages" component={MessagesScreen} />
      <Tab.Screen name="Market" component={MarketplaceScreen} />
      <Tab.Screen name="Food" component={FoodDeliveryScreen} />
      <Tab.Screen name="Ride" component={RideScreen} />
      <Tab.Screen name="Wallet" component={WalletScreen} />
    </Tab.Navigator>
  );
}

function HomeStackNavigator() {
  const { user } = React.useContext(AuthContext);
  
  return (
    <Stack.Navigator>
      <Stack.Screen name="Home" component={HomeScreen} options={{ 
        headerShown: true,
        headerTitle: 'Nexora',
        headerStyle: { backgroundColor: '#0284c7' },
        headerTintColor: '#fff',
        headerRight: () => (
          <TouchableOpacity onPress={() => {}}>
            <Icon name="person" size={24} color="#fff" />
          </TouchableOpacity>
        ),
      }} />
      <Stack.Screen name="Profile" component={ProfileScreen} options={{ 
        headerShown: true,
        title: 'Profile',
        headerStyle: { backgroundColor: '#0284c7' },
        headerTintColor: '#fff',
      }} />
    </Stack.Navigator>
  );
}

// Auth Stack
function AuthStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
    </Stack.Navigator>
  );
}

// Main App
function App(): React.JSX.Element {
  const [user, setUser] = useState<User | null>(null);

  const login = (user: User) => setUser(user);
  const logout = () => setUser(null);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <NavigationContainer>
        {user ? <MainTabs /> : <AuthStack />}
      </NavigationContainer>
    </AuthContext.Provider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    padding: 20,
    justifyContent: 'center',
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 30,
  },
  logo: {
    width: 70,
    height: 70,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  logoText: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#fff',
  },
  appName: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 8,
  },
  tagline: {
    fontSize: 16,
    color: '#94a3b8',
  },
  formCard: {
    borderRadius: 20,
    padding: 24,
    borderWidth: 1,
    borderColor: '#334155',
  },
  inputContainer: {
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: '#e2e8f0',
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderRadius: 12,
    padding: 14,
    fontSize: 16,
  },
  eyeButton: {
    position: 'absolute',
    right: 12,
    top: 14,
  },
  forgotPassword: {
    textAlign: 'right',
    marginBottom: 16,
    fontSize: 14,
  },
  button: {
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 20,
  },
  dividerLine: {
    flex: 1,
    borderWidth: 0.5,
  },
  dividerText: {
    marginHorizontal: 12,
    fontSize: 12,
  },
  socialButtons: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 12,
    marginBottom: 20,
  },
  socialButton: {
    width: 50,
    height: 50,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  switchText: {
    textAlign: 'center',
    fontSize: 14,
  },
  screen: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  header: {
    padding: 24,
    paddingTop: 16,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerSubtitle: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
  gridContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 16,
    justifyContent: 'space-between',
  },
  gridItem: {
    width: '30%',
    alignItems: 'center',
    marginBottom: 16,
  },
  gridIcon: {
    width: 56,
    height: 56,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  gridLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: '#1e293b',
  },
  pageTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1e293b',
    padding: 16,
    paddingBottom: 8,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  searchInput: {
    flex: 1,
    marginLeft: 8,
    fontSize: 16,
    color: '#1e293b',
  },
  productGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 8,
  },
  productCard: {
    width: '48%',
    backgroundColor: '#fff',
    borderRadius: 12,
    margin: 8,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  productImage: {
    width: '100%',
    height: 120,
  },
  productInfo: {
    padding: 12,
  },
  productName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1e293b',
  },
  productSeller: {
    fontSize: 12,
    color: '#64748b',
    marginTop: 2,
  },
  productPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0284c7',
    marginTop: 4,
  },
  addButton: {
    backgroundColor: '#0284c7',
    padding: 8,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 8,
  },
  addButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  addressInput: {
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    padding: 14,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    fontSize: 16,
    color: '#1e293b',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1e293b',
    padding: 16,
    paddingBottom: 8,
  },
  restaurantCard: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    borderRadius: 12,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  restaurantImage: {
    width: 100,
    height: 100,
  },
  restaurantInfo: {
    flex: 1,
    padding: 12,
  },
  restaurantName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1e293b',
  },
  restaurantCuisine: {
    fontSize: 14,
    color: '#64748b',
    marginTop: 2,
  },
  restaurantMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  rating: {
    marginLeft: 4,
    fontSize: 14,
    color: '#1e293b',
  },
  deliveryTime: {
    marginLeft: 4,
    fontSize: 14,
    color: '#64748b',
  },
  rideCard: {
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  locationInput: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f1f5f9',
    borderRadius: 12,
    padding: 12,
    marginBottom: 12,
  },
  locationInputField: {
    flex: 1,
    marginLeft: 8,
    fontSize: 16,
    color: '#1e293b',
  },
  requestButton: {
    backgroundColor: '#22c55e',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 8,
  },
  requestButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  rideOptions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 16,
    paddingTop: 0,
  },
  rideOption: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    width: '30%',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  rideType: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1e293b',
    marginTop: 8,
  },
  ridePrice: {
    fontSize: 12,
    color: '#64748b',
    marginTop: 4,
  },
  videoCard: {
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    borderRadius: 12,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  videoThumbnail: {
    width: '100%',
    height: 180,
  },
  videoInfo: {
    padding: 12,
  },
  videoTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1e293b',
  },
  videoCreator: {
    fontSize: 14,
    color: '#64748b',
    marginTop: 4,
  },
  videoMeta: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  videoViews: {
    fontSize: 12,
    color: '#64748b',
  },
  videoDuration: {
    fontSize: 12,
    color: '#64748b',
  },
  walletCard: {
    margin: 16,
    marginTop: 0,
    borderRadius: 16,
    padding: 24,
  },
  walletLabel: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
  },
  walletBalance: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#fff',
    marginTop: 8,
  },
  walletButtons: {
    flexDirection: 'row',
    marginTop: 16,
    gap: 12,
  },
  walletButton: {
    flex: 1,
    backgroundColor: 'rgba(255,255,255,0.2)',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  walletButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  transaction: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 0,
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  txType: {
    fontSize: 16,
    fontWeight: '500',
    color: '#1e293b',
    textTransform: 'capitalize',
  },
  txDate: {
    fontSize: 12,
    color: '#64748b',
    marginTop: 2,
  },
  txAmount: {
    fontSize: 16,
    fontWeight: '600',
  },
  messageItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#64748b',
    justifyContent: 'center',
    alignItems: 'center',
  },
  unreadAvatar: {
    backgroundColor: '#0284c7',
  },
  avatarText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
  messageContent: {
    flex: 1,
    marginLeft: 12,
  },
  messageName: {
    fontSize: 16,
    fontWeight: '500',
    color: '#1e293b',
  },
  unreadText: {
    fontWeight: '700',
  },
  messageText: {
    fontSize: 14,
    color: '#64748b',
    marginTop: 2,
  },
  messageTime: {
    fontSize: 12,
    color: '#94a3b8',
  },
  profileHeader: {
    alignItems: 'center',
    padding: 24,
    backgroundColor: '#fff',
    marginBottom: 16,
  },
  profileAvatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  profileAvatarText: {
    color: '#fff',
    fontSize: 32,
    fontWeight: 'bold',
  },
  profileName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1e293b',
    marginTop: 16,
  },
  profileEmail: {
    fontSize: 14,
    color: '#64748b',
    marginTop: 4,
  },
  profileMenu: {
    backgroundColor: '#fff',
    marginHorizontal: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  menuText: {
    flex: 1,
    marginLeft: 12,
    fontSize: 16,
    color: '#1e293b',
  },
  logoutButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#fff',
    margin: 16,
    marginTop: 24,
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#fecaca',
  },
  logoutText: {
    marginLeft: 8,
    fontSize: 16,
    fontWeight: '600',
    color: '#ef4444',
  },
});

export default App;
