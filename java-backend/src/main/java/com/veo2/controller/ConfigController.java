package com.veo2.controller;

import com.veo2.model.Config;
import com.veo2.util.CookieManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private CookieManager cookieManager;

    @Value("${veo2.gemini.api-key}")
    private String geminiApiKey;

    @Value("${veo2.flow.url}")
    private String flowUrl;

    @Value("${veo2.flow.project-id}")
    private String flowProjectId;

    @Value("${veo2.video.default-duration}")
    private int defaultDuration;

    @Value("${veo2.video.max-scenes}")
    private int maxScenes;

    @Value("#{'${veo2.supported-ratios}'.split(',')}")
    private List<String> supportedRatios;

    @Value("${veo2.storage.cookies-file}")
    private String cookiesFile;

    @Value("${veo2.browser.headless}")
    private boolean headless;

    @Value("${veo2.flow.timeout}")
    private int timeout;

    @Value("${veo2.storage.base-dir}")
    private String baseDir;

    /**
     * Get configuration
     * GET /api/config
     */
    @GetMapping
    public ResponseEntity<?> getConfig() {
        try {
            // Check if cookies file exists
            File cookiesFileObj = new File(cookiesFile);
            boolean cookiesValid = false;
            LocalDateTime cookiesExpiryDate = null;

            if (cookiesFileObj.exists()) {
                List<Map<String, Object>> cookies = cookieManager.loadCookiesFromFile(cookiesFile);
                cookiesValid = cookieManager.areCookiesValid(cookies);
                cookiesExpiryDate = cookieManager.getCookiesExpiryDate(cookies);
            }

            Config config = Config.builder()
                    .geminiApiKey(geminiApiKey)
                    .flowUrl(flowUrl)
                    .flowProjectId(flowProjectId)
                    .cookiesValid(cookiesValid)
                    .cookiesExpiryDate(cookiesExpiryDate)
                    .defaultDuration(defaultDuration)
                    .sceneDuration(8)
                    .maxScenes(maxScenes)
                    .supportedRatios(supportedRatios)
                    .timeout(timeout)
                    .headless(headless)
                    .downloadDir(baseDir + "/videos")
                    .cookiesFile(cookiesFile)
                    .build();

            return ResponseEntity.ok(config);

        } catch (Exception e) {
            log.error("Failed to get config: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get config",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Upload cookies file
     * POST /api/config/cookies
     */
    @PostMapping("/cookies")
    public ResponseEntity<?> uploadCookies(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No file uploaded"
                ));
            }

            // Save cookies file
            File cookiesFileObj = new File(cookiesFile);
            cookiesFileObj.getParentFile().mkdirs();
            file.transferTo(cookiesFileObj);

            // Validate cookies
            List<Map<String, Object>> cookies = cookieManager.loadCookiesFromFile(cookiesFile);
            boolean valid = cookieManager.areCookiesValid(cookies);
            LocalDateTime expiryDate = cookieManager.getCookiesExpiryDate(cookies);

            log.info("Cookies uploaded: {} cookies, valid={}", cookies.size(), valid);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cookies uploaded successfully",
                    "cookiesLoaded", cookies.size(),
                    "cookiesValid", valid,
                    "expiryDate", expiryDate != null ? expiryDate.toString() : "unknown"
            ));

        } catch (Exception e) {
            log.error("Failed to upload cookies: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to upload cookies",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Update API key
     * POST /api/config/api-key
     */
    @PostMapping("/api-key")
    public ResponseEntity<?> updateApiKey(@RequestBody Map<String, String> request) {
        try {
            String newApiKey = request.get("apiKey");

            if (newApiKey == null || newApiKey.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "API key cannot be empty"
                ));
            }

            // Update the API key (in memory for current session)
            this.geminiApiKey = newApiKey;

            log.info("API key updated successfully");

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "API key updated successfully"
            ));

        } catch (Exception e) {
            log.error("Failed to update API key: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to update API key",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Test cookies
     * GET /api/config/test-cookies
     */
    @GetMapping("/test-cookies")
    public ResponseEntity<?> testCookies() {
        try {
            File cookiesFileObj = new File(cookiesFile);

            if (!cookiesFileObj.exists()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Cookies file not found"
                ));
            }

            List<Map<String, Object>> cookies = cookieManager.loadCookiesFromFile(cookiesFile);
            boolean valid = cookieManager.areCookiesValid(cookies);

            return ResponseEntity.ok(Map.of(
                    "cookiesValid", valid,
                    "cookiesCount", cookies.size(),
                    "expiryDate", cookieManager.getCookiesExpiryDate(cookies)
            ));

        } catch (Exception e) {
            log.error("Failed to test cookies: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to test cookies",
                    "message", e.getMessage()
            ));
        }
    }
}
