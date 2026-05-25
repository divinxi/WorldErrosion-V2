# 🌍 World Erosion — Fabric Mod
### by Divinxxii

> *The world is slowly dying. Every surface block vanishes over time — terrain, builds, structures, everything. How long can you survive?*

---

## Overview

**World Erosion** is a Fabric mod for Minecraft 1.21.4 where the highest non-air block in every loaded chunk column is removed on a configurable timer. The ground beneath you literally disappears.

---

## Commands

All commands require **operator permission (level 2)**.

| Command | Description |
|---|---|
| `/erosion start <seconds>` | Start erosion with a wave every `<seconds>` seconds |
| `/erosion pause` | Pause the timer — no blocks are removed |
| `/erosion resume` | Resume from where it left off |
| `/erosion stop` | Stop erosion completely |
| `/erosion accelerate <factor%> <minSec>` | Enable accelerating erosion |

### Examples

```
/erosion start 10          → wave every 10 seconds
/erosion start 30          → wave every 30 seconds (good for survival)
/erosion accelerate 95 2   → each wave is 5% faster, minimum 2s interval
/erosion pause             → freeze the timer
/erosion resume            → continue countdown
/erosion stop              → disable entirely
```

---

## HUD Timer

A real-time countdown displays just above the hotbar:

```
Next Erosion: 6s    ← green (normal)
Next Erosion: 3s    ← red (warning — anvil sound plays)
⏸ Paused            ← grey (paused state)
(hidden)            ← when stopped
```

---

## Block Removal Rules

- Processes **only currently loaded chunks** — no force-loading, no lag from unexplored terrain
- For each X/Z column: finds the **highest non-air block** and removes it
- **No special treatment** for structures, villages, player builds, terrain — everything erodes equally
- Works in **Overworld, Nether, and End**

### Protected Blocks (never removed)

| Block |
|---|
| Bedrock |
| Command Block (all types) |
| Barrier |
| Structure Void |

---

## Optional: Accelerating Erosion

Use `/erosion accelerate <factor%> <minSec>` to make waves get progressively faster.

```
/erosion start 60
/erosion accelerate 90 3
```
→ Starts at 60s, each wave multiplies interval by 0.90, floors at 3s. Great for challenge runs!

---

## Warning Effects

At **3 seconds** before each erosion wave:
- A high-pitched anvil clang plays for all players
- The HUD timer turns **red**

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.4
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `world-erosion-1.0.0.jar` into your `mods/` folder
4. Launch Minecraft

---

## Building from Source

```bash
git clone <repo>
cd world-erosion
./gradlew build
# Output: build/libs/world-erosion-1.0.0.jar
```

Requires: Java 21, Gradle 8+

---

## Content Creator Tips (for Divinxxii 👋)

**Short-form content hooks:**
- Set to 5–10s for intense, fast-paced clips
- Accelerating erosion creates natural tension arcs — start at 60s, watch panic increase
- The anvil warning sound is very satisfying on audio

**Challenge ideas:**
- "Last to be standing on the eroded world wins"
- "Build something before erosion destroys it"
- "How deep can I dig before erosion catches me?"
- Combine with other mods (keep inventory, mobs) for chaos

---

## Compatibility

- **Minecraft:** 1.21.4
- **Loader:** Fabric 0.16.9+
- **Fabric API:** Required
- **Server-side:** Full support (clients see the HUD automatically)
- **Singleplayer:** Full support
