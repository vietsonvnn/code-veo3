# 🚀 Hướng Dẫn Setup & Sử Dụng VEO Automation

## 📋 Yêu Cầu Hệ Thống

- Python 3.10 trở lên
- Windows/Linux/macOS
- 4GB RAM trở lên (8GB khuyến nghị)
- Kết nối internet ổn định

## 🔧 Cài Đặt

### Bước 1: Clone/Download project

```bash
cd C:\Users\Trading\veo-automation
```

### Bước 2: Tạo môi trường ảo Python

```bash
python -m venv venv
```

**Kích hoạt môi trường:**

Windows:
```bash
venv\Scripts\activate
```

Linux/Mac:
```bash
source venv/bin/activate
```

### Bước 3: Cài đặt dependencies

```bash
pip install -r requirements.txt
```

### Bước 4: Cài đặt Playwright browser

```bash
playwright install chromium
```

### Bước 5: Cấu hình API Key

1. Tạo file `.env` từ template:
```bash
copy .env.example .env
```

2. Lấy Gemini API Key:
   - Truy cập: https://aistudio.google.com/apikey
   - Tạo API key mới
   - Copy key

3. Mở file `.env` và điền API key:
```
GEMINI_API_KEY=your_actual_api_key_here
```

### Bước 6: Trích xuất Cookies từ Flow

**Quan trọng:** Cookies cần để automation có thể truy cập Flow

#### Cách 1: Dùng Cookie Editor Extension (Khuyến nghị)

1. Cài extension "Cookie Editor":
   - Chrome: https://chrome.google.com/webstore/detail/cookie-editor/hlkenndednhfkekhgcdicdfddnkalmdm
   - Edge: Search "Cookie Editor" trong Microsoft Edge Add-ons

2. Đăng nhập Google:
   - Mở https://labs.google/fx/vi/tools/flow
   - Đăng nhập với tài khoản Google có quyền truy cập Flow

3. Export cookies:
   - Click vào icon Cookie Editor trên toolbar
   - Click nút "Export" → "Export as JSON"
   - Copy toàn bộ JSON
   - Paste vào file `cookies_raw.json`

4. Convert cookies:
```bash
python tools/extract_cookies.py cookies_raw.json
```

#### Cách 2: Manual (Nâng cao)

Nếu bạn biết cách extract cookies từ DevTools:
1. F12 → Application → Cookies
2. Copy tất cả cookies của domain `.google.com`
3. Format thành JSON array
4. Lưu vào `config/cookies.json`

## 🎬 Sử Dụng

### Test Script Generator (Không cần cookies)

Test tạo kịch bản với Gemini API:

```bash
python -m src.script_generator.gemini_generator
```

### Chạy Full Pipeline

Tạo video hoàn chỉnh từ chủ đề:

```bash
python main.py --topic "Hành trình khám phá rừng nhiệt đới Amazon" --duration 60
```

**Tham số:**
- `--topic`: Chủ đề video (bắt buộc)
- `--duration`: Tổng thời lượng video (giây) - mặc định: 60
- `--scene-duration`: Thời lượng mỗi cảnh (giây) - mặc định: 8
- `--style`: Phong cách video - mặc định: "cinematic"
- `--aspect-ratio`: Tỷ lệ khung hình (16:9, 9:16, 1:1) - mặc định: 16:9

**Ví dụ nâng cao:**

```bash
python main.py \
  --topic "Cuộc sống dưới đáy đại dương" \
  --duration 120 \
  --scene-duration 10 \
  --style "documentary" \
  --aspect-ratio 16:9
```

### Chỉ Tạo Kịch Bản

Nếu chỉ muốn tạo script mà không generate video:

```bash
python main.py --script-only --topic "Your topic here"
```

Kịch bản sẽ được lưu vào: `data/scripts/script_YYYYMMDD_HHMMSS.json`

### Generate Video Từ Kịch Bản Có Sẵn

Nếu đã có file kịch bản từ trước:

```bash
python main.py --from-script data/scripts/script_20250124_123456.json
```

## 📁 Cấu Trúc Thư Mục

```
veo-automation/
├── data/
│   ├── scripts/          # Kịch bản đã tạo (.json)
│   ├── videos/           # Video scenes & final video
│   └── logs/             # Logs và results
├── config/
│   ├── cookies.json      # Cookies của bạn (KHÔNG commit lên git)
│   └── config.yaml       # Cấu hình hệ thống
├── src/                  # Source code
└── tools/                # Helper scripts
```

## 🔍 Quy Trình Hoạt Động

1. **Script Generation** (10-30 giây)
   - Gemini API tạo kịch bản với prompts tối ưu cho VEO
   - Chia video thành các scenes
   - Mỗi scene có prompt chi tiết

2. **Browser Automation** (5-10 phút/scene)
   - Playwright mở Chromium
   - Load cookies → Truy cập Flow
   - Với mỗi scene:
     - Nhập prompt vào Flow
     - Click Generate
     - Chờ VEO 3.1 tạo video (~5 phút)
     - Download video

3. **Video Processing** (1-2 phút)
   - Validate tất cả video scenes
   - Merge thành 1 video hoàn chỉnh
   - Export file cuối cùng

