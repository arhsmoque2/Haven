# 📋 Proposal: ARH-Terminal Porting & Haven Integration Strategy

## 🎯 1. Executive Summary

**Objective**: Pivot the mobile terminal foundation from the experimental, custom Compose-based `ARH-Terminal` to a dedicated fork of **Haven** (`GlassHaven/Haven`), enriching it with ARH-specific agent quick-action macros, 1-tap code-block extraction, and host workflows.

### Why Pivot to Haven?
1. **True VT100 / xterm PTY Engine**: Haven uses a battle-tested terminal engine (`termlib` / `org.connectbot.terminal`) that natively handles ANSI 256/truecolor, alternate screen buffers (`vim`, `htop`, `fzf`, `tmux`, `psmux`), cursor addressing, and raw interactive byte streams.
2. **Rock-Solid Mobile IME & Touch UX**: Keyboard popping up does not displace or occlude dialogs; it includes touch selection handles, pinch-to-zoom, and responsive toolbar controls.
3. **Advanced Built-In Agent Subsystem**: Haven already contains an internal `McpServer.kt` (HTTP/SSE), `OscHandler.kt` (handling OSC 8 hyperlinks, OSC 52 clipboard, OSC 7 working directories), and consent gates.

---

## 🔍 2. As-Is vs. To-Be Architecture

```
[ BEFORE: ARH-Terminal (Custom Compose MVP) ]
   User Input ──> OutlinedTextField ──> String (\n) ──> psmux -CC (SSH)
   Output     ──> Regex Parser     ──> LazyColumn (Text chunks only, no curses/PTY)
   * Friction: Keyboard hides UI dialogs, agent parser blank on non-conforming tokens.

[ AFTER: Haven Fork (Battle-Tested Terminal + ARH Agent Layer) ]
   User Input ──> Virtual Key Rail + ARH Macro HUD ──> Native PTY / SSH Engine
   Output     ──> termlib (VT100 Engine) + OscHandler ──> Full-Screen Terminal Screen
                       │
                       ├──> 1-Tap Code-Block & Diff Copy Drawer
                       ├──> Clickable Hyperlinks (URLs & file:// paths)
                       └──> On-Device MCP Bridge for PC Agent Fleet
```

---

## 🛠️ 3. Detailed Porting & Enrichment Roadmap

### Phase 1: Rebranding & Side-by-Side Isolation (Day 1)
* **Goal**: Enable installing your custom fork on your Android device alongside official Haven without package name collisions.
* **Tasks**:
  1. Set `applicationId` to `com.arh.haven` in `app/build.gradle.kts`.
  2. Set App Name to `Haven ARH` in `res/values/strings.xml`.
  3. Ensure Android KeyStore alias namespace is unique (`arh_haven_key`).

### Phase 2: Terminal QoL & Agent Enhancements (Days 2–4)
* **1-Tap Code-Block & Snippet Extractor Drawer**:
  * *Origin*: Port concept from ARH-Terminal's [`ArtifactPreviewSheet.kt`](file:///D:/ARH-GITHUB/arhsmoque2/ARH-Terminal/app/src/main/java/com/arh/terminal/ui/components/ArtifactPreviewSheet.kt).
  * *Action*: Add a toolbar button on the terminal screen that scans the visible scrollback buffer for markdown blocks (```` ``` ```` / diffs), displaying them in a slide-up sheet with syntax-highlighted 1-tap `[Copy Code]` buttons.
* **Agent Fast-Approval & Macro Bar**:
  * *Origin*: Port [`FloatingApprovalHud.kt`](file:///D:/ARH-GITHUB/arhsmoque2/ARH-Terminal/app/src/main/java/com/arh/terminal/ui/components/FloatingApprovalHud.kt) and [`WorkflowMacrosModal.kt`](file:///D:/ARH-GITHUB/arhsmoque2/ARH-Terminal/app/src/main/java/com/arh/terminal/ui/components/WorkflowMacrosModal.kt).
  * *Action*: Add an optional expandable macro row directly above Haven's virtual keyboard with quick keys:
    * `[Approve (y+Enter)]`
    * `[Reject (n+Enter)]`
    * `[Ctrl+C]` / `[Esc]`
    * Slash commands: `/plan`, `/goal`, `agy`, `arh-agent`
* **Enhanced Hyperlink Dispatcher**:
  * Expand `OscHandler.kt` & regex scan to intercept `file:///` paths and project links, providing direct intent sharing or editor opening.

### Phase 3: Host Profiles & Dev Workflows (Days 5–6)
* Add default connection profile presets for local Windows dev host (`psmux` on Tailscale IP).
* Hook into `TerminalSessionRegistry.kt` for local agent fleet monitoring.

---

## 🔄 4. Upstream Synchronization & Maintenance Discipline

To ensure we can pull bug fixes and updates from `GlassHaven/Haven` without conflicts:

```powershell
# 1. Main branch tracks upstream exactly
git checkout main
git fetch upstream
git merge upstream/main
git push origin main

# 2. Work happens on dedicated feature branches
git checkout -b feat/arh-agent-macro-rail
# (Apply changes modularly)
git push -u origin feat/arh-agent-macro-rail
```

### Module Isolation Rule:
Keep ARH-specific additions inside isolated components (e.g., `sh.haven.feature.terminal.arh.*` or dedicated composables) and keep modifications to Haven core files (`TerminalScreen.kt`, `SelectionToolbar.kt`) as small, clean hooks.

---

## 📊 5. Feature Comparison & Porting Checklist

| Feature | Upstream Haven | ARH-Terminal | Action in Haven Fork |
| :--- | :---: | :---: | :--- |
| **VT100 / xterm PTY Core** | ✅ Full | ❌ Stub | Keep Upstream |
| **SSH / Mosh / SFTP / VNC / RDP**| ✅ Full | ⚠️ SSH Only | Keep Upstream |
| **Virtual Keyboard Toolbar** | ✅ Standard | ⚠️ Joypad | Extend with ARH Macro Rail |
| **Code Block Extraction & Copy** | ❌ None | ⚠️ Basic | **Port & Embed** |
| **1-Tap Agent Approval HUD** | ❌ None | ✅ Yes | **Port & Embed** |
| **OSC 8 Hyperlinks & OSC 52** | ✅ Full | ❌ Basic | Keep Upstream & Add URI Router |
| **On-Device MCP Server** | ✅ Full | ✅ Full | Merge custom ARH MCP tools |
