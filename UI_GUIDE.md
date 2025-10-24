# 🎨 Web UI Guide

## 🚀 Khởi Động UI

### Windows:
```bash
run_ui.bat
```

### Linux/Mac:
```bash
python app.py
```

UI sẽ mở tại: **http://localhost:7860**

## 📋 Các Tab Chính

### 1. 📝 Script Generation
**Tạo kịch bản video tự động**

**Input:**
- Chủ đề video (bắt buộc)
- Tổng thời lượng (10-300 giây)
- Thời lượng mỗi scene (5-15 giây)
- Phong cách (cinematic, documentary, anime, etc.)
- Tỷ lệ khung hình (16:9, 9:16, 1:1)

**Output:**
- Kịch bản JSON với prompts tối ưu cho VEO
- File được lưu tự động trong `data/scripts/`

**Example:**
1. Nhập: "Khám phá rừng Amazon huyền bí"
2. Duration: 30s
3. Scene duration: 8s
4. Style: cinematic
5. Click "Generate Script"
6. ➡️ Nhận kịch bản với 3-4 scenes

### 2. 📚 Script Library
**Quản lý kịch bản đã tạo**

**Features:**
- Xem danh sách tất cả scripts
- Load và preview script
- Copy file path để dùng với CLI

**Usage:**
1. Click "Refresh List" để cập nhật
2. Chọn script từ dropdown
3. Click "Load Script" để xem chi tiết

### 3. 🎥 Video Generation
**Thông tin về video automation**

**Hiển thị:**
- System status (API key, cookies, dependencies)
- Hướng dẫn generate video qua CLI
- Requirements và lưu ý

**Note:** Video generation chạy qua CLI vì cần browser automation.

### 4. ⚙️ Settings
**Cấu hình hệ thống**

**Hiển thị:**
- API key status
- Hướng dẫn config .env
- Hướng dẫn extract cookies
- Links đến documentation

### 5. ❓ Help
**Hướng dẫn và troubleshooting**

**Nội dung:**
- Quick start guide
- Installation instructions
- Troubleshooting tips
- Example topics
- Best practices

## 🎯 Workflow Điển Hình

### Workflow 1: Tạo Video Mới
```
1. Vào tab "Script Generation"
2. Nhập thông tin video
3. Generate script
4. Copy script file path
5. Mở terminal:
   python main.py --from-script <path>
6. Chờ video hoàn thành (~40-60 phút cho 60s video)
```

### Workflow 2: Batch Scripts
```
1. Tạo nhiều scripts trên UI:
   - Script 1: Topic A
   - Script 2: Topic B
   - Script 3: Topic C

2. Vào tab "Script Library"
3. Review từng script

4. Generate videos qua CLI (tuần tự):
   python main.py --from-script script_1.json
   python main.py --from-script script_2.json
   python main.py --from-script script_3.json
```

## 🖼️ Screenshots

### Script Generation Tab
```
┌─────────────────────────────────────────┐
│  Topic: [Khám phá rừng Amazon...]       │
│  Duration: [60] seconds                 │
│  Scene Duration: [8] seconds            │
│  Style: [cinematic ▼]                   │
│  Aspect Ratio: ○ 16:9  ○ 9:16  ○ 1:1  │
│                                         │
│  [🚀 Generate Script]                   │
│                                         │
│  Result:                                │
│  ✅ Script Generated Successfully!      │
│  Title: ...                             │
│  Scenes: 8                              │
│  ...                                    │
└─────────────────────────────────────────┘
```

## 🔧 Customization

### Thêm Style Mới

Edit `app.py`:
```python
style_input = gr.Dropdown(
    choices=[
        "cinematic",
        "documentary",
        "anime",
        "YOUR_NEW_STYLE",  # Add here
    ],
    ...
)
```

### Thay Đổi Port

```python
app.launch(
    server_port=8080,  # Change from 7860
    ...
)
```

### Enable Public Sharing

```python
app.launch(
    share=True,  # Creates public URL
    ...
)
```

## 🐛 Troubleshooting

### UI không khởi động
```bash
# Check Gradio installed
pip install gradio

# Check for errors
python app.py
```

### Script generation không hoạt động
- ✅ Check GEMINI_API_KEY trong .env
- ✅ Test: `python quick_test.py`
- ✅ Check internet connection

### Port 7860 đã được dùng
```bash
# Find and kill process using port
netstat -ano | findstr :7860

# Or change port in app.py
server_port=8080
```

## 💡 Tips

### 1. Faster Script Testing
Dùng duration ngắn (10-30s) để test nhanh prompt quality.

### 2. Parallel Script Generation
Mở nhiều tabs browser, generate nhiều scripts cùng lúc.

### 3. Save Favorite Settings
Note lại settings tốt nhất cho từng loại video.

### 4. Preview Before Generate
Luôn review script kỹ trong "Script Library" trước khi generate video.

## 📊 UI Features

### ✅ Hiện Có
- [x] Script generation với Gemini
- [x] Script library management
- [x] System status check
- [x] Settings display
- [x] Help & documentation

### 🚧 Planned (Future)
- [ ] Direct video generation từ UI
- [ ] Video preview player
- [ ] Progress tracking
- [ ] Project management
- [ ] Batch operations UI
- [ ] Analytics dashboard

## 🔐 Security

**⚠️ Important:**
- UI chỉ accessible trên localhost mặc định
- Không bật `share=True` nếu có sensitive data
- Không commit `.env` file

## 📞 Support

**UI Issues:**
```bash
# Check logs
tail -f data/logs/automation.log

# Test dependencies
python quick_test.py

# Reinstall Gradio
pip install --upgrade gradio
```

**Need Help?**
- Read: [SETUP_GUIDE.md](SETUP_GUIDE.md)
- Check: [README.md](README.md)
- Test: `python quick_test.py`

---

**Happy creating! 🎬**
