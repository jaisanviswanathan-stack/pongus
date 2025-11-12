# Online Pong Game - Complete Setup & Feature Guide

Welcome to your new online Pong game! This document covers everything you need to know.

---

## 📦 What You Have

### Core Files
- **web/server.js** - Node.js WebSocket server that runs the game
- **web/package.json** - Dependencies (Express, WebSocket)
- **web/public/index.html** - Game UI (menu, keybinds, canvas)
- **web/public/game.js** - Game logic and client code

### Documentation
- **README.md** - Full documentation with deployment options
- **UPDATES.md** - Latest UI improvements
- **KEYBINDS.md** - Custom keybinds technical docs
- **KEYBINDS_QUICK_START.md** - User guide for keybinds
- **ARCHITECTURE.md** - System architecture diagrams
- **CHANGES_SUMMARY.md** - Implementation details

---

## 🚀 Quick Start (5 Minutes)

### Step 1: Install Node.js
1. Go to https://nodejs.org/
2. Click "LTS" and download
3. Install it
4. Restart your computer

### Step 2: Install Dependencies
```bash
cd web
npm install
```

### Step 3: Start Server
```bash
npm start
```

You'll see:
```
Pong Game Server running on port 3000
Open browser to http://localhost:3000
```

### Step 4: Play!
1. Open http://localhost:3000 in your browser
2. Click "Host Game"
3. Copy the link
4. Open another browser (or send link to friend)
5. Open the link and join
6. Both players configure keybinds (optional)
7. Host clicks "Start Game"
8. Play!

---

## 🎮 Game Features

### Host & Join System
- **Host Mode**: Create a room, configure keybinds, start the game
- **Join Mode**: Enter room ID or open shared link, configure keybinds, wait for host to start

### Custom Keybinds ⭐ (NEW!)
- **Host (Player 1)**: Defaults to W (up) and S (down)
- **Guest (Player 2)**: Defaults to ↑ (up) and ↓ (down)
- **Customizable**: Click any field and press a key to bind it
- **Resettable**: Click "Reset" to go back to defaults
- **Real-time Sync**: Changes instantly sent to other player

### Game Rules
- **First to 5 points** wins
- **Control**: Use your configured keys to move paddle
- **Score**: Ball past opponent = 1 point
- **Ball Physics**: Bounces off walls and paddles

### Multiple Games
- Play multiple games with different opponents
- Customize keybinds each game
- No saved settings between games

---

## 🎯 How Each Player Controls The Game

### Host (Player 1) - Left Side
```
Default:  W (up), S (down)
Custom:   Any keys you choose
```

### Guest (Player 2) - Right Side
```
Default:  ↑ (up), ↓ (down)
Custom:   Any keys you choose
```

---

## 📚 Documentation Guide

### For New Players
→ **KEYBINDS_QUICK_START.md** - How to customize your controls

### For Understanding the Game
→ **README.md** - Complete game documentation

### For Developers
→ **ARCHITECTURE.md** - System design and data flow
→ **CHANGES_SUMMARY.md** - Code changes made

### For Technical Details
→ **KEYBINDS.md** - Implementation details of keybind system

---

## ☁️ Deploy to Cloud (Optional)

Once you've tested locally, deploy to the cloud so anyone can play:

### Railway.app (Easiest)
1. Sign up at https://railway.app
2. Connect your GitHub repo
3. Deploy automatically
4. Get your live URL

### Heroku.com
1. Create account at https://heroku.com
2. Install Heroku CLI
3. Run: `heroku create my-pong-game`
4. Run: `git push heroku main`
5. Get your live URL

### Other Options
- Render.com
- Fly.io
- Replit.com
- See README.md for detailed instructions

---

## 🎨 User Interface

### Main Menu
```
PONG - Play Online with Friends - No Download Required!

How to Play:
1 Host clicks "Host Game"
2 Host copies the link
3 Friend opens the link
4 Game starts automatically when both join
5 Use W/↑ and S/↓ to move
6 First to 5 points wins!

[Host Game] [Join Game]
```

### Host Menu (After Clicking Host)
```
Room Created!
Share this link with your friend:

[https://...?room=ABC123]  [📋 Copy Link]

⌨️ Your Controls (Player 1)
Move Up:    [W]    [Reset]
Move Down:  [S]    [Reset]

⏳ Waiting for Player 2...

[🎮 Start Game - DISABLED until P2 joins]
[Back]
```

### Guest Keybind Panel (After Joining)
```
Configure Your Controls

Welcome Player 2!
The host is waiting to start the game.
Customize your controls below:

⌨️ Your Controls (Player 2)
Move Up:    [↑]    [Reset]
Move Down:  [↓]    [Reset]

✓ Waiting for Host to Start Game
You can change your controls anytime
before the game starts.

[Leave Game]
```

