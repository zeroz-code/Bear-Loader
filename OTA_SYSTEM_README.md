# KeyAuth Android Loader - OTA Update System

This document provides a comprehensive overview of the Over-The-Air (OTA) update system implemented in the KeyAuth Android Loader application.

## 🚀 Features Implemented

### ✅ Post-Authentication Features
- **Update Check Screen**: Modern Material Design UI showing "App version 4 available for download"
- **GitHub-based OTA System**: Fetches version information from configurable JSON endpoint
- **Variant Selection**: Choose between GL, KR, TW, and VNG variants of BearMod

### ✅ Download and Installation System
- **Automatic Download**: Downloads both APK and OBB files for selected variant
- **One-click Installation**: Automatically installs OBB files to correct Android/obb directory
- **Progress Tracking**: Separate progress indicators for APK and OBB downloads
- **SHA256 Verification**: Ensures file integrity before installation

### ✅ Permission Management
- **Smart Permissions**: Requests INSTALL_PACKAGES and WRITE_EXTERNAL_STORAGE permissions
- **Dynamic Requests**: Handles permissions either during installation or when needed
- **Clear Explanations**: Provides user-friendly permission explanations

### ✅ JSON Structure Integration
- **Flexible Parsing**: Handles nested JSON with version, build, and variants
- **Error Handling**: Robust error handling for network failures and invalid data
- **Resume Support**: Allows resuming interrupted downloads

### ✅ Technical Implementation
- **KeyAuth Integration**: Uses existing KeyAuth API v1.3 as authentication gate
- **Material Design 3**: Maintains dark theme and modern UI principles
- **Background Downloads**: Supports background downloading with notifications
- **File Management**: Proper file handling and cleanup

## 📁 Project Structure

```
app/src/main/java/com/keyauth/loader/
├── config/
│   └── KeyAuthConfig.kt              # Updated with OTA configuration
├── data/
│   ├── api/
│   │   ├── KeyAuthApiService.kt      # Existing KeyAuth API
│   │   └── OTAApiService.kt          # New OTA API service
│   ├── model/
│   │   ├── KeyAuthResponse.kt        # Existing models
│   │   └── OTAModels.kt              # New OTA data models
│   └── repository/
│       ├── KeyAuthRepository.kt      # Existing repository
│       └── OTARepository.kt          # New OTA repository
├── network/
│   └── NetworkFactory.kt             # Updated with OTA factory methods
├── ui/
│   ├── LoginActivity.kt              # Existing login
│   ├── MainActivity.kt               # Updated with update button
│   └── ota/                          # New OTA UI package
│       ├── UpdateAvailableActivity.kt
│       ├── VariantSelectionActivity.kt
│       ├── DownloadActivity.kt
│       ├── OTAViewModel.kt
│       └── adapter/
│           └── VariantAdapter.kt
└── utils/
    ├── SecurePreferences.kt          # Existing utilities
    ├── PermissionManager.kt          # New permission management
    └── APKInstaller.kt               # New APK installation utility
```

## 🎨 UI Components

### 1. Update Available Screen
- **Modern Design**: Clean Material Design 3 interface
- **Version Information**: Shows current vs available version
- **Action Buttons**: Download Update, Later, Retry options
- **Progress Indicators**: Loading states and error handling

### 2. Variant Selection Screen
- **Card-based Layout**: Each variant displayed as a selectable card
- **Regional Icons**: Visual indicators for GL, KR, TW, VNG variants
- **File Size Info**: Shows APK and OBB file sizes
- **Responsive Design**: Adapts to different screen sizes

### 3. Download Progress Screen
- **Dual Progress**: Separate tracking for APK and OBB downloads
- **Overall Progress**: Combined progress indicator
- **Status Updates**: Real-time status messages
- **Installation Flow**: Seamless transition to installation

## ⚙️ Configuration

### KeyAuthConfig.kt Updates
```kotlin
// OTA Update System Configuration
const val OTA_VERSION_ENDPOINT = "https://api.github.com/repos/your-username/your-repo/releases/latest"
const val CURRENT_VERSION = 3
const val CURRENT_BUILD = 1
val AVAILABLE_VARIANTS = listOf("GL", "KR", "TW", "VNG")
const val DEFAULT_VARIANT = "GL"
```

### Required Permissions (AndroidManifest.xml)
```xml
<!-- OTA Update Permissions -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.INSTALL_PACKAGES" 
    tools:ignore="ProtectedPermissions" />
```

## 🔧 How to Use

### 1. Setup
1. Update `OTA_VERSION_ENDPOINT` in `KeyAuthConfig.kt` with your JSON endpoint
2. Configure `CURRENT_VERSION` and `CURRENT_BUILD` to match your app
3. Ensure your JSON endpoint returns the expected structure (see `OTA_JSON_STRUCTURE.md`)

### 2. User Flow
1. User authenticates with KeyAuth license key
2. User clicks "Check for Updates" button in main screen
3. System checks for available updates
4. If update available, user selects variant (GL/KR/TW/VNG)
5. System downloads APK and OBB files with progress tracking
6. System automatically installs OBB file and prompts for APK installation
7. User completes installation through Android system installer

### 3. Testing
1. Set `CURRENT_VERSION` to a lower number than your JSON response
2. Launch app and authenticate
3. Click "Check for Updates"
4. Follow the complete flow to test all features

## 🛡️ Security Features

- **HTTPS Only**: All downloads use secure HTTPS connections
- **SHA256 Verification**: Every file is verified before installation
- **Permission Validation**: Proper permission checks before operations
- **File Isolation**: Downloads stored in app-specific directories
- **Error Recovery**: Graceful handling of network and file system errors

## 🎯 Material Design 3 Compliance

- **Dark Theme**: Consistent with existing app theme
- **Color System**: Uses accent colors and proper contrast ratios
- **Typography**: Material Design typography scale
- **Components**: Material buttons, cards, progress indicators
- **Animations**: Smooth transitions and loading states
- **Accessibility**: Proper content descriptions and touch targets

## 🔄 Update Process Flow

```
Authentication → Check Updates → Update Available → Variant Selection → Download → Install → Complete
     ↓              ↓              ↓                 ↓                ↓         ↓        ↓
KeyAuth API → OTA Endpoint → Show Update UI → Select Variant → Download Files → Install → Success
```

## 📱 Supported Android Versions

- **Minimum SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Permission Handling**: Adaptive for different Android versions
- **File Provider**: Secure file sharing for APK installation

## 🚨 Error Handling

The system handles various error scenarios:
- Network connectivity issues
- Invalid JSON responses
- File download failures
- Hash verification failures
- Insufficient storage space
- Permission denials
- Installation failures

## 📋 TODO / Future Enhancements

- [ ] Background download notifications
- [ ] Download resume after app restart
- [ ] Delta updates for smaller downloads
- [ ] Automatic update scheduling
- [ ] Update rollback functionality
- [ ] Analytics and crash reporting
- [ ] Multi-language support for OTA UI

## 🤝 Integration with Existing Code

The OTA system is designed to integrate seamlessly with the existing KeyAuth loader:

- **Non-intrusive**: Doesn't modify existing authentication flow
- **Modular**: Can be easily enabled/disabled
- **Consistent**: Follows existing code patterns and architecture
- **Extensible**: Easy to add new features and variants

## 📞 Support

For issues or questions about the OTA system:
1. Check the error logs in the app
2. Verify JSON endpoint structure
3. Ensure proper permissions are granted
4. Test with a simple JSON endpoint first
