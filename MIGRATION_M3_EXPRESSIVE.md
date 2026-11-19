# Migration to Jetpack Compose 1.13.0 & Material 3 Expressive 1.5.0 (Stable)

## 1. Executive Summary

This document details the architectural migration of **Monomail** to the newly released stable milestone of **Compose Material 3 version 1.5.0** and the **Jetpack Compose November '26 Release (version 1.13.0 via BOM `2026.11.01`)**.

* **Target Compose BOM:** `2026.11.01` (was `2026.09.00`)
* **Target Material 3 Expressive:** `1.5.0` Stable (was `1.5.0-rc01`)
* **Core Foundation & UI Text:** Aligned to `1.13.0` (was `1.12.1`)
* **CI/CD Automation:** Configured automatic build triggers on any branch commit push.

---

## 2. Whole Thinking Process & Architectural Rationale

### A. The Milestone: Material 3 Expressive Graduates to Stable
During initial development, Monomail was an early adopter of Google's new **Material 3 Expressive** design system. Because Expressive APIs were previously pre-release (alphas, betas, and `1.5.0-rc01`), code was gated behind `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`.

With Google's release of Compose Material 3 `1.5.0`:
1. Core Expressive wrappers—notably `MaterialExpressiveTheme`, `MotionScheme`, `MotionScheme.expressive()`, and physics transition specs (`fastEffectsSpec`, `defaultSpatialSpec`)—have graduated to fully stable APIs.
2. Leaving outdated experimental annotations in production code generates false warnings, hides genuinely unstable APIs, and increases cognitive friction during maintenance and code reviews.
3. Upgrading to stable `1.5.0` ensures binary and source compatibility for long-term production use.

### B. Compose 1.13.0 Graphics & Foundation Alignment
Monomail's version catalog previously pinned `foundation` and `uiText` to `1.12.1`. Keeping them pinned to 1.12.1 while bumping the BOM caused version disparity and prevented access to Compose 1.13's new graphics engine enhancements (such as native progressive blurs via `BlurRadiusSpec` and runtime polygon morphing). Aligning foundation and text with the `2026.11.01` BOM ensures consistency across the entire UI tree.

### C. Continuous Integration Workflow Optimization
Previously:
* `ci.yml` was configured only for `pull_request` against `main`.
* `build-playstore-debug.yml` was locked specifically to the `skray9403-patch-1` branch.

**The Problem:** Pushing commits to new feature branches or `main` would fail to trigger automatic verification builds, requiring manual dispatch or PR creation to see if an APK builds cleanly.
**The Fix:** We expanded workflow triggers (`on: push: branches: ['**']`), guaranteeing that every push immediately executes compilation and regression checks, providing rapid feedback.

---

## 3. Detailed Changes Implemented

### 1. Version Catalog Updates (`gradle/libs.versions.toml`)
* `composeBom`: Upgraded from `2026.09.00` to `2026.11.01`.
* `material3Expressive`: Promoted from `1.5.0-rc01` to `1.5.0` (Stable).
* `foundation`: Bumped from `1.12.1` to `1.13.0`.
* `uiText`: Bumped from `1.12.1` to `1.13.0`.

### 2. Design System & Theme Modernization (`core/designsystem/.../Theme.kt`)
* Removed `import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi`.
* Removed `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` from `MonoMailTheme`.
* `MaterialExpressiveTheme` and `MotionScheme.expressive()` now compile directly as stable top-level APIs.

### 3. Navigation Transition Cleanups (`app/.../NavGraph.kt`)
* Removed `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` from:
  * `ReauthDialog`
  * `slideInForRoute`
  * `slideOutForRoute`
  * `popEnterForRoute`
  * `popExitForRoute`
* All transition animations now cleanly consume `motionScheme.fastEffectsSpec()` without opt-ins.

### 4. GitHub Actions Workflows (`.github/workflows/`)
* **`ci.yml`**: Added `push:` trigger to run Android lint, unit tests, and build assemble steps across all branches.
* **`build-playstore-debug.yml`**: Updated `push.branches` to `['**']` so that pushing commits to any active branch automatically triggers the Play Store debug build and packages the resulting APK artifact.

---

## 4. Handoff & Maintenance Guide

For maintainers touching Monomail after this migration:

1. **Building Locally:**
   Ensure JDK 17+ is configured (`JAVA_HOME`).
   ```bash
   ./gradlew assemblePlaystoreDebug
   ```
2. **Where Experimental APIs Remain:**
   `LoadingIndicator` (in `ShimmerEffect.kt` and `NavGraph.kt`) is one of the few APIs still requiring `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`. Do not remove the opt-in on those specific composables until Google stabilizes the loading indicator API in future releases.
3. **Future UI Enhancements to Leverage:**
   * **Native Progressive Blur:** The mail detail screen and reading view can now use `Modifier.blur` with `BlurRadiusSpec.verticalGradient` directly, replacing manual gradient overlays.
   * **Segmented Lists:** Settings rows can adopt `SegmentedListItem` with `ListItemDefaults.segmentedShapes` for modern rounded card grouping.
