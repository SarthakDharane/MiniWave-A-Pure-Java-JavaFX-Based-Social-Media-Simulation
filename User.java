public class User {
    public String username;
    public String password;
    public String bio;
    public java.util.Set<Integer> userPosts;
    public java.util.Set<String> following;
    
    public User(String username, String password, String bio) {
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.userPosts = new java.util.HashSet<>();
        this.following = new java.util.HashSet<>();
    }
}