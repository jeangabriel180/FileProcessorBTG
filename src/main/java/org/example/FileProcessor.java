package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileProcessor {
    public static void main(String[] args) throws IOException, InterruptedException {

        Path path = Path.of("data.txt");

        // Leitura 1x o arquivo
        List<String> fileLines = new ArrayList<>();
        try (var br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            for (String line; (line = br.readLine()) != null; ) {
                fileLines.add(line);
            }
        }

        //Define um array de retorno + thread pool
        int n = fileLines.size();
        String[] output = new String[n];
        ExecutorService executor = Executors.newFixedThreadPool(5);

        //Envia as tarefas para o pool e aguarda conclusão
        try {
            for (int i = 0; i < n; i++) {
                final int idx = i;
                final String line = fileLines.get(idx);
                executor.submit(() -> output[idx] = line.toUpperCase());
            }
        } finally {
            executor.shutdown();
        }

        // Aguarda todas as threads finalizarem se não, lança exceção
        if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
            executor.shutdownNow();
            throw new IllegalStateException("Timeout processing file");
        }

        System.out.println("Lines processed: " + output.length);
    }
}
