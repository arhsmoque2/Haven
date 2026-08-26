# Haven ARH Runbooks & Recipes 📖

### 1. Build Lean Terminal Debug APK (Fast Local Build)
```powershell
Set-Location 'D:\ARH-GITHUB\arhsmoque2\Haven'
# Build lean 64-bit ARM terminal APK (~20-25 MB, no desktop bloat)
.\gradlew :app:assembleArm64TerminalDebug
# Output: app/build/outputs/apk/arm64Terminal/debug/app-arm64-terminal-debug.apk
```

---

### 2. Build & Sideload Release APK via Taildrop
```powershell
Set-Location 'D:\ARH-GITHUB\arhsmoque2\Haven'
# Assemble optimized, minified release APK
.\gradlew :app:assembleArm64TerminalRelease

# 1-Command Sideload to your Android phone (e.g. arh-f7) over Tailscale Taildrop:
tailscale file cp app/build/outputs/apk/arm64Terminal/release/app-arm64-terminal-release.apk arh-f7:
```

---

### 3. Remote Cloud Builder via GitHub Actions (Zero Local Load)
```powershell
# Trigger remote build on GitHub, stream logs, download APKs, and verify signatures:
python scripts/remote_apk_builder.py --type release --verify --output-dir ./build-outputs

# Or download the latest green release build:
python scripts/remote_apk_builder.py --from-latest --type release --verify --output-dir ./build-outputs
```

---

### 4. Run Maestro Automated UI & Clash Audit
```powershell
# 1. Static tag conformance check
python scripts/ci_maestro_doctor.py

# 2. Run live E2E flows against connected Android device/emulator
maestro test .maestro/01_app_launch_and_theme_audit.yaml
maestro test .maestro/03_hud_and_joypad_clash_audit.yaml
```

---

### 5. Launch Persistent `psmux` Session on Windows Dev Host
```powershell
# Start background psmux session with named socket
psmux -u -S arh-agent new-session -s arh-agent
```
