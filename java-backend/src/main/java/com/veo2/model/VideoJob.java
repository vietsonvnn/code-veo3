package com.veo2.model;

import java.util.List;
import java.util.UUID;

public class VideoJob {
    private String jobId;
    private String scriptId;
    private String status; // processing, completed, failed, cancelled
    private int progress; // 0-100
    private int completedScenes;
    private int totalScenes;
    private List<Integer> sceneNumbers;
    private String quality;
    private Scene currentScene;
    private String currentOperationId; // VEO API operation ID
    private long estimatedTime; // seconds
    private long startTime;
    private long endTime;
    private String errorMessage;

    // Constructors
    public VideoJob() {}

    public VideoJob(String jobId, String scriptId, String status, int progress,
                   int completedScenes, int totalScenes, List<Integer> sceneNumbers,
                   String quality, Scene currentScene, long estimatedTime,
                   long startTime, long endTime, String errorMessage) {
        this.jobId = jobId;
        this.scriptId = scriptId;
        this.status = status;
        this.progress = progress;
        this.completedScenes = completedScenes;
        this.totalScenes = totalScenes;
        this.sceneNumbers = sceneNumbers;
        this.quality = quality;
        this.currentScene = currentScene;
        this.estimatedTime = estimatedTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getScriptId() { return scriptId; }
    public void setScriptId(String scriptId) { this.scriptId = scriptId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getCompletedScenes() { return completedScenes; }
    public void setCompletedScenes(int completedScenes) { this.completedScenes = completedScenes; }

    public int getTotalScenes() { return totalScenes; }
    public void setTotalScenes(int totalScenes) { this.totalScenes = totalScenes; }

    public List<Integer> getSceneNumbers() { return sceneNumbers; }
    public void setSceneNumbers(List<Integer> sceneNumbers) { this.sceneNumbers = sceneNumbers; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public Scene getCurrentScene() { return currentScene; }
    public void setCurrentScene(Scene currentScene) { this.currentScene = currentScene; }

    public String getCurrentOperationId() { return currentOperationId; }
    public void setCurrentOperationId(String currentOperationId) { this.currentOperationId = currentOperationId; }

    public long getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(long estimatedTime) { this.estimatedTime = estimatedTime; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Static method
    public static String generateJobId() {
        return "job-" + UUID.randomUUID().toString();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jobId;
        private String scriptId;
        private String status;
        private int progress;
        private int completedScenes;
        private int totalScenes;
        private List<Integer> sceneNumbers;
        private String quality;
        private Scene currentScene;
        private long estimatedTime;
        private long startTime;
        private long endTime;
        private String errorMessage;

        public Builder jobId(String jobId) { this.jobId = jobId; return this; }
        public Builder scriptId(String scriptId) { this.scriptId = scriptId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder progress(int progress) { this.progress = progress; return this; }
        public Builder completedScenes(int completedScenes) { this.completedScenes = completedScenes; return this; }
        public Builder totalScenes(int totalScenes) { this.totalScenes = totalScenes; return this; }
        public Builder sceneNumbers(List<Integer> sceneNumbers) { this.sceneNumbers = sceneNumbers; return this; }
        public Builder quality(String quality) { this.quality = quality; return this; }
        public Builder currentScene(Scene currentScene) { this.currentScene = currentScene; return this; }
        public Builder estimatedTime(long estimatedTime) { this.estimatedTime = estimatedTime; return this; }
        public Builder startTime(long startTime) { this.startTime = startTime; return this; }
        public Builder endTime(long endTime) { this.endTime = endTime; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public VideoJob build() {
            return new VideoJob(jobId, scriptId, status, progress, completedScenes,
                              totalScenes, sceneNumbers, quality, currentScene,
                              estimatedTime, startTime, endTime, errorMessage);
        }
    }
}
