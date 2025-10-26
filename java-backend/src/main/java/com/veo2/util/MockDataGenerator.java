package com.veo2.util;

import com.veo2.model.Scene;
import com.veo2.model.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MockDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(MockDataGenerator.class);

    /**
     * Generate mock script for testing
     */
    public Script generateMockScript(String topic, int duration, String aspectRatio, String style) {
        log.info("Generating mock script for testing: {}", topic);

        int numScenes = duration / 8;
        List<Scene> scenes = new ArrayList<>();

        for (int i = 1; i <= numScenes; i++) {
            Scene scene = Scene.builder()
                    .sceneNumber(i)
                    .duration(8)
                    .description("Cảnh " + i + ": " + getSceneDescription(i, topic))
                    .veoPrompt(getVeoPrompt(i, topic, style))
                    .cameraMovement(getCameraMovement(i))
                    .timeOfDay(getTimeOfDay(i))
                    .mood(getMood(i))
                    .status("pending")
                    .progress(0)
                    .build();

            scenes.add(scene);
        }

        return Script.builder()
                .scriptId(Script.generateId())
                .title("[MOCK] " + topic)
                .topic(topic)
                .totalDuration(duration)
                .numScenes(numScenes)
                .aspectRatio(aspectRatio)
                .style(style)
                .status("completed")
                .scenes(scenes)
                .createdAt(System.currentTimeMillis())
                .build();
    }

    private String getSceneDescription(int sceneNumber, String topic) {
        String[] templates = {
                "Mở đầu với cảnh tổng quan về " + topic,
                "Phóng to chi tiết của " + topic + " đang hoạt động",
                "Góc nhìn từ xa cho thấy toàn cảnh " + topic,
                "Cảnh chuyển động chậm tập trung vào " + topic,
                "Ánh sáng chiếu rọi lên " + topic + " tạo hiệu ứng đẹp",
                "Kết thúc với cảnh tổng hợp về " + topic,
                "Cảnh cuối cùng với thông điệp về " + topic
        };

        return templates[Math.min(sceneNumber - 1, templates.length - 1)];
    }

    private String getVeoPrompt(int sceneNumber, String topic, String style) {
        return String.format(
                "A %s shot of %s, scene %d, professional %s style, high quality, " +
                        "cinematic lighting, detailed textures, 4k resolution, smooth motion, " +
                        "vibrant colors, atmospheric depth, dramatic composition",
                getCameraMovement(sceneNumber).toLowerCase(),
                topic.toLowerCase(),
                sceneNumber,
                style
        );
    }

    private String getCameraMovement(int sceneNumber) {
        String[] movements = {
                "Slow pan left",
                "Zoom in",
                "Static wide shot",
                "Tracking shot",
                "Slow pan right",
                "Zoom out",
                "Aerial view"
        };

        return movements[(sceneNumber - 1) % movements.length];
    }

    private String getTimeOfDay(int sceneNumber) {
        String[] times = {
                "golden hour",
                "midday",
                "sunset",
                "night",
                "dawn",
                "afternoon",
                "twilight"
        };

        return times[(sceneNumber - 1) % times.length];
    }

    private String getMood(int sceneNumber) {
        String[] moods = {
                "inspiring",
                "peaceful",
                "energetic",
                "mysterious",
                "dramatic",
                "cheerful",
                "epic"
        };

        return moods[(sceneNumber - 1) % moods.length];
    }
}
