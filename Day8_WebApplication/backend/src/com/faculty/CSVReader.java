package com.faculty;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSVReader {
    private String filePath;
    
    public CSVReader(String filePath) {
        this.filePath = filePath;
    }
    
    public List<Map<String, String>> readCSV() {
        List<Map<String, String>> data = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            System.out.println("[" + getTimestamp() + "] Reading from: " + filePath);
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("[" + getTimestamp() + "] File is empty");
                return data;
            }
            
            String[] headers = parseCSVLine(headerLine);
            System.out.println("[" + getTimestamp() + "] Headers found: " + headers.length);
            String line;
            int rowCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] values = parseCSVLine(line);
                Map<String, String> row = new HashMap<>();
                
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = (i < values.length) ? values[i].trim() : "";
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    row.put(key, value);
                }
                
                data.add(row);
                rowCount++;
            }
            
            System.out.println("[" + getTimestamp() + "] Successfully read " + rowCount + " rows from " + filePath);
        } catch (IOException e) {
            System.err.println("[" + getTimestamp() + "] Error reading CSV file: " + filePath);
            System.err.println("[" + getTimestamp() + "] " + e.getMessage());
            e.printStackTrace();
        }
        
        return data;
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
                currentField.append(c);
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    public Map<String, String> findRow(String columnName, String value) {
        List<Map<String, String>> rows = readCSV();
        for (Map<String, String> row : rows) {
            if (row.getOrDefault(columnName, "").equals(value)) {
                return row;
            }
        }
        return null;
    }
    
    public List<Map<String, String>> findRows(String columnName, String pattern) {
        List<Map<String, String>> results = new ArrayList<>();
        List<Map<String, String>> rows = readCSV();
        String lowerPattern = pattern.toLowerCase();
        
        for (Map<String, String> row : rows) {
            String value = row.getOrDefault(columnName, "").toLowerCase();
            if (value.contains(lowerPattern)) {
                results.add(row);
            }
        }
        return results;
    }

    public List<Map<String, String>> readAll() {
        List<Map<String, String>> data = readCSV();
        boolean hasFacultyId = !data.isEmpty() && data.get(0).containsKey("faculty_id");
        if (hasFacultyId) {
            data.sort((a, b) -> {
                try {
                    int idA = Integer.parseInt(a.getOrDefault("faculty_id", "999999"));
                    int idB = Integer.parseInt(b.getOrDefault("faculty_id", "999999"));
                    return Integer.compare(idA, idB);
                } catch (NumberFormatException e) {
                    return 0;
                }
            });
        }
        return data;
    }
    
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
}