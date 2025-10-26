package com.veo2.model;

public class Scene {
    private int sceneNumber;
    private int duration;
    private String description;
    private String veoPrompt;
    private String cameraMovement;
    private String timeOfDay;
    private String mood;
    private String status; // pending, generating, downloading, completed, failed
    private String videoUrl;
    private String thumbnailUrl;
    private String videoFilePath;
    private long fileSize;
    private String resolution;
    private int progress; // 0-100

    // Constructors
    public Scene() {}

    public Scene(int sceneNumber, int duration, String description, String veoPrompt,
                 String cameraMovement, String timeOfDay, String mood, String status,
                 String videoUrl, String thumbnailUrl, String videoFilePath,
                 long fileSize, String resolution, int progress) {
        this.sceneNumber = sceneNumber;
        this.duration = duration;
        this.description = description;
        this.veoPrompt = veoPrompt;
        this.cameraMovement = cameraMovement;
        this.timeOfDay = timeOfDay;
        this.mood = mood;
        this.status = status;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.videoFilePath = videoFilePath;
        this.fileSize = fileSize;
        this.resolution = resolution;
        this.progress = progress;
    }

    // Getters and Setters
    public int getSceneNumber() { return sceneNumber; }
    public void setSceneNumber(int sceneNumber) { this.sceneNumber = sceneNumber; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVeoPrompt() { return veoPrompt; }
    public void setVeoPrompt(String veoPrompt) { this.veoPrompt = veoPrompt; }

    public String getCameraMovement() { return cameraMovement; }
    public void setCameraMovement(String cameraMovement) { this.cameraMovement = cameraMovement; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getVideoFilePath() { return videoFilePath; }
    public void setVideoFilePath(String videoFilePath) { this.videoFilePath = videoFilePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int sceneNumber;
        private int duration;
        private String description;
        private String veoPrompt;
        private String cameraMovement;
        private String timeOfDay;
        private String mood;
        private String status;
        private String videoUrl;
        private String thumbnailUrl;
        private String videoFilePath;
        private long fileSize;
        private String resolution;
        private int progress;

        public Builder sceneNumber(int sceneNumber) { this.sceneNumber = sceneNumber; return this; }
        public Builder duration(int duration) { this.duration = duration; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder veoPrompt(String veoPrompt) { this.veoPrompt = veoPrompt; return this; }
        public Builder cameraMovement(String cameraMovement) { this.cameraMovement = cameraMovement; return this; }
        public Builder timeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; return this; }
        public Builder mood(String mood) { this.mood = mood; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder videoUrl(String videoUrl) { this.videoUrl = videoUrl; return this; }
        public Builder thumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }
        public Builder videoFilePath(String videoFilePath) { this.videoFilePath = videoFilePath; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }
        public Builder resolution(String resolution) { this.resolution = resolution; return this; }
        public Builder progress(int progress) { this.progress = progress; return this; }

        public Scene build() {
            return new Scene(sceneNumber, duration, description, veoPrompt, cameraMovement,
                           timeOfDay, mood, status, videoUrl, thumbnailUrl, videoFilePath,
                           fileSize, resolution, progress);
        }
    }
}
