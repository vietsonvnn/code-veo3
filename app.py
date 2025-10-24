"""
VEO 3.1 Automation - Gradio Web UI
Giao diện web để tạo video tự động
"""

import gradio as gr
import os
import sys
import io
import json
import asyncio
from datetime import datetime
from pathlib import Path

# Fix Windows encoding
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from dotenv import load_dotenv
from src.script_generator import ScriptGenerator

# Load environment
load_dotenv()

# Global variables
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
script_generator = None

if GEMINI_API_KEY:
    script_generator = ScriptGenerator(GEMINI_API_KEY)


# ========== Script Generation Functions ==========

def generate_script_ui(topic, duration, scene_duration, style, aspect_ratio):
    """Generate script from UI inputs"""
    try:
        if not script_generator:
            return "❌ Error: GEMINI_API_KEY not configured in .env file", None, ""

        if not topic:
            return "❌ Error: Please enter a topic", None, ""

        # Generate script
        script = script_generator.generate_script(
            topic=topic,
            duration=int(duration),
            scene_duration=int(scene_duration),
            style=style,
            aspect_ratio=aspect_ratio
        )

        # Save script
        filepath = script_generator.save_script(script)

        # Format output
        output = f"""
✅ Script Generated Successfully!

📝 Title: {script['title']}
📄 Description: {script['description']}
🎬 Scenes: {len(script['scenes'])}
⏱️ Duration: {script['total_duration']}s
📐 Aspect Ratio: {aspect_ratio}
🎨 Style: {style}

📁 Saved to: {filepath}
"""

        # Format scenes for display
        scenes_display = ""
        for i, scene in enumerate(script['scenes'], 1):
            scenes_display += f"""
{'='*60}
🎬 Scene {i} ({scene['duration']}s)
{'='*60}

📝 Description:
{scene['description']}

🎥 Camera: {scene.get('camera_movement', 'N/A')}
🌅 Time: {scene.get('time_of_day', 'N/A')}
🎭 Mood: {scene.get('mood', 'N/A')}

🤖 VEO Prompt:
{scene['veo_prompt']}

"""

        return output, filepath, scenes_display

    except Exception as e:
        return f"❌ Error: {str(e)}", None, ""


def load_script_file(filepath):
    """Load and display script from file"""
    try:
        if not filepath or not os.path.exists(filepath):
            return "❌ Script file not found", ""

        with open(filepath, 'r', encoding='utf-8') as f:
            script = json.load(f)

        # Format output
        output = f"""
📁 Script Loaded

📝 Title: {script['title']}
📄 Description: {script['description']}
🎬 Scenes: {len(script['scenes'])}
⏱️ Duration: {script['total_duration']}s
"""

        # Format scenes
        scenes_display = ""
        for i, scene in enumerate(script['scenes'], 1):
            scenes_display += f"""
{'='*60}
🎬 Scene {i}
{'='*60}
{scene['description']}

VEO Prompt:
{scene['veo_prompt']}

"""

        return output, scenes_display

    except Exception as e:
        return f"❌ Error: {str(e)}", ""


def list_saved_scripts():
    """Get list of saved scripts"""
    script_dir = Path("./data/scripts")
    if not script_dir.exists():
        return []

    scripts = list(script_dir.glob("*.json"))
    return [str(s) for s in sorted(scripts, reverse=True)]


# ========== Video Generation Functions ==========

def generate_video_info():
    """Info about video generation"""
    return """
⚠️ Video Generation Requirements:

1. Install dependencies:
   pip install playwright moviepy
   playwright install chromium

2. Valid cookies in config/cookies.json

3. VEO 3.1 quota available on your account

Note: Video generation takes 5-7 minutes per scene.
For 60s video (8 scenes), expect ~40-60 minutes total.
"""


def check_system_status():
    """Check system readiness"""
    status = []

    # Check API key
    if GEMINI_API_KEY and GEMINI_API_KEY != 'your_api_key_here':
        status.append("✅ Gemini API Key configured")
    else:
        status.append("❌ Gemini API Key not configured")

    # Check cookies
    if os.path.exists('./config/cookies.json'):
        status.append("✅ Cookies file found")
    else:
        status.append("❌ Cookies file not found")

    # Check playwright
    try:
        import playwright
        status.append("✅ Playwright installed")
    except:
        status.append("❌ Playwright not installed")

    # Check moviepy
    try:
        import moviepy
        status.append("✅ MoviePy installed")
    except:
        status.append("❌ MoviePy not installed")

    return "\n".join(status)


