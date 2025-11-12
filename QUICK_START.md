# Quick Start Guide

## Project Structure at a Glance

```
pong_with_learning_ai/
├── src/                         ← 👈 ALL YOUR CODE IS HERE
│   ├── game/        Main game engine
│   ├── entities/    Game objects (ball, paddles, etc.)
│   ├── network/     Online multiplayer (ngrok, Tailscale)
│   ├── physics/     Physics calculations & collisions
│   ├── ai/          AI & special effects
│   └── utils/       Constants & utilities
│
├── build/           Compiled .class files (auto-generated)
├── data/            Game saves & player data
├── docs/            Documentation & old files
└── resources/       For future assets (images, sounds)
```

## Most Important Files to Know

### 1. **Main Entry Point**
```
src/game/PingPongGame.java
├── This is where EVERYTHING happens
├── Contains game loop, rendering, AI, online logic
└── 331 KB file with all the core logic
```

### 2. **Game Objects**
```
src/entities/
├── Ball.java       - The ball
├── Paddle.java     - The paddles
├── Projectile.java - Bullets & lasers
└── PowerUpEntity.java - Special items
```

### 3. **Physics & Collisions**
```
src/physics/
├── PhysicsEngine.java      - Movement & gravity
└── CollisionDetector.java  - Collision logic
```

### 4. **Online Play (NO PORT FORWARDING!)**
```
src/network/
├── OnlineMultiplayerManager.java  - Connection handler
└── OnlineConnectionDialog.java    - Connection UI
```

### 5. **Configuration**
```
src/utils/GameConstants.java
└── All game settings in ONE place!
    (Ball speed, paddle size, AI difficulty, etc.)
```

---

## Common Tasks

### 🎮 Want to change game settings?
**Go to:** `src/utils/GameConstants.java`
- Change ball speed
- Change paddle size
- Change AI difficulty
- Change colors, etc.

### 🤖 Want to improve the AI?
**Go to:** `src/game/PingPongGame.java`
- Look for "learningAI" methods
- AI tracks player behavior
- Adjusts based on skill level

### 🌐 Want to add online features?
**Go to:** `src/network/OnlineMultiplayerManager.java`
- Supports ngrok (easiest)
- Supports Tailscale (most secure)
- Supports Direct IP (traditional)

### 💥 Want to add new power-ups?
**Go to:** `src/entities/PowerUpEntity.java`
- Define new power-up types
- Add effects in `PingPongGame.java`

### ⚡ Want to change physics?
**Go to:** `src/physics/PhysicsEngine.java`
- Ball bounce physics
- Gravity effects
- Movement calculations

---

## How to Compile & Run

### **Option 1: Compile from Source**
```bash
cd pong_with_learning_ai
javac src/game/PingPongGame.java
java -cp src game.PingPongGame
```

### **Option 2: Run from JAR**
```bash
cd pong_with_learning_ai
java -jar build/PongGame.jar
```

### **Option 3: From IDE (VS Code)**
1. Install Java Extension Pack
2. Click "Run" button in top-right
3. Or press `F5`

---

## File Organization Explained

### Why this structure?

| Principle | Benefit |
|-----------|---------|
| **Separation of Concerns** | Each folder handles one thing |
| **Easy Navigation** | Find what you need quickly |
| **Scalable** | Easy to add new features |
| **Professional** | Standard Java project layout |
| **Maintainable** | Future you will thank current you |

### Before vs After

**Before:** 😫
```
pong_with_learning_ai/
├── PingPongGame.java (331 KB monster file!)
├── Ball.java
├── Paddle.java
├── Player.java
├── PhysicsEngine.java
├── CollisionDetector.java
├── OnlineMultiplayerManager.java
├── GameConstants.java
├── ... 10 more files in the root
└── 50+ class files scattered everywhere
```

**After:** 😊
```
pong_with_learning_ai/
├── src/
│   ├── game/          (core logic)
│   ├── entities/      (game objects)
│   ├── network/       (online features)
│   ├── physics/       (physics)
│   ├── ai/            (AI features)
│   └── utils/         (helpers)
├── build/             (compiled code)
├── data/              (saves)
└── docs/              (documentation)
```

---

## Key Game Modes

| Mode | File | How It Works |
|------|------|-------------|
| **Single Player** | PingPongGame.java | You vs AI |
| **Local 2-Player** | PingPongGame.java | You vs Friend (same PC) |
| **Online** | OnlineMultiplayerManager.java | You vs Friend (any location!) |

---

## Online Play (NEW!)

### Three Ways to Connect:

#### 1️⃣ **ngrok** (EASIEST - Recommended)
- Zero port forwarding
- Works anywhere
- Free
- Takes 30 seconds to setup

#### 2️⃣ **Tailscale** (MOST SECURE)
- Private VPN network
- End-to-end encrypted
- Free
- Takes 2 minutes to setup

#### 3️⃣ **Direct IP** (TRADITIONAL)
- Manual port forwarding
- Works on same network
- More complex setup
- Fallback method

---

## Next Steps

1. **Read:** `PROJECT_STRUCTURE.md` (detailed explanation)
2. **Read:** `DIRECTORY_GUIDE.md` (file purposes)
3. **Explore:** Each folder in `src/`
4. **Modify:** `src/utils/GameConstants.java` to customize
5. **Run:** The game and test your changes!

---

## Troubleshooting

### Java files won't compile?
```
Make sure javac is installed:
  javac -version
```

### Game won't start?
```
Check that PingPongGame.java is in src/game/
Check that all .java files are in correct folders
```

### Online connection fails?
```
Check OnlineMultiplayerManager.java
Make sure you have ngrok or Tailscale installed
```

---

## Tips for Future Development

✅ Keep related code together in same folder
✅ Use clear, descriptive class names
✅ Add comments for complex logic
✅ Test after each change
✅ Use version control (git)
✅ Update documentation when adding features

---

**Questions?** Check the relevant markdown file or look at the code comments!

**Last Updated:** November 12, 2025
