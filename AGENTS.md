# CLAUDE.md — GetUpOrElse

**App:** GetUpOrElse
**Package:** com.getuporelse
**Platform:** Android (minSdk 24 / Android 7+, targetSdk latest stable)
**Language:** Kotlin · Jetpack Compose · CameraX · MediaPipe · Hilt · DataStore

---

## What This App Does

An alarm app that forces the user to complete push-ups before the alarm stops.
No snooze. No dismiss. No escape. The alarm ends only when the reps are done.

---

## Non-Negotiable Rules

These apply to every file you touch. No exceptions.

- **Never use `GlobalScope`** — use structured coroutines with proper scopes
- **Never put logic in Activities** — Activities only handle window/lifecycle flags
- **Never access repositories from Composables** — only from ViewModels
- **Never run pose analysis on the Main thread** — always offload
- **Never hardcode push-up logic into UI** — use `ExerciseDetector`
- **Never add magic numbers** — declare named constants in `core/constants/`
- **Never add internet permission** — this app is offline-only
- **Never add a dismiss/snooze/bypass** — the No Escape rule is a product requirement
- **Never implement future features** — build architecture that allows them, ship nothing early

---

## Stack at a Glance

| Concern | Tool |
|---|---|
| UI | Jetpack Compose |
| Camera | CameraX |
| Pose estimation | MediaPipe Pose Landmarker |
| DI | Hilt (manual DI acceptable for MVP) |
| Persistence | DataStore |
| Future persistence | Room (design interfaces for it now) |
| Architecture | MVVM + Clean Architecture (lightweight) |

---

## Package Structure

```
com.getuporelse/
├── alarm/
├── exercise/
├── pose/
├── camera/
├── audio/
├── ui/
│   ├── screens/
│   ├── viewmodels/
│   └── components/
├── domain/
│   ├── exercise/
│   ├── pose/
│   ├── alarm/
│   └── models/
├── data/
│   ├── repository/
│   ├── local/
│   └── mediapipe/
└── core/
    ├── util/
    ├── extensions/
    └── constants/
```

---

## Alarm Implementation

```kotlin
// Always use this — exact timing is required
alarmManager.setExactAndAllowWhileIdle(...)
```

`AlarmActivity` window flags (required):

```kotlin
window.addFlags(
    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
)
```

Alarm audio + pose tracking must run inside a **foreground service**.
The alarm must survive reboot — register a `BroadcastReceiver` for `BOOT_COMPLETED`.

---

## Exercise Abstraction — Required Interface

Every exercise must implement this. Do not skip it.

```kotlin
interface ExerciseDetector {
    fun processPose(result: PoseResult): ExerciseState
}
```

Current implementation: `PushUpDetector`
Future (do not implement yet): `SquatDetector`, `JumpingJackDetector`, `BurpeeDetector`

Each detector owns its own thresholds, constants, and state machine.

---

## Push-Up Detection Logic

Valid rep = full `UP → DOWN → UP` cycle where:
- Both arms are visible
- Shoulder descends below elbow level
- Sufficient vertical body displacement

Use tolerance-based thresholds. Do not require perfect biomechanics.

MVP anti-cheat (implement these, nothing more):
- Reject if either arm is missing from the pose
- Require a minimum motion threshold
- Ignore micro-movements that don't complete a full cycle

Do not implement: depth estimation, face verification, or advanced cheat detection.

---

## Audio — Volume Scaling

```kotlin
// Run this after every completed rep
val volume = (1.0f - (completedReps.toFloat() / targetReps.toFloat())).coerceAtLeast(0.05f)
audioManager.setVolume(volume)
```

- Alarm starts at full volume
- Volume decreases proportionally as reps complete
- Never reaches zero until the final rep is done

---

## State Management

```kotlin
// All UI state is immutable
data class AlarmUiState(
    val repCount: Int = 0,
    val targetReps: Int = 10,
    val feedback: String = "",
    val isComplete: Boolean = false,
    // ...
)

// Exposed from ViewModel as:
val uiState: StateFlow<AlarmUiState>
```

