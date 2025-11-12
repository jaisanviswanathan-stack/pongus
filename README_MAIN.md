# Pong Game - Online Multiplayer Edition with Custom Keybinds

## 🎮 Welcome!

You now have a **fully functional online Pong game** that you and your friends can play in a web browser. No JAR files, no downloads - just a browser link!

### What's Special About This Version?

✨ **Custom Keybinds** - Each player can configure their own keyboard controls
✨ **No Downloads** - Play in any web browser
✨ **Easy to Share** - One link plays the game
✨ **Real-time Multiplayer** - Smooth 60 FPS gameplay
✨ **Cloud Ready** - Deploy to internet in 5 minutes

---

## 📖 Which Guide Should I Read?

### 🚀 **Just Want to Play?**
→ Read: [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)
- Step-by-step setup (10 minutes)
- How to test locally
- Troubleshooting

### 🎯 **Want Features Overview?**
→ Read: [KEYBINDS_FEATURE.md](KEYBINDS_FEATURE.md)
- What the game can do
- Custom keybinds explanation
- User interface walkthrough

### 🌐 **Want to Deploy Online?**
→ Read: [web/README.md](web/README.md)
- Cloud deployment options
- Heroku, Railway, Fly.io instructions
- Free hosting options

### ⚙️ **Developer/Technical Info?**
→ Read: [web/ARCHITECTURE.md](web/ARCHITECTURE.md)
- System design
- Data flow diagrams
- How keybinds work internally

### 🎮 **User Quick Start?**
→ Read: [web/KEYBINDS_QUICK_START.md](web/KEYBINDS_QUICK_START.md)
- How to customize controls
- Tips and best practices
- Screenshots of UI

### 📋 **All Technical Details?**
→ Read: [web/CHANGES_SUMMARY.md](web/CHANGES_SUMMARY.md)
- Everything that was added
- Code examples
- File-by-file breakdown

---

## 🚀 Quick Start (TL;DR)

```bash
# 1. Make sure you have Node.js installed
# Download from https://nodejs.org/

# 2. Navigate to web folder
cd web

# 3. Install dependencies
npm install

# 4. Start the server
npm start

# 5. Open browser to http://localhost:3000
# Done! Click "Host Game" or "Join Game"
```

---

## 📁 Project Files

### Core Game Files
```
web/
├── server.js                  # WebSocket server
├── package.json              # Dependencies
├── public/
│   ├── index.html           # Game UI & HTML
│   └── game.js              # Game logic & client
└── Procfile                 # For cloud deployment
```

### Documentation (Read These!)
```
web/
├── README.md                         # Main documentation
├── KEYBINDS.md                      # Custom keybinds docs
├── KEYBINDS_QUICK_START.md          # User guide
├── ARCHITECTURE.md                  # System design
├── CHANGES_SUMMARY.md               # Implementation details
└── UPDATES.md                       # Latest improvements
```

### Top-Level Guides
```
├── INSTALLATION_GUIDE.md            # Setup instructions
├── KEYBINDS_FEATURE.md              # Feature overview
├── WEB_GAME_SETUP.md                # Complete guide
└── README_MAIN.md                   # This file
```

---

## 🎯 Features

### Host/Join System
- Create a game room and share a link
- Join friend's game by opening the link
- Automatic player detection and sync

### Custom Keybinds ⭐
- Each player configures their own keys
- Host (Player 1): Default W/S, customizable to anything
- Guest (Player 2): Default Arrows, customizable to anything
- Real-time sync of keybind changes
- Reset buttons for each keybind

### Game Features
- Real-time multiplayer (60 FPS)
- Ball physics and collision detection
- Score tracking (first to 5 wins)
- Multiple games can be played
- Responsive UI for desktop and tablets

### Hosting Options
- Local testing (localhost:3000)
- Cloud deployment (Heroku, Railway, Fly.io)
- Free hosting available
- No credit card required for most services

---

## 🎮 How to Play

### Host (Player 1) - Left Side
1. Click "Host Game"
2. Customize your keys (W/S default)
3. Copy and share the link
4. Click "Start Game" when player 2 joins

### Guest (Player 2) - Right Side
1. Click link from host
2. Customize your keys (↑/↓ default)
3. Wait for host to click "Start Game"
4. Game begins automatically

### During Game
- Move your paddle with your configured keys
- Ball bounces between paddles
- Opponent's side scores if ball passes you
- First to 5 points wins
- Click "Play Again" to rematch

---

## 🔧 System Requirements

### To Run Locally
- Node.js 14 or higher
- Any modern web browser
- 50MB disk space
- Internet connection (for cloud version)

### To Deploy to Cloud
- GitHub account (recommended)
- Choose hosting: Railway, Heroku, or Fly.io
- All services offer free tier

---

## 📊 Architecture Overview

```
┌─────────────────────────────────────┐
│    Players' Browsers (Client)       │
│  • Keybind Configuration UI         │
│  • Game Canvas & Rendering          │
│  • Input Handling                   │
└─────────────┬───────────────────────┘
              │ WebSocket Messages
              ▼
┌─────────────────────────────────────┐
│  Node.js Server (Port 3000)         │
│  • Game State Management            │
│  • Ball Physics                     │
│  • Collision Detection              │
│  • Input Processing                 │
└─────────────────────────────────────┘
              ▲ 60 FPS Sync
              │
┌─────────────────────────────────────┐
│    Browser #2 (Guest/Player 2)      │
│  • Renders with opponent's input    │
│  • Sends own keybind config         │
│  • Receives game state updates      │
└─────────────────────────────────────┘
```

