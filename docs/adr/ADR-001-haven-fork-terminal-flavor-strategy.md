# Architectural Decision Record (ADR)

# ADR-001: Fork GlassHaven/Haven as ARH Mobile Terminal Platform & Target `arm64Terminal` Lean Build Flavor

* **Status**: Accepted
* **Date**: 2026-08-26
* **Deciders**: Abdul Rahman Hilmi & Antigravity Agent
* **Repository**: `arhsmoque2/Haven` (Fork of `GlassHaven/Haven`)

---

## Context & Problem Statement

The initial custom Android terminal project (`ARH-Terminal`) was designed as a lightweight Jetpack Compose frontend for `psmux -CC` (tmux control mode). While its cryptographic, MCP, and turn-parsing modules were well-tested in isolation, real-world mobile usage revealed critical usability bottlenecks:
1. **Lack of a Native VT100 / PTY Terminal Engine**: Rendering output into a Compose `LazyColumn` broke full-screen curses/ncurses applications (`vim`, `htop`, `fzf`, interactive CLI prompts, password prompts).
2. **Soft Keyboard (IME) Inset Clashes**: Typing on mobile triggered soft keyboard insets that pushed dialogs off-screen or caused bottom sheets to disappear unexpectedly.
3. **Empty Chat State on Parser Mismatch**: The chat UI relied on rigid regex tokens from Claude Code/Codex; unparsed standard shell sessions resulted in a blank "Waiting for agent activity" screen.

Evaluating options to replace the terminal core with a mature PTY engine led to **Haven** (`GlassHaven/Haven`), an open-source Android client with:
- Full VT100/xterm PTY emulation via `termlib` (`org.connectbot.terminal`).
- Mature SSH, Mosh, SFTP, and key management.
- Robust Compose IME keyboard toolbar and touch gesture handling.
- Built-in MCP server daemon (`McpServer.kt`, `McpTools.kt`) and OSC sequence handlers (OSC 8 hyperlinks, OSC 52 clipboard, OSC 7 cwd).

However, Haven's full build is relatively heavy (~85 MB) because it packages a full PRoot Alpine Linux userland and Wayland desktop environment (`labwc`).

---

## Decision Drivers

1. **Terminal Reliability**: Real-world interactive CLI workflows require 100% ANSI/VT100 conformance, curses support, and proper PTY resizing.
2. **Mobile Keyboard Ergonomics**: The soft keyboard must not obscure active input fields or dialogs.
3. **Binary Footprint**: Daily-driver terminal usage should not carry unnecessary 60+ MB PRoot/desktop payloads.
4. **Upstream Maintainability**: Enhancements must not break future merges from upstream (`GlassHaven/Haven`).

---

## Considered Options

* **Option A: Write/Vendor PTY inside `ARH-Terminal` from scratch**: High operational cost, requires rewriting months of keyboard, selection, and terminal lifecycle code.
* **Option B: Fork Haven and Hard-Prune Unused Modules (delete RDP, VNC, Mail, Reticulum)**: Destroys Dagger/Hilt dependency injection graphs, creates permanent merge conflicts with upstream.
* **Option C: Fork Haven and Leverage the Built-In `arm64Terminal` Product Flavor**: Utilizes Haven's official build configuration (`dimension = "features"`, `terminal` flavor excluding PRoot/desktop, `arm64` ABI filter) for a ~20–25 MB lean APK with zero upstream code divergence.

---

## Decision

We adopt **Option C**:
1. **Fork Base**: Maintain `arhsmoque2/Haven` tracking `upstream` (`GlassHaven/Haven`).
2. **Target Lean Build Flavor**: Standardize all developer and daily-driver builds on the **`arm64Terminal`** flavor (`:app:assembleArm64TerminalDebug` and `:app:assembleArm64TerminalRelease`).
3. **Package & Brand Isolation**: Change `applicationId` to `com.arh.haven` and app name to `Haven ARH` so the APK can be installed side-by-side with official Haven.
4. **Targeted Extensions**:
   - Embed an **Agent Fast-Approval & Macro Rail** (`[Approve (y)]`, `[Reject (n)]`, `Ctrl+C`, `Esc`, `/plan`, `agy`, `arh-agent`) above the virtual keyboard.
   - Embed a **1-Tap Code-Block & Diff Extraction Drawer** scanning terminal scrollback for triple-backtick (```` ``` ````) snippets.
   - Retain seamless upstream compatibility (`git fetch upstream && git merge upstream/main`).

---

## Consequences

### Positive
* **Instant Production Stability**: Decouples terminal emulation and keyboard handling from experimental custom implementations.
* **60–70% Smaller Binary**: The `arm64Terminal` flavor builds an APK of ~20–25 MB without PRoot/desktop bloat.
* **Zero Merge Debt**: Core modules remain intact, allowing 1-command upstream updates.
* **Mobile Agent Ergonomics**: ARH-specific agent quick actions are integrated directly into a rock-solid terminal UI.

### Negative / Trade-offs
* Multi-module Gradle build requires initial dependency sync time.
* AGPL-3.0 copyleft license applies to modifications distributed in public APKs.
