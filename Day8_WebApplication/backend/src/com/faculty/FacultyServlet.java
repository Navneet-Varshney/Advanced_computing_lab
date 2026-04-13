package com.faculty;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacultyServlet implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        
        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }
        
        try {
            if (method.equals("GET") && path.equals("/api/faculty")) {
                handleGetAll(exchange);
            } else if (method.equals("GET") && path.matches("/api/faculty/\\d+")) {
                String facultyId = path.replaceAll("/api/faculty/", "");
                handleGetById(exchange, facultyId);
            } else if (method.equals("POST") && path.equals("/api/faculty")) {
                handleAdd(exchange);
            } else if (method.equals("PUT") && path.matches("/api/faculty/\\d+")) {
                String facultyId = path.replaceAll("/api/faculty/", "");
                handleUpdate(exchange, facultyId);
            } else if (method.equals("DELETE") && path.matches("/api/faculty/\\d+")) {
                String facultyId = path.replaceAll("/api/faculty/", "");
                handleDelete(exchange, facultyId);
            } else {
                sendError(exchange, 404, "Endpoint not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void handleGetAll(HttpExchange exchange) throws IOException {
        // GET is allowed for all (including guests)
        // No authorization required for read-only access
        List<Map<String, String>> faculty = FacultyManager.getAllFacultySorted();
        String response = FacultyManager.toJSON(faculty);
        sendResponse(exchange, 200, response);
    }
    
    private void handleGetById(HttpExchange exchange, String facultyId) throws IOException {
        // GET is allowed for all (including guests)
        // No authorization required for read-only access
        Map<String, String> faculty = FacultyManager.getFacultyById(facultyId);
        if (faculty != null) {
            String response = FacultyManager.toJSON(faculty);
            sendResponse(exchange, 200, response);
        } else {
            sendError(exchange, 404, "Faculty not found");
        }
    }
    
    private void handleAdd(HttpExchange exchange) throws IOException {
        // Check authorization and role
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !isAuthorized(auth)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        String role = extractRole(auth);
        if (!role.equals("admin")) {
            sendError(exchange, 403, "Only admins can add faculty");
            return;
        }
        
        // Read request body
        String body = readRequestBody(exchange);
        
        try {
            JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();
            Map<String, String> facultyData = new HashMap<>();
            
            for (String key : requestJson.keySet()) {
                facultyData.put(key, requestJson.get(key).getAsString());
            }
            
            // Use addFacultyWithUser to create user entry automatically
            Map<String, String> result = FacultyManager.addFacultyWithUser(facultyData);
            
            if (result != null) {
                // Check if this is an error response (duplicate ID)
                if (result.containsKey("error") && result.get("error").equals("true")) {
                    // Duplicate faculty ID error
                    JsonObject response = new JsonObject();
                    response.addProperty("success", false);
                    response.addProperty("message", result.get("message"));
                    response.addProperty("faculty_id", result.get("faculty_id"));
                    response.addProperty("suggested_ids", result.get("suggested_ids"));
                    sendResponse(exchange, 409, response.toString());
                } else {
                    // Success response
                    JsonObject response = new JsonObject();
                    response.addProperty("success", true);
                    response.addProperty("message", "Faculty added successfully");
                    response.addProperty("username", result.get("username"));
                    response.addProperty("generated_password", result.get("generated_password"));
                    response.addProperty("faculty_id", result.get("faculty_id"));
                    response.addProperty("name", result.get("name"));
                    sendResponse(exchange, 201, response.toString());
                }
            } else {
                sendError(exchange, 500, "Failed to add faculty");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }
    
    private void handleUpdate(HttpExchange exchange, String facultyId) throws IOException {
        // Check authorization
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !isAuthorized(auth)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        String role = extractRole(auth);
        String username = extractUsername(auth);
        
        System.out.println("[" + getTimestamp() + "] UPDATE REQUEST - FacultyID: " + facultyId + ", Role: " + role + ", Username from token: " + username);
        
        // Check if user is admin or faculty updating their own record
        if (!role.equals("admin")) {
            if (role.equals("faculty")) {
                // Faculty can only update their own record and only mobile/email
                Map<String, String> facultyRecord = FacultyManager.getFacultyById(facultyId);
                System.out.println("[" + getTimestamp() + "] Faculty record found: " + (facultyRecord != null));
                if (facultyRecord != null) {
                    String recordUsername = facultyRecord.getOrDefault("username", "");
                    System.out.println("[" + getTimestamp() + "] Faculty record username: '" + recordUsername + "'");
                    System.out.println("[" + getTimestamp() + "] Token username: '" + username + "'");
                    System.out.println("[" + getTimestamp() + "] Match: " + recordUsername.equals(username));
                }
                
                if (facultyRecord == null || !facultyRecord.getOrDefault("username", "").equals(username)) {
                    System.out.println("[" + getTimestamp() + "] 403 FORBIDDEN - Faculty cannot update this record");
                    sendError(exchange, 403, "Faculty can only update their own record");
                    return;
                }
            } else {
                sendError(exchange, 403, "Unauthorized to update faculty record");
                return;
            }
        }
        
        // Read request body
        String body = readRequestBody(exchange);
        
        try {
            JsonObject requestJson = JsonParser.parseString(body).getAsJsonObject();
            Map<String, String> updates = new HashMap<>();
            
            // If faculty member, only allow mobile and email updates
            if (role.equals("faculty") && !role.equals("admin")) {
                if (requestJson.has("mobile")) {
                    updates.put("mobile", requestJson.get("mobile").getAsString());
                }
                if (requestJson.has("email")) {
                    updates.put("email", requestJson.get("email").getAsString());
                }
            } else {
                // Admin can update all fields
                for (String key : requestJson.keySet()) {
                    updates.put(key, requestJson.get(key).getAsString());
                }
            }
            
            if (FacultyManager.updateFaculty(facultyId, updates)) {
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("message", "Faculty updated successfully");
                sendResponse(exchange, 200, response.toString());
            } else {
                sendError(exchange, 404, "Faculty not found");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }
    
    private void handleDelete(HttpExchange exchange, String facultyId) throws IOException {
        // Check authorization and role
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !isAuthorized(auth)) {
            sendError(exchange, 401, "Unauthorized");
            return;
        }
        
        String role = extractRole(auth);
        if (!role.equals("admin")) {
            sendError(exchange, 403, "Only admins can delete faculty");
            return;
        }
        
        if (FacultyManager.deleteFaculty(facultyId)) {
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Faculty deleted successfully");
            sendResponse(exchange, 200, response.toString());
        } else {
            sendError(exchange, 404, "Faculty not found");
        }
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
    
    private boolean isAuthorized(String auth) {
        return auth != null && auth.startsWith("Bearer ");
    }
    
    private String extractRole(String auth) {
        // Simple token parsing: Bearer username:role
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (token.contains(":")) {
                return token.split(":")[1];
            }
        }
        return "";
    }
    
    private String extractUsername(String auth) {
        // Simple token parsing: Bearer username:role
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (token.contains(":")) {
                return token.split(":")[0];
            }
        }
        return "";
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
