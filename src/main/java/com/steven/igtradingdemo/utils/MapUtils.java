package com.steven.igtradingdemo.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MapUtils {
    public static File convertMapToCsv(Map<String, String> map, String fileName) {
        File file = new File(fileName.concat(".csv"));
        try {
            FileWriter fileWriter = new FileWriter(file);
            String header = String.join(",", map.keySet());
            String values = String.join(",", map.values());
            fileWriter.write(header.concat("\n"));
            fileWriter.write(values.concat("\n"));
            fileWriter.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create csv file for ".concat(fileName));
        }
    }
}
