package com.veo2.integration;

import com.veo2.model.Scene;
import com.veo2.util.CookieManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class FlowAutomation {

    private static final Logger log = LoggerFactory.getLogger(FlowAutomation.class);

    @Value("${veo2.flow.url}")
    private String flowUrl;

    @Value("${veo2.flow.timeout}")
    private int timeout;

    @Value("${veo2.browser.headless}")
    private boolean headless;

    @Value("${veo2.storage.cookies-file}")
    private String cookiesFile;

    @Autowired
    private CookieManager cookieManager;

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * Initialize browser and load cookies
     */
    public void initializeBrowser() {
        log.info("Initializing Chrome browser...");

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        // Set download preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", System.getProperty("user.dir") + "/data/videos");
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeout / 1000));

        log.info("Browser initialized successfully");
    }

    /**
     * Load cookies and navigate to Flow
     */
    public boolean loadCookiesAndNavigate() {
        try {
            log.info("Loading cookies from {}", cookiesFile);

            // Load cookies from file
            List<Map<String, Object>> jsonCookies = cookieManager.loadCookiesFromFile(cookiesFile);

            if (jsonCookies.isEmpty()) {
                log.error("No cookies found in file");
                return false;
            }

            // Check if cookies are valid
            if (!cookieManager.areCookiesValid(jsonCookies)) {
                log.error("Cookies have expired");
                return false;
            }

            // Navigate to Flow domain first
            driver.get(flowUrl);
            Thread.sleep(2000);

            // Add cookies to browser
            Set<Cookie> cookies = cookieManager.convertToCookies(jsonCookies);
            for (Cookie cookie : cookies) {
                try {
                    driver.manage().addCookie(cookie);
                } catch (Exception e) {
                    log.warn("Failed to add cookie: {}", cookie.getName());
                }
            }

            // Navigate to Flow again with cookies
            driver.get(flowUrl);
            Thread.sleep(3000);

            // Check if authentication was successful
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("accounts.google.com")) {
                log.error("Authentication failed - redirected to login page");
                return false;
            }

            log.info("Successfully authenticated with Flow");
            return true;

        } catch (Exception e) {
            log.error("Failed to load cookies and navigate: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate video for a scene
     */
    public boolean generateVideo(Scene scene) {
        try {
            log.info("Generating video for scene {}", scene.getSceneNumber());

            // Find and fill prompt textarea
            WebElement textarea = findPromptTextarea();
            if (textarea == null) {
                log.error("Could not find prompt textarea");
                return false;
            }

            textarea.clear();
            textarea.sendKeys(scene.getVeoPrompt());
            Thread.sleep(1000);

            // Click generate button
            WebElement generateButton = findGenerateButton();
            if (generateButton == null) {
                log.error("Could not find generate button");
                return false;
            }

            generateButton.click();
            log.info("Clicked generate button for scene {}", scene.getSceneNumber());

            // Wait for video generation to complete
            return waitForVideoGeneration();

        } catch (Exception e) {
            log.error("Failed to generate video for scene {}: {}", scene.getSceneNumber(), e.getMessage());
            return false;
        }
    }

    /**
     * Find prompt textarea using multiple selectors
     */
    private WebElement findPromptTextarea() {
        String[] selectors = {
                "textarea[node='72']",
                "textarea[placeholder*='Tạo một video']",
                "textarea[placeholder*='Create a video']",
                "textarea"
        };

        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    log.debug("Found textarea with selector: {}", selector);
                    return elements.get(0);
                }
            } catch (Exception e) {
                log.debug("Selector failed: {}", selector);
            }
        }

        return null;
    }

    /**
     * Find generate button using multiple selectors
     */
    private WebElement findGenerateButton() {
        String[] selectors = {
                "button:has-text('Tạo')",
                "button:has-text('arrow_forward')",
                "button[type='submit']",
                "button[aria-label*='Generate']"
        };

        for (String selector : selectors) {
            try {
                WebElement element = driver.findElement(By.cssSelector(selector));
                if (element.isDisplayed() && element.isEnabled()) {
                    log.debug("Found generate button with selector: {}", selector);
                    return element;
                }
            } catch (Exception e) {
                log.debug("Selector failed: {}", selector);
            }
        }

        return null;
    }

    /**
     * Wait for video generation to complete
     */
    private boolean waitForVideoGeneration() {
        log.info("Waiting for video generation (max {} seconds)...", timeout / 1000);

        try {
            // Wait for video element to appear
            WebElement videoElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.tagName("video"))
            );

            if (videoElement != null) {
                log.info("Video generation completed successfully");
                Thread.sleep(2000); // Wait for video to fully load
                return true;
            }

        } catch (TimeoutException e) {
            log.error("Video generation timed out after {} seconds", timeout / 1000);
        } catch (Exception e) {
            log.error("Error waiting for video: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Download video from UI
     */
    public String downloadVideo(Scene scene, String downloadPath) {
        try {
            log.info("Downloading video for scene {}", scene.getSceneNumber());

            // Find video element
            WebElement videoElement = driver.findElement(By.tagName("video"));
            String videoUrl = videoElement.getAttribute("src");

            if (videoUrl == null || videoUrl.isEmpty()) {
                log.error("Could not get video URL");
                return null;
            }

            log.info("Found video URL: {}", videoUrl);

            // Click download button (three-dot menu)
            clickDownloadButton();

            // Wait for download to complete
            Thread.sleep(5000);

            log.info("Video downloaded successfully for scene {}", scene.getSceneNumber());
            return videoUrl;

        } catch (Exception e) {
            log.error("Failed to download video for scene {}: {}", scene.getSceneNumber(), e.getMessage());
            return null;
        }
    }

    /**
     * Click download button
     */
    private void clickDownloadButton() {
        try {
            // Find three-dot menu button
            WebElement menuButton = driver.findElement(
                    By.cssSelector("button:has-text('more_vert')")
            );
            menuButton.click();
            Thread.sleep(1000);

            // Find download option
            WebElement downloadOption = driver.findElement(
                    By.cssSelector("div[role='menuitem']:has-text('Tải xuống')")
            );
            downloadOption.click();
            Thread.sleep(1000);

            // Select 1080p quality
            WebElement quality1080p = driver.findElement(
                    By.cssSelector("div:has-text('1080p')")
            );
            quality1080p.click();

        } catch (Exception e) {
            log.warn("Could not click download button: {}", e.getMessage());
        }
    }

    /**
     * Take screenshot for debugging
     */
    public void takeScreenshot(String filename) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);

            java.nio.file.Files.write(
                    java.nio.file.Paths.get("./data/logs/" + filename),
                    screenshotBytes
            );

            log.info("Screenshot saved: {}", filename);
        } catch (Exception e) {
            log.error("Failed to take screenshot: {}", e.getMessage());
        }
    }

    /**
     * Close browser
     */
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            log.info("Browser closed");
        }
    }

    /**
     * Get current page source for debugging
     */
    public String getPageSource() {
        return driver != null ? driver.getPageSource() : "";
    }
}
