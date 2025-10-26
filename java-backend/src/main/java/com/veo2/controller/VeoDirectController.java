package com.veo2.controller;

import com.veo2.model.VideoJob;
import com.veo2.service.VeoDirectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for direct VEO API video generation
 * Alternative to browser automation
 */
@RestController
@RequestMapping("/api/veo")
@CrossOrigin(origins = "*")
public class VeoDirectController {

    private static final Logger log = LoggerFactory.getLogger(VeoDirectController.class);

    @Autowired
    private VeoDirectService veoDirectService;

    /**
     * Generate video using direct VEO API
     * POST /api/veo/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateVideo(@RequestBody Map<String, Object> request) {
        try {
            String scriptId = (String) request.get("scriptId");
            Integer sceneNumber = (Integer) request.get("sceneNumber");

            if (scriptId == null || sceneNumber == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Missing required fields",
                        "required", "scriptId, sceneNumber"
                ));
            }

            log.info("Starting VEO API video generation: script={}, scene={}", scriptId, sceneNumber);

            VideoJob job = veoDirectService.generateVideoDirectAPI(scriptId, sceneNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "jobId", job.getJobId(),
                    "status", job.getStatus(),
                    "message", "VEO API video generation started"
            ));

        } catch (Exception e) {
            log.error("Failed to start VEO video generation", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to generate video",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get VEO video generation job status
     * GET /api/veo/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        try {
            VideoJob job = veoDirectService.getJob(jobId);

            if (job == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            log.error("Failed to get job status", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to get status",
                    "message", e.getMessage()
            ));
        }
    }


    /**
     * List all VEO jobs
     * GET /api/veo/jobs
     */
    @GetMapping("/jobs")
    public ResponseEntity<?> listJobs() {
        try {
            Map<String, VideoJob> jobs = veoDirectService.getAllJobs();

            return ResponseEntity.ok(Map.of(
                    "jobs", jobs,
                    "count", jobs.size()
            ));

        } catch (Exception e) {
            log.error("Failed to list jobs", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to list jobs",
                    "message", e.getMessage()
            ));
        }
    }
}
