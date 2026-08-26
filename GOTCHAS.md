# Haven ARH Gotchas & Failure Capsules 💡

This document captures proven failure capsules, Android toolchain gotchas, and architectural lessons learned from ARH-Terminal and mobile agent pairing.

---

### 1. Jetpack Compose `Modifier.testTag` Invisible to Maestro Without `testTagsAsResourceId`
* **Symptom**: Maestro E2E UI flows fail with `Assertion is false: id: <test_tag> is visible` (timing out after 30s), even though the app launches cleanly.
* **Root Cause**: Compose `Modifier.testTag(...)` sets internal Compose Semantics by default and does NOT expose `testTag` as an Android `resource-id` / `viewIdResourceName` in the OS Accessibility hierarchy.
* **Permanent Fix**: Enable `testTagsAsResourceId = true` in the root Composable's semantics in `MainActivity.kt`:
  ```kotlin
  Surface(
      modifier = Modifier
          .fillMaxSize()
          .semantics {
              @OptIn(ExperimentalComposeUiApi::class)
              testTagsAsResourceId = true
          }
  ) { ... }
  ```

---

### 2. Windows Gradle Daemon File Locking (`classes.jar`)
* **Symptom**: `FileSystemException: classes.jar: The process cannot access the file because it is being used by another process`.
* **Root Cause**: Windows file system holds open file locks on jar outputs when parallel or orphaned Gradle daemons linger.
* **Permanent Fix**: Run `.\gradlew --stop` before executing clean multi-module compilation tasks.

---

### 3. Release Signing Fails with `BadPaddingException` on Empty Env Vars
* **Symptom**: `:app:assembleArm64TerminalRelease` fails deep in AGP packaging with `BadPaddingException: Given final block not properly padded`.
* **Root Cause**: In Kotlin, `System.getenv(...) ?: fallback` evaluates to `""` (empty string) instead of fallback when env vars are defined but empty in CI, passing an empty password to the PKCS12 keystore decryptor.
* **Permanent Fix**: Check for `.isNullOrBlank()` rather than null checks, and fail fast with an actionable error message during `gradle.taskGraph.whenReady` if release tasks run without valid signing secrets.

---

### 4. `android.util.Base64` vs `java.util.Base64` in JVM Tests
* **Symptom**: `NullPointerException` during JVM unit test execution of cryptographic or token routines.
* **Root Cause**: `android.util.Base64` is an unmocked Android framework stub when running on host JVM test suites (`testDebugUnitTest`).
* **Permanent Fix**: Use standard `java.util.Base64` (available on Android API 26+ and Java 8+), which executes identically on both Android runtime and JVM host runners.

---

### 5. `android.util.Log` Mocking in Submodule Unit Tests
* **Symptom**: `RuntimeException: Method not mocked` when tests hit methods that log via `android.util.Log`.
* **Root Cause**: AGP unit tests on JVM stubs throw exceptions for unmocked framework calls by default.
* **Permanent Fix**: Add `testOptions { unitTests.isReturnDefaultValues = true }` inside `android { ... }` in submodule `build.gradle.kts` files.

---

### 6. Headless ATD Emulator Cannot Install ARM-Only APKs in CI
* **Symptom**: GitHub Actions Android emulator step fails with `INSTALL_FAILED_NO_MATCHING_ABIS`.
* **Root Cause**: CI workflows run an `x86_64` Android emulator while debug builds were restricted only to `arm64-v8a`.
* **Permanent Fix**: In the `debug` build configuration, permit `x86_64` ABI alongside `arm64-v8a`. Keep `release` restricted to `arm64-v8a` for production APK size optimization.

---

### 7. Secret Leak Scan Fails on `workflow_dispatch` Manual Runs
* **Symptom**: Normal `push` passes Gitleaks, but manual `workflow_dispatch` fails on an old dead token committed in docs days earlier.
* **Root Cause**: `gitleaks-action` scans full git history on manual triggers when no before/after diff SHA exists.
* **Permanent Fix**: Maintain `.gitleaksignore` with the exact `commit:file:rule:line` fingerprint.
