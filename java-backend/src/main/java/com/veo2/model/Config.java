package com.veo2.model;

import java.time.LocalDateTime;
import java.util.List;

public class Config {
    private String geminiApiKey;
    private String flowUrl;
    private String flowProjectId;
    private boolean cookiesValid;
    private LocalDateTime cookiesExpiryDate;
    private int defaultDuration;
    private int sceneDuration;
    private int maxScenes;
    private List<String> supportedRatios;
    private int timeout;
    private boolean headless;
    private String downloadDir;
    private String cookiesFile;

    // Constructors
    public Config() {}

    public Config(String geminiApiKey, String flowUrl, String flowProjectId, boolean cookiesValid,
                  LocalDateTime cookiesExpiryDate, int defaultDuration, int sceneDuration, int maxScenes,
                  List<String> supportedRatios, int timeout, boolean headless, String downloadDir,
                  String cookiesFile) {
        this.geminiApiKey = geminiApiKey;
        this.flowUrl = flowUrl;
        this.flowProjectId = flowProjectId;
        this.cookiesValid = cookiesValid;
        this.cookiesExpiryDate = cookiesExpiryDate;
        this.defaultDuration = defaultDuration;
        this.sceneDuration = sceneDuration;
        this.maxScenes = maxScenes;
        this.supportedRatios = supportedRatios;
        this.timeout = timeout;
        this.headless = headless;
        this.downloadDir = downloadDir;
        this.cookiesFile = cookiesFile;
    }

    // Getters and Setters
    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

    public String getFlowUrl() { return flowUrl; }
    public void setFlowUrl(String flowUrl) { this.flowUrl = flowUrl; }

    public String getFlowProjectId() { return flowProjectId; }
    public void setFlowProjectId(String flowProjectId) { this.flowProjectId = flowProjectId; }

    public boolean isCookiesValid() { return cookiesValid; }
    public void setCookiesValid(boolean cookiesValid) { this.cookiesValid = cookiesValid; }

    public LocalDateTime getCookiesExpiryDate() { return cookiesExpiryDate; }
    public void setCookiesExpiryDate(LocalDateTime cookiesExpiryDate) { this.cookiesExpiryDate = cookiesExpiryDate; }

    public int getDefaultDuration() { return defaultDuration; }
    public void setDefaultDuration(int defaultDuration) { this.defaultDuration = defaultDuration; }

    public int getSceneDuration() { return sceneDuration; }
    public void setSceneDuration(int sceneDuration) { this.sceneDuration = sceneDuration; }

    public int getMaxScenes() { return maxScenes; }
    public void setMaxScenes(int maxScenes) { this.maxScenes = maxScenes; }

    public List<String> getSupportedRatios() { return supportedRatios; }
    public void setSupportedRatios(List<String> supportedRatios) { this.supportedRatios = supportedRatios; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public boolean isHeadless() { return headless; }
    public void setHeadless(boolean headless) { this.headless = headless; }

    public String getDownloadDir() { return downloadDir; }
    public void setDownloadDir(String downloadDir) { this.downloadDir = downloadDir; }

    public String getCookiesFile() { return cookiesFile; }
    public void setCookiesFile(String cookiesFile) { this.cookiesFile = cookiesFile; }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String geminiApiKey;
        private String flowUrl;
        private String flowProjectId;
        private boolean cookiesValid;
        private LocalDateTime cookiesExpiryDate;
        private int defaultDuration;
        private int sceneDuration;
        private int maxScenes;
        private List<String> supportedRatios;
        private int timeout;
        private boolean headless;
        private String downloadDir;
        private String cookiesFile;

        public Builder geminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; return this; }
        public Builder flowUrl(String flowUrl) { this.flowUrl = flowUrl; return this; }
        public Builder flowProjectId(String flowProjectId) { this.flowProjectId = flowProjectId; return this; }
        public Builder cookiesValid(boolean cookiesValid) { this.cookiesValid = cookiesValid; return this; }
        public Builder cookiesExpiryDate(LocalDateTime cookiesExpiryDate) { this.cookiesExpiryDate = cookiesExpiryDate; return this; }
        public Builder defaultDuration(int defaultDuration) { this.defaultDuration = defaultDuration; return this; }
        public Builder sceneDuration(int sceneDuration) { this.sceneDuration = sceneDuration; return this; }
        public Builder maxScenes(int maxScenes) { this.maxScenes = maxScenes; return this; }
        public Builder supportedRatios(List<String> supportedRatios) { this.supportedRatios = supportedRatios; return this; }
        public Builder timeout(int timeout) { this.timeout = timeout; return this; }
        public Builder headless(boolean headless) { this.headless = headless; return this; }
        public Builder downloadDir(String downloadDir) { this.downloadDir = downloadDir; return this; }
        public Builder cookiesFile(String cookiesFile) { this.cookiesFile = cookiesFile; return this; }

        public Config build() {
            return new Config(geminiApiKey, flowUrl, flowProjectId, cookiesValid, cookiesExpiryDate,
                            defaultDuration, sceneDuration, maxScenes, supportedRatios, timeout,
                            headless, downloadDir, cookiesFile);
        }
    }
}
