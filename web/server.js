const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const path = require('path');
const cors = require('cors');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Serve static files
app.use(express.static(path.join(__dirname, 'public')));
app.use(cors());

// Game rooms
const rooms = new Map();

class GameRoom {
  constructor(roomId) {
    this.roomId = roomId;
    this.players = new Map();
    this.gameState = {
      ballX: 250,
      ballY: 150,
      ballVelX: 2,
      ballVelY: 2,
      paddle1Y: 100,
      paddle2Y: 100,
      player1Score: 0,
      player2Score: 0,
      gameRunning: false,
      winner: null
    };
    this.gameLoop = null;
  }

  addPlayer(playerId, playerNumber, ws) {
    this.players.set(playerId, {
      playerNumber,
      ws,
      controls: { up: false, down: false },
      connected: true
    });

    // Notify all players when a new player joins
    if (this.players.size === 2) {
      // Notify that player 2 has joined (for host to enable start button)
      this.broadcastMessage({
        type: 'playerJoined',
        message: 'Player 2 has joined the game'
      });
    }
  }

  broadcastMessage(message) {
    const msgString = JSON.stringify(message);
    this.players.forEach((player) => {
      if (player.ws.readyState === WebSocket.OPEN) {
        player.ws.send(msgString);
      }
    });
  }

  removePlayer(playerId) {
    this.players.delete(playerId);
    if (this.players.size < 2) {
      this.stopGame();
    }
  }

  startGame() {
    if (this.gameLoop) return;

    this.gameState.gameRunning = true;
    this.gameLoop = setInterval(() => this.updateGameState(), 1000 / 60); // 60 FPS
  }

  stopGame() {
    if (this.gameLoop) {
      clearInterval(this.gameLoop);
      this.gameLoop = null;
    }
    this.gameState.gameRunning = false;
  }

  updateGameState() {
    const state = this.gameState;
    const CANVAS_WIDTH = 500;
    const CANVAS_HEIGHT = 300;
    const PADDLE_HEIGHT = 60;

    // Update paddle positions based on player input
    this.players.forEach((player) => {
      const paddleKey = player.playerNumber === 1 ? 'paddle1Y' : 'paddle2Y';
      if (player.controls.up && state[paddleKey] > 0) {
        state[paddleKey] -= 4;
      }
      if (player.controls.down && state[paddleKey] < CANVAS_HEIGHT - PADDLE_HEIGHT) {
        state[paddleKey] += 4;
      }
    });

    // Update ball position
    state.ballX += state.ballVelX;
    state.ballY += state.ballVelY;

    // Ball collision with top/bottom walls
    if (state.ballY <= 0 || state.ballY >= CANVAS_HEIGHT) {
      state.ballVelY = -state.ballVelY;
      state.ballY = Math.max(0, Math.min(CANVAS_HEIGHT, state.ballY));
    }

    // Ball collision with paddles
    const PADDLE_WIDTH = 10;
    if (state.ballX <= PADDLE_WIDTH) {
      if (state.ballY >= state.paddle1Y && state.ballY <= state.paddle1Y + PADDLE_HEIGHT) {
        state.ballVelX = Math.abs(state.ballVelX);
        state.ballX = PADDLE_WIDTH;
      }
    }

    if (state.ballX >= CANVAS_WIDTH - PADDLE_WIDTH) {
      if (state.ballY >= state.paddle2Y && state.ballY <= state.paddle2Y + PADDLE_HEIGHT) {
        state.ballVelX = -Math.abs(state.ballVelX);
        state.ballX = CANVAS_WIDTH - PADDLE_WIDTH;
      }
    }

    // Ball out of bounds - scoring
    if (state.ballX < 0) {
      state.player2Score++;
      this.resetBall();
    }
    if (state.ballX > CANVAS_WIDTH) {
      state.player1Score++;
      this.resetBall();
    }

    // Check for winner (first to 5)
    if (state.player1Score >= 5) {
      state.winner = 1;
      this.stopGame();
    } else if (state.player2Score >= 5) {
      state.winner = 2;
      this.stopGame();
    }

    // Broadcast game state to all players
    this.broadcastGameState();
  }

  resetBall() {
    this.gameState.ballX = 250;
    this.gameState.ballY = 150;
    this.gameState.ballVelX = (Math.random() > 0.5 ? 1 : -1) * 2;
    this.gameState.ballVelY = (Math.random() > 0.5 ? 1 : -1) * 2;
  }

  broadcastGameState() {
    const message = JSON.stringify({
      type: 'gameState',
      data: this.gameState
    });

    this.players.forEach((player) => {
      if (player.ws.readyState === WebSocket.OPEN) {
        player.ws.send(message);
      }
    });
  }

  handlePlayerInput(playerId, controls) {
    const player = this.players.get(playerId);
    if (player) {
      player.controls = controls;
    }
  }
}

// WebSocket connection handler
wss.on('connection', (ws) => {
  let roomId = null;
  let playerId = null;
  let playerNumber = null;

  ws.on('message', (data) => {
    try {
      const message = JSON.parse(data);

      if (message.type === 'join') {
        roomId = message.roomId;
        playerId = message.playerId;
        playerNumber = message.playerNumber;

        // Create room if it doesn't exist
        if (!rooms.has(roomId)) {
          rooms.set(roomId, new GameRoom(roomId));
        }

        const room = rooms.get(roomId);
        room.addPlayer(playerId, playerNumber, ws);

        // Send confirmation
        ws.send(JSON.stringify({
          type: 'joinConfirm',
          roomId,
          playerNumber,
          gameState: room.gameState
        }));
      }

      if (message.type === 'input') {
        const room = rooms.get(roomId);
        if (room) {
          room.handlePlayerInput(playerId, message.controls);
        }
      }

      if (message.type === 'keybinds') {
        const room = rooms.get(roomId);
        if (room) {
          // Broadcast keybinds to the other player
          room.players.forEach((player) => {
            if (player.ws.readyState === WebSocket.OPEN && player.playerNumber !== playerNumber) {
              player.ws.send(JSON.stringify({
                type: 'playerKeybinds',
                keybinds: message.keybinds
              }));
            }
          });
        }
      }

      if (message.type === 'startGame') {
        const room = rooms.get(roomId);
        if (room) {
          console.log('Starting game in room:', roomId);
          room.startGame();
          // Notify guest that game is starting
          room.broadcastMessage({
            type: 'gameStarted',
            message: 'Game has started!'
          });
        }
      }

      if (message.type === 'restart') {
        const room = rooms.get(roomId);
        if (room) {
          room.gameState.player1Score = 0;
          room.gameState.player2Score = 0;
          room.gameState.winner = null;
          room.resetBall();
          room.startGame();
        }
      }
    } catch (error) {
      console.error('Message handling error:', error);
    }
  });

  ws.on('close', () => {
    if (roomId && playerId) {
      const room = rooms.get(roomId);
      if (room) {
        room.removePlayer(playerId);
        // Clean up empty rooms
        if (room.players.size === 0) {
          rooms.delete(roomId);
        }
      }
    }
  });

  ws.on('error', (error) => {
    console.error('WebSocket error:', error);
  });
});

// Routes
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.get('/api/rooms', (req, res) => {
  const roomList = Array.from(rooms.entries()).map(([roomId, room]) => ({
    roomId,
    playerCount: room.players.size,
    maxPlayers: 2
  }));
  res.json(roomList);
});

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Pong Game Server running on port ${PORT}`);
  console.log(`Open browser to http://localhost:${PORT}`);
});
