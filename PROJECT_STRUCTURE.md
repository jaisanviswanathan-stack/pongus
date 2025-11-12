# Pong with Learning AI - Project Structure

## Overview

This is an advanced Pong game with AI learning capabilities, power-ups, online multiplayer, and special effects. The project has been reorganized into a clean, modular structure for easier navigation and maintenance.

## Directory Structure

```
pong_with_learning_ai/
├── src/                          # All source code
│   ├── game/                      # Core game logic
│   │   └── PingPongGame.java     # Main game engine & entry point
│   │
│   ├── entities/                  # Game objects
│   │   ├── Ball.java             # Ball physics and rendering
│   │   ├── Paddle.java           # Paddle logic and collision
│   │   ├── Player.java           # Player data (scores, stats)
│   │   ├── Projectile.java       # Base projectile class
│   │   ├── BulletProjectile.java # Bullet implementation
│   │   ├── LaserProjectile.java  # Laser implementation
│   │   └── PowerUpEntity.java    # Power-up objects
│   │
│   ├── network/                   # Online multiplayer
│   │   ├── OnlineMultiplayerManager.java  # Connection management (ngrok, Tailscale, Direct IP)
│   │   └── OnlineConnectionDialog.java    # Connection setup UI
│   │
│   ├── physics/                   # Physics & collisions
│   │   ├── PhysicsEngine.java    # Physics calculations
│   │   └── CollisionDetector.java # Collision detection logic
│   │
│   ├── ai/                        # AI system
│   │   └── ExplosionEffect.java  # AI effects (explosions)
│   │
│   └── utils/                     # Utilities & constants
│       ├── GameConstants.java    # Game constants & config
│       ├── ScoreManager.java     # Score tracking
│       └── GameState.class       # Game state management
│
├── build/                         # Compiled files
│   ├── classes/                   # .class files (auto-generated)
│   └── PongGame.jar              # Executable JAR
│
├── data/                          # Game data
│   ├── saves/                     # Save files
│   │   └── pong_save.dat         # Player save data
│   ├── Pongus/                    # Additional data
│   └── datasaves/                 # Legacy save data
│
├── docs/                          # Documentation & configs
│   ├── README.TXT                # Original readme
│   ├── QUICK_REFERENCE.md        # Quick reference guide
│   ├── REFACTORING_*.md          # Refactoring notes
│   ├── package.bluej             # BlueJ project config
│   ├── .vscode/                  # VS Code settings
│   └── [scripts & backups]       # Utility scripts & backups
│
└── resources/                     # Game assets (images, sounds)
    └── [assets go here]
```

## Core Components

### 1. **Game Loop** (`src/game/PingPongGame.java`)
- Main game engine handling all logic
- Manages game state, rendering, and updates
- Supports single-player, local multiplayer, and online modes
- Features:
  - Learning AI that adapts to player skill
  - Power-ups with special effects
  - Cheat mode system
  - Online matchmaking

### 2. **Entities** (`src/entities/`)
Game objects with behavior:
- **Ball.java** - Physics, collision detection, rendering
- **Paddle.java** - Player controlled or AI controlled
- **Player.java** - Player stats and information
- **Projectile.java** - Bullet and laser system

### 3. **Online Multiplayer** (`src/network/`)
Connect players across different networks:
- **OnlineMultiplayerManager.java**
  - ngrok relay (no port forwarding needed)
  - Tailscale VPN (secure mesh network)
  - Direct IP (traditional port forwarding)
  - Automatic connection method detection

- **OnlineConnectionDialog.java**
  - User-friendly connection UI
  - Setup instructions for each method
  - Join/Host options

### 4. **Physics** (`src/physics/`)
Realistic game mechanics:
- **PhysicsEngine.java** - Ball movement, gravity, bouncing
- **CollisionDetector.java** - Paddle-ball, wall collisions

### 5. **AI System** (`src/ai/`)
Learning AI that adapts:
- Tracks player behavior patterns
- Adjusts difficulty based on player skill
- Learning modes (Normal, Hard, Impossible, Learning AI)

### 6. **Utilities** (`src/utils/`)
Supporting systems:
- **GameConstants.java** - All game configuration values
- **ScoreManager.java** - Score tracking and persistence
- **GameState.class** - Game state management

## How to Compile

```bash
cd src
javac game/PingPongGame.java
```

All referenced classes will be compiled automatically.

## How to Run

```bash
java -cp src game.PingPongGame
```

Or use the JAR file:
```bash
java -jar build/PongGame.jar
```

## File Organization Tips

### Adding New Features
1. **New entity type?** → `src/entities/`
2. **New physics behavior?** → `src/physics/`
3. **New AI behavior?** → `src/ai/`
4. **New UI screen?** → Create appropriate package
5. **New game mode?** → `src/game/` or new package

### Naming Conventions
- Classes: `PascalCase` (e.g., `PhysicsEngine.java`)
- Methods: `camelCase` (e.g., `calculateCollision()`)
- Constants: `UPPER_SNAKE_CASE` (in `GameConstants.java`)
- Packages: `lowercase` with dots (e.g., `game.physics`)

## Key Features

### Game Modes
- **Single Player** - Play against AI
- **Local Multiplayer** - Two players on same computer
- **Online Multiplayer** - Play across internet (no port forwarding needed!)

### AI Difficulty Levels
1. **Normal** - Basic AI
2. **Hard** - Advanced strategies
3. **Impossible** - Unbeatable AI
4. **Learning AI** - Adapts to your play style

### Power-ups & Effects
- Danger Zone
- Gravity & Physics Effects
- Paddle Modifiers
- Special Effects (Mirror, Teleport, Fireball, etc.)

### Online Connection Methods
- **ngrok** - Relay server, easiest setup
- **Tailscale** - VPN, most secure
- **Direct IP** - Traditional port forwarding

## Version History
- **v1.0** - Original Pong
- **v156** (latest) - Full learning AI, online multiplayer, power-ups

## Future Improvements
- Mobile companion app
- Tournament mode
- Replay system
- Advanced analytics
- Custom AI personality presets

---

**Last Updated:** November 12, 2025
