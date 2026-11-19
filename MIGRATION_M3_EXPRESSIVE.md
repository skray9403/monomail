# Migration to Jetpack Compose 1.13.0 & Material 3 Expressive 1.5.0 (Stable)

## 1. Executive Summary

This document details the architectural migration of **Monomail** to the newly released stable milestone of **Compose Material 3 version 1.5.0** and the **Jetpack Compose November '26 Release (version 1.13.0 via BOM `2026.11.01`)**.

* **Target Compose BOM:** `2026.09.00` / `compose-bom-beta:2026.11.00`
* **Target Material 3 Expressive:** `1.5.0-rc01` (Latest Google Maven milestone graduating Expressive APIs to stable)
* **Core Foundation & UI Text:** `1.12.1`
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

---

## 5. Edge-Aware Predictive Back Navigation & Card Surface Overhaul

### Problem Addressed
1. **Asymmetric Pop Jump:** Popping a drill screen used `slideIntoContainer(Start)`, causing the underlying screen (Inbox) to teleport from the left edge instead of resting underneath.
2. **Missing Compose Pop:** Popping the compose sheet fell into `else -> fadeIn()`, snapping unexpectedly.
3. **Permanent 28dp Screen Clipping & Shadow:** Every destination permanently wrapped itself in `.shadow(8.dp).clip(28.dp)`, cutting display corners at rest and burning GPU cycles on static full-screen shadow compositing.
4. **Spring Seek Inconsistency:** Seeking gestures with physics springs caused non-linear progress tracking.

### Solution Architecture (`PredictiveBack.kt`)
* **Dynamic Swipe Edge Tracking:** `TrackBackSwipeEdge()` reads `NavigationEventTransitionState.InProgress` to determine gesture direction (`EDGE_LEFT` vs `EDGE_RIGHT`).
* **Directional Drift & Delayed Fade (`backCardExit`):** Exiting cards shrink from $100\% \rightarrow 90\%$ and track the finger across the display, remaining fully opaque until the final $20\%$ of travel before sliding completely off-screen on commit.
* **Recessed Depth & Dimming (`backRevealEnter` & `revealDimModifier`):** True 1:1 parity with Android 15/16/17 Settings app: the revealed screen sits recessed in background depth at $94\%$ scale with a $28\%$ dark scrim, scaling **UP** from $94\% \rightarrow 100\%$ as the card is peeled away.
* **Zero-Cost Rest State (`BackCardSurface`):** Animates corner radius from $0\text{dp}$ at rest to $28\text{dp}$ during transit, with `graphicsLayer` elevation active strictly while moving. Fully addresses resting screen clipping and shadow overdraw.

---

## 6. Container Morphing & Expressive List Fluidity

### A. True 1:1 Android Settings Predictive Back Parity
* **The Problem:** The previous implementation used `slideOutHorizontally { width }`. In Navigation Compose's seekable transition, dragging the thumb halfway across the screen immediately translated the card by $40-50\%$ of the screen width ($\approx 450\text{px}$ offscreen). This cut the card in half, sliced off its rounded corners, and broke the floating card illusion seen in Android 15/16/17 Settings.
* **The Fix:**
  1. **Subtle Finger Drift (`driftPx = 24dp`):** In `backCardExit`, replaced full-width slide with a subtle $24\text{dp}$ touch-tracking drift in the swipe direction (`targetOffsetX = { (if (BackSwipe.edge == NavigationEvent.EDGE_RIGHT) -driftPx else driftPx) }`). The card remains comfortably centered in the viewport as a floating preview with all four $28\text{dp}$ rounded corners and elevation shadow fully visible.
  2. **Recessed Parent Depth (`RevealStartScale = 0.94f`):** The underlying screen sits recessed at $94\%$ scale with a $28\%$ dark scrim, scaling smoothly **UP** to $100\%$ while its scrim clears as the gesture progresses.
  3. **Diffused Ambient & Spot Shadow:** On API 28+, `BackCardSurface` specifies `ambientShadowColor = Color.Black.copy(0.20f)` and `spotShadowColor = Color.Black.copy(0.35f)` for soft, deep elevation during the gesture.
  4. **Delayed Fade (`FadeStartMs = 260ms` of $320\text{ms}$):** The card remains solid and opaque throughout touch seeking, fading only upon release if committed.

