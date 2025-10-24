# 📋 Project Summary - VEO 3.1 Automation

## 🎯 Tổng Quan

Hệ thống automation hoàn chỉnh để tạo video trên Google Labs Flow (VEO 3.1):
- **Không dùng API VEO** - Chỉ dùng browser automation với cookies
- **Gemini API** chỉ để tạo kịch bản (script generation)
- **Playwright** để tự động hóa tương tác với Flow
- **MoviePy** để merge video scenes

## 📦 Đã Triển Khai

### ✅ Core Modules

#### 1. Script Generator ([src/script_generator/](src/script_generator/))
- `gemini_generator.py`: Tạo kịch bản với Gemini API
- Features:
  - Tự động chia video thành scenes
  - Generate prompts tối ưu cho VEO 3.1
  - Hỗ trợ nhiều styles (cinematic, documentary, etc.)
  - Export/import JSON scripts

#### 2. Browser Automation ([src/browser_automation/](src/browser_automation/))
- `flow_controller.py`: Playwright automation cho Flow
- Features:
  - Cookie-based authentication
  - Navigate và interact với Flow UI
  - Generate video từng scene
  - Monitor progress và detect errors
  - Auto download videos
  - Retry mechanism

#### 3. Video Processor ([src/video_processor/](src/video_processor/))
- `merger.py`: Merge video scenes
- Features:
  - Validate video files
  - Concatenate scenes
  - Add crossfade transitions
  - Export final video

### ✅ Tools & Utilities

#### 4. Cookie Extractor ([tools/extract_cookies.py](tools/extract_cookies.py))
- Convert browser cookies to Playwright format
- Support Cookie Editor extension format
- Auto-detect authentication cookies

#### 5. Setup Tester ([test_setup.py](test_setup.py))
- Validate Python version
- Check dependencies
- Test API key
- Verify cookies
- Test Gemini connection

### ✅ Configuration

#### 6. Config Files
- [config/config.yaml](config/config.yaml): System settings
- [.env.example](.env.example): Environment template
- [requirements.txt](requirements.txt): Python dependencies

### ✅ Documentation

#### 7. User Guides
- [README.md](README.md): Comprehensive project overview
- [QUICKSTART.md](QUICKSTART.md): 5-minute quick start
- [SETUP_GUIDE.md](SETUP_GUIDE.md): Detailed setup & troubleshooting

### ✅ Main Entry Point

#### 8. CLI Application ([main.py](main.py))
- Full pipeline orchestration
- Multiple execution modes:
  - Full automation (script → videos → merge)
  - Script-only mode
  - Generate from existing script
- Command-line arguments
- Logging và results export

## 🏗️ Kiến Trúc

```
┌──────────────────────────────────────────────────────────┐
│                       USER INPUT                         │
│              (Topic, Duration, Style, etc.)              │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                  SCRIPT GENERATOR                        │
│                   (Gemini API)                           │
│  • Parse user requirements                               │
│  • Calculate number of scenes                            │
│  • Generate VEO-optimized prompts                        │
│  • Export JSON script                                    │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                 BROWSER AUTOMATION                       │
│                 (Playwright + Flow)                      │
│  • Load cookies (authentication)                         │
│  • Navigate to Flow                                      │
│  • For each scene:                                       │
│    - Input prompt                                        │
│    - Click generate                                      │
│    - Monitor progress (polling)                          │
│    - Detect completion/errors                            │
│    - Download video                                      │
│  • Handle retries                                        │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                  VIDEO PROCESSOR                         │
│                    (MoviePy)                             │
│  • Validate all scene videos                             │
│  • Apply transitions (optional)                          │
│  • Concatenate scenes                                    │
│  • Export final video                                    │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
                   📹 FINAL VIDEO
```

## 📊 File Structure

```
veo-automation/                  (Root directory)
│
├── src/                         (Source code)
│   ├── script_generator/        (Gemini API integration)
│   │   ├── __init__.py
│   │   └── gemini_generator.py  (ScriptGenerator class)
│   │
│   ├── browser_automation/      (Playwright automation)
│   │   ├── __init__.py
│   │   └── flow_controller.py   (FlowController class)
│   │
│   ├── video_processor/         (Video processing)
│   │   ├── __init__.py
│   │   └── merger.py            (VideoMerger class)
│   │
│   └── utils/                   (Helpers - extensible)
│       └── __init__.py
│
├── config/                      (Configuration)
│   ├── config.yaml              (System config)
│   └── cookies.json             (User cookies - gitignored)
│
├── data/                        (Runtime data)
│   ├── scripts/                 (Generated scripts)
│   ├── videos/                  (Downloaded & final videos)
│   └── logs/                    (Automation logs)
│
├── tools/                       (Helper scripts)
│   └── extract_cookies.py       (Cookie extraction tool)
│
├── main.py                      (CLI entry point)
├── test_setup.py                (Setup validator)
│
├── requirements.txt             (Python dependencies)
├── .env.example                 (Environment template)
├── .gitignore                   (Git ignore rules)
│
└── Documentation/
    ├── README.md                (Main documentation)
    ├── QUICKSTART.md            (Quick start guide)
    ├── SETUP_GUIDE.md           (Detailed setup)
    └── PROJECT_SUMMARY.md       (This file)
```

