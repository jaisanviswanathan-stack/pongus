# Installation & Quick Start Guide

## Prerequisites

Before you start, make sure you have:
- **A computer** (Windows, Mac, or Linux)
- **Internet connection**
- **A web browser** (Chrome, Firefox, Safari, Edge)
- **Node.js** (we'll install this)

---

## 🎯 Installation Steps (Windows, Mac, or Linux)

### Step 1: Install Node.js ⏱️ (5 minutes)

Node.js is the server software that runs your Pong game.

**For Windows:**
1. Go to https://nodejs.org/
2. Click the big blue "LTS" button
3. Run the installer
4. Click "Next" through all screens
5. Click "Install" at the end
6. Restart your computer when done

**For Mac:**
1. Go to https://nodejs.org/
2. Click the big blue "LTS" button (Mac version)
3. Open the .pkg file
4. Follow the installer
5. Restart your computer when done

**For Linux:**
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

**Verify installation:**
Open terminal/command prompt and type:
```
node --version
npm --version
```

You should see version numbers like `v18.16.0`

---

### Step 2: Navigate to Web Folder ⏱️ (1 minute)

Open your terminal/command prompt and navigate to the web folder:

**Windows:**
```bash
cd "c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai\web"
```

**Mac/Linux:**
```bash
cd ~/Documents/Coding\ Projects/Java\ Projects/pong_with_learning_ai/web
```

Or just right-click the `web` folder and select "Open in Terminal" (on most systems)

---

### Step 3: Install Dependencies ⏱️ (2 minutes)

Type this command in your terminal:
```bash
npm install
```

You'll see text scrolling. Wait for it to complete. It should end with something like:
```
added 50 packages in 15s
```

This downloads all the server software your game needs.

---

### Step 4: Start the Server ⏱️ (instant)

Type this command:
```bash
npm start
```

You should see:
```
Pong Game Server running on port 3000
Open browser to http://localhost:3000
```

**Keep this window open!** Your game server is now running.

---

### Step 5: Play! 🎮

Open your web browser and go to:
```
http://localhost:3000
```

You should see the Pong game menu!

---

## 🎮 How to Play (Local Test)

### Test with Two Browser Windows

**In Browser #1:**
1. Click "Host Game"
2. See the room created
3. Copy the link (📋 button)
4. Paste it in another browser window (or give to a friend)

**In Browser #2:**
1. Paste the link
2. You'll see keybind configuration
3. Configure your keys (or use defaults)

**Back in Browser #1:**
1. See that Player 2 joined
2. Click "Start Game"

**Both Browsers:**
1. Game starts!
2. Control your paddle with your keys
3. First to 5 points wins!

---

## ⌨️ Default Controls

### Player 1 (Host)
- **Up**: W
- **Down**: S

### Player 2 (Guest)
- **Up**: ↑ (Up Arrow)
- **Down**: ↓ (Down Arrow)

You can customize these before the game starts!

---

## 🆘 Troubleshooting

### "Node: command not found"
**Problem:** Node.js isn't installed
**Solution:**
1. Go back to Step 1
2. Download and install Node.js
3. Restart your computer
4. Try again

### "npm: command not found"
**Problem:** Node.js installation incomplete
**Solution:**
1. Restart your computer
2. Try `node --version`
3. If that works, npm should work too

### Port 3000 already in use
**Problem:** Another program is using port 3000
**Solution:**
1. Close other Node.js servers
2. Or change port in server.js (line 58)
3. Change `3000` to `3001` or another number

### Can't connect to http://localhost:3000
**Problem:** Server didn't start properly
**Solution:**
1. Check that `npm start` is running in terminal
2. Check for error messages
3. Try stopping (Ctrl+C) and restarting
4. Try a different port (see above)

### Second browser can't join
**Problem:** Link might be wrong
**Solution:**
1. Make sure you copy the FULL link from the box
2. Include the `?room=XXXXX` part
3. Don't just type the room ID
4. Paste the full URL

### Game feels jumpy/laggy
**Problem:** Server might have crashed
**Solution:**
1. Check terminal running the server
2. Look for error messages
3. Stop (Ctrl+C) and restart with `npm start`
4. Refresh browser

### Keybinds not working
**Problem:** Keys might be blocked by system
**Solution:**
1. Make sure game has started (not config screen)
2. Try a different key
3. Some OS-level keys can't be rebinded
4. Reset to defaults and try again

---

## 📋 Setup Checklist

- [ ] Downloaded and installed Node.js
- [ ] Verified Node.js with `node --version`
- [ ] Opened terminal in `web` folder
- [ ] Ran `npm install` successfully
- [ ] Ran `npm start` successfully
- [ ] Server shows "Pong Game Server running on port 3000"
- [ ] Opened http://localhost:3000 in browser
- [ ] Clicked "Host Game" successfully
- [ ] Clicked "Join Game" in another window
- [ ] Configured keybinds
- [ ] Started game successfully
- [ ] Both paddles moved with keybinds
- [ ] Ball bounced properly
- [ ] Score tracked correctly

✅ If all checked: You're ready to play!

---

## 🚀 Next Steps

### 1. Play with Friends
- Get friend's IP address or domain
- Deploy to cloud (see below)
- Share the live URL

### 2. Deploy to Cloud (Optional)
Choose one of these free hosting services:

**Railway.app (Easiest):**
1. Sign up at https://railway.app
2. Connect your GitHub repo
3. Automatic deployment
4. Get your live URL

**Heroku (Popular):**
1. Sign up at https://heroku.com
2. Download Heroku CLI
3. Follow deployment steps in README.md

**Fly.io (Modern):**
1. Sign up at https://fly.io
2. Download flyctl
3. Run `flyctl launch` and `flyctl deploy`

See README.md in web folder for detailed deployment instructions.

### 3. Customize the Game (Optional)
- Modify colors in index.html
- Change game rules in server.js
- Adjust ball speed
- Add sounds

---

## 📚 Documentation Files

After you get it running, check out:

1. **WEB_GAME_SETUP.md** - Complete overview of features
2. **KEYBINDS_FEATURE.md** - All about custom keybinds
3. **KEYBINDS_QUICK_START.md** - User-friendly guide
4. **README.md** - Full technical documentation
5. **ARCHITECTURE.md** - How the system works

---

## 🎮 Common Commands

### Start the game server
```bash
npm start
```

### Stop the game server
Press `Ctrl+C` in the terminal

### Install missing dependencies
```bash
npm install
```

### Check Node.js version
```bash
node --version
```

### Update Node.js
Go to https://nodejs.org and reinstall

---

## 💡 Tips for Success

1. **Keep terminal open** - Server needs to keep running
2. **Use modern browser** - Chrome, Firefox, Safari, Edge all work
3. **Test locally first** - Before deploying to cloud
4. **Keep both windows visible** - Easier to test on one screen
5. **Copy links carefully** - Include the full URL with `?room=`

---

## 🎉 You're All Set!

You now have everything you need to:
✅ Run the game locally
✅ Play with yourself
✅ Play with friends on same network
✅ Deploy to cloud (optional)
✅ Customize keybinds

**Start playing:**
```bash
cd web
npm start
```

Then open http://localhost:3000

**Have fun!** 🎮

---

## 📞 Quick Reference

**Installation time:** 10-15 minutes
**Server port:** 3000
**Browser:** http://localhost:3000
**Keybinds:** Customizable before game
**Players:** 2 players per game
**Score to win:** First to 5 points

---

## 🚀 Ready?

1. Install Node.js
2. Run `npm install`
3. Run `npm start`
4. Open http://localhost:3000
5. Play!

Enjoy your online Pong game! 🎉