---

## 🚀 Deployment Options

### Local Testing
```bash
npm start
→ http://localhost:3000
```

### Railway.app (Easiest)
1. Sign up: https://railway.app
2. Connect GitHub repo
3. Deploy: Automatic
4. URL: Provided instantly

### Heroku.com
1. Create account
2. Install Heroku CLI
3. Run: `heroku create` and `git push heroku main`
4. URL: Your-app-name.herokuapp.com

### Fly.io
1. Create account
2. Install flyctl
3. Run: `flyctl launch` and `flyctl deploy`
4. URL: Your-app-name.fly.dev

See [web/README.md](web/README.md) for detailed instructions.

---

## 🎨 Default Keybinds

### Player 1 (Host)
| Action | Default Key | Custom? |
|--------|------------|---------|
| Move Up | W | Yes |
| Move Down | S | Yes |

### Player 2 (Guest)
| Action | Default Key | Custom? |
|--------|------------|---------|
| Move Up | ↑ (Up Arrow) | Yes |
| Move Down | ↓ (Down Arrow) | Yes |

---

## ❓ FAQ

**Q: Can I play with friends online?**
A: Yes! Deploy to cloud (Heroku, Railway, etc.) and share the URL.

**Q: Do they need to download anything?**
A: No! Just open the browser link.

**Q: Can we use different keys?**
A: Yes! Each player customizes independently before game starts.

**Q: Can we play multiple games?**
A: Yes! Keep playing as many times as you want.

**Q: Are keybinds saved?**
A: No, they reset each game. Customize fresh each time.

**Q: Does it work on mobile?**
A: Yes, with on-screen keyboard or external keyboard.

**Q: How much does it cost?**
A: Free to run locally. Free tiers available for cloud hosting.

---

## 📞 Getting Help

### Installation Issues
→ Read: [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)
→ Section: "Troubleshooting"

### Feature Questions
→ Read: [KEYBINDS_FEATURE.md](KEYBINDS_FEATURE.md)
→ Section: "FAQ"

### Deployment Questions
→ Read: [web/README.md](web/README.md)
→ Section: "Deployment to Cloud"

### Technical Questions
→ Read: [web/ARCHITECTURE.md](web/ARCHITECTURE.md)
→ Or: [web/CHANGES_SUMMARY.md](web/CHANGES_SUMMARY.md)

---

## 🎯 Next Steps

### Step 1: Get It Running
```bash
cd web
npm install
npm start
```
→ [Follow INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)

### Step 2: Play Locally
- Open http://localhost:3000
- Host and join in two browser windows
- Test with custom keybinds
- Make sure everything works

### Step 3: Deploy (Optional)
- Choose hosting: Railway, Heroku, or Fly.io
- Follow deployment guide
- Share URL with friends
- Play online!

### Step 4: Customize (Optional)
- Change game rules
- Modify colors/theme
- Add sound effects
- Add new features

---

## 📈 What's Included

✅ Complete web-based Pong game
✅ Custom keybinds for each player
✅ WebSocket multiplayer sync
✅ Beautiful responsive UI
✅ Multiple documentation guides
✅ Ready for cloud deployment
✅ Works in any modern browser
✅ No database needed
✅ No paid services required
✅ Open source and customizable

---

## 🎓 Learn More

### Understanding the Code
- Read server.js for game logic
- Read game.js for client logic
- Check ARCHITECTURE.md for data flow

### Customizing the Game
- Modify colors in index.html (CSS section)
- Change game rules in server.js
- Adjust paddle size or speed
- Add new features

### Hosting Your Game
- See Procfile for deployment config
- Choose platform in README.md
- Follow step-by-step deployment

---

## 🏆 Credits

**Original Developer:** Jaisan Viswanathan
**Features:**
- Online multiplayer (ngrok/Tailscale support)
- Custom keybinds system
- Web-based version with real-time sync
- Beautiful responsive UI

---

## 📄 License

This project is yours to use, modify, and share!

---

## 🎮 Ready to Play?

**Right Now:**
```bash
cd web && npm install && npm start
# Then open http://localhost:3000
```

**Later:**
- Deploy to cloud for friends
- Customize keybinds
- Add new features
- Have fun!

---

## 📚 Documentation Quick Links

| Document | Purpose | Audience |
|----------|---------|----------|
| [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md) | Step-by-step setup | Everyone starting out |
| [KEYBINDS_FEATURE.md](KEYBINDS_FEATURE.md) | Feature overview | Players & users |
| [web/KEYBINDS_QUICK_START.md](web/KEYBINDS_QUICK_START.md) | User guide | End users |
| [web/README.md](web/README.md) | Full docs | Everyone |
| [web/ARCHITECTURE.md](web/ARCHITECTURE.md) | System design | Developers |
| [web/CHANGES_SUMMARY.md](web/CHANGES_SUMMARY.md) | Implementation | Developers |
| [WEB_GAME_SETUP.md](WEB_GAME_SETUP.md) | Complete overview | Everyone |

---

## 💡 Pro Tips

1. **Test locally first** before deploying
2. **Use Chrome or Firefox** for best experience
3. **Copy full link with room ID** when sharing
4. **Keep server terminal open** while playing
5. **Try different keybinds** to find your preference
6. **Deploy to cloud** for friends anywhere
7. **Check README.md** for full deployment guide

---

## 🎉 You're All Set!

Everything you need is here. Install, play, and enjoy!

**Questions?** Check the documentation files above.
**Ready?** Run `npm install && npm start` in the web folder!

Have fun playing Pong! 🎮
