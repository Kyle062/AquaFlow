package database;

// This class is used to store the session information of the currently logged-in user.
public class UserSession {
    public static int userId = 0;
    public static String username = "";
    public static String role = "";

    /**
     * Clears the session variables when the user logs out.
     */
    public static void clearSession() {
        userId = 0;
        username = "";
        role = "";
    }
}