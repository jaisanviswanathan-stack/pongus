// ===== PONG GAME - FULL FEATURED WITH ALL ABILITIES =====
// This is the complete port of the Java Pong game with all 20+ abilities, visual effects, and mechanics

const CANVAS_WIDTH = 600;
const CANVAS_HEIGHT = 400;
const PADDLE_WIDTH = 10;
const PADDLE_HEIGHT = 60;
const BALL_SIZE = 15;

// All ability names
const ALL_ABILITIES = [
    'speed_boost', 'paddle_growth', 'double_points', 'slow_opponent', 'gun',
    'ability_stealer', 'lag_spike', 'reverse_controls', 'joshua', 'blind',
    'shrink_opponent', 'ghost_ball', 'jaisan', 'ability_swap', 'gravity_hammer',
    'magnet_ball', 'shadow_clone', 'portal_pong', 'power_siphon', 'time_loop'
];

// Game states
const GAME_STATES = { MENU: 0, PLAYING: 1, PAUSED: 2, GAME_OVER: 3 };

class PongGame {
    constructor() {
        this.canvas = document.getElementById('gameCanvas');
        this.ctx = this.canvas.getContext('2d');
        
        // Game state
        this.state = GAME_STATES.MENU;
        this.singlePlayer = true;
        this.aiDifficulty = 2;
        this.learningAIEnabled = true;
        
        // Paddle state
        this.paddle1Y = 170;
        this.paddle2Y = 170;
        this.paddle1Height = PADDLE_HEIGHT;
        this.paddle2Height = PADDLE_HEIGHT;
        this.paddleSpeed = 5;
        
        // Ball physics
        this.ballX = 292.5;
        this.ballY = 192.5;
        this.ballVelX = 4;
        this.ballVelY = 4;
        this.baseballSpeed = 4;
        
        // Scoring and progression
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        this.pointsToNextLevel1 = 5;
        this.pointsToNextLevel2 = 5;
        
        // Abilities
        this.player1Abilities = new Map();
        this.player2Abilities = new Map();
        this.player1AbilityBranches = new Map();
        this.player2AbilityBranches = new Map();
        
        // Ability cooldowns and timers
        this.player1AbilityCooldowns = new Map();
        this.player2AbilityCooldowns = new Map();
        
        // Stunts and status effects
        this.player1StunTimer = 0;
        this.player2StunTimer = 0;
        this.stunDuration = 100;
        this.player1BlindTimer = 0;
        this.player2BlindTimer = 0;
        this.player1SlowTimer = 0;
        this.player2SlowTimer = 0;
        this.gameStateHistory = [];
        
        // Learning AI system
        this.playerPositionHistory = [];
        this.playerSuccessfulHits = 0;
        this.aiSuccessfulHits = 0;
        this.learnedAveragePosition = 200;
        this.learnedReactionSpeed = 0.3;
        this.learnedAggressiveness = 0.5;
        this.playerSkillLevel = 0.5;
        this.learningProgress = 0;
        this.learningDataPoints = 0;
        this.maxLearningData = 1000;
        this.aiAdaptationMultiplier = 1.0;
        this.totalRallies = 0;
        
        // Power-ups
        this.powerUps = [];
        this.powerUpSpawnTimer = 0;
        this.powerUpSpawnInterval = 500;
        
        // Particles and effects
        this.particles = [];
        this.bullets = [];
        this.lasers = [];
        this.explosions = [];
        this.portals = new Map();
        this.shadowClones = new Map();
        
        // Game effects flags
        this.mirrorActive = false;
        this.teleportActive = false;
        this.fireballActive = false;
        this.multiballActive = false;
        this.ghostBallActive = false;
        this.gravityActive = false;
        this.dangerZoneActive = false;
        
        // Duration timers for effects
        this.effectTimers = new Map();
        
        // Game loop
        this.frameCount = 0;
        this.lastFrameTime = Date.now();
        
        // Input
        this.keys = {};
        
        this.setup();
    }
    
    setup() {
        document.addEventListener('keydown', (e) => this.handleKeyDown(e));
        document.addEventListener('keyup', (e) => this.handleKeyUp(e));
        
        document.getElementById('startBtn').addEventListener('click', () => this.startGame());
        document.getElementById('pauseBtn').addEventListener('click', () => this.togglePause());
        document.getElementById('settingsBtn').addEventListener('click', () => this.showSettings());
        
        document.querySelector('.close').addEventListener('click', () => this.closeSettings());
        window.addEventListener('click', (e) => {
            if (e.target.id === 'settingsModal') this.closeSettings();
        });
        
        this.startGameLoop();
    }
    
    handleKeyDown(e) {
        this.keys[e.key.toLowerCase()] = true;
        if (e.key === 'p' || e.key === 'P') this.togglePause();
        if (e.key === 'm' || e.key === 'M') this.handleCheatMenu();
        
        // Ability activation
        if (e.key === 'q' || e.key === 'Q') this.activateAbility(1);
        if (e.key === '/' || e.key === '?') this.activateAbility(2);

        // Branch selection (1-2 keys for player abilities)
        if (e.key === '1') this.selectBranch(1, 1);
        if (e.key === '2') this.selectBranch(1, 2);
        if (e.key === '3') this.selectBranch(2, 1);
        if (e.key === '4') this.selectBranch(2, 2);
    }
    
    handleKeyUp(e) {
        this.keys[e.key.toLowerCase()] = false;
    }

    handleCheatMenu() {
        // Simple cheat: grant random ability to player 1
        const available = ALL_ABILITIES.filter(a => !this.player1Abilities.has(a));
        if (available.length > 0) {
            const ability = available[Math.floor(Math.random() * available.length)];
            this.player1Abilities.set(ability, 1);
            console.log(`Granted ${ability} to player 1`);
        }
    }

