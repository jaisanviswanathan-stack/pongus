// ===== GAME CONSTANTS =====
const CANVAS_WIDTH = 600;
const CANVAS_HEIGHT = 400;
const PADDLE_WIDTH = 10;
const PADDLE_HEIGHT = 60;
const BALL_SIZE = 15;

// Game states
const GAME_STATES = {
    MENU: 'menu',
    PLAYING: 'playing',
    PAUSED: 'paused',
    GAME_OVER: 'gameOver'
};

// ===== GAME CLASS =====
class PongGame {
    constructor() {
        this.canvas = document.getElementById('gameCanvas');
        this.ctx = this.canvas.getContext('2d');
        
        // Game state
        this.state = GAME_STATES.MENU;
        this.isPaused = false;
        this.singlePlayer = true;
        this.aiDifficulty = 2;
        this.learningAIEnabled = true;
        
        // Paddles
        this.paddle1Y = 170;
        this.paddle2Y = 170;
        this.paddleSpeed = 5;
        
        // Ball
        this.ballX = 292.5;
        this.ballY = 192.5;
        this.ballVelX = 4;
        this.ballVelY = 4;
        
        // Score and levels
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        
        // Input tracking
        this.keys = {};
        this.keybinds = {
            player1Up: 'w',
            player1Down: 's',
            player1Ability: 'q',
            player2Up: 'ArrowUp',
            player2Down: 'ArrowDown',
            player2Ability: '/'
        };
        
        // Abilities
        this.player1Abilities = new Map();
        this.player2Abilities = new Map();
        
        // Power-ups
        this.powerUps = [];
        this.powerUpSpawnTimer = 0;
        this.powerUpSpawnInterval = 500;
        
        // Learning AI
        this.playerPositionHistory = [];
        this.learnedAveragePosition = 200;
        this.learnedReactionSpeed = 0.3;
        this.learnedAggressiveness = 0.5;
        this.playerSkillLevel = 0.5;
        this.learningProgress = 0;
        this.learningDataPoints = 0;
        this.maxLearningData = 1000;
        
        // Visual effects
        this.particles = [];
        this.explosions = [];
        this.mirrors = [];
        this.bullets = [];
        
        // Game timer
        this.frameCount = 0;
        this.lastFrameTime = Date.now();
        
        // Setup listeners
        this.setupEventListeners();
        this.startGameLoop();
    }
    
    setupEventListeners() {
        document.addEventListener('keydown', (e) => {
            const key = e.key.toLowerCase();
            this.keys[key] = true;
            
            if (e.key === 'p' || e.key === 'P') {
                this.togglePause();
            }
        });
        
        document.addEventListener('keyup', (e) => {
            const key = e.key.toLowerCase();
            this.keys[key] = false;
        });
        
        // UI button listeners
        document.getElementById('startBtn').addEventListener('click', () => this.startGame());
        document.getElementById('pauseBtn').addEventListener('click', () => this.togglePause());
        document.getElementById('settingsBtn').addEventListener('click', () => this.showSettings());
        
        // Settings modal
        const modal = document.getElementById('settingsModal');
        const closeBtn = document.querySelector('.close');
        closeBtn.addEventListener('click', () => this.closeSettings());
        window.addEventListener('click', (e) => {
            if (e.target === modal) this.closeSettings();
        });
    }
    
    startGame() {
        const gameMode = document.querySelector('input[name="gameMode"]:checked').value;
        this.singlePlayer = gameMode === 'single';
        this.aiDifficulty = parseInt(document.getElementById('aiDifficulty').value);
        this.learningAIEnabled = document.getElementById('enableLearningAI').checked;
        
        this.resetGame();
        this.state = GAME_STATES.PLAYING;
        this.closeSettings();
    }
    
    resetGame() {
        this.ballX = CANVAS_WIDTH / 2 - BALL_SIZE / 2;
        this.ballY = CANVAS_HEIGHT / 2 - BALL_SIZE / 2;
        this.ballVelX = 4 * (Math.random() > 0.5 ? 1 : -1);
        this.ballVelY = 4 * (Math.random() > 0.5 ? 1 : -1);
        
        this.paddle1Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddle2Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        
        this.player1Abilities.clear();
        this.player2Abilities.clear();
        this.powerUps = [];
        this.bullets = [];
        this.particles = [];
    }
    
