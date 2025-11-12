# How to Run Your Pong Game

## Quick Start

```bash
cd c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai
javac *.java
java PingPongGame
```

## Step-by-Step Instructions

### Step 1: Open Command Prompt
Press `Win + R`, type `cmd`, and press Enter

### Step 2: Navigate to Project Folder
```bash
cd c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai
```

### Step 3: Compile the Game
```bash
javac *.java
```
(This creates .class files from your .java source files)

### Step 4: Run the Game
```bash
java PingPongGame
```

A window will appear with the game launcher!

---

## Game Features

### Game Modes
- **Single Player** - Play against AI with selectable difficulty
- **Local Multiplayer** - Play against a friend on the same computer
- **Online Multiplayer** - Play against a friend over the internet (NO port forwarding needed!)

### AI Difficulty Levels
- **Normal** - Good challenge
- **Hard** - Fast and smart AI
- **Impossible** - BRUTAL (150% faster)
- **Learning AI** - Adapts to your play style!

### Features
- Power-ups with special effects
- Gravity and physics effects
- Paddle modifiers
- Special visual effects
- Score tracking
- Game saves

---

## Troubleshooting

### Error: "javac is not recognized"
**Solution:** Java is not installed or not in PATH
1. Download Java JDK from: https://www.oracle.com/java/technologies/downloads/
2. Install it
3. Try again

### Error: "Cannot find file"
**Solution:** Make sure you're in the right directory
```bash
cd "c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai"
```

### Game won't start
**Solution:** Try these in order:
1. Make sure all .java files are in the root directory (not in subfolders)
2. Delete all .class files: `del *.class`
3. Recompile: `javac *.java`
4. Run again: `java PingPongGame`

### Online features not working
- Make sure ngrok or Tailscale is installed for online play
- Or use direct IP with port forwarding
- See documentation for setup details

---

## Controls

### Single/Local Multiplayer
- **Player 1:** W (up), S (down)
- **Player 2:** ' (up), / (down)
- **Space:** Start/pause game
- **Q:** Use ability (Player 1)
- **;:** Use ability (Player 2)

---

## File Structure

```
pong_with_learning_ai/
├── *.java              All game source files
├── *.class             Compiled game files (auto-generated)
├── RUN_GAME.md         This file
├── ERRORS_FIXED_REPORT.md    What was fixed
└── [other docs]
```

---

## Success!

If you see a game window with the menu, everything is working correctly!

Enjoy playing Pong with your AI or friends! 🎮

---

## Need Help?

- Check `ERRORS_FIXED_REPORT.md` for technical details
- Check `PROJECT_STRUCTURE.md` for code organization
- Check `QUICK_START.md` for quick reference
