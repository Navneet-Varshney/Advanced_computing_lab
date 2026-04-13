package com.faculty;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.*;
import java.io.*;
import java.util.*;

public class SearchServlet implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        
        if (method.equals("OPTIONS")) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }
        
        if (method.equals("GET") && path.equals("/api/search")) {
            handleSearch(exchange, query);
        } else {
            sendError(exchange, 404, "Endpoint not found");
        }
    }
    
    private void handleSearch(HttpExchange exchange, String query) throws IOException {
        try {
            String searchTerm = "";
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("q=")) {
                        searchTerm = param.substring(2);
                        searchTerm = java.net.URLDecoder.decode(searchTerm, "UTF-8");
                        break;
                    }
                }
            }
            
            if (searchTerm.isEmpty()) {
                sendError(exchange, 400, "Search query parameter 'q' is required");
                return;
            }
            
            List<Map<String, String>> results = FacultyManager.searchFaculty(searchTerm);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("count", results.size());
            response.add("results", JsonParser.parseString(FacultyManager.toJSON(results)));
            
            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Search error: " + e.getMessage());
        }
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
}
