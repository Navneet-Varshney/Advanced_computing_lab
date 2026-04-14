package com.faculty;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.file.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.Scanner;

public class Server {
    private static final int PORT = 9002;
    private static final String FRONTEND_DIR = "frontend";
    private static String serverIP = "localhost";  // Default IP
    
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP address (press Enter for localhost): ");
        String userInput = scanner.nextLine().trim();
        if (!userInput.isEmpty()) {
            serverIP = userInput;
        }
        scanner.close();
        
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        
        server.createContext("/config", new ConfigHandler());
        
        // API endpoints
        server.createContext("/api/login", new UserServlet());
        server.createContext("/api/forgot-password", new UserServlet());
        server.createContext("/api/faculty", new FacultyServlet());
        server.createContext("/api/search", new SearchServlet());
        
        // Serve static files (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler(FRONTEND_DIR));
        
        server.setExecutor(Executors.newCachedThreadPool());
        
        server.start();
        System.out.println("Server started successfully!");
        System.out.println("Server IP: " + serverIP + ":" + PORT);
        System.out.println("Access URL: http://" + serverIP + ":" + PORT);
        System.out.println("Frontend files served from: " + new File(FRONTEND_DIR).getAbsolutePath());
        System.out.println("\nAPI endpoints available:");
        System.out.println("   GET    /config                - Get server configuration");
        System.out.println("   POST   /api/login             - Login (username, password)");
        System.out.println("   POST   /api/forgot-password   - Forgot password recovery");
        System.out.println("   GET    /api/faculty           - Get all faculty");
        System.out.println("   GET    /api/faculty/:id       - Get faculty by ID");
        System.out.println("   POST   /api/faculty           - Add faculty (admin only)");
        System.out.println("   PUT    /api/faculty/:id       - Update faculty");
        System.out.println("   DELETE /api/faculty/:id       - Delete faculty (admin only)");
        System.out.println("   GET    /api/search?q=term     - Search faculty");
    }
    
    /**
     * Static file handler for serving HTML, CSS, JS files
     */
    static class StaticFileHandler implements HttpHandler {
        private String baseDir;
        
        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html if root is requested
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }
            
            // Security: prevent directory traversal
            if (path.contains("..")) {
                send404(exchange);
                return;
            }
            
            File file = new File(baseDir + path);
            
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                
                try {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, fileContent.length);
                    
                    OutputStream os = exchange.getResponseBody();
                    os.write(fileContent);
                    os.close();
                } catch (IOException e) {
                    send500(exchange);
                }
            } else {
                send404(exchange);
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                return "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                return "application/javascript; charset=UTF-8";
            } else if (path.endsWith(".json")) {
                return "application/json; charset=UTF-8";
            } else if (path.endsWith(".png")) {
                return "image/png";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (path.endsWith(".gif")) {
                return "image/gif";
            } else {
                return "application/octet-stream";
            }
        }
        
        private void send404(HttpExchange exchange) throws IOException {
            String response = "404 - Not Found";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
        private void send500(HttpExchange exchange) throws IOException {
            String response = "500 - Internal Server Error";
            exchange.sendResponseHeaders(500, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    /**
     * Config handler - returns server configuration
     */
    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Build JSON response with API base URL
                String json = String.format(
                    "{\"api_base\": \"http://%s:%d\", \"server_ip\": \"%s\"}",
                    serverIP, PORT, serverIP
                );
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, json.getBytes().length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(json.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
}

