package com.veo2.controller;

import com.veo2.model.AssemblyJob;
import com.veo2.model.Script;
import com.veo2.model.VideoJob;
import com.veo2.service.ScriptService;
import com.veo2.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private ScriptService scriptService;

    /**
     * Generate videos for script
     * POST /api/video/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateVideos(@RequestBody Map<String, Object> request) {
        try {
            String scriptId = (String) request.get("scriptId");
            @SuppressWarnings("unchecked")
            List<Integer> sceneNumbers = (List<Integer>) request.get("sceneNumbers");
            String quality = (String) request.getOrDefault("quality", "1080p");

            log.info("Generating videos: scriptId={}, scenes={}", scriptId, sceneNumbers);

            VideoJob job = videoService.generateVideos(scriptId, sceneNumbers, quality);

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to generate videos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to generate videos",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get video generation job status
     * GET /api/video/status/{jobId}
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        try {
            VideoJob job = videoService.getJobStatus(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to get job status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get job status",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get all videos for a script
     * GET /api/video/list/{scriptId}
     */
    @GetMapping("/list/{scriptId}")
    public ResponseEntity<?> listVideos(@PathVariable String scriptId) {
        try {
            Script script = scriptService.getScript(scriptId);

            if (script == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("scriptId", scriptId);
            response.put("scenes", script.getScenes());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to list videos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to list videos",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Serve video file
     * GET /api/video/file/{scriptId}/{filename}
     */
    @GetMapping("/file/{scriptId}/{filename}")
    public ResponseEntity<Resource> getVideoFile(@PathVariable String scriptId, @PathVariable String filename) {
        try {
            String filePath = videoService.getVideoFilePath(scriptId, filename);
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to get video file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Serve thumbnail file
     * GET /api/video/thumbnail/{scriptId}/{filename}
     */
    @GetMapping("/thumbnail/{scriptId}/{filename}")
    public ResponseEntity<Resource> getThumbnailFile(@PathVariable String scriptId, @PathVariable String filename) {
        try {
            String filePath = videoService.getVideoFilePath(scriptId, filename);
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to get thumbnail file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Download video file
     * GET /api/video/download/{scriptId}/{filename}
     */
    @GetMapping("/download/{scriptId}/{filename}")
    public ResponseEntity<Resource> downloadVideoFile(@PathVariable String scriptId, @PathVariable String filename) {
        try {
            String filePath = videoService.getVideoFilePath(scriptId, filename);
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download video file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assemble videos
     * POST /api/video/assemble
     */
    @PostMapping("/assemble")
    public ResponseEntity<?> assembleVideos(@RequestBody Map<String, Object> request) {
        try {
            String scriptId = (String) request.get("scriptId");
            @SuppressWarnings("unchecked")
            List<Integer> sceneNumbers = (List<Integer>) request.get("sceneNumbers");
            Boolean addTransitions = (Boolean) request.getOrDefault("addTransitions", false);
            Double transitionDuration = ((Number) request.getOrDefault("transitionDuration", 0.5)).doubleValue();
            String outputFormat = (String) request.getOrDefault("outputFormat", "mp4");
            Integer fps = (Integer) request.getOrDefault("fps", 30);

            log.info("Assembling videos: scriptId={}", scriptId);

            AssemblyJob job = videoService.assembleVideos(
                    scriptId, sceneNumbers, addTransitions, transitionDuration, outputFormat, fps
            );

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to assemble videos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to assemble videos",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get assembly job status
     * GET /api/video/assembly/status/{assemblyJobId}
     */
    @GetMapping("/assembly/status/{assemblyJobId}")
    public ResponseEntity<?> getAssemblyJobStatus(@PathVariable String assemblyJobId) {
        try {
            AssemblyJob job = videoService.getAssemblyJobStatus(assemblyJobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to get assembly job status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get assembly job status",
                    "message", e.getMessage()
            ));
        }
    }
}
