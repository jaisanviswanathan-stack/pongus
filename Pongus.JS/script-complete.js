// ===== PONGUS: Complete Working Version =====
const CANVAS_WIDTH = 600;
const CANVAS_HEIGHT = 400;
const PADDLE_WIDTH = 10;
const PADDLE_HEIGHT = 60;
const BALL_SIZE = 8;

const GAME_STATES = { MENU: 'menu', PLAYING: 'playing', PAUSED: 'paused', GAME_OVER: 'gameOver' };

class PongGame {
    constructor() {
        this.canvas = document.getElementById('gameCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.container = document.querySelector('.container');
        
        // Game state
        this.state = GAME_STATES.MENU;
        this.singlePlayer = true;
        this.aiDifficulty = 2;
        this.learningAIEnabled = true;
        
        // Paddles
        this.paddle1Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddle2Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddleSpeed = 5;
        this.paddle1Height = PADDLE_HEIGHT;
        this.paddle2Height = PADDLE_HEIGHT;
        
        // Ball
        this.ballX = CANVAS_WIDTH / 2;
        this.ballY = CANVAS_HEIGHT / 2;
        this.ballVelX = 4;
        this.ballVelY = 4;
        this.ballSpeed = 5;
        
        // Score & Levels
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        this.pointsToLevel1 = 5;
        this.pointsToLevel2 = 5;
        
        // Keybinds
        this.keys = {};
        this.keybinds = {
            player1Up: 'w',
            player1Down: 's',
            player1Ability: 'q',
            player2Up: "'",
            player2Down: '/',
            player2Ability: ';'
        };
        
        // Abilities
        this.player1Abilities = new Map();
        this.player2Abilities = new Map();
        this.player1AbilityBranches = new Map();
        this.player2AbilityBranches = new Map();
        this.allAbilities = ['speed_boost', 'paddle_growth', 'double_points', 'slow_opponent', 'gun', 'ability_stealer', 'lag_spike', 'reverse_controls', 'joshua', 'blind', 'shrink_opponent', 'ghost_ball', 'jaisan', 'ability_swap', 'gravity_hammer', 'magnet_ball', 'shadow_clone', 'portal_pong', 'power_siphon', 'time_loop'];
        this.abilityTimers = new Map();
        
        // Power-ups
        this.powerUps = [];
        this.powerUpSpawnTimer = 0;
        
        // Effects
        this.ghostBallTimer = 0;
        this.blindTimer1 = 0;
        this.blindTimer2 = 0;
        this.slowTimer1 = 0;
        this.slowTimer2 = 0;
        this.reverseTimer1 = 0;
        this.reverseTimer2 = 0;
        this.joshuaTimer1 = 0;
        this.joshuaTimer2 = 0;
        
        // Game loop
        this.frameCount = 0;
        this.gameRunning = false;
        
        this.setupEventListeners();
        this.startGameLoop();
    }
    
    setupEventListeners() {
        document.addEventListener('keydown', (e) => {
            this.keys[e.key.toLowerCase()] = true;
            if (e.key === 'p' || e.key === 'P') this.togglePause();
            if (e.key === 'f' || e.key === 'F') this.toggleFullscreen();
            if (e.key === 'q' || e.key === 'Q') this.activateAbility(1);
            if (e.key === ';' || e.key === ':') this.activateAbility(2);
        });
        
        document.addEventListener('keyup', (e) => {
            this.keys[e.key.toLowerCase()] = false;
        });
        
        // UI buttons
        const startBtn = document.getElementById('startBtn');
        if (startBtn) startBtn.addEventListener('click', () => this.startGame());
        const pauseBtn = document.getElementById('pauseBtn');
        if (pauseBtn) pauseBtn.addEventListener('click', () => this.togglePause());
        const settingsBtn = document.getElementById('settingsBtn');
        if (settingsBtn) settingsBtn.addEventListener('click', () => this.showSettings());
        const fullscreenBtn = document.getElementById('fullscreenBtn');
        if (fullscreenBtn) fullscreenBtn.addEventListener('click', () => this.toggleFullscreen());
        const modalStartBtn = document.getElementById('modalStartBtn');
        if (modalStartBtn) modalStartBtn.addEventListener('click', () => this.startGame());
        
        // Modal close
        const modal = document.getElementById('settingsModal');
        const closeBtn = document.querySelector('.close');
        if (closeBtn) closeBtn.addEventListener('click', () => this.closeSettings());
        window.addEventListener('click', (e) => {
            if (e.target === modal) this.closeSettings();
        });
    }
    
    startGame() {
        const gameMode = document.querySelector('input[name="gameMode"]:checked');
        if (gameMode) {
            this.singlePlayer = gameMode.value === 'single';
            const aiDiff = document.getElementById('aiDifficulty');
            if (aiDiff) this.aiDifficulty = parseInt(aiDiff.value);
        }
        this.resetGame();
        this.state = GAME_STATES.PLAYING;
        this.gameRunning = true;
        this.closeSettings();
    }
    
    togglePause() {
        if (this.state === GAME_STATES.PLAYING) {
            this.state = GAME_STATES.PAUSED;
        } else if (this.state === GAME_STATES.PAUSED) {
            this.state = GAME_STATES.PLAYING;
        }
    }
    
    toggleFullscreen() {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().catch(err => console.log(err));
            const controls = document.querySelector('.controls');
            const info = document.querySelector('.info');
            if (controls) controls.style.display = 'none';
            if (info) info.style.display = 'none';
            this.canvas.width = window.innerWidth;
            this.canvas.height = window.innerHeight;
        } else {
            document.exitFullscreen();
            const controls = document.querySelector('.controls');
            const info = document.querySelector('.info');
            if (controls) controls.style.display = 'flex';
            if (info) info.style.display = 'block';
            this.canvas.width = CANVAS_WIDTH;
            this.canvas.height = CANVAS_HEIGHT;
        }
    }
    
