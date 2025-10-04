import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Map;

public class DummyDataGenerator {
    
    // Sample data arrays
    private static final String[] USERNAMES = {
        "alice_wonder", "bob_builder", "charlie_tech", "diana_art", 
        "evan_travel", "fiona_books", "greg_gamer", "hannah_chef",
        "ian_music", "julia_fitness", "kevin_photo", "lisa_science"
    };
    
    private static final String[] BIOS = {
        "Adventure seeker and coffee enthusiast",
        "Software developer by day, gamer by night",
        "Photography lover exploring the world one click at a time",
        "Book lover and aspiring writer",
        "Fitness trainer sharing workout tips",
        "Tech enthusiast and gadget reviewer",
        "Food blogger sharing recipes and restaurant reviews",
        "Travel blogger documenting adventures",
        "Music producer and guitar player",
        "Science teacher with a passion for astronomy",
        "Digital artist creating fantasy worlds",
        "Nature lover and environmental activist"
    };
    
    private static final String[] POST_CONTENTS = {
        "Just finished reading an amazing book! Highly recommend it to everyone.",
        "Working on a new project today. Can't wait to share the results!",
        "The weather is perfect for a hike this weekend. Anyone interested?",
        "Finally mastered that recipe I've been trying for weeks!",
        "Just released my new song on SoundCloud. Link in bio!",
        "Looking for recommendations on good documentaries to watch.",
        "Today's workout was intense but totally worth it.",
        "Visited the new art exhibition downtown. Mind-blowing experience!",
        "Coding all night to fix this bug. Need more coffee...",
        "Captured the most beautiful sunset today. Nature is amazing!",
        "Just adopted a puppy! Meet Max, my new coding buddy.",
        "Started learning a new language today. ¡Hola a todos!",
        "Anyone else excited about the upcoming tech conference?",
        "My garden is finally blooming after months of care.",
        "Tried that new restaurant downtown. The food was incredible!",
        "Reflecting on how much I've grown over the past year.",
        "Working from home today with my cat as my assistant.",
        "Just finished my first marathon! Exhausted but proud.",
        "Experimenting with new photography techniques today.",
        "Unpopular opinion: pineapple belongs on pizza!"
    };
    
    /**
     * Populates the application with dummy users and posts
     * @param allPosts The map to store posts in
     * @param nextPostId The current next post ID
     * @return The updated next post ID
     */
    public static int generateDummyData(Map<Integer, String> allPosts, int nextPostId) {
        System.out.println("Generating dummy data...");
        Random random = new Random();
        
        // Create users
        for (int i = 0; i < USERNAMES.length; i++) {
            String username = USERNAMES[i];
            String password = "password123"; // Simple password for all dummy users
            String bio = BIOS[i % BIOS.length];
            
            boolean success = UserManager.registerUser(username, password, bio);
            if (success) {
                System.out.println("Created user: " + username);
            }
        }
        
        // Create posts for each user
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        
        for (String username : USERNAMES) {
            // Login as this user
            UserManager.loginUser(username, "password123");
            User user = UserManager.getCurrentUser();
            
            if (user != null) {
                // Create 3-7 posts for each user
                int postsCount = 3 + random.nextInt(5);
                
                for (int i = 0; i < postsCount; i++) {
                    String content = POST_CONTENTS[random.nextInt(POST_CONTENTS.length)];
                    String timestamp = timeFormat.format(new Date());
                    String postContent = username + ": " + content + " (" + timestamp + ")";
                    
                    // Directly add to the allPosts map
                    allPosts.put(nextPostId, postContent);
                    user.userPosts.add(nextPostId);
                    nextPostId++;
                }
                
                System.out.println("Created " + postsCount + " posts for " + username);
            }
        }
        
        // Create following relationships
        for (String username : USERNAMES) {
            // Login as this user
            UserManager.loginUser(username, "password123");
            
            // Follow 3-6 random users
            int followCount = 3 + random.nextInt(4);
            int followedCount = 0;
            
            while (followedCount < followCount) {
                String userToFollow = USERNAMES[random.nextInt(USERNAMES.length)];
                
                // Don't follow yourself
                if (!userToFollow.equals(username)) {
                    boolean success = UserManager.followUser(userToFollow);
                    if (success) {
                        followedCount++;
                    }
                }
            }
            
            System.out.println(username + " is following " + followedCount + " users");
        }
        
        // Logout after data generation
        UserManager.logout();
        System.out.println("Dummy data generation completed!");
        
        return nextPostId;
    }
}