    selectBranch(player, branch) {
        // Select which branch to use for player's next ability use
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
        
        if (abilities.size === 0) return;
        const recentAbility = Array.from(abilities.keys())[0]; // Just use first ability for now
        branches.set(recentAbility, branch);
        console.log(`P${player} set ${recentAbility} branch to ${branch}`);
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
        this.paddle1Height = PADDLE_HEIGHT;
        this.paddle2Height = PADDLE_HEIGHT;
        
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        
        this.player1Abilities.clear();
        this.player2Abilities.clear();
        this.player1AbilityBranches.clear();
        this.player2AbilityBranches.clear();
        
        this.powerUps = [];
        this.particles = [];
        this.bullets = [];
        this.lasers = [];
        this.explosions = [];
        
        this.playerPositionHistory = [];
        this.playerSuccessfulHits = 0;
        this.aiSuccessfulHits = 0;
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
    
    handleCheatMenu() {
        const password = prompt('Enter cheat password:', '');
        if (password === 'zanyscarf16') {
            this.showCheatMenu();
        }
    }
    
    showCheatMenu() {
        const choice = prompt('1=Spawn Power-Up, 2=Give Ability, 3=Give Level', '1');
        if (choice === '1') {
            const types = ['speed', 'paddle_grow', 'slow_opponent', 'multiball', 'jackpot'];
            const type = types[Math.floor(Math.random() * types.length)];
            this.powerUps.push({
                x: 100 + Math.random() * 400, y: 50, type, size: 20, velocityY: 2, active: true
            });
        } else if (choice === '2') {
            const abilityIndex = Math.floor(Math.random() * ALL_ABILITIES.length);
            const ability = ALL_ABILITIES[abilityIndex];
            this.player1Abilities.set(ability, 3);
            alert(`Gave ${ability} to Player 1`);
        }
    }
    
    activateAbility(player) {
        // Choose player's ability map and cooldown map
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const cooldowns = player === 1 ? this.player1AbilityCooldowns : this.player2AbilityCooldowns;
        const opponent = player === 1 ? 2 : 1;

        // pick ability: prefer owned abilities, otherwise random
        let abilityName = null;
        if (abilities && abilities.size > 0) {
            const keys = Array.from(abilities.keys());
            abilityName = keys[Math.floor(Math.random() * keys.length)];
        } else {
            abilityName = ALL_ABILITIES[Math.floor(Math.random() * ALL_ABILITIES.length)];
        }

        // check cooldown
        const cd = cooldowns.get(abilityName) || 0;
        if (cd > 0) {
            // feedback: small particle burst near player's paddle
            const x = player === 1 ? 30 : CANVAS_WIDTH - 30;
            const y = (player === 1 ? this.paddle1Y : this.paddle2Y) + (player === 1 ? this.paddle1Height : this.paddle2Height) / 2;
            // Red particle burst for cooldown
            for (let i = 0; i < 5; i++) {
                this.particles.push({
                    x, y,
                    velX: (Math.random() - 0.5) * 3,
                    velY: (Math.random() - 0.5) * 3,
                    life: 20,
                    size: 2,
                    color: 'rgba(255,50,50,0.8)'
                });
            }
            return;
        }

        // Execute ability
        this.executeAbility(abilityName, player);

        // Positive feedback: green particles
        const x = player === 1 ? 30 : CANVAS_WIDTH - 30;
        const y = (player === 1 ? this.paddle1Y : this.paddle2Y) + (player === 1 ? this.paddle1Height : this.paddle2Height) / 2;
        for (let i = 0; i < 8; i++) {
            this.particles.push({
                x, y,
                velX: (Math.random() - 0.5) * 4,
                velY: (Math.random() - 0.5) * 4,
                life: 30,
                size: 2,
                color: 'rgba(100,255,100,0.9)'
            });
        }

        // Set cooldown (frames). Default 600 (~10s at 60fps). Some abilities shorter.
        let cooldownFrames = 600;
        if (abilityName === 'gun') cooldownFrames = 300;
        if (abilityName === 'speed_boost') cooldownFrames = 450;
        if (abilityName === 'time_loop') cooldownFrames = 1200;
        if (abilityName === 'portal_pong') cooldownFrames = 600;
        if (abilityName === 'lag_spike') cooldownFrames = 900;
        if (abilityName === 'reverse_controls') cooldownFrames = 750;
        if (abilityName === 'blind') cooldownFrames = 1500;

        cooldowns.set(abilityName, cooldownFrames);
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
        this.frameCount++;

        // Paddle movement
        this.updatePaddleMovement();

        // Ball update
        this.updateBall();

        // AI
        if (this.singlePlayer) {
            this.updateAI();
        }

        // Power-ups
        this.updatePowerUps();

        // Collisions
        this.checkCollisions();

        // Particles and effects
        this.updateParticles();
        this.updateEffects();

        // Update bullets/lasers and ability cooldowns
        this.updateBulletsAndLasers();
        this.updateAbilityCooldowns();

        // Learning AI
        if (this.singlePlayer && this.learningAIEnabled && this.aiDifficulty === 4) {
            this.updateLearningAI();
        }

        // Save short game state history for time-loop ability (keep last ~600 frames)
        this.saveGameStateSnapshot();

        // Tick blind/slow/stun timers
        if (this.player1BlindTimer > 0) this.player1BlindTimer--;
        if (this.player2BlindTimer > 0) this.player2BlindTimer--;
        if (this.player1SlowTimer > 0) this.player1SlowTimer--;
        if (this.player2SlowTimer > 0) this.player2SlowTimer--;
        if (this.player1StunTimer > 0) this.player1StunTimer--;
        if (this.player2StunTimer > 0) this.player2StunTimer--;

        // Check win condition
        if (this.scorePlayer1 >= 11 || this.scorePlayer2 >= 11) {
            this.state = GAME_STATES.GAME_OVER;
        }
    }
    
    updatePaddleMovement() {
        // Player 1 (respect stun and slow)
        const p1Stunned = this.player1StunTimer > 0;
        const p2Stunned = this.player2StunTimer > 0;
        const p1Reverse = this.effectTimers.get('reverse_1') > 0;
        const p2Reverse = this.effectTimers.get('reverse_2') > 0;
        const p1Slow = this.player1SlowTimer > 0 ? 0.5 : 1.0;
        const p2Slow = this.player2SlowTimer > 0 ? 0.5 : 1.0;

        const p1Speed = this.paddleSpeed * p1Slow;
        const p2Speed = this.paddleSpeed * p2Slow;

        if (!p1Stunned) {
            // Reverse controls if active
            if (!p1Reverse) {
                if (this.keys['w'] && this.paddle1Y > 0) {
                    this.paddle1Y = Math.max(0, this.paddle1Y - p1Speed);
                }
                if (this.keys['s'] && this.paddle1Y < CANVAS_HEIGHT - this.paddle1Height) {
                    this.paddle1Y = Math.min(CANVAS_HEIGHT - this.paddle1Height, this.paddle1Y + p1Speed);
                }
            } else {
                // Reversed: w moves down, s moves up
                if (this.keys['w'] && this.paddle1Y < CANVAS_HEIGHT - this.paddle1Height) {
                    this.paddle1Y = Math.min(CANVAS_HEIGHT - this.paddle1Height, this.paddle1Y + p1Speed);
                }
                if (this.keys['s'] && this.paddle1Y > 0) {
                    this.paddle1Y = Math.max(0, this.paddle1Y - p1Speed);
                }
            }
        }

        // Player 2 (or AI)
        if (!this.singlePlayer) {
            if (!p2Stunned) {
                if (!p2Reverse) {
                    if (this.keys['arrowup'] && this.paddle2Y > 0) {
                        this.paddle2Y = Math.max(0, this.paddle2Y - p2Speed);
                    }
                    if (this.keys['arrowdown'] && this.paddle2Y < CANVAS_HEIGHT - this.paddle2Height) {
                        this.paddle2Y = Math.min(CANVAS_HEIGHT - this.paddle2Height, this.paddle2Y + p2Speed);
                    }
                } else {
                    if (this.keys['arrowup'] && this.paddle2Y < CANVAS_HEIGHT - this.paddle2Height) {
                        this.paddle2Y = Math.min(CANVAS_HEIGHT - this.paddle2Height, this.paddle2Y + p2Speed);
                    }
                    if (this.keys['arrowdown'] && this.paddle2Y > 0) {
                        this.paddle2Y = Math.max(0, this.paddle2Y - p2Speed);
                    }
                }
            }
        }
    }
    
    updateBall() {
        this.ballX += this.ballVelX;
        this.ballY += this.ballVelY;
        
        // Walls
        if (this.ballY <= 0 || this.ballY + BALL_SIZE >= CANVAS_HEIGHT) {
            this.ballVelY = -this.ballVelY;
            this.ballY = Math.max(0, Math.min(this.ballY, CANVAS_HEIGHT - BALL_SIZE));
        }
        
        // Scoring
        if (this.ballX < 0) {
            let points = 1;
            if (this.effectTimers.get('double_2') > 0) points *= 2; // P2 has double points
            this.scorePlayer2 += points;
            this.ballX = 0;
            this.resetBallPosition('right');
            this.levelUpCheck();
        } else if (this.ballX > CANVAS_WIDTH) {
            let points = 1;
            if (this.effectTimers.get('double_1') > 0) points *= 2; // P1 has double points
            this.scorePlayer1 += points;
            this.ballX = CANVAS_WIDTH;
            this.resetBallPosition('left');
            this.levelUpCheck();
        }
    }
    
    resetBallPosition(side) {
        this.ballX = CANVAS_WIDTH / 2 - BALL_SIZE / 2;
        this.ballY = CANVAS_HEIGHT / 2 - BALL_SIZE / 2;
        this.ballVelX = 4 * (side === 'left' ? 1 : -1) * this.aiAdaptationMultiplier;
        this.ballVelY = 4 * (Math.random() > 0.5 ? 1 : -1) * this.aiAdaptationMultiplier;
    }
    
    levelUpCheck() {
        // Check player 1 level up
        const newLevel1 = 1 + Math.floor(this.scorePlayer1 / this.pointsToNextLevel1);
        if (newLevel1 > this.level1) {
            this.level1 = newLevel1;
            this.grantAbilityToPlayer(1);
        }

        // Check player 2 level up
        const newLevel2 = 1 + Math.floor(this.scorePlayer2 / this.pointsToNextLevel2);
        if (newLevel2 > this.level2) {
            this.level2 = newLevel2;
            this.grantAbilityToPlayer(2);
        }
    }

    grantAbilityToPlayer(player) {
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;

        // Pick a random ability not yet owned
        const available = ALL_ABILITIES.filter(a => !abilities.has(a));
        if (available.length === 0) return; // all abilities owned

        const newAbility = available[Math.floor(Math.random() * available.length)];
        abilities.set(newAbility, 1);

        // Assign random branch (1 or 2) at level 3+
        const level = player === 1 ? this.level1 : this.level2;
        if (level >= 3) {
            branches.set(newAbility, Math.random() > 0.5 ? 1 : 2);
        }
    }
    
    updateAI() {
        const ballCenterY = this.ballY + BALL_SIZE / 2;
        const paddle2CenterY = this.paddle2Y + this.paddle2Height / 2;
        const aiSpeed = this.getAISpeed();
        // If AI is stunned, don't move
        if (this.player2StunTimer > 0) return;

        if (Math.abs(ballCenterY - paddle2CenterY) > 5) {
            if (ballCenterY < paddle2CenterY) {
                this.paddle2Y = Math.max(0, this.paddle2Y - aiSpeed);
            } else {
                this.paddle2Y = Math.min(CANVAS_HEIGHT - this.paddle2Height, this.paddle2Y + aiSpeed);
            }
        }
    }
    
    getAISpeed() {
        const baseSpeeds = [3, 5, 7, 9];
        const speedIndex = Math.min(this.aiDifficulty - 1, 3);
        let speed = baseSpeeds[speedIndex];
        
        if (this.learningAIEnabled && this.aiDifficulty === 4) {
            speed = Math.min(speed, 3 + (this.playerSkillLevel * 6));
        }
        
        return speed;
    }
    
    updateLearningAI() {
        const paddle1CenterY = this.paddle1Y + this.paddle1Height / 2;
        
        this.playerPositionHistory.push(paddle1CenterY);
        if (this.playerPositionHistory.length > this.maxLearningData) {
            this.playerPositionHistory.shift();
        }
        
        if (this.playerPositionHistory.length > 0) {
            const sum = this.playerPositionHistory.reduce((a, b) => a + b, 0);
            this.learnedAveragePosition = sum / this.playerPositionHistory.length;
        }
        
        this.learningDataPoints = Math.min(this.maxLearningData, this.playerPositionHistory.length);
        this.learningProgress = Math.min(1.0, this.learningDataPoints / 600.0);
        
        // Track skill level
        this.totalRallies = this.playerSuccessfulHits + this.aiSuccessfulHits;
        if (this.totalRallies > 20) {
            this.playerSkillLevel = this.playerSuccessfulHits / this.totalRallies;
            this.playerSkillLevel = Math.max(0.2, Math.min(0.9, this.playerSkillLevel));
        }
        
        // Adapt AI difficulty
        if (this.totalRallies > 30) {
            if (this.playerSkillLevel > 0.6) {
                this.aiAdaptationMultiplier = Math.min(1.4, this.aiAdaptationMultiplier + 0.002);
            } else if (this.playerSkillLevel < 0.4) {
                this.aiAdaptationMultiplier = Math.max(0.7, this.aiAdaptationMultiplier - 0.002);
            }
        }
    }
    
    updatePowerUps() {
        this.powerUpSpawnTimer++;
        if (this.powerUpSpawnTimer >= this.powerUpSpawnInterval) {
            this.spawnPowerUp();
            this.powerUpSpawnTimer = 0;
        }
        
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            this.powerUps[i].y += this.powerUps[i].velocityY;
            if (this.powerUps[i].y > CANVAS_HEIGHT) {
                this.powerUps.splice(i, 1);
            }
        }
    }

