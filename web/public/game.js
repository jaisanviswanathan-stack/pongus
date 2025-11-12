// Game state
let ws = null;
let gameState = {
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

let playerNumber = null;
let roomId = null;
let isHost = false;
let animationId = null;
let keys = {};

// Keybind storage
let playerKeybinds = {
    upKey: null,
    downKey: null
};

let otherPlayerKeybinds = {
    upKey: null,
    downKey: null
};

let listeningForKey = null;

// Default keybinds based on player
const DEFAULT_PLAYER1_BINDS = { upKey: 'KeyW', downKey: 'KeyS' };
const DEFAULT_PLAYER2_BINDS = { upKey: 'ArrowUp', downKey: 'ArrowDown' };

// Canvas setup
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

const CANVAS_WIDTH = 500;
const CANVAS_HEIGHT = 300;
const PADDLE_WIDTH = 10;
const PADDLE_HEIGHT = 60;
const BALL_SIZE = 8;

// Event listeners for controls
document.addEventListener('keydown', (e) => {
    // If we're listening for a key to bind, capture it
    if (listeningForKey) {
        captureKeybind(e);
        return;
    }

    keys[e.code] = true;
    handleInput();
});

document.addEventListener('keyup', (e) => {
    keys[e.code] = false;
    handleInput();
});

// Keybind Functions
function listenForKey(inputId) {
    listeningForKey = inputId;
    const input = document.getElementById(inputId);
    input.classList.add('listening');
    input.value = '... press a key ...';
    input.style.fontWeight = 'bold';
}

function captureKeybind(event) {
    event.preventDefault();
    const inputId = listeningForKey;
    const keyCode = event.code;
    const keyDisplay = getKeyDisplay(keyCode);

    // Get the input element
    const input = document.getElementById(inputId);
    input.value = keyDisplay;
    input.classList.remove('listening');

    // Update playerKeybinds based on which input was changed
    if (inputId === 'hostUpKey' || inputId === 'guestUpKey') {
        playerKeybinds.upKey = keyCode;
    } else if (inputId === 'hostDownKey' || inputId === 'guestDownKey') {
        playerKeybinds.downKey = keyCode;
    }

    // Send keybinds to server
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'keybinds',
            keybinds: playerKeybinds
        }));
    }

    listeningForKey = null;
}

function getKeyDisplay(keyCode) {
    const keyMap = {
        'KeyW': 'W',
        'KeyA': 'A',
        'KeyS': 'S',
        'KeyD': 'D',
        'ArrowUp': '↑',
        'ArrowDown': '↓',
        'ArrowLeft': '←',
        'ArrowRight': '→',
        'Space': 'Space',
        'Enter': 'Enter',
        'Shift': 'Shift',
        'Control': 'Ctrl',
        'Alt': 'Alt'
    };
    return keyMap[keyCode] || keyCode;
}

function resetKeybind(inputId, defaultKeyCode) {
    const input = document.getElementById(inputId);
    const keyDisplay = getKeyDisplay(defaultKeyCode);
    input.value = keyDisplay;
    input.classList.remove('listening');

    // Update playerKeybinds
    if (inputId === 'hostUpKey' || inputId === 'guestUpKey') {
        playerKeybinds.upKey = defaultKeyCode;
    } else if (inputId === 'hostDownKey' || inputId === 'guestDownKey') {
        playerKeybinds.downKey = defaultKeyCode;
    }

    // Send keybinds to server
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'keybinds',
            keybinds: playerKeybinds
        }));
    }

    listeningForKey = null;
}

// Helper functions
function generateRoomId() {
    return Math.random().toString(36).substring(2, 8).toUpperCase();
}

function generatePlayerId() {
    return Date.now().toString() + Math.random().toString(36).substring(2, 9);
}

function getServerUrl() {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        return `ws://localhost:3000`;
    }
    return `wss://${window.location.host}`;
}

// Menu functions
function startAsHost() {
    isHost = true;
    playerNumber = 1;
    roomId = generateRoomId();

    // Initialize default keybinds for player 1
    playerKeybinds = { ...DEFAULT_PLAYER1_BINDS };

    document.getElementById('mainMenu').style.display = 'none';
    document.getElementById('hostMenu').style.display = 'block';

    const shareUrl = `${window.location.origin}?room=${roomId}`;
    document.getElementById('shareLink').textContent = shareUrl;
    document.getElementById('hostStatus').textContent = `✓ Room created: ${roomId}`;
}

