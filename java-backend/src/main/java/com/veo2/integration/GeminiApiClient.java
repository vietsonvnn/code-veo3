package com.veo2.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.veo2.model.Scene;
import com.veo2.model.Script;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GeminiApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

    @Value("${veo2.gemini.api-key}")
    private String apiKey;

    @Value("${veo2.gemini.api-url}")
    private String apiUrl;

    @Value("${veo2.gemini.model}")
    private String model;

    @Value("${veo2.gemini.temperature}")
    private double temperature;

    @Value("${veo2.gemini.max-tokens}")
    private int maxTokens;

    private final OkHttpClient httpClient;
    private final Gson gson;

    public GeminiApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Generate script using Gemini API
     */
    public Script generateScript(String topic, int duration, String aspectRatio, String style) throws IOException {
        log.info("Generating script for topic: {}, duration: {}s", topic, duration);

        int numScenes = duration / 8; // 8 seconds per scene
        String prompt = buildPrompt(topic, duration, numScenes, aspectRatio, style);

        String response = callGeminiApi(prompt);
        Script script = parseScriptResponse(response, topic, duration, aspectRatio, style);

        log.info("Generated script with {} scenes", script.getNumScenes());
        return script;
    }

    /**
     * Build prompt for Gemini API
     */
    private String buildPrompt(String topic, int duration, int numScenes, String aspectRatio, String style) {
        return String.format("""
            You are a professional video script writer. Create a detailed video script for VEO 3.1 AI video generator.

            Topic: %s
            Total Duration: %d seconds
            Number of Scenes: %d (each scene is exactly 8 seconds)
            Aspect Ratio: %s
            Visual Style: %s

            Requirements:
            1. Each scene must be EXACTLY 8 seconds long
            2. Create vivid, detailed descriptions in Vietnamese
            3. Write clear English prompts for VEO 3.1 AI (optimized for video generation)
            4. Include camera movements, time of day, and mood for each scene
            5. Ensure smooth transitions between scenes
            6. Make the story compelling and visually interesting

            Return ONLY a valid JSON object (no markdown, no code blocks) with this exact structure:
            {
              "title": "Engaging video title in Vietnamese",
              "total_duration": %d,
              "num_scenes": %d,
              "scenes": [
                {
                  "scene_number": 1,
                  "duration": 8,
                  "description": "Vietnamese description of the scene",
                  "veo_prompt": "Detailed English prompt optimized for VEO 3.1: describe visual elements, actions, lighting, composition, camera angle, and movement in cinematic detail",
                  "camera_movement": "slow pan left | static | zoom in | tracking shot | etc",
                  "time_of_day": "golden hour | night | day | sunset | dawn",
                  "mood": "peaceful | energetic | mysterious | dramatic | etc"
                }
              ]
            }

            IMPORTANT:
            - VEO prompts must be detailed and cinematic (at least 2-3 sentences)
            - Include specific visual details: lighting, colors, composition, camera angles
            - Avoid abstract concepts, focus on concrete visual elements
            - Use film/photography terminology for better results
            """, topic, duration, numScenes, aspectRatio, style, duration, numScenes);
    }

    /**
     * Call Gemini API
     */
    private String callGeminiApi(String prompt) throws IOException {
        String url = String.format("%s/%s:generateContent?key=%s", apiUrl, model, apiKey);

        JsonObject requestBody = new JsonObject();
        JsonObject contents = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);

        JsonObject contentsArray = new JsonObject();
        contentsArray.add("parts", gson.toJsonTree(new JsonObject[]{gson.fromJson(parts.toString(), JsonObject.class)}));

        requestBody.add("contents", gson.toJsonTree(new JsonObject[]{gson.fromJson(contentsArray.toString(), JsonObject.class)}));

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", temperature);
        generationConfig.addProperty("maxOutputTokens", maxTokens);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Gemini API response: {}", responseBody);

            return extractTextFromResponse(responseBody);
        }
    }

    /**
     * Extract text from Gemini API response
     */
    private String extractTextFromResponse(String response) {
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        try {
            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response", e);
            throw new RuntimeException("Invalid Gemini API response format");
        }
    }

    /**
     * Parse script response from Gemini
     */
    private Script parseScriptResponse(String jsonText, String topic, int duration, String aspectRatio, String style) {
        // Clean up response (remove markdown code blocks if present)
        jsonText = jsonText.trim()
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();

        JsonObject jsonObject = gson.fromJson(jsonText, JsonObject.class);

        Script script = Script.builder()
                .scriptId(Script.generateId())
                .title(jsonObject.get("title").getAsString())
                .topic(topic)
                .totalDuration(jsonObject.get("total_duration").getAsInt())
                .numScenes(jsonObject.get("num_scenes").getAsInt())
                .aspectRatio(aspectRatio)
                .style(style)
                .status("completed")
                .createdAt(System.currentTimeMillis())
                .build();

        List<Scene> scenes = new ArrayList<>();
        jsonObject.getAsJsonArray("scenes").forEach(sceneElement -> {
            JsonObject sceneObj = sceneElement.getAsJsonObject();

            Scene scene = Scene.builder()
                    .sceneNumber(sceneObj.get("scene_number").getAsInt())
                    .duration(sceneObj.get("duration").getAsInt())
                    .description(sceneObj.get("description").getAsString())
                    .veoPrompt(sceneObj.get("veo_prompt").getAsString())
                    .cameraMovement(sceneObj.get("camera_movement").getAsString())
                    .timeOfDay(sceneObj.get("time_of_day").getAsString())
                    .mood(sceneObj.get("mood").getAsString())
                    .status("pending")
                    .progress(0)
                    .build();

            scenes.add(scene);
        });

        script.setScenes(scenes);
        return script;
    }
}
