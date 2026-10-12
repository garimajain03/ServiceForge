package com.serviceforge.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Lightweight memory recording helper for training/demo purposes.
 * Writes small JSON lines into .github/outputs/serviceforge-memory.json so reviewer
 * rules can consult a stable artifact. This is intentionally simple and not a
 * replacement for a real memory service.
 */
public final class MemoryRecorder {

    private static final Path OUT_DIR = Paths.get(".github", "outputs");
    private static final Path OUT_FILE = OUT_DIR.resolve("serviceforge-memory.jsonl");

    private MemoryRecorder() {
    }

    public static synchronized void record(String namespace, String jsonPayload) {
        try {
            if (!Files.exists(OUT_DIR)) {
                Files.createDirectories(OUT_DIR);
            }
            try (FileWriter fw = new FileWriter(OUT_FILE.toFile(), true)) {
                String line = String.format("{\"ts\":\"%s\",\"ns\":\"%s\",\"payload\":%s}%n",
                        Instant.now().toString(), namespace, jsonPayload);
                fw.write(line);
            }
        } catch (IOException e) {
            // Fail silently in tests; recording is best-effort for this training repo.
            System.err.println("MemoryRecorder: failed to write memory artifact: " + e.getMessage());
        }
    }
}
