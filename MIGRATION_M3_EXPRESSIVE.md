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

### A. True 1:1 Google Android Predictive Back Parity (Small Background Scaling Up)
* **The Problem:** The user noted with real-device screenshots that in Google's official implementation (Files/Settings), the background page is visibly small and scales up with rounded corners, while the foreground card shrinks, rounds its corners, and previews the back transition. Previously, the Inbox was not wrapped in `BackCardSurface`, so it lacked rounded corners, and the scale factor did not make the background card prominent enough.
* **The Fix:**
  1. **Background Screen Small & Scaling Up (`RevealStartScale = 0.90f`):** Updated `backRevealEnter()` with `initialScale = 0.90f` and centered `TransformOrigin(0.5f, 0.5f)`. When peeking back, the background page starts small at $90\%$ scale with $28\text{dp}$ rounded corners and dark scrim, and smoothly scales **UP** to $100\%$ while its scrim clears and corners square out.
  2. **Universal `BackCardSurface`:** Wrapped `Screen.Inbox` in `BackCardSurface(navController)` alongside all drill destinations. Every screen participates symmetrically as a card with $28\text{dp}$ radius and $8\text{dp}$ drop shadow during gesture transitions.
  3. **Subtle Finger Drift (`driftPx = 24dp`):** In `backCardExit`, replaced full-width slide with a $24\text{dp}$ touch-tracking drift in the swipe direction (`targetOffsetX = { (if (BackSwipe.edge == NavigationEvent.EDGE_RIGHT) -driftPx else driftPx) }`). The card remains comfortably centered in the viewport as a floating preview with all four corners visible.

### B. Single Persistent Floating Dock & Cross-Screen Morphing
* **The Problem:**
  1. **Two Floating Bars in Email Detail:** An inline Reply/Forward row was present inside the email `LazyColumn` item block in addition to the global floating dock, creating overlapping double bars at the bottom of emails.
  2. **Settings Showing Inbox Tabs:** Individual child screens (`InboxScreen`, `SettingsScreen`) competed to write to `dockController.mode` via `LaunchedEffect`/`DisposableEffect`. When navigating from Inbox to Settings, `InboxScreen`'s exit recomposition re-fired and overwrote the mode back to `INBOX`.
  3. **Glitching & Text Truncation (`box`, `ent`, `chived`):** During inbox scrolling, `isScrollInProgress` toggled rapidly on micro-finger pauses, while `SegmentedButton` clipped its labels during width animations.
  4. **Compose FAB Layout Shift on Scroll:** The Compose FAB previously remained visible at 64dp while the dock shrank to 180dp, causing an asymmetrical offset and awkward centering during list reading.
* **The Architectural Resolution:**
  1. **Centralized Route-Driven Mode in `NavGraph.kt`:** Completely removed `dockController.mode = ...` from all child screens. `NavGraph.kt` determines `activeDockMode` directly from `navController.currentBackStackEntry`:
     - `"inbox"` $\to$ `DockMode.INBOX`
     - `"thread/..."` $\to$ `DockMode.THREAD`
     - `"compose"` $\to$ `DockMode.COMPOSE`
     - `"settings"` (hub only) $\to$ `DockMode.SETTINGS`
     - All sub-settings (`"settings/{section}"`) and viewers $\to$ `DockMode.HIDDEN`
     - Automatically resets `dockController.isScrolling = false` on any non-inbox route so returning to Inbox always presents a clean resting state.
  2. **Eliminated Inline Duplicate Bar:** Removed the duplicate inline Reply/Forward buttons from `EmailDetailScreen.kt`, replacing them with comfortable scrolling clearance spacer ($100\text{dp}$). Exactly one dock is rendered.
  3. **Glitch-Free Scroll Hysteresis, FAB Collapse & Clean Pill:**
     - In `InboxScreen.kt`, added a $300\text{ms}$ debounce on scroll completion, preventing any flickering or bounce during active reading.
     - In `ExpressivePersistentDock.kt`, when `isCollapsed` is true, the Compose FAB smoothly scales down and fades away (`visible = mode == INBOX && !isCollapsed`).
     - The row uses `Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)`, smoothly centering the compact $180\text{dp}$ pill during scrolling with zero layout snapping or jump.
     - When collapsed, segmented tab labels are `{}` (empty), rendering pure centered 20dp icons without any text clipping (`box`, `ent`, `chived`).
  4. **Diffused Ambient Elevation Shadow:**
     - Elevated to $10\text{dp}$ with `graphicsLayer` ambient shadow ($0.22$ alpha) and spot shadow ($0.32$ alpha) on Android P+, creating Google's signature soft floating bloom.
     - Corner radius upgraded to $32\text{dp}$ (`ShapeDefaults.ExtraLarge`) for seamless parity with Google's native system predictive back cards.

### C. List Item Fluidity (`Modifier.animateItem()`)
* **The Problem:** Archived, deleted, or bulk-managed emails snapped their row heights abruptly, and expanding/collapsing smart groups caused visual jumpiness.
* **The Fix:** Integrated `Modifier.animateItem()` on all `LazyColumn` items in `InboxScreen.kt` (`SectionHeader`, `GroupHeaderItem`, `SwipeableEmailItem`, and nested thread columns). Row removals, additions, and rearrangements now animate fluidly with Material 3 Expressive spring physics.

### C. Floating Action Dock Container Morphing
* **The Problem:** The home screen featured an expressive floating pill dock (`BottomDockBar`), but entering an email thread relegated Reply and Forward to the bottom of long email threads, losing visual continuity.
* **The Solution:**
  1. **Email Detail Screen:** Added a floating bottom action dock (`Surface` with $64\text{dp}$ height, $50\%$ pill radius, and `surfaceContainerHigh`) housing instant **Reply** and **Forward** buttons. Because its spatial location, dimensions, elevation, and background match `BottomDockBar` identically, the bottom dock visually morphs between Inbox navigation tabs and email action controls.
  2. **Settings Screen:** Added a matching floating search pill container ($56\text{dp}$ height, $50\%$ pill radius, `surfaceContainerHigh`) at the top of `SettingsHubScreen` providing real-time settings section search and filtering.
