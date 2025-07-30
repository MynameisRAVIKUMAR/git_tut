# ⚡ Quick Start: Build & Upload APK in 5 Minutes

**Fast track to get your Music Player APK built and uploaded to GitHub!**

## 🚀 Prerequisites
- ✅ Android Studio installed
- ✅ Git configured
- ✅ GitHub repository created

## ⏰ 5-Minute Process

### **Step 1: Setup** (1 minute)
```bash
# Navigate to your project
cd /path/to/music-player-project

# Make scripts executable  
chmod +x build-release.sh gradlew
```

### **Step 2: Build APK** (2-3 minutes)
```bash
# One command to build everything
./build-release.sh
```
**Output:** `app/release/MusicPlayer-v1.0.0-release.apk` (~18MB)

### **Step 3: Upload to GitHub** (1-2 minutes)

#### **Quick Upload:**
1. **Go to:** `https://github.com/yourusername/your-repo/releases`
2. **Click:** "Create a new release"
3. **Tag:** `v1.0.0`
4. **Title:** `🎵 Music Player v1.0.0`
5. **Upload:** Drag `MusicPlayer-v1.0.0-release.apk`
6. **Publish:** Click "Publish release"

#### **Copy-Paste Description:**
```markdown
🎵 **Feature-rich Android Music Player v1.0.0**

✨ **Features:**
- Modern Material Design 3 UI
- Background playback with notifications  
- Local music library scanning
- Playlists, favorites, search
- Android 7.0+ support

📱 **Install:** Download APK → Enable "Unknown Sources" → Install

🔒 **Privacy:** No internet, no tracking, fully offline
```

## ✅ Done! 
Your APK is now publicly available at:
`https://github.com/yourusername/repo/releases/latest`

---

## 🔧 Troubleshooting

**Build fails?**
```bash
./gradlew clean
./gradlew assembleRelease
```

**Can't upload?** 
- APK must be under 100MB ✅
- Use GitHub web interface if command line fails

**Need help?** Check `RELEASE_GUIDE.md` for detailed instructions.

---

**🎉 Congratulations! Your music player is now live!**