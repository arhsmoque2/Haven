# Architectural Decision Record (ADR)

# ADR-002: Autonomous Mobile AI Pair Programming Architecture — Workspace Repo Selector, Continuous Disk Journaling, Autonomous Permission Bypass & Seamless Network Roaming

* **Status**: Accepted
* **Date**: 2026-08-27
* **Deciders**: Abdul Rahman Hilmi & Antigravity Agent
* **Repository**: `arhsmoque2/Haven` (Branch: `feat/arh-terminal-port`)

---

## Context & Problem Statement

Building on ADR-001 (adopting Haven as the mobile terminal foundation), real-world remote AI pair programming workflows between mobile clients and external autonomous coding agents (Antigravity CLI `agy`, Claude Code, Kimi, Cursor) revealed five operational friction points:

1. **Permission Friction in Autonomous Loops**: The default security posture required human interactive approval for every file edit, terminal write, and shell command. During multi-step debugging loops (e.g. 50+ file edits/builds), repeated popups rendered mobile pairing impractical.
2. **Hardcoded Endpoint & Network Fragility**: The MCP server defaulted to loopback (`127.0.0.1:8730`). Mobile devices regularly switch between home Wi-Fi, cellular (5G/LTE), and VPNs (Tailscale), causing open agent connections to drop or fail to bind the new interface.
3. **Memory Pressure vs. Infinite History**: Storing 50,000–100,000 lines of terminal history in mobile RAM (~2 KB per cell row) risks Android Out-Of-Memory (OOM) process termination. Conversely, capping scrollback at 25,000 lines destroys historical context from multi-hour agent builds.
4. **Lack of Repository / Workspace Context Handshake**: Starting an agent session required manually navigating to the target project directory and explaining the repository structure, branch, and environment variables.
5. **Accidental Multi-line Execution & Terminal Input Quirks**: Pasting multi-line code blocks directly into raw terminals often executed lines prematurely before review, and moving the cursor on touch screens required awkward virtual arrow tapping.

---

## Decision Drivers

1. **Zero-Friction Autonomous Agent Loops**: Developers should have the option to grant hands-free autonomy across local, LAN, and Tailscale connections without interactive consent sheet interruptions.
2. **Dynamic Network Resilience**: The MCP agent server must seamlessly bind custom hosts/ports (Tailscale `100.x.y.z`) and automatically recover during Wi-Fi ↔ Cellular ↔ VPN transitions.
3. **Infinite Session Memory with Constant RAM**: Retain 100,000+ lines of session history with under 5 MB RAM overhead.
4. **Instant Repository Context**: 1-tap workspace selection that automatically injects `$HAVEN_TARGET_REPO` and `$HAVEN_WORKSPACE_PATH` into the shell and agent environment.
5. **Ergonomic Safety Gates**: Safe multi-line paste buffering, bracketed paste mode, double-Escape clear shortcuts, and precision tap-to-position caret placement.

---

## Architecture & Technical Solutions

```
┌──────────────────────────────────────────────────────────────────────────┐
│                               Haven ARH                                  │
├─────────────────────────┬─────────────────────────┬──────────────────────┤
│  Agent Macro Rail       │  Workspace Repo Picker  │  Streaming Journaler │
│  • [Approve] [Reject]   │  • M3 BottomSheet       │  • 10k Chunk Appends │
│  • [📁 Repo] [⚡ Env]    │  • Export $HAVEN_REPO   │  • Persistent .md    │
│  • Double-ESC Clear     │  • cd <path>            │  • < 5 MB RAM        │
├─────────────────────────┴─────────────────────────┴──────────────────────┤
│                     MCP Server & Network Engine                          │
│  • Autonomous Mode (mcpAutoApproveEnabled: true)                         │
│  • Custom Host / Tailscale IP (100.x.y.z) & Custom Port (8730..65535)    │
│  • Android ConnectivityManager Roaming Watcher (Auto-Rebind on Roam)     │
└──────────────────────────────────────────────────────────────────────────┘
```

### 1. Autonomous Agent Mode (`mcpAutoApproveEnabled`)
* Adds a global master toggle in DataStore preferences.
* When active, incoming MCP JSON-RPC requests across `DEVICE`, `LAN`, and `TUNNELED` origins are marked `trusted = true`, bypassing pairing popups and interactive consent sheets.

### 2. Custom Endpoint Host (Tailscale IP/Domain) & Port Configuration
* Exposes `mcp_custom_host` and `mcp_custom_port` preferences with an in-app settings dialog.
* `McpServer.kt` dynamically binds to the configured port and exposes Tailscale MagicDNS or CGNAT IPs (`100.64.0.0/10`).

### 3. Seamless Network Change Watcher (`onNetworkChanged`)
* Registers an Android `ConnectivityManager.NetworkCallback` watching `NET_CAPABILITY_INTERNET` and VPN interfaces.
* When transitioning from Wi-Fi to cellular data or connecting to Tailscale, the server automatically cleans stale sockets, scans available interfaces via `pickTailscaleAddress()`, and re-binds without terminating the process.

### 4. Workspace & GitHub Repository Quick-Selector (`WorkspaceRepoSelectorSheet.kt`)
* Adapted from modern Material 3 bottom-sheet patterns (`sameerasw/essentials` and `cnrture/PickerSheet`).
* Displays saved repositories with fuzzy search, branch indicators, and local directory paths.
* 1-Tap selection executes:
  ```bash
  export HAVEN_TARGET_REPO="<repo_name>"
  export HAVEN_WORKSPACE_PATH="<local_path>"
  cd "<local_path>" && git status -s
  ```

### 5. Continuous Streaming Disk Journaler (`SessionJournaler.kt`)
* Resolves the RAM vs. History dilemma by decoupling in-memory rendering from archival storage.
* The terminal renderer maintains a snappy, lightweight in-memory ring buffer (5,000 lines).
* A background streaming journaler batches incoming PTY stream text and flushes every 10,000 characters to `/data/user/0/sh.haven/files/transcripts/<session>_<date>.md`.
* Guarantees 100,000+ line crash-proof durability with under 5 MB RAM usage.

### 6. Terminal QoL Shortcuts & Safe Paste
* **Double-Escape Shortcut (`ESC ESC`)**: Pressing ESC twice within 400ms clears the active dialog draft or terminal command buffer (`Ctrl+U` / `Ctrl+C`).
* **Safe Multi-line Paste**: Enforces xterm Bracketed Paste Mode (`\u001b[200~... \u001b[201~`) and routes unverified pastes through the floating editor sandbox.
* **Tap-to-Position Cursor**: Accurate offset coordinate mapping in `FloatingTextInputDialog.kt` and `TerminalScreen.kt`.

---

## Consequences

### Positive
* **Uninterrupted Autonomy**: Developer agents on PCs/cloud can refactor, test, and commit code continuously over Tailscale without mobile confirmation bottlenecks.
* **Rock-Solid Connectivity**: Phone can roam between office Wi-Fi and mobile 5G without losing the agent bridge.
* **Infinite Session Recall**: Multi-day logs and 100k+ line compilation traces are permanently journaled to readable Markdown with 0 MB memory leak.
* **Instant Project Alignment**: Switching between repositories is reduced to a single tap.

### Trade-offs & Mitigations
* **Autonomous Mode Risk**: Disabling interactive consent means any paired agent can execute arbitrary terminal commands.
  * *Mitigation*: Autonomous mode defaults to `OFF`; clearly labeled in settings; scoped to paired clients.
* **Flash Storage Wear**: Continuous file appending.
  * *Mitigation*: Batched writes in 10,000-character chunks with write buffering minimize I/O cycles.
