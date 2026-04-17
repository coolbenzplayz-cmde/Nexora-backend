
import { BrowserRouter, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { MessageCircle, ShoppingBag, Home, User, Wallet, Video, Utensils, Car, LogOut, Menu, X, MapPin, Eye, EyeOff, AlertCircle, Loader2, ArrowRight, Image, Heart, Share2, Search, Gamepad2, Scissors, CreditCard, Crown, Megaphone, ShoppingCart, Sparkles, Zap, Puzzle, Dice5, Trophy, Star, Gift, Gem, DollarSign, TrendingUp, Shield } from 'lucide-react';

// Types
interface User {
    id: string;
    username: string;
    email: string;
    role: string;
}

interface Post {
    id: string;
    content: string;
    imageUrl?: string;
    author: { username: string };
    likes: number;
    comments: number;
    createdAt: string;
}

interface VideoItem {
    id: string;
    title: string;
    thumbnailUrl: string;
    views: number;
    creator: string;
    duration: string;
}

interface WalletData {
    balance: number;
    transactions: { id: string; amount: number; type: string; date: string }[];
}

// Auth Service
const authService = {
    login: async (email: string, password: string) => {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });
        return response.json();
    },
    register: async (username: string, email: string, password: string) => {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password }),
        });
        return response.json();
    },
};

// Mock Data
const mockPosts: Post[] = [
    { id: '1', content: 'Welcome to Nexora! Your all-in-one digital super app.', imageUrl: '', author: { username: 'Nexora' }, likes: 42, comments: 12, createdAt: new Date().toISOString() },
    { id: '2', content: 'Check out our new marketplace features!', imageUrl: '', author: { username: 'Admin' }, likes: 28, comments: 5, createdAt: new Date().toISOString() },
];

const mockProducts: Product[] = [
    { id: '1', name: 'Smartphone Pro', price: 699, imageUrl: 'https://picsum.photos/200/200?random=1', seller: 'TechStore' },
    { id: '2', name: 'Wireless Earbuds', price: 149, imageUrl: 'https://picsum.photos/200/200?random=2', seller: 'AudioWorld' },
    { id: '3', name: 'Laptop Ultra', price: 1299, imageUrl: 'https://picsum.photos/200/200?random=3', seller: 'TechStore' },
    { id: '4', name: 'Smart Watch', price: 299, imageUrl: 'https://picsum.photos/200/200?random=4', seller: 'GadgetHub' },
];

const mockRestaurants: Restaurant[] = [
    { id: '1', name: 'Burger Palace', cuisine: 'American', rating: 4.5, deliveryTime: '25-35 min', imageUrl: 'https://picsum.photos/200/150?random=5' },
    { id: '2', name: 'Pizza Heaven', cuisine: 'Italian', rating: 4.8, deliveryTime: '30-40 min', imageUrl: 'https://picsum.photos/200/150?random=6' },
    { id: '3', name: 'Sushi Master', cuisine: 'Japanese', rating: 4.7, deliveryTime: '35-45 min', imageUrl: 'https://picsum.photos/200/150?random=7' },
    { id: '4', name: 'Tandoori Grill', cuisine: 'Indian', rating: 4.6, deliveryTime: '20-30 min', imageUrl: 'https://picsum.photos/200/150?random=8' },
];

const mockVideos: VideoItem[] = [
    { id: '1', title: 'How to use Nexora', thumbnailUrl: 'https://picsum.photos/300/200?random=9', views: 15000, creator: 'Nexora Official', duration: '5:30' },
    { id: '2', title: 'Creator Economy Guide', thumbnailUrl: 'https://picsum.photos/300/200?random=10', views: 8500, creator: 'Tech Influencer', duration: '12:45' },
    { id: '3', title: 'Maximizing Your Earnings', thumbnailUrl: 'https://picsum.photos/300/200?random=11', views: 12000, creator: 'Business Pro', duration: '8:20' },
];

const mockWallet: WalletData = {
    balance: 1250.00,
    transactions: [
        { id: '1', amount: 500, type: 'deposit', date: '2026-04-15' },
        { id: '2', amount: -120, type: 'purchase', date: '2026-04-14' },
        { id: '3', amount: 850, type: 'deposit', date: '2026-04-10' },
    ],
};

