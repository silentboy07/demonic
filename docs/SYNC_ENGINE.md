# Synchronization Engine Specification — DEMONIC

DEMONIC achieves millisecond playback parity across multiple distinct devices through a combination of server-side timestamping, client listener execution, and periodic drift correction.

---

## 1. Firebase Schema

```
rooms/
  {roomCode}/
    hostId: string
    videoId: string
    state: "playing" | "paused"
    position: number          // seconds at the time updatedAt was recorded
    updatedAt: ServerValue.TIMESTAMP
    videoTitle: string
    members/
      {uid}:
        name: string
        photoUrl: string
        joinedAt: ServerValue.TIMESTAMP
    messages/
      {pushId}:
        senderId: string
        senderName: string
        senderPhotoUrl: string
        text: string
        sentAt: ServerValue.TIMESTAMP
    typing/
      {uid}:
        name: string
        isTyping: boolean
```

---

## 2. Mathematical Sync Algorithm

### Rule 1: Host Writes with Server Timestamp
Only the room Host writes to `state`, `position`, `updatedAt`, and `videoId`. Every write utilizes `ServerValue.TIMESTAMP`:

$$\text{updatedAt} = \text{Firebase Server Timestamp (ms)}$$

Never use local client timestamps for playback writes.

### Rule 2: Listener-Driven Execution
All clients (both host and listeners) attach a `ValueEventListener` on `rooms/{roomCode}`. When a snapshot arrives:

1. **Calculate Server Now**:
   $$\text{serverNow} = \text{System.currentTimeMillis()} + \text{serverTimeOffset}$$
   where `serverTimeOffset` is read from `/.info/serverTimeOffset`.

2. **If State == "playing"**:
   $$\Delta t = \max(0, \text{serverNow} - \text{updatedAt})$$
   $$\text{targetPosition} = \text{position} + \frac{\Delta t}{1000.0}$$
   The client invokes:
   ```kotlin
   player.seekTo(targetPosition)
   player.play()
   ```

3. **If State == "paused"**:
   $$\text{targetPosition} = \text{position}$$
   The client invokes:
   ```kotlin
   player.seekTo(targetPosition)
   player.pause()
   ```

### Rule 3: 8-Second Periodic Drift Monitoring
Due to video buffering, network jitter, or hardware decoding differences, individual devices may slowly drift apart.

Every 8 seconds, `RoomViewModel` runs a check:

$$\text{drift} = \text{actualPlayerSecond} - \text{expectedTargetSecond}$$

- If $|\text{drift}| \le 1.5\text{s}$, no intervention is performed.
- If $|\text{drift}| > 1.5\text{s}$, the player silently executes:
  ```kotlin
  player.seekTo(expectedTargetSecond)
  ```
  The playback is **never paused** during this correction.

### Rule 4: Host Action Loopback
When the Host taps Play, Pause, Seeks on the scrubber, or loads a new YouTube URL, the local player is **never modified directly** from the button click. Instead:
1. The action writes the change to Firebase.
2. The Firebase listener fires back to all clients (including the host).
3. The host's player updates along with all other clients, preserving absolute synchronization integrity.
