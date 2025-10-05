# AGENT Guide — Duplicate Logic Analysis & Refactor Plan

This document identifies duplicate or overlapping logic in the Java/Kotlin codebase and provides a conservative, incremental refactor plan that preserves the KeyAuth behavior.

Important constraint

- Do not change KeyAuth initialization/auth sequencing or token handling semantics without tests and a migration plan. Specifically, keep `KeyAuthRepository.initialize()`, `authenticateWithLicense()`, and `restoreSession()` behavior identical unless a validated migration is provided.

Quick summary

- Risk areas: authentication/session handling, HWID generation and persistence, secure storage access patterns, OTA download/verify logic, and permission/installation utilities.

- Goal: move repeated logic into single-responsibility modules (service/adapter classes) and avoid duplicated functions across Java/Kotlin files.

High-level recommended modules (one concern per file)

- session/SessionService (Kotlin)

- Purpose: centralize session token expiry, storage, and clearing behavior (wraps `SecurePreferences`).

- API examples: `getSessionToken()`, `storeSessionToken(token, expiryMillis)`, `clearSessionToken()`, `clearCorruptedSession()`.

- Notes: Do not call KeyAuth init from this service; it is only a storage & in-memory coordination layer.

- auth/KeyAuthFacade (Kotlin)

- Purpose: thin façade over `KeyAuthRepository` used by UI/ViewModels for consistent error mapping.

- API examples: `initializeApp()`, `authenticate(license)`, `checkSession()`.

- hwid/HWIDProvider (Kotlin)

- Purpose: single implementation for HWID generation and persistence.

- API examples: `ensureStoredHWID(context): String`, `hasHWIDChanged(context): Boolean`.

- storage/SecurePrefsAdapter (Kotlin)

- Purpose: typed convenience wrapper around `SecurePreferences` to avoid call-site duplication (expiry checks, encryption fallbacks).

- API examples: `isSessionTokenValid()`, `getBoundLicenseKey()`, `setDeviceRegistered(...)`, `getDeviceTrustLevel()`.

- ota/OTAService (Kotlin)

- Purpose: encapsulate OTA flows (check, download, verify) and standardize progress events.

- util/DownloadUtils, util/HashUtils (Kotlin)

- Purpose: small shared helpers for streaming downloads and SHA-256 verification used by OTA and other download flows.

Concrete duplicate/overlap examples (from codebase)

1. Session clearing and "session not found" handling

- Present in: `KeyAuthRepository.initialize()`, `KeyAuthRepository.checkSession()`, and `LoginActivity.clearCorruptedSessionData()`.

- Problem: repeated sequence of clearing persistent token and resetting in-memory `sessionId`.

- Fix: add `SessionService.clearCorruptedSession()` and replace duplicates with calls to it. This method should call `securePreferences.clearSessionToken()` then expose a callback or allow `KeyAuthRepository` to reset its internal `sessionId` safely.

1. HWID generation/storage

- Present in: `KeyAuthRepository.generateHWID()` and `SecurePreferences` (storage only).

- Problem: generation logic lived inside repository, mixing concerns.

- Fix: extract `HWIDProvider.generatePersistentHWID()` and a `ensureStoredHWID()` wrapper that stores via `SecurePreferences.storeHWID()`.

1. SecurePreferences usage

- Present across many call sites (UI, repository, utilities).

- Problem: repeated checks and slight variations (expiry logic, decryption fallbacks).

- Fix: build `SecurePrefsAdapter` with explicit, well-tested methods so callers don't reimplement expiry checks.

1. OTA download + verification

- Present in: `OTARepository.downloadFileWithProgress()` and `verifyFileHash()`.

- Opportunity: extract small `DownloadUtils.streamWithProgress()` and `HashUtils.sha256File()` so other features can reuse them.

Safe, incremental refactor strategy

1. Add adapters/wrappers (no behavior change)

- Implement `SessionService`, `HWIDProvider`, `SecurePrefsAdapter`, `OTAService`, and `DownloadUtils` as thin delegating wrappers to the existing implementations.

- Do not remove or modify existing methods yet. This step is additive and low-risk.

1. Update call sites incrementally

- Replace duplicated call-sites in UI/ViewModels to use the new adapters.

- Example: change `LoginActivity.clearCorruptedSessionData()` to call `SessionService.clearCorruptedSession()`.

1. Move logic into services once call sites are switched

- After the UI and viewmodel callers use the adapter, safely refactor internal duplicates (e.g., switch `KeyAuthRepository` to call `HWIDProvider` for generation).

- Preserve exact side effects (order of clearing tokens, logging) so KeyAuth behavior is identical.

1. Remove duplicates and tighten APIs

- Once coverage and tests pass, remove old duplicated code and keep the centralized services.

1. Tests & verification

- Add unit tests for `SessionService`, `HWIDProvider`, and `SecurePrefsAdapter` under `app/src/test`.

- Reuse `KeyAuthRepositoryTest.kt` as a template to ensure no regressions in init -> license -> checkSession flows.

- Run `./gradlew assembleDebug` and `./gradlew test` after each incremental change.

File-level proposals (first pass)

- `app/src/main/java/com/bearmod/loader/session/SessionService.kt`

- Delegates to `SecurePreferences` and exposes `clearCorruptedSession()` and `resetInMemorySession()` helpers.

- `app/src/main/java/com/bearmod/loader/hwid/HWIDProvider.kt`

- Encapsulates `generatePersistentHWID()` logic and storage.

- `app/src/main/java/com/bearmod/loader/storage/SecurePrefsAdapter.kt`

- Typed, narrow methods for token/license/trust-level operations.

- `app/src/main/java/com/bearmod/loader/ota/OTAService.kt` and `app/src/main/java/com/bearmod/loader/util/DownloadUtils.kt`

- Wrap `OTARepository` flows and extract streaming/hash helpers.

Compilation & interoperability tips

- Keep public signatures compatible with existing call sites. Use delegating wrappers with the same method names if needed to avoid large diffs and compilation churn.

- Prefer Kotlin for new services (it interoperates with existing Java code). Use packages like `com.bearmod.loader.session`, `com.bearmod.loader.hwid`, `com.bearmod.loader.storage`, `com.bearmod.loader.ota`.

KeyAuth safety notes

- Never pass stored session tokens into `KeyAuthRepository.initialize()`; the code intentionally performs clean init without external tokens. Preserve `initialize(preserveSession: Boolean = false)` semantics.

- When detecting "session not found" or server-side session errors, always clear persistent session via `securePreferences.clearSessionToken()` and then clear in-memory `sessionId` (use `SessionService.clearCorruptedSession()` to ensure both happen).

Deliverables for the refactor (minimal first wave)

- New wrapper classes: `SessionService`, `HWIDProvider`, `SecurePrefsAdapter`, `OTAService`, `DownloadUtils`.

- Unit tests for each wrapper under `app/src/test`.

- Small migration README describing the safe sequence to switch callers to use new services.

Next steps I can implement now (pick one)

1) Implement `SessionService` and update `LoginActivity.clearCorruptedSessionData()` to call it (safe, minimal change).

2) Implement `HWIDProvider` and replace generation inside `KeyAuthRepository` (larger; requires careful tests).

3) Implement `SecurePrefsAdapter` and update a single ViewModel to use it (medium risk).

Tell me which option to implement and I will create the files, add unit tests, and run the local unit tests.
