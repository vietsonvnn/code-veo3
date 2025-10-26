package com.veo2.service;

import com.veo2.integration.VeoApiClient;
import com.veo2.model.Scene;
import com.veo2.model.Script;
import com.veo2.model.VideoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for direct VEO API video generation
 * Alternative to browser automation (FlowAutomation)
 */
@Service
public class VeoDirectService {

    private static final Logger log = LoggerFactory.getLogger(VeoDirectService.class);

    @Autowired
    private VeoApiClient veoApiClient;

    @Autowired
    private ScriptService scriptService;

    @Value("${veo2.storage.videos-dir}")
    private String videosDir;

    private final Map<String, VideoJob> jobCache = new ConcurrentHashMap<>();
    private final Map<String, VeoApiClient.VeoVideoResponse> operationCache = new ConcurrentHashMap<>();

    /**
     * Generate video using direct VEO API
     * @param scriptId Script ID
     * @param sceneNumber Scene number to generate
     * @return Video job with operation ID
     */
    public VideoJob generateVideoDirectAPI(String scriptId, int sceneNumber) {
        log.info("Starting direct VEO API video generation: script={}, scene={}", scriptId, sceneNumber);

        // Get script and scene
        Script script = scriptService.getScript(scriptId);
        if (script == null) {
            throw new RuntimeException("Script not found: " + scriptId);
        }

        Scene scene = script.getScenes().stream()
                .filter(s -> s.getSceneNumber() == sceneNumber)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Scene not found: " + sceneNumber));

        // Create video job
        VideoJob job = VideoJob.builder()
                .jobId(VideoJob.generateJobId())
                .scriptId(scriptId)
                .sceneNumbers(List.of(sceneNumber))
                .status("pending")
                .totalScenes(1)
                .completedScenes(0)
                .startTime(System.currentTimeMillis())
                .progress(0)
                .build();

        jobCache.put(job.getJobId(), job);

        // Start async video generation
        CompletableFuture.runAsync(() -> {
            try {
                job.setStatus("generating");
                scene.setStatus("generating");

                // Call VEO API
                log.info("Calling VEO API for scene {}: {}", sceneNumber, scene.getVeoPrompt());

                VeoApiClient.VeoVideoResponse response = veoApiClient.generateVideo(
                        scene.getVeoPrompt(),
                        scene.getDuration(),
                        script.getAspectRatio()
                );

                // Cache operation for status checking
                operationCache.put(response.getOperationId(), response);

                // Update job with operation ID
                job.setCurrentOperationId(response.getOperationId());
                job.setStatus("processing");
                job.setProgress(25);

                log.info("VEO API video generation started: operationId={}", response.getOperationId());

                // Poll for completion
                pollVideoStatus(job, scene, response.getOperationId());

            } catch (Exception e) {
                log.error("Failed to generate video via VEO API", e);
                job.setStatus("failed");
                job.setErrorMessage(e.getMessage());
                scene.setStatus("failed");
            }
        });

        return job;
    }

    /**
     * Poll VEO API for video completion
     */
    private void pollVideoStatus(VideoJob job, Scene scene, String operationId) {
        int maxAttempts = 60; // 5 minutes (60 * 5s)
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                Thread.sleep(5000); // Wait 5 seconds

                VeoApiClient.VeoStatusResponse status = veoApiClient.checkStatus(operationId);

                // Update progress
                job.setProgress(Math.max(job.getProgress(), status.getProgress()));
                scene.setProgress(status.getProgress());

                log.info("VEO status check {}/{}: operationId={}, status={}, progress={}%",
                         attempt + 1, maxAttempts, operationId, status.getStatus(), status.getProgress());

                if ("COMPLETED".equalsIgnoreCase(status.getStatus())) {
                    // Video is ready!
                    log.info("VEO video completed");

                    // Download video
                    String fileName = String.format("scene_%d_%s.mp4", scene.getSceneNumber(), job.getJobId());
                    String outputPath = videosDir + File.separator + fileName;

                    new File(videosDir).mkdirs();

                    // Check if video is base64 or URL
                    if (status.getVideoBase64() != null && !status.getVideoBase64().isEmpty()) {
                        log.info("Downloading video from base64 data");
                        veoApiClient.downloadVideo(status.getVideoBase64(), true, outputPath);
                    } else if (status.getVideoUrl() != null && !status.getVideoUrl().isEmpty()) {
                        log.info("Downloading video from URL: {}", status.getVideoUrl());
                        veoApiClient.downloadVideo(status.getVideoUrl(), false, outputPath);
                    } else {
                        throw new IOException("No video data or URL in response");
                    }

                    // Update scene and job
                    scene.setStatus("completed");
                    scene.setVideoUrl(status.getVideoUrl());
                    scene.setVideoFilePath(outputPath);
                    scene.setProgress(100);

                    job.setStatus("completed");
                    job.setProgress(100);
                    job.setCompletedScenes(1);
                    job.setEndTime(System.currentTimeMillis());

                    log.info("Video downloaded successfully: {}", outputPath);
                    return;

                } else if ("FAILED".equalsIgnoreCase(status.getStatus())) {
                    // Video generation failed
                    log.error("VEO video generation failed: {}", status.getErrorMessage());

                    job.setStatus("failed");
                    job.setErrorMessage(status.getErrorMessage());
                    scene.setStatus("failed");
                    return;
                }

                attempt++;

            } catch (InterruptedException e) {
                log.error("Status polling interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                log.error("Failed to check VEO status", e);
                attempt++;
            }
        }

        // Timeout
        if (attempt >= maxAttempts) {
            log.error("VEO video generation timeout after {} attempts", maxAttempts);
            job.setStatus("timeout");
            job.setErrorMessage("Video generation timeout after 5 minutes");
            scene.setStatus("timeout");
        }
    }

    /**
     * Get video generation job status
     */
    public VideoJob getJob(String jobId) {
        return jobCache.get(jobId);
    }

    /**
     * Get all jobs
     */
    public Map<String, VideoJob> getAllJobs() {
        return new ConcurrentHashMap<>(jobCache);
    }

}
