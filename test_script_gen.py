"""Test script generation only (without browser automation dependencies)"""

import os
import sys
import io

# Fix Windows encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from dotenv import load_dotenv
from src.script_generator import ScriptGenerator

load_dotenv()

api_key = os.getenv("GEMINI_API_KEY")
if not api_key:
    print("ERROR: GEMINI_API_KEY not found in .env")
    exit(1)

print("="*60)
print("TESTING SCRIPT GENERATION")
print("="*60)

generator = ScriptGenerator(api_key)

print("\nGenerating script...")
print("Topic: Khám phá rừng Amazon huyền bí")
print("Duration: 30 seconds")
print("Scene duration: 8 seconds")
print("\nPlease wait...\n")

script = generator.generate_script(
    topic="Khám phá rừng Amazon huyền bí",
    duration=30,
    scene_duration=8,
    style="cinematic documentary"
)

print("="*60)
print(f"SCRIPT GENERATED SUCCESSFULLY")
print("="*60)
print(f"Title: {script['title']}")
print(f"Description: {script['description']}")
print(f"Number of scenes: {len(script['scenes'])}")
print(f"Total duration: {script['total_duration']}s")
print("\n" + "="*60)
print("SCENES:")
print("="*60)

for i, scene in enumerate(script['scenes'], 1):
    print(f"\n[Scene {i}]")
    print(f"Duration: {scene['duration']}s")
    print(f"Description: {scene['description']}")
    print(f"Camera: {scene.get('camera_movement', 'N/A')}")
    print(f"Time: {scene.get('time_of_day', 'N/A')}")
    print(f"\nVEO Prompt:")
    print(f"{scene['veo_prompt']}")
    print("-"*60)

# Save script
filepath = generator.save_script(script)

print("\n" + "="*60)
print(f"Script saved to: {filepath}")
print("="*60)
print("\nYou can now:")
print("1. Review the script JSON file")
print("2. Install full dependencies to generate videos:")
print("   pip install playwright moviepy")
print("   playwright install chromium")
print("3. Run full automation:")
print(f"   python main.py --from-script {filepath}")
print("="*60)