**Tổng thời gian:** ~10 phút cho video 60 giây (8 scenes)

## ⚙️ Customization

### Thay đổi model Gemini

Sửa trong [config/config.yaml](config/config.yaml):

```yaml
gemini:
  model: "gemini-2.0-flash-exp"  # Hoặc "gemini-1.5-pro"
  temperature: 0.7
```

### Tùy chỉnh video settings

```yaml
video:
  default_duration: 60
  scene_duration: 8
  aspect_ratio: "16:9"
  quality: "1080p"
```

### Chạy headless (không hiện browser)

```yaml
browser:
  headless: true  # false để xem browser hoạt động
```

## 🐛 Troubleshooting

### Lỗi "GEMINI_API_KEY not found"

**Giải pháp:**
- Kiểm tra file `.env` có tồn tại
- Đảm bảo định dạng: `GEMINI_API_KEY=your_key`
- Không có dấu ngoặc kép hoặc khoảng trắng thừa

### Lỗi "Not logged in"

**Nguyên nhân:** Cookies hết hạn hoặc không đúng

**Giải pháp:**
1. Extract lại cookies mới từ browser
2. Đảm bảo đăng nhập đúng tài khoản Google
3. Kiểm tra file `config/cookies.json` có cookies của `.google.com`

### Video generation timeout

**Nguyên nhân:** VEO 3.1 đang quá tải hoặc prompt quá phức tạp

**Giải pháp:**
1. Tăng timeout trong `config/config.yaml`:
```yaml
flow:
  wait_timeout: 600  # Tăng lên 10 phút
```

2. Đơn giản hóa prompt (giảm complexity)

### Playwright không tìm thấy selector

**Nguyên nhân:** Flow UI có thể thay đổi

**Giải pháp:**
1. Mở browser với `headless: false`
2. Quan sát UI của Flow
3. Update selectors trong [src/browser_automation/flow_controller.py](src/browser_automation/flow_controller.py)

**Các selector cần check:**
- `prompt_selector` (line ~90)
- `generate_button_selectors` (line ~110)
- `video_selectors` (line ~150)

### MoviePy error khi merge videos

**Giải pháp:**
```bash
pip install --upgrade moviepy
pip install imageio-ffmpeg
```

## 📊 Logs & Debugging

Logs được lưu tại: `data/logs/automation.log`

Xem logs:
```bash
tail -f data/logs/automation.log
```

Mỗi project có file results riêng:
```
data/logs/project_YYYYMMDD_HHMMSS_results.json
```

## 🎯 Tips & Best Practices

### 1. Optimize Prompts

Prompt tốt cho VEO 3.1:
- Chi tiết về chuyển động camera
- Mô tả ánh sáng và màu sắc
- Tránh yêu cầu quá phức tạp (nhiều object chuyển động)
- Độ dài: 100-200 từ

### 2. Scene Duration

- **8 giây**: Lý tưởng cho dynamic scenes
- **10-12 giây**: Tốt cho landscape/ambient shots
- **< 6 giây**: Quá ngắn, khó render tốt

### 3. Batch Processing

Nếu tạo nhiều video:

```bash
for topic in "Topic 1" "Topic 2" "Topic 3"; do
  python main.py --topic "$topic" --duration 60
  sleep 60  # Delay giữa các video
done
```

### 4. Cookie Management

- Cookies thường hết hạn sau 1-2 tuần
- Extract lại khi automation báo "Not logged in"
- Không share cookies (chứa thông tin nhạy cảm)

## 🔐 Security Notes

**⚠️ QUAN TRỌNG:**

- File `.env` và `config/cookies.json` chứa thông tin nhạy cảm
- **KHÔNG** commit lên Git
- **KHÔNG** share cho người khác
- Đã thêm vào `.gitignore`

## 📚 API Limits

### Gemini API (Free Tier)
- 60 requests/minute
- 1500 requests/day
- Đủ cho ~100 videos/ngày

### Flow/VEO 3.1
- Free: 5 videos/tháng
- AI Pro: ~50 videos/tháng
- AI Ultra: ~100 videos/tháng

**Tip:** Dùng `--script-only` để test kịch bản trước, tránh lãng phí quota.

## 🚀 Next Steps

1. Test với video ngắn (30s, 4 scenes) trước
2. Kiểm tra quality của scenes
3. Tùy chỉnh prompts trong code nếu cần
4. Scale lên video dài hơn

## 💡 Advanced Usage

### Custom Prompt Templates

Sửa prompt template trong [src/script_generator/gemini_generator.py](src/script_generator/gemini_generator.py#L40)

### Parallel Scene Generation

**⚠️ Chú ý:** Flow có rate limit, không nên generate quá nhiều scene song song

### Video Post-Processing

Thêm effects sau khi merge trong [src/video_processor/merger.py](src/video_processor/merger.py)

## 📞 Support

Nếu gặp vấn đề:
1. Check logs: `data/logs/automation.log`
2. Re-extract cookies
3. Update dependencies: `pip install -r requirements.txt --upgrade`

Happy automating! 🎬✨