    togglePause() {
        if (this.state === GAME_STATES.PLAYING) {
            this.state = GAME_STATES.PAUSED;
        } else if (this.state === GAME_STATES.PAUSED) {
            this.state = GAME_STATES.PLAYING;
        }
    }
    
    showSettings() {
        document.getElementById('settingsModal').style.display = 'block';
    }
    
    closeSettings() {
        document.getElementById('settingsModal').style.display = 'none';
    }
    
    startGameLoop() {
        const gameLoop = () => {
            if (this.state === GAME_STATES.PLAYING) {
                this.update();
            }
            this.render();
            requestAnimationFrame(gameLoop);
        };
        gameLoop();
    }
    
    update() {
        this.frameCount++;
        
        // Handle paddle movement
        this.handlePaddleMovement();
        
        // Update ball
        this.updateBall();
        
        // Update AI
        if (this.singlePlayer) {
            this.updateAI();
        }
        
        // Update power-ups
        this.updatePowerUps();
        
        // Update particles and effects
        this.updateParticles();
        this.updateBullets();
        
        // Check collisions
        this.checkCollisions();
        
        // Update learning AI
        if (this.singlePlayer && this.learningAIEnabled && this.aiDifficulty === 4) {
            this.updateLearningAI();
        }
        
        // Check win condition
        if (this.scorePlayer1 >= 11 || this.scorePlayer2 >= 11) {
            this.state = GAME_STATES.GAME_OVER;
        }
    }
    
    handlePaddleMovement() {
        // Player 1 movement
        if (this.keys[this.keybinds.player1Up] && this.paddle1Y > 0) {
            this.paddle1Y -= this.paddleSpeed;
        }
        if (this.keys[this.keybinds.player1Down] && this.paddle1Y < CANVAS_HEIGHT - PADDLE_HEIGHT) {
            this.paddle1Y += this.paddleSpeed;
        }
        
        // Player 2 movement (or AI)
        if (!this.singlePlayer) {
            if (this.keys[this.keybinds.player2Up] && this.paddle2Y > 0) {
                this.paddle2Y -= this.paddleSpeed;
            }
            if (this.keys[this.keybinds.player2Down] && this.paddle2Y < CANVAS_HEIGHT - PADDLE_HEIGHT) {
                this.paddle2Y += this.paddleSpeed;
            }
        }
    }
    
    updateBall() {
        this.ballX += this.ballVelX;
        this.ballY += this.ballVelY;
        
        // Top and bottom collisions
        if (this.ballY <= 0 || this.ballY + BALL_SIZE >= CANVAS_HEIGHT) {
            this.ballVelY = -this.ballVelY;
            this.ballY = Math.max(0, Math.min(this.ballY, CANVAS_HEIGHT - BALL_SIZE));
        }
        
        // Left and right boundaries (scoring)
        if (this.ballX < 0) {
            this.scorePlayer2++;
            this.resetBallPosition('right');
        } else if (this.ballX > CANVAS_WIDTH) {
            this.scorePlayer1++;
            this.resetBallPosition('left');
        }
    }
    
    resetBallPosition(direction) {
        this.ballX = CANVAS_WIDTH / 2 - BALL_SIZE / 2;
        this.ballY = CANVAS_HEIGHT / 2 - BALL_SIZE / 2;
        this.ballVelX = 4 * (direction === 'left' ? 1 : -1);
        this.ballVelY = 4 * (Math.random() > 0.5 ? 1 : -1);
    }
    
    updateAI() {
        const aiSpeed = this.getAISpeed();
        const paddle2CenterY = this.paddle2Y + PADDLE_HEIGHT / 2;
        const ballCenterY = this.ballY + BALL_SIZE / 2;
        
        // AI tracks the ball
        if (Math.abs(ballCenterY - paddle2CenterY) > 5) {
            if (ballCenterY < paddle2CenterY) {
                this.paddle2Y = Math.max(0, this.paddle2Y - aiSpeed);
            } else {
                this.paddle2Y = Math.min(CANVAS_HEIGHT - PADDLE_HEIGHT, this.paddle2Y + aiSpeed);
            }
        }
    }
    
