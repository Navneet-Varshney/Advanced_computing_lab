package com.faculty;

import com.google.gson.Gson;
import java.util.*;

public class UserManager {
    private static final String USERS_CSV = "data/users.csv";
    private static final String[] USER_HEADERS = {"username", "password", "role", "faculty_id", "name", "email"};
    private static Gson gson = new Gson();
    
    public static boolean addUser(Map<String, String> userData) {
        try {
            CSVReader reader = new CSVReader(USERS_CSV);
            List<Map<String, String>> existingUsers = reader.readCSV();
            
            existingUsers.add(userData);
            CSVWriter writer = new CSVWriter(USERS_CSV);
            boolean result = writer.writeCSV(existingUsers, Arrays.asList(USER_HEADERS));
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static Map<String, String> authenticate(String username, String password) {
        CSVReader reader = new CSVReader(USERS_CSV);
        Map<String, String> user = reader.findRow("username", username);
        
        if (user != null && user.getOrDefault("password", "").equals(password)) {
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get("username"));
            response.put("role", user.get("role"));
            return response;
        }
        
        return null; // Authentication failed
    }
    
    public static Map<String, String> getUserByUsername(String username) {
        CSVReader reader = new CSVReader(USERS_CSV);
        Map<String, String> user = reader.findRow("username", username);
        
        if (user != null) {
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get("username"));
            response.put("role", user.get("role"));
            return response;
        }
        
        return null;
    }
    
    public static List<Map<String, String>> getAllUsers() {
        CSVReader reader = new CSVReader(USERS_CSV);
        List<Map<String, String>> users = reader.readCSV();
        
        for (Map<String, String> user : users) {
            user.remove("password");
        }
        
        return users;
    }
    
    public static boolean userExists(String username) {
        CSVReader reader = new CSVReader(USERS_CSV);
        return reader.findRow("username", username) != null;
    }
    
    public static Map<String, String> recoverAdminPassword(String username, String email) {
        try {
            CSVReader reader = new CSVReader(USERS_CSV);
            Map<String, String> user = reader.findRow("username", username);
            
            if (user == null) {
                System.out.println("[DEBUG] Admin user not found: " + username);
                return null;
            }
            
            // Verify email matches (case-insensitive)
            String storedEmail = user.getOrDefault("email", "").toLowerCase().trim();
            String providedEmail = email.toLowerCase().trim();
            
            if (!storedEmail.equals(providedEmail)) {
                System.out.println("[DEBUG] Email mismatch for admin. Expected: " + storedEmail + ", Got: " + providedEmail);
                return null;
            }
            
            // Verify it's admin role
            String role = user.getOrDefault("role", "");
            if (!role.equals("admin")) {
                System.out.println("[DEBUG] User is not admin: " + username);
                return null;
            }
            
            // Return user with password (for pwd recovery only)
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get("username"));
            response.put("password", user.get("password"));
            response.put("email", user.get("email"));
            response.put("role", user.get("role"));
            System.out.println("[SUCCESS] Password recovered for admin: " + username);
            return response;
        } catch (Exception e) {
            System.err.println("[ERROR] Error in recoverAdminPassword: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static Map<String, String> recoverPassword(String facultyId, String email) {
        try {
            CSVReader reader = new CSVReader(USERS_CSV);
            List<Map<String, String>> allUsers = reader.readCSV();
            
            // Search for user by faculty_id and email
            Map<String, String> user = null;
            for (Map<String, String> u : allUsers) {
                String uFacultyId = u.getOrDefault("faculty_id", "");
                String uEmail = u.getOrDefault("email", "").toLowerCase().trim();
                String provEmail = email.toLowerCase().trim();
                
                if (facultyId.equals(uFacultyId) && uEmail.equals(provEmail)) {
                    user = u;
                    break;
                }
            }
            
            if (user == null) {
                System.out.println("[DEBUG] Faculty not found with ID: " + facultyId + " and email: " + email);
                return null;
            }
            
            // Verify it's a faculty user
            String role = user.getOrDefault("role", "");
            if (!role.equals("faculty")) {
                System.out.println("[DEBUG] User is not faculty: " + user.get("username"));
                return null;
            }
            
            // Return user with password (for pwd recovery only)
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get("username"));
            response.put("password", user.get("password"));
            response.put("faculty_id", user.get("faculty_id"));
            response.put("name", user.get("name"));
            response.put("email", user.get("email"));
            System.out.println("[SUCCESS] Password recovered for faculty: " + facultyId);
            return response;
        } catch (Exception e) {
            System.err.println("[ERROR] Error in recoverPassword: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static String toJSON(Map<String, String> user) {
        return gson.toJson(user);
    }
    
    public static String toJSON(List<Map<String, String>> userList) {
        return gson.toJson(userList);
    }
    
    public static boolean deleteUser(String username) {
        try {
            CSVReader reader = new CSVReader(USERS_CSV);
            List<Map<String, String>> data = reader.readCSV();
            
            boolean removed = false;
            int removeIndex = -1;
            
            for (int i = 0; i < data.size(); i++) {
                if (username.equals(data.get(i).get("username"))) {
                    removeIndex = i;
                    removed = true;
                    break;
                }
            }
            
            if (!removed) {
                return false;
            }
            
            System.out.println("[DEBUG-USER] Found user at index " + removeIndex + ", removing...");
            data.remove(removeIndex);
            System.out.println("[DEBUG-USER] Total users after deletion: " + data.size());
            
            CSVWriter writer = new CSVWriter(USERS_CSV);
            boolean writeResult = writer.writeAll(data, Arrays.asList(USER_HEADERS));
            
            if (writeResult) {
                System.out.println("[SUCCESS-USER] User deleted successfully: " + username);
            } else {
                System.err.println("[ERROR-USER] Failed to write users.csv after deletion");
            }
            
            return writeResult;
        } catch (Exception e) {
            System.err.println("[EXCEPTION-USER] Error in deleteUser(): " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Authenticate admin user with username, password, and email
     */
    public static Map<String, String> authenticateAdminWithEmail(String username, String password, String email) {
        CSVReader reader = new CSVReader(USERS_CSV);
        Map<String, String> user = reader.findRow("username", username);
        
        if (user != null && 
            user.getOrDefault("password", "").equals(password) &&
            user.getOrDefault("role", "").equals("admin") &&
            user.getOrDefault("email", "").equalsIgnoreCase(email)) {
            // Authentication successful
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get("username"));
            response.put("role", user.get("role"));
            response.put("email", user.get("email"));
            return response;
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Authenticate faculty user with faculty_id, password, and email
     */
    public static Map<String, String> authenticateFacultyWithEmail(String facultyId, String password, String email) {
        CSVReader reader = new CSVReader(USERS_CSV);
        List<Map<String, String>> allUsers = reader.readCSV();
        
        for (Map<String, String> user : allUsers) {
            if (user.getOrDefault("faculty_id", "").equals(facultyId) &&
                user.getOrDefault("password", "").equals(password) &&
                user.getOrDefault("role", "").equals("faculty") &&
                user.getOrDefault("email", "").equalsIgnoreCase(email)) {
                // Authentication successful
                Map<String, String> response = new HashMap<>();
                response.put("username", user.get("username"));
                response.put("faculty_id", user.get("faculty_id"));
                response.put("role", user.get("role"));
                response.put("email", user.get("email"));
                response.put("name", user.get("name"));
                return response;
            }
        }
        
        return null; // Authentication failed
    }
}
