# Monomail Navigation, Motion & Gesture Architecture: Working Implementation & Reasoning

## 1. Executive Summary

This document details the engineering decisions, high-level reasoning summary, and complete working file implementations graduated from the **Motion Lab** prototyping environment into the production codebase of **Monomail** on branch `skray9403-patch-1`.

All implementations were verified on official **Android 15 (API 35)** running on **Google Pixel 8a hardware** (120Hz display, 1080×2400 resolution) and built remotely via GitHub Actions (`Build Playstore Debug`).

---

## 2. High-Level Reasoning Summary

### A. Dual-Element 56dp Substantial Dock Architecture
- **Problem:** A unified enclosing background container added visual clutter, squeezed touch targets to 48dp, and prevented fluid morphing between screens.
- **Reasoning:** Material 3 Expressive guidelines favor decoupled floating elements. Monomail establishes a substantial **56dp height** dual-element structure:
  - **Left Element (Navigation / Back):** Dynamic sliding tabs (Home), Reply action pill (Thread Detail), circular Back bubble (Settings), or Drafts/Attach bubble (Compose).
  - **Right Element (Action / Title):** Compose FAB (Home), Forward action pill (Thread Detail), dynamic section Title pill (Settings), or Send pill (Compose).
- **Outcome:** Clean, unified floating architecture with 44dp item targets, bold 14sp labels, and identical 56dp height across all application routes.

### B. Directional Scroll Hysteresis vs. Translation Dismissal
- **Problem:** Previous builds translated the entire dock 140dp off-screen on any scroll. When scrolling stopped, the dock popped back open abruptly, distracting reading.
- **Reasoning:** Rather than dismissing the dock on vertical scroll, scrolling down into content should contract the dock into a compact 3-icon state (`[Icon] [Icon] [Icon]`) and shrink the FAB to 46dp. Pausing or stopping reading preserves the collapsed state; the dock only regains its expanded shape when the user explicitly reverses scroll direction upward.
- **Outcome:** Stable reading flow without dock popping or layout jitter.

### C. Zero Shadow Bug & Viewport Elevation
- **Problem:** Outer container surfaces with `animateContentSize` caused Android RenderNode to cache rectangular clip paths for drop shadows, truncating shadows against the navigation bar.
- **Reasoning:** The `Surface` must maintain a strict `CircleShape` outline provider without resizing animations on the surface itself. By moving `animateContentSize` to the inner content row and elevating the dock with `.navigationBarsPadding().imePadding().padding(bottom = 20.dp)`, shadows diffuse in a 360° soft circle above the gesture bar.
- **Outcome:** Flawless circular shadow with zero rectangular clipping.

### D. Search Expansion Hoisting & Reliable Back Navigation
- **Problem:** Tapping back or swiping from the edge while searching sometimes failed to exit search or left the dock hidden, because search expansion was isolated inside `InboxSearchBar` while back handlers only cleared text focus in `InboxScreen`.
- **Reasoning:** Hoisting `isSearchExpanded` to `InboxScreen` allows a single authoritative `BackHandler` to intercept back gestures, collapse the search bar back into its capsule, clear focus, and restore the dock immediately.
- **Outcome:** 100% reliable back navigation from search to inbox with zero dead-ends.

### E. Centered Material 3 Provider Selection Dialog
- **Problem:** `ProviderSelectionDialog` was previously drawn directly into root layout boxes, pinning it to the top-left status bar and clipping dialog edges.
- **Reasoning:** Dialog flows must be wrapped in `androidx.compose.ui.window.Dialog(usePlatformDefaultWidth = false)` with centered alignment and a standard Material 3 modal scrim.
- **Outcome:** Clean centered dialog presentation with proper backdrop darkening and boundary isolation.

### F. Symmetrical Downward Physics for Compose
- **Problem:** Compose entered by sliding up from the bottom (`slideIntoContainer(Up)`), but popped via horizontal card drift (`backCardExit`), creating jarring spatial discontinuity.
- **Reasoning:** Sheet-style full-screen overlays must exit in the exact direction of arrival. `backSheetExit()` applies downward vertical translation (`slideOutVertically`) combined with scale and fade.
- **Outcome:** Perfectly symmetrical, physically natural sheet dismissal.

### G. Authentic Google Apps Avatar Vertical Slide
- **Problem:** 3D coin-flip rotations (`rotationX`) felt artificial and disconnected from modern Android platform conventions.
- **Reasoning:** Standard Google applications (Gmail, Google Drive, Google Maps) use directional vertical sliding inside a circular avatar mask to cycle accounts.
- **Outcome:** Dragging down slides the current avatar down and brings the next account in from the top; dragging up slides the current avatar up and brings the prior account in from the bottom.