### Game Screen
```
PONG - Online

[Canvas with game]

Player 1: 0    Player 2: 0

🎮 Controls:
W or ↑ - Move paddle up
S or ↓ - Move paddle down

Goal: Be first to score 5 points!
Tip: Hit the ball with your paddle

[Play Again] [Exit Game]
```

---

## 🔧 Troubleshooting

### Server won't start
```
Error: Address already in use
→ Change port in server.js line 58
→ Or kill process using port 3000
```

### Can't connect to server
```
→ Make sure server is running: npm start
→ Check http://localhost:3000 loads
→ Try refreshing page
→ Check browser console (F12) for errors
```

### Opponent can't join
```
→ Share the FULL URL with room ID
→ Not just the room ID - full link
→ Make sure server is running
→ If deployed: verify deployment succeeded
```

### Game is laggy
```
→ Check internet connection
→ Server syncs 60 times/second
→ Should be smooth on decent connection
→ Try restarting server: Ctrl+C, npm start
```

### Keybinds not working
```
→ Make sure you're in game (not config screen)
→ Some keys may be blocked by OS/browser
→ Try different key
→ Check that key works in browser first
```

---

## 📋 Full Feature List

✅ Host & Join via link
✅ WebSocket real-time synchronization
✅ Custom keybinds for each player
✅ Keybind reset to defaults
✅ Waiting screens with status
✅ Score tracking
✅ Win detection (first to 5)
✅ Play again functionality
✅ Ball physics
✅ Paddle collision detection
✅ Multiple sequential games
✅ Responsive design
✅ Mobile support

---

## 🎯 Next Steps

### 1. Test Locally
```bash
cd web
npm install
npm start
→ Open http://localhost:3000
→ Host game and join in another window
→ Test with friend
```

### 2. Customize (Optional)
- Change default keybinds in game.js
- Modify game rules (score to win, ball speed)
- Add new power-ups or effects
- Customize colors and styling

### 3. Deploy (Optional)
- Choose a hosting platform (Railway, Heroku, etc.)
- Deploy following platform instructions
- Share public URL with friends
- Play online anytime!

### 4. Add Features (Optional)
- Sound effects
- Background music
- Power-ups
- Difficulty levels
- Leaderboard
- Chat

---

## 📞 Support

### Documentation
- README.md - Main documentation
- KEYBINDS_QUICK_START.md - User guide
- ARCHITECTURE.md - Technical reference

### Common Issues
See "Troubleshooting" section above

### Browser Console
Press F12 to open developer console
Shows connection errors and debug info

---

## 🎮 Game Controls Reference

### Host (Player 1) - Default
```
Move Up:    W
Move Down:  S
```

### Guest (Player 2) - Default
```
Move Up:    ↑ (Up Arrow)
Move Down:  ↓ (Down Arrow)
```

### During Game
```
Your Keys:  Move your paddle
Opponent:   Bounces the ball
Goal:       Get ball past opponent
Win:        First to 5 points
```

---

## 💾 Project Structure

```
pong_with_learning_ai/
├── web/                          # Web game folder
│   ├── server.js                # WebSocket server
│   ├── package.json             # Dependencies
│   ├── Procfile                 # Deployment config
│   ├── .gitignore               # Git ignore rules
│   ├── public/
│   │   ├── index.html           # Game UI
│   │   └── game.js              # Game logic
│   ├── README.md                # Main documentation
│   ├── UPDATES.md               # UI improvements
│   ├── KEYBINDS.md              # Keybind documentation
│   ├── KEYBINDS_QUICK_START.md  # User guide
│   ├── ARCHITECTURE.md          # System design
│   └── CHANGES_SUMMARY.md       # Implementation details
│
├── PingPongGame.java            # Original Java version
├── ONLINE_SETUP.md              # Original setup guide
├── KEYBINDS_FEATURE.md          # Feature overview
└── WEB_GAME_SETUP.md            # This file
```

---

## 🚀 You're Ready!

Everything is set up and ready to go. Start the server and enjoy playing Pong online with custom keybinds!

```bash
cd web
npm install
npm start
```

Then open http://localhost:3000 and start playing!

---

## Questions?

### Feature Overview
→ KEYBINDS_FEATURE.md

### Installation Help
→ ONLINE_SETUP.md

### Technical Details
→ ARCHITECTURE.md

### User Guide
→ KEYBINDS_QUICK_START.md

---

## Summary

✨ **You now have:**
- A fully functional online Pong game
- Custom keybind support for both players
- Easy-to-use web interface
- Ready to deploy to the cloud
- Complete documentation

🎮 **Players can:**
- Host a game and share a link
- Join friend's games by opening link
- Customize their keyboard controls
- Play with real-time multiplayer sync
- Multiple games with different opponents

🚀 **Next steps:**
1. Test locally
2. Invite a friend
3. Deploy to cloud (optional)
4. Start playing!

Enjoy your online Pong game! 🎉
