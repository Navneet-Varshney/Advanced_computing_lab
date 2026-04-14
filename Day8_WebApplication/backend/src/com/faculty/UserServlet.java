package com.faculty;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserServlet implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        
        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }
        
        if (method.equals("POST") && path.equals("/api/login")) {
            handleLogin(exchange);
        } else if (method.equals("POST") && path.equals("/api/forgot-password")) {
            handleForgotPassword(exchange);
        } else {
            sendError(exchange, 404, "Not found");
        }
    }
    
    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        
        try {
            JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();
            
            // Get password (required for both)
            if (!requestJson.has("password") || requestJson.get("password").isJsonNull()) {
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Password is required");
                sendResponse(exchange, 400, response.toString());
                return;
            }
            String password = requestJson.get("password").getAsString();
            
            // Check if email is provided
            String email = null;
            if (requestJson.has("email") && !requestJson.get("email").isJsonNull()) {
                email = requestJson.get("email").getAsString();
            }
            
            // Check if this is faculty login (has faculty_id) or admin login (has username)
            String facultyId = null;
            String username = null;
            
            if (requestJson.has("faculty_id") && !requestJson.get("faculty_id").isJsonNull()) {
                facultyId = requestJson.get("faculty_id").getAsString();
            }
            
            if (requestJson.has("username") && !requestJson.get("username").isJsonNull()) {
                username = requestJson.get("username").getAsString();
            }
            
            // Authenticate based on role indicators
            Map<String, String> user = null;
            String errorMessage = "Invalid credentials";
            
            // Faculty login: faculty_id + password + email
            if (facultyId != null && email != null) {
                user = UserManager.authenticateFacultyWithEmail(facultyId, password, email);
                errorMessage = "Invalid faculty ID, password, or email";
            } 
            // Admin login: username + password + email
            else if (username != null && email != null) {
                user = UserManager.authenticateAdminWithEmail(username, password, email);
                errorMessage = "Invalid username, password, or email";
            }
            // Fallback for regular login without email (backward compatibility)
            else if (username != null) {
                user = UserManager.authenticate(username, password);
            }
            else {
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Must provide either username or faculty_id");
                sendResponse(exchange, 400, response.toString());
                return;
            }
            
            if (user == null) {
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", errorMessage);
                sendResponse(exchange, 401, response.toString());
                return;
            }
            
            // Login successful
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("username", user.get("username"));
            response.addProperty("role", user.get("role"));
            
            if (user.containsKey("faculty_id")) {
                response.addProperty("faculty_id", user.get("faculty_id"));
            }
            
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("success", false);
            response.addProperty("message", "Error processing login: " + e.getMessage());
            sendResponse(exchange, 400, response.toString());
        }
    }
    
    private void handleForgotPassword(HttpExchange exchange) throws IOException {
        // Read request body
        String body = readRequestBody(exchange);
        
        System.out.println("[" + getTimestamp() + "] === FORGOT PASSWORD REQUEST START ===");
        System.out.println("[" + getTimestamp() + "] Raw body: " + body);
        
        try {
            // Check if body is empty
            if (body == null || body.trim().isEmpty()) {
                System.out.println("[ERROR] Body is empty");
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Request body is empty");
                sendResponse(exchange, 400, response.toString());
                return;
            }
            
            JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();
            System.out.println("[" + getTimestamp() + "] Parsed JSON success");
            System.out.println("[" + getTimestamp() + "] JSON keys: " + requestJson.keySet().toString());
            
            // Get userType with safe null check
            String userType = null;
            if (requestJson.has("userType") && !requestJson.get("userType").isJsonNull()) {
                userType = requestJson.get("userType").getAsString();
                System.out.println("[" + getTimestamp() + "] userType: " + userType);
            } else {
                System.out.println("[ERROR] userType is missing or null");
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Missing or null userType field");
                sendResponse(exchange, 400, response.toString());
                return;
            }
            
            if ("admin".equals(userType)) {
                System.out.println("[" + getTimestamp() + "] Processing ADMIN recovery");
                
                // Check fields
                if (!requestJson.has("username") || requestJson.get("username").isJsonNull()) {
                    System.out.println("[ERROR] username is missing or null");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Missing username field");
                    sendResponse(exchange, 400, response.toString());
                    return;
                }
                
                if (!requestJson.has("email") || requestJson.get("email").isJsonNull()) {
                    System.out.println("[ERROR] email is missing or null");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Missing email field");
                    sendResponse(exchange, 400, response.toString());
                    return;
                }
                
                String username = requestJson.get("username").getAsString();
                String email = requestJson.get("email").getAsString();
                
                System.out.println("[" + getTimestamp() + "] Admin - username: " + username + ", email: " + email);
                
                Map<String, String> result = UserManager.recoverAdminPassword(username, email);
                
                if (result != null) {
                    System.out.println("[" + getTimestamp() + "] Admin password found!");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("message", "Password retrieved successfully");
                    response.addProperty("username", result.get("username"));
                    response.addProperty("password", result.get("password"));
                    response.addProperty("email", result.get("email"));
                    sendResponse(exchange, 200, response.toString());
                } else {
                    System.out.println("[" + getTimestamp() + "] Admin password not found!");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid username or email");
                    sendResponse(exchange, 401, response.toString());
                }
            } else if ("faculty".equals(userType)) {
                System.out.println("[" + getTimestamp() + "] Processing FACULTY recovery");
                
                // Check fields
                if (!requestJson.has("faculty_id") || requestJson.get("faculty_id").isJsonNull()) {
                    System.out.println("[ERROR] faculty_id is missing or null");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Missing faculty_id field");
                    sendResponse(exchange, 400, response.toString());
                    return;
                }
                
                if (!requestJson.has("email") || requestJson.get("email").isJsonNull()) {
                    System.out.println("[ERROR] email is missing or null");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Missing email field");
                    sendResponse(exchange, 400, response.toString());
                    return;
                }
                
                String facultyId = requestJson.get("faculty_id").getAsString();
                String email = requestJson.get("email").getAsString();
                
                System.out.println("[" + getTimestamp() + "] Faculty - facultyId: " + facultyId + ", email: " + email);
                
                Map<String, String> result = UserManager.recoverPassword(facultyId, email);
                
                if (result != null) {
                    System.out.println("[" + getTimestamp() + "] Faculty password found!");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("message", "Password retrieved successfully");
                    response.addProperty("username", result.get("username"));
                    response.addProperty("password", result.get("password"));
                    response.addProperty("faculty_id", result.get("faculty_id"));
                    response.addProperty("name", result.get("name"));
                    response.addProperty("email", result.get("email"));
                    sendResponse(exchange, 200, response.toString());
                } else {
                    System.out.println("[" + getTimestamp() + "] Faculty password not found!");
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", "Invalid Faculty ID or email");
                    sendResponse(exchange, 401, response.toString());
                }
            } else {
                System.out.println("[ERROR] Invalid userType: " + userType);
                JsonObject response = new JsonObject();
                response.addProperty("success", false);
                response.addProperty("message", "Invalid user type: " + userType);
                sendResponse(exchange, 400, response.toString());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in handleForgotPassword: " + e.getClass().getName());
            System.err.println("[ERROR] Message: " + e.getMessage());
            e.printStackTrace();
            
            JsonObject response = new JsonObject();
            response.addProperty("success", false);
            response.addProperty("message", "Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            try {
                sendResponse(exchange, 400, response.toString());
            } catch (Exception ex) {
                System.err.println("[ERROR] Failed to send error response: " + ex.getMessage());
            }
        }
        System.out.println("[" + getTimestamp() + "] === FORGOT PASSWORD REQUEST END ===");
    }
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int length;
        
        while ((length = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, length));
        }
        
        return sb.toString();
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("message", message);
        sendResponse(exchange, statusCode, error.toString());
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
}