# ========== Gradio UI ==========

def create_ui():
    """Create Gradio interface"""

    with gr.Blocks(
        title="VEO 3.1 Video Automation",
        theme=gr.themes.Soft()
    ) as app:

        gr.Markdown("""
        # 🎬 VEO 3.1 Video Automation
        ### Tự động tạo video AI với Google Labs Flow
        """)

        with gr.Tabs():

            # ===== Tab 1: Script Generation =====
            with gr.Tab("📝 Script Generation"):
                gr.Markdown("""
                ## Tạo Kịch Bản Video
                Sử dụng Gemini API để tạo kịch bản chi tiết cho video
                """)

                with gr.Row():
                    with gr.Column(scale=1):
                        topic_input = gr.Textbox(
                            label="🎯 Chủ đề video",
                            placeholder="VD: Khám phá rừng Amazon huyền bí",
                            lines=2
                        )

                        with gr.Row():
                            duration_input = gr.Slider(
                                minimum=10,
                                maximum=300,
                                value=60,
                                step=10,
                                label="⏱️ Tổng thời lượng (giây)"
                            )

                            scene_duration_input = gr.Slider(
                                minimum=5,
                                maximum=15,
                                value=8,
                                step=1,
                                label="🎬 Thời lượng mỗi scene (giây)"
                            )

                        style_input = gr.Dropdown(
                            choices=[
                                "cinematic",
                                "documentary",
                                "anime",
                                "realistic",
                                "artistic",
                                "sci-fi"
                            ],
                            value="cinematic",
                            label="🎨 Phong cách"
                        )

                        aspect_ratio_input = gr.Radio(
                            choices=["16:9", "9:16", "1:1"],
                            value="16:9",
                            label="📐 Tỷ lệ khung hình"
                        )

                        generate_btn = gr.Button(
                            "🚀 Generate Script",
                            variant="primary",
                            size="lg"
                        )

                    with gr.Column(scale=1):
                        script_output = gr.Textbox(
                            label="📊 Kết quả",
                            lines=15,
                            interactive=False
                        )

                        script_filepath = gr.Textbox(
                            label="📁 File path",
                            interactive=False,
                            visible=False
                        )

                gr.Markdown("### 🎬 Scenes Chi Tiết")
                scenes_output = gr.Textbox(
                    label="Các cảnh quay",
                    lines=20,
                    interactive=False
                )

                # Connect generate button
                generate_btn.click(
                    fn=generate_script_ui,
                    inputs=[
                        topic_input,
                        duration_input,
                        scene_duration_input,
                        style_input,
                        aspect_ratio_input
                    ],
                    outputs=[script_output, script_filepath, scenes_output]
                )

            # ===== Tab 2: Script Library =====
            with gr.Tab("📚 Script Library"):
                gr.Markdown("""
                ## Quản Lý Kịch Bản
                Xem và sử dụng lại các kịch bản đã tạo
                """)

                with gr.Row():
                    refresh_btn = gr.Button("🔄 Refresh List")

                script_list = gr.Dropdown(
                    label="📄 Scripts đã lưu",
                    choices=list_saved_scripts(),
                    interactive=True
                )

                load_btn = gr.Button("📂 Load Script", variant="primary")

                loaded_script_info = gr.Textbox(
                    label="📊 Thông tin script",
                    lines=10,
                    interactive=False
                )

                loaded_scenes = gr.Textbox(
                    label="🎬 Scenes",
                    lines=15,
                    interactive=False
                )

                # Connect buttons
                refresh_btn.click(
                    fn=lambda: gr.Dropdown(choices=list_saved_scripts()),
                    outputs=script_list
                )

                load_btn.click(
                    fn=load_script_file,
                    inputs=script_list,
                    outputs=[loaded_script_info, loaded_scenes]
                )

            # ===== Tab 3: Video Generation =====
            with gr.Tab("🎥 Video Generation"):
                gr.Markdown("""
                ## Tạo Video Tự Động
                Sử dụng browser automation để generate video trên VEO 3.1
                """)

                gr.Markdown(generate_video_info())

                gr.Markdown("### 📋 System Status")
                status_output = gr.Textbox(
                    label="Trạng thái hệ thống",
                    value=check_system_status(),
                    lines=6,
                    interactive=False
                )

                check_status_btn = gr.Button("🔄 Check Status")
                check_status_btn.click(
                    fn=check_system_status,
                    outputs=status_output
                )

                gr.Markdown("""
                ### 🚀 Generate Video

                ⚠️ **Important:**
                - Video generation requires Playwright and MoviePy installed
                - Each scene takes 5-7 minutes to generate
                - Make sure your cookies are valid
                - You need available VEO 3.1 quota

                **To generate videos, use CLI:**
                ```bash
                # From existing script
                python main.py --from-script ./data/scripts/script_xxx.json

                # Or full automation
                python main.py --topic "Your topic" --duration 60
                ```
                """)

            # ===== Tab 4: Settings =====
            with gr.Tab("⚙️ Settings"):
                gr.Markdown("""
                ## Cấu Hình Hệ Thống
                """)

                api_key_display = gr.Textbox(
                    label="🔑 Gemini API Key",
                    value=GEMINI_API_KEY[:20] + "..." if GEMINI_API_KEY else "Not configured",
                    interactive=False,
                    type="password"
                )

                gr.Markdown("""
                ### 📝 Cấu hình .env

                Chỉnh sửa file `.env` để thay đổi settings:

                ```bash
                GEMINI_API_KEY=your_api_key_here
                FLOW_URL=https://labs.google/fx/vi/tools/flow
                DEFAULT_VIDEO_DURATION=60
                SCENE_DURATION=8
                ```

                ### 🍪 Cookies

                Cookies được lưu tại: `config/cookies.json`

                Để extract cookies:
                ```bash
                python tools/extract_cookies.py cookies_raw.json
                ```

                ### 📚 Documentation

                - [Quick Start](QUICKSTART.md)
                - [Setup Guide](SETUP_GUIDE.md)
                - [Installation](INSTALL.md)
                """)

            # ===== Tab 5: Help =====
            with gr.Tab("❓ Help"):
                gr.Markdown("""
                ## 📖 Hướng Dẫn Sử Dụng

                ### 🚀 Quick Start

                1. **Tạo Script**
                   - Vào tab "Script Generation"
                   - Nhập chủ đề video
                   - Chọn thời lượng và style
                   - Click "Generate Script"

                2. **Xem Scripts Đã Lưu**
                   - Vào tab "Script Library"
                   - Chọn script từ dropdown
                   - Click "Load Script"

                3. **Generate Video (CLI)**
                   ```bash
                   python main.py --from-script <script_file>
                   ```

                ### 🔧 Installation

                ```bash
                # Install all dependencies
                pip install -r requirements.txt

                # Install Playwright browser
                playwright install chromium
                ```

                ### ⚠️ Troubleshooting

                **Script generation fails:**
                - Check GEMINI_API_KEY in .env file
                - Verify internet connection

                **Video generation fails:**
                - Ensure Playwright is installed
                - Check cookies are valid
                - Verify VEO 3.1 quota available

                ### 📞 Support

                - Check logs: `./data/logs/automation.log`
                - Review documentation: `SETUP_GUIDE.md`
                - Run diagnostics: `python quick_test.py`

                ### 🎯 Examples

                **Example Topics:**
                - "Khám phá rừng Amazon huyền bí"
                - "Hành trình dưới đáy đại dương"
                - "Cuộc sống trên sao Hỏa"
                - "Lịch sử phát triển của AI"

                **Recommended Settings:**
                - Duration: 30-60s (optimal)
                - Scene duration: 6-10s
                - Style: cinematic (best quality)
                - Aspect ratio: 16:9 (YouTube standard)
                """)

        gr.Markdown("""
        ---
        ### 🎬 VEO 3.1 Automation v1.0
        Made with ❤️ | [Documentation](README.md) | [GitHub](https://github.com)
        """)

    return app


# ========== Main ==========

if __name__ == "__main__":
    print("="*60)
    print("Starting VEO 3.1 Automation Web UI...")
    print("="*60)

    if not GEMINI_API_KEY:
        print("⚠️  WARNING: GEMINI_API_KEY not found in .env")
        print("Some features will be disabled.")

    print("\nLaunching Gradio interface...")
    print("Access at: http://localhost:7860")
    print("="*60)

    app = create_ui()
    app.launch(
        server_name="0.0.0.0",
        server_port=7860,
        share=False,
        show_error=True
    )