No mutable shared state. No singleton state containers.

---

## MediaPipe Integration

- All MediaPipe code lives in `data/mediapipe/`
- UI and ViewModels interact only with wrapper interfaces, never MediaPipe APIs directly
- Pose analysis runs on a background dispatcher

---

## Compose Rules

**DO:**
- Keep composables small and focused
- Hoist all state to the ViewModel
- Build stateless reusable components in `ui/components/`
- Separate screen-level composables from widget composables

**DO NOT:**
- Put any business logic inside a composable
- Call a repository from a composable
- Run or trigger pose analysis from a composable

---

## ViewModel Rules

- ViewModels call use cases and expose `StateFlow<UiState>`
- ViewModels never import or reference any `android.view.*` or `android.widget.*` class
- Keep ViewModels thin — move logic into use cases or detectors

---

## Persistence

DataStore keys to persist for MVP:
- Alarm time
- Target rep count
- Last selected exercise type

Write repository interfaces today that Room can back later. Do not implement Room now.

---

## Screens

| Screen | Purpose |
|---|---|
| `AlarmSetupScreen` | Set alarm time and rep target |
| `AlarmRingingScreen` | Alarm is firing, prompt to start |
| `ExerciseScreen` | Camera + pose + rep counter + feedback |
| `CompletionScreen` | Reps done, alarm dismissed |

---

## Naming Rules

**Classes** — descriptive nouns:
- ✅ `PushUpDetector`, `AlarmScheduler`, `PoseAnalyzer`
- ❌ `Manager`, `Helper`, `Utils`

**Functions** — verb-first:
- ✅ `scheduleAlarm()`, `analyzePose()`, `countRep()`
- ❌ `alarm()`, `pose()`, `rep()`

---

## Error Handling

Every failure path must show user feedback. Never fail silently.

| Failure | Required behavior |
|---|---|
| Camera unavailable | Show message, do not crash |
| Pose not detected | Show `"Body not fully visible"` feedback |
| Permissions denied | Show rationale screen, re-request |

---

## Build Order (follow this sequence)

1. Alarm scheduling + `BOOT_COMPLETED` restore
2. `AlarmActivity` lock screen + wake flags
3. Alarm audio (looping foreground service)
4. CameraX preview integration
5. MediaPipe wrapper + pose pipeline
6. `PushUpDetector` state machine
7. Rep counting + `ExerciseScreen` UI
8. Volume scaling + feedback strings
9. DataStore persistence
10. UI polish + `CompletionScreen`

---

## What NOT to Build

Do not implement any of these in this codebase right now:

- Multiple alarms — **do not implement, but design repository and alarm scheduling interfaces to support it**
- Snooze or emergency dismiss of any kind
- Exercise history or statistics
- Streak tracking
- Graphs or charts
- Difficulty modes
- Sound customization
- Internet connectivity of any kind
- Any analytics or telemetry

---

## Phase-by-Phase Development Plan

### Phase 1 — Alarm Core - ✅ COMPLETE
**Goal:** Alarm fires reliably. Nothing else matters until this works.

Tasks:
- [x] Create `AlarmScheduler` — wraps `AlarmManager.setExactAndAllowWhileIdle()`
- [x] Create `AlarmRepository` with DataStore — persist single alarm time and rep target
- [x] Register `BootReceiver` (`BOOT_COMPLETED`) — reschedule alarm after reboot
- [x] Create `AlarmActivity` — set window flags (wake, lock screen, keep on)
- [x] Create `AlarmForegroundService` — plays looping alarm audio
- [x] Build `AlarmRingingScreen` — "Wake up" UI, no dismiss button
- [x] Build `AlarmSetupScreen` — time picker + rep count picker
- [x] Verify: alarm fires on a locked device, screen wakes, audio plays

**Exit criterion:** Alarm fires at the scheduled time on a locked physical device.

---

### Phase 2 — Camera + Pose Pipeline - ✅ COMPLETE
**Goal:** Camera preview works and pose landmarks are flowing.

