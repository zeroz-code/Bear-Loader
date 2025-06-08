# OTA Update System JSON Structure

This document describes the JSON structure expected by the OTA update system.

## JSON Endpoint Configuration

The JSON endpoint URL is configured in `KeyAuthConfig.kt`:

```kotlin
const val OTA_VERSION_ENDPOINT = "https://api.github.com/repos/your-username/your-repo/releases/latest"
```

## Expected JSON Structure

The OTA system expects a JSON response with the following structure:

```json
{
  "version": 4,
  "build": 1,
  "variants": {
    "GL": {
      "apk": {
        "name": "BearMod_GL_v4.0.apk",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/BearMod_GL_v4.0.apk",
        "sha256": "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456",
        "size": 157286400
      },
      "obb": {
        "name": "main.1.com.bearmod.gl.obb",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/main.1.com.bearmod.gl.obb",
        "sha256": "fedcba0987654321098765432109876543210fedcba0987654321098765432109",
        "size": 1288490188
      }
    },
    "KR": {
      "apk": {
        "name": "BearMod_KR_v4.0.apk",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/BearMod_KR_v4.0.apk",
        "sha256": "b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234567a",
        "size": 161061888
      },
      "obb": {
        "name": "main.1.com.bearmod.kr.obb",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/main.1.com.bearmod.kr.obb",
        "sha256": "edcba0987654321098765432109876543210fedcba0987654321098765432109f",
        "size": 1342177280
      }
    },
    "TW": {
      "apk": {
        "name": "BearMod_TW_v4.0.apk",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/BearMod_TW_v4.0.apk",
        "sha256": "c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234567ab2",
        "size": 159383552
      },
      "obb": {
        "name": "main.1.com.bearmod.tw.obb",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/main.1.com.bearmod.tw.obb",
        "sha256": "dcba0987654321098765432109876543210fedcba0987654321098765432109fe",
        "size": 1310720000
      }
    },
    "VNG": {
      "apk": {
        "name": "BearMod_VNG_v4.0.apk",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/BearMod_VNG_v4.0.apk",
        "sha256": "d4e5f6789012345678901234567890abcdef1234567890abcdef1234567ab2c3",
        "size": 163840000
      },
      "obb": {
        "name": "main.1.com.bearmod.vng.obb",
        "url": "https://github.com/your-username/your-repo/releases/download/v4.0/main.1.com.bearmod.vng.obb",
        "sha256": "cba0987654321098765432109876543210fedcba0987654321098765432109fed",
        "size": 1395864371
      }
    }
  }
}
```

## Field Descriptions

### Root Level
- `version` (integer): The version number of the update
- `build` (integer): The build number of the update
- `variants` (object): Contains variant-specific information

### Variant Level
Each variant (GL, KR, TW, VNG) contains:
- `apk` (object): APK file information
- `obb` (object): OBB file information

### File Information (APK/OBB)
- `name` (string): The filename of the file
- `url` (string): Direct download URL for the file
- `sha256` (string): SHA256 hash for file verification
- `size` (integer, optional): File size in bytes

## Version Comparison

The system compares the remote version with the local version configured in `KeyAuthConfig.kt`:

```kotlin
const val CURRENT_VERSION = 3
const val CURRENT_BUILD = 1
```

An update is considered available if:
- Remote version > local version, OR
- Remote version == local version AND remote build > local build

## GitHub Integration

For GitHub releases, you can use the GitHub API to automatically generate this JSON structure, or create a static JSON file in your repository.

Example GitHub API endpoint:
```
https://api.github.com/repos/your-username/your-repo/releases/latest
```

## Security Considerations

1. **HTTPS Only**: All URLs should use HTTPS for secure downloads
2. **SHA256 Verification**: All files are verified using SHA256 hashes before installation
3. **File Size Validation**: Optional file size validation for additional security
4. **Permission Checks**: The app requests appropriate permissions before downloading

## Error Handling

The system handles various error scenarios:
- Network connectivity issues
- Invalid JSON structure
- Missing files or broken URLs
- Hash verification failures
- Insufficient storage space
- Permission denials

## Testing

To test the OTA system:

1. Update the `OTA_VERSION_ENDPOINT` in `KeyAuthConfig.kt`
2. Ensure your JSON endpoint returns the expected structure
3. Set `CURRENT_VERSION` to a lower number than your JSON version
4. Launch the app and navigate to the update screen
5. Test the complete download and installation flow
