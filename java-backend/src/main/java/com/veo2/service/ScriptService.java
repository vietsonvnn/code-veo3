package com.veo2.service;

import com.google.gson.Gson;
import com.veo2.integration.GeminiApiClient;
import com.veo2.model.Script;
import com.veo2.util.FileUtils;
import com.veo2.util.MockDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScriptService {

    private static final Logger log = LoggerFactory.getLogger(ScriptService.class);

    @Autowired
    private GeminiApiClient geminiApiClient;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private MockDataGenerator mockDataGenerator;

    @Value("${veo2.storage.scripts-dir}")
    private String scriptsDir;

    private final Gson gson = new Gson();
    private final Map<String, Script> scriptCache = new HashMap<>();

    /**
     * Generate script using Gemini API or Mock Data
     */
    public Script generateScript(String topic, int duration, String aspectRatio, String style) {
        return generateScript(topic, duration, aspectRatio, style, false);
    }

    /**
     * Generate script with option to use mock data
     */
    public Script generateScript(String topic, int duration, String aspectRatio, String style, boolean useMock) {
        try {
            log.info("Generating script for topic: {} (mock={})", topic, useMock);

            // Validate inputs
            if (duration < 8 || duration > 180) {
                throw new IllegalArgumentException("Duration must be between 8 and 180 seconds");
            }

            // Generate script using Mock or Gemini
            Script script;
            if (useMock || topic.toLowerCase().startsWith("[mock]") || topic.toLowerCase().startsWith("test")) {
                log.info("Using mock data generator");
                script = mockDataGenerator.generateMockScript(topic, duration, aspectRatio, style);
            } else {
                log.info("Using Gemini API");
                script = geminiApiClient.generateScript(topic, duration, aspectRatio, style);
            }

            // Save script to file
            saveScript(script);

            // Cache script
            scriptCache.put(script.getScriptId(), script);

            log.info("Script generated successfully: {}", script.getScriptId());
            return script;

        } catch (IOException e) {
            log.error("Failed to generate script: {}", e.getMessage());
            throw new RuntimeException("Script generation failed", e);
        }
    }

    /**
     * Get script by ID
     */
    public Script getScript(String scriptId) {
        // Check cache first
        if (scriptCache.containsKey(scriptId)) {
            return scriptCache.get(scriptId);
        }

        // Load from file
        try {
            Script script = loadScript(scriptId);
            scriptCache.put(scriptId, script);
            return script;
        } catch (IOException e) {
            log.error("Failed to load script {}: {}", scriptId, e.getMessage());
            return null;
        }
    }

    /**
     * Save script to JSON file
     */
    private void saveScript(Script script) throws IOException {
        fileUtils.createDirectory(scriptsDir);

        String filePath = String.format("%s/%s.json", scriptsDir, script.getScriptId());
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(script, writer);
            log.info("Script saved to: {}", filePath);
        }
    }

    /**
     * Load script from JSON file
     */
    private Script loadScript(String scriptId) throws IOException {
        String filePath = String.format("%s/%s.json", scriptsDir, scriptId);

        if (!fileUtils.fileExists(filePath)) {
            throw new IOException("Script file not found: " + scriptId);
        }

        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Script.class);
        }
    }

    /**
     * Update script
     */
    public Script updateScript(Script script) {
        try {
            saveScript(script);
            scriptCache.put(script.getScriptId(), script);
            log.info("Script updated: {}", script.getScriptId());
            return script;
        } catch (IOException e) {
            log.error("Failed to update script: {}", e.getMessage());
            throw new RuntimeException("Script update failed", e);
        }
    }

    /**
     * Delete script
     */
    public boolean deleteScript(String scriptId) {
        String filePath = String.format("%s/%s.json", scriptsDir, scriptId);
        scriptCache.remove(scriptId);
        return fileUtils.deleteFile(filePath);
    }

    /**
     * Get all script IDs
     */
    public String[] getAllScriptIds() {
        fileUtils.createDirectory(scriptsDir);
        java.io.File[] files = fileUtils.getFilesWithExtension(scriptsDir, "json");

        return java.util.Arrays.stream(files)
                .map(file -> file.getName().replace(".json", ""))
                .toArray(String[]::new);
    }
}
