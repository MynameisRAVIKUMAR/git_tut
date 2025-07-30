# 🎵 Feature-Rich Android Music Player

A modern, feature-rich music player for Android with smooth animations, beautiful UI, and comprehensive music management capabilities.

## ✨ Features

### 🎨 Modern UI/UX
- **Material Design 3** with beautiful animations and transitions
- **Dark/Light themes** with dynamic color adaptation
- **Edge-to-edge display** with immersive experience
- **Smooth animations** for all interactions
- **Custom album art display** with dynamic color palettes
- **Mini player** with seamless transitions to full player

### 🎵 Core Music Features
- **Local music scanning** with comprehensive metadata extraction
- **Background playback** with foreground service
- **Notification controls** with media session support
- **Shuffle and repeat modes** (None, One, All)
- **Seek bar** with precise position control
- **Volume control** integration
- **Audio focus management** for calls and other apps

### 📚 Library Management
- **Songs, Albums, Artists, Playlists** organization
- **Search functionality** across all metadata
- **Favorites system** with quick access
- **Recently played** and **Most played** tracking
- **Play count statistics** for each song
- **Custom playlists** creation and management
- **Smart playlists** based on usage patterns

### 🎧 Advanced Features
- **Equalizer integration** (system equalizer)
- **Audio effects** support
- **Sleep timer** for bedtime listening
- **Gesture controls** for quick actions
- **Widget support** for home screen
- **Android Auto** compatibility
- **Bluetooth and headset controls**

### 🔧 Technical Features
- **ExoPlayer** for robust audio playback
- **Room Database** for local storage
- **MVVM Architecture** with LiveData and ViewModels
- **Coroutines** for asynchronous operations
- **Modern Android APIs** (API 24+)
- **Permission handling** for Android 13+
- **Adaptive icons** and **splash screen**

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Media Player**: ExoPlayer
- **UI**: Material Design 3 + View Binding
- **Async**: Kotlin Coroutines + LiveData
- **Image Loading**: Glide
- **Navigation**: Navigation Component
- **Permissions**: Dexter

### Project Structure
```
app/
├── src/main/java/com/musicplayer/app/
│   ├── data/                 # Data models (Song, Playlist, etc.)
│   ├── database/             # Room database and DAOs
│   ├── repository/           # Repository classes
│   ├── service/              # Music service for background playback
│   ├── receiver/             # Broadcast receivers
│   ├── ui/                   # UI components
│   │   ├── activities/       # Activities
│   │   ├── fragments/        # Fragments
│   │   └── adapters/         # RecyclerView adapters
│   ├── utils/                # Utility classes
│   └── viewmodel/            # ViewModels
└── src/main/res/
    ├── layout/               # XML layouts
    ├── drawable/             # Icons and graphics
    ├── values/               # Colors, strings, themes
    ├── menu/                 # Menu resources
    ├── navigation/           # Navigation graphs
    └── anim/                 # Animations
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (Android 7.0) or higher
- Kotlin 1.9.20 or later

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/music-player-android.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

### Permissions Required
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `READ_MEDIA_AUDIO` (Android 13+)
- `WAKE_LOCK` (for background playback)
- `FOREGROUND_SERVICE` (for music service)
- `POST_NOTIFICATIONS` (for playback controls)

## 🎯 Key Components

### Music Service
- Background playback with ExoPlayer
- Media session integration
- Notification controls
- Audio focus management
- Playlist management

### Database Schema
- **Songs**: Complete metadata with play statistics
- **Playlists**: User-created and smart playlists
- **PlaylistSongs**: Many-to-many relationship table

### Repository Pattern
- Single source of truth for data
- Caching and offline support
- Automatic music library scanning
- Statistics tracking

## 🎨 UI Highlights

### Main Screen
- Bottom navigation with 4 tabs
- Search bar with real-time results
- Mini player with album art
- Pull-to-refresh for library updates

### Player Screen
- Full-screen album art with blur effects
- Animated seek bar and controls
- Shuffle/repeat toggle buttons
- Queue management

### Library Views
- Grid/List view options
- Sort by various criteria
- Fast scroll with alphabet indexer
- Empty states with helpful messages

## 🔧 Customization

### Themes
The app supports both light and dark themes with Material You dynamic colors:

```xml
<!-- Light Theme -->
<style name="Theme.MusicPlayer" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/purple_500</item>
    <!-- ... -->
</style>

<!-- Dark Theme -->
<style name="Theme.MusicPlayer.Dark" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/purple_200</item>
    <!-- ... -->
</style>
```

### Custom Animations
Smooth transitions between screens:
- Slide up/down for player
- Fade in/out for dialogs
- Shared element transitions for album art

## 📱 Screenshots

| Home Screen | Player | Library | Playlists |
|-------------|---------|---------|-----------|
| ![Home](screenshots/home.png) | ![Player](screenshots/player.png) | ![Library](screenshots/library.png) | ![Playlists](screenshots/playlists.png) |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Material Design** for design guidelines
- **ExoPlayer** for robust media playback
- **Android Jetpack** for modern Android development
- **Glide** for efficient image loading
- Open source community for inspiration

## 🐛 Known Issues

- Some album art may not load on older Android versions
- Equalizer requires system equalizer to be available
- Android Auto support is experimental

## 🔮 Future Plans

- [ ] Cloud storage integration (Google Drive, Dropbox)
- [ ] Lyrics display with synchronization
- [ ] Cross-fade between tracks
- [ ] Custom equalizer implementation
- [ ] Social features (sharing, recommendations)
- [ ] Podcast support
- [ ] Audio recording and voice memos

---

**Made with ❤️ for music lovers**