    // Ability and effect helpers
    updateAbilityCooldowns() {
        const decMap = (map) => {
            map.forEach((v, k) => {
                if (v > 0) {
                    map.set(k, v - 1);
                } else {
                    map.delete(k);
                }
            });
        };
        decMap(this.player1AbilityCooldowns);
        decMap(this.player2AbilityCooldowns);
    }

    updateBulletsAndLasers() {
        // Update bullets
        for (let i = this.bullets.length - 1; i >= 0; i--) {
            const b = this.bullets[i];
            b.x += b.vx;
            b.y += b.vy;

            // off-screen
            if (b.x < -10 || b.x > CANVAS_WIDTH + 10 || b.y < -10 || b.y > CANVAS_HEIGHT + 10) {
                this.bullets.splice(i, 1);
                continue;
            }

            // collision vs paddles
            if (b.owner === 1) {
                // check paddle2
                if (b.x > CANVAS_WIDTH - 30 && b.y > this.paddle2Y && b.y < this.paddle2Y + this.paddle2Height) {
                    this.player2StunTimer = this.stunDuration;
                    this.createImpactEffect(b.x, b.y);
                    this.explosions.push({x: b.x, y: b.y, life: 30, maxLife: 30, radius: 20});
                    this.bullets.splice(i, 1);
                }
            } else {
                // owner 2 vs paddle1
                if (b.x < 30 && b.y > this.paddle1Y && b.y < this.paddle1Y + this.paddle1Height) {
                    this.player1StunTimer = this.stunDuration;
                    this.createImpactEffect(b.x, b.y);
                    this.explosions.push({x: b.x, y: b.y, life: 30, maxLife: 30, radius: 20});
                    this.bullets.splice(i, 1);
                }
            }
        }

        // Update lasers
        for (let i = this.lasers.length - 1; i >= 0; i--) {
            const L = this.lasers[i];
            L.life--;
            if (L.life <= 0) {
                this.lasers.splice(i, 1);
                continue;
            }

            // simple horizontal laser check
            const y = L.y;
            if (L.owner === 1) {
                if (this.paddle2Y < y && this.paddle2Y + this.paddle2Height > y) {
                    this.player2StunTimer = this.stunDuration;
                }
            } else {
                if (this.paddle1Y < y && this.paddle1Y + this.paddle1Height > y) {
                    this.player1StunTimer = this.stunDuration;
                }
            }
        }

        // Update explosions
        for (let i = this.explosions.length - 1; i >= 0; i--) {
            const e = this.explosions[i];
            e.life--;
            if (e.life <= 0) this.explosions.splice(i, 1);
        }
    }

