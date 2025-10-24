# 🚀 Hướng Dẫn Cài Đặt Hoàn Chỉnh

## ✅ Đã Hoàn Thành

Hiện tại đã setup thành công:
- ✅ API Key: Gemini API đã được cấu hình
- ✅ Cookies: Flow cookies đã sẵn sàng
- ✅ Script Generator: Test thành công, tạo được kịch bản

**Script mẫu đã được tạo tại:**
```
./data/scripts/script_20251024_225450.json
```

## 📦 Cài Đặt Dependencies Còn Lại

Để chạy full automation (browser + video), cần cài thêm:

### Bước 1: Cài Playwright và MoviePy

```bash
cd veo-automation
pip install playwright moviepy opencv-python aiohttp tqdm
```

### Bước 2: Cài Playwright Browser

```bash
playwright install chromium
```

Lệnh này sẽ download Chromium browser (~200MB).

### Bước 3: Verify Installation

```bash
python quick_test.py
```

Nếu tất cả test PASS → Bạn đã sẵn sàng!

## 🎬 Chạy Full Automation

### Option 1: Tạo Video Mới (Full Pipeline)

```bash
python main.py --topic "Hành trình khám phá đại dương" --duration 30
```

**Lưu ý:** Video 30s sẽ mất khoảng 20-30 phút (VEO cần thời gian xử lý).

### Option 2: Generate Từ Script Có Sẵn

Bạn đã có file script, có thể chạy luôn:

```bash
python main.py --from-script ./data/scripts/script_20251024_225450.json
```

### Option 3: Chỉ Test Script (Không Tốn Quota)

```bash
python test_script_gen.py
```

Hoặc:

```bash
python main.py --script-only --topic "Your topic" --duration 60
```

## 🔍 Kiểm Tra Trước Khi Chạy

### Test 1: Gemini API
```bash
python test_script_gen.py
```
✅ Đã test thành công!

### Test 2: Cookies (Sau khi cài Playwright)
Chạy script test này:

```python
# test_cookies.py
from playwright.sync_api import sync_playwright
import json

with sync_playwright() as p:
    browser = p.chromium.launch(headless=False)
    context = browser.new_context()

    # Load cookies
    with open('./config/cookies.json', 'r') as f:
        cookies = json.load(f)
    context.add_cookies(cookies)

    page = context.new_page()
    page.goto('https://labs.google/fx/vi/tools/flow')

    print("Browser opened. Check if you're logged in.")
    input("Press Enter to close...")

    browser.close()
```

## 📊 Dự Kiến Thời Gian

### Video 30 giây (3-4 scenes):
- Script generation: 10-30s ✅ **ĐÃ TEST**
- Browser automation: 15-25 phút
- Video download: 1-2 phút
- Merging: 30s
- **Tổng: ~20-30 phút**

### Video 60 giây (8 scenes):
- Script generation: 10-30s
- Browser automation: 40-60 phút
- Video download: 3-5 phút
- Merging: 1-2 phút
- **Tổng: ~45-70 phút**

## 🎯 Next Steps

### 1. Test Script Generation (Đã Xong ✅)
```bash
python test_script_gen.py
```

### 2. Cài Playwright
```bash
pip install playwright
playwright install chromium
```

### 3. Test Browser Automation
Chạy test đơn giản:

```python
# test_browser.py
import asyncio
from src.browser_automation import FlowController

async def test():
    controller = FlowController(headless=False)
    await controller.start()

    success = await controller.goto_flow()

    if success:
        print("✅ Successfully accessed Flow!")
        input("Check browser, press Enter to close...")
    else:
        print("❌ Failed to access Flow - cookies may be expired")

    await controller.close()

asyncio.run(test())
```

### 4. Run Full Pipeline
```bash
python main.py --topic "Test video" --duration 30
```

## 🐛 Troubleshooting

### Lỗi "Module not found"
```bash
pip install -r requirements.txt
```

### Lỗi "Playwright browser not found"
```bash
playwright install chromium
```

### Lỗi "Not logged in" khi test browser
→ Cookies có thể đã hết hạn. Extract lại cookies mới:
1. Mở https://labs.google/fx/vi/tools/flow trong browser
2. Đăng nhập
3. Export cookies bằng Cookie Editor extension
4. Chạy: `python tools/extract_cookies.py cookies_raw.json`

### Unicode/Encoding errors trên Windows
→ Đã fix trong `test_script_gen.py` với:
```python
import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
```

## 📁 Files Structure

```
veo-automation/
├── .env                          ✅ API key configured
├── config/
│   └── cookies.json              ✅ Cookies ready
├── data/
│   ├── scripts/
│   │   └── script_xxx.json       ✅ Sample script created
│   ├── videos/                   (Videos will be saved here)
│   └── logs/                     (Automation logs)
├── test_script_gen.py            ✅ Working script test
├── quick_test.py                 (Quick validation)
└── main.py                       (Main entry point)
```

## ✅ Checklist

- [x] Python 3.10+ installed
- [x] Project files created
- [x] .env configured with GEMINI_API_KEY
- [x] cookies.json configured
- [x] Basic dependencies installed (dotenv, genai, yaml)
- [x] Script generation tested successfully
- [ ] Playwright installed
- [ ] MoviePy installed
- [ ] Playwright browser downloaded
- [ ] Full automation tested

**Còn 4 bước nữa để hoàn tất!**

## 🎉 Kết Quả

Sau khi hoàn tất, bạn sẽ có hệ thống tự động:

1. **Input:** Chủ đề video
2. **Process:**
   - Gemini tạo kịch bản
   - Playwright tự động generate trên VEO
   - Download videos
   - Merge thành video hoàn chỉnh
3. **Output:** File MP4 sẵn sàng upload

**Video output:** `./data/videos/project_xxx_final.mp4`

---

**Cần hỗ trợ?**
- Check logs: `./data/logs/automation.log`
- Review script: `./data/scripts/script_xxx.json`
- Read guides: [SETUP_GUIDE.md](SETUP_GUIDE.md)

**Happy automating! 🚀**
