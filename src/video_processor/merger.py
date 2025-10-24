"""
Video Processor - Merge scenes into final video
"""

import os
from typing import List, Optional
from moviepy.editor import VideoFileClip, concatenate_videoclips, CompositeVideoClip
import logging


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class VideoMerger:
    def __init__(self, output_dir: str = "./data/videos"):
        """
        Initialize Video Merger

        Args:
            output_dir: Output directory for merged videos
        """
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)

    def merge_scenes(
        self,
        video_paths: List[str],
        output_filename: str,
        add_transitions: bool = False,
        transition_duration: float = 0.5
    ) -> Optional[str]:
        """
        Merge multiple video scenes into one

        Args:
            video_paths: List of video file paths (in order)
            output_filename: Output filename
            add_transitions: Add crossfade transitions between scenes
            transition_duration: Transition duration in seconds

        Returns:
            Path to merged video or None if failed
        """
        try:
            logger.info(f"🎬 Merging {len(video_paths)} videos...")

            # Load all video clips
            clips = []
            for i, path in enumerate(video_paths, 1):
                if not os.path.exists(path):
                    logger.error(f"❌ Video not found: {path}")
                    continue

                logger.info(f"📂 Loading scene {i}: {os.path.basename(path)}")
                clip = VideoFileClip(path)
                clips.append(clip)

            if not clips:
                logger.error("❌ No valid video clips found")
                return None

            # Merge clips
            if add_transitions:
                logger.info(f"✨ Adding {transition_duration}s crossfade transitions")
                final_clip = self._merge_with_transitions(clips, transition_duration)
            else:
                logger.info("🔗 Concatenating clips")
                final_clip = concatenate_videoclips(clips, method="compose")

            # Export
            output_path = os.path.join(self.output_dir, output_filename)
            logger.info(f"💾 Exporting to: {output_path}")

            final_clip.write_videofile(
                output_path,
                codec='libx264',
                audio_codec='aac',
                fps=30,
                preset='medium',
                threads=4,
                logger=None  # Suppress moviepy logs
            )

            # Clean up
            for clip in clips:
                clip.close()
            final_clip.close()

            logger.info(f"✅ Video merged successfully: {output_path}")
            return output_path

        except Exception as e:
            logger.error(f"❌ Error merging videos: {str(e)}")
            return None

    def _merge_with_transitions(
        self,
        clips: List[VideoFileClip],
        transition_duration: float
    ) -> VideoFileClip:
        """
        Merge clips with crossfade transitions

        Args:
            clips: List of video clips
            transition_duration: Transition duration in seconds

        Returns:
            Merged video clip with transitions
        """
        if len(clips) == 1:
            return clips[0]

        # Apply crossfade between consecutive clips
        result = [clips[0]]

        for i in range(1, len(clips)):
            # Crossfade: overlap previous and current clip
            result.append(
                clips[i].crossfadein(transition_duration)
            )

        return concatenate_videoclips(result, method="compose")

    def validate_videos(self, video_paths: List[str]) -> List[str]:
        """
        Validate and filter existing video files

        Args:
            video_paths: List of video paths

        Returns:
            List of valid video paths
        """
        valid_paths = []

        for path in video_paths:
            if not os.path.exists(path):
                logger.warning(f"⚠️  Video not found: {path}")
                continue

            try:
                # Try to open video to check if valid
                clip = VideoFileClip(path)
                duration = clip.duration
                clip.close()

                if duration > 0:
                    valid_paths.append(path)
                    logger.info(f"✅ Valid: {os.path.basename(path)} ({duration:.1f}s)")
                else:
                    logger.warning(f"⚠️  Invalid duration: {path}")

            except Exception as e:
                logger.warning(f"⚠️  Cannot read video: {path} - {str(e)}")

        logger.info(f"📊 Valid videos: {len(valid_paths)}/{len(video_paths)}")
        return valid_paths

    def get_video_info(self, video_path: str) -> dict:
        """Get video metadata"""
        try:
            clip = VideoFileClip(video_path)
            info = {
                'duration': clip.duration,
                'fps': clip.fps,
                'size': clip.size,
                'width': clip.w,
                'height': clip.h
            }
            clip.close()
            return info
        except Exception as e:
            logger.error(f"❌ Error getting video info: {str(e)}")
            return {}


# Example usage
if __name__ == "__main__":
    merger = VideoMerger()

    # Test with sample videos
    video_paths = [
        "./data/videos/scene_001.mp4",
        "./data/videos/scene_002.mp4",
        "./data/videos/scene_003.mp4"
    ]

    # Validate videos
    valid_videos = merger.validate_videos(video_paths)

    if valid_videos:
        # Merge
        output = merger.merge_scenes(
            valid_videos,
            "final_video.mp4",
            add_transitions=True
        )

        if output:
            print(f"✅ Success: {output}")
