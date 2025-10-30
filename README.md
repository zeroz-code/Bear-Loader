# KeyAuth Android Loader

A modern Android login application implementing KeyAuth API v1.3 authentication with Material Design principles.

## Features

- 🔐 **KeyAuth API v1.3 Integration** - License key authentication (no application secret required)
- 🎨 **Modern Material Design UI** - Dark theme with clean, professional interface
- 💾 **Secure Storage** - Encrypted storage for license keys using Android Security Crypto
- 🔄 **Auto-Login Support** - Remember license keys and automatic authentication
- 📱 **Mobile-Optimized** - Responsive design for Android devices
- 🛡️ **Security Features** - Hardware ID generation and secure network communication

## Screenshots

The app features a modern dark theme login interface similar to the reference design with:
- Circular app logo with bear mascot
- License key input field with paste functionality
- Remember Key and Automatic Login checkboxes
- Material Design buttons and components
- Loading states and error handling

## Setup Instructions

### 1. KeyAuth Configuration

Before building the app, you need to configure your KeyAuth application details:

1. Visit [KeyAuth Dashboard](https://keyauth.cc/app/)
2. Create or select your application
3. Note down the following details:
   - **Application Name**: Found on the main application page
   - **Owner ID**: Click your profile picture → Account Settings
   - **Application Version**: Should match your KeyAuth dashboard version

### 2. Update Configuration

Edit `app/src/main/java/com/bearmod/loader/config/KeyAuthConfig.kt`:

```kotlin
object KeyAuthConfig {
    // Replace with your actual KeyAuth application details
    const val APP_NAME = "your-app-name"           // Your application name
    const val OWNER_ID = "your-owner-id"           // Your owner ID
    const val APP_VERSION = "1.3"                  // Your app version
    const val API_BASE_URL = "https://keyauth.win/api/1.3/"  // API endpoint
    const val APP_DISPLAY_NAME = "YOUR-APP"        // Display name in UI
    const val APP_DISPLAY_VERSION = "1.0.0"        // Display version
}
```

### 3. Build and Run

1. Open the project in Android Studio
2. Sync the project to download dependencies
3. Build and run on your device or emulator

## Project Structure

```
app/src/main/java/com/keyauth/loader/
├── config/
│   └── KeyAuthConfig.kt              # Configuration constants
├── data/
│   ├── api/
│   │   └── KeyAuthApiService.kt      # Retrofit API interface
│   ├── model/
│   │   └── KeyAuthResponse.kt        # Data models
│   └── repository/
│       └── KeyAuthRepository.kt      # Repository pattern implementation
├── network/
│   └── NetworkFactory.kt             # Network components factory
├── ui/
│   ├── LoginActivity.kt              # Login screen
│   ├── MainActivity.kt               # Main app screen
│   └── login/
│       └── LoginViewModel.kt         # Login ViewModel
└── utils/
    ├── NetworkResult.kt              # Network result wrapper
    └── SecurePreferences.kt          # Encrypted preferences
```

## Dependencies

- **Retrofit 2** - HTTP client for API communication
- **OkHttp 3** - HTTP client with logging
- **Material Components** - Material Design UI components
- **Android Security Crypto** - Encrypted SharedPreferences
- **Kotlin Coroutines** - Asynchronous programming
- **AndroidX Lifecycle** - ViewModel and LiveData

## KeyAuth API v1.3 Features

This implementation uses KeyAuth API v1.3 which includes:

- ✅ **No Application Secret Required** - Simplified authentication flow
- ✅ **License Key Authentication** - Direct license validation
- ✅ **Hardware ID Generation** - Device-specific identification
- ✅ **Session Management** - Secure session handling
- ✅ **Error Handling** - Comprehensive error responses

## Security Features

- **Encrypted Storage**: License keys are stored using Android's EncryptedSharedPreferences
- **Hardware ID**: Unique device identification using Android ID and device info
- **Network Security**: HTTPS communication with certificate pinning support
- **Input Validation**: Client-side validation for license keys
- **Session Management**: Secure session token handling

## Customization

### UI Theming

Colors can be customized in `app/src/main/res/values/colors.xml`:
- Dark background colors
- Primary blue accent
- Error and success states
- Text colors for different states

### Strings

All user-facing text is in `app/src/main/res/values/strings.xml` for easy localization.

### Logo

Replace the bear logo in `app/src/main/res/drawable/ic_bear_logo.xml` with your own app icon.

## API Endpoints

The app communicates with KeyAuth API v1.3 endpoints:

- `POST /` - Initialize application
- `POST /` - License authentication
- `POST /` - Session validation
- `POST /` - Fetch application stats
- `POST /` - Log user activity

## Error Handling

The app handles various error scenarios:

- Network connectivity issues
- Invalid license keys
- Expired licenses
- Server errors
- Initialization failures

## License

This project is provided as an example implementation. Please ensure compliance with KeyAuth's licensing terms when using in production applications.

## Support

For KeyAuth-specific issues, visit:
- [KeyAuth Documentation](https://docs.keyauth.cc/)
- [KeyAuth Dashboard](https://keyauth.cc/app/)
- [KeyAuth Telegram](https://t.me/keyauth)

For Android development issues, refer to the Android documentation or create an issue in this repository.



















<!-- PROJECT_METRICS_START -->
# Project Metrics
Generated: 2025-10-30T06:15:09.324Z

## Pull Requests
Open PRs: 0

Open PRs by age:
- <= 7 days: 0
- <= 14 days: 0
- <= 30 days: 0
- > 30 days: 0

## Merge Metrics
Mean time to merge (last 30 days): 0.00 days
Median time to merge (last 30 days): 0.00 days
Average review comments (merged PRs): 0.00

## CI Metrics
Workflow runs (last 30 days): 22
Pass rate: 77%

## Issues
Open issues: 0
Open issues by age:
- <= 7 days: 0
- <= 14 days: 0
- <= 30 days: 0
- > 30 days: 0

Stale issues (> 60 days): 0
<!-- PROJECT_METRICS_END -->

