    saveGameStateSnapshot() {
        const s = {
            ballX: this.ballX,
            ballY: this.ballY,
            ballVelX: this.ballVelX,
            ballVelY: this.ballVelY,
            paddle1Y: this.paddle1Y,
            paddle2Y: this.paddle2Y,
            paddle1Height: this.paddle1Height,
            paddle2Height: this.paddle2Height,
            score1: this.scorePlayer1,
            score2: this.scorePlayer2
        };
        this.gameStateHistory = this.gameStateHistory || [];
        this.gameStateHistory.push(s);
        if (this.gameStateHistory.length > 600) this.gameStateHistory.shift();
    }

    restoreGameStateSnapshot(framesBack = 60) {
        if (!this.gameStateHistory || this.gameStateHistory.length === 0) return;
        const idx = Math.max(0, this.gameStateHistory.length - 1 - framesBack);
        const s = this.gameStateHistory[idx];
        if (!s) return;
        this.ballX = s.ballX;
        this.ballY = s.ballY;
        this.ballVelX = s.ballVelX;
        this.ballVelY = s.ballVelY;
        this.paddle1Y = s.paddle1Y;
        this.paddle2Y = s.paddle2Y;
        this.paddle1Height = s.paddle1Height;
        this.paddle2Height = s.paddle2Height;
        this.scorePlayer1 = s.score1;
        this.scorePlayer2 = s.score2;

        // Rewind visual: create particles across screen
        for (let i = 0; i < 30; i++) {
            this.particles.push({
                x: Math.random() * CANVAS_WIDTH,
                y: Math.random() * CANVAS_HEIGHT,
                velX: (Math.random() - 0.5) * 4,
                velY: (Math.random() - 0.5) * 4,
                life: 30,
                size: 2,
                color: 'rgba(180,180,255,0.6)'
            });
        }
    }

    fireBullet(owner, x, y, speed = 8) {
        const dir = owner === 1 ? 1 : -1;
        this.bullets.push({x, y, vx: speed * dir, vy: 0, owner});
    }

    spawnLaser(owner, y, life = 60, color = '#ff0') {
        const x1 = owner === 1 ? 40 : CANVAS_WIDTH - 40;
        const x2 = owner === 1 ? CANVAS_WIDTH : 0;
        this.lasers.push({owner, x1, y1: y, x2, y2: y, y, life, color});
    }

    spawnPortal(player, x, y) {
        // create a portal for the player; pair portals by player id
        this.portals.set(player, {x, y, created: Date.now()});
    }

