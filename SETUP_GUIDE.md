# KeyAuth Android Loader - Quick Setup Guide

## 🚀 Quick Start (5 minutes)

### Step 1: Get Your KeyAuth Details

1. Go to [KeyAuth Dashboard](https://keyauth.cc/app/)
2. Login to your account
3. Select your application or create a new one
4. Note down these details:
   - **Application Name**: Visible on the main app page
   - **Owner ID**: Profile Picture → Account Settings
   - **Version**: Should match your KeyAuth app version

### Step 2: Configure the App

Open `app/src/main/java/com/keyauth/loader/config/KeyAuthConfig.kt` and replace:

```kotlin
object KeyAuthConfig {
    const val APP_NAME = "your-app-name"        // ← Replace this
    const val OWNER_ID = "your-owner-id"        // ← Replace this  
    const val APP_VERSION = "1.0"               // ← Replace this
    
    // Optional: Customize display names
    const val APP_DISPLAY_NAME = "YOUR-APP"     // ← App name shown in UI
    const val APP_DISPLAY_VERSION = "1.0.0"     // ← Version shown in UI
}
```

### Step 3: Build and Test

1. Open project in Android Studio
2. Sync project (Ctrl+Shift+O / Cmd+Shift+O)
3. Run on device or emulator
4. Test with a valid license key from your KeyAuth dashboard

## 🔧 Advanced Configuration

### Custom Domain (Optional)

If you have a custom domain for KeyAuth API:

```kotlin
const val API_BASE_URL = "https://your-domain.com/api/1.3/"
```

### UI Customization

#### Colors
Edit `app/src/main/res/values/colors.xml`:
- `primary_blue` - Main accent color
- `dark_background` - App background
- `button_background` - Login button color

#### App Logo
Replace `app/src/main/res/drawable/ic_bear_logo.xml` with your logo

#### App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_title">YOUR-APP-NAME</string>
```

## 🧪 Testing

### Test License Authentication

1. Create test license keys in your KeyAuth dashboard
2. Run the app
3. Enter a valid license key
4. Verify successful authentication

### Test Features

- ✅ License key validation
- ✅ Remember key functionality  
- ✅ Auto-login feature
- ✅ Error handling (invalid keys, network issues)
- ✅ Secure storage

## 🔒 Security Notes

- License keys are encrypted using Android Security Crypto
- Hardware ID is generated from device-specific information
- All API communication uses HTTPS
- Network security config prevents cleartext traffic

## 📱 Supported Android Versions

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## 🐛 Troubleshooting

### Common Issues

**"Initialization failed"**
- Check your APP_NAME and OWNER_ID in KeyAuthConfig.kt
- Verify internet connection
- Ensure KeyAuth API is accessible

**"Invalid license key"**
- Verify the license key exists in your KeyAuth dashboard
- Check if the license is expired or already used
- Ensure the license is for the correct application

**"Network error"**
- Check internet connectivity
- Verify API_BASE_URL is correct
- Check if your domain/IP is blocked

### Debug Mode

For debugging, check the logs in Android Studio Logcat:
- Filter by "KeyAuth" or "OkHttp" tags
- Network requests and responses are logged in debug builds

## 📞 Support

- **KeyAuth Issues**: [KeyAuth Telegram](https://t.me/keyauth)
- **KeyAuth Docs**: [docs.keyauth.cc](https://docs.keyauth.cc/)
- **Android Issues**: Create an issue in this repository

## 🎯 Next Steps

After successful setup:

1. **Customize UI** - Update colors, logo, and branding
2. **Add Features** - Implement your app's main functionality
3. **Security** - Add obfuscation for production builds
4. **Testing** - Test with various license scenarios
5. **Distribution** - Prepare for app store or direct distribution

## 📋 Checklist

- [ ] Updated KeyAuthConfig.kt with your details
- [ ] Tested with valid license key
- [ ] Customized app branding (optional)
- [ ] Tested remember key feature
- [ ] Tested auto-login feature
- [ ] Verified error handling
- [ ] Ready for your app's main features

---

**Need help?** Check the main README.md for detailed documentation or reach out for support!