Tasks:
- [x] Integrate CameraX — preview renders in `ExerciseScreen`
- [x] Create MediaPipe wrapper in `data/mediapipe/` — `PoseAnalyzer` interface
- [x] Wire MediaPipe Pose Landmarker to CameraX frame stream
- [x] Run pose analysis on a background dispatcher
- [x] Emit `PoseResult` to ViewModel via `StateFlow`
- [x] Display raw landmark overlay on preview (debug mode)
- [x] Verify: pose landmarks visible on screen in real time at stable FPS

**Exit criterion:** Pose landmarks stream reliably from camera to UI without Main thread work.

---

### Phase 3 — Push-Up Detection - ✅ COMPLETE
**Goal:** Valid reps are counted. Cheating is rejected.

Tasks:
- [x] Implement `ExerciseDetector` interface
- [x] Implement `PushUpDetector` state machine (`UP → DOWN → UP`)
- [x] Add threshold constants to `core/constants/` (no magic numbers)
- [x] Enforce anti-cheat: both arms required, motion threshold, micro-movement rejection
- [x] Wire `PushUpDetector` into ViewModel
- [x] Display live rep counter on `ExerciseScreen`
- [x] Display pose feedback string (e.g. "Lower more", "Good rep")
- [ ] Unit test all state machine transitions
- [ ] Unit test rep rejection cases

**Exit criterion:** 10 real push-ups counted correctly. Micro-cheats rejected.

---

### Phase 4 — Volume Scaling + Completion Flow - 🚧 IN PROGRESS
**Goal:** Alarm reacts to progress and stops only when reps are done.

Tasks:
- [ ] Implement volume scaling in `AlarmForegroundService`:
  `volume = (1.0f - completedReps / targetReps).coerceAtLeast(0.05f)`
- [ ] Connect rep count from ViewModel to service volume control
- [x] Stop alarm and release audio when final rep is completed
- [ ] Build `CompletionScreen` — shown after all reps done
- [ ] Dismiss `AlarmActivity` only from `CompletionScreen`
- [ ] Unit test volume formula boundary values and clamp

**Exit criterion:** Volume drops per rep. Alarm stops only after all reps. No other exit path.

---

### Phase 5 — Persistence + Polish - 🚧 IN PROGRESS
**Goal:** App state survives restarts. UX is clear and stable.

Tasks:
- [ ] Persist alarm time, rep target, last exercise type via DataStore
- [ ] Restore alarm on boot via `BootReceiver`
- [x] Handle permission denial gracefully (camera, notifications, exact alarm)
- [x] Handle camera unavailable gracefully
- [x] Handle pose loss gracefully (feedback string, no crash)
- [ ] UI pass: `AlarmSetupScreen`, `ExerciseScreen`, `CompletionScreen`
- [ ] Confirm: `"There is no emergency dismissal."` is visible on `AlarmRingingScreen`
- [ ] Remove any debug overlays from production build

**Exit criterion:** App works end-to-end on a fresh install after reboot, with no crashes on permission denial or camera failure.

---

### Phase 6 — Testing + Release Prep
**Goal:** Core paths are tested. App is shippable.

Tasks:
- [ ] Unit tests: `PushUpDetector` state machine (all branches)
- [ ] Unit tests: rep counting (valid, rejected, edge cases)
- [ ] Unit tests: volume scaling (min clamp, full range)
- [ ] Instrumentation test: alarm fires on locked device
- [ ] Instrumentation test: `AlarmActivity` shows over lock screen
- [ ] Instrumentation test: camera opens and closes cleanly
- [ ] ProGuard / R8 rules for MediaPipe
- [ ] Final APK smoke test on physical device

**Exit criterion:** All tests pass. Alarm fires. Push-ups counted. Alarm stops. App ships.

---



**Unit test these — no exceptions:**
- `PushUpDetector` state machine (all transitions)
- Rep counting logic (valid reps, rejected micro-movements)
- Volume scaling formula (boundary values, clamp behavior)

**Instrumentation test:**
- Alarm fires at scheduled time
- `AlarmActivity` appears over lock screen
- Camera lifecycle (open/close/resume)
