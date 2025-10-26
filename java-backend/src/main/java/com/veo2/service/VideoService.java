package com.veo2.service;

import com.veo2.integration.FFmpegProcessor;
import com.veo2.integration.FlowAutomation;
import com.veo2.model.*;
import com.veo2.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private FlowAutomation flowAutomation;

    @Autowired
    private FFmpegProcessor ffmpegProcessor;

    @Autowired
    private FileUtils fileUtils;

    @Value("${veo2.storage.videos-dir}")
    private String videosDir;

    @Value("${server.port}")
    private int serverPort;

    private final Map<String, VideoJob> jobCache = new ConcurrentHashMap<>();
    private final Map<String, AssemblyJob> assemblyJobCache = new ConcurrentHashMap<>();

    /**
     * Start video generation for all scenes in a script
     */
    public VideoJob generateVideos(String scriptId, List<Integer> sceneNumbers, String quality) {
        Script script = scriptService.getScript(scriptId);
        if (script == null) {
            throw new RuntimeException("Script not found: " + scriptId);
        }

        // Create video job
        VideoJob job = VideoJob.builder()
                .jobId(VideoJob.generateJobId())
                .scriptId(scriptId)
                .sceneNumbers(sceneNumbers != null ? sceneNumbers : getAllSceneNumbers(script))
                .quality(quality)
                .status("processing")
                .progress(0)
                .completedScenes(0)
                .totalScenes(sceneNumbers != null ? sceneNumbers.size() : script.getNumScenes())
                .estimatedTime(calculateEstimatedTime(sceneNumbers != null ? sceneNumbers.size() : script.getNumScenes()))
                .startTime(System.currentTimeMillis())
                .build();

        jobCache.put(job.getJobId(), job);

        // Start generation in background
        CompletableFuture.runAsync(() -> processVideoGeneration(job, script));

        return job;
    }

    /**
     * Process video generation for all scenes
     */
    private void processVideoGeneration(VideoJob job, Script script) {
        try {
            log.info("Starting video generation for job: {}", job.getJobId());

            // Initialize browser
            flowAutomation.initializeBrowser();

            if (!flowAutomation.loadCookiesAndNavigate()) {
                job.setStatus("failed");
                job.setErrorMessage("Failed to authenticate with Flow");
                return;
            }

            // Generate videos for each scene
            for (Integer sceneNumber : job.getSceneNumbers()) {
                Scene scene = script.getScenes().get(sceneNumber - 1);
                scene.setStatus("generating");
                job.setCurrentScene(scene);

                log.info("Generating video for scene {}", sceneNumber);

                // Generate video
                boolean success = flowAutomation.generateVideo(scene);

                if (!success) {
                    scene.setStatus("failed");
                    log.error("Failed to generate video for scene {}", sceneNumber);
                    continue;
                }

                // Download video
                String videoDir = String.format("%s/%s", videosDir, script.getScriptId());
                fileUtils.createDirectory(videoDir);

                String videoFileName = fileUtils.getSceneFilename(sceneNumber);
                String videoPath = String.format("%s/%s", videoDir, videoFileName);

                String videoUrl = flowAutomation.downloadVideo(scene, videoPath);

                if (videoUrl != null) {
                    scene.setStatus("completed");
                    scene.setProgress(100);
                    scene.setVideoFilePath(videoPath);
                    scene.setVideoUrl(String.format("http://localhost:%d/api/video/file/%s/%s",
                            serverPort, script.getScriptId(), videoFileName));

                    // Extract thumbnail
                    try {
                        String thumbnailFileName = fileUtils.getThumbnailFilename(sceneNumber);
                        String thumbnailPath = String.format("%s/%s", videoDir, thumbnailFileName);
                        ffmpegProcessor.extractThumbnail(videoPath, thumbnailPath, 2.0);

                        scene.setThumbnailUrl(String.format("http://localhost:%d/api/video/thumbnail/%s/%s",
                                serverPort, script.getScriptId(), thumbnailFileName));
                    } catch (Exception e) {
                        log.warn("Failed to extract thumbnail for scene {}", sceneNumber);
                    }

                    // Get video info
                    try {
                        scene.setFileSize(ffmpegProcessor.getVideoFileSize(videoPath));
                        scene.setResolution(ffmpegProcessor.getVideoResolution(videoPath));
                    } catch (Exception e) {
                        log.warn("Failed to get video info for scene {}", sceneNumber);
                    }

                    job.setCompletedScenes(job.getCompletedScenes() + 1);
                    job.setProgress((job.getCompletedScenes() * 100) / job.getTotalScenes());

                    log.info("Scene {} completed ({}/{})", sceneNumber,
                            job.getCompletedScenes(), job.getTotalScenes());
                } else {
                    scene.setStatus("failed");
                    log.error("Failed to download video for scene {}", sceneNumber);
                }

                // Update script
                scriptService.updateScript(script);
            }

            // Complete job
            job.setStatus("completed");
            job.setProgress(100);
            job.setEndTime(System.currentTimeMillis());

            log.info("Video generation completed for job: {}", job.getJobId());

        } catch (Exception e) {
            log.error("Video generation failed for job {}: {}", job.getJobId(), e.getMessage());
            job.setStatus("failed");
            job.setErrorMessage(e.getMessage());
        } finally {
            flowAutomation.closeBrowser();
        }
    }

    /**
     * Get video job status
     */
    public VideoJob getJobStatus(String jobId) {
        return jobCache.get(jobId);
    }

    /**
     * Assemble videos into final output
     */
    public AssemblyJob assembleVideos(String scriptId, List<Integer> sceneNumbers,
                                      boolean addTransitions, double transitionDuration,
                                      String outputFormat, int fps) {
        Script script = scriptService.getScript(scriptId);
        if (script == null) {
            throw new RuntimeException("Script not found: " + scriptId);
        }

        // Create assembly job
        AssemblyJob job = AssemblyJob.builder()
                .assemblyJobId(AssemblyJob.generateAssemblyJobId())
                .scriptId(scriptId)
                .sceneNumbers(sceneNumbers != null ? sceneNumbers : getAllSceneNumbers(script))
                .addTransitions(addTransitions)
                .transitionDuration(transitionDuration)
                .outputFormat(outputFormat)
                .fps(fps)
                .status("processing")
                .progress(0)
                .estimatedTime(120)
                .build();

        assemblyJobCache.put(job.getAssemblyJobId(), job);

        // Start assembly in background
        CompletableFuture.runAsync(() -> processVideoAssembly(job, script));

        return job;
    }

    /**
     * Process video assembly
     */
    private void processVideoAssembly(AssemblyJob job, Script script) {
        try {
            log.info("Starting video assembly for job: {}", job.getAssemblyJobId());

            // Collect video files
            List<String> videoFiles = new ArrayList<>();
            String videoDir = String.format("%s/%s", videosDir, script.getScriptId());

            for (Integer sceneNumber : job.getSceneNumbers()) {
                String videoFileName = fileUtils.getSceneFilename(sceneNumber);
                String videoPath = String.format("%s/%s", videoDir, videoFileName);

                if (fileUtils.fileExists(videoPath)) {
                    videoFiles.add(videoPath);
                } else {
                    log.warn("Video file not found for scene {}: {}", sceneNumber, videoPath);
                }
            }

            if (videoFiles.isEmpty()) {
                job.setStatus("failed");
                job.setErrorMessage("No video files found to assemble");
                return;
            }

            // Assemble videos
            String outputFileName = String.format("final_video_%s.mp4", System.currentTimeMillis());
            String outputPath = String.format("%s/%s", videoDir, outputFileName);

            job.setProgress(50);

            String assembledVideo = ffmpegProcessor.assembleVideos(
                    videoFiles, outputPath, job.isAddTransitions(), job.getTransitionDuration()
            );

            // Get video info
            job.setOutputFilePath(assembledVideo);
            job.setOutputUrl(String.format("http://localhost:%d/api/video/file/%s/%s",
                    serverPort, script.getScriptId(), outputFileName));
            job.setFileSize(ffmpegProcessor.getVideoFileSize(assembledVideo));
            job.setTotalDuration((int) ffmpegProcessor.getVideoDuration(assembledVideo));

            job.setStatus("completed");
            job.setProgress(100);

            log.info("Video assembly completed: {}", assembledVideo);

        } catch (Exception e) {
            log.error("Video assembly failed for job {}: {}", job.getAssemblyJobId(), e.getMessage());
            job.setStatus("failed");
            job.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Get assembly job status
     */
    public AssemblyJob getAssemblyJobStatus(String assemblyJobId) {
        return assemblyJobCache.get(assemblyJobId);
    }

    /**
     * Get all scene numbers from script
     */
    private List<Integer> getAllSceneNumbers(Script script) {
        List<Integer> sceneNumbers = new ArrayList<>();
        for (int i = 1; i <= script.getNumScenes(); i++) {
            sceneNumbers.add(i);
        }
        return sceneNumbers;
    }

    /**
     * Calculate estimated time (seconds)
     */
    private long calculateEstimatedTime(int numScenes) {
        return numScenes * 300L; // 5 minutes per scene
    }

    /**
     * Get video file path
     */
    public String getVideoFilePath(String scriptId, String filename) {
        return String.format("%s/%s/%s", videosDir, scriptId, filename);
    }
}