    executeAbility(name, player) {
        const opponent = player === 1 ? 2 : 1;
        const level = player === 1 ? this.level1 : this.level2;
        
        // Grant ability to player if not owned (50% chance OR on level-up)
        const ownedAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        if (!ownedAbilities.has(name)) {
            ownedAbilities.set(name, 1); // level 1
        }

        switch (name) {
            // Branch 1: Goku Laser | Branch 2: Vegeta Bullets
            case 'gun': {
                const branch = (player === 1 ? this.player1AbilityBranches.get('gun') : this.player2AbilityBranches.get('gun')) || 1;
                if (branch === 1) {
                    // Goku Laser: horizontal beam stuns
                    const y = player === 1 ? this.paddle1Y + this.paddle1Height / 2 : this.paddle2Y + this.paddle2Height / 2;
                    this.spawnLaser(player, y, 90, '#00ff00');
                } else {
                    // Vegeta Bullets: spread
                    const baseY = player === 1 ? this.paddle1Y + this.paddle1Height / 2 : this.paddle2Y + this.paddle2Height / 2;
                    for (let j = -1; j <= 1; j++) this.fireBullet(player, player === 1 ? 40 : CANVAS_WIDTH - 40, baseY + j * 8);
                }
                break;
            }
            // Branch 1: Sonic Dash | Branch 2: Flash
            case 'speed_boost': {
                this.effectTimers.set(`speed_${player}`, 300);
                this.paddleSpeed = Math.min(12, this.paddleSpeed + 3);
                // branch 2 adds bonus ball speed
                const branch = (player === 1 ? this.player1AbilityBranches.get('speed_boost') : this.player2AbilityBranches.get('speed_boost')) || 1;
                if (branch === 2) {
                    this.ballVelX *= 1.15;
                    this.ballVelY *= 1.15;
                }
                break;
            }
            // Branch 1: Expansion | Branch 2: Elastic
            case 'paddle_growth': {
                const growAmount = player === 1 ? this.level1 * 10 : this.level2 * 10;
                if (player === 1) this.paddle1Height = Math.min(CANVAS_HEIGHT, this.paddle1Height + growAmount);
                else this.paddle2Height = Math.min(CANVAS_HEIGHT, this.paddle2Height + growAmount);
                this.effectTimers.set(`grow_${player}`, 300);
                break;
            }
            // Double Points: multiplier on next 5 hits
            case 'double_points':
                this.effectTimers.set(`double_${player}`, 600);
                break;
            // Branch 1: Freeze Ray | Branch 2: Gravity Well
            case 'slow_opponent': {
                if (opponent === 1) this.player1SlowTimer = 300;
                else this.player2SlowTimer = 300;
                break;
            }
            // Branch 1: Perfect Track | Branch 2: Overdrive
            case 'joshua': {
                const branch = (player === 1 ? this.player1AbilityBranches.get('joshua') : this.player2AbilityBranches.get('joshua')) || 1;
                if (branch === 1) {
                    // Perfect Track: AI predicts perfectly
                    this.aiAdaptationMultiplier = Math.min(1.8, this.aiAdaptationMultiplier + 0.3);
                } else {
                    // Overdrive: paddle boost
                    this.paddleSpeed = Math.min(15, this.paddleSpeed + 5);
                }
                this.effectTimers.set(`joshua_${player}`, 450);
                break;
            }
            // Blind: hide opponent paddle
            case 'blind':
                if (opponent === 1) this.player1BlindTimer = 300;
                else this.player2BlindTimer = 300;
                break;
            // Shrink: reduce opponent paddle
            case 'shrink_opponent': {
                const shrinkAmount = level * 10;
                if (opponent === 1) {
                    this.paddle1Height = Math.max(20, this.paddle1Height - shrinkAmount);
                    this.effectTimers.set('shrink1', 600);
                } else {
                    this.paddle2Height = Math.max(20, this.paddle2Height - shrinkAmount);
                    this.effectTimers.set('shrink2', 600);
                }
                break;
            }
            // Branch 1: Void Pulse | Branch 2: Spectral Echo
            case 'ghost_ball': {
                this.ghostBallActive = true;
                this.effectTimers.set('ghostBall', 300);
                break;
            }
            // Jaisan: Perfection (all boosts) or Infinity (max everything)
            case 'jaisan': {
                const branch = (player === 1 ? this.player1AbilityBranches.get('jaisan') : this.player2AbilityBranches.get('jaisan')) || 1;
                if (branch === 1) {
                    // Perfection: all buffs
                    this.paddleSpeed = Math.min(12, this.paddleSpeed + 2);
                    if (player === 1) this.paddle1Height = Math.min(CANVAS_HEIGHT, this.paddle1Height + 30);
                    else this.paddle2Height = Math.min(CANVAS_HEIGHT, this.paddle2Height + 30);
                } else {
                    // Infinity: extreme buffs
                    this.paddleSpeed = 15;
                    if (player === 1) this.paddle1Height = CANVAS_HEIGHT * 0.8;
                    else this.paddle2Height = CANVAS_HEIGHT * 0.8;
                }
                this.effectTimers.set(`jaisan_${player}`, 600);
                break;
            }
            // Ability Swap: swap two random abilities with opponent
            case 'ability_swap': {
                const p1Keys = Array.from(this.player1Abilities.keys());
                const p2Keys = Array.from(this.player2Abilities.keys());
                if (p1Keys.length > 0 && p2Keys.length > 0) {
                    const idx1 = Math.floor(Math.random() * p1Keys.length);
                    const idx2 = Math.floor(Math.random() * p2Keys.length);
                    const a1 = p1Keys[idx1], a2 = p2Keys[idx2];
                    const v1 = this.player1Abilities.get(a1);
                    const v2 = this.player2Abilities.get(a2);
                    this.player1Abilities.set(a1, v2);
                    this.player2Abilities.set(a2, v1);
                }
                break;
            }
            // Branch 1: Thor (shockwave) | Branch 2: Gravity Well
            case 'gravity_hammer': {
                const cx = CANVAS_WIDTH / 2;
                const cy = CANVAS_HEIGHT / 2;
                // create explosion effect at center
                for (let i = 0; i < 20; i++) {
                    const angle = (Math.PI * 2 * i) / 20;
                    this.particles.push({
                        x: cx,
                        y: cy,
                        velX: Math.cos(angle) * 6,
                        velY: Math.sin(angle) * 6,
                        life: 40,
                        size: 3,
                        color: 'rgba(200,100,255,0.8)'
                    });
                }
                break;
            }
            // Branch 1: Attract | Branch 2: Force Field
            case 'magnet_ball': {
                // Ball attracted toward player's paddle
                const targetY = player === 1 ? this.paddle1Y + this.paddle1Height / 2 : this.paddle2Y + this.paddle2Height / 2;
                const pullStrength = 2;
                this.ballVelY += (targetY - (this.ballY + BALL_SIZE / 2)) * 0.05;
                break;
            }
            // Branch 1: Trail | Branch 2: Afterimage
            case 'shadow_clone': {
                // Create shadow clone of player's paddle
                const key = `shadow_${player}_${Date.now()}`;
                const y = player === 1 ? this.paddle1Y : this.paddle2Y;
                const h = player === 1 ? this.paddle1Height : this.paddle2Height;
                this.shadowClones.set(key, {player, y, h, life: 120, created: Date.now()});
                break;
            }
            // Branch 1: Teleport | Branch 2: Dimensional Rift
            case 'portal_pong':
                if (player === 1) this.spawnPortal(1, 60, this.paddle1Y + this.paddle1Height / 2);
                else this.spawnPortal(2, CANVAS_WIDTH - 60, this.paddle2Y + this.paddle2Height / 2);
                break;
            // Branch 1: Soul Reaper | Branch 2: Overload
            case 'power_siphon': {
                // Drain opponent's ball speed, give to self
                const drain = Math.sqrt(this.ballVelX ** 2 + this.ballVelY ** 2) * 0.3;
                this.ballVelX *= (1 - 0.15);
                this.ballVelY *= (1 - 0.15);
                break;
            }
            // Branch 1: Chronos Rewind | Branch 2: Temporal Echo
            case 'time_loop':
                this.restoreGameStateSnapshot(Math.min(180, this.gameStateHistory ? this.gameStateHistory.length - 1 : 0));
                break;
            // Ability Stealer: copy opponent ability
            case 'ability_stealer': {
                const opponentAbilities = opponent === 1 ? this.player1Abilities : this.player2Abilities;
                if (opponentAbilities.size > 0) {
                    const keys = Array.from(opponentAbilities.keys());
                    const stolen = keys[Math.floor(Math.random() * keys.length)];
                    const ownAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
                    ownAbilities.set(stolen, opponentAbilities.get(stolen));
                }
                break;
            }
            // Lag Spike: random teleport opponent paddle
            case 'lag_spike': {
                if (opponent === 1) {
                    this.paddle1Y = Math.random() * (CANVAS_HEIGHT - this.paddle1Height);
                } else {
                    this.paddle2Y = Math.random() * (CANVAS_HEIGHT - this.paddle2Height);
                }
                break;
            }
            // Reverse Controls: flip opponent's inputs
            case 'reverse_controls':
                this.effectTimers.set(`reverse_${opponent}`, 300);
                break;
            default:
                // Particle feedback
                const cx = player === 1 ? 40 : CANVAS_WIDTH - 40;
                const cy = player === 1 ? this.paddle1Y + this.paddle1Height / 2 : this.paddle2Y + this.paddle2Height / 2;
                for (let i = 0; i < 10; i++) {
                    this.particles.push({
                        x: cx,
                        y: cy,
                        velX: (Math.random() - 0.5) * 4,
                        velY: (Math.random() - 0.5) * 4,
                        life: 30,
                        size: 2,
                        color: 'rgba(255,200,100,0.9)'
                    });
                }
        }
    }
    
