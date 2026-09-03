# Architecture & Design Patterns — DEMONIC

DEMONIC follows modern Android Architecture guidelines with unidirectional data flow (UDF), MVVM (Model-View-ViewModel), and the Repository pattern.

---

## 1. Layers Overview

### 1.1 UI Layer (Jetpack Compose)
- **Declarative Composable Functions**: Screens are pure representations of their respective `UiState` StateFlows.
- **Theme & Design Tokens**: Defined in `ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`).
- **Interactive Reusable Components**:
  - `VinylDisc`: Canvas-rendered grooved vinyl disc with continuous rotation physics.
  - `SyncStatusBadge`: Real-time status indicator with pulsing cyber animations.
  - `MemberAvatarRow`: Horizontal presence indicator with host badging.
  - `ChatComponents`: Live chat bubbles, typing indicators, and tactile input bars.
  - `DemonicButton` & `DemonicTextField`: Custom obsidian-ember controls.

### 1.2 ViewModel Layer
- **`LoginViewModel`**: Manages authentication state, Google Sign-In callbacks, and guest mode.
- **`HomeViewModel`**: Coordinates room creation (unique code generation, collision retries) and room validation.
- **`RoomViewModel`**: Hosts the complete synchronization loop, drift detection, playback control, presence, and chat event handling.

### 1.3 Data & Repository Layer
- **`AuthRepository`**: Abstracts Firebase Authentication and Google Sign-In credential exchange.
- **`RoomRepository`**: Direct interaction with Firebase Realtime Database:
  - `.info/serverTimeOffset` monitoring.
  - `ValueEventListener` for room playback state and member lists.
  - `ChildEventListener` (`onChildAdded`) for high-throughput live chat.
  - Host-only write transactions with `ServerValue.TIMESTAMP`.

### 1.4 Player Layer
- **`YouTubePlayerManager`**: Wraps `com.pierfrancescosoffritti.androidyoutubeplayer:core`. Exposes thread-safe playback commands (`play`, `pause`, `seekTo`, `loadOrCueVideo`) and emits current playback seconds via `StateFlow<Float>`.
- **`YouTubeUrlParser`**: Robust regex utility to extract 11-char video IDs from standard URLs, short links, shorts, or raw IDs.

---

## 2. Unidirectional Data Flow (UDF)

```
[User Action in Compose UI]
            │
            ▼
[ViewModel triggers Repository method]
            │
            ▼
[Repository writes to Firebase Realtime DB]
            │
            ▼
[Firebase ValueEventListener emits new snapshot]
            │
            ▼
[Repository parses Model and pushes to Flow]
            │
            ▼
[ViewModel updates StateFlow]
            │
            ▼
[Compose Recomposes UI smoothly]
```