function showJoinForm() {
    document.getElementById('mainMenu').style.display = 'none';
    document.getElementById('joinMenu').style.display = 'block';
}

function backToMenu() {
    document.getElementById('mainMenu').style.display = 'block';
    document.getElementById('hostMenu').style.display = 'none';
    document.getElementById('joinMenu').style.display = 'none';
    if (ws) ws.close();
}

function connectAsGuest() {
    const input = document.getElementById('roomIdInput').value.trim();
    if (!input) {
        alert('Please enter a room ID or server address');
        return;
    }

    isHost = false;
    playerNumber = 2;
    roomId = input.split('/').pop().split('?room=').pop();

    // Initialize default keybinds for player 2
    playerKeybinds = { ...DEFAULT_PLAYER2_BINDS };

    startHostedGame();
}

function copyShareLink() {
    const link = document.getElementById('shareLink').textContent;
    navigator.clipboard.writeText(link).then(() => {
        alert('Link copied to clipboard!');
    });
}

function startHostedGame() {
    // Connect to WebSocket server
    const serverUrl = getServerUrl();
    const playerId = generatePlayerId();

    try {
        ws = new WebSocket(serverUrl);

        ws.addEventListener('open', () => {
            console.log('Connected to server');

            // Send join message
            ws.send(JSON.stringify({
                type: 'join',
                roomId: roomId,
                playerId: playerId,
                playerNumber: playerNumber
            }));

            // For host, show waiting screen instead of game panel
            if (isHost) {
                document.getElementById('mainMenu').style.display = 'none';
                document.getElementById('hostMenu').style.display = 'block';
                document.getElementById('waitingStatus').style.display = 'block';
            } else {
                // For guest, show keybind configuration panel
                document.getElementById('menuPanel').style.display = 'none';
                document.getElementById('guestKeybindPanel').style.display = 'flex';
                document.getElementById('guestKeybindPanel').style.flexDirection = 'column';
                document.getElementById('guestKeybindPanel').style.justifyContent = 'center';
            }
        });

        ws.addEventListener('message', (event) => {
            const message = JSON.parse(event.data);

            if (message.type === 'joinConfirm') {
                console.log('Joined room:', message.roomId);
                gameState = message.gameState;

                // If this is the host, update the waiting status
                if (isHost) {
                    document.getElementById('gameStatus').textContent = '';
                } else {
                    document.getElementById('gameStatus').textContent = 'Waiting for host to start...';
                }
            }

            if (message.type === 'playerJoined') {
                console.log('Player 2 joined! Game can now start.');

                if (isHost) {
                    // Enable the start button for host
                    const startBtn = document.getElementById('startGameBtn');
                    startBtn.disabled = false;
                    startBtn.classList.remove('button-disabled');
                    startBtn.style.cursor = 'pointer';
                    startBtn.textContent = '🎮 Start Game';
                    startBtn.onclick = startGame;

                    document.getElementById('waitingStatus').style.display = 'none';
                    document.getElementById('gameStatus').innerHTML = '<strong style="color: #51cf66;">✓ Player 2 joined! Ready to play!</strong>';
                }
            }

            if (message.type === 'playerKeybinds') {
                console.log('Received other player keybinds:', message.keybinds);
                otherPlayerKeybinds = message.keybinds;
            }

            if (message.type === 'gameStarted') {
                console.log('Host started the game!');
                // Hide keybind panel and show game
                document.getElementById('guestKeybindPanel').style.display = 'none';
                document.getElementById('gamePanel').classList.add('active');
                document.getElementById('gameStatus').textContent = 'Game started! Both players connected.';
                startGameLoop();
            }

            if (message.type === 'gameState') {
                gameState = message.data;
                updateScore();

                // Check for game over
                if (gameState.winner) {
                    showGameOverModal();
                }
            }
        });

        ws.addEventListener('close', () => {
            console.log('Disconnected from server');
            document.getElementById('gameStatus').innerHTML = '<span style="color: #ff6b6b;">Disconnected from server</span>';
        });

        ws.addEventListener('error', (error) => {
            console.error('WebSocket error:', error);
            document.getElementById('gameStatus').innerHTML = '<span style="color: #ff6b6b;">Connection error. Make sure server is running.</span>';
        });
    } catch (error) {
        console.error('Connection error:', error);
        alert('Failed to connect to server: ' + error.message);
    }
}

