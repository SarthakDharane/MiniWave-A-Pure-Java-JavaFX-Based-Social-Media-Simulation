import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.util.*;
import java.text.SimpleDateFormat;


public class ModernSocialMediaApp extends Application {
    
    private WebView webView;
    private WebEngine webEngine;
    private Map<Integer, String> allPosts;
    private int nextPostId;
    
    @Override
public void start(Stage primaryStage) {
    // Initialize your data
    allPosts = new HashMap<>();
    nextPostId = 1;
    
    // Generate dummy data with direct access to your maps
    nextPostId = DummyDataGenerator.generateDummyData(allPosts, nextPostId);
    
    // Create the WebView
    webView = new WebView();
    webEngine = webView.getEngine();
    
    // Load the HTML UI
    webEngine.loadContent(getHtmlContent());
    
    // Connect Java backend to JavaScript frontend
    webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
        if (newState == State.SUCCEEDED) {
            // Get the JavaScript window object
            JSObject window = (JSObject) webEngine.executeScript("window");
            
            // Make the Java app instance available to JavaScript
            window.setMember("javaBackend", this);
            
            // Initialize any UI elements that need data from Java
            webEngine.executeScript("initializeApp()");
        }
    });
    
    // Set up the scene
    StackPane root = new StackPane();
    root.getChildren().add(webView);
    Scene scene = new Scene(root, 900, 700);
    primaryStage.setTitle("MiniWave - Modern Social Media");
    primaryStage.setScene(scene);
    primaryStage.show();

    }
    
    // Methods that will be called from JavaScript
    
    public boolean registerUser(String username, String password, String bio) {
        return UserManager.registerUser(username, password, bio);
    }
    
    public boolean loginUser(String username, String password) {
        return UserManager.loginUser(username, password);
    }
    
    public void logout() {
        UserManager.logout();
    }
    
    public String getCurrentUsername() {
        User user = UserManager.getCurrentUser();
        return user != null ? user.username : "";
    }
    
    public String getCurrentUserBio() {
        User user = UserManager.getCurrentUser();
        return user != null ? user.bio : "";
    }
    
    public void updateUserBio(String newBio) {
        User user = UserManager.getCurrentUser();
        if (user != null) {
            user.bio = newBio;
        }
    }
    
    public String createPost(String content) {
        User currentUser = UserManager.getCurrentUser();
        if (currentUser == null || content.trim().isEmpty()) {
            return "error";
        }
        
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String postContent = currentUser.username + ": " + content + " (" + timestamp + ")";
        allPosts.put(nextPostId, postContent);
        currentUser.userPosts.add(nextPostId);
        
        int postId = nextPostId;
        nextPostId++;
        
        // Return JSON representation of the new post
        return "{\"id\":" + postId + ",\"content\":\"" + escapeJsonString(content) + 
               "\",\"username\":\"" + currentUser.username + 
               "\",\"timestamp\":\"" + timestamp + "\"}";
    }
    
    public String getFeedPosts() {
        User currentUser = UserManager.getCurrentUser();
        if (currentUser == null) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        
        // User's own posts
        for (int postId : currentUser.userPosts) {
            if (allPosts.containsKey(postId)) {
                String post = allPosts.get(postId);
                String username = post.substring(0, post.indexOf(":"));
                String content = post.substring(post.indexOf(":") + 2, post.lastIndexOf("(") - 1);
                String timestamp = post.substring(post.lastIndexOf("(") + 1, post.lastIndexOf(")"));
                
                json.append("{\"id\":").append(postId)
                    .append(",\"username\":\"").append(username)
                    .append("\",\"content\":\"").append(escapeJsonString(content))
                    .append("\",\"timestamp\":\"").append(timestamp)
                    .append("\",\"isOwn\":true},");
            }
        }
        
        // Posts from followed users
        for (String username : currentUser.following) {
            User followedUser = UserManager.getUserByUsername(username);
            if (followedUser != null) {
                for (int postId : followedUser.userPosts) {
                    if (allPosts.containsKey(postId)) {
                        String post = allPosts.get(postId);
                        String postUsername = post.substring(0, post.indexOf(":"));
                        String content = post.substring(post.indexOf(":") + 2, post.lastIndexOf("(") - 1);
                        String timestamp = post.substring(post.lastIndexOf("(") + 1, post.lastIndexOf(")"));
                        
                        json.append("{\"id\":").append(postId)
                            .append(",\"username\":\"").append(postUsername)
                            .append("\",\"content\":\"").append(escapeJsonString(content))
                            .append("\",\"timestamp\":\"").append(timestamp)
                            .append("\",\"isOwn\":false},");
                    }
                }
            }
        }
        
        if (json.charAt(json.length() - 1) == ',') {
            json.setLength(json.length() - 1);
        }
        json.append("]");
        
        return json.toString();
    }
    
    public boolean deletePost(int postId) {
        User currentUser = UserManager.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        if (currentUser.userPosts.contains(postId)) {
            currentUser.userPosts.remove(Integer.valueOf(postId)); // Use valueOf to avoid removing by index
            allPosts.remove(postId);
            return true;
        }
        return false;
    }
    
    public String searchUsers(String query) {
        List<User> results = UserManager.searchUsers(query);
        StringBuilder json = new StringBuilder("[");
        
        User currentUser = UserManager.getCurrentUser();
        for (User user : results) {
            boolean isFollowing = (currentUser != null && 
                                  currentUser.following.contains(user.username));
            
            json.append("{\"username\":\"").append(user.username)
                .append("\",\"bio\":\"").append(escapeJsonString(user.bio))
                .append("\",\"isFollowing\":").append(isFollowing)
                .append("},");
        }
        
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("]");
        
        return json.toString();
    }
    
    public boolean followUser(String username) {
        return UserManager.followUser(username);
    }
    
    public boolean unfollowUser(String username) {
        return UserManager.unfollowUser(username);
    }
    
    public String getFollowingList() {
        User currentUser = UserManager.getCurrentUser();
        if (currentUser == null) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (String username : currentUser.following) {
            json.append("\"").append(username).append("\",");
        }
        
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("]");
        
        return json.toString();
    }
    
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String getHtmlContent() {
        // HTML, CSS, and JavaScript for the modern UI
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MiniWave</title>
    <style>
        /* Vibrant Color Palette */
        :root {
            --primary:rgb(202, 17, 60);
            --primary-dark:rgb(200, 2, 48);
            --secondary: #FF7675;
            --accent: #FDCB6E;
            --light: #F5F6FA;
            --dark: #2D3436;
            --success: #00B894;
            --warning:rgb(233, 109, 109);
            --info: #0984E3;
        }
        
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Poppins', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            color: var(--dark);
            min-height: 100vh;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .nav-bar {
            background-color: white;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            padding: 15px 0;
            margin-bottom: 20px;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        
        .nav-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
        
        .logo {
            font-size: 28px;
            font-weight: 800;
            color: var(--primary);
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .nav-menu {
            display: flex;
            list-style: none;
        }
        
        .nav-item {
            margin-left: 20px;
            cursor: pointer;
            padding: 8px 16px;
            border-radius: 30px;
            transition: all 0.3s ease;
            font-weight: 600;
        }
        
        .nav-item:hover {
            background-color: var(--primary);
            color: white;
            transform: translateY(-2px);
        }
        
        .nav-item.active {
            background-color: var(--primary);
            color: white;
            box-shadow: 0 4px 8px rgba(108, 92, 231, 0.3);
        }
        
        .card {
            background-color: white;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            padding: 30px;
            margin-bottom: 30px;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.15);
        }
        
        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 30px;
            cursor: pointer;
            font-size: 16px;
            font-weight: 600;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .btn-primary {
            background-color: var(--primary);
            color: white;
            box-shadow: 0 4px 8px rgba(108, 92, 231, 0.3);
        }
        
        .btn-primary:hover {
            background-color: var(--primary-dark);
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(108, 92, 231, 0.4);
        }
        
        .btn-danger {
            background-color: var(--secondary);
            color: white;
            box-shadow: 0 4px 8px rgba(255, 118, 117, 0.3);
        }
        
        .btn-danger:hover {
            background-color: #FF5E5C;
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(255, 118, 117, 0.4);
        }
        
        .btn-success {
            background-color: var(--success);
            color: white;
            box-shadow: 0 4px 8px rgba(0, 184, 148, 0.3);
        }
        
        .btn-success:hover {
            background-color: #00A885;
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(0, 184, 148, 0.4);
        }
        
        input, textarea {
            width: 100%;
            padding: 15px;
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            margin-bottom: 20px;
            font-size: 16px;
            transition: all 0.3s ease;
        }
        
        input:focus, textarea:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(108, 92, 231, 0.2);
            outline: none;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: var(--dark);
        }
        
        .post {
            border-left: 4px solid var(--primary);
            padding-left: 20px;
            margin-bottom: 25px;
            transition: all 0.3s ease;
        }
        
        .post:hover {
            transform: translateX(5px);
        }
        
        .post-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 15px;
            align-items: center;
        }
        
        .post-author {
            font-weight: 700;
            color: var(--primary);
            font-size: 18px;
        }
        
        .post-time {
            color: var(--dark);
            font-size: 14px;
            opacity: 0.7;
        }
        
        .post-content {
            line-height: 1.6;
            margin-bottom: 15px;
            font-size: 16px;
        }
        
        .post-actions {
            display: flex;
            gap: 10px;
        }
        
        .user-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            border-bottom: 1px solid #eee;
            transition: all 0.3s ease;
        }
        
        .user-card:hover {
            background-color: var(--light);
            border-radius: 10px;
        }
        
        .user-info {
            flex: 1;
        }
        
        .user-name {
            font-weight: 700;
            color: var(--primary);
            font-size: 18px;
        }
        
        .user-bio {
            color: var(--dark);
            font-size: 14px;
            opacity: 0.8;
        }
        
        .hidden {
            display: none;
        }
        
        .section {
            padding: 20px;
        }
        
        .tab-content {
            display: none;
            animation: fadeIn 0.5s ease;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .tab-content.active {
            display: block;
        }
        
        .alert {
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            font-weight: 600;
            animation: slideIn 0.5s ease;
        }
        
        @keyframes slideIn {
            from { transform: translateX(-20px); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        
        .alert-success {
            background-color: rgba(0, 184, 148, 0.1);
            color: var(--success);
            border: 2px solid var(--success);
        }
        
        .alert-danger {
            background-color: rgba(255, 118, 117, 0.1);
            color: var(--secondary);
            border: 2px solid var(--secondary);
        }
        
        h2, h3 {
            color: var(--primary);
            margin-bottom: 20px;
            font-weight: 800;
        }
        
        /* New Auth Card Styles */
        #auth-section .card {
            text-align: center;
            max-width: 450px;
            margin: 50px auto;
            padding: 40px;
        }
        
        .auth-title {
            font-size: 24px;
            margin-bottom: 10px;
            color: var(--primary);
            font-weight: 800;
            text-transform: uppercase;
        }
        
        .auth-subtitle {
            color: var(--dark);
            opacity: 0.8;
            margin-bottom: 30px;
            font-size: 16px;
        }
        
        .auth-buttons {
            display: flex;
            flex-direction: column;
            gap: 15px;
            margin-top: 20px;
        }
        
        .auth-divider {
            display: flex;
            align-items: center;
            margin: 20px 0;
            color: #999;
            font-size: 14px;
        }
        
        .auth-divider::before,
        .auth-divider::after {
            content: "";
            flex: 1;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .auth-divider::before {
            margin-right: 10px;
        }
        
        .auth-divider::after {
            margin-left: 10px;
        }
        
        .auth-switch {
            margin-top: 20px;
            font-size: 14px;
            color: var(--dark);
        }
        
        .auth-switch a {
            color: var(--primary);
            text-decoration: none;
            font-weight: 600;
            cursor: pointer;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .nav-menu {
                display: none;
            }
            
            .card {
                padding: 20px;
            }
            
            .btn {
                padding: 10px 20px;
            }
            
            #auth-section .card {
                margin: 20px auto;
                padding: 25px;
            }
        }
    </style>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
    <div class="nav-bar">
        <div class="nav-container">
            <div class="logo">MiniWave</div>
            <ul class="nav-menu" id="nav-menu">
                <!-- Navigation items will be added by JavaScript -->
            </ul>
        </div>
    </div>
    
    <div class="container">
        <!-- Login/Register Section -->
        <div id="auth-section" class="tab-content active">
            <div class="card">
                <h2 class="auth-title">Sign In</h2>
                <p class="auth-subtitle">Hello, Friend!</p>
                <p class="auth-subtitle">Enter your personal details and start your journey with us</p>
                
                <button id="register-toggle" class="btn btn-primary">SIGN UP</button>
                
                <div class="auth-divider">or use your account</div>
                
                <div class="form-group">
                    <input type="email" id="email" placeholder="Email">
                </div>
                
                <div class="form-group">
                    <input type="password" id="password" placeholder="Password">
                </div>
                
                <div class="form-group" style="text-align: right;">
                    <a href="#" style="color: var(--primary); text-decoration: none; font-size: 14px;">Forgot your password?</a>
                </div>
                
                <div id="auth-alert" class="alert" style="display: none;"></div>
                
                <button id="login-btn" class="btn btn-primary" style="width: 100%;">SIGN IN</button>
                
                <div class="auth-switch" id="login-switch">
                    Don't have an account? <a id="switch-to-register">Register</a>
                </div>
                
                <!-- Registration Form (Hidden by default) -->
                <div id="register-form" style="display: none;">
                    <h2 class="auth-title">Create Account</h2>
                    <p class="auth-subtitle">Hello, Friend!</p>
                    <p class="auth-subtitle">Enter your personal details and start your journey with us</p>
                    
                    <div class="form-group">
                        <input type="text" id="reg-username" placeholder="Username">
                    </div>
                    
                    <div class="form-group">
                        <input type="email" id="reg-email" placeholder="Email">
                    </div>
                    
                    <div class="form-group">
                        <input type="password" id="reg-password" placeholder="Password">
                    </div>
                    
                    <div class="form-group">
                        <input type="password" id="reg-confirm-password" placeholder="Confirm Password">
                    </div>
                    
                    <div id="reg-auth-alert" class="alert" style="display: none;"></div>
                    
                    <button id="register-btn" class="btn btn-primary" style="width: 100%;">SIGN UP</button>
                    
                    <div class="auth-switch">
                        Already have an account? <a id="switch-to-login">Sign In</a>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Feed Section -->
        <div id="feed-section" class="tab-content">
            <div class="card">
                <h3>Create Post</h3>
                <div class="form-group">
                    <textarea id="post-content" placeholder="What's on your mind?"></textarea>
                </div>
                <button id="post-btn" class="btn btn-primary">Post</button>
            </div>
            
            <div class="card">
                <h3>Your Feed</h3>
                <div id="posts-container">
                    <!-- Posts will be added here by JavaScript -->
                </div>
            </div>
        </div>
        
        <!-- Profile Section -->
        <div id="profile-section" class="tab-content">
            <div class="card">
                <h3>Your Profile</h3>
                <div class="form-group">
                    <label for="profile-username">Username</label>
                    <input type="text" id="profile-username" disabled>
                </div>
                
                <div class="form-group">
                    <label for="profile-bio">Bio</label>
                    <textarea id="profile-bio" placeholder="Tell us about yourself"></textarea>
                </div>
                
                <button id="update-bio-btn" class="btn btn-primary">Update Bio</button>
            </div>
            
            <div class="card">
                <h3>People You Follow</h3>
                <div id="following-container">
                    <!-- Following list will be added here by JavaScript -->
                </div>
            </div>
        </div>
        
        <!-- Explore Section -->
        <div id="explore-section" class="tab-content">
            <div class="card">
                <h3>Find Users</h3>
                <div class="form-group">
                    <input type="text" id="search-input" placeholder="Search for users...">
                </div>
                <button id="search-btn" class="btn btn-primary">Search</button>
            </div>
            
            <div class="card">
                <h3>Search Results</h3>
                <div id="search-results">
                    <!-- Search results will be added here by JavaScript -->
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // State management
        let isRegistering = false;
        let currentTab = 'auth-section';
        
        // DOM Elements
        const navMenu = document.getElementById('nav-menu');
        const authSection = document.getElementById('auth-section');
        const feedSection = document.getElementById('feed-section');
        const profileSection = document.getElementById('profile-section');
        const exploreSection = document.getElementById('explore-section');
        
        // Initialize the application
        function initializeApp() {
            setupEventListeners();
            updateNavigation();
        }
        
        // Set up event listeners
        function setupEventListeners() {
            // Auth events
            document.getElementById('login-btn').addEventListener('click', handleLogin);
            document.getElementById('register-toggle').addEventListener('click', showRegisterForm);
            document.getElementById('register-btn').addEventListener('click', handleRegister);
            document.getElementById('switch-to-register').addEventListener('click', showRegisterForm);
            document.getElementById('switch-to-login').addEventListener('click', showLoginForm);
            
            // Post events
            document.getElementById('post-btn').addEventListener('click', createPost);
            
            // Profile events
            document.getElementById('update-bio-btn').addEventListener('click', updateBio);
            
            // Search events
            document.getElementById('search-btn').addEventListener('click', searchUsers);
        }
        
        // Update navigation based on login state
        function updateNavigation() {
            const username = javaBackend.getCurrentUsername();
            const isLoggedIn = username !== "";
            
            // Clear existing nav items
            navMenu.innerHTML = '';
            
            if (isLoggedIn) {
                // Create nav items for logged in users
                createNavItem('Feed', 'feed-section', true);
                createNavItem('Profile', 'profile-section');
                createNavItem('Explore', 'explore-section');
                createNavItem('Logout', null);
                
                // Switch to feed view
                if (currentTab === 'auth-section') {
                    switchTab('feed-section');
                    refreshFeed();
                    loadProfile();
                }
            } else {
                // Create nav item for login/register
                createNavItem('Login/Register', 'auth-section', true);
                switchTab('auth-section');
            }
        }
        
        // Create navigation item
        function createNavItem(text, targetId, isActive = false) {
            const item = document.createElement('li');
            item.className = 'nav-item';
            item.textContent = text;
            if (isActive) item.classList.add('active');
            
            item.addEventListener('click', () => {
                if (text === 'Logout') {
                    javaBackend.logout();
                    updateNavigation();
                    showLoginForm();
                } else {
                    document.querySelectorAll('.nav-item').forEach(el => {
                        el.classList.remove('active');
                    });
                    item.classList.add('active');
                    switchTab(targetId);
                    
                    if (targetId === 'feed-section') {
                        refreshFeed();
                    } else if (targetId === 'profile-section') {
                        loadProfile();
                    }
                }
            });
            
            navMenu.appendChild(item);
        }
        
        // Switch between tabs
        function switchTab(tabId) {
            document.querySelectorAll('.tab-content').forEach(tab => {
                tab.classList.remove('active');
            });
            document.getElementById(tabId).classList.add('active');
            currentTab = tabId;
        }
        
        // Show register form
        function showRegisterForm() {
            document.getElementById('auth-section').querySelector('.card > *:not(#register-form)').style.display = 'none';
            document.getElementById('register-form').style.display = 'block';
            isRegistering = true;
        }
        
        // Show login form
        function showLoginForm() {
            document.getElementById('auth-section').querySelector('.card > *:not(#register-form)').style.display = 'block';
            document.getElementById('register-form').style.display = 'none';
            isRegistering = false;
        }
        
        // Handle login
        function handleLogin() {
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            if (!email || !password) {
                showAuthAlert('Please enter both email and password', 'danger');
                return;
            }
            
            // For demo purposes, we'll use email as username
            const success = javaBackend.loginUser(email, password);
            if (success) {
                document.getElementById('email').value = '';
                document.getElementById('password').value = '';
                updateNavigation();
            } else {
                showAuthAlert('Invalid email or password', 'danger');
            }
        }
        
        // Handle registration
        function handleRegister() {
            const username = document.getElementById('reg-username').value;
            const email = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const confirmPassword = document.getElementById('reg-confirm-password').value;
            
            if (!username || !email || !password || !confirmPassword) {
                showRegAuthAlert('Please fill in all fields', 'danger');
                return;
            }
            
            if (password !== confirmPassword) {
                showRegAuthAlert('Passwords do not match', 'danger');
                return;
            }
            
            const success = javaBackend.registerUser(username, password, "");
            if (success) {
                showRegAuthAlert('Registration successful! You can now log in.', 'success');
                showLoginForm();
                document.getElementById('reg-username').value = '';
                document.getElementById('reg-email').value = '';
                document.getElementById('reg-password').value = '';
                document.getElementById('reg-confirm-password').value = '';
            } else {
                showRegAuthAlert('Username already exists', 'danger');
            }
        }
        
        // Show authentication alerts
        function showAuthAlert(message, type) {
            const alert = document.getElementById('auth-alert');
            alert.textContent = message;
            alert.className = `alert alert-${type}`;
            alert.style.display = 'block';
            
            setTimeout(() => {
                alert.style.display = 'none';
            }, 3000);
        }
        
        function showRegAuthAlert(message, type) {
            const alert = document.getElementById('reg-auth-alert');
            alert.textContent = message;
            alert.className = `alert alert-${type}`;
            alert.style.display = 'block';
            
            setTimeout(() => {
                alert.style.display = 'none';
            }, 3000);
        }
        
        // Create a new post
        function createPost() {
            const content = document.getElementById('post-content').value;
            if (!content.trim()) return;
            
            const result = javaBackend.createPost(content);
            if (result !== 'error') {
                document.getElementById('post-content').value = '';
                refreshFeed();
            }
        }
        
        // Refresh the feed
        function refreshFeed() {
            const postsContainer = document.getElementById('posts-container');
            postsContainer.innerHTML = '';
            
            const feedJson = javaBackend.getFeedPosts();
            const posts = JSON.parse(feedJson);
            
            if (posts.length === 0) {
                postsContainer.innerHTML = '<p>No posts to show. Create a post or follow other users to see their posts.</p>';
                return;
            }
            
            // Sort posts by ID (newer posts first)
            posts.sort((a, b) => b.id - a.id);
            
            posts.forEach(post => {
                const postElement = document.createElement('div');
                postElement.className = 'post';
                
                const header = document.createElement('div');
                header.className = 'post-header';
                
                const author = document.createElement('div');
                author.className = 'post-author';
                author.textContent = '@' + post.username;
                
                const time = document.createElement('div');
                time.className = 'post-time';
                time.textContent = post.timestamp;
                
                header.appendChild(author);
                header.appendChild(time);
                
                const content = document.createElement('div');
                content.className = 'post-content';
                content.textContent = post.content;
                
                postElement.appendChild(header);
                postElement.appendChild(content);
                
                // Add delete button if it's user's own post
                if (post.isOwn) {
                    const actions = document.createElement('div');
                    actions.className = 'post-actions';
                    
                    const deleteBtn = document.createElement('button');
                    deleteBtn.className = 'btn btn-danger';
                    deleteBtn.textContent = 'Delete';
                    deleteBtn.addEventListener('click', () => {
                        const success = javaBackend.deletePost(post.id);
                        if (success) {
                            refreshFeed();
                        }
                    });
                    
                    actions.appendChild(deleteBtn);
                    postElement.appendChild(actions);
                }
                
                postsContainer.appendChild(postElement);
            });
        }
        
        // Load user profile
        function loadProfile() {
            const username = javaBackend.getCurrentUsername();
            const bio = javaBackend.getCurrentUserBio();
            
            document.getElementById('profile-username').value = username;
            document.getElementById('profile-bio').value = bio;
            
            loadFollowingList();
        }
        
        // Update user bio
        function updateBio() {
            const newBio = document.getElementById('profile-bio').value;
            javaBackend.updateUserBio(newBio);
            showAuthAlert('Bio updated successfully!', 'success');
        }
        
        // Load following list
        function loadFollowingList() {
            const followingContainer = document.getElementById('following-container');
            followingContainer.innerHTML = '';
            
            const followingJson = javaBackend.getFollowingList();
            const following = JSON.parse(followingJson);
            
            if (following.length === 0) {
                followingContainer.innerHTML = '<p>You are not following anyone yet.</p>';
                return;
            }
            
            following.forEach(username => {
                const userCard = document.createElement('div');
                userCard.className = 'user-card';
                
                const userInfo = document.createElement('div');
                userInfo.className = 'user-info';
                
                const userName = document.createElement('div');
                userName.className = 'user-name';
                userName.textContent = '@' + username;
                
                userInfo.appendChild(userName);
                
                const unfollowBtn = document.createElement('button');
                unfollowBtn.className = 'btn btn-danger';
                unfollowBtn.textContent = 'Unfollow';
                unfollowBtn.addEventListener('click', () => {
                    const success = javaBackend.unfollowUser(username);
                    if (success) {
                        loadFollowingList();
                    }
                });
                
                userCard.appendChild(userInfo);
                userCard.appendChild(unfollowBtn);
                
                followingContainer.appendChild(userCard);
            });
        }
        
        // Search for users
        function searchUsers() {
            const query = document.getElementById('search-input').value;
            if (!query.trim()) return;
            
            const resultsContainer = document.getElementById('search-results');
            resultsContainer.innerHTML = '';
            
            const resultsJson = javaBackend.searchUsers(query);
            const results = JSON.parse(resultsJson);
            
            if (results.length === 0) {
                resultsContainer.innerHTML = '<p>No users found matching your search.</p>';
                return;
            }
            
            results.forEach(user => {
                const userCard = document.createElement('div');
                userCard.className = 'user-card';
                
                const userInfo = document.createElement('div');
                userInfo.className = 'user-info';
                
                const userName = document.createElement('div');
                userName.className = 'user-name';
                userName.textContent = '@' + user.username;
                
                const userBio = document.createElement('div');
                userBio.className = 'user-bio';
                userBio.textContent = user.bio || 'No bio available';
                
                userInfo.appendChild(userName);
                userInfo.appendChild(userBio);
                
                const followBtn = document.createElement('button');
                
                if (user.isFollowing) {
                    followBtn.className = 'btn btn-danger';
                    followBtn.textContent = 'Unfollow';
                    followBtn.addEventListener('click', () => {
                        const success = javaBackend.unfollowUser(user.username);
                        if (success) {
                            followBtn.className = 'btn btn-primary';
                            followBtn.textContent = 'Follow';
                            user.isFollowing = false;
                        }
                    });
                } else {
                    followBtn.className = 'btn btn-primary';
                    followBtn.textContent = 'Follow';
                    followBtn.addEventListener('click', () => {
                        const success = javaBackend.followUser(user.username);
                        if (success) {
                            followBtn.className = 'btn btn-danger';
                            followBtn.textContent = 'Unfollow';
                            user.isFollowing = true;
                        }
                    });
                }
                
                userCard.appendChild(userInfo);
                userCard.appendChild(followBtn);
                
                resultsContainer.appendChild(userCard);
            });
        }
        
        // Initialize the app when DOM is loaded
        document.addEventListener('DOMContentLoaded', initializeApp);
    </script>
</body>
</html>
                """;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}