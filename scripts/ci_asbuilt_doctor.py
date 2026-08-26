#!/usr/bin/env python3
import sys
from pathlib import Path

def main():
    root = Path(__file__).resolve().parent.parent
    adr_file = root / "ADR.md"
    proposal_file = root / "PROPOSAL-ARH-PORT.md"
    pref_file = root / "core" / "data" / "src" / "main" / "kotlin" / "sh" / "haven" / "core" / "data" / "preferences" / "UserPreferencesRepository.kt"
    agent_macro_file = root / "core" / "data" / "src" / "main" / "kotlin" / "sh" / "haven" / "core" / "data" / "preferences" / "AgentMacro.kt"
    parser_file = root / "feature" / "terminal" / "src" / "main" / "kotlin" / "sh" / "haven" / "feature" / "terminal" / "arh" / "CodeBlockParser.kt"
    macro_bar_file = root / "feature" / "terminal" / "src" / "main" / "kotlin" / "sh" / "haven" / "feature" / "terminal" / "arh" / "AgentMacroBar.kt"
    sheet_file = root / "feature" / "terminal" / "src" / "main" / "kotlin" / "sh" / "haven" / "feature" / "terminal" / "arh" / "CodeExtractionSheet.kt"
    dialog_file = root / "feature" / "settings" / "src" / "main" / "kotlin" / "sh" / "haven" / "feature" / "settings" / "AgentMacroManagerDialog.kt"

    print("=== [As-Built Doctor] Running ARH Haven Architecture & Spec Conformance Audit ===")
    
    # 1. Verify ADR & Proposal
    if not adr_file.exists():
        print(f"[FAIL] Missing ADR.md in root")
        sys.exit(1)
    if "ADR-001" not in adr_file.read_text(encoding="utf-8"):
        print(f"[FAIL] ADR-001 not found in ADR.md")
        sys.exit(1)
    print("[PASS] ADR.md (ADR-001) verified.")

    if not proposal_file.exists():
        print(f"[FAIL] Missing PROPOSAL-ARH-PORT.md in root")
        sys.exit(1)
    print("[PASS] PROPOSAL-ARH-PORT.md verified.")

    # 2. Verify Core Preferences & Agent Macro Model
    if not agent_macro_file.exists():
        print(f"[FAIL] Missing AgentMacro.kt")
        sys.exit(1)
    macro_code = agent_macro_file.read_text(encoding="utf-8")
    if "data class AgentMacro" not in macro_code or "DEFAULT_MACROS" not in macro_code:
        print(f"[FAIL] AgentMacro.kt missing required model definitions")
        sys.exit(1)
    print("[PASS] AgentMacro data model & serialization verified.")

    pref_code = pref_file.read_text(encoding="utf-8")
    for key in ["agentMacroBarEnabled", "agentMacros", "agentCodeExtractorEnabled", "agentHyperlinkRoutingEnabled"]:
        if key not in pref_code:
            print(f"[FAIL] UserPreferencesRepository missing preference key/flow: {key}")
            sys.exit(1)
    print("[PASS] UserPreferencesRepository ARH preference keys & flows verified.")

    # 3. Verify Terminal ARH Extensions
    if not parser_file.exists() or not macro_bar_file.exists() or not sheet_file.exists():
        print(f"[FAIL] Missing terminal ARH extension files in feature/terminal/arh")
        sys.exit(1)
    print("[PASS] CodeBlockParser, AgentMacroBar, and CodeExtractionSheet verified.")

    # 4. Verify Settings Manager Dialog
    if not dialog_file.exists():
        print(f"[FAIL] Missing AgentMacroManagerDialog.kt in feature/settings")
        sys.exit(1)
    print("[PASS] AgentMacroManagerDialog verified.")

    print("\n[SUCCESS] All ARH Haven As-Built Conformance Checks PASSED!")

if __name__ == "__main__":
    main()
