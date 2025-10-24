"""
VEO 3.1 Video Automation - Main Entry Point
Tự động hóa toàn bộ quy trình tạo video từ chủ đề đến video hoàn chỉnh
"""

import asyncio
import argparse
import os
import json
from datetime import datetime
from dotenv import load_dotenv
import logging

from src.script_generator import ScriptGenerator
from src.browser_automation import FlowController
from src.video_processor import VideoMerger


# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('./data/logs/automation.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


class VeoAutomation:
    def __init__(self, config_path: str = "./config/config.yaml"):
        """Initialize VEO Automation System"""
        load_dotenv()

        # Get API key
        self.gemini_api_key = os.getenv("GEMINI_API_KEY")
        if not self.gemini_api_key:
            raise ValueError("GEMINI_API_KEY not found in .env file")

        # Initialize components
        self.script_generator = ScriptGenerator(self.gemini_api_key)
        self.flow_controller = FlowController(
            cookies_path="./config/cookies.json",
            download_dir="./data/videos",
            headless=False
        )
        self.video_merger = VideoMerger(output_dir="./data/videos")

        # Create necessary directories
        os.makedirs("./data/scripts", exist_ok=True)
        os.makedirs("./data/videos", exist_ok=True)
        os.makedirs("./data/logs", exist_ok=True)

    async def run_full_pipeline(
        self,
        topic: str,
        duration: int = 60,
        scene_duration: int = 8,
        style: str = "cinematic",
        aspect_ratio: str = "16:9",
        add_transitions: bool = True
    ) -> dict:
        """
        Run complete automation pipeline

        Args:
            topic: Video topic
            duration: Total video duration in seconds
            scene_duration: Duration per scene
            style: Visual style
            aspect_ratio: Video aspect ratio
            add_transitions: Add transitions between scenes

        Returns:
            Dict with results and file paths
        """
        project_name = f"project_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        logger.info(f"\n{'='*80}")
        logger.info(f"🚀 Starting VEO Automation Pipeline")
        logger.info(f"Project: {project_name}")
        logger.info(f"Topic: {topic}")
        logger.info(f"{'='*80}\n")

        results = {
            'project_name': project_name,
            'topic': topic,
            'started_at': datetime.now().isoformat(),
            'script_path': None,
            'scenes': [],
            'final_video_path': None,
            'status': 'started'
        }

        try:
            # Step 1: Generate Script
            logger.info("📝 STEP 1: Generating script with Gemini API...")
            script = self.script_generator.generate_script(
                topic=topic,
                duration=duration,
                scene_duration=scene_duration,
                aspect_ratio=aspect_ratio,
                style=style
            )

            script_path = self.script_generator.save_script(
                script,
                output_dir="./data/scripts"
            )
            results['script_path'] = script_path

            logger.info(f"✅ Script generated: {len(script['scenes'])} scenes")
            logger.info(f"   Title: {script['title']}")

            # Step 2: Start browser and generate videos
            logger.info("\n🌐 STEP 2: Starting browser automation...")
            await self.flow_controller.start()

            # Navigate to Flow
            flow_ready = await self.flow_controller.goto_flow()
            if not flow_ready:
                raise Exception("Failed to access Flow. Please check cookies.")

            # Save cookies for next time
            await self.flow_controller.save_cookies()

            # Step 3: Generate videos for each scene
            logger.info(f"\n🎬 STEP 3: Generating {len(script['scenes'])} videos...")
            scene_results = await self.flow_controller.generate_scene_videos(
                scenes=script['scenes'],
                project_name=project_name
            )

            results['scenes'] = scene_results

            # Count successful videos
            successful_scenes = [s for s in scene_results if s['status'] == 'success']
            logger.info(f"\n📊 Generation Summary:")
            logger.info(f"   Total scenes: {len(scene_results)}")
            logger.info(f"   Successful: {len(successful_scenes)}")
            logger.info(f"   Failed: {len(scene_results) - len(successful_scenes)}")

            # Step 4: Merge videos
            if successful_scenes:
                logger.info(f"\n🔗 STEP 4: Merging videos...")

                video_paths = [
                    s['download_path'] for s in successful_scenes
                    if s.get('download_path')
                ]

                # Validate videos before merging
                valid_videos = self.video_merger.validate_videos(video_paths)

                if valid_videos:
                    final_video_path = self.video_merger.merge_scenes(
                        video_paths=valid_videos,
                        output_filename=f"{project_name}_final.mp4",
                        add_transitions=add_transitions
                    )

                    results['final_video_path'] = final_video_path

                    if final_video_path:
                        logger.info(f"\n✅ SUCCESS! Final video: {final_video_path}")
                        results['status'] = 'completed'
                    else:
                        logger.error("\n❌ Failed to merge videos")
                        results['status'] = 'merge_failed'
                else:
                    logger.error("\n❌ No valid videos to merge")
                    results['status'] = 'no_valid_videos'
            else:
                logger.error("\n❌ No successful scenes generated")
                results['status'] = 'generation_failed'

        except Exception as e:
            logger.error(f"\n❌ Pipeline error: {str(e)}")
            results['status'] = 'error'
            results['error'] = str(e)

        finally:
            # Cleanup
            logger.info("\n🧹 Cleaning up...")
            await self.flow_controller.close()

            # Save results
            results['completed_at'] = datetime.now().isoformat()
            results_path = f"./data/logs/{project_name}_results.json"

            with open(results_path, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=2)

            logger.info(f"💾 Results saved: {results_path}")

        return results

    async def generate_script_only(
        self,
        topic: str,
        duration: int = 60,
        scene_duration: int = 8,
        style: str = "cinematic"
    ) -> str:
        """Generate script only without video creation"""
        logger.info("📝 Generating script only...")

        script = self.script_generator.generate_script(
            topic=topic,
            duration=duration,
            scene_duration=scene_duration,
            style=style
        )

        script_path = self.script_generator.save_script(script)

        logger.info(f"✅ Script saved: {script_path}")
        logger.info(f"   Title: {script['title']}")
        logger.info(f"   Scenes: {len(script['scenes'])}")

        return script_path

    async def generate_from_script(self, script_path: str) -> dict:
        """Generate videos from existing script file"""
        logger.info(f"📂 Loading script from: {script_path}")

        script = self.script_generator.load_script(script_path)
        project_name = f"project_{datetime.now().strftime('%Y%m%d_%H%M%S')}"

        logger.info(f"🎬 Generating videos for: {script['title']}")

        # Start browser
        await self.flow_controller.start()
        await self.flow_controller.goto_flow()

        # Generate videos
        scene_results = await self.flow_controller.generate_scene_videos(
            scenes=script['scenes'],
            project_name=project_name
        )

        await self.flow_controller.close()

        return {
            'project_name': project_name,
            'script_path': script_path,
            'scenes': scene_results
        }


