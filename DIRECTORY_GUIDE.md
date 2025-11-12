# Directory Organization Guide

## Current Structure

```
pong_with_learning_ai/
│
├── src/                          ← ALL SOURCE CODE HERE
│   ├── game/                      # Main game engine
│   │   └── PingPongGame.java     # Entry point & core logic
│   │
│   ├── entities/                  # Game objects & data
│   │   ├── Ball.java
│   │   ├── Paddle.java
│   │   ├── Player.java
│   │   ├── Projectile.java
│   │   ├── BulletProjectile.java
│   │   ├── LaserProjectile.java
│   │   └── PowerUpEntity.java
│   │
│   ├── network/                   # Online multiplayer
│   │   ├── OnlineMultiplayerManager.java
│   │   └── OnlineConnectionDialog.java
│   │
│   ├── physics/                   # Game physics
│   │   ├── PhysicsEngine.java
│   │   └── CollisionDetector.java
│   │
│   ├── ai/                        # AI & effects
│   │   └── ExplosionEffect.java
│   │
│   └── utils/                     # Utilities
│       ├── GameConstants.java
│       └── ScoreManager.java
│
├── build/                         ← COMPILED CODE
│   ├── classes/                   # .class files (auto-generated)
│   └── PongGame.jar              # Runnable JAR
│
├── data/                          ← GAME DATA
│   └── saves/
│       ├── pong_save.dat         # Player saves
│       ├── Pongus/               # Game data
│       └── datasaves/            # Legacy saves
│
├── docs/                          ← DOCUMENTATION
│   ├── README files
│   ├── Refactoring guides
│   ├── Backup files (.bak)
│   ├── Config files (package.bluej)
│   ├── Utility scripts
│   └── .vscode/                  # IDE settings
│
├── resources/                     ← ASSETS (for future use)
│   └── [images, sounds, etc.]
│
└── PROJECT_STRUCTURE.md           ← Project overview (READ THIS FIRST!)
```

## File Purposes

### Core Game (`src/game/`)
| File | Purpose |
|------|---------|
| `PingPongGame.java` | Main game engine, rendering, AI, all game logic |

### Game Objects (`src/entities/`)
| File | Purpose |
|------|---------|
| `Ball.java` | Ball physics, movement, collision response |
| `Paddle.java` | Paddle logic, collision detection, rendering |
| `Player.java` | Player data structure (name, score, stats) |
| `Projectile.java` | Base class for all projectiles |
| `BulletProjectile.java` | Bullet implementation (extends Projectile) |
| `LaserProjectile.java` | Laser implementation (extends Projectile) |
| `PowerUpEntity.java` | Power-up objects and effects |

### Online Multiplayer (`src/network/`)
| File | Purpose |
|------|---------|
| `OnlineMultiplayerManager.java` | Handles 3 connection methods (ngrok/Tailscale/DirectIP) |
| `OnlineConnectionDialog.java` | User-friendly connection UI |

### Physics & Collisions (`src/physics/`)
| File | Purpose |
|------|---------|
| `PhysicsEngine.java` | Ball movement, gravity, bouncing calculations |
| `CollisionDetector.java` | Detects and handles all collisions |

### AI & Effects (`src/ai/`)
| File | Purpose |
|------|---------|
| `ExplosionEffect.java` | Visual effects and AI behavior |

### Configuration & Utils (`src/utils/`)
| File | Purpose |
|------|---------|
| `GameConstants.java` | All game settings & constants |
| `ScoreManager.java` | Score tracking & persistence |

### Compiled Code (`build/`)
- **auto-generated, don't edit**
- Delete `build/` and rebuild with: `javac src/game/PingPongGame.java`

### Game Data (`data/`)
- Player save files
- Game state backups
- User profiles

### Documentation (`docs/`)
- Project documentation
- Refactoring notes
- Utility scripts
- IDE configuration

---

## How to Add New Features

### Add a New Game Object Type
1. Create `src/entities/MyEntity.java`
2. Extend base class if needed (e.g., `Projectile`)
3. Implement physics in `PhysicsEngine.java`
4. Implement collision in `CollisionDetector.java`

### Add a New Game Mode
1. Add constants to `src/utils/GameConstants.java`
2. Add logic to `src/game/PingPongGame.java`
3. Update UI if needed

### Add a New AI Behavior
1. Create/modify `src/ai/` files
2. Link to AI in `PingPongGame.java`

### Add Network Features
1. Modify `src/network/OnlineMultiplayerManager.java`
2. Update connection dialog if needed

---

## Compilation

```bash
# Compile all files
cd pong_with_learning_ai
javac src/game/PingPongGame.java

# This will automatically compile all dependencies
# Output goes to build/classes/
```

## Running

```bash
# From command line
java -cp src game.PingPongGame

# From JAR
java -jar build/PongGame.jar
```

---

## Quick Reference

| What do you need? | Where is it? |
|------------------|-------------|
| Change game settings | `src/utils/GameConstants.java` |
| Fix/improve physics | `src/physics/` |
| Add new entity type | `src/entities/` |
| Modify AI | `src/ai/` + `src/game/PingPongGame.java` |
| Fix online connection | `src/network/` |
| Player saves/scores | `data/saves/` |
| Old documentation | `docs/` |
| Asset files (future) | `resources/` |

---

**Last Updated:** November 12, 2025
**Organized by:** Claude Code