    getAISpeed() {
        const baseSpeeds = [3, 5, 7, 9]; // Easy to Impossible
        const speedIndex = Math.min(this.aiDifficulty - 1, 3);
        let speed = baseSpeeds[speedIndex];
        
        // Learning AI adjustment
        if (this.learningAIEnabled && this.aiDifficulty === 4) {
            speed = Math.min(speed, 3 + (this.playerSkillLevel * 6));
        }
        
        return speed;
    }
    
    updateLearningAI() {
        const paddle1CenterY = this.paddle1Y + PADDLE_HEIGHT / 2;
        const ballCenterY = this.ballY + BALL_SIZE / 2;
        
        // Track player position
        this.playerPositionHistory.push(paddle1CenterY);
        if (this.playerPositionHistory.length > this.maxLearningData) {
            this.playerPositionHistory.shift();
        }
        
        // Calculate learned average position
        if (this.playerPositionHistory.length > 0) {
            const sum = this.playerPositionHistory.reduce((a, b) => a + b, 0);
            this.learnedAveragePosition = sum / this.playerPositionHistory.length;
        }
        
        // Update learning progress
        this.learningDataPoints = Math.min(this.maxLearningData, this.playerPositionHistory.length);
        this.learningProgress = Math.min(1.0, this.learningDataPoints / 600.0);
        
        // Estimate player skill based on ball returns
        if (this.frameCount % 100 === 0) {
            // Measure success rate (player hitting ball on left side)
            if (this.ballX < 50 && this.ballVelX < 0) {
                const hitSuccess = Math.abs(ballCenterY - paddle1CenterY) < 40;
                if (hitSuccess) {
                    this.playerSkillLevel = Math.min(0.9, this.playerSkillLevel + 0.01);
                } else {
                    this.playerSkillLevel = Math.max(0.2, this.playerSkillLevel - 0.01);
                }
            }
        }
    }
    
