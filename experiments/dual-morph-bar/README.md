# Dual Morph Bar Lab

An isolated Compose test app for the scroll-reactive two-action bar inspired by the supplied Google Messages recording.

## What to test

- Scroll the inbox upward: Search expands while Compose contracts and exchanges positions.
- Scroll downward: Compose expands while Search contracts and exchanges positions.
- Tap either action to pin that action as expanded.
- Watch width, position, corner radius, elevation, label visibility and container tint animate together.

## Implementation

The demo intentionally uses ordinary Jetpack Compose and Material 3 building blocks:

- Material 3 `Surface` for each action container
- `NestedScrollConnection` for direction detection
- `animateDpAsState` for width, position, shape and elevation
- `animateFloatAsState` and Compose color interpolation for the subtle container tint
- `AnimatedVisibility` with fade/expand transitions for labels

No Canvas, custom drawing, Android Views, third-party animation libraries or custom render loop.

## Build

From the repository root:

```bash
./gradlew -p experiments/dual-morph-bar :app:assembleDebug
```

Install the resulting APK from:

```
experiments/dual-morph-bar/app/build/outputs/apk/debug/app-debug.apk
```

This experiment lives on `experiment/messages-dual-morph-bar` and does not change MonoMail's production app modules or default branch.
