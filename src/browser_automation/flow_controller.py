"""
Google Labs Flow Browser Automation
Tự động hóa việc tạo video trên Flow với VEO 3.1
"""

import asyncio
import json
import os
from typing import Dict, List, Optional
from datetime import datetime
from playwright.async_api import async_playwright, Page, Browser, BrowserContext
import logging


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class FlowController:
    def __init__(
        self,
        cookies_path: str = "./config/cookies.json",
        download_dir: str = "./data/videos",
        headless: bool = False
    ):
        """
        Initialize Flow Controller

        Args:
            cookies_path: Path to cookies JSON file
            download_dir: Directory to save downloaded videos
            headless: Run browser in headless mode
        """
        self.cookies_path = cookies_path
        self.download_dir = download_dir
        self.headless = headless
        self.browser: Optional[Browser] = None
        self.context: Optional[BrowserContext] = None
        self.page: Optional[Page] = None

        os.makedirs(download_dir, exist_ok=True)

    async def start(self):
        """Start browser and load cookies"""
        playwright = await async_playwright().start()

        logger.info("🚀 Starting browser...")
        self.browser = await playwright.chromium.launch(
            headless=self.headless,
            args=[
                '--disable-blink-features=AutomationControlled',
                '--disable-dev-shm-usage',
                '--no-sandbox',
            ]
        )

        # Create context with viewport
        self.context = await self.browser.new_context(
            viewport={'width': 1920, 'height': 1080},
            user_agent='Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            accept_downloads=True
        )

        # Load cookies if exists
        if os.path.exists(self.cookies_path):
            logger.info(f"📂 Loading cookies from {self.cookies_path}")
            with open(self.cookies_path, 'r') as f:
                cookies = json.load(f)
                await self.context.add_cookies(cookies)
        else:
            logger.warning(f"⚠️  Cookies file not found: {self.cookies_path}")

        self.page = await self.context.new_page()
        logger.info("✅ Browser started")

    async def save_cookies(self):
        """Save current cookies to file"""
        cookies = await self.context.cookies()
        os.makedirs(os.path.dirname(self.cookies_path), exist_ok=True)

        with open(self.cookies_path, 'w') as f:
            json.dump(cookies, f, indent=2)

        logger.info(f"💾 Cookies saved to {self.cookies_path}")

    async def goto_flow(self):
        """Navigate to Flow page"""
        logger.info("🌐 Navigating to Flow...")
        await self.page.goto(
            "https://labs.google/fx/vi/tools/flow",
            wait_until="domcontentloaded",
            timeout=60000
        )
        await asyncio.sleep(3)  # Wait for dynamic content

        # Check if logged in
        page_content = await self.page.content()
        if "đăng nhập" in page_content.lower() or "sign in" in page_content.lower():
            logger.error("❌ Not logged in! Please update cookies.")
            return False

        logger.info("✅ Successfully loaded Flow")
        return True

    async def create_video_from_prompt(
        self,
        prompt: str,
        aspect_ratio: str = "16:9",
        wait_for_generation: bool = True
    ) -> Optional[str]:
        """
        Create video from text prompt

        Args:
            prompt: Text prompt for VEO 3.1
            aspect_ratio: Aspect ratio (16:9, 9:16, 1:1)
            wait_for_generation: Wait for video to finish generating

        Returns:
            Video URL or None if failed
        """
        logger.info(f"🎬 Creating video with prompt: {prompt[:100]}...")

        try:
            # Find and fill prompt textarea
            # Note: Selectors need to be updated based on actual Flow DOM
            prompt_selector = 'textarea[placeholder*="prompt"], textarea[aria-label*="prompt"], textarea'
            await self.page.wait_for_selector(prompt_selector, timeout=10000)
            await self.page.fill(prompt_selector, prompt)

            logger.info("✅ Prompt entered")
            await asyncio.sleep(1)

            # Set aspect ratio if needed
            # This selector needs to be verified on actual Flow page
            if aspect_ratio != "16:9":
                logger.info(f"⚙️  Setting aspect ratio to {aspect_ratio}")
                # await self.page.click(f'button[data-ratio="{aspect_ratio}"]')
                # await asyncio.sleep(1)

            # Click generate button
            generate_button_selectors = [
                'button:has-text("Tạo")',
                'button:has-text("Generate")',
                'button[type="submit"]',
                'button.generate-btn'
            ]

            clicked = False
            for selector in generate_button_selectors:
                try:
                    await self.page.click(selector, timeout=5000)
                    clicked = True
                    logger.info("✅ Generate button clicked")
                    break
                except:
                    continue

            if not clicked:
                logger.error("❌ Could not find generate button")
                return None

            if not wait_for_generation:
                return "pending"

            # Wait for video generation
            logger.info("⏳ Waiting for video generation...")
            video_url = await self.wait_for_video_generation()

            return video_url

        except Exception as e:
            logger.error(f"❌ Error creating video: {str(e)}")
            return None

    async def wait_for_video_generation(self, timeout: int = 300) -> Optional[str]:
        """
        Wait for video generation to complete

        Args:
            timeout: Maximum wait time in seconds

        Returns:
            Video URL or None if timeout/failed
        """
        start_time = datetime.now()

        while (datetime.now() - start_time).seconds < timeout:
            try:
                # Check for video element
                # These selectors need to be verified
                video_selectors = [
                    'video[src]',
                    'video source[src]',
                    'a[href*=".mp4"]',
                    '[data-video-url]'
                ]

                for selector in video_selectors:
                    try:
                        element = await self.page.query_selector(selector)
                        if element:
                            # Try to get video URL
                            video_url = await element.get_attribute('src') or \
                                       await element.get_attribute('href') or \
                                       await element.get_attribute('data-video-url')

                            if video_url:
                                logger.info(f"✅ Video generated: {video_url}")
                                return video_url
                    except:
                        continue

                # Check for error messages
                error_selectors = ['[role="alert"]', '.error', '.error-message']
                for selector in error_selectors:
                    try:
                        error = await self.page.query_selector(selector)
                        if error:
                            error_text = await error.inner_text()
                            logger.error(f"❌ Generation error: {error_text}")
                            return None
                    except:
                        continue

                await asyncio.sleep(5)  # Check every 5 seconds

            except Exception as e:
                logger.warning(f"⚠️  Error while waiting: {str(e)}")
                await asyncio.sleep(5)

        logger.error(f"❌ Timeout after {timeout} seconds")
        return None

    async def download_video(self, video_url: str, filename: str) -> Optional[str]:
        """
        Download video from URL

        Args:
            video_url: Video URL
            filename: Output filename

        Returns:
            Path to downloaded file or None
        """
        try:
            logger.info(f"📥 Downloading video: {video_url}")

            filepath = os.path.join(self.download_dir, filename)

            # Method 1: Use page.goto to trigger download
            async with self.page.expect_download() as download_info:
                await self.page.goto(video_url)

            download = await download_info.value
            await download.save_as(filepath)

            logger.info(f"✅ Video saved: {filepath}")
            return filepath

        except Exception as e:
            logger.error(f"❌ Download error: {str(e)}")
            return None

    async def generate_scene_videos(
        self,
        scenes: List[Dict],
        project_name: str = "video_project"
    ) -> List[Dict]:
        """
        Generate videos for all scenes

        Args:
            scenes: List of scene dictionaries with 'veo_prompt'
            project_name: Project name for organizing files

        Returns:
            List of scenes with video URLs and download paths
        """
        results = []

        for i, scene in enumerate(scenes, 1):
            logger.info(f"\n{'='*60}")
            logger.info(f"🎬 Processing Scene {i}/{len(scenes)}")
            logger.info(f"{'='*60}")

            prompt = scene.get('veo_prompt', scene.get('description', ''))

            # Create video
            video_url = await self.create_video_from_prompt(
                prompt=prompt,
                aspect_ratio=scene.get('aspect_ratio', '16:9'),
                wait_for_generation=True
            )

            scene_result = {
                **scene,
                'video_url': video_url,
                'download_path': None,
                'status': 'success' if video_url else 'failed'
            }

            if video_url and video_url != "pending":
                # Download video
                filename = f"{project_name}_scene_{i:03d}.mp4"
                download_path = await self.download_video(video_url, filename)
                scene_result['download_path'] = download_path

            results.append(scene_result)

            # Small delay between scenes
            if i < len(scenes):
                logger.info("⏳ Waiting 10 seconds before next scene...")
                await asyncio.sleep(10)

        return results

    async def close(self):
        """Close browser"""
        if self.browser:
            await self.browser.close()
            logger.info("👋 Browser closed")


# Example usage
async def main():
    controller = FlowController(headless=False)

    try:
        await controller.start()
        await controller.goto_flow()

        # Save cookies after successful login
        await controller.save_cookies()

        # Test single video creation
        test_prompt = """
        A cinematic shot of a lush Amazon rainforest canopy at golden hour.
        The camera slowly pans right across the dense green foliage as rays of
        warm sunlight pierce through the mist. Birds fly across the frame.
        Peaceful and serene atmosphere with soft ambient lighting.
        """

        video_url = await controller.create_video_from_prompt(test_prompt)

        if video_url:
            print(f"✅ Video created: {video_url}")
        else:
            print("❌ Failed to create video")

    finally:
        await controller.close()


if __name__ == "__main__":
    asyncio.run(main())