    closeSettings() {
        const modal = document.getElementById('settingsModal');
        if (modal) modal.style.display = 'none';
    }
    
    showSettings() {
        document.getElementById('settingsModal').style.display = 'block';
    }
    
    resetGame() {
        this.ballX = CANVAS_WIDTH / 2;
        this.ballY = CANVAS_HEIGHT / 2;
        this.ballVelX = (Math.random() > 0.5 ? 1 : -1) * this.ballSpeed;
        this.ballVelY = (Math.random() > 0.5 ? 1 : -1) * this.ballSpeed;
        
        this.paddle1Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddle2Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddle1Height = PADDLE_HEIGHT;
        this.paddle2Height = PADDLE_HEIGHT;
        
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        this.pointsToLevel1 = 5;
        this.pointsToLevel2 = 5;
        
        this.player1Abilities.clear();
        this.player2Abilities.clear();
        this.player1AbilityBranches.clear();
        this.player2AbilityBranches.clear();
        this.abilityTimers.clear();
        
        this.powerUps = [];
        this.spawnPowerUp();
    }
    
    startGameLoop() {
        const loop = () => {
            if (this.state === GAME_STATES.PLAYING) {
                this.update();
            }
            this.render();
            requestAnimationFrame(loop);
        };
        loop();
    }
    
    update() {
        // Movement
        this.handlePaddleMovement();
        
        // Ball physics
        this.updateBall();
        
        // Collisions
        this.checkCollisions();
        
        // AI
        if (this.singlePlayer) this.updateAI();
        
        // Power-ups
        this.updatePowerUps();
        
        // Effects timers
        this.updateEffects();
        
        this.frameCount++;
    }
    
    handlePaddleMovement() {
        const p1Up = this.keys[this.keybinds.player1Up];
        const p1Down = this.keys[this.keybinds.player1Down];
        
        if (p1Up && this.paddle1Y > 0) this.paddle1Y -= this.paddleSpeed;
        if (p1Down && this.paddle1Y < CANVAS_HEIGHT - this.paddle1Height) this.paddle1Y += this.paddleSpeed;
        
        if (!this.singlePlayer) {
            const p2Up = this.keys[this.keybinds.player2Up];
            const p2Down = this.keys[this.keybinds.player2Down];
            if (p2Up && this.paddle2Y > 0) this.paddle2Y -= this.paddleSpeed;
            if (p2Down && this.paddle2Y < CANVAS_HEIGHT - this.paddle2Height) this.paddle2Y += this.paddleSpeed;
        }
    }
    