## 🔄 Workflow Chi Tiết

### Phase 1: Script Generation (10-30s)
```
User Input → Gemini API → Script JSON
```

**Input:**
- Topic (string)
- Duration (int, seconds)
- Scene duration (int, seconds)
- Style (string)
- Aspect ratio (string)

**Output:**
```json
{
  "title": "Video title",
  "description": "Overview",
  "scenes": [
    {
      "scene_number": 1,
      "duration": 8,
      "description": "Vietnamese description",
      "veo_prompt": "English prompt for VEO",
      "camera_movement": "slow pan left",
      "time_of_day": "golden hour"
    }
  ]
}
```

### Phase 2: Video Generation (5-10 min/scene)
```
For each scene:
  Load cookies → Navigate to Flow → Input prompt →
  Click generate → Wait (polling) → Download video
```

**Flow Automation Steps:**
1. Start Playwright browser
2. Load cookies from `config/cookies.json`
3. Navigate to `https://labs.google/fx/vi/tools/flow`
4. For each scene:
   - Find prompt textarea (selector: `textarea[placeholder*="prompt"]`)
   - Input VEO prompt
   - Click generate button
   - Poll for video completion (check for `<video>` element)
   - Get video URL
   - Download to `data/videos/`
5. Save results

**Error Handling:**
- Timeout after 5 minutes → Retry (max 3 attempts)
- Not logged in → Alert user to refresh cookies
- Video not found → Log error, continue next scene

### Phase 3: Video Merging (1-2 min)
```
Validate videos → Add transitions → Concatenate → Export
```

**Steps:**
1. Load all scene videos from `data/videos/`
2. Validate each video (check duration > 0)
3. Apply crossfade transitions (if enabled)
4. Concatenate using MoviePy
5. Export as `project_xxx_final.mp4`

## 🎯 Use Cases & Examples

### 1. Full Automation
```bash
python main.py --topic "Journey through Amazon rainforest" --duration 60
```

**What happens:**
1. Gemini creates 8-scene script (60s / 8s per scene)
2. Browser opens, loads cookies
3. Generates 8 videos on Flow (takes ~40-60 minutes)
4. Downloads all videos
5. Merges into final video
6. Saves to `data/videos/project_xxx_final.mp4`

### 2. Script-Only Mode
```bash
python main.py --script-only --topic "Deep ocean exploration"
```

**Use case:** Preview kịch bản trước khi generate (tiết kiệm quota)

### 3. Generate from Script
```bash
python main.py --from-script data/scripts/script_20250124_123456.json
```

**Use case:** Đã có script, chỉ cần generate videos

### 4. Batch Processing
```bash
# Generate multiple scripts first
python main.py --script-only --topic "Topic 1"
python main.py --script-only --topic "Topic 2"
python main.py --script-only --topic "Topic 3"

# Review scripts, then generate videos one by one
python main.py --from-script data/scripts/script_001.json
python main.py --from-script data/scripts/script_002.json
```

## 🔧 Configuration Options

### Video Settings
```yaml
video:
  default_duration: 60      # Total video length (seconds)
  scene_duration: 8         # Per scene length (seconds)
  aspect_ratio: "16:9"      # 16:9, 9:16, or 1:1
  quality: "1080p"
  format: "mp4"
```

### Gemini Settings
```yaml
gemini:
  model: "gemini-2.0-flash-exp"  # or gemini-1.5-pro
  temperature: 0.7               # Creativity (0-1)
  max_tokens: 8192
```

### Browser Settings
```yaml
browser:
  headless: false           # Show browser for debugging
  viewport:
    width: 1920
    height: 1080
  user_agent: "Mozilla/5.0..."
```

### Flow Settings
```yaml
flow:
  url: "https://labs.google/fx/vi/tools/flow"
  wait_timeout: 300         # Max wait time per video (seconds)
  retry_attempts: 3
  retry_delay: 5
```

## 🚀 Performance Metrics

### Timing
- **Script generation**: 10-30 seconds
- **Per scene video**: 5-7 minutes (VEO 3.1 processing time)
- **Download**: 10-30 seconds per video
- **Merging**: 1-2 minutes

### For 60s Video (8 scenes)
- **Total time**: 40-60 minutes
- **Breakdown**:
  - Script: 0.5 min
  - 8 videos: 40-56 min (8 × 5-7 min)
  - Downloads: 4 min
  - Merge: 2 min

