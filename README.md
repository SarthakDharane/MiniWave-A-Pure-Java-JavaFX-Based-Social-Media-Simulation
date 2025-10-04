# MiniWave-A-Pure-Java-JavaFX-Based-Social-Media-Simulation
MiniWave is a pure Java and JavaFX-based social media web application that simulates core functionalities of modern social platforms. It integrates Java backend logic with a web-based frontend (HTML, CSS, JavaScript) through JavaFX’s WebView and JSObject bridge, creating an interactive and responsive user experience.

---

## 📝 **Project Description**

**Project Title:** *MiniWave – A Pure Java & JavaFX-Based Social Media Simulation*

**Description:**
MiniWave is a pure **Java and JavaFX-based social media web application** that simulates core functionalities of modern social platforms. It integrates **Java backend logic** with a **web-based frontend** (HTML, CSS, JavaScript) through **JavaFX’s WebView and JSObject bridge**, creating an interactive and responsive user experience.

This project demonstrates key **software engineering and full-stack concepts** such as frontend-backend integration, user authentication, session handling, content management, and basic social networking interactions — all implemented entirely using **Java and JavaFX** without external frameworks.

---

## 📘 **README.md**

````markdown
# 🌊 MiniWave – Pure Java & JavaFX-Based Social Media Application

## 📖 Overview
**MiniWave** is a **pure Java and JavaFX-based social media simulation** that demonstrates how a Java desktop application can integrate with modern web technologies (HTML, CSS, JavaScript) through the **JavaFX WebView** component.  
It offers a simplified version of a social media platform with user authentication, post creation, following system, and a dynamic feed — all powered by Java logic behind an interactive web interface.

---

## 🏗️ System Architecture
MiniWave employs a **hybrid architecture** that bridges Java and web technologies:

| Layer | Technology | Description |
|-------|-------------|-------------|
| **Frontend** | HTML, CSS, JavaScript | Provides the user interface rendered inside JavaFX WebView |
| **Backend** | Java Classes | Manages user data, authentication, and social logic |
| **Integration Layer** | JSObject Bridge | Enables communication between Java and JavaScript for seamless UI updates |

---

## ⚙️ Key Components

### 1. `ModernSocialMediaApp.java`
- Entry point of the application (`extends Application`)
- Initializes the WebView UI
- Bridges Java backend methods to JavaScript frontend
- Manages post creation, retrieval, and deletion
- Handles user sessions and interface navigation

### 2. `User.java`
- Represents an individual user
- Stores username, password, and bio
- Tracks user posts and followed accounts

### 3. `UserManager.java`
- Static utility class for:
  - User registration and authentication
  - In-memory user data management using `HashMap`
  - Follow/unfollow and search functionality

---

## 💡 Features and Functionality

### 👤 User Authentication
- Register new users with username, password, and bio  
- Secure login/logout with session tracking (`currentUser`)  

### 📝 Content Management
- Create text-based posts with automatic timestamps  
- View and delete own posts  
- View posts from followed users in a combined feed  

### 🌐 Social Networking
- Search for users  
- Follow/unfollow functionality  
- Feed updates dynamically based on followed users  

### 🎨 User Interface
- Responsive web-style design  
- Tab-based navigation: **Feed | Profile | Explore**  
- Dynamic updates without page reloads  
- Built using HTML, CSS, and JavaScript rendered in JavaFX WebView  

---

## 🧩 Technical Implementation

### 🔗 Java–JavaScript Bridge
Integration between Java backend and frontend JavaScript is achieved via:
```java
JSObject window = (JSObject) webEngine.executeScript("window");
window.setMember("javaBackend", this);
````

This allows JavaScript to call Java methods directly:

```js
const success = javaBackend.loginUser(username, password);
```

### 🧠 Data Management

* In-memory data structures for simplicity:

  * `HashMap<String, User>` – user storage
  * `HashMap<Integer, String>` – post storage
* Incrementing counter for post IDs

---

## ⚠️ Limitations

* No persistent data storage (in-memory only)
* Passwords stored in plain text (no encryption)
* Local, single-user instance (no networking)
* Limited features (no images or multimedia)

---

## 🚀 Potential Improvements

* Integrate **JDBC/JPA** for database persistence
* Implement **password hashing** and validation
* Add **image uploads** and formatted text support
* Introduce **real-time notifications**
* Expand **profile customization**
* Develop a **client-server model** for multi-user access

---

## 🧰 Technologies Used

* **Language:** Java (JDK 8+)
* **Framework:** JavaFX
* **Frontend:** HTML, CSS, JavaScript
* **Libraries:** `netscape.javascript.JSObject`
* **IDE Recommended:** IntelliJ IDEA / Eclipse / VS Code with JavaFX support

---

## ▶️ Running the Application

1. Clone or download the repository.
2. Ensure **JavaFX SDK** is configured in your IDE or project path.
3. Compile and run `ModernSocialMediaApp.java`.
4. The app launches a JavaFX window rendering the HTML interface.
5. Start interacting by registering a new user and exploring MiniWave!

---

## 🏁 Conclusion

**MiniWave** demonstrates the power of **pure Java development** using **JavaFX** for creating interactive web-like applications.
It provides a solid foundation for learning full-stack concepts within Java and serves as an educational showcase of integrating **Java backend logic with modern web frontends**.

---

## 👨‍💻 Author

**Developer:** Sarthak Dharane ,Sahil Ghongate, Mitali Diwate, Apurva Deokar
**Technology Stack:** Pure Java & JavaFX
**Purpose:** Educational / Demonstration Project


