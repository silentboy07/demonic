# UI & Custom Components — DEMONIC

DEMONIC features a custom dark design system built entirely with Jetpack Compose without generic template feel.

---

## 1. Design System Tokens

### Colors
- **`DemonicBackground`**: `#0A090D` — Deep obsidian near-black with subtle warmth.
- **`DemonicSurface`**: `#14111A` — Elevated container surface.
- **`DemonicSurfaceVariant`**: `#1E1927` — Secondary interactive card surface.
- **`DemonicCrimson`**: `#FF2A54` — Signature primary accent color.
- **`DemonicCrimsonDark`**: `#C70039` — Primary gradient anchor.
- **`DemonicViolet`**: `#9D4EDD` — Secondary highlight for badges and sender names.
- **`DemonicSyncTeal`**: `#00F5D4` — Cyber neon indicator for in-sync state.

---

## 2. Core Custom Components

### `VinylDisc`
- **Location**: `ui/components/VinylDisc.kt`
- **Description**: The emotional centerpiece of the room screen.
- **Features**:
  - Continuous angle interpolation using `withFrameNanos`.
  - Realistic grooved sound concentric rings drawn on `Canvas`.
  - Dual light reflection sheen sweep gradients.
  - Video thumbnail loaded dynamically into the spindle center label.
  - Smooth deceleration and persistent pause angle.

### `SyncStatusBadge`
- **Location**: `ui/components/SyncStatusBadge.kt`
- **Description**: Real-time feedback badge showing sync status.
- **Features**:
  - Animated pulsing aura via `rememberInfiniteTransition`.
  - Millisecond drift calculation display.
  - Color coded: Cyan for synced, Amber for minor drift, Red for desynced.

### `MemberAvatarRow`
- **Location**: `ui/components/MemberAvatarRow.kt`
- **Description**: Horizontally scrollable list of active listeners.
- **Features**:
  - Animated entrance transitions (`fadeIn` + `scaleIn`).
  - Host avatar badged with golden star/crown.
  - Member count chip.

### `ChatComponents`
- **Location**: `ui/components/ChatComponents.kt`
- **Description**: Real-time room chat system.
- **Features**:
  - Visually distinct bubbles for own messages (crimson gradient, right aligned) vs others' messages (obsidian card with sender avatar, left aligned).
  - Animated 3-dot wave typing indicator.
  - Auto-scrolling message list that respects user scroll position.
  - Tactile send button with spring feedback and haptics.
