# Setup Your Online Pong Game

## The Easy Way (5 minutes)

### Step 1: Install Node.js
Download from: https://nodejs.org/
- Click "LTS" (Long Term Support)
- Install it
- Restart your computer

### Step 2: Navigate to the web folder
Open Command Prompt or PowerShell and run:
```
cd "c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai\web"
```

### Step 3: Install dependencies
```
npm install
```
Wait for it to finish (might take 1-2 minutes).

### Step 4: Start the server
```
npm start
```

You should see:
```
Pong Game Server running on port 3000
Open browser to http://localhost:3000
```

### Step 5: Play!
- Open your browser to: http://localhost:3000
- Click "Host Game"
- Copy the link that appears
- Send that link to your friend
- Friend opens the link and clicks "Join"
- You're playing!

---

## Deploy to the Cloud (So Anyone Can Play)

Once you've tested locally, deploy to the cloud so your friend can play even when your computer is off.

### Free Deployment Options

#### Option A: Railway.app (Easiest)
1. Go to https://railway.app
2. Sign up with GitHub
3. Create new project → "Deploy from GitHub"
4. Select this repository (pong_with_learning_ai)
5. Specify start command: `npm start`
6. Click deploy
7. Share your Railway URL with friends

#### Option B: Heroku.com (Also Easy)
1. Go to https://heroku.com and create account
2. Download Heroku CLI: https://devcenter.heroku.com/articles/heroku-cli
3. Open Command Prompt in the `web` folder:
```
cd web
heroku login
heroku create my-pong-game
git push heroku main
heroku open
```
4. Share the URL with friends

#### Option C: Render.com (Free Tier)
1. Go to https://render.com
2. Sign up with GitHub
3. New → Web Service
4. Connect your repository
5. Build command: `npm install`
6. Start command: `npm start`
7. Deploy and get your URL

#### Option D: Fly.io
1. Go to https://fly.io
2. Download flyctl: https://fly.io/docs/getting-started/installing-flyctl/
3. In the `web` folder:
```
cd web
flyctl launch
flyctl deploy
```

---

## Share Your Game

Once deployed:
1. Send your deployed URL to friends
2. They click "Host Game" to host
3. Or they click "Join Game" and paste a room ID
4. Everyone plays in their browser!

---

## Troubleshooting

**"npm: command not found"**
- Node.js isn't installed
- Download and install from https://nodejs.org/

**Port 3000 already in use**
- Change port in server.js (line 58): `const PORT = process.env.PORT || 3001;`

**Can't connect when deployed**
- Wait 2-3 minutes after deployment
- Clear browser cache (Ctrl+Shift+Delete)
- Check if deployment succeeded on your platform's dashboard

**Game is laggy**
- Check internet connection
- Server syncs 60 times per second - should be smooth
- Restart server and refresh browser

---

## What's Different from Java Version?

Your Java game had these features:
- ✅ Learning AI
- ✅ Power-ups
- ✅ Paddle abilities
- ✅ Score tracking

This web version has:
- ✅ Multiplayer (you wanted this!)
- ✅ No JAR file needed
- ✅ Plays in browser
- ⚠️ Simplified for now (we can add features!)

You can gradually add your features to the web version. Would you like help with that?

---

## Next Steps

1. Test locally first (the quick start above)
2. Deploy to cloud (pick any option)
3. Share URL with friends and play!
4. Come back if you want to add your AI or power-up features