### Resource Usage
- **CPU**: Low (mostly waiting for VEO)
- **RAM**: ~500MB (browser + Python)
- **Disk**: ~100MB per scene video
- **Network**: Stable connection required

### Scalability
- **Single machine**: 1 video at a time (browser limitation)
- **Daily capacity**: ~20-30 videos (depends on Flow quota)
- **Parallel**: Can generate scripts in batch, videos sequentially

## 🔒 Security & Best Practices

### Security
1. **Never commit cookies.json** (contains auth tokens)
2. **Keep .env private** (API keys)
3. **Gitignore configured** for sensitive files
4. **Cookie refresh**: Every 1-2 weeks
5. **Respect rate limits**: Don't abuse Flow

### Best Practices
1. **Test with short videos** (30s) first
2. **Review scripts** before generating (save quota)
3. **Monitor logs**: `tail -f data/logs/automation.log`
4. **Backup cookies**: Keep a copy of working cookies
5. **Track quota**: Know your Flow video limit

## 🐛 Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| `GEMINI_API_KEY not found` | Missing .env | Create .env from .env.example |
| `Not logged in` | Expired cookies | Re-extract cookies |
| `Timeout waiting for video` | VEO slow/overloaded | Increase wait_timeout |
| `Selector not found` | Flow UI changed | Update selectors in flow_controller.py |
| `MoviePy error` | Missing codecs | `pip install imageio-ffmpeg` |
| `Playwright browser not found` | Not installed | `playwright install chromium` |

## 📈 Future Enhancements

### Planned Features
- [ ] Parallel scene generation (với rate limiting)
- [ ] Advanced error recovery (auto-retry failed scenes)
- [ ] Video upscaling (1080p → 4K)
- [ ] SEO metadata generation (title, description, tags)
- [ ] YouTube auto-upload integration
- [ ] Web UI dashboard (Flask/FastAPI)
- [ ] Queue system cho batch processing
- [ ] Database storage (SQLite) cho projects

### Possible Extensions
- Character consistency (upload reference images)
- Voice-over integration (TTS APIs)
- Background music (royalty-free library)
- Subtitle generation (Whisper API)
- Multi-language support
- Cloud deployment (AWS/GCP)

## 📚 Tech Stack

### Core Dependencies
- **Python 3.10+**: Main language
- **Playwright 1.48+**: Browser automation
- **google-generativeai 0.8+**: Gemini API
- **MoviePy 1.0+**: Video processing
- **PyYAML 6.0+**: Config management
- **python-dotenv 1.0+**: Environment variables

### Development Tools
- **asyncio**: Async/await for Playwright
- **logging**: Comprehensive logging
- **argparse**: CLI argument parsing
- **json**: Script serialization

### External Services
- **Google Gemini API**: Script generation
- **Google Labs Flow**: VEO 3.1 video generation

## 🎓 Learning Resources

### For Understanding the Code
1. **Playwright**: https://playwright.dev/python/
2. **Gemini API**: https://ai.google.dev/docs
3. **MoviePy**: https://zulko.github.io/moviepy/
4. **Async Python**: https://docs.python.org/3/library/asyncio.html

### For Extending
1. **Prompt Engineering**: Optimize VEO prompts
2. **Video Processing**: Learn MoviePy effects
3. **Web Scraping**: Advanced Playwright techniques
4. **API Rate Limiting**: Handle quotas gracefully

## 📞 Getting Help

### Self-Service
1. Run diagnostics: `python test_setup.py`
2. Check logs: `cat data/logs/automation.log`
3. Read docs: [SETUP_GUIDE.md](SETUP_GUIDE.md)

### Documentation
- **Quick Start**: [QUICKSTART.md](QUICKSTART.md)
- **Full Setup**: [SETUP_GUIDE.md](SETUP_GUIDE.md)
- **Project Overview**: [README.md](README.md)
- **This Summary**: [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

## ✅ Checklist - Ready to Run?

- [ ] Python 3.10+ installed
- [ ] Dependencies installed (`pip install -r requirements.txt`)
- [ ] Playwright browser installed (`playwright install chromium`)
- [ ] `.env` created with valid `GEMINI_API_KEY`
- [ ] Cookies extracted to `config/cookies.json`
- [ ] Test passed (`python test_setup.py`)
- [ ] Directories created (auto-created on first run)

**If all checked → You're ready to go! 🚀**

```bash
python main.py --topic "Your amazing topic" --duration 60
```

---

## 📝 Notes

**Project created:** October 2025
**Language:** Python 3.10+
**Status:** Production-ready v1.0
**License:** Educational use

**Author Notes:**
- This system is designed for legitimate use cases
- Respect Google's Terms of Service
- Monitor your API/Flow quotas
- Don't abuse automation - be reasonable with request rates

---

**End of Project Summary** ✨
