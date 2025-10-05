<!-- Copilot instructions for Bear-Loader repository -->
# Copilot / AI coding assistant instructions

Keep guidance short and actionable. Focus on KeyAuth initialization, session management, secure storage, OTA flow, and Android-specific build/testing workflows.

- Project purpose: Android KeyAuth v1.3 loader app. Main entry points: `app/src/main/java/com/bearmod/loader/ui/LoginActivity.kt` and `MainActivity.kt`.
- Key configuration: `app/src/main/java/com/bearmod/loader/config/KeyAuthConfig.kt` — update API_BASE_URL, APP_NAME, OWNER_ID, CUSTOM_HASH when working with authentication flows.
- Network factory: `app/src/main/java/com/bearmod/loader/network/NetworkFactory.kt` — creates Retrofit/OkHttp clients. OTA uses a separate long-timeout client; OTA base URLs are dynamic.

- Critical flows & constraints (do not break):
  - KeyAuth initialization (KeyAuthRepository.initialize) MUST run before any license/auth calls. Follow the C++-style init -> response.success pattern used across `KeyAuthRepository` and `LoginActivity`.
  - Never pass stored session tokens into the `init()` call. The repo contains explicit fixes around "session not found" and token-related init failures — preserve that logic.
  - Session tokens are stored and validated via `SecurePreferences.kt`. Use `SecurePreferences` helpers for reading/writing encrypted tokens, HWID, and device registration state.

- Authentication patterns and examples:
  - See `KeyAuthRepository.initialize()`, `authenticateWithLicense()`, and `restoreSession()` for the intended lifecycle: init -> license -> checkSession.
  - HWID generation and device trust levels are handled in `KeyAuthRepository.generateHWID()` and persisted in `SecurePreferences`.
  - When handling errors that mention "session not found", clear session storage with `securePreferences.clearSessionToken()` and reset in-memory `sessionId` (see existing code for exact sequence).

- OTA and downloads:
  - OTA APIs live in `app/src/main/java/com/bearmod/loader/data/api/OTAApiService.kt` and `OTARepository.kt`.
  - Use streaming `downloadFile()` for large files and respect the verify/hash steps in `OTARepository.downloadVariant()` (SHA-256 verification).
  - OTA uses a different OkHttp client with long read timeout in `NetworkFactory.createOTAOkHttpClient()`.

- Security & storage:
  - Sensitive values use `SecurePreferences` which implements Android Keystore AES/GCM directly (not EncryptedSharedPreferences). When modifying storage, preserve the Keystore initialization and fallback behavior.
  - Keys and tokens are Base64(wrapped IV + ciphertext). Decryption/Encryption helpers live in `SecurePreferences.kt`.

- Build & test commands (Android Studio / Gradle):
  - Typical IDE flow: open in Android Studio, Sync Gradle, run on device/emulator.
  - Command-line: use Gradle wrapper from repo root on Windows PowerShell:

```powershell
./gradlew assembleDebug
./gradlew connectedAndroidTest
./gradlew test
```

- Project conventions and patterns:
  - Mixed Kotlin/Java code with AndroidX + Kotlin Coroutines. Prefer existing ViewModel + repository pattern.
  - Network calls use Retrofit + suspend functions. Wrap responses with `NetworkResult` (Success/Error/Loading) and follow existing error logging conventions.
  - UI uses viewBinding (enabled in module `build.gradle.kts`). Use Activity/Fragment binding patterns already present.

- Quick hunting tips (useful file anchors):
  - `KeyAuthRepository.kt` — core authentication rules and many critical fixes. Read before modifying auth logic.
  - `SecurePreferences.kt` — storage/encryption details; required when touching tokens/HWID.
  - `NetworkFactory.kt` — Retrofit/OkHttp creation; OTA vs KeyAuth clients differ.
  - `KeyAuthConfig.kt` — update app-specific constants here.
  - `LoginActivity.kt` — shows how initialization, auto-login, and corrupted-session recovery are wired up.

- When making changes, run these quick local checks:
  - Build the app: `./gradlew assembleDebug` (fix compile errors)
  - Run unit tests: `./gradlew test` (there are a few tests under `app/src/test`)
  - Run lint if needed: `./gradlew lint` (lint baseline is present)

- PR Guidance:
  - Keep behavioral changes to auth/session management minimal and well-tested — small diffs are easier to review.
  - If changing storage formats, include a migration in `PreferencesMigration.kt` and keep fallback behavior.
  - Document any API endpoint or config changes in `KeyAuthConfig.kt` and README.

If anything in these instructions is unclear or you'd like more detail (examples, test commands, or coverage), tell me which area to expand.