### H. Material 3 Surface Scrims vs. Un-Material Blur
- **Problem:** Long-press on email threads activated `Modifier.blur(12.dp)`, washing out background text and breaking Material 3 design tokens.
- **Reasoning:** Material 3 uses tonal surface elevation and system scrims (`alpha = 0.32f`) rather than heavy Gaussian blur filters.
- **Outcome:** Crisp, responsive thread selection and bottom sheet presentations.

---

## 3. Complete Working File Mapping

| File Path | Role & Changes Applied |
|---|---|
| [`app/.../ui/navigation/ExpressivePersistentDock.kt`](file:///F:/monomail/app/src/main/java/com/shrivatsav/monomail/ui/navigation/ExpressivePersistentDock.kt) | Implements 56dp dual floating dock, `DynamicSwipeTabPill` with 1:1 swipe tracking, directional scroll collapse to compact 3-icon pill, Calm Crossfade (160ms/30ms delay/0.94x scale), and 56dp Settings/Thread action pills. |
| [`feature/inbox/.../feature/inbox/InboxScreen.kt`](file:///F:/monomail/feature/inbox/src/main/java/com/shrivatsav/monomail/feature/inbox/InboxScreen.kt) | Hoists `isSearchExpanded` state, connects unified `BackHandler`, wires avatar swipe cycling (`onCycleAccount`), removes `Modifier.blur(12.dp)`, and connects directional nested scroll. |
| [`feature/inbox/.../components/InboxSearchBar.kt`](file:///F:/monomail/feature/inbox/src/main/java/com/shrivatsav/monomail/feature/inbox/components/InboxSearchBar.kt) | Implements Google apps vertical drag avatar slider (`detectVerticalDragGestures`), connects hoisted search state, and provides smooth capsule expansion/collapse. |
| [`feature/inbox/.../components/ModalOverlay.kt`](file:///F:/monomail/feature/inbox/src/main/java/com/shrivatsav/monomail/feature/inbox/components/ModalOverlay.kt) | Wraps `ProviderSelectionDialog` in `androidx.compose.ui.window.Dialog(usePlatformDefaultWidth = false)` to center the dialog and eliminate status bar overlap. |
| [`feature/settings/.../feature/settings/SettingsScreen.kt`](file:///F:/monomail/feature/settings/src/main/java/com/shrivatsav/monomail/feature/settings/SettingsScreen.kt) | Wires directional `NestedScrollConnection` to `dockController.isScrolling` so 56dp back bubble and title pill contract inward on downward scroll and expand on reverse scroll. |
| [`app/.../ui/navigation/PredictiveBack.kt`](file:///F:/monomail/app/src/main/java/com/shrivatsav/monomail/ui/navigation/PredictiveBack.kt) | Adds `backSheetExit()` providing symmetrical downward vertical exit animation for sheet-style full-screen destinations. |
| [`app/.../ui/navigation/NavGraph.kt`](file:///F:/monomail/app/src/main/java/com/shrivatsav/monomail/ui/navigation/NavGraph.kt) | Configures `backSheetExit()` as `popExitTransition` for `"compose"` route, preserves `dockController.isScrolling` for Settings, and applies 20dp elevated bottom dock padding. |
| [`app/.../ui/motionlab/MotionLabActivity.kt`](file:///F:/monomail/app/src/main/java/com/shrivatsav/monomail/ui/motionlab/MotionLabActivity.kt) | Independent launcher activity containing the standalone interactive Motion Lab sandbox with live parameter sliders and morphing options. |

---

## 4. GitHub Actions Delivery & Verification

All changes were pushed and validated through continuous integration:

- **Git Branch:** `skray9403-patch-1`
- **Commits:**
  - `984a493`: `feat(navigation): integrate 56dp directional dock, Google avatar slide, centered dialog, and predictive transitions into production`
  - `7a3bb64`: `fix(inbox): resolve UserProfile id reference and BackHandler in InboxSearchBar`
  - `0857d4a`: `fix(inbox): hoist search bar expanded state to ensure back gesture reliably collapses search and restores dock`
  - `6c612e4`: `fix(inbox): qualify rememberSaveable in InboxScreen`
- **GitHub Actions Run:** [Build Playstore Debug #44723622453](https://github.com/skray9403/monomail/actions/runs/44723622453) (Status: **Success**, Duration: **1m48s**)
- **Hardware Verified:** Google Pixel 8a (`53081XEKB3TB96`) via ADB install
