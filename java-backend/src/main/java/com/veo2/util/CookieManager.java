package com.veo2.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class CookieManager {

    private static final Logger log = LoggerFactory.getLogger(CookieManager.class);

    private final Gson gson = new Gson();

    /**
     * Load cookies from JSON file
     */
    public List<Map<String, Object>> loadCookiesFromFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> cookies = gson.fromJson(reader, listType);
            log.info("Loaded {} cookies from {}", cookies.size(), filePath);
            return cookies;
        } catch (IOException e) {
            log.error("Failed to load cookies from {}: {}", filePath, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Convert JSON cookies to Selenium Cookie objects
     */
    public Set<Cookie> convertToCookies(List<Map<String, Object>> jsonCookies) {
        Set<Cookie> cookies = new HashSet<>();

        for (Map<String, Object> cookieData : jsonCookies) {
            try {
                Cookie.Builder builder = new Cookie.Builder(
                    (String) cookieData.get("name"),
                    (String) cookieData.get("value")
                );

                if (cookieData.containsKey("domain")) {
                    builder.domain((String) cookieData.get("domain"));
                }

                if (cookieData.containsKey("path")) {
                    builder.path((String) cookieData.get("path"));
                }

                if (cookieData.containsKey("secure")) {
                    builder.isSecure((Boolean) cookieData.get("secure"));
                }

                if (cookieData.containsKey("httpOnly")) {
                    builder.isHttpOnly((Boolean) cookieData.get("httpOnly"));
                }

                if (cookieData.containsKey("expirationDate")) {
                    Double expiration = (Double) cookieData.get("expirationDate");
                    Date expiryDate = new Date(expiration.longValue() * 1000);
                    // Selenium Cookie.Builder expects java.util.Date
                    builder.expiresOn(expiryDate);
                }

                // Set sameSite attribute (default to Lax)
                String sameSite = (String) cookieData.getOrDefault("sameSite", "Lax");
                builder.sameSite(sameSite);

                cookies.add(builder.build());
            } catch (Exception e) {
                log.error("Failed to convert cookie: {}", cookieData.get("name"), e);
            }
        }

        log.info("Converted {} cookies to Selenium format", cookies.size());
        return cookies;
    }

    /**
     * Save cookies to JSON file
     */
    public void saveCookiesToFile(Set<Cookie> cookies, String filePath) {
        List<Map<String, Object>> cookieList = new ArrayList<>();

        for (Cookie cookie : cookies) {
            Map<String, Object> cookieData = new HashMap<>();
            cookieData.put("name", cookie.getName());
            cookieData.put("value", cookie.getValue());
            cookieData.put("domain", cookie.getDomain());
            cookieData.put("path", cookie.getPath());
            cookieData.put("secure", cookie.isSecure());
            cookieData.put("httpOnly", cookie.isHttpOnly());

            if (cookie.getExpiry() != null) {
                cookieData.put("expirationDate", cookie.getExpiry().getTime() / 1000);
            }

            cookieData.put("sameSite", cookie.getSameSite());
            cookieList.add(cookieData);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(cookieList, writer);
            log.info("Saved {} cookies to {}", cookieList.size(), filePath);
        } catch (IOException e) {
            log.error("Failed to save cookies to {}: {}", filePath, e.getMessage());
        }
    }

    /**
     * Check if cookies are still valid (not expired)
     */
    public boolean areCookiesValid(List<Map<String, Object>> cookies) {
        long currentTime = System.currentTimeMillis() / 1000;

        for (Map<String, Object> cookie : cookies) {
            if (cookie.containsKey("expirationDate")) {
                Double expiration = (Double) cookie.get("expirationDate");
                if (expiration < currentTime) {
                    log.warn("Cookie {} has expired", cookie.get("name"));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get cookies expiry date
     */
    public LocalDateTime getCookiesExpiryDate(List<Map<String, Object>> cookies) {
        long minExpiry = Long.MAX_VALUE;

        for (Map<String, Object> cookie : cookies) {
            if (cookie.containsKey("expirationDate")) {
                Double expiration = (Double) cookie.get("expirationDate");
                minExpiry = Math.min(minExpiry, expiration.longValue());
            }
        }

        if (minExpiry == Long.MAX_VALUE) {
            return null;
        }

        return LocalDateTime.ofInstant(
            new Date(minExpiry * 1000).toInstant(),
            ZoneId.systemDefault()
        );
    }
}