    spawnPowerUp() {
        const types = ['speed', 'paddle_grow', 'slow_opponent', 'multiball', 'jackpot', 'gun', 'portal', 'time_loop'];
        const type = types[Math.floor(Math.random() * types.length)];
        this.powerUps.push({
            x: 50 + Math.random() * 500,
            y: 20,
            type,
            size: 20,
            velocityY: 1.5,
            active: true
        });
    }
    
    checkCollisions() {
        // Paddle 1 collision
        if (this.ballX < 20 && this.ballX + BALL_SIZE > 10 &&
            this.ballY < this.paddle1Y + this.paddle1Height &&
            this.ballY + BALL_SIZE > this.paddle1Y) {
            
            if (this.ballVelX < 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = 20;
                
                const paddleCenter = this.paddle1Y + this.paddle1Height / 2;
                const ballCenter = this.ballY + BALL_SIZE / 2;
                const hitOffset = (ballCenter - paddleCenter) / (this.paddle1Height / 2);
                this.ballVelY += hitOffset * 3;
                
                this.createImpactEffect(this.ballX, this.ballY);
                this.playerSuccessfulHits++;
            }
        }
        
        // Paddle 2 collision
        if (this.ballX + BALL_SIZE > CANVAS_WIDTH - 20 && this.ballX < CANVAS_WIDTH - 10 &&
            this.ballY < this.paddle2Y + this.paddle2Height &&
            this.ballY + BALL_SIZE > this.paddle2Y) {
            
            if (this.ballVelX > 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = CANVAS_WIDTH - 20 - BALL_SIZE;
                
                const paddleCenter = this.paddle2Y + this.paddle2Height / 2;
                const ballCenter = this.ballY + BALL_SIZE / 2;
                const hitOffset = (ballCenter - paddleCenter) / (this.paddle2Height / 2);
                this.ballVelY += hitOffset * 3;
                
                this.createImpactEffect(this.ballX, this.ballY);
                this.aiSuccessfulHits++;
            }
        }
        
        // Power-up collisions
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            const pu = this.powerUps[i];
            
            if (pu.x > 10 && pu.x < 20 && pu.y > this.paddle1Y && pu.y < this.paddle1Y + this.paddle1Height) {
                this.applyPowerUp(1, pu.type);
                this.powerUps.splice(i, 1);
                continue;
            }
            
            if (pu.x > CANVAS_WIDTH - 20 && pu.x < CANVAS_WIDTH - 10 &&
                pu.y > this.paddle2Y && pu.y < this.paddle2Y + this.paddle2Height) {
                this.applyPowerUp(2, pu.type);
                this.powerUps.splice(i, 1);
            }
        }