### B. Single Persistent Floating Dock & Cross-Screen Morphing
* **The Problem:** Previously, `InboxScreen`, `EmailDetailScreen`, and `ComposeScreen` each declared their own local bottom bars within their respective `Scaffold` composables. During predictive back gestures, Navigation Compose draws both the exiting and entering destinations simultaneously, causing both bottom bars to be visible at once (overlapping duplicate pills).
* **The Solution Architecture (`ExpressivePersistentDock` & `FloatingDockController`):**
  1. **Hoisted Outside NavHost:** Moved the dock to the application root in `NavGraph.kt` above `NavHost`. Exactly **ONE** dock instance exists in the window tree at all times, making bottom bar duplication physically impossible during back gestures.
  2. **Expressive Contextual Morphing:** Controlled via `FloatingDockController`, the dock smoothly morphs across screen modes using Material 3 Expressive spring physics (`Spring.DampingRatioNoBouncy`, `Spring.StiffnessMediumLow`):
     - **Inbox Mode:** Displays 3 segmented tabs (`Inbox`, `Sent`, `Archived`) alongside a floating Compose FAB (`[ ✏ ]`).
     - **Thread Mode (Email Detail):** Morphs into dual action pills (`[ ↩ Reply ]` filled button and `[ ➡ Forward ]` outlined button).
     - **Compose Mode:** Morphs into attachment actions (`[ 📎 Attach ]`, `[ 📄 Drafts ]`, `[ 🚀 Send ]`).
     - **Settings Mode:** Morphs into an interactive search bar pill (`[ 🔍 Search settings... ]`) with live filtering and auto-IME keyboard elevation.
     - **Hidden Mode:** Fades and collapses for auth/viewer screens.
  3. **Scroll-Collapse (Google Messages Aesthetic):** In `InboxScreen`, `listState.isScrollInProgress` drives `dockController.isScrolling`. While scrolling, text labels hide via `AnimatedVisibility` and the pill dynamically collapses to an ultra-compact icon-only pill ($200\text{dp}$ width) via `animateDpAsState` springs, expanding back to full width when scrolling settles.
  4. **Diffused Elevation Shadow:** Emulates the soft, diffused floating toolbar shadows of Google Messages using dual-layer `ambientShadowColor` (0.18f alpha) and `spotShadowColor` (0.28f alpha) in `graphicsLayer` with a subtle $1\text{dp}$ outline.

### C. List Item Fluidity (`Modifier.animateItem()`)
* **The Problem:** Archived, deleted, or bulk-managed emails snapped their row heights abruptly, and expanding/collapsing smart groups caused visual jumpiness.
* **The Fix:** Integrated `Modifier.animateItem()` on all `LazyColumn` items in `InboxScreen.kt` (`SectionHeader`, `GroupHeaderItem`, `SwipeableEmailItem`, and nested thread columns). Row removals, additions, and rearrangements now animate fluidly with Material 3 Expressive spring physics.

### C. Floating Action Dock Container Morphing
* **The Problem:** The home screen featured an expressive floating pill dock (`BottomDockBar`), but entering an email thread relegated Reply and Forward to the bottom of long email threads, losing visual continuity.
* **The Solution:**
  1. **Email Detail Screen:** Added a floating bottom action dock (`Surface` with $64\text{dp}$ height, $50\%$ pill radius, and `surfaceContainerHigh`) housing instant **Reply** and **Forward** buttons. Because its spatial location, dimensions, elevation, and background match `BottomDockBar` identically, the bottom dock visually morphs between Inbox navigation tabs and email action controls.
  2. **Settings Screen:** Added a matching floating search pill container ($56\text{dp}$ height, $50\%$ pill radius, `surfaceContainerHigh`) at the top of `SettingsHubScreen` providing real-time settings section search and filtering.