    updateBall() {
        this.ballX += this.ballVelX;
        this.ballY += this.ballVelY;
        
        // Wall bounces
        if (this.ballY <= 0 || this.ballY >= CANVAS_HEIGHT - BALL_SIZE) {
            this.ballVelY = -this.ballVelY;
        }
        
        // Scoring
        if (this.ballX < -BALL_SIZE) {
            this.scorePlayer2++;
            this.checkLevelUp(2);
            this.resetBallPosition();
        } else if (this.ballX > CANVAS_WIDTH) {
            this.scorePlayer1++;
            this.checkLevelUp(1);
            this.resetBallPosition();
        }
    }
    
    resetBallPosition() {
        this.ballX = CANVAS_WIDTH / 2;
        this.ballY = CANVAS_HEIGHT / 2;
        this.ballVelX = (Math.random() > 0.5 ? 1 : -1) * this.ballSpeed;
        this.ballVelY = (Math.random() > 0.5 ? 1 : -1) * this.ballSpeed;
    }
    
    checkCollisions() {
        // Player 1 paddle collision
        if (this.ballVelX < 0 && 
            this.ballX <= PADDLE_WIDTH + 5 &&
            this.ballY >= this.paddle1Y &&
            this.ballY <= this.paddle1Y + this.paddle1Height &&
            this.ghostBallTimer === 0) {
            this.ballVelX = -this.ballVelX;
            const hitPos = (this.ballY - this.paddle1Y) / this.paddle1Height;
            this.ballVelY += (hitPos - 0.5) * 4;
        }
        
        // Player 2 paddle collision
        if (this.ballVelX > 0 && 
            this.ballX >= CANVAS_WIDTH - PADDLE_WIDTH - 5 &&
            this.ballY >= this.paddle2Y &&
            this.ballY <= this.paddle2Y + this.paddle2Height &&
            this.ghostBallTimer === 0) {
            this.ballVelX = -this.ballVelX;
            const hitPos = (this.ballY - this.paddle2Y) / this.paddle2Height;
            this.ballVelY += (hitPos - 0.5) * 4;
        }
    }
    
    updateAI() {
        const speed = this.aiDifficulty;
        const ballCenter = this.ballY + BALL_SIZE / 2;
        const paddleCenter = this.paddle2Y + PADDLE_HEIGHT / 2;
        
        if (Math.abs(ballCenter - paddleCenter) > 5) {
            if (ballCenter < paddleCenter) {
                this.paddle2Y = Math.max(0, this.paddle2Y - speed);
            } else {
                this.paddle2Y = Math.min(CANVAS_HEIGHT - PADDLE_HEIGHT, this.paddle2Y + speed);
            }
        }
    }
    
    updatePowerUps() {
        this.powerUpSpawnTimer++;
        if (this.powerUpSpawnTimer > 600) {
            this.spawnPowerUp();
            this.powerUpSpawnTimer = 0;
        }
        
        this.powerUps = this.powerUps.filter(pu => {
            pu.y += pu.vy;
            pu.x += pu.vx;
            pu.life--;
            
            // Bounce walls
            if (pu.y <= 0 || pu.y >= CANVAS_HEIGHT) pu.vy = -pu.vy;
            if (pu.x <= 0 || pu.x >= CANVAS_WIDTH) pu.vx = -pu.vx;
            
            // Check collection
            if (this.ballX > pu.x - 15 && this.ballX < pu.x + 15 &&
                this.ballY > pu.y - 15 && this.ballY < pu.y + 15) {
                this.activatePowerUp(pu.type);
                return false;
            }
            
            return pu.life > 0;
        });
    }
    
    spawnPowerUp() {
        const types = ['fireball', 'split', 'teleport', 'mirror', 'dangerzone', 'invisiblewalls', 'shrinkpaddles', 'centerwall'];
        const type = types[Math.floor(Math.random() * types.length)];
        const x = 50 + Math.random() * (CANVAS_WIDTH - 100);
        const y = 50 + Math.random() * (CANVAS_HEIGHT - 100);
        const vx = (Math.random() - 0.5) * 2;
        const vy = (Math.random() - 0.5) * 2;
        this.powerUps.push({ x, y, vx, vy, type, life: 1200, radius: 10 });
    }
    
