# 🌟 Only Care - Premium Care & Support Platform

A modern Android application built with Jetpack Compose, offering premium audio and video calling services with elegant dark theme design.

![Only Care](app/src/main/res/drawable/app_logo.png)

## 📱 Features

### For Creators (Female Users)
- ✨ **Premium Profile** - Showcase your personality
- 💰 **Earnings Dashboard** - Track your income in real-time
- 💳 **UPI Withdrawals** - Instant money transfers
- 📊 **KYC Verification** - Secure PAN card verification
- 🎯 **Call Availability** - Toggle audio/video availability
- 📈 **Analytics** - View your performance stats

### For Users (Male Users)
- 🔍 **Discover Creators** - Browse verified creators
- 📞 **Audio/Video Calls** - HD quality calls
- 💬 **Real-time Chat** - Instant messaging
- ⭐ **Ratings & Reviews** - Rate your experience
- 💰 **Wallet System** - Easy recharge options
- 🎁 **Refer & Earn** - Invite friends and earn

### General Features
- 🌙 **Premium Dark Theme** - Modern, elegant design
- 🔐 **Secure Authentication** - OTP-based login
- 🌍 **Multi-language Support** - English, Hindi, Tamil, Telugu, Kannada
- 📱 **WhatsApp Channels** - Stay connected
- 🎨 **Beautiful Animations** - Smooth, responsive UI
- 🔔 **Push Notifications** - Real-time updates

## 🎨 Design Philosophy

Only Care features a **premium dark theme** with:
- Modern gradient effects
- Smooth animations
- Clean, minimalist UI
- High contrast for readability
- Professional color palette

## 🛠️ Tech Stack

### Core
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### Architecture
- **Pattern:** MVVM (Model-View-ViewModel)
- **DI:** Hilt (Dagger)
- **Navigation:** Navigation Compose
- **State Management:** Compose State & ViewModel

### Libraries & SDKs
- **Networking:** Retrofit + OkHttp
- **Image Loading:** Coil + Glide
- **Video/Audio Calling:** Agora RTC SDK
- **Push Notifications:** OneSignal
- **Async:** Kotlin Coroutines + Flow
- **Data Storage:** DataStore Preferences
- **JSON:** Gson

## 📦 Project Structure

```
com.onlycare.app/
├── data/
│   ├── model/           # Data models
│   ├── repository/      # Data repositories
│   └── remote/          # API services
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Business logic
├── presentation/
│   ├── components/      # Reusable UI components
│   ├── screens/         # App screens
│   │   ├── auth/        # Authentication screens
│   │   ├── main/        # Main app screens
│   │   ├── female/      # Creator-specific screens
│   │   ├── call/        # Call screens
│   │   └── settings/    # Settings screens
│   ├── navigation/      # Navigation setup
│   ├── theme/           # App theme & colors
│   └── viewmodel/       # ViewModels
└── di/                  # Dependency injection modules
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/onlycare_app.git
   cd onlycare_app
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository
   - Wait for Gradle sync

3. **Configure API Keys:**
   Create `local.properties` in the root directory:
   ```properties
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   AGORA_APP_ID=your_agora_app_id
   ONESIGNAL_APP_ID=your_onesignal_app_id
   ```

4. **Build and Run:**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press Shift+F10

## 🧪 Testing

### Test Credentials
- **Phone Number:** 011011
- **OTP:** 011011

### Running Tests
```bash
# Unit tests
.\gradlew test

# Instrumentation tests
.\gradlew connectedAndroidTest

# Lint checks
.\gradlew lint
```

## 📱 Building Release APK/AAB

### Quick Build
```bash
# Build signed AAB (for Play Store)
.\gradlew bundleRelease

# Build signed APK (for direct install)
.\gradlew assembleRelease
```

### Detailed Instructions
See [RELEASE.md](RELEASE.md) for complete release documentation.

## 🎨 App Theming

### Color Palette
```kotlin
// Primary Colors
val BackgroundBlack = Color(0xFF000000)
val SurfaceBlack = Color(0xFF0A0A0A)
val CardGray = Color(0xFF1A1A1A)
val MediumGray = Color(0xFF2A2A2A)

// Accent Colors
val PrimaryPurple = Color(0xFF9C27B0)
val AccentPink = Color(0xFFE91E63)
val OnlineGreen = Color(0xFF4CAF50)
val OfflineRed = Color(0xFFF44336)

// Text Colors
val White = Color(0xFFFFFFFF)
val LightGray = Color(0xFFCCCCCC)
```

## 📸 Screenshots

(Add your app screenshots here)

## 🗺️ Roadmap

- [ ] Firebase integration
- [ ] Social media login
- [ ] Advanced analytics
- [ ] Gift system
- [ ] Story feature
- [ ] Live streaming
- [ ] Multi-currency support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is proprietary software. All rights reserved.

## 👥 Team

- **Developer:** Only Care Team
- **UI/UX Design:** Only Care Design Team
- **Project Manager:** Only Care Management

## 📞 Support

For support, email support@onlycare.app or join our WhatsApp channel.

## 🙏 Acknowledgments

- Jetpack Compose team for the amazing UI framework
- Agora for the video/audio SDK
- Material Design 3 for design guidelines
- All contributors and testers

---

**Version:** 1.0.0  
**Last Updated:** November 11, 2025  
**Status:** Ready for Production 🚀