// Components
function Navbar({ user, onLogout, activeTab, setActiveTab }: { user: User | null; onLogout: () => void; activeTab: string; setActiveTab: (tab: string) => void }) {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const navItems = [
        { id: 'home', icon: Home, label: 'Home' },
        { id: 'social', icon: MessageCircle, label: 'Social' },
        { id: 'marketplace', icon: ShoppingBag, label: 'Market' },
        { id: 'food', icon: Utensils, label: 'Food' },
        { id: 'ride', icon: Car, label: 'Ride' },
        { id: 'video', icon: Video, label: 'Video' },
        { id: 'wallet', icon: Wallet, label: 'Wallet' },
        { id: 'games', icon: Gamepad2, label: 'Games' },
        { id: 'editor', icon: Scissors, label: 'Editor' },
        { id: 'creator', icon: Star, label: 'Creator' },
        { id: 'credits', icon: Gem, label: 'Credits' },
        { id: 'premium', icon: Crown, label: 'Premium' },
        { id: 'admin', icon: Shield, label: 'Admin' },
    ];

    return (
        <nav className="bg-white shadow-md sticky top-0 z-50">
            <div className="max-w-7xl mx-auto px-4">
                <div className="flex justify-between items-center h-16">
                    <div className="flex items-center space-x-2">
                        <span className="text-2xl font-bold text-primary-600">N</span>
                        <span className="text-xl font-bold text-gray-800">exora</span>
                    </div>

                    <div className="hidden md:flex items-center space-x-1">
                        {navItems.map((item) => (
                            <button
                                key={item.id}
                                onClick={() => setActiveTab(item.id)}
                                className={`px-3 py-2 rounded-lg flex items-center space-x-1 transition-colors ${
                                    activeTab === item.id
                                        ? 'bg-primary-100 text-primary-600'
                                        : 'text-gray-600 hover:bg-gray-100'
                                }`}
                            >
                                <item.icon size={18} />
                                <span className="text-sm">{item.label}</span>
                            </button>
                        ))}
                    </div>

                    <div className="flex items-center space-x-3">
                        {user ? (
                            <div className="flex items-center space-x-3">
                                <span className="text-sm text-gray-600">{user.username}</span>
                                <button
                                    onClick={onLogout}
                                    className="p-2 text-gray-600 hover:text-red-500 transition-colors"
                                >
                                    <LogOut size={18} />
                                </button>
                            </div>
                        ) : (
                            <Link
                                to="/login"
                                className="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700 transition-colors"
                            >
                                Sign In
                            </Link>
                        )}
                        <button
                            className="md:hidden p-2 text-gray-600"
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        >
                            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
                        </button>
                    </div>
                </div>

                {mobileMenuOpen && (
                    <div className="md:hidden py-4 border-t">
                        <div className="grid grid-cols-4 gap-2">
                            {navItems.map((item) => (
                                <button
                                    key={item.id}
                                    onClick={() => {
                                        setActiveTab(item.id);
                                        setMobileMenuOpen(false);
                                    }}
                                    className={`p-2 rounded-lg flex flex-col items-center ${
                                        activeTab === item.id
                                            ? 'bg-primary-100 text-primary-600'
                                            : 'text-gray-600'
                                    }`}
                                >
                                    <item.icon size={20} />
                                    <span className="text-xs mt-1">{item.label}</span>
                                </button>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </nav>
    );
}

function LoginPage({ onLogin }: { onLogin: (user: User) => void }) {
    const [isRegister, setIsRegister] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            if (isRegister) {
                const response = await authService.register(formData.username, formData.email, formData.password);
                if (response.success) {
                    onLogin(response.data.user);
                } else {
                    setError(response.message || 'Registration failed');
                }
            } else {
                const response = await authService.login(formData.email, formData.password);
                if (response.success) {
                    onLogin(response.data.user);
                } else {
                    setError(response.message || 'Login failed');
                }
            }
        } catch {
            // For demo, use mock login
            onLogin({ id: '1', username: formData.username || 'demo', email: formData.email, role: 'USER' });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-900 via-primary-900 to-slate-900 flex items-center justify-center p-4">
            {/* Background decorative elements */}
            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute -top-40 -right-40 w-80 h-80 bg-primary-500/20 rounded-full blur-3xl"></div>
                <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-purple-500/20 rounded-full blur-3xl"></div>
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-primary-600/10 rounded-full blur-3xl"></div>
            </div>
            
            <div className="relative w-full max-w-md">
                {/* Logo */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-500 to-primary-700 rounded-2xl mb-4 shadow-lg shadow-primary-500/30">
                        <span className="text-3xl font-bold text-white">N</span>
                    </div>
                    <h1 className="text-3xl font-bold text-white">
                        {isRegister ? 'Create Account' : 'Welcome Back'}
                    </h1>
                    <p className="text-slate-400 mt-2">
                        {isRegister ? 'Join the Nexora ecosystem' : 'Sign in to continue to your super app'}
                    </p>
                </div>

                <div className="bg-slate-800/50 backdrop-blur-xl rounded-2xl p-8 border border-slate-700/50 shadow-2xl">
                    <form onSubmit={handleSubmit} className="space-y-5">
                        {isRegister && (
                            <div>
                                <label className="block text-sm font-medium text-slate-300 mb-2">Username</label>
                                <div className="relative">
                                    <input
                                        type="text"
                                        value={formData.username}
                                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                        className="w-full px-4 py-3 bg-slate-700/50 border border-slate-600 rounded-xl text-white placeholder-slate-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
                                        placeholder="johndoe"
                                        required={isRegister}
                                    />
                                </div>
                            </div>
                        )}
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Email</label>
                            <div className="relative">
                                <input
                                    type="email"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    className="w-full px-4 py-3 bg-slate-700/50 border border-slate-600 rounded-xl text-white placeholder-slate-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all"
                                    placeholder="you@example.com"
                                    required
                                />
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-300 mb-2">Password</label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    value={formData.password}
                                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                    className="w-full px-4 py-3 bg-slate-700/50 border border-slate-600 rounded-xl text-white placeholder-slate-400 focus:ring-2 focus:ring-primary-500 focus:border-primary-500 transition-all pr-12"
                                    placeholder="••••••••"
                                    required
                                    minLength={6}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white transition-colors"
                                >
                                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>

                        {!isRegister && (
                            <div className="flex items-center justify-between">
                                <label className="flex items-center">
                                    <input type="checkbox" className="w-4 h-4 rounded border-slate-600 bg-slate-700 text-primary-500 focus:ring-primary-500" />
                                    <span className="ml-2 text-sm text-slate-400">Remember me</span>
                                </label>
                                <button type="button" className="text-sm text-primary-400 hover:text-primary-300">Forgot password?</button>
                            </div>
                        )}

                        {error && (
                            <div className="flex items-center gap-2 p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
                                <AlertCircle className="text-red-400 flex-shrink-0" size={18} />
                                <p className="text-red-400 text-sm">{error}</p>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full py-3 bg-gradient-to-r from-primary-600 to-primary-700 text-white rounded-xl font-semibold hover:from-primary-500 hover:to-primary-600 transition-all transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                        >
                            {isLoading ? (
                                <>
                                    <Loader2 className="animate-spin" size={20} />
                                    Please wait...
                                </>
                            ) : (
                                <>
                                    {isRegister ? 'Create Account' : 'Sign In'}
                                    <ArrowRight size={18} />
                                </>
                            )}
                        </button>
                    </form>

                    {/* Divider */}
                    <div className="relative my-6">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-slate-700"></div>
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="px-4 bg-slate-800 text-slate-500">Or continue with</span>
                        </div>
                    </div>

                    {/* Social Login */}
                    <div className="grid grid-cols-2 gap-3">
                        <button className="flex items-center justify-center gap-2 py-2.5 bg-slate-700/50 hover:bg-slate-700 border border-slate-600 rounded-xl transition-all">
                            <svg className="w-5 h-5" viewBox="0 0 24 24"><path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                            <span className="text-slate-300 text-sm font-medium">Google</span>
                        </button>
                        <button className="flex items-center justify-center gap-2 py-2.5 bg-slate-700/50 hover:bg-slate-700 border border-slate-600 rounded-xl transition-all">
                            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
                            <span className="text-slate-300 text-sm font-medium">GitHub</span>
                        </button>
                    </div>

                    <p className="text-center mt-6 text-slate-400">
                        {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
                        <button
                            onClick={() => { setIsRegister(!isRegister); setError(''); }}
                            className="text-primary-400 font-medium hover:text-primary-300 transition-colors"
                        >
                            {isRegister ? 'Sign In' : 'Register now'}
                        </button>
                    </p>
                </div>

                {/* Footer */}
                <p className="text-center mt-6 text-slate-500 text-sm">
                    By continuing, you agree to our Terms of Service and Privacy Policy
                </p>
            </div>
        </div>
    );
}

function HomePage() {
    return (
        <div className="p-6">
            <div className="bg-gradient-to-r from-primary-600 to-primary-800 rounded-2xl p-8 text-white mb-8">
                <h1 className="text-3xl font-bold mb-2">Welcome to Nexora</h1>
                <p className="text-primary-100">Your all-in-one digital super app ecosystem</p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
                {[
                    { icon: MessageCircle, label: 'Social', color: 'bg-violet-500' },
                    { icon: ShoppingBag, label: 'Marketplace', color: 'bg-blue-500' },
                    { icon: Utensils, label: 'Food Delivery', color: 'bg-orange-500' },
                    { icon: Car, label: 'Ride Hailing', color: 'bg-green-500' },
                    { icon: Video, label: 'Video Platform', color: 'bg-pink-500' },
                    { icon: Wallet, label: 'Wallet', color: 'bg-yellow-500' },
                ].map((service, index) => (
                    <div key={index} className="bg-white rounded-xl p-4 shadow-sm hover:shadow-md transition-shadow cursor-pointer text-center">
                        <div className={`${service.color} w-12 h-12 rounded-xl flex items-center justify-center mb-3 mx-auto`}>
                            <service.icon className="text-white" size={22} />
                        </div>
                        <h3 className="font-semibold text-gray-800 text-sm">{service.label}</h3>
                    </div>
                ))}
            </div>

            <div className="bg-white rounded-xl p-6 shadow-sm">
                <h2 className="text-xl font-bold text-gray-800 mb-4">Latest Updates</h2>
                <div className="space-y-4">
                    {mockPosts.map((post) => (
                        <div key={post.id} className="border-b pb-4 last:border-0">
                            <p className="text-gray-700">{post.content}</p>
                            <div className="flex items-center mt-2 text-sm text-gray-500">
                                <span>@{post.author.username}</span>
                                <span className="mx-2">•</span>
                                <span>{post.likes} likes</span>
                                <span className="mx-2">•</span>
                                <span>{post.comments} comments</span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="mt-8 bg-gradient-to-r from-violet-600 to-purple-700 rounded-2xl p-6 text-white">
                <h2 className="text-xl font-bold mb-2">Go Premium</h2>
                <p className="text-violet-100 mb-4">Unlock all features with Nexora Premium</p>
                <button className="bg-white text-purple-600 px-6 py-2 rounded-lg font-semibold hover:bg-violet-50 transition-colors">
                    Learn More
                </button>
            </div>
        </div>
    );
}

function SocialPage() {
    const [posts, setPosts] = useState<Post[]>(mockPosts);
    const [newPost, setNewPost] = useState('');

    const handlePost = () => {
        if (newPost.trim()) {
            const post: Post = {
                id: Date.now().toString(),
                content: newPost,
                author: { username: 'You' },
                likes: 0,
                comments: 0,
                createdAt: new Date().toISOString(),
            };
            setPosts([post, ...posts]);
            setNewPost('');
        }
    };

    return (
        <div className="p-6 max-w-2xl mx-auto">
            <div className="bg-white rounded-xl p-4 shadow-sm mb-6">
        <textarea
            value={newPost}
            onChange={(e) => setNewPost(e.target.value)}
            placeholder="What's on your mind?"
            className="w-full p-3 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            rows={3}
        />
                <div className="flex justify-between items-center mt-3">
                    <div className="flex space-x-2">
                        <button className="p-2 text-gray-500 hover:text-primary-600 hover:bg-gray-100 rounded-lg">
                            <Image size={20} />
                        </button>
                        <button className="p-2 text-gray-500 hover:text-primary-600 hover:bg-gray-100 rounded-lg">
                            <Video size={20} />
                        </button>
                    </div>
                    <button
                        onClick={handlePost}
                        className="px-4 py-2 bg-primary-600 text-white rounded-lg font-medium hover:bg-primary-700 transition-colors"
                    >
                        Post
                    </button>
                </div>
            </div>

            <div className="space-y-4">
                {posts.map((post) => (
                    <div key={post.id} className="bg-white rounded-xl p-4 shadow-sm">
                        <div className="flex items-center mb-3">
                            <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                                <span className="text-primary-600 font-bold">{post.author.username[0]}</span>
                            </div>
                            <div className="ml-3">
                                <p className="font-semibold text-gray-800">{post.author.username}</p>
                                <p className="text-xs text-gray-500">{new Date(post.createdAt).toLocaleDateString()}</p>
                            </div>
                        </div>
                        <p className="text-gray-700 mb-3">{post.content}</p>
                        {post.imageUrl && (
                            <img src={post.imageUrl} alt="Post" className="w-full rounded-lg mb-3" />
                        )}
                        <div className="flex items-center space-x-4 pt-3 border-t">
                            <button className="flex items-center space-x-1 text-gray-500 hover:text-red-500">
                                <Heart size={18} />
                                <span>{post.likes}</span>
                            </button>
                            <button className="flex items-center space-x-1 text-gray-500 hover:text-primary-600">
                                <MessageCircle size={18} />
                                <span>{post.comments}</span>
                            </button>
                            <button className="flex items-center space-x-1 text-gray-500 hover:text-green-600">
                                <Share2 size={18} />
                                <span>Share</span>
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function MarketplacePage() {
    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Marketplace</h1>
                <div className="relative">
                    <input
                        type="text"
                        placeholder="Search products..."
                        className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                    />
                    <Search className="absolute left-3 top-2.5 text-gray-400" size={18} />
                </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {mockProducts.map((product) => (
                    <div key={product.id} className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow overflow-hidden">
                        <img src={product.imageUrl} alt={product.name} className="w-full h-40 object-cover" />
                        <div className="p-4">
                            <h3 className="font-semibold text-gray-800">{product.name}</h3>
                            <p className="text-sm text-gray-500">{product.seller}</p>
                            <p className="text-primary-600 font-bold mt-2">${product.price}</p>
                            <button className="w-full mt-3 py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function FoodDeliveryPage() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Food Delivery</h1>

            <div className="mb-6">
                <input
                    type="text"
                    placeholder="Enter delivery address..."
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg"
                />
            </div>

            <div className="grid grid-cols-2 gap-4">
                {mockRestaurants.map((restaurant) => (
                    <div key={restaurant.id} className="bg-white rounded-xl shadow-sm overflow-hidden">
                        <img src={restaurant.imageUrl} alt={restaurant.name} className="w-full h-32 object-cover" />
                        <div className="p-4">
                            <h3 className="font-semibold text-gray-800">{restaurant.name}</h3>
                            <p className="text-sm text-gray-500">{restaurant.cuisine}</p>
                            <div className="flex items-center mt-2">
                                <span className="text-yellow-500">★</span>
                                <span className="ml-1 text-sm">{restaurant.rating}</span>
                                <span className="mx-2">•</span>
                                <span className="text-sm text-gray-500">{restaurant.deliveryTime}</span>
                            </div>
                            <button className="w-full mt-3 py-2 bg-orange-500 text-white rounded-lg text-sm font-medium hover:bg-orange-600">
                                Order Now
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function RidePage() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Ride Hailing</h1>

            <div className="bg-white rounded-xl p-6 shadow-sm mb-6">
                <div className="space-y-4">
                    <div className="flex items-center p-3 bg-gray-50 rounded-lg">
                        <MapPin className="text-green-500" size={20} />
                        <input
                            type="text"
                            placeholder="Pickup location"
                            className="ml-3 flex-1 bg-transparent"
                        />
                    </div>
                    <div className="flex items-center p-3 bg-gray-50 rounded-lg">
                        <MapPin className="text-red-500" size={20} />
                        <input
                            type="text"
                            placeholder="Drop-off location"
                            className="ml-3 flex-1 bg-transparent"
                        />
                    </div>
                </div>

                <button className="w-full mt-6 py-3 bg-green-500 text-white rounded-lg font-medium hover:bg-green-600">
                    Request Ride
                </button>
            </div>

            <div className="grid grid-cols-3 gap-4">
                {['Economy', 'Comfort', 'Premium'].map((type) => (
                    <div key={type} className="bg-white rounded-xl p-4 shadow-sm text-center">
                        <Car className="mx-auto text-gray-400 mb-2" size={24} />
                        <h3 className="font-semibold text-gray-800">{type}</h3>
                        <p className="text-sm text-gray-500">${(Math.random() * 20 + 10).toFixed(2)}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}

function VideoPage() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Video Platform</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {mockVideos.map((video) => (
                    <div key={video.id} className="bg-white rounded-xl shadow-sm overflow-hidden">
                        <div className="relative">
                            <img src={video.thumbnailUrl} alt={video.title} className="w-full h-40 object-cover" />
                            <span className="absolute bottom-2 right-2 bg-black/70 text-white text-xs px-2 py-1 rounded">
                {video.duration}
              </span>
                        </div>
                        <div className="p-4">
                            <h3 className="font-semibold text-gray-800 mb-2 line-clamp-2">{video.title}</h3>
                            <div className="flex items-center justify-between text-sm text-gray-500">
                                <span>{video.creator}</span>
                                <span>{(video.views / 1000).toFixed(1)}K views</span>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

function WalletPage() {
    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Wallet</h1>

            <div className="bg-gradient-to-r from-primary-600 to-primary-800 rounded-xl p-6 text-white mb-6">
                <p className="text-primary-100">Available Balance</p>
                <p className="text-4xl font-bold mt-2">${mockWallet.balance.toFixed(2)}</p>
                <div className="flex space-x-3 mt-4">
                    <button className="flex-1 py-2 bg-white/20 rounded-lg text-sm font-medium hover:bg-white/30">
                        Add Money
                    </button>
                    <button className="flex-1 py-2 bg-white/20 rounded-lg text-sm font-medium hover:bg-white/30">
                        Send
                    </button>
                </div>
            </div>

            <div className="bg-white rounded-xl p-6 shadow-sm">
                <h2 className="font-semibold text-gray-800 mb-4">Recent Transactions</h2>
                <div className="space-y-3">
                    {mockWallet.transactions.map((tx) => (
                        <div key={tx.id} className="flex items-center justify-between py-3 border-b last:border-0">
                            <div>
                                <p className="font-medium text-gray-800">{tx.type}</p>
                                <p className="text-sm text-gray-500">{tx.date}</p>
                            </div>
                            <p className={`font-semibold ${tx.amount > 0 ? 'text-green-500' : 'text-red-500'}`}>
                                {tx.amount > 0 ? '+' : ''}${tx.amount}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

// Mini Games Page with Rewards
function GamesPage() {
    const [coins, setCoins] = useState(150);
    const [currentGame, setCurrentGame] = useState<string | null>(null);
    const [gameScore, setGameScore] = useState(0);

    const games = [
        { id: 'quiz', name: 'Daily Quiz', icon: Puzzle, color: 'bg-purple-500', reward: 10, description: 'Answer daily trivia questions' },
        { id: 'spin', name: 'Lucky Spin', icon: Dice5, color: 'bg-blue-500', reward: 15, description: 'Spin wheel for rewards' },
        { id: 'match', name: 'Memory Match', icon: Zap, color: 'bg-yellow-500', reward: 20, description: 'Find matching pairs' },
        { id: 'quiz2', name: 'Trivia Race', icon: Trophy, color: 'bg-green-500', reward: 25, description: 'Fastest answers win' },
    ];

    const handlePlayGame = (gameId: string) => {
        setCurrentGame(gameId);
        setGameScore(0);
        // Simulate playing
        setTimeout(() => {
            const reward = games.find(g => g.id === gameId)?.reward || 10;
            setCoins(prev => prev + reward);
            alert(`Congratulations! You earned ${reward} coins (${(reward * 0.001).toFixed(2)})!`);
            setCurrentGame(null);
        }, 3000);
    };

    return (
        <div className="p-6">
            <div className="bg-gradient-to-r from-violet-600 to-purple-700 rounded-2xl p-6 text-white mb-6">
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-violet-200">Your Coins</p>
                        <p className="text-4xl font-bold mt-1">{coins}</p>
                        <p className="text-violet-200 text-sm mt-1">= ${(coins * 0.001).toFixed(2)} USD</p>
                    </div>
                    <div className="bg-white/20 p-4 rounded-xl">
                        <Gem size={32} />
                    </div>
                </div>
            </div>

            <h2 className="text-xl font-bold text-gray-800 mb-4">Play & Earn</h2>
            <div className="grid grid-cols-2 gap-4">
                {games.map((game) => (
                    <div key={game.id} className="bg-white rounded-xl p-4 shadow-sm">
                        <div className={`${game.color} w-12 h-12 rounded-xl flex items-center justify-center mb-3`}>
                            <game.icon className="text-white" size={24} />
                        </div>
                        <h3 className="font-semibold text-gray-800">{game.name}</h3>
                        <p className="text-sm text-gray-500 mb-3">{game.description}</p>
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-yellow-600 font-medium">+{game.reward} coins</span>
                            <button
                                onClick={() => handlePlayGame(game.id)}
                                disabled={currentGame !== null}
                                className="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700 disabled:opacity-50"
                            >
                                {currentGame === game.id ? 'Playing...' : 'Play'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-6 bg-white rounded-xl p-4 shadow-sm">
                <h3 className="font-semibold text-gray-800 mb-3">How it works</h3>
                <ul className="space-y-2 text-sm text-gray-600">
                    <li className="flex items-center gap-2"><Gem size={16} className="text-yellow-500" /> 10 coins = $0.01 USD</li>
                    <li className="flex items-center gap-2"><Gift size={16} className="text-purple-500" /> Complete games daily</li>
                    <li className="flex items-center gap-2"><TrendingUp size={16} className="text-green-500" /> Earn more with streaks</li>
                </ul>
            </div>
        </div>
    );
}

// Video Editor Page (CapCut-like)
function EditorPage() {
    const [activeTool, setActiveTool] = useState('trim');
    const [isPremium, setIsPremium] = useState(false);

    const tools = [
        { id: 'trim', name: 'Trim', icon: Scissors, premium: false },
        { id: 'filters', name: 'Filters', icon: Sparkles, premium: false },
        { id: 'text', name: 'Text', icon: Type, premium: false },
        { id: 'music', name: 'Music', icon: Music, premium: false },
        { id: 'ai', name: 'AI Studio', icon: Zap, premium: true },
        { id: 'effects', name: 'Effects', icon: Wand2, premium: true },
        { id: 'export', name: 'Export', icon: Download, premium: false },
        { id: 'color', name: 'Color Grade', icon: Palette, premium: true },
    ];

    const premiumFeatures = [
        { name: '4K Export', price: '50 credits', description: 'Export in ultra HD quality' },
        { name: 'No Watermark', price: '100 credits', description: 'Remove Nexora watermark' },
        { name: 'AI Enhancement', price: '75 credits', description: 'AI-powered video enhancement' },
        { name: 'Cinematic Mode', price: '150 credits', description: 'Apply cinema-grade color grading' },
        { name: 'Anime Style', price: '200 credits', description: 'Transform video to anime style' },
    ];

    return (
        <div className="p-6">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Video Editor</h1>
                <button
                    onClick={() => setIsPremium(!isPremium)}
                    className={`px-4 py-2 rounded-lg font-medium ${isPremium ? 'bg-yellow-500 text-white' : 'bg-gray-200 text-gray-700'}`}
                >
                    {isPremium ? 'Premium' : 'Free'}
                </button>
            </div>

            {/* Editor Preview */}
            <div className="bg-black rounded-xl aspect-video mb-6 flex items-center justify-center">
                <div className="text-center">
                    <Video size={48} className="text-gray-600 mx-auto mb-2" />
                    <p className="text-gray-400">Tap to add video</p>
                </div>
            </div>

            {/* Tool Bar */}
            <div className="grid grid-cols-4 gap-2 mb-6">
                {tools.map((tool) => (
                    <button
                        key={tool.id}
                        onClick={() => setActiveTool(tool.id)}
                        className={`p-3 rounded-xl flex flex-col items-center transition-colors ${
                            activeTool === tool.id
                                ? 'bg-primary-100 text-primary-600'
                                : 'bg-white text-gray-600 hover:bg-gray-100'
                        }`}
                    >
                        <tool.icon size={20} />
                        <span className="text-xs mt-1">{tool.name}</span>
                        {tool.premium && <Gem size={12} className="text-yellow-500 absolute top-1 right-1" />}
                    </button>
                ))}
            </div>

            {/* Premium Features Section */}
            {isPremium && (
                <div className="bg-gradient-to-r from-yellow-50 to-amber-50 rounded-xl p-4 border border-yellow-200">
                    <h3 className="font-semibold text-yellow-800 mb-3">Premium Features</h3>
                    <div className="space-y-2">
                        {premiumFeatures.map((feature, index) => (
                            <div key={index} className="flex items-center justify-between bg-white rounded-lg p-3">
                                <div>
                                    <p className="font-medium text-gray-800">{feature.name}</p>
                                    <p className="text-xs text-gray-500">{feature.description}</p>
                                </div>
                                <span className="text-sm font-medium text-yellow-600">{feature.price}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Export Options */}
            <div className="bg-white rounded-xl p-4 shadow-sm mt-4">
                <h3 className="font-semibold text-gray-800 mb-3">Export Options</h3>
                <div className="grid grid-cols-3 gap-3">
                    <button className="p-3 bg-gray-100 rounded-lg text-center">
                        <p className="font-medium text-sm">720p</p>
                        <p className="text-xs text-gray-500">Free</p>
                    </button>
                    <button className={`p-3 rounded-lg text-center ${isPremium ? 'bg-primary-100' : 'bg-gray-100'}`}>
                        <p className="font-medium text-sm">1080p</p>
                        <p className="text-xs text-gray-500">{isPremium ? 'Included' : '50 credits'}</p>
                    </button>
                    <button className={`p-3 rounded-lg text-center ${isPremium ? 'bg-yellow-100' : 'bg-gray-100'}`}>
                        <p className="font-medium text-sm">4K</p>
                        <p className="text-xs text-gray-500">{isPremium ? 'Included' : '150 credits'}</p>
                    </button>
                </div>
            </div>
        </div>
    );
}

// Creator Economy Page
function CreatorPage() {
    const [earnings, setEarnings] = useState(1250);
    const [subscribers, setSubscribers] = useState(1250);

    const monetizationOptions = [
        { name: 'Subscriptions', description: 'Monthly subscriber fees', icon: User, active: true },
        { name: 'Tips', description: 'One-time payments from fans', icon: Gift, active: true },
        { name: 'Ad Revenue', description: 'Earn from video ads', icon: DollarSign, active: false },
        { name: 'Featured Content', description: 'Promoted posts', icon: Star, active: false },
    ];

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Creator Studio</h1>

            {/* Stats */}
            <div className="grid grid-cols-2 gap-4 mb-6">
                <div className="bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl p-4 text-white">
                    <p className="text-green-100 text-sm">Total Earnings</p>
                    <p className="text-3xl font-bold mt-1">${earnings}</p>
                    <p className="text-green-200 text-sm mt-1">+12% this month</p>
                </div>
                <div className="bg-gradient-to-r from-purple-500 to-pink-600 rounded-xl p-4 text-white">
                    <p className="text-purple-100 text-sm">Subscribers</p>
                    <p className="text-3xl font-bold mt-1">{subscribers.toLocaleString()}</p>
                    <p className="text-purple-200 text-sm mt-1">+50 this week</p>
                </div>
            </div>

            {/* Monetization */}
            <h2 className="text-lg font-semibold text-gray-800 mb-4">Monetization</h2>
            <div className="space-y-3">
                {monetizationOptions.map((option, index) => (
                    <div key={index} className="bg-white rounded-xl p-4 shadow-sm flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className={`p-3 rounded-lg ${option.active ? 'bg-green-100' : 'bg-gray-100'}`}>
                                <option.icon size={20} className={option.active ? 'text-green-600' : 'text-gray-400'} />
                            </div>
                            <div>
                                <p className="font-medium text-gray-800">{option.name}</p>
                                <p className="text-sm text-gray-500">{option.description}</p>
                            </div>
                        </div>
                        <span className={`px-3 py-1 rounded-full text-sm ${option.active ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-500'}`}>
                            {option.active ? 'Active' : 'Coming Soon'}
                        </span>
                    </div>
                ))}
            </div>

            {/* Analytics */}
            <div className="bg-white rounded-xl p-4 shadow-sm mt-6">
                <h3 className="font-semibold text-gray-800 mb-4">Performance Analytics</h3>
                <div className="space-y-3">
                    <div className="flex items-center justify-between">
                        <span className="text-gray-600">Total Views</span>
                        <span className="font-semibold">125,432</span>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-gray-600">Watch Time</span>
                        <span className="font-semibold">45.2K hours</span>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-gray-600">Engagement Rate</span>
                        <span className="font-semibold">8.5%</span>
                    </div>
                </div>
            </div>
        </div>
    );
}

// Credits/Coins Page
function CreditsPage() {
    const [credits, setCredits] = useState(500);
    const [history, setHistory] = useState([
        { id: '1', amount: -50, type: 'Video Export', date: '2026-04-15' },
        { id: '2', amount: 100, type: 'Game Reward', date: '2026-04-14' },
        { id: '3', amount: -25, type: 'AI Filter', date: '2026-04-13' },
    ]);

    const creditPacks = [
        { coins: 100, price: 0.99, bonus: 0 },
        { coins: 500, price: 4.99, bonus: 50 },
        { coins: 1000, price: 9.99, bonus: 150 },
        { coins: 5000, price: 49.99, bonus: 1000 },
    ];

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Nexora Credits</h1>

            {/* Balance Card */}
            <div className="bg-gradient-to-r from-yellow-400 to-amber-500 rounded-2xl p-6 text-white mb-6">
                <p className="text-yellow-100">Available Credits</p>
                <p className="text-5xl font-bold mt-2">{credits}</p>
                <p className="text-yellow-100 mt-2">= ${(credits * 0.01).toFixed(2)} USD</p>
            </div>

            {/* Purchase Packs */}
            <h2 className="text-lg font-semibold text-gray-800 mb-4">Buy Credits</h2>
            <div className="grid grid-cols-2 gap-4 mb-6">
                {creditPacks.map((pack, index) => (
                    <div key={index} className="bg-white rounded-xl p-4 shadow-sm border-2 border-yellow-200">
                        <div className="flex items-center gap-2 mb-2">
                            <Gem className="text-yellow-500" size={20} />
                            <span className="font-bold text-gray-800">{pack.coins + pack.bonus} credits</span>
                        </div>
                        {pack.bonus > 0 && (
                            <p className="text-xs text-green-600 mb-2">+{pack.bonus} bonus!</p>
                        )}
                        <button className="w-full py-2 bg-yellow-500 text-white rounded-lg font-medium">
                            ${pack.price}
                        </button>
                    </div>
                ))}
            </div>

            {/* Transaction History */}
            <div className="bg-white rounded-xl p-4 shadow-sm">
                <h3 className="font-semibold text-gray-800 mb-4">Recent Transactions</h3>
                <div className="space-y-3">
                    {history.map((tx) => (
                        <div key={tx.id} className="flex items-center justify-between py-2 border-b last:border-0">
                            <div>
                                <p className="font-medium text-gray-800">{tx.type}</p>
                                <p className="text-sm text-gray-500">{tx.date}</p>
                            </div>
                            <p className={`font-semibold ${tx.amount > 0 ? 'text-green-500' : 'text-red-500'}`}>
                                {tx.amount > 0 ? '+' : ''}{tx.amount} credits
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

// Premium/Subscription Page
function PremiumPage() {
    const plans = [
        {
            name: 'Basic',
            price: 4.99,
            features: ['Ad-free experience', 'Basic editing tools', '720p export', '50 credits/month'],
            color: 'bg-blue-500',
        },
        {
            name: 'Pro',
            price: 9.99,
            popular: true,
            features: ['Everything in Basic', '1080p export', 'No watermark', '200 credits/month', 'AI filters'],
            color: 'bg-purple-500',
        },
        {
            name: 'Elite',
            price: 19.99,
            features: ['Everything in Pro', '4K export', 'Priority support', '500 credits/month', 'Exclusive templates'],
            color: 'bg-yellow-500',
        },
    ];

    return (
        <div className="p-6">
            <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-r from-yellow-400 to-amber-500 rounded-2xl mb-4">
                    <Crown size={32} className="text-white" />
                </div>
                <h1 className="text-3xl font-bold text-gray-800">Nexora Plus</h1>
                <p className="text-gray-500 mt-2">Unlock the full potential of Nexora</p>
            </div>

            <div className="space-y-4">
                {plans.map((plan, index) => (
                    <div key={index} className={`bg-white rounded-2xl p-6 shadow-sm ${plan.popular ? 'ring-2 ring-purple-500' : ''}`}>
                        {plan.popular && (
                            <span className="bg-purple-100 text-purple-600 text-xs px-2 py-1 rounded-full">Most Popular</span>
                        )}
                        <div className="flex items-center justify-between mt-3">
                            <div>
                                <h3 className="text-xl font-bold text-gray-800">{plan.name}</h3>
                                <p className="text-3xl font-bold text-gray-800 mt-1">${plan.price}<span className="text-sm text-gray-500">/mo</span></p>
                            </div>
                            <button className={`px-6 py-3 ${plan.color} text-white rounded-xl font-medium`}>
                                Subscribe
                            </button>
                        </div>
                        <ul className="mt-4 space-y-2">
                            {plan.features.map((feature, i) => (
                                <li key={i} className="flex items-center gap-2 text-gray-600">
                                    <svg className="w-4 h-4 text-green-500" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" /></svg>
                                    {feature}
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>

            <p className="text-center text-gray-500 text-sm mt-6">
                Cancel anytime. Prices in USD.
            </p>
        </div>
    );
}

// Helper components
function Type({ size }: { size: number }) {
    return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polyline points="4 7 4 4 20 4 20 7" /><line x1="9" y1="20" x2="15" y2="20" /><line x1="12" y1="4" x2="12" y2="20" /></svg>;
}

function Music({ size }: { size: number }) {
    return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 18V5l12-2v13" /><circle cx="6" cy="18" r="3" /><circle cx="18" cy="16" r="3" /></svg>;
}

function Wand2({ size }: { size: number }) {
    return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 4V2" /><path d="M15 16v-2" /><path d="M8 9h2" /><path d="M20 9h2" /><path d="M17.8 11.8L19 13" /><path d="M15 9h0" /><path d="M17.8 6.2L19 5" /><path d="M3 21l9-9" /><path d="M12.2 6.2L11 5" /></svg>;
}

function Download({ size }: { size: number }) {
    return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" y1="15" x2="12" y2="3" /></svg>;
}

function Palette({ size }: { size: number }) {
    return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="13.5" cy="6.5" r="0.5" fill="currentColor" /><circle cx="17.5" cy="10.5" r="0.5" fill="currentColor" /><circle cx="8.5" cy="7.5" r="0.5" fill="currentColor" /><circle cx="6.5" cy="12.5" r="0.5" fill="currentColor" /><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.555C21.965 6.013 17.95 2 12 2z" /></svg>;
}

// Main App
function App() {
    const [user, setUser] = useState<User | null>(null);
    const [activeTab, setActiveTab] = useState('home');

    const handleLogin = (user: User) => {
        setUser(user);
        localStorage.setItem('nexora_user', JSON.stringify(user));
    };

    const handleLogout = () => {
        setUser(null);
        localStorage.removeItem('nexora_user');
    };

    useEffect(() => {
        const savedUser = localStorage.getItem('nexora_user');
        if (savedUser) {
            setUser(JSON.parse(savedUser));
        }
    }, []);

    const renderPage = () => {
        switch (activeTab) {
            case 'home':
                return <HomePage />;
            case 'social':
                return <SocialPage />;
            case 'marketplace':
                return <MarketplacePage />;
            case 'food':
                return <FoodDeliveryPage />;
            case 'ride':
                return <RidePage />;
            case 'video':
                return <VideoPage />;
            case 'wallet':
                return <WalletPage />;
            case 'games':
                return <GamesPage />;
            case 'editor':
                return <EditorPage />;
            case 'creator':
                return <CreatorPage />;
            case 'credits':
                return <CreditsPage />;
            case 'premium':
                return <PremiumPage />;
            case 'admin':
                return <AdminPage />;
            default:
                return <HomePage />;
        }
    };

    return (
        <BrowserRouter>
            <div className="min-h-screen bg-gray-50">
                {!user ? (
                    <LoginPage onLogin={handleLogin} />
                ) : (
                    <>
                        <Navbar
                            user={user}
                            onLogout={handleLogout}
                            activeTab={activeTab}
                            setActiveTab={setActiveTab}
                        />
                        <main>{renderPage()}</main>
                    </>
                )}
            </div>
        </BrowserRouter>
    );
}

export default App;
S