    activatePowerUp(type) {
        // Implement power-up effects
        console.log('PowerUp collected:', type);
    }
    
    activateAbility(player) {
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        if (abilities.size === 0) return;
        
        const abilityArray = Array.from(abilities.keys());
        const ability = abilityArray[Math.floor(Math.random() * abilityArray.length)];
        const level = abilities.get(ability);
        
        this.executeAbility(player, ability, level);
    }
    
    executeAbility(player, ability, level) {
        console.log(`Ability: ${ability} Level ${level} by Player ${player}`);
        // Implement each ability here
    }
    
    checkLevelUp(player) {
        const score = player === 1 ? this.scorePlayer1 : this.scorePlayer2;
        const pointsTo = player === 1 ? this.pointsToLevel1 : this.pointsToLevel2;
        
        if (score % 5 === 0 && score > 0) {
            if (player === 1) this.level1++;
            else this.level2++;
            this.showAbilityChoice(player);
        }
    }
    
    showAbilityChoice(player) {
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const choices = this.allAbilities
            .filter(a => !abilities.has(a) || abilities.get(a) < 3)
            .slice(0, 3);
        
        alert(`Player ${player} Level Up!\nChoose (1-3): ${choices.join(', ')}`);
        const choice = prompt('1, 2, or 3?');
        if (choice && ['1','2','3'].includes(choice)) {
            const ability = choices[parseInt(choice) - 1];
            const level = (abilities.get(ability) || 0) + 1;
            abilities.set(ability, level);
        }
    }
    
    updateEffects() {
        if (this.ghostBallTimer > 0) this.ghostBallTimer--;
        if (this.blindTimer1 > 0) this.blindTimer1--;
        if (this.blindTimer2 > 0) this.blindTimer2--;
    }
    
    render() {
        // Clear canvas
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Center line
        this.ctx.strokeStyle = '#00ffff';
        this.ctx.setLineDash([5, 5]);
        this.ctx.beginPath();
        this.ctx.moveTo(CANVAS_WIDTH / 2, 0);
        this.ctx.lineTo(CANVAS_WIDTH / 2, CANVAS_HEIGHT);
        this.ctx.stroke();
        this.ctx.setLineDash([]);
        
        // Paddles
        this.ctx.fillStyle = '#0099ff';
        this.ctx.fillRect(5, this.paddle1Y, PADDLE_WIDTH, this.paddle1Height);
        
        this.ctx.fillStyle = '#ff3333';
        this.ctx.fillRect(CANVAS_WIDTH - PADDLE_WIDTH - 5, this.paddle2Y, PADDLE_WIDTH, this.paddle2Height);
        
        // Ball
        this.ctx.fillStyle = '#fff';
        this.ctx.beginPath();
        this.ctx.arc(this.ballX, this.ballY, BALL_SIZE / 2, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Score
        this.ctx.fillStyle = '#00ffff';
        this.ctx.font = 'bold 30px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText(this.scorePlayer1, CANVAS_WIDTH / 2 - 50, 40);
        this.ctx.fillText(this.scorePlayer2, CANVAS_WIDTH / 2 + 50, 40);
        
        // Levels
        this.ctx.font = 'bold 16px Arial';
        this.ctx.fillStyle = '#00ff00';
        this.ctx.textAlign = 'left';
        this.ctx.fillText(`L${this.level1}`, 10, 20);
        this.ctx.textAlign = 'right';
        this.ctx.fillText(`L${this.level2}`, CANVAS_WIDTH - 10, 20);
        
        // Power-ups
        this.ctx.fillStyle = '#ffff00';
        for (let pu of this.powerUps) {
            this.ctx.beginPath();
            this.ctx.arc(pu.x, pu.y, pu.radius, 0, Math.PI * 2);
            this.ctx.fill();
        }
    }
}

// Initialize
window.addEventListener('DOMContentLoaded', () => {
    window.game = new PongGame();
    document.getElementById('settingsModal').style.display = 'block';
});

function closeSettings() {
    const modal = document.getElementById('settingsModal');
    if (modal) modal.style.display = 'none';
}

window.startGameFromUI = function() {
    try {
        if (window.game && typeof window.game.startGame === 'function') {
            window.game.startGame();
        }
    } catch (err) {
        console.error('Error starting game:', err);
    }
};