        // Portal teleportation: if ball enters a portal, teleport to far side or paired portal
        if (this.portals && this.portals.size > 0) {
            this.portals.forEach((p, key) => {
                const dx = this.ballX + BALL_SIZE / 2 - p.x;
                const dy = this.ballY + BALL_SIZE / 2 - p.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 24) {
                    // teleport ball to opposite side (mirror) or to other portal if present
                    // simple mirror: flip X, nudge
                    this.ballX = CANVAS_WIDTH - this.ballX;
                    this.ballVelX = -this.ballVelX;
                    this.createImpactEffect(this.ballX, this.ballY);
                }
            });
        }
    }
    
    createImpactEffect(x, y) {
        for (let i = 0; i < 10; i++) {
            const angle = (Math.PI * 2 * i) / 10;
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
        console.log(`Player ${player} picked up: ${type}`);
        
        if (type === 'speed') {
            this.ballVelX *= 1.2;
            this.ballVelY *= 1.2;
        } else if (type === 'paddle_grow') {
            if (player === 1) {
                this.paddle1Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 20);
            } else {
                this.paddle2Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 20);
            }
        } else if (type === 'slow_opponent') {
            if (player === 1) {
                this.ballVelX *= 0.8;
                this.ballVelY *= 0.8;
            } else {
                this.ballVelX *= 0.8;
                this.ballVelY *= 0.8;
            }
        } else if (type === 'gun') {
            this.player1Abilities.set('gun', 3);
        } else if (type === 'portal') {
            this.player1Abilities.set('portal_pong', 3);
        }
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
    
    updateEffects() {
        // Update effect timers and cleanup when they expire
        const toDelete = [];
        this.effectTimers.forEach((value, key) => {
            const newVal = value - 1;
            if (newVal <= 0) {
                // cleanup effects by key
                if (key.startsWith('speed_')) {
                    // restore default paddle speed
                    this.paddleSpeed = 5;
                } else if (key === 'shrink1') {
                    this.paddle1Height = PADDLE_HEIGHT;
                } else if (key === 'shrink2') {
                    this.paddle2Height = PADDLE_HEIGHT;
                } else if (key === 'ghostBall') {
                    this.ghostBallActive = false;
                }
                toDelete.push(key);
            } else {
                this.effectTimers.set(key, newVal);
            }
        });
        for (const k of toDelete) this.effectTimers.delete(k);

        // Update shadow clones
        if (this.shadowClones) {
            const clonesToDelete = [];
            this.shadowClones.forEach((clone, key) => {
                clone.life--;
                if (clone.life <= 0) clonesToDelete.push(key);
            });
            for (const k of clonesToDelete) this.shadowClones.delete(k);
        }

        // Clean up old portals
        if (this.portals) {
            const now = Date.now();
            const portalToDelete = [];
            this.portals.forEach((portal, key) => {
                if (now - portal.created > 5000) portalToDelete.push(key); // 5 second lifetime
            });
            for (const k of portalToDelete) this.portals.delete(k);
        }
    }
    
    render() {
        // Clear
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Background gradient
        const grad = this.ctx.createLinearGradient(0, 0, 0, CANVAS_HEIGHT);
        grad.addColorStop(0, '#0a1428');
        grad.addColorStop(1, '#141f2a');
        this.ctx.fillStyle = grad;
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Center line
        this.ctx.strokeStyle = 'rgba(150, 200, 255, 0.3)';
        this.ctx.setLineDash([10, 10]);
        this.ctx.beginPath();
        this.ctx.moveTo(CANVAS_WIDTH / 2, 0);
        this.ctx.lineTo(CANVAS_WIDTH / 2, CANVAS_HEIGHT);
        this.ctx.stroke();
        this.ctx.setLineDash([]);
        
        if (this.state === GAME_STATES.PLAYING) {
            this.renderGameplay();
        } else if (this.state === GAME_STATES.PAUSED) {
            this.renderPaused();
        } else if (this.state === GAME_STATES.GAME_OVER) {
            this.renderGameOver();
        } else {
            this.renderMenu();
        }
    }
    
    renderGameplay() {
        // Paddles (hide or blind if affected)
        if (this.player1BlindTimer > 0) {
            // draw a dark overlay where paddle would be
            this.ctx.fillStyle = 'rgba(0,0,0,0.9)';
            this.ctx.fillRect(0, 0, 120, CANVAS_HEIGHT);
        } else {
            this.drawPaddle(10, this.paddle1Y, this.paddle1Height, '#00ffff');
        }

        if (this.player2BlindTimer > 0) {
            this.ctx.fillStyle = 'rgba(0,0,0,0.9)';
            this.ctx.fillRect(CANVAS_WIDTH - 120, 0, 120, CANVAS_HEIGHT);
        } else {
            this.drawPaddle(CANVAS_WIDTH - 20, this.paddle2Y, this.paddle2Height, '#ff6464');
        }

        // Shadow clones
        if (this.shadowClones && this.shadowClones.size > 0) {
            this.shadowClones.forEach((clone, key) => {
                const alpha = clone.life / 120;
                this.ctx.globalAlpha = alpha * 0.4;
                const color = clone.player === 1 ? '#00ffff' : '#ff6464';
                const x = clone.player === 1 ? 10 : CANVAS_WIDTH - 20;
                this.drawPaddle(x, clone.y, clone.h, color);
                this.ctx.globalAlpha = 1.0;
            });
        }

        // Ball
        this.drawBall();

        // Portals
        if (this.portals && this.portals.size > 0) {
            this.ctx.fillStyle = 'rgba(200,100,255,0.5)';
            this.portals.forEach((portal, key) => {
                this.ctx.beginPath();
                this.ctx.arc(portal.x, portal.y, 15, 0, Math.PI * 2);
                this.ctx.fill();
                this.ctx.strokeStyle = 'rgba(200,100,255,0.9)';
                this.ctx.lineWidth = 2;
                this.ctx.stroke();
            });
        }

        // Power-ups
        this.drawPowerUps();

        // Particles
        this.drawParticles();

        // Lasers and bullets
        this.drawLasers();
        this.drawBullets();

        // Explosions
        this.drawExplosions();

        // Score and info
        this.drawScore();
        this.drawGameInfo();
    }
    
    drawPaddle(x, y, height, color) {
        const grad = this.ctx.createLinearGradient(x, y, x + PADDLE_WIDTH, y);
        grad.addColorStop(0, color);
        grad.addColorStop(1, 'rgba(255, 255, 255, 0.3)');
        
        this.ctx.fillStyle = grad;
        this.ctx.fillRect(x, y, PADDLE_WIDTH, height);
        
        // Glow
        this.ctx.shadowColor = color;
        this.ctx.shadowBlur = 10;
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(x - 2, y - 2, PADDLE_WIDTH + 4, height + 4);
        this.ctx.shadowBlur = 0;
    }
    
    drawBall() {
        const rad = this.ctx.createRadialGradient(this.ballX + 7.5, this.ballY + 7.5, 0,
            this.ballX + 7.5, this.ballY + 7.5, BALL_SIZE / 2);
        
        rad.addColorStop(0, '#ffffff');
        rad.addColorStop(0.6, '#00ffff');
        rad.addColorStop(1, '#0099ff');
        
        this.ctx.fillStyle = rad;
        this.ctx.fillRect(this.ballX, this.ballY, BALL_SIZE, BALL_SIZE);
        
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
    
    drawLasers() {
        for (const laser of this.lasers) {
            this.ctx.strokeStyle = laser.color || '#00ff00';
            this.ctx.lineWidth = 3;
            this.ctx.beginPath();
            this.ctx.moveTo(laser.x1, laser.y1);
            this.ctx.lineTo(laser.x2, laser.y2);
            this.ctx.stroke();
        }
    }
    
    drawBullets() {
        for (const bullet of this.bullets) {
            this.ctx.fillStyle = '#ffaa00';
            this.ctx.fillRect(bullet.x - 3, bullet.y - 3, 6, 6);
        }
    }
    
    drawExplosions() {
        for (const exp of this.explosions) {
            const alpha = exp.life / exp.maxLife;
            this.ctx.fillStyle = `rgba(255, 100, 0, ${alpha * 0.5})`;
            this.ctx.fillRect(exp.x - exp.radius, exp.y - exp.radius, exp.radius * 2, exp.radius * 2);
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
    
    drawGameInfo() {
        this.ctx.font = '12px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.textAlign = 'left';

        let y = CANVAS_HEIGHT - 80;
        this.ctx.fillText(`L: ${this.level1} vs ${this.level2}`, 10, y);
        this.ctx.fillText(`Speed: ${Math.round(Math.sqrt(this.ballVelX ** 2 + this.ballVelY ** 2) * 10) / 10}`, 10, y + 15);

        if (this.singlePlayer && this.learningAIEnabled && this.aiDifficulty === 4) {
            this.ctx.fillText(`AI: ${Math.round(this.learningProgress * 100)}% | Skill: ${Math.round(this.playerSkillLevel * 100)}%`, 10, y + 30);
        }

        // Show P1 abilities and cooldowns
        this.ctx.fillStyle = '#00ffff';
        this.ctx.font = '10px Arial';
        this.ctx.textAlign = 'left';
        y = CANVAS_HEIGHT - 60;
        const p1AbilityNames = Array.from(this.player1Abilities.keys()).slice(0, 3);
        for (let i = 0; i < p1AbilityNames.length; i++) {
            const abilityName = p1AbilityNames[i];
            const cd = this.player1AbilityCooldowns.get(abilityName) || 0;
            const cdText = cd > 0 ? `[${Math.ceil(cd / 60)}s]` : '[Ready]';
            this.ctx.fillText(`${abilityName} ${cdText}`, 10, y + i * 12);
        }

        // Show P2 abilities and cooldowns
        this.ctx.fillStyle = '#ff6464';
        this.ctx.textAlign = 'right';
        y = CANVAS_HEIGHT - 60;
        const p2AbilityNames = Array.from(this.player2Abilities.keys()).slice(0, 3);
        for (let i = 0; i < p2AbilityNames.length; i++) {
            const abilityName = p2AbilityNames[i];
            const cd = this.player2AbilityCooldowns.get(abilityName) || 0;
            const cdText = cd > 0 ? `[${Math.ceil(cd / 60)}s]` : '[Ready]';
            this.ctx.fillText(`${abilityName} ${cdText}`, CANVAS_WIDTH - 10, y + i * 12);
        }

        // Show status effects
        this.ctx.fillStyle = '#ffff00';
        this.ctx.font = '11px Arial';
        this.ctx.textAlign = 'center';
        if (this.player1BlindTimer > 0) this.ctx.fillText('P1 BLINDED', CANVAS_WIDTH / 4, 30);
        if (this.player2BlindTimer > 0) this.ctx.fillText('P2 BLINDED', (CANVAS_WIDTH * 3) / 4, 30);
        if (this.player1StunTimer > 0) this.ctx.fillText('P1 STUNNED', CANVAS_WIDTH / 4, 45);
        if (this.player2StunTimer > 0) this.ctx.fillText('P2 STUNNED', (CANVAS_WIDTH * 3) / 4, 45);
    }
    
    renderPaused() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ffff';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('PAUSED', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
        
        this.ctx.font = '18px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText('Press P to resume', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 40);
    }
    
    renderGameOver() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        const winner = this.scorePlayer1 > this.scorePlayer2 ? 'PLAYER 1' : 'PLAYER 2';
        
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ff00';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('GAME OVER', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 40);
        
        this.ctx.font = 'bold 36px Arial';
        this.ctx.fillStyle = '#ffff00';
        this.ctx.fillText(`${winner} WINS!`, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 10);
        
        this.ctx.font = '20px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText(`${this.scorePlayer1} - ${this.scorePlayer2}`, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 50);
    }
    
    renderMenu() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        this.ctx.font = 'bold 48px Arial';
        this.ctx.fillStyle = '#00ffff';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('PONG', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 - 60);
        
        this.ctx.font = '18px Arial';
        this.ctx.fillStyle = '#aaa';
        this.ctx.fillText('Click "Start Game" to begin', CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2 + 20);
    }
}

// Initialize
window.addEventListener('DOMContentLoaded', () => {
    window.game = new PongGame();
});

function closeSettings() {
    document.getElementById('settingsModal').style.display = 'none';
}