def main():
    parser = argparse.ArgumentParser(
        description="VEO 3.1 Video Automation System"
    )

    parser.add_argument(
        "--topic",
        type=str,
        help="Video topic/subject"
    )

    parser.add_argument(
        "--duration",
        type=int,
        default=60,
        help="Total video duration in seconds (default: 60)"
    )

    parser.add_argument(
        "--scene-duration",
        type=int,
        default=8,
        help="Duration per scene in seconds (default: 8)"
    )

    parser.add_argument(
        "--style",
        type=str,
        default="cinematic",
        help="Visual style (default: cinematic)"
    )

    parser.add_argument(
        "--aspect-ratio",
        type=str,
        default="16:9",
        choices=["16:9", "9:16", "1:1"],
        help="Aspect ratio (default: 16:9)"
    )

    parser.add_argument(
        "--script-only",
        action="store_true",
        help="Generate script only without videos"
    )

    parser.add_argument(
        "--from-script",
        type=str,
        help="Generate videos from existing script file"
    )

    args = parser.parse_args()

    # Initialize automation
    automation = VeoAutomation()

    async def run():
        if args.script_only:
            if not args.topic:
                print("❌ --topic is required for script generation")
                return

            await automation.generate_script_only(
                topic=args.topic,
                duration=args.duration,
                scene_duration=args.scene_duration,
                style=args.style
            )

        elif args.from_script:
            await automation.generate_from_script(args.from_script)

        else:
            if not args.topic:
                print("❌ --topic is required")
                return

            await automation.run_full_pipeline(
                topic=args.topic,
                duration=args.duration,
                scene_duration=args.scene_duration,
                style=args.style,
                aspect_ratio=args.aspect_ratio
            )

    asyncio.run(run())


if __name__ == "__main__":
    main()
