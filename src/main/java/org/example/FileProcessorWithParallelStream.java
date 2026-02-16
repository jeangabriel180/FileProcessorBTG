package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessorWithParallelStream {
    private static List<String> lines = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get("data.txt"));
        
        lines = fileLines.parallelStream()
            .map(String::toUpperCase)
            .toList();
        
        System.out.println("Lines processed: " + lines.size());
    }
}