function startGame() {
    // Host starts the game - show game panel
    document.getElementById('hostMenu').style.display = 'none';
    document.getElementById('menuPanel').style.display = 'none';
    document.getElementById('gamePanel').classList.add('active');
    document.getElementById('gameStatus').textContent = 'Game started! Both players connected.';
    startGameLoop();

    // Send start message to server
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'startGame'
        }));
    }
}

function handleInput() {
    const controls = {
        up: playerKeybinds.upKey ? keys[playerKeybinds.upKey] : false,
        down: playerKeybinds.downKey ? keys[playerKeybinds.downKey] : false
    };

    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'input',
            controls: controls
        }));
    }
}

function updateScore() {
    document.getElementById('score1').textContent = gameState.player1Score;
    document.getElementById('score2').textContent = gameState.player2Score;
}

function startGameLoop() {
    if (animationId) cancelAnimationFrame(animationId);

    const draw = () => {
        // Clear canvas
        ctx.fillStyle = '#000';
        ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw center line
        ctx.strokeStyle = '#fff';
        ctx.setLineDash([5, 5]);
        ctx.beginPath();
        ctx.moveTo(CANVAS_WIDTH / 2, 0);
        ctx.lineTo(CANVAS_WIDTH / 2, CANVAS_HEIGHT);
        ctx.stroke();
        ctx.setLineDash([]);

        // Draw paddles
        ctx.fillStyle = '#fff';
        // Player 1 (left paddle)
        ctx.fillRect(0, gameState.paddle1Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        // Player 2 (right paddle)
        ctx.fillRect(CANVAS_WIDTH - PADDLE_WIDTH, gameState.paddle2Y, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        ctx.fillStyle = '#fff';
        ctx.beginPath();
        ctx.arc(gameState.ballX, gameState.ballY, BALL_SIZE / 2, 0, Math.PI * 2);
        ctx.fill();

        // Draw scores
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 24px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(gameState.player1Score, CANVAS_WIDTH / 4, 30);
        ctx.fillText(gameState.player2Score, (CANVAS_WIDTH * 3) / 4, 30);

        animationId = requestAnimationFrame(draw);
    };

    draw();
}

function restartGame() {
    gameState.player1Score = 0;
    gameState.player2Score = 0;
    gameState.winner = null;
    document.getElementById('gameOverModal').classList.remove('active');
    document.getElementById('restartBtn').style.display = 'none';

    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            type: 'restart'
        }));
    }
}

function exitGame() {
    if (animationId) cancelAnimationFrame(animationId);
    if (ws) ws.close();

    // Reset game state
    listeningForKey = null;

    document.getElementById('menuPanel').style.display = 'flex';
    document.getElementById('gamePanel').classList.remove('active');
    document.getElementById('gameOverModal').classList.remove('active');
    document.getElementById('guestKeybindPanel').style.display = 'none';
    document.getElementById('mainMenu').style.display = 'block';
    document.getElementById('hostMenu').style.display = 'none';
    document.getElementById('joinMenu').style.display = 'none';
}

function showGameOverModal() {
    const winnerText = document.getElementById('winnerText');
    if (gameState.winner === 1) {
        winnerText.textContent = 'Player 1 Wins!';
    } else {
        winnerText.textContent = 'Player 2 Wins!';
    }

    document.getElementById('gameOverModal').classList.add('active');
    document.getElementById('restartBtn').style.display = 'block';
}

// Check for room ID in URL
window.addEventListener('load', () => {
    const params = new URLSearchParams(window.location.search);
    const roomFromUrl = params.get('room');

    if (roomFromUrl) {
        document.getElementById('roomIdInput').value = roomFromUrl;
        isHost = false;
        playerNumber = 2;
        roomId = roomFromUrl;
        startHostedGame();
    }
});
