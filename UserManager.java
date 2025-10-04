import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {
    private static Map<String, User> users = new HashMap<>();
    private static User currentUser = null;
    
    public static boolean registerUser(String username, String password, String bio) {
        if (users.containsKey(username)) {
            return false;
        }
        
        User newUser = new User(username, password, bio);
        users.put(username, newUser);
        return true;
    }
    
    public static boolean loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }
    
    public static void logout() {
        currentUser = null;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static User getUserByUsername(String username) {
        return users.get(username);
    }
    
    public static List<User> searchUsers(String query) {
        List<User> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (User user : users.values()) {
            if (currentUser != null && user.username.equals(currentUser.username)) {
                continue; // Skip current user
            }
            
            if (user.username.toLowerCase().contains(lowerQuery) || 
                user.bio.toLowerCase().contains(lowerQuery)) {
                results.add(user);
            }
        }
        
        return results;
    }
    
    public static boolean followUser(String username) {
        if (currentUser == null) {
            return false;
        }
        
        User userToFollow = users.get(username);
        if (userToFollow != null && !currentUser.username.equals(username)) {
            currentUser.following.add(username);
            return true;
        }
        
        return false;
    }
    
    public static boolean unfollowUser(String username) {
        if (currentUser == null) {
            return false;
        }
        
        if (currentUser.following.contains(username)) {
            currentUser.following.remove(username);
            return true;
        }
        
        return false;
    }
}