    updatePowerUps() {
        this.powerUpSpawnTimer++;
        if (this.powerUpSpawnTimer >= this.powerUpSpawnInterval) {
            this.spawnPowerUp();
            this.powerUpSpawnTimer = 0;
        }
        
        // Update existing power-ups
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            const pu = this.powerUps[i];
            pu.y += pu.velocityY;
            
            // Remove if off-screen
            if (pu.y < 0 || pu.y > CANVAS_HEIGHT) {
                this.powerUps.splice(i, 1);
            }
        }
    }
    
    spawnPowerUp() {
        const types = ['speed', 'paddle_grow', 'slow_opponent', 'multiball'];
        const randomType = types[Math.floor(Math.random() * types.length)];
        const x = 100 + Math.random() * 400;
        const y = 0;
        
        this.powerUps.push({
            x: x,
            y: y,
            velocityY: 2,
            type: randomType,
            size: 20,
            active: true
        });
    }
    
    updateParticles() {
        for (let i = this.particles.length - 1; i >= 0; i--) {
            const p = this.particles[i];
            p.life--;
            p.x += p.velX;
            p.y += p.velY;
            
            if (p.life <= 0) {
                this.particles.splice(i, 1);
            }
        }
    }
    
    updateBullets() {
        for (let i = this.bullets.length - 1; i >= 0; i--) {
            const bullet = this.bullets[i];
            bullet.x += bullet.velX;
            bullet.y += bullet.velY;
            
            // Remove if off-screen
            if (bullet.x < 0 || bullet.x > CANVAS_WIDTH || bullet.y < 0 || bullet.y > CANVAS_HEIGHT) {
                this.bullets.splice(i, 1);
            }
        }
    }
    
    checkCollisions() {
        // Ball-paddle 1 collision
        if (this.ballX < 20 && this.ballX + BALL_SIZE > 10 &&
            this.ballY < this.paddle1Y + PADDLE_HEIGHT && this.ballY + BALL_SIZE > this.paddle1Y) {
            
            if (this.ballVelX < 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = 20;
                
                // Add spin based on paddle position
                const paddleCenter = this.paddle1Y + PADDLE_HEIGHT / 2;
                const ballCenter = this.ballY + BALL_SIZE / 2;
                const hitOffset = (ballCenter - paddleCenter) / (PADDLE_HEIGHT / 2);
                this.ballVelY += hitOffset * 3;
                
                // Create impact effect
                this.createImpactEffect(this.ballX, this.ballY);
            }
        }
        
        // Ball-paddle 2 collision
        if (this.ballX + BALL_SIZE > CANVAS_WIDTH - 20 && this.ballX < CANVAS_WIDTH - 10 &&
            this.ballY < this.paddle2Y + PADDLE_HEIGHT && this.ballY + BALL_SIZE > this.paddle2Y) {
            
            if (this.ballVelX > 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = CANVAS_WIDTH - 20 - BALL_SIZE;
                
                // Add spin based on paddle position
                const paddleCenter = this.paddle2Y + PADDLE_HEIGHT / 2;
                const ballCenter = this.ballY + BALL_SIZE / 2;
                const hitOffset = (ballCenter - paddleCenter) / (PADDLE_HEIGHT / 2);
                this.ballVelY += hitOffset * 3;
                
                // Create impact effect
                this.createImpactEffect(this.ballX, this.ballY);
            }
        }
        
        // Power-up collision
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            const pu = this.powerUps[i];
            
            // Check collision with player 1
            if (pu.x > 10 && pu.x < 20 && pu.y > this.paddle1Y && pu.y < this.paddle1Y + PADDLE_HEIGHT) {
                this.applyPowerUp(1, pu.type);
                this.powerUps.splice(i, 1);
                continue;
            }
            
            // Check collision with player 2
            if (pu.x > CANVAS_WIDTH - 20 && pu.x < CANVAS_WIDTH - 10 && 
                pu.y > this.paddle2Y && pu.y < this.paddle2Y + PADDLE_HEIGHT) {
                this.applyPowerUp(2, pu.type);
                this.powerUps.splice(i, 1);
                continue;
            }
        }
    }
    
    createImpactEffect(x, y) {
        for (let i = 0; i < 8; i++) {
            const angle = (Math.PI * 2 * i) / 8;
            const speed = 2 + Math.random() * 2;
            this.particles.push({
                x: x + BALL_SIZE / 2,
                y: y + BALL_SIZE / 2,
                velX: Math.cos(angle) * speed,
                velY: Math.sin(angle) * speed,
                life: 20,
                size: 3,
                color: 'rgba(255, 255, 255, 0.8)'
            });
        }
    }
    
    applyPowerUp(player, type) {
        console.log(`Player ${player} got power-up: ${type}`);
        // Implement power-up effects here
        if (type === 'speed') {
            this.ballVelX *= 1.2;
            this.ballVelY *= 1.2;
        } else if (type === 'paddle_grow') {
            // Extend paddle (not implemented in basic version)
        }
    }
    
    render() {
        // Clear canvas
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Draw background gradient
        const gradient = this.ctx.createLinearGradient(0, 0, 0, CANVAS_HEIGHT);
        gradient.addColorStop(0, '#0a1428');
        gradient.addColorStop(1, '#141f2a');
        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Draw center line
        this.ctx.strokeStyle = 'rgba(150, 200, 255, 0.5)';
        this.ctx.setLineDash([10, 10]);
        this.ctx.beginPath();
        this.ctx.moveTo(CANVAS_WIDTH / 2, 0);
        this.ctx.lineTo(CANVAS_WIDTH / 2, CANVAS_HEIGHT);
        this.ctx.stroke();
        this.ctx.setLineDash([]);
        
        if (this.state === GAME_STATES.PLAYING) {
            // Draw paddles
            this.drawPaddle(10, this.paddle1Y, '#00ffff');
            this.drawPaddle(CANVAS_WIDTH - 20, this.paddle2Y, '#ff6464');
            
            // Draw ball
            this.drawBall();
            
            // Draw power-ups
            this.drawPowerUps();
            
            // Draw particles
            this.drawParticles();
            
            // Draw score
            this.drawScore();
            
            // Draw status info
            this.drawStatusInfo();
        } else if (this.state === GAME_STATES.PAUSED) {
            this.drawPaused();
        } else if (this.state === GAME_STATES.GAME_OVER) {
            this.drawGameOver();
        } else if (this.state === GAME_STATES.MENU) {
            this.drawMenu();
        }
    }
    
    drawPaddle(x, y, color) {
        const gradient = this.ctx.createLinearGradient(x, y, x + PADDLE_WIDTH, y);
        gradient.addColorStop(0, color);
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0.3)');
        
        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Glow effect
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(x - 2, y - 2, PADDLE_WIDTH + 4, PADDLE_HEIGHT + 4);
    }
    
    drawBall() {
        const gradient = this.ctx.createRadialGradient(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 0,
            this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2);
        
        gradient.addColorStop(0, '#ffffff');
        gradient.addColorStop(0.6, '#00ffff');
        gradient.addColorStop(1, '#0099ff');
        
        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(this.ballX, this.ballY, BALL_SIZE, BALL_SIZE);
        
        // Glow
        this.ctx.shadowColor = '#00ffff';
        this.ctx.shadowBlur = 15;
        this.ctx.strokeStyle = '#00ffff';
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(this.ballX, this.ballY, BALL_SIZE, BALL_SIZE);
        this.ctx.shadowBlur = 0;
    }
    
    drawPowerUps() {
        for (const pu of this.powerUps) {
            this.ctx.fillStyle = '#ffff00';
            this.ctx.fillRect(pu.x - pu.size / 2, pu.y - pu.size / 2, pu.size, pu.size);
            
            // Rotate border
            this.ctx.save();
            this.ctx.translate(pu.x, pu.y);
            this.ctx.rotate((this.frameCount * 0.02) % (Math.PI * 2));
            this.ctx.strokeStyle = '#ffaa00';
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(-pu.size / 2, -pu.size / 2, pu.size, pu.size);
            this.ctx.restore();
        }
    }
    
    drawParticles() {
        for (const p of this.particles) {
            this.ctx.fillStyle = p.color;
            this.ctx.fillRect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }
    }
    
    drawScore() {
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ffff';
        this.ctx.textAlign = 'right';
        this.ctx.fillText(this.scorePlayer1, CANVAS_WIDTH / 2 - 30, 50);
        
        this.ctx.fillStyle = '#ff6464';
        this.ctx.textAlign = 'left';
        this.ctx.fillText(this.scorePlayer2, CANVAS_WIDTH / 2 + 30, 50);
    }
    
    drawStatusInfo() {
        this.ctx.font = '12px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.textAlign = 'left';
        
        let yPos = CANVAS_HEIGHT - 40;
        this.ctx.fillText(`FPS: ${Math.round(1000 / (Date.now() - this.lastFrameTime))}`, 10, yPos);
        this.ctx.fillText(`Ball Speed: ${Math.round(Math.sqrt(this.ballVelX ** 2 + this.ballVelY ** 2) * 10) / 10}`, 10, yPos + 15);
        
        if (this.singlePlayer && this.learningAIEnabled && this.aiDifficulty === 4) {
            this.ctx.fillText(`AI Learning: ${Math.round(this.learningProgress * 100)}%`, 10, yPos + 30);
        }
        
        this.lastFrameTime = Date.now();
    }
    
    drawPaused() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ffff';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('PAUSED', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
        
        this.ctx.font = '20px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText('Press P to resume', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 40);
    }
    
    drawGameOver() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        const winner = this.scorePlayer1 > this.scorePlayer2 ? 'PLAYER 1' : 'PLAYER 2';
        
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ff00';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('GAME OVER', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 40);
        
        this.ctx.font = 'bold 36px Arial';
        this.ctx.fillStyle = '#ffff00';
        this.ctx.fillText(`${winner} WINS!`, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 20);
        
        this.ctx.font = '20px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText(`${this.scorePlayer1} - ${this.scorePlayer2}`, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 70);
    }
    
    drawMenu() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        this.ctx.font = 'bold 36px Arial';
        this.ctx.fillStyle = '#00ffff';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('PONG', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 60);
        
        this.ctx.font = '18px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText('Click "Start Game" to begin', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 20);
    }
}

// Initialize game when page loads
window.addEventListener('DOMContentLoaded', () => {
    window.game = new PongGame();
});

// Modal functions
function closeSettings() {
    document.getElementById('settingsModal').style.display = 'none';
}
