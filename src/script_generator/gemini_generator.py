"""
Gemini API Script Generator
Tạo kịch bản video chia theo scenes với prompt cho VEO 3.1
"""

import google.generativeai as genai
import json
import os
from typing import List, Dict
from datetime import datetime


class ScriptGenerator:
    def __init__(self, api_key: str, model: str = "gemini-2.0-flash-exp"):
        """
        Initialize Gemini API

        Args:
            api_key: Gemini API key
            model: Model name (default: gemini-2.0-flash-exp)
        """
        genai.configure(api_key=api_key)
        self.model = genai.GenerativeModel(model)

    def generate_script(
        self,
        topic: str,
        duration: int = 60,
        scene_duration: int = 8,
        aspect_ratio: str = "16:9",
        style: str = "cinematic"
    ) -> Dict:
        """
        Generate video script with scenes

        Args:
            topic: Video topic/subject
            duration: Total video duration in seconds
            scene_duration: Duration per scene in seconds
            aspect_ratio: Video aspect ratio (16:9, 9:16, 1:1)
            style: Visual style (cinematic, anime, realistic, etc.)

        Returns:
            Dict with script metadata and scenes
        """
        num_scenes = duration // scene_duration

        prompt = f"""
Tạo kịch bản video về "{topic}" với các yêu cầu sau:

**Thông số kỹ thuật:**
- Tổng thời lượng: {duration} giây
- Số lượng cảnh: {num_scenes} cảnh
- Thời lượng mỗi cảnh: {scene_duration} giây
- Tỷ lệ khung hình: {aspect_ratio}
- Phong cách: {style}

**Yêu cầu:**
1. Chia video thành {num_scenes} cảnh (scenes) rõ ràng
2. Mỗi cảnh cần có:
   - Mô tả chi tiết cảnh quay (description)
   - Prompt tối ưu cho VEO 3.1 (veo_prompt) - tập trung vào chuyển động, góc máy, ánh sáng
   - Hướng chuyển động camera (camera_movement)
   - Thời điểm trong ngày (time_of_day)

3. Prompt phải:
   - Chi tiết về visual elements
   - Mô tả rõ camera movement (pan, zoom, dolly, etc.)
   - Bao gồm lighting và mood
   - Dài 100-200 từ tiếng Anh
   - Tối ưu cho VEO 3.1 (tránh yêu cầu quá phức tạp)

**Output format (JSON):**
```json
{{
  "title": "Tiêu đề video",
  "description": "Mô tả tổng quan",
  "total_duration": {duration},
  "num_scenes": {num_scenes},
  "style": "{style}",
  "scenes": [
    {{
      "scene_number": 1,
      "duration": {scene_duration},
      "description": "Mô tả cảnh bằng tiếng Việt",
      "veo_prompt": "Detailed English prompt optimized for VEO 3.1 with camera movements, lighting, and visual details",
      "camera_movement": "slow pan left",
      "time_of_day": "golden hour",
      "mood": "peaceful"
    }}
  ]
}}
```

Hãy tạo kịch bản hoàn chỉnh theo format trên.
"""

        response = self.model.generate_content(
            prompt,
            generation_config={
                "temperature": 0.7,
                "top_p": 0.95,
                "top_k": 40,
                "max_output_tokens": 8192,
            }
        )

        # Extract JSON from response
        text = response.text

        # Remove markdown code blocks if present
        if "```json" in text:
            text = text.split("```json")[1].split("```")[0]
        elif "```" in text:
            text = text.split("```")[1].split("```")[0]

        script_data = json.loads(text.strip())

        # Add metadata
        script_data["created_at"] = datetime.now().isoformat()
        script_data["aspect_ratio"] = aspect_ratio

        return script_data

    def save_script(self, script_data: Dict, output_dir: str = "./data/scripts") -> str:
        """
        Save script to JSON file

        Args:
            script_data: Script dictionary
            output_dir: Output directory

        Returns:
            Path to saved file
        """
        os.makedirs(output_dir, exist_ok=True)

        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"script_{timestamp}.json"
        filepath = os.path.join(output_dir, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(script_data, f, ensure_ascii=False, indent=2)

        print(f"✅ Script saved: {filepath}")
        return filepath

    def load_script(self, filepath: str) -> Dict:
        """Load script from JSON file"""
        with open(filepath, 'r', encoding='utf-8') as f:
            return json.load(f)

    def refine_scene_prompt(self, scene_description: str, style: str = "cinematic") -> str:
        """
        Refine a single scene description into optimized VEO prompt

        Args:
            scene_description: Scene description in Vietnamese
            style: Visual style

        Returns:
            Optimized English prompt for VEO 3.1
        """
        prompt = f"""
Convert this scene description into an optimized VEO 3.1 prompt:

Scene: {scene_description}
Style: {style}

Requirements:
- 100-200 words in English
- Include camera movement details
- Describe lighting and mood
- Focus on visual elements and motion
- Use cinematic terminology
- Optimize for VEO 3.1 capabilities

Output only the prompt, no explanation.
"""

        response = self.model.generate_content(prompt)
        return response.text.strip()


# Example usage
if __name__ == "__main__":
    import os
    from dotenv import load_dotenv

    load_dotenv()
    api_key = os.getenv("GEMINI_API_KEY")

    if not api_key:
        print("❌ GEMINI_API_KEY not found in .env file")
        exit(1)

    generator = ScriptGenerator(api_key)

    # Test generation
    print("🎬 Generating script...")
    script = generator.generate_script(
        topic="Hành trình khám phá rừng nhiệt đới Amazon",
        duration=60,
        scene_duration=8,
        style="cinematic documentary"
    )

    print(f"\n✅ Generated {len(script['scenes'])} scenes")
    print(f"Title: {script['title']}")

    # Save script
    filepath = generator.save_script(script)
    print(f"📁 Saved to: {filepath}")
