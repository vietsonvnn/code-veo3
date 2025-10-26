package com.veo2.model;

import java.util.List;
import java.util.UUID;

public class Script {
    private String scriptId;
    private String title;
    private String topic;
    private int totalDuration;
    private int numScenes;
    private String aspectRatio;
    private String style;
    private String status; // pending, generating, completed, failed
    private List<Scene> scenes;
    private long createdAt;

    // Constructors
    public Script() {}

    public Script(String scriptId, String title, String topic, int totalDuration,
                 int numScenes, String aspectRatio, String style, String status,
                 List<Scene> scenes, long createdAt) {
        this.scriptId = scriptId;
        this.title = title;
        this.topic = topic;
        this.totalDuration = totalDuration;
        this.numScenes = numScenes;
        this.aspectRatio = aspectRatio;
        this.style = style;
        this.status = status;
        this.scenes = scenes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getScriptId() { return scriptId; }
    public void setScriptId(String scriptId) { this.scriptId = scriptId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getTotalDuration() { return totalDuration; }
    public void setTotalDuration(int totalDuration) { this.totalDuration = totalDuration; }

    public int getNumScenes() { return numScenes; }
    public void setNumScenes(int numScenes) { this.numScenes = numScenes; }

    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Scene> getScenes() { return scenes; }
    public void setScenes(List<Scene> scenes) { this.scenes = scenes; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Static method
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String scriptId;
        private String title;
        private String topic;
        private int totalDuration;
        private int numScenes;
        private String aspectRatio;
        private String style;
        private String status;
        private List<Scene> scenes;
        private long createdAt;

        public Builder scriptId(String scriptId) { this.scriptId = scriptId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder totalDuration(int totalDuration) { this.totalDuration = totalDuration; return this; }
        public Builder numScenes(int numScenes) { this.numScenes = numScenes; return this; }
        public Builder aspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; return this; }
        public Builder style(String style) { this.style = style; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder scenes(List<Scene> scenes) { this.scenes = scenes; return this; }
        public Builder createdAt(long createdAt) { this.createdAt = createdAt; return this; }

        public Script build() {
            return new Script(scriptId, title, topic, totalDuration, numScenes,
                            aspectRatio, style, status, scenes, createdAt);
        }
    }
}
