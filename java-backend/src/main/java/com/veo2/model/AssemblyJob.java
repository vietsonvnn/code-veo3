package com.veo2.model;

import java.util.List;
import java.util.UUID;

public class AssemblyJob {
    private String assemblyJobId;
    private String scriptId;
    private List<Integer> sceneNumbers;
    private boolean addTransitions;
    private double transitionDuration;
    private String outputFormat;
    private int fps;
    private String status; // processing, completed, failed
    private int progress; // 0-100
    private long estimatedTime; // seconds
    private String outputFilePath;
    private String outputUrl;
    private long fileSize;
    private int totalDuration;
    private String errorMessage;

    // Constructors
    public AssemblyJob() {}

    public AssemblyJob(String assemblyJobId, String scriptId, List<Integer> sceneNumbers,
                      boolean addTransitions, double transitionDuration, String outputFormat,
                      int fps, String status, int progress, long estimatedTime,
                      String outputFilePath, String outputUrl, long fileSize,
                      int totalDuration, String errorMessage) {
        this.assemblyJobId = assemblyJobId;
        this.scriptId = scriptId;
        this.sceneNumbers = sceneNumbers;
        this.addTransitions = addTransitions;
        this.transitionDuration = transitionDuration;
        this.outputFormat = outputFormat;
        this.fps = fps;
        this.status = status;
        this.progress = progress;
        this.estimatedTime = estimatedTime;
        this.outputFilePath = outputFilePath;
        this.outputUrl = outputUrl;
        this.fileSize = fileSize;
        this.totalDuration = totalDuration;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public String getAssemblyJobId() { return assemblyJobId; }
    public void setAssemblyJobId(String assemblyJobId) { this.assemblyJobId = assemblyJobId; }

    public String getScriptId() { return scriptId; }
    public void setScriptId(String scriptId) { this.scriptId = scriptId; }

    public List<Integer> getSceneNumbers() { return sceneNumbers; }
    public void setSceneNumbers(List<Integer> sceneNumbers) { this.sceneNumbers = sceneNumbers; }

    public boolean isAddTransitions() { return addTransitions; }
    public void setAddTransitions(boolean addTransitions) { this.addTransitions = addTransitions; }

    public double getTransitionDuration() { return transitionDuration; }
    public void setTransitionDuration(double transitionDuration) { this.transitionDuration = transitionDuration; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public int getFps() { return fps; }
    public void setFps(int fps) { this.fps = fps; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public long getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(long estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }

    public String getOutputUrl() { return outputUrl; }
    public void setOutputUrl(String outputUrl) { this.outputUrl = outputUrl; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getTotalDuration() { return totalDuration; }
    public void setTotalDuration(int totalDuration) { this.totalDuration = totalDuration; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Static method
    public static String generateAssemblyJobId() {
        return "assembly-" + UUID.randomUUID().toString();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String assemblyJobId;
        private String scriptId;
        private List<Integer> sceneNumbers;
        private boolean addTransitions;
        private double transitionDuration;
        private String outputFormat;
        private int fps;
        private String status;
        private int progress;
        private long estimatedTime;
        private String outputFilePath;
        private String outputUrl;
        private long fileSize;
        private int totalDuration;
        private String errorMessage;

        public Builder assemblyJobId(String assemblyJobId) { this.assemblyJobId = assemblyJobId; return this; }
        public Builder scriptId(String scriptId) { this.scriptId = scriptId; return this; }
        public Builder sceneNumbers(List<Integer> sceneNumbers) { this.sceneNumbers = sceneNumbers; return this; }
        public Builder addTransitions(boolean addTransitions) { this.addTransitions = addTransitions; return this; }
        public Builder transitionDuration(double transitionDuration) { this.transitionDuration = transitionDuration; return this; }
        public Builder outputFormat(String outputFormat) { this.outputFormat = outputFormat; return this; }
        public Builder fps(int fps) { this.fps = fps; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder progress(int progress) { this.progress = progress; return this; }
        public Builder estimatedTime(long estimatedTime) { this.estimatedTime = estimatedTime; return this; }
        public Builder outputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; return this; }
        public Builder outputUrl(String outputUrl) { this.outputUrl = outputUrl; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder totalDuration(int totalDuration) { this.totalDuration = totalDuration; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public AssemblyJob build() {
            return new AssemblyJob(assemblyJobId, scriptId, sceneNumbers, addTransitions,
                                  transitionDuration, outputFormat, fps, status, progress,
                                  estimatedTime, outputFilePath, outputUrl, fileSize,
                                  totalDuration, errorMessage);
        }
    }
}
