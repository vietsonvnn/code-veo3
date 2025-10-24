"""
Simple script generator - No browser automation needed
Chỉ tạo kịch bản, không generate video
"""

import os
import sys
import io
import argparse
from dotenv import load_dotenv

# Fix Windows encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from src.script_generator import ScriptGenerator

load_dotenv()

def main():
    parser = argparse.ArgumentParser(description="Generate VEO video scripts")

    parser.add_argument("--topic", type=str, required=True, help="Video topic")
    parser.add_argument("--duration", type=int, default=60, help="Total duration (seconds)")
    parser.add_argument("--scene-duration", type=int, default=8, help="Scene duration (seconds)")
    parser.add_argument("--style", type=str, default="cinematic", help="Visual style")
    parser.add_argument("--aspect-ratio", type=str, default="16:9", choices=["16:9", "9:16", "1:1"])

    args = parser.parse_args()

    # Get API key
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        print("ERROR: GEMINI_API_KEY not found in .env")
        return 1

    print("="*60)
    print("VEO SCRIPT GENERATOR")
    print("="*60)
    print(f"Topic: {args.topic}")
    print(f"Duration: {args.duration}s")
    print(f"Scene Duration: {args.scene_duration}s")
    print(f"Style: {args.style}")
    print(f"Aspect Ratio: {args.aspect_ratio}")
    print("="*60)
    print("\nGenerating script with Gemini API...")

    # Generate
    generator = ScriptGenerator(api_key)

    try:
        script = generator.generate_script(
            topic=args.topic,
            duration=args.duration,
            scene_duration=args.scene_duration,
            style=args.style,
            aspect_ratio=args.aspect_ratio
        )

        print("\n" + "="*60)
        print("SUCCESS!")
        print("="*60)
        print(f"Title: {script['title']}")
        print(f"Description: {script['description']}")
        print(f"Scenes: {len(script['scenes'])}")
        print(f"Duration: {script['total_duration']}s")

        # Show scenes
        print("\n" + "="*60)
        print("SCENES")
        print("="*60)

        for i, scene in enumerate(script['scenes'], 1):
            print(f"\n[Scene {i}] ({scene['duration']}s)")
            print(f"Description: {scene['description']}")
            print(f"Camera: {scene.get('camera_movement', 'N/A')}")
            print(f"Prompt: {scene['veo_prompt'][:100]}...")

        # Save
        filepath = generator.save_script(script)

        print("\n" + "="*60)
        print(f"Saved to: {filepath}")
        print("="*60)

        print("\nNext steps:")
        print("1. Review the script JSON file")
        print("2. Install Playwright to generate videos:")
        print("   pip install playwright moviepy")
        print("   playwright install chromium")
        print("3. Generate video:")
        print(f"   python main.py --from-script {filepath}")

        return 0

    except Exception as e:
        print(f"\nERROR: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
