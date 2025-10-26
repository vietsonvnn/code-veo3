package com.veo2.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Direct VEO API Client
 * Calls Google VEO API directly without browser automation
 */
@Component
public class VeoApiClient {

    private static final Logger log = LoggerFactory.getLogger(VeoApiClient.class);

    @Value("${veo2.gemini.api-key}")
    private String apiKey;

    // Google AI Sandbox VEO API endpoint (used by Flow)
    private static final String VEO_API_BASE = "https://aisandbox-pa.googleapis.com/v1";
    private static final String VEO_MODEL = "veo-3.1-generate-preview";

    private final OkHttpClient httpClient;
    private final Gson gson;

    public VeoApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Generate video using VEO API (Google Veo 3.1)
     * @param prompt Video generation prompt
     * @param duration Duration in seconds
     * @param aspectRatio Aspect ratio (16:9, 9:16, 1:1)
     * @return Video generation response
     */
    public VeoVideoResponse generateVideo(String prompt, int duration, String aspectRatio) throws IOException {
        log.info("Generating video with VEO 3.1 API: prompt='{}', duration={}, ratio={}",
                 prompt.substring(0, Math.min(50, prompt.length())), duration, aspectRatio);

        // Build request body according to Google Veo 3.1 API format
        JsonObject instance = new JsonObject();
        instance.addProperty("prompt", prompt);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("aspectRatio", aspectRatio);
        parameters.addProperty("resolution", "720p");
        parameters.addProperty("durationSeconds", duration);

        JsonObject requestBody = new JsonObject();
        requestBody.add("instances", gson.toJsonTree(new JsonObject[]{instance}));
        requestBody.add("parameters", parameters);

        // Create request with aisandbox endpoint (same format as generativelanguage)
        String url = VEO_API_BASE + "/models/" + VEO_MODEL + ":predictLongRunning?key=" + apiKey;

        log.info("Calling aisandbox VEO API: {}", url.replace(apiKey, "***"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json")
                ))
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("VEO API error: HTTP {}, body: {}", response.code(), responseBody);
                throw new IOException("VEO API error: HTTP " + response.code() + " - " + responseBody);
            }

            log.info("VEO API success: {}", responseBody.substring(0, Math.min(200, responseBody.length())));

            // Parse response - operation name is returned
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

            VeoVideoResponse videoResponse = new VeoVideoResponse();
            if (responseJson.has("name")) {
                videoResponse.setOperationId(responseJson.get("name").getAsString());
                videoResponse.setStatus("PENDING");
            }

            return videoResponse;
        }
    }

    /**
     * Check video generation status
     * @param operationName Operation name from generateVideo (includes full path)
     * @return Status response
     */
    public VeoStatusResponse checkStatus(String operationName) throws IOException {
        log.info("Checking VEO video status: {}", operationName);

        // Build URL - operation name already includes the full path
        // For aisandbox, might need different format, so handle both cases
        String url;
        if (operationName.startsWith("operations/")) {
            url = VEO_API_BASE + "/" + operationName + "?key=" + apiKey;
        } else {
            url = VEO_API_BASE + "/operations/" + operationName + "?key=" + apiKey;
        }

        log.info("Polling aisandbox status: {}", url.replace(apiKey, "***"));

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("VEO status check error: HTTP {}, body: {}", response.code(), responseBody);
                throw new IOException("VEO status error: HTTP " + response.code());
            }

            // Parse response
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

            VeoStatusResponse statusResponse = new VeoStatusResponse();
            statusResponse.setOperationId(operationName);

            // Check if operation is done
            boolean done = responseJson.has("done") && responseJson.get("done").getAsBoolean();

            if (done) {
                // Check for error
                if (responseJson.has("error")) {
                    statusResponse.setStatus("FAILED");
                    JsonObject error = responseJson.getAsJsonObject("error");
                    statusResponse.setErrorMessage(error.has("message") ? error.get("message").getAsString() : "Unknown error");
                    statusResponse.setProgress(0);
                } else if (responseJson.has("response")) {
                    // Video is ready
                    statusResponse.setStatus("COMPLETED");
                    statusResponse.setProgress(100);

                    JsonObject responseData = responseJson.getAsJsonObject("response");
                    if (responseData.has("predictions")) {
                        JsonObject predictions = responseData.getAsJsonArray("predictions").get(0).getAsJsonObject();

                        // Check for base64 encoded video
                        if (predictions.has("bytesBase64Encoded")) {
                            statusResponse.setVideoBase64(predictions.get("bytesBase64Encoded").getAsString());
                        }

                        // Check for video URI
                        if (predictions.has("uri")) {
                            statusResponse.setVideoUrl(predictions.get("uri").getAsString());
                        }
                    }
                }
            } else {
                // Still processing
                statusResponse.setStatus("PROCESSING");
                statusResponse.setProgress(50); // Estimate progress
            }

            log.info("VEO status: {}, progress: {}%", statusResponse.getStatus(), statusResponse.getProgress());
            return statusResponse;
        }
    }

    /**
     * Download video from base64 or URL
     * @param videoData Base64 string or URL
     * @param isBase64 True if videoData is base64 encoded
     * @param outputPath Local file path to save
     */
    public void downloadVideo(String videoData, boolean isBase64, String outputPath) throws IOException {
        log.info("Downloading video to: {}", outputPath);

        byte[] videoBytes;

        if (isBase64) {
            // Decode base64
            log.info("Decoding base64 video data");
            videoBytes = java.util.Base64.getDecoder().decode(videoData);
        } else {
            // Download from URL
            log.info("Downloading video from URL: {}", videoData);

            Request request = new Request.Builder()
                    .url(videoData)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download video: HTTP " + response.code());
                }

                videoBytes = response.body().bytes();
            }
        }

        // Save to file
        java.nio.file.Files.write(
            java.nio.file.Paths.get(outputPath),
            videoBytes
        );

        log.info("Video saved successfully: {} bytes", videoBytes.length);
    }

    // Response classes
    public static class VeoVideoResponse {
        private String operationId;
        private String status;
        private String videoUrl;
        private String thumbnailUrl;
        private Object metadata;

        public String getOperationId() { return operationId; }
        public void setOperationId(String operationId) { this.operationId = operationId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

        public Object getMetadata() { return metadata; }
        public void setMetadata(Object metadata) { this.metadata = metadata; }
    }

    public static class VeoStatusResponse {
        private String operationId;
        private String status; // PENDING, PROCESSING, COMPLETED, FAILED
        private String videoUrl;
        private String videoBase64; // Base64 encoded video data
        private String thumbnailUrl;
        private int progress; // 0-100
        private String errorMessage;

        public String getOperationId() { return operationId; }
        public void setOperationId(String operationId) { this.operationId = operationId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getVideoBase64() { return videoBase64; }
        public void setVideoBase64(String videoBase64) { this.videoBase64 = videoBase64; }

        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
