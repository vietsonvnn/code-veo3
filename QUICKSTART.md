# ⚡ Quick Start - 5 Phút Setup

## 🎯 Mục Tiêu
Chạy được automation tạo video VEO 3.1 trong 5 phút

## 📝 Các Bước

### 1️⃣ Cài Đặt Dependencies (2 phút)

```bash
cd veo-automation

# Tạo virtual environment
python -m venv venv

# Kích hoạt (Windows)
venv\Scripts\activate

# Cài đặt packages
pip install -r requirements.txt

# Cài playwright
playwright install chromium
```

### 2️⃣ Cấu Hình API Key (1 phút)

```bash
# Copy template
copy .env.example .env

# Mở .env và điền API key
notepad .env
```

**Lấy API key:**
1. Vào: https://aistudio.google.com/apikey
2. Click "Create API Key"
3. Copy và paste vào file `.env`

```
GEMINI_API_KEY=AIzaSyC...your_actual_key
```

### 3️⃣ Extract Cookies (1 phút)

**Cài extension Cookie Editor:**
- Chrome: https://bit.ly/cookie-editor-chrome
- Tìm "Cookie Editor" và install

**Export cookies:**
1. Mở https://labs.google/fx/vi/tools/flow
2. Đăng nhập Google
3. Click icon Cookie Editor → Export → Export as JSON
4. Save as `cookies_raw.json` trong thư mục veo-automation
5. Chạy:

```bash
python tools/extract_cookies.py cookies_raw.json
```

### 4️⃣ Test Setup (30 giây)

```bash
python test_setup.py
```

Nếu thấy "All tests passed!" → Bạn đã sẵn sàng! ✅

### 5️⃣ Tạo Video Đầu Tiên (30 giây để test)

**Test tạo kịch bản (không tốn quota):**

```bash
python main.py --script-only --topic "Khám phá vũ trụ"
```

**Tạo video hoàn chỉnh:**

```bash
python main.py --topic "Hành trình khám phá rừng Amazon" --duration 30
```

⏳ Sẽ mất ~5-10 phút để hoàn thành.

## 🎬 Kết Quả

Sau khi chạy xong, bạn sẽ có:

```
veo-automation/
├── data/
│   ├── scripts/
│   │   └── script_20250124_123456.json    # Kịch bản
│   ├── videos/
│   │   ├── project_xxx_scene_001.mp4      # Video từng scene
│   │   ├── project_xxx_scene_002.mp4
│   │   └── project_xxx_final.mp4          # ✨ Video hoàn chỉnh
│   └── logs/
│       └── automation.log                  # Logs
```

## 🚨 Troubleshooting

### Lỗi "GEMINI_API_KEY not found"
→ Kiểm tra file `.env` có đúng format không

### Lỗi "Not logged in"
→ Extract lại cookies

### Lỗi "playwright not found"
→ Chạy: `playwright install chromium`

## 📚 Đọc Thêm

- Chi tiết: [SETUP_GUIDE.md](SETUP_GUIDE.md)
- Code: [README.md](README.md)

## 💡 Tips

### Tạo Video Nhanh (30s)
```bash
python main.py --topic "Your topic" --duration 30 --scene-duration 6
```

### Test Trước Khi Generate
```bash
# 1. Tạo script trước
python main.py --script-only --topic "Your topic"

# 2. Kiểm tra script trong data/scripts/

# 3. Generate từ script
python main.py --from-script data/scripts/script_xxx.json
```

### Chạy Không Hiển Thị Browser
Sửa [config/config.yaml](config/config.yaml):
```yaml
browser:
  headless: true
```

---

**Chúc bạn thành công! 🎉**

Nếu gặp vấn đề, check logs: `tail -f data/logs/automation.log`
