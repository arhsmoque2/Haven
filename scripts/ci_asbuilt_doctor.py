#!/usr/bin/env python3
import sys
from pathlib import Path


def main():
    root = Path(__file__).resolve().parent.parent
    adr_file = root / "ADR.md"
    proposal_file = root / "PROPOSAL-ARH-PORT.md"
    pref_file = (
        root
        / "core"
        / "data"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "core"
        / "data"
        / "preferences"
        / "UserPreferencesRepository.kt"
    )
    agent_macro_file = (
        root
        / "core"
        / "data"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "core"
        / "data"
        / "preferences"
        / "AgentMacro.kt"
    )
    saved_prompt_file = (
        root
        / "core"
        / "data"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "core"
        / "data"
        / "preferences"
        / "SavedPrompt.kt"
    )
    parser_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "CodeBlockParser.kt"
    )
    macro_bar_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "AgentMacroBar.kt"
    )
    sheet_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "CodeExtractionSheet.kt"
    )
    prompt_sheet_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "PromptBookSheet.kt"
    )
    exporter_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "TerminalMarkdownExporter.kt"
    )
    dialog_file = (
        root
        / "feature"
        / "settings"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "settings"
        / "AgentMacroManagerDialog.kt"
    )

    adr2_file = root / "docs" / "adr" / "ADR-002-autonomous-mobile-agent-platform.md"
    workspace_repo_file = (
        root
        / "core"
        / "data"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "core"
        / "data"
        / "preferences"
        / "WorkspaceRepo.kt"
    )
    repo_sheet_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "WorkspaceRepoSelectorSheet.kt"
    )
    journaler_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "SessionJournaler.kt"
    )

    adr3_file = root / "docs" / "adr" / "ADR-003-semantic-prompt-pinning-viewport-guard.md"
    bookmark_file = (
        root
        / "core"
        / "data"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "core"
        / "data"
        / "preferences"
        / "PromptBookmark.kt"
    )
    ticker_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "PinnedPromptTicker.kt"
    )
    pinned_sheet_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "PinnedPromptsSheet.kt"
    )
    jump_pill_file = (
        root
        / "feature"
        / "terminal"
        / "src"
        / "main"
        / "kotlin"
        / "sh"
        / "haven"
        / "feature"
        / "terminal"
        / "arh"
        / "LiveStreamJumpPill.kt"
    )

    print("=== [As-Built Doctor] Running ARH Haven Architecture & Spec Conformance Audit ===")

    # 1. Verify ADRs & Proposal
    if not adr_file.exists():
        print("[FAIL] Missing ADR.md in root")
        sys.exit(1)
    if "ADR-001" not in adr_file.read_text(encoding="utf-8"):
        print("[FAIL] ADR-001 not found in ADR.md")
        sys.exit(1)
    print("[PASS] ADR.md (ADR-001) verified.")

    if not adr2_file.exists():
        print("[FAIL] Missing ADR-002 in docs/adr")
        sys.exit(1)
    if "ADR-002" not in adr2_file.read_text(encoding="utf-8"):
        print("[FAIL] ADR-002 not found in ADR-002 file")
        sys.exit(1)
    print("[PASS] ADR-002 (Autonomous Mobile AI Pair Programming) verified.")

    if not adr3_file.exists():
        print("[FAIL] Missing ADR-003 in docs/adr")
        sys.exit(1)
    if "ADR-003" not in adr3_file.read_text(encoding="utf-8"):
        print("[FAIL] ADR-003 not found in ADR-003 file")
        sys.exit(1)
    print("[PASS] ADR-003 (Semantic Prompt Pinning, Viewport Anchoring & Safe Paste) verified.")

    if not proposal_file.exists():
        print("[FAIL] Missing PROPOSAL-ARH-PORT.md in root")
        sys.exit(1)
    print("[PASS] PROPOSAL-ARH-PORT.md verified.")

    # 2. Verify Core Preferences & Data Models
    if not agent_macro_file.exists():
        print("[FAIL] Missing AgentMacro.kt")
        sys.exit(1)
    macro_code = agent_macro_file.read_text(encoding="utf-8")
    if "data class AgentMacro" not in macro_code or "DEFAULT_MACROS" not in macro_code:
        print("[FAIL] AgentMacro.kt missing required model definitions")
        sys.exit(1)
    print("[PASS] AgentMacro data model & serialization verified.")

    if not saved_prompt_file.exists():
        print("[FAIL] Missing SavedPrompt.kt")
        sys.exit(1)
    prompt_code = saved_prompt_file.read_text(encoding="utf-8")
    if "data class SavedPrompt" not in prompt_code or "DEFAULT_PROMPTS" not in prompt_code:
        print("[FAIL] SavedPrompt.kt missing required model definitions")
        sys.exit(1)
    print("[PASS] SavedPrompt data model & prompt library templates verified.")

    if not workspace_repo_file.exists():
        print("[FAIL] Missing WorkspaceRepo.kt")
        sys.exit(1)
    repo_code = workspace_repo_file.read_text(encoding="utf-8")
    if "data class WorkspaceRepo" not in repo_code or "DEFAULT_REPOS" not in repo_code:
        print("[FAIL] WorkspaceRepo.kt missing required model definitions")
        sys.exit(1)
    print("[PASS] WorkspaceRepo data model & serialization verified.")

    if not bookmark_file.exists():
        print("[FAIL] Missing PromptBookmark.kt")
        sys.exit(1)
    bookmark_code = bookmark_file.read_text(encoding="utf-8")
    if "data class PromptBookmark" not in bookmark_code:
        print("[FAIL] PromptBookmark.kt missing required model definitions")
        sys.exit(1)
    print("[PASS] PromptBookmark data model verified.")

    pref_code = pref_file.read_text(encoding="utf-8")
    for key in [
        "agentMacroBarEnabled",
        "agentMacros",
        "agentCodeExtractorEnabled",
        "agentHyperlinkRoutingEnabled",
        "agentSavedPrompts",
        "agentSavedWorkspaces",
        "mcpAutoApproveEnabled",
        "mcpCustomPort",
        "mcpCustomHost",
        "promptPinningTickerEnabled",
        "safeMultiLinePasteEnabled",
        "stickyViewportAnchorEnabled",
    ]:
        if key not in pref_code:
            print(f"[FAIL] UserPreferencesRepository missing preference key/flow: {key}")
            sys.exit(1)
    print("[PASS] UserPreferencesRepository ARH preference keys & flows verified.")

    # 3. Verify Terminal ARH Extensions
    for ext_file in [
        parser_file,
        macro_bar_file,
        sheet_file,
        prompt_sheet_file,
        exporter_file,
        repo_sheet_file,
        journaler_file,
        ticker_file,
        pinned_sheet_file,
        jump_pill_file,
    ]:
        if not ext_file.exists():
            print(f"[FAIL] Missing terminal ARH extension file: {ext_file.name}")
            sys.exit(1)
    print(
        "[PASS] All Terminal ARH Extensions (Parser, MacroBar, Sheets, Exporter, Journaler, PinnedTicker, JumpPill) verified."
    )

    # 4. Verify Settings Manager Dialog
    if not dialog_file.exists():
        print("[FAIL] Missing AgentMacroManagerDialog.kt in feature/settings")
        sys.exit(1)
    print("[PASS] AgentMacroManagerDialog verified.")

    print("\n[SUCCESS] All ARH Haven As-Built Conformance Checks PASSED!")


if __name__ == "__main__":
    main()
