package com.veo2.controller;

import com.veo2.model.Script;
import com.veo2.service.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/script")
@CrossOrigin(origins = "*")
public class ScriptController {

    private static final Logger log = LoggerFactory.getLogger(ScriptController.class);

    @Autowired
    private ScriptService scriptService;

    /**
     * Generate script
     * POST /api/script/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateScript(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            Integer duration = (Integer) request.getOrDefault("duration", 60);
            String aspectRatio = (String) request.getOrDefault("aspectRatio", "16:9");
            String style = (String) request.getOrDefault("style", "cinematic");

            log.info("Generating script: topic={}, duration={}", topic, duration);

            Script script = scriptService.generateScript(topic, duration, aspectRatio, style);

            return ResponseEntity.ok(script);

        } catch (Exception e) {
            log.error("Failed to generate script: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to generate script",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get script by ID
     * GET /api/script/{scriptId}
     */
    @GetMapping("/{scriptId}")
    public ResponseEntity<?> getScript(@PathVariable String scriptId) {
        try {
            Script script = scriptService.getScript(scriptId);

            if (script == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(script);

        } catch (Exception e) {
            log.error("Failed to get script: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get script",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Update script
     * PUT /api/script/{scriptId}
     */
    @PutMapping("/{scriptId}")
    public ResponseEntity<?> updateScript(@PathVariable String scriptId, @RequestBody Script script) {
        try {
            script.setScriptId(scriptId);
            Script updatedScript = scriptService.updateScript(script);

            return ResponseEntity.ok(updatedScript);

        } catch (Exception e) {
            log.error("Failed to update script: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to update script",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete script
     * DELETE /api/script/{scriptId}
     */
    @DeleteMapping("/{scriptId}")
    public ResponseEntity<?> deleteScript(@PathVariable String scriptId) {
        try {
            boolean deleted = scriptService.deleteScript(scriptId);

            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Script deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Failed to delete script: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to delete script",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get all scripts
     * GET /api/script/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAllScripts() {
        try {
            String[] scriptIds = scriptService.getAllScriptIds();

            Map<String, Object> response = new HashMap<>();
            response.put("scriptIds", scriptIds);
            response.put("count", scriptIds.length);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get scripts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get scripts",
                    "message", e.getMessage()
            ));
        }
    }
}
