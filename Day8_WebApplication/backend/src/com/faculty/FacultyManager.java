package com.faculty;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

public class FacultyManager {
    private static final String FACULTY_CSV = "data/faculty.csv";
    private static final String[] FACULTY_HEADERS = {"faculty_id", "name", "department", "designation", "mobile", "email", "specialization", "username"};
    private static final Gson gson = new Gson();

    public static List<Map<String, String>> getAllFaculty() {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        return reader.readAll();
    }

    public static Map<String, String> getFacultyById(String id) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        return data.stream()
                .filter(row -> id.equals(row.get("faculty_id")))
                .findFirst()
                .orElse(null);
    }

    public static Map<String, String> getFacultyByUsername(String username) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        return data.stream()
                .filter(row -> username.equals(row.get("username")))
                .findFirst()
                .orElse(null);
    }

    public static List<Map<String, String>> searchFaculty(String query) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        String lowerQuery = query.toLowerCase();
        List<Map<String, String>> results = new ArrayList<>();
        
        for (Map<String, String> faculty : data) {
            if (faculty.get("faculty_id").toLowerCase().contains(lowerQuery) ||
                faculty.get("name").toLowerCase().contains(lowerQuery) ||
                faculty.get("department").toLowerCase().contains(lowerQuery) ||
                faculty.get("email").toLowerCase().contains(lowerQuery)
                || faculty.get("designation").toLowerCase().contains(lowerQuery)
                || faculty.get("specialization").toLowerCase().contains(lowerQuery)) {
                results.add(faculty);
            }
        }
        return results;
    }

    /**
     * Add new faculty (admin only) - ORIGINAL METHOD FOR BACKWARD COMPATIBILITY
     * Returns boolean for if-checks in servlets
     */
    public static boolean addFaculty(Map<String, String> facultyData) {
        CSVWriter writer = new CSVWriter(FACULTY_CSV);
        return writer.appendRow(facultyData, Arrays.asList(FACULTY_HEADERS));
    }

    /**
     * Add new faculty with auto user creation - NEW METHOD
     * Returns Map with generated credentials (username, password)
     * If duplicate ID: returns Map with error, nextAvailableId, suggestedIds
     */
    public static Map<String, String> addFacultyWithUser(Map<String, String> facultyData) {
        try {
            String facultyId = facultyData.get("faculty_id");
            
            // Check if faculty_id already exists
            if (facultyIdExists(facultyId)) {
                System.out.println("[WARNING] Duplicate faculty ID attempted: " + facultyId);
                
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "true");
                errorResponse.put("message", "Faculty ID already exists: " + facultyId);
                errorResponse.put("faculty_id", facultyId);
                
                // Suggest next available IDs in same department
                List<String> suggestedIds = getSuggestedAlternativeIds(facultyId);
                errorResponse.put("suggested_ids", String.join(",", suggestedIds));
                
                return errorResponse;
            }
            
            // Generate username if not provided
            if (!facultyData.containsKey("username") || facultyData.get("username").isEmpty()) {
                String name = facultyData.get("name");
                String username = generateUsername(name);
                facultyData.put("username", username);
            }
            
            System.out.println("[" + getTimestamp() + "] Adding faculty: " + facultyData.get("name"));
            
            // Add to faculty.csv
            CSVWriter writer = new CSVWriter(FACULTY_CSV);
            if (!writer.appendRow(facultyData, Arrays.asList(FACULTY_HEADERS))) {
                System.err.println("[ERROR] Failed to add faculty to faculty.csv: " + facultyData.get("name"));
                return null;
            }
            
            System.out.println("[" + getTimestamp() + "] Faculty added to CSV successfully");
            
            // Generate simple password (username_before_underscore + 123)
            String username = facultyData.get("username");
            String simplePassword = generateSimplePassword(username);
            
            // Auto-create user entry in users.csv
            Map<String, String> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("password", simplePassword);
            userData.put("role", "faculty");
            userData.put("faculty_id", facultyData.get("faculty_id"));
            userData.put("name", facultyData.get("name"));
            userData.put("email", facultyData.getOrDefault("email", ""));
            
            System.out.println("[" + getTimestamp() + "] UserData prepared: username=" + username + ", password=" + simplePassword);
            
            // Add user to users.csv
            boolean userAdded = UserManager.addUser(userData);
            System.out.println("[" + getTimestamp() + "] UserManager.addUser() returned: " + userAdded);
            
            if (userAdded) {
                // Return result with generated credentials
                Map<String, String> result = new HashMap<>(userData);
                result.put("generated_password", simplePassword);
                System.out.println("[SUCCESS] Faculty added: " + facultyData.get("name") + 
                                 " | Username: " + username + 
                                 " | Password: " + simplePassword);
                return result;
            } else {
                System.err.println("[ERROR] Failed to add user to users.csv for: " + facultyData.get("name"));
                return null;
            }
        } catch (Exception e) {
            System.err.println("[EXCEPTION] Error in addFacultyWithUser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update faculty by ID
     */
    public static boolean updateFaculty(String id, Map<String, String> facultyData) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        
        for (Map<String, String> row : data) {
            if (id.equals(row.get("faculty_id"))) {
                row.putAll(facultyData);
                CSVWriter writer = new CSVWriter(FACULTY_CSV);
                return writer.writeAll(data, Arrays.asList(FACULTY_HEADERS));
            }
        }
        return false;
    }

    /**
     * Delete faculty by ID
     */
    public static boolean deleteFaculty(String id) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        
        // First, find and store faculty data BEFORE deleting (to get username)
        Map<String, String> facultyToDelete = null;
        int deleteIndex = -1;
        
        for (int i = 0; i < data.size(); i++) {
            if (id.equals(data.get(i).get("faculty_id"))) {
                facultyToDelete = data.get(i);
                deleteIndex = i;
                break;
            }
        }
        
        if (facultyToDelete == null) {
            System.err.println("[ERROR] Faculty not found for deletion: " + id);
            return false;
        }
        
        String username = facultyToDelete.get("username");
        System.out.println("[" + getTimestamp() + "] Deleting faculty: " + facultyToDelete.get("name") + " (ID: " + id + ", Username: " + username + ")");
        
        // Remove from faculty.csv
        data.remove(deleteIndex);
        
        CSVWriter writer = new CSVWriter(FACULTY_CSV);
        if (!writer.writeAll(data, Arrays.asList(FACULTY_HEADERS))) {
            System.err.println("[ERROR] Failed to update faculty.csv after deletion");
            return false;
        }
        
        System.out.println("[SUCCESS] Faculty removed from faculty.csv");
        
        // Now remove user from users.csv
        if (username != null && !username.isEmpty()) {
            System.out.println("[" + getTimestamp() + "] Attempting to delete user: " + username);
            boolean userDeleted = UserManager.deleteUser(username);
            if (userDeleted) {
                System.out.println("[SUCCESS] User deleted from users.csv: " + username);
            } else {
                System.err.println("[ERROR] Failed to delete user from users.csv: " + username);
            }
        }
        
        return true;
    }

    /**
     * Generate unique username from faculty name
     * Example: "Ahmad Ali Khan" -> "ahmad_a"
     */
    private static String generateUsername(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) return "user_" + System.currentTimeMillis();
        
        String firstName = parts[0].toLowerCase();
        String lastInitial = parts.length > 1 ? parts[parts.length - 1].toLowerCase().charAt(0) + "" : "";
        
        // Check if username exists, add number suffix if needed
        String username = firstName + (lastInitial.isEmpty() ? "" : "_" + lastInitial);
        int counter = 1;
        String originalUsername = username;
        
        while (getFacultyByUsername(username) != null || UserManager.getUserByUsername(username) != null) {
            username = originalUsername + counter;
            counter++;
        }
        
        return username;
    }

    /**
     * Generate simple password: username_before_underscore + "@123"
     * Example: "ahmad_khan" -> "ahmad@123"
     */
    private static String generateSimplePassword(String username) {
        if (username == null || username.isEmpty()) {
            return "user@123";
        }
        
        // Get part before underscore, or whole username if no underscore
        String prefix = username.contains("_") ? username.split("_")[0] : username;
        
        System.out.println("[" + getTimestamp() + "] Generated password: " + prefix + "@123 (from username: " + username + ")");
        return prefix + "@123";
    }

    /**
     * Generate secure random password (deprecated - no longer used)
     * Format: 4 special characters + 2 numbers + 2 letters (shuffled)
     * Examples: @7#R$2m!, &$%Oq8%0, s!%*!X11
     */
    @Deprecated
    private static String generateRandomPassword() {
        String specialChars = "@#$%&*!";
        String numbers = "0123456789";
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Add 4 random special characters
        for (int i = 0; i < 4; i++) {
            password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        }
        
        // Add 2 random numbers
        for (int i = 0; i < 2; i++) {
            password.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        
        // Add 2 random letters
        for (int i = 0; i < 2; i++) {
            password.append(letters.charAt(random.nextInt(letters.length())));
        }
        
        // Shuffle the password
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            result.append(c);
        }
        
        return result.toString();
    }

    /**
     * Get the next available faculty ID
     */
    public static int getNextFacultyId() {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        
        int maxId = 100;
        for (Map<String, String> row : data) {
            try {
                int id = Integer.parseInt(row.get("faculty_id"));
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric IDs
            }
        }
        
        return maxId + 1;
    }

    /**
     * Check if faculty ID already exists
     */
    public static boolean facultyIdExists(String facultyId) {
        CSVReader reader = new CSVReader(FACULTY_CSV);
        List<Map<String, String>> data = reader.readAll();
        
        for (Map<String, String> row : data) {
            if (facultyId.equals(row.get("faculty_id"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get suggested alternative IDs in the same department
     * Example: if 105 exists in dept 100-199, suggests 106, 107, 108
     */
    public static List<String> getSuggestedAlternativeIds(String existingId) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            int id = Integer.parseInt(existingId);
            int department = (id / 100) * 100; // Get department range (e.g., 100-199)
            int maxInDept = department + 99;
            int nextId = id + 1;
            
            // Suggest next 3 available IDs in same department
            int count = 0;
            while (nextId <= maxInDept && count < 3) {
                String candidateId = String.valueOf(nextId);
                if (!facultyIdExists(candidateId)) {
                    suggestions.add(candidateId);
                    count++;
                }
                nextId++;
            }
            
            // If we couldn't find 3 in same department, try next department
            if (count < 3) {
                nextId = maxInDept + 1;
                int maxNewDept = nextId + 99;
                while (nextId <= maxNewDept && count < 3) {
                    String candidateId = String.valueOf(nextId);
                    if (!facultyIdExists(candidateId)) {
                        suggestions.add(candidateId);
                        count++;
                    }
                    nextId++;
                }
            }
        } catch (NumberFormatException e) {
            // If ID is not numeric, just suggest generic next ID
            suggestions.add(String.valueOf(getNextFacultyId()));
        }
        
        return suggestions;
    }

    /**
     * Convert single faculty object to JSON string
     */
    public static String toJSON(Map<String, String> faculty) {
        return gson.toJson(faculty);
    }

    /**
     * Convert list of faculty to JSON string
     */
    public static String toJSON(List<Map<String, String>> facultyList) {
        return gson.toJson(facultyList);
    }

    /**
     * Get all faculty sorted by faculty_id
     */
    public static List<Map<String, String>> getAllFacultySorted() {
        List<Map<String, String>> data = getAllFaculty();
        data.sort((a, b) -> {
            try {
                int idA = Integer.parseInt(a.getOrDefault("faculty_id", "999999"));
                int idB = Integer.parseInt(b.getOrDefault("faculty_id", "999999"));
                return Integer.compare(idA, idB);
            } catch (NumberFormatException e) {
                return 0;
            }
        });
        return data;
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
}
