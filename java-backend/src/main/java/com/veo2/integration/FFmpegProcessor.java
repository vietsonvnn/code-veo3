package com.veo2.integration;

import com.veo2.model.Scene;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FFmpegProcessor {

    private static final Logger log = LoggerFactory.getLogger(FFmpegProcessor.class);

    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    public FFmpegProcessor() {
        try {
            // Try to find FFmpeg in system PATH
            this.ffmpeg = new FFmpeg("ffmpeg");
            this.ffprobe = new FFprobe("ffprobe");
            log.info("FFmpeg initialized successfully");
        } catch (IOException e) {
            log.error("FFmpeg not found in system PATH. Please install FFmpeg.");
            log.error("macOS: brew install ffmpeg");
            log.error("Ubuntu: sudo apt-get install ffmpeg");
            log.error("Windows: Download from https://ffmpeg.org/download.html");
        }
    }

    /**
     * Assemble multiple video files into one
     */
    public String assembleVideos(List<String> videoFiles, String outputPath, boolean addTransitions, double transitionDuration) throws IOException {
        log.info("Assembling {} videos into {}", videoFiles.size(), outputPath);

        if (videoFiles.isEmpty()) {
            throw new IllegalArgumentException("No video files to assemble");
        }

        // Create parent directories
        Files.createDirectories(Paths.get(outputPath).getParent());

        if (addTransitions && videoFiles.size() > 1) {
            return assembleWithTransitions(videoFiles, outputPath, transitionDuration);
        } else {
            return assembleSimple(videoFiles, outputPath);
        }
    }

    /**
     * Simple concatenation without transitions
     */
    private String assembleSimple(List<String> videoFiles, String outputPath) throws IOException {
        // Create concat file
        String concatFilePath = outputPath.replace(".mp4", "_concat.txt");
        List<String> concatLines = new ArrayList<>();

        for (String videoFile : videoFiles) {
            concatLines.add("file '" + new File(videoFile).getAbsolutePath() + "'");
        }

        Files.write(Paths.get(concatFilePath), concatLines);

        try {
            // Build FFmpeg command
            FFmpegBuilder builder = new FFmpegBuilder()
                    .addExtraArgs("-f", "concat")
                    .addExtraArgs("-safe", "0")
                    .setInput(concatFilePath)
                    .addOutput(outputPath)
                    .setVideoCodec("libx264")
                    .setAudioCodec("aac")
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            log.info("Videos assembled successfully: {}", outputPath);
            return outputPath;

        } finally {
            // Clean up concat file
            Files.deleteIfExists(Paths.get(concatFilePath));
        }
    }

    /**
     * Concatenation with crossfade transitions
     */
    private String assembleWithTransitions(List<String> videoFiles, String outputPath, double transitionDuration) throws IOException {
        log.info("Assembling videos with {}s crossfade transitions", transitionDuration);

        // For complex transitions, we need to use filter_complex
        // This is a simplified version - full implementation would be more complex
        StringBuilder filterComplex = new StringBuilder();

        for (int i = 0; i < videoFiles.size() - 1; i++) {
            if (i == 0) {
                filterComplex.append(String.format(
                        "[0:v][1:v]xfade=transition=fade:duration=%.1f:offset=7.5[v01];",
                        transitionDuration
                ));
            } else {
                filterComplex.append(String.format(
                        "[v%02d][%d:v]xfade=transition=fade:duration=%.1f:offset=%.1f[v%02d];",
                        i - 1, i + 1, transitionDuration, (i + 1) * 8 - transitionDuration / 2, i
                ));
            }
        }

        // Remove trailing semicolon
        if (filterComplex.length() > 0) {
            filterComplex.setLength(filterComplex.length() - 1);
        }

        // For now, fall back to simple concatenation
        // Full transition support requires more complex FFmpeg command building
        log.warn("Crossfade transitions not fully implemented, using simple concatenation");
        return assembleSimple(videoFiles, outputPath);
    }

    /**
     * Get video duration in seconds
     */
    public double getVideoDuration(String videoPath) throws IOException {
        FFmpegProbeResult probe = ffprobe.probe(videoPath);
        return probe.getFormat().duration;
    }

    /**
     * Get video resolution
     */
    public String getVideoResolution(String videoPath) throws IOException {
        FFmpegProbeResult probe = ffprobe.probe(videoPath);
        int width = probe.getStreams().get(0).width;
        int height = probe.getStreams().get(0).height;
        return width + "x" + height;
    }

    /**
     * Extract thumbnail from video
     */
    public String extractThumbnail(String videoPath, String thumbnailPath, double timeInSeconds) throws IOException {
        log.info("Extracting thumbnail from {} at {}s", videoPath, timeInSeconds);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(videoPath)
                .addExtraArgs("-ss", String.valueOf(timeInSeconds))
                .addOutput(thumbnailPath)
                .setFrames(1)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        log.info("Thumbnail extracted: {}", thumbnailPath);
        return thumbnailPath;
    }

    /**
     * Convert video to different format/quality
     */
    public String convertVideo(String inputPath, String outputPath, String codec, int crf) throws IOException {
        log.info("Converting video {} to {}", inputPath, outputPath);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputPath)
                .addOutput(outputPath)
                .setVideoCodec(codec)
                .addExtraArgs("-crf", String.valueOf(crf))
                .setAudioCodec("aac")
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();

        log.info("Video converted successfully: {}", outputPath);
        return outputPath;
    }

    /**
     * Validate video file
     */
    public boolean isValidVideo(String videoPath) {
        try {
            FFmpegProbeResult probe = ffprobe.probe(videoPath);
            return probe.getStreams().size() > 0;
        } catch (IOException e) {
            log.error("Invalid video file: {}", videoPath);
            return false;
        }
    }

    /**
     * Get video file size
     */
    public long getVideoFileSize(String videoPath) {
        File file = new File(videoPath);
        return file.exists() ? file.length() : 0;
    }
}
