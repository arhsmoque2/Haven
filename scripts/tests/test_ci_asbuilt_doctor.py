import sys
from pathlib import Path

# Add scripts directory to sys.path
scripts_dir = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(scripts_dir))

import ci_asbuilt_doctor  # noqa: E402


def test_ci_asbuilt_doctor_passes_on_current_repo():
    """Verify that ci_asbuilt_doctor runs and passes cleanly on the current codebase."""
    try:
        ci_asbuilt_doctor.main()
    except SystemExit as exc:
        assert exc.code == 0 or exc.code is None, f"ci_asbuilt_doctor exited with code {exc.code}"


def test_required_arh_files_exist():
    """Verify all critical ARH mobile agent and CI files are present on disk."""
    root = scripts_dir.parent
    expected_files = [
        root / "docs" / "adr" / "ADR-002-autonomous-mobile-agent-platform.md",
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
        / "WorkspaceRepo.kt",
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
        / "WorkspaceRepoSelectorSheet.kt",
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
        / "SessionJournaler.kt",
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
        / "AgentMacroBar.kt",
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
        / "PromptBookSheet.kt",
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
        / "CodeExtractionSheet.kt",
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
        / "TerminalMarkdownExporter.kt",
        root / ".python-version",
        root / "pyproject.toml",
    ]
    for path in expected_files:
        assert path.exists(), f"Expected ARH file does not exist: {path}"
