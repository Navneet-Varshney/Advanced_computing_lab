package com.faculty;

import java.io.*;
import java.util.*;

public class CSVWriter {
    private String filePath;
    
    public CSVWriter(String filePath) {
        this.filePath = filePath;
    }
    
    public boolean writeCSV(List<Map<String, String>> data, List<String> headers) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            
            String headerLine = String.join(",", headers);
            writer.write(headerLine);
            writer.newLine();
            int count = 0;
            for (Map<String, String> row : data) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    values.add(row.getOrDefault(header, ""));
                }
                writer.write(String.join(",", values));
                writer.newLine();
                count++;
            }
            
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + filePath);
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean appendRow(Map<String, String> row, List<String> headers) {
        try {
            List<Map<String, String>> existingData = new CSVReader(filePath).readCSV();
            existingData.add(row);
            
            if (headers.contains("faculty_id")) {
                existingData.sort((a, b) -> {
                    try {
                        int idA = Integer.parseInt(a.getOrDefault("faculty_id", "999999"));
                        int idB = Integer.parseInt(b.getOrDefault("faculty_id", "999999"));
                        return Integer.compare(idA, idB);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });
            }
            
            return writeCSV(existingData, headers);
        } catch (Exception e) {
            System.err.println("Error appending row to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateRow(String matchColumn, String matchValue, Map<String, String> updatedRow, List<String> headers) {
        List<Map<String, String>> allData = new CSVReader(filePath).readCSV();
        boolean found = false;
        
        for (Map<String, String> row : allData) {
            if (row.getOrDefault(matchColumn, "").equals(matchValue)) {
                row.putAll(updatedRow);
                found = true;
                break;
            }
        }
        
        if (found) {
            return writeCSV(allData, headers);
        }
        
        return false;
    }
    
    public boolean deleteRow(String matchColumn, String matchValue, List<String> headers) {
        List<Map<String, String>> allData = new CSVReader(filePath).readCSV();
        boolean found = false;
        
        for (int i = 0; i < allData.size(); i++) {
            if (allData.get(i).getOrDefault(matchColumn, "").equals(matchValue)) {
                allData.remove(i);
                found = true;
                break;
            }
        }
        
        if (found) {
            return writeCSV(allData, headers);
        }
        
        return false;
    }

    public boolean writeAll(List<Map<String, String>> data, List<String> headers) {
        return writeCSV(data, headers);
    }
}
