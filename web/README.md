# Pong Game - Online Multiplayer

Play Pong with anyone online! No JAR files needed - just a web browser.

## Features

- **Browser-Based**: No downloads or installations required
- **Real-Time Multiplayer**: Play with friends online
- **WebSocket Sync**: Low-latency game state synchronization
- **Easy Sharing**: Generate a link and share with friends
- **Responsive Design**: Works on desktop and tablet

## Quick Start (Local)

### Prerequisites
- Node.js 14+ installed
- npm (comes with Node.js)

### Setup

1. Navigate to the web folder:
```bash
cd web
```

2. Install dependencies:
```bash
npm install
```

3. Start the server:
```bash
npm start
```

4. Open your browser to:
```
http://localhost:3000
```

5. Click "Host Game" and share the generated link with your friend
6. Friend clicks the link and joins automatically

## How to Play

### Controls
- **W or Up Arrow**: Move paddle up
- **S or Down Arrow**: Move paddle down

### Game Rules
- First player to 5 points wins
- Click "Host Game" to create a room
- Click "Join Game" to join an existing room
- Share the room link with your friend

## Deployment to Cloud (Free Options)

### Option 1: Deploy to Heroku (Recommended)

1. Create a free account at https://heroku.com

2. Install Heroku CLI: https://devcenter.heroku.com/articles/heroku-cli

3. Create a Procfile in the `web` folder:
```bash
echo "web: node server.js" > Procfile
```

4. Deploy:
```bash
cd web
heroku login
heroku create your-app-name
git push heroku main
```

5. Open your app:
```bash
heroku open
```

Your game will be available at: `https://your-app-name.herokuapp.com`

### Option 2: Deploy to Railway.app

1. Sign up at https://railway.app

2. Connect your GitHub repository

3. Railway auto-detects Node.js and deploys

4. Set the start command to `npm start`

### Option 3: Deploy to Replit

1. Go to https://replit.com

2. Create new project from GitHub (import this repo)

3. Click "Run" - it will start the server

4. Use the generated URL to share with friends

### Option 4: Deploy to Fly.io

1. Sign up at https://fly.io

2. Install Fly CLI

3. Run:
```bash
flyctl launch
flyctl deploy
```

## Architecture

```
web/
├── server.js           # WebSocket server & game logic
├── package.json        # Dependencies
└── public/
    ├── index.html      # UI
    └── game.js         # Client-side game code
```

### How It Works

1. **Player 1 (Host)** clicks "Host Game" → Gets a room ID
2. **Player 1** shares the link with Player 2
3. **Player 2** opens the link → Automatically joins the room
4. **Server** syncs game state to both players at 60 FPS
5. **Players** send input (paddle movement) to server
6. **Server** updates ball position, checks collisions, updates scores
7. **Both clients** render the synchronized game state

## Troubleshooting

**Can't connect to server?**
- Make sure server is running: `npm start`
- Check if port 3000 is available
- Try accessing http://localhost:3000

**Connection drops during game?**
- Server may have crashed - restart with `npm start`
- Check network connection
- Look at browser console (F12) for errors

**Game feels laggy?**
- Server syncs at 60 FPS - should be smooth
- Check your internet connection
- Try moving closer to your router

**Friend can't join my game?**
- Make sure the link includes the room ID
- Check the URL has `?room=ROOMID`
- Verify your server is running and accessible

## Features Coming Soon

- Power-ups
- AI opponent
- Different game modes
- Sound effects
- Custom paddle colors
- Leaderboard

## Support

If you encounter issues:
1. Check the browser console (F12)
2. Check server logs (terminal where you ran `npm start`)
3. Make sure both players are using the same server
4. Restart both the server and refresh the browser

## License

MIT License - Feel free to modify and share!
