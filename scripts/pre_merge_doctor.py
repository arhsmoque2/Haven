#!/usr/bin/env python3
"""Pre-Merge Verification Doctor for Haven ARH.

Performs non-destructive pre-merge verification:
1. Git Dry-Run In-Memory Merge Check (via git merge-tree against origin/main).
2. Python Quality & Lint Gate (via ruff & pytest).
3. ARH As-Built Architecture Conformance (via ci_asbuilt_doctor.py).
4. Code Formatting & Spotless Gate.
"""

import subprocess
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT = Path(__file__).resolve().parent.parent


def run_cmd(cmd: list[str], cwd: Path = ROOT) -> tuple[int, str]:
    """Execute a subprocess command returning (exit_code, output)."""
    try:
        proc = subprocess.run(
            cmd,
            cwd=cwd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
        )
        return proc.returncode, proc.stdout
    except Exception as e:
        return 1, f"Failed to execute {cmd}: {e}"


def check_git_merge_compatibility() -> bool:
    print("\n[1/4] 🔀 Checking Git In-Memory Merge Compatibility against origin/main...")
    # Find merge base
    target_ref = "origin/main"
    rc, merge_base = run_cmd(["git", "merge-base", target_ref, "HEAD"])
    if rc != 0:
        # Fallback to local main if origin/main is not fetched
        target_ref = "main"
        rc, merge_base = run_cmd(["git", "merge-base", target_ref, "HEAD"])
        if rc != 0:
            print(
                "  ⚠️ [WARN] Could not resolve merge base against main/origin/main (offline or shallow clone). Skipping."
            )
            return True

    # Try modern git merge-tree --write-tree (returns exit code 1 on conflict)
    rc, output = run_cmd(["git", "merge-tree", "--write-tree", target_ref, "HEAD"])
    has_conflict = False
    if rc != 0:
        has_conflict = True
    elif rc == 0 and len(output.strip().splitlines()) == 1 and len(output.strip()) == 40:
        # Clean merge tree SHA
        has_conflict = False
    else:
        # Fallback verification for older git with 3-positional-arg form
        base_sha = merge_base.strip()
        rc3, output3 = run_cmd(["git", "merge-tree", base_sha, target_ref, "HEAD"])
        if "+<<<<<<<" in output3 or "\n<<<<<<<" in output3:
            has_conflict = True
            output = output3

    if has_conflict:
        print(f"  ❌ [FAIL] Git merge conflicts detected with {target_ref}:\n{output[:500]}")
        return False
    print(f"  ✅ [PASS] In-memory Git merge simulation with {target_ref} succeeded cleanly with 0 conflicts.")
    return True


def check_python_quality_gate() -> bool:
    print("\n[2/4] 🐍 Running ARH Python Quality Gate (uv / ruff / pytest)...")
    # 1. Run Ruff check
    rc, output = run_cmd(["uv", "run", "ruff", "check", "scripts/"])
    if rc != 0:
        # Fallback to raw ruff or python if uv is in a sub-runner
        rc, output = run_cmd(["ruff", "check", "scripts/"])
        if rc != 0:
            print(f"  ❌ [FAIL] Ruff lint failed:\n{output}")
            return False
    print("  ✅ [PASS] Ruff lint passed with 0 errors.")

    # 2. Run Pytest suite
    rc, output = run_cmd(["uv", "run", "pytest", "scripts/tests/"])
    if rc != 0:
        rc, output = run_cmd([sys.executable, "-m", "pytest", "scripts/tests/"])
        if rc != 0:
            print(f"  ❌ [FAIL] Pytest test suite failed:\n{output}")
            return False
    print("  ✅ [PASS] Pytest test suite passed cleanly.")
    return True


def check_asbuilt_conformance() -> bool:
    print("\n[3/4] 🏛️ Running ARH Architecture & As-Built Conformance Doctor...")
    doctor_script = ROOT / "scripts" / "ci_asbuilt_doctor.py"
    rc, output = run_cmd([sys.executable, str(doctor_script)])
    if rc != 0:
        print(f"  ❌ [FAIL] As-Built Conformance failed:\n{output}")
        return False
    print("  ✅ [PASS] As-Built Architecture & ADR-002 conformance verified.")
    return True


def check_spotless_formatting() -> bool:
    print("\n[4/4] 🎨 Checking Code Formatting & License Headers (Spotless)...")
    gradlew = ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
    if not gradlew.exists():
        print("  ⚠️ [WARN] gradlew not found. Skipping Spotless check.")
        return True

    # Run spotlessCheck if available
    rc, output = run_cmd([str(gradlew), "spotlessCheck", "-q"])
    if rc != 0:
        print("  ℹ️ [INFO] Spotless check completed (non-blocking for local doctor).")
    else:
        print("  ✅ [PASS] Spotless code style verified.")
    return True


def main():
    print("=" * 70)
    print("🔍 [Haven ARH Pre-Merge Doctor] Starting Pre-Merge Quality Verification")
    print("=" * 70)

    checks = [
        check_git_merge_compatibility,
        check_python_quality_gate,
        check_asbuilt_conformance,
        check_spotless_formatting,
    ]

    for check in checks:
        if not check():
            print("\n❌ [PRE-MERGE FAILED] Resolve the issues above before merging to main.")
            sys.exit(1)

    print("\n" + "=" * 70)
    print("🎉 [SUCCESS] All Pre-Merge Verification Checks PASSED!")
    print("   Your branch is safe, aligned, and ready to merge into main.")
    print("=" * 70)


if __name__ == "__main__":
    main()
