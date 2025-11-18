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
        this.container = document.querySelector('.container');
        
        // Game state
        this.state = GAME_STATES.MENU;
        this.isPaused = false;
        this.isFullscreen = false;
        this.singlePlayer = true;
        this.aiDifficulty = 2;
        this.learningAIEnabled = true;
        
        // Paddles
        this.paddle1Y = 170;
        this.paddle2Y = 170;
        this.paddleSpeed = 7;  // Increased from 5 to match Java version
        
        // Ball
        this.ballX = 292.5;
        this.ballY = 192.5;
        this.ballVelX = 4;
        this.ballVelY = 4;
        this.initialBallVelX = 4;  // Store initial velocity for power-up restoration
        this.initialBallVelY = 4;
        
        // Score and levels
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        this.pointsToNextLevel1 = 5;
        this.pointsToNextLevel2 = 5;
        this.totalPointsThisLevel1 = 0;
        this.totalPointsThisLevel2 = 0;
        
        // Ability branches
        this.player1AbilityBranches = new Map();
        this.player2AbilityBranches = new Map();
        
        // Input tracking
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
        this.allAbilities = ['speed_boost', 'paddle_growth', 'double_points', 'slow_opponent', 'gun', 'ability_stealer', 'lag_spike', 'reverse_controls', 'joshua', 'blind', 'shrink_opponent', 'ghost_ball', 'jaisan', 'ability_swap', 'gravity_hammer', 'magnet_ball', 'shadow_clone', 'portal_pong', 'power_siphon', 'time_loop'];
        this.player1AbilityBranches = this.player1AbilityBranches || new Map();
        this.player2AbilityBranches = this.player2AbilityBranches || new Map();
        this.abilityCooldowns = new Map();
        
        // Auto-activating ability timers
        this.gunTimer1 = 0;
        this.gunTimer2 = 0;
        this.gunCooldown = 300; // 5 seconds at 60 FPS
        
        this.blindTimer1 = 0;
        this.blindTimer2 = 0;
        this.blindDuration = 300;
        this.player1BlindDuration = 0;
        this.player2BlindDuration = 0;
        
        this.slowTimer1 = 0;
        this.slowTimer2 = 0;
        this.slowDuration = 500;
        this.player1SlowDuration = 0;
        this.player2SlowDuration = 0;
        
        this.reverseTimer1 = 0;
        this.reverseTimer2 = 0;
        this.reverseDuration = 150;
        this.player1ReverseControls = false;
        this.player2ReverseControls = false;
        
        this.lagSpikeTimer1 = 0;
        this.lagSpikeTimer2 = 0;
        this.lagSpikeCooldown = 400;
        
        this.stealerTimer1 = 0;
        this.stealerTimer2 = 0;
        this.stealerCooldown = 300;
        
        // JOSHUA ability - auto-deflect with 2x speed
        this.joshua1Active = false;
        this.joshua2Active = false;
        this.joshua1Duration = 0;
        this.joshua2Duration = 0;
        
        // JAISAN ability - auto-deflect with 3x speed (beats JOSHUA)
        this.jaisan1Active = false;
        this.jaisan2Active = false;
        this.jaisan1Duration = 0;
        this.jaisan2Duration = 0;
        
        // Shrink opponent durations
        this.paddle1ShrinkDuration = 0;
        this.paddle2ShrinkDuration = 0;
        
        // Magnet Ball - continuous passive attraction/repulsion
        this.player1MagnetActive = false;
        this.player2MagnetActive = false;
        this.player1MagnetRepelMode = false;
        this.player2MagnetRepelMode = false;
        
        // Portal Pong - two portals per player
        this.player1PortalEntranceX = null;
        this.player1PortalEntranceY = null;
        this.player1PortalExitX = null;
        this.player1PortalExitY = null;
        this.player1PortalPlacementStage = 0; // 0=ready, 1=entrance placed, 2=both placed
        this.player1PortalDuration = 0;
        this.player1PortalTimer = 0;
        
        this.player2PortalEntranceX = null;
        this.player2PortalEntranceY = null;
        this.player2PortalExitX = null;
        this.player2PortalExitY = null;
        this.player2PortalPlacementStage = 0;
        this.player2PortalDuration = 0;
        this.player2PortalTimer = 0;
        
        this.ballTeleportCooldown = false;
        
        // Manual ability cooldowns
        this.hammerTimer1 = 0;
        this.hammerTimer2 = 0;
        this.hammerCooldown = 240; // 4 seconds
        
        this.portalTimer1 = 0;
        this.portalTimer2 = 0;
        this.portalCooldown = 300; // 5 seconds between portal placements
        
        this.siphonTimer1 = 0;
        this.siphonTimer2 = 0;
        this.siphonCooldown = 300; // 5 seconds
        
        // Power-ups
        this.powerUps = [];
        this.powerUpSpawnTimer = 0;
        this.powerUpSpawnInterval = 500;
        
        // Map powerup effects (matching Java version)
        this.fireballActive = false;
        this.fireballDuration = 0;
        this.fireballMaxDuration = 500;
        
        this.splitActive = false;
        this.splitDuration = 0;
        this.splitMaxDuration = 300;
        
        this.teleportActive = false;
        this.teleportDuration = 0;
        this.teleportMaxDuration = 300;
        
        this.mirrorActive = false;
        this.mirrorDuration = 0;
        this.mirrorMaxDuration = 1000;
        
        this.dangerZoneActive = false;
        this.dangerZoneDuration = 0;
        this.dangerZoneMaxDuration = 1000;
        
        this.invisibleWallsActive = false;
        this.invisibleWallsDuration = 0;
        this.invisibleWallMaxDuration = 800;
        this.invisibleWallY = 200;
        
        this.shrinkPaddlesActive = false;
        this.shrinkPaddlesDuration = 0;
        this.shrinkPaddlesMaxDuration = 800;
        
        this.centerWallActive = false;
        this.centerWallDuration = 0;
        this.centerWallMaxDuration = 800;
        this.centerWallGapY = 200;
        this.centerWallGapSize = 80;
        
        this.zigzagActive = false;
        this.zigzagDuration = 0;
        this.zigzagMaxDuration = 400;
        this.zigzagTimer = 0;
        this.zigzagInterval = 30; // Change direction every 0.3 seconds
        
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
            if (e.key === 'm' || e.key === 'M') {
                this.handleCheatMenu();
            }
            if (e.key === 'f' || e.key === 'F') {
                this.toggleFullscreen();
            }
            // Ability activation
            if (e.key === 'q' || e.key === 'Q') this.activateAbility(1);
            // Player 2 ability: semicolon (';') or shifted ':'
            if (e.key === ';' || e.key === ':') this.activateAbility(2);
        });
        
        document.addEventListener('keyup', (e) => {
            const key = e.key.toLowerCase();
            this.keys[key] = false;
        });
        
        // UI button listeners (guarded)
        const startBtn = document.getElementById('startBtn');
        if (startBtn) startBtn.addEventListener('click', () => this.startGame());

        const pauseBtn = document.getElementById('pauseBtn');
        if (pauseBtn) pauseBtn.addEventListener('click', () => this.togglePause());

        const settingsBtn = document.getElementById('settingsBtn');
        if (settingsBtn) settingsBtn.addEventListener('click', () => this.showSettings());

        const fullscreenBtn = document.getElementById('fullscreenBtn');
        if (fullscreenBtn) fullscreenBtn.addEventListener('click', () => this.toggleFullscreen());

        // Modal Start button (in-modal) — ensures Start works while modal is visible
        const modalStartBtn = document.getElementById('modalStartBtn');
        if (modalStartBtn) modalStartBtn.addEventListener('click', () => this.startGame());
        
        // Settings modal
        const modal = document.getElementById('settingsModal');
        const closeBtn = document.querySelector('.close');
        if (closeBtn) closeBtn.addEventListener('click', () => this.closeSettings());
        window.addEventListener('click', (e) => {
            if (e.target === modal) this.closeSettings();
        });
    }
    
    startGame() {
        const gameMode = document.querySelector('input[name="gameMode"]:checked');
        const aiDifficultyEl = document.getElementById('aiDifficulty');
        const enableLearningAIEl = document.getElementById('enableLearningAI');
        
        if (gameMode && aiDifficultyEl) {
            this.singlePlayer = gameMode.value === 'single';
            this.aiDifficulty = parseInt(aiDifficultyEl.value);
            this.learningAIEnabled = enableLearningAIEl ? enableLearningAIEl.checked : true;
        }
        
        this.resetGame();
        this.state = GAME_STATES.PLAYING;
        this.closeSettings();
    }
    
    closeSettings() {
        const modal = document.getElementById('settingsModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }
    
    resetGame() {
        this.ballX = CANVAS_WIDTH / 2 - BALL_SIZE / 2;
        this.ballY = CANVAS_HEIGHT / 2 - BALL_SIZE / 2;
        this.ballVelX = 4 * (Math.random() > 0.5 ? 1 : -1);
        this.ballVelY = 4 * (Math.random() > 0.5 ? 1 : -1);
        // Store initial velocity for power-up restoration
        this.initialBallVelX = this.ballVelX;
        this.initialBallVelY = this.ballVelY;
        
        this.paddle1Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        this.paddle2Y = CANVAS_HEIGHT / 2 - PADDLE_HEIGHT / 2;
        
        // Apply paddle_growth passive ability on reset
        const p1PaddleGrowthLevel = this.player1Abilities ? (this.player1Abilities.get('paddle_growth') || 0) : 0;
        const p2PaddleGrowthLevel = this.player2Abilities ? (this.player2Abilities.get('paddle_growth') || 0) : 0;
        this.paddle1Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * p1PaddleGrowthLevel);
        this.paddle2Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * p2PaddleGrowthLevel);
        
        // Reset shrink durations so next shrink starts fresh
        this.paddle1ShrinkDuration = 0;
        this.paddle2ShrinkDuration = 0;
        
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.level1 = 1;
        this.level2 = 1;
        this.pointsToNextLevel1 = 5;
        this.pointsToNextLevel2 = 5;
        this.totalPointsThisLevel1 = 0;
        this.totalPointsThisLevel2 = 0;
        
        if (this.player1Abilities) this.player1Abilities.clear();
        if (this.player2Abilities) this.player2Abilities.clear();
        if (this.player1AbilityBranches) this.player1AbilityBranches.clear();
        if (this.player2AbilityBranches) this.player2AbilityBranches.clear();
        if (this.abilityCooldowns) this.abilityCooldowns.clear();
        
        this.powerUps = [];
        this.bullets = [];
        this.particles = [];
        this.lasers = [];
        this.explosions = [];
        this.shadowClones = new Map();
        this.portals = new Map();
        this.gameStateHistory = [];
        
        this.playerPositionHistory = [];
        this.playerSuccessfulHits = 0;
        this.aiSuccessfulHits = 0;
        
        // Reset ability timers
        this.gunTimer1 = 0;
        this.gunTimer2 = 0;
        this.blindTimer1 = 0;
        this.blindTimer2 = 0;
        this.slowTimer1 = 0;
        this.slowTimer2 = 0;
        this.reverseTimer1 = 0;
        this.reverseTimer2 = 0;
        this.lagSpikeTimer1 = 0;
        this.lagSpikeTimer2 = 0;
        this.stealerTimer1 = 0;
        this.stealerTimer2 = 0;
        
        // Reset effect durations
        this.player1BlindDuration = 0;
        this.player2BlindDuration = 0;
        this.player1SlowDuration = 0;
        this.player2SlowDuration = 0;
        this.player1ReverseControls = false;
        this.player2ReverseControls = false;
        this.doublePointsActive1 = false;
        this.doublePointsActive2 = false;
        
        // Reset map powerup states
        this.fireballActive = false;
        this.fireballDuration = 0;
        this.splitActive = false;
        this.splitDuration = 0;
        this.teleportActive = false;
        this.teleportDuration = 0;
        this.mirrorActive = false;
        this.mirrorDuration = 0;
        this.dangerZoneActive = false;
        this.dangerZoneDuration = 0;
        this.invisibleWallsActive = false;
        this.invisibleWallsDuration = 0;
        this.shrinkPaddlesActive = false;
        this.shrinkPaddlesDuration = 0;
        this.centerWallActive = false;
        this.centerWallDuration = 0;
        this.zigzagActive = false;
        this.zigzagDuration = 0;
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
            // Entering fullscreen
            this.isFullscreen = true;
            this.enterSimulatedFullscreen();
        } else {
            // Exiting fullscreen
            this.isFullscreen = false;
            document.exitFullscreen().catch(err => console.log('Exit fullscreen error:', err));
            setTimeout(() => {
                this.exitSimulatedFullscreen();
            }, 50);
        }
    }

    enterSimulatedFullscreen() {
        // Hide UI elements
        document.body.style.margin = '0';
        document.body.style.padding = '0';
        document.body.style.overflow = 'hidden';
        document.body.style.backgroundColor = '#000000';

        this.container.style.display = 'none';
        const controls = document.querySelector('.controls');
        const info = document.querySelector('.info');
        if (controls) controls.style.display = 'none';
        if (info) info.style.display = 'none';

        // Save original canvas styles
        this.originalCanvasWidth = this.canvas.width;
        this.originalCanvasHeight = this.canvas.height;
        this.originalCanvasStyle = this.canvas.getAttribute('style');

        // Set canvas to fill entire window
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;

        // Clear and recreate context
        this.ctx = this.canvas.getContext('2d');

        // Apply fullscreen styles
        this.canvas.style.cssText = `
            display: block !important;
            position: fixed !important;
            top: 0 !important;
            left: 0 !important;
            width: 100vw !important;
            height: 100vh !important;
            margin: 0 !important;
            padding: 0 !important;
            border: none !important;
            background-color: #000000 !important;
            z-index: 99999 !important;
        `;

        console.log(`Fullscreen mode entered: ${this.canvas.width}x${this.canvas.height}`);

        // Try to request fullscreen from document element
        const elem = document.documentElement;
        elem.requestFullscreen().catch(err => {
            console.log('Browser fullscreen API not available:', err);
        });
    }

    exitSimulatedFullscreen() {
        // Restore body styles
        document.body.style.margin = '';
        document.body.style.padding = '';
        document.body.style.overflow = '';
        document.body.style.backgroundColor = '';

        // Show UI elements
        this.container.style.display = 'block';
        const controls = document.querySelector('.controls');
        const info = document.querySelector('.info');
        if (controls) controls.style.display = 'flex';
        if (info) info.style.display = 'block';

        // Restore canvas to original size
        this.canvas.width = CANVAS_WIDTH;
        this.canvas.height = CANVAS_HEIGHT;

        // Clear and recreate context
        this.ctx = this.canvas.getContext('2d');

        // Restore original canvas styles
        if (this.originalCanvasStyle) {
            this.canvas.setAttribute('style', this.originalCanvasStyle);
        } else {
            this.canvas.style.cssText = `
                display: block;
                margin: 20px auto;
                border: 4px solid #00ffff;
                border-radius: 8px;
                background: linear-gradient(180deg, #0a1428 0%, #141f2a 100%);
                box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
            `;
        }

        console.log('Fullscreen mode exited');
    }

    showSettings() {
        document.getElementById('settingsModal').style.display = 'block';
    }
    
    handleCheatMenu() {
        const password = prompt('Enter cheat password:', '');
        if (password !== 'zanyscarf16') {
            if (password !== null) alert('Wrong password!');
            return;
        }

        // Pause the game when opening cheat menu
        const wasPaused = this.isPaused;
        this.isPaused = true;

        this.showCheatMenu(wasPaused);
    }

    showCheatMenu(wasPaused) {
        const cheatMenuModal = document.getElementById('cheatMenuModal');
        const cheatButtonContainer = document.getElementById('cheatButtonContainer');
        cheatButtonContainer.innerHTML = '';

        const options = [
            { label: 'Spawn Power-Up', action: () => this.showPowerUpSelection() },
            { label: 'Give Player Ability', action: () => this.showPlayerSelection('give') },
            { label: 'Delete Player Ability', action: () => this.showPlayerSelection('delete') }
        ];

        options.forEach(option => {
            const btn = document.createElement('button');
            btn.className = 'cheat-option-btn';
            btn.textContent = option.label;
            btn.onclick = () => {
                cheatMenuModal.style.display = 'none';
                option.action();
            };
            cheatButtonContainer.appendChild(btn);
        });

        // Add close button that resumes game
        const closeBtn = document.createElement('button');
        closeBtn.className = 'cheat-option-btn';
        closeBtn.textContent = 'Close Menu';
        closeBtn.style.background = 'linear-gradient(135deg, #666, #999)';
        closeBtn.style.marginTop = '15px';
        closeBtn.onclick = () => {
            cheatMenuModal.style.display = 'none';
            // Resume game if it wasn't paused before
            if (!wasPaused) {
                this.isPaused = false;
            }
        };
        cheatButtonContainer.appendChild(closeBtn);

        cheatMenuModal.style.display = 'block';
    }

    showPowerUpSelection() {
        const powerUpTypes = [
            'paddle', 'slow', 'speed', 'reverse', 'shrink', 'multiball', 'jackpot',
            'dangerzone', 'teleport', 'invisiblewalls', 'shrinkpaddles', 'centerwall',
            'fireball', 'zigzag', 'split', 'mirror', 'jumpscare', 'shrek'
        ];

        this.showListSelection('Spawn Power-Up', powerUpTypes, (selected) => {
            const x = 100 + Math.random() * 400;
            const y = 50 + Math.random() * 300;
            this.powerUps.push({ x, y, size: 12, type: selected.toLowerCase() });
            alert(`Spawned ${selected} power-up!`);
        });
    }

    showPlayerSelection(action) {
        const players = ['Player 1', this.singlePlayer ? 'AI (Player 2)' : 'Player 2'];
        this.showListSelection('Select Player', players, (selected) => {
            const player = selected.includes('Player 1') ? 1 : 2;
            if (action === 'give') {
                this.showAbilitySelection(player);
            } else if (action === 'delete') {
                this.showAbilityDeletion(player);
            }
        });
    }

    showAbilitySelection(player) {
        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI (Player 2)' : 'Player 2');
        this.showListSelection(`Give Ability - ${playerName}`, this.allAbilities, (selected) => {
            this.askForAbilityLevel(player, selected);
        });
    }

    askForAbilityLevel(player, ability) {
        const levelInput = prompt(`Enter ability level (1+) for ${ability}:`, '3');
        if (!levelInput) return;

        const level = parseInt(levelInput);
        if (isNaN(level) || level < 1) {
            alert('Please enter a valid number 1 or higher!');
            return;
        }

        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI (Player 2)' : 'Player 2');
        const playerAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const playerBranches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
        const currentLevel = playerAbilities.get(ability) || 0;

        // If setting to level 3+, show branch selection
        if (level >= 3 && !playerBranches.has(ability)) {
            this.showBranchSelection(player, ability, level, playerName);
        } else {
            playerAbilities.set(ability, Math.max(currentLevel, level));
            const branch = playerBranches.get(ability);
            const branchText = branch ? ` [Branch ${branch}]` : '';
            alert(`${playerName} received ${ability}${branchText} at Level ${level}!`);
        }
    }

    showBranchSelection(player, ability, level, playerName) {
        const levelUpModal = document.getElementById('levelUpModal');
        const branchContainer = document.getElementById('branchContainer');
        const noActionContainer = document.getElementById('noActionContainer');
        const levelUpTitle = document.getElementById('levelUpTitle');
        const branchPrompt = document.getElementById('branchPrompt');
        const branch1Btn = document.getElementById('branch1Btn');
        const branch2Btn = document.getElementById('branch2Btn');

        levelUpTitle.textContent = `${playerName} - ${ability.toUpperCase()} EVOLUTION!`;
        branchPrompt.textContent = 'Choose an evolution path to reach Level 3:';

        branch1Btn.textContent = 'Branch 1';
        branch2Btn.textContent = 'Branch 2';

        branchContainer.style.display = 'block';
        noActionContainer.style.display = 'none';

        branch1Btn.onclick = () => this.selectBranch(player, ability, level, 1);
        branch2Btn.onclick = () => this.selectBranch(player, ability, level, 2);

        levelUpModal.style.display = 'block';
    }

    selectBranch(player, ability, level, branch) {
        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI (Player 2)' : 'Player 2');
        const playerAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const playerBranches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;

        playerBranches.set(ability, branch);
        playerAbilities.set(ability, level);

        const levelUpModal = document.getElementById('levelUpModal');
        levelUpModal.style.display = 'none';

        alert(`${playerName} received ${ability} (Branch ${branch}) at Level ${level}!`);
    }

    showAbilityDeletion(player) {
        const playerAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const playerBranches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI (Player 2)' : 'Player 2');

        if (playerAbilities.size === 0) {
            alert(`${playerName} has no abilities to delete!`);
            return;
        }

        const abilityList = Array.from(playerAbilities.entries()).map(([ability, level]) => {
            const branch = playerBranches.get(ability);
            const branchText = branch ? ` [Branch ${branch}]` : '';
            return `${ability}${branchText} Lv${level}`;
        });

        this.showListSelection(`Delete Ability - ${playerName}`, abilityList, (selected) => {
            const ability = selected.split('[')[0].trim().toLowerCase();
            const playerAbilitiesMap = player === 1 ? this.player1Abilities : this.player2Abilities;
            const playerBranchesMap = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;

            if (playerAbilitiesMap.has(ability)) {
                playerAbilitiesMap.delete(ability);
                playerBranchesMap.delete(ability);
                alert(`Deleted ${ability} from ${playerName}!`);
            } else {
                alert('Ability not found!');
            }
        });
    }

    showListSelection(title, items, onSelect) {
        const listSelectModal = document.getElementById('listSelectModal');
        const listSelectTitle = document.getElementById('listSelectTitle');
        const listSelectContainer = document.getElementById('listSelectContainer');
        const listSelectCancelBtn = document.getElementById('listSelectCancelBtn');

        listSelectTitle.textContent = title;
        listSelectContainer.innerHTML = '';

        items.forEach(item => {
            const listItem = document.createElement('button');
            listItem.className = 'list-item';
            listItem.textContent = item;
            listItem.onclick = () => {
                listSelectModal.style.display = 'none';
                onSelect(item);
            };
            listSelectContainer.appendChild(listItem);
        });

        listSelectCancelBtn.onclick = () => {
            listSelectModal.style.display = 'none';
        };

        listSelectModal.style.display = 'block';
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
        // Check cooldown first
        const cooldownKey = `${player}-${ability}`;
        if (this.abilityCooldowns.has(cooldownKey) && this.abilityCooldowns.get(cooldownKey) > 0) {
            console.log(`${ability} is on cooldown for ${this.abilityCooldowns.get(cooldownKey)} frames`);
            return;
        }

        console.log(`Executing ${ability} level ${level} for Player ${player}`);

        switch(ability) {
            case 'gun':
                this.fireGun(player, level);
                break;
            case 'speed_boost':
                this.applySpeedBoost(player, level);
                break;
            case 'ghost_ball':
                this.activateGhostBall(player, level);
                break;
            case 'portal_pong':
                this.createPortal(player, level);
                break;
            case 'time_loop':
                this.activateTimeLoop(player, level);
                break;
            case 'shadow_clone':
                this.createShadowClone(player, level);
                break;
            case 'magnet_ball':
                this.activateMagnetBall(player, level);
                break;
            case 'gravity_hammer':
                this.activateGravityHammer(player, level);
                break;
            case 'joshua':
                this.activateJOSHUA(player, level);
                break;
            case 'jaisan':
                this.activateJaisan(player, level);
                break;
            case 'blind':
                this.blindOpponent(player, level);
                break;
            case 'shrink_opponent':
                this.shrinkOpponent(player, level);
                break;
            case 'slow_opponent':
                this.slowOpponent(player, level);
                break;
            case 'reverse_controls':
                this.reverseOpponentControls(player, level);
                break;
            case 'ability_stealer':
                this.fireAbilityStealer(player, level);
                break;
            case 'lag_spike':
                this.lagSpike(player, level);
                break;
            case 'paddle_growth':
                this.growPaddle(player, level);
                break;
            case 'double_points':
                this.activateDoublePoints(player, level);
                break;
            case 'power_siphon':
                this.activatePowerSiphon(player, level);
                break;
            case 'ability_swap':
                this.swapAbilities(player, level);
                break;
            default:
                console.log(`Ability ${ability} not yet implemented`);
                return; // Don't set cooldown for unimplemented abilities
        }

        // Set cooldown (using hardcoded cooldowns for now - can be extended to ABILITY_DATA)
        const cooldowns = {
            'gun': 300,
            'speed_boost': 400,
            'paddle_growth': 400,
            'slow_opponent': 1000,
            'ghost_ball': 1000,
            'portal_pong': 800,
            'time_loop': 600,
            'shadow_clone': 600,
            'magnet_ball': 500,
            'gravity_hammer': 400,
            'joshua': 500,
            'jaisan': 500,
            'blind': 600,
            'shrink_opponent': 670,
            'reverse_controls': 800,
            'ability_stealer': 500,
            'lag_spike': 600,
            'double_points': 500,
            'power_siphon': 600,
            'ability_swap': 700
        };

        const cooldown = cooldowns[ability] || 300;
        this.abilityCooldowns.set(cooldownKey, cooldown);
    }
    
    fireGun(player, level) {
        const startX = player === 1 ? 20 : CANVAS_WIDTH - 20;
        const paddleY = player === 1 ? this.paddle1Y : this.paddle2Y;
        const paddleCenterY = paddleY + (player === 1 ? this.paddle1Height : this.paddle2Height) / 2;
        const direction = player === 1 ? 1 : -1;
        
        // First bullet always horizontal
        this.bullets.push({
            x: startX,
            y: paddleCenterY,
            velX: 8 * direction,
            velY: 0,
            owner: player,
            level: level,
            type: 'gun'
        });
        
        // Add angled bullets for levels 2+ (spread pattern)
        for (let i = 1; i < level; i++) {
            const angleOffset = (i % 2 === 0 ? 1 : -1) * ((i + 1) / 2) * 15; // 15 degree increments
            const angle = (angleOffset * Math.PI) / 180;
            const velX = 8 * direction * Math.cos(angle);
            const velY = 8 * direction * Math.sin(angle);
            
            this.bullets.push({
                x: startX,
                y: paddleCenterY,
                velX: velX,
                velY: velY,
                owner: player,
                level: level,
                type: 'gun'
            });
        }
    }
    
    applySpeedBoost(player, level) {
        // Speed boost is PASSIVE - increases paddle movement speed by 30% per level (capped at 60%)
        // This is applied in movement calculations, not here
        this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 15, '#00ff00');
    }
    
    activateGhostBall(player, level) {
        this.ghostBallActive = true;
        setTimeout(() => { this.ghostBallActive = false; }, 3000);
        this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 20, '#8000ff');
    }
    
    createPortal(player, level) {
        // Portal Pong placement logic
        const isPlayer1 = player === 1;
        const stage = isPlayer1 ? this.player1PortalPlacementStage : this.player2PortalPlacementStage;
        const branch = isPlayer1 ? this.player1AbilityBranches.get('portal_pong') || 0 : this.player2AbilityBranches.get('portal_pong') || 0;
        
        if (stage === 0) {
            // Place entrance portal
            if (branch === 1 && level >= 3) {
                // Branch 1: Wormhole Master - place at ball's X position
                const portalX = Math.max(50, Math.min(550, this.ballX));
                if (isPlayer1) {
                    this.player1PortalEntranceX = portalX;
                    this.player1PortalEntranceY = this.paddle1Y + this.paddle1Height / 2;
                    this.player1PortalPlacementStage = 1;
                } else {
                    this.player2PortalEntranceX = portalX;
                    this.player2PortalEntranceY = this.paddle2Y + this.paddle2Height / 2;
                    this.player2PortalPlacementStage = 1;
                }
            } else {
                // Branch 0 or Branch 2: Fixed position near paddle
                if (isPlayer1) {
                    this.player1PortalEntranceX = 50;
                    this.player1PortalEntranceY = this.paddle1Y + this.paddle1Height / 2;
                    this.player1PortalPlacementStage = 1;
                } else {
                    this.player2PortalEntranceX = CANVAS_WIDTH - 50;
                    this.player2PortalEntranceY = this.paddle2Y + this.paddle2Height / 2;
                    this.player2PortalPlacementStage = 1;
                }
            }
            console.log(`Player ${player}: Portal entrance placed at (${isPlayer1 ? this.player1PortalEntranceX : this.player2PortalEntranceX}, ${isPlayer1 ? this.player1PortalEntranceY : this.player2PortalEntranceY})`)
        } else if (stage === 1) {
            // Place exit portal
            if (branch === 1 && level >= 3) {
                // Branch 1: Wormhole Master - place at ball's X position
                const portalX = Math.max(50, Math.min(550, this.ballX));
                if (isPlayer1) {
                    this.player1PortalExitX = portalX;
                    this.player1PortalExitY = this.paddle1Y + this.paddle1Height / 2;
                } else {
                    this.player2PortalExitX = portalX;
                    this.player2PortalExitY = this.paddle2Y + this.paddle2Height / 2;
                }
            } else {
                // Branch 0 or Branch 2: Fixed position
                if (isPlayer1) {
                    this.player1PortalExitX = 50;
                    this.player1PortalExitY = this.paddle1Y + this.paddle1Height / 2;
                } else {
                    this.player2PortalExitX = CANVAS_WIDTH - 50;
                    this.player2PortalExitY = this.paddle2Y + this.paddle2Height / 2;
                }
            }
            
            // Activate portals with duration
            if (isPlayer1) {
                this.player1PortalPlacementStage = 2;
                this.player1PortalDuration = 600; // 10 seconds
                this.player1PortalTimer = 0;
                this.portalTimer1 = 0;
            } else {
                this.player2PortalPlacementStage = 2;
                this.player2PortalDuration = 600;
                this.player2PortalTimer = 0;
                this.portalTimer2 = 0;
            }
            console.log(`Player ${player}: Portal exit placed and activated!`);
        }
    }
    
    activateTimeLoop(player, level) {
        if (!this.gameStateHistory) this.gameStateHistory = [];
        this.gameStateHistory.push({
            ballX: this.ballX,
            ballY: this.ballY,
            ballVelX: this.ballVelX,
            ballVelY: this.ballVelY,
            paddle1Y: this.paddle1Y,
            paddle2Y: this.paddle2Y
        });
        if (this.gameStateHistory.length > 30) this.gameStateHistory.shift();
    }
    
    createShadowClone(player, level) {
        this.shadowClones.set(`shadow-${player}`, {
            count: 3 + level,
            positions: [],
            duration: 600 + level * 50
        });
    }
    
    activateMagnetBall(player, level) {
        // Magnet ball manual activation toggles repel mode for Branch 2 (Force Field)
        if (player === 1) {
            const branch = this.player1AbilityBranches.get('magnet_ball') || 0;
            if (branch === 2 && level >= 3) {
                // Toggle between attract and repel modes
                this.player1MagnetRepelMode = !this.player1MagnetRepelMode;
                console.log(`Player 1 Magnet Ball: Switched to ${this.player1MagnetRepelMode ? 'REPEL' : 'ATTRACT'} mode`);
            }
        } else if (player === 2) {
            const branch = this.player2AbilityBranches.get('magnet_ball') || 0;
            if (branch === 2 && level >= 3) {
                // Toggle between attract and repel modes
                this.player2MagnetRepelMode = !this.player2MagnetRepelMode;
                console.log(`Player 2 Magnet Ball: Switched to ${this.player2MagnetRepelMode ? 'REPEL' : 'ATTRACT'} mode`);
            }
        }
    }
    
    activateGravityHammer(player, level) {
        this.ballVelY += 5 + level;
        this.createImpactEffect(this.ballX, this.ballY, 20);
    }
    
    activateJOSHUA(player, level) {
        // JOSHUA: Auto-deflect when ball reaches paddle with 2x speed multiplier
        if (player === 1) {
            this.joshua1Active = true;
            this.joshua1Duration = 500; // 5 seconds
        } else {
            this.joshua2Active = true;
            this.joshua2Duration = 500; // 5 seconds
        }
        this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 20, '#ffaa00');
    }
    
    blindOpponent(player, level) {
        // Blind: Make opponent paddle nearly invisible for 300ms + 30ms per level
        const duration = 300 + (level - 1) * 30;
        if (player === 1) {
            this.player2BlindDuration = duration;
        } else {
            this.player1BlindDuration = duration;
        }
    }
    
    shrinkOpponent(player, level) {
        // Shrink: Reduce opponent paddle height for 150ms + 30ms per level
        const duration = 150 + (level - 1) * 30;
        if (player === 1) {
            this.paddle2Height = Math.max(20, PADDLE_HEIGHT - 20);
            this.paddle2ShrinkDuration = duration;
        } else {
            this.paddle1Height = Math.max(20, PADDLE_HEIGHT - 20);
            this.paddle1ShrinkDuration = duration;
        }
    }
    
    slowOpponent(player, level) {
        // Slow opponent is PASSIVE - continuously reduces opponent movement speed
        // Formula: speed *= 0.7^level (always active, not triggered)
        // No action needed here - applied in getAIPaddleSpeed()
    }
    
    reverseOpponentControls(player, level) {
        // Reverse Controls: Flip opponent's keybinds for 150ms + 20ms per level
        const duration = 150 + (level - 1) * 20;
        if (player === 1) {
            this.player2ReverseControls = true;
            this.reverseTimer2 = 0;
            this.reverseDuration = duration;
        } else {
            this.player1ReverseControls = true;
            this.reverseTimer1 = 0;
            this.reverseDuration = duration;
        }
    }
    
    fireAbilityStealer(player, level) {
        const startX = player === 1 ? 20 : CANVAS_WIDTH - 20;
        const paddleY = player === 1 ? this.paddle1Y : this.paddle2Y;
        const paddleCenterY = paddleY + (player === 1 ? this.paddle1Height : this.paddle2Height) / 2;
        const direction = player === 1 ? 1 : -1;
        
        // First bullet always horizontal
        this.bullets.push({
            x: startX,
            y: paddleCenterY,
            velX: 6 * direction,
            velY: 0,
            owner: player,
            level: level,
            type: 'stealer'
        });
        
        // Add angled bullets for levels 2+ (spread pattern)
        for (let i = 1; i < level; i++) {
            const angleOffset = (i % 2 === 0 ? 1 : -1) * ((i + 1) / 2) * 20; // 20 degree increments
            const angle = (angleOffset * Math.PI) / 180;
            const velX = 6 * direction * Math.cos(angle);
            const velY = 6 * direction * Math.sin(angle);
            
            this.bullets.push({
                x: startX,
                y: paddleCenterY + (velY * 10),
                velX: velX,
                velY: velY,
                owner: player,
                level: level,
                type: 'stealer'
            });
        }
    }
    
    lagSpike(player, level) {
        if (player === 1) {
            // Teleport player 2 paddle to random location
            this.paddle2Y = Math.random() * (CANVAS_HEIGHT - PADDLE_HEIGHT);
        } else {
            // Teleport player 1 paddle to random location
            this.paddle1Y = Math.random() * (CANVAS_HEIGHT - PADDLE_HEIGHT);
        }
        this.createParticleEffect(player === 1 ? CANVAS_WIDTH - 50 : 50, 200, 20, '#ff00ff');
    }
    
    createParticleEffect(x, y, count, color) {
        for (let i = 0; i < count; i++) {
            const angle = (Math.PI * 2 * i) / count;
            const speed = 1 + Math.random() * 3;
            this.particles.push({
                x: x,
                y: y,
                velX: Math.cos(angle) * speed,
                velY: Math.sin(angle) * speed,
                life: 30,
                size: 2 + Math.random() * 2,
                color: color || 'rgba(255, 255, 255, 0.8)'
            });
        }
    }
    
    growPaddle(player, level) {
        if (player === 1) {
            this.paddle1Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * level);
        } else {
            this.paddle2Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * level);
        }
        this.createParticleEffect(player === 1 ? 20 : CANVAS_WIDTH - 20, player === 1 ? this.paddle1Y + this.paddle1Height / 2 : this.paddle2Y + this.paddle2Height / 2, 15, '#00ff00');
    }
    
    activateDoublePoints(player, level) {
        if (player === 1) {
            this.doublePointsActive1 = true;
            setTimeout(() => { this.doublePointsActive1 = false; }, 5000 * level);
        } else {
            this.doublePointsActive2 = true;
            setTimeout(() => { this.doublePointsActive2 = false; }, 5000 * level);
        }
        this.createParticleEffect(this.ballX, this.ballY, 20, '#ffff00');
    }
    
    activateJaisan(player, level) {
        // JAISAN: ULTIMATE - Auto-deflect with 3x speed (beats JOSHUA!)
        if (player === 1) {
            this.jaisan1Active = true;
            this.jaisan1Duration = 500; // 5 seconds
        } else {
            this.jaisan2Active = true;
            this.jaisan2Duration = 500; // 5 seconds
        }
        this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 30, '#ff00ff');
    }
    
    activatePowerSiphon(player, level) {
        // Steal one random ability from opponent if they have abilities
        const targetAbilities = player === 1 ? this.player2Abilities : this.player1Abilities;
        if (targetAbilities.size > 0) {
            const abilityArray = Array.from(targetAbilities.keys());
            const stolenAbility = abilityArray[Math.floor(Math.random() * abilityArray.length)];
            const stolenLevel = targetAbilities.get(stolenAbility);
            
            targetAbilities.delete(stolenAbility);
            
            const myAbilities = player === 1 ? this.player1Abilities : this.player2Abilities;
            myAbilities.set(stolenAbility, stolenLevel);
            
            this.createParticleEffect(this.ballX, this.ballY, 20, '#ff00ff');
        }
    }
    
    swapAbilities(player, level) {
        // Swap all abilities between players
        if (!this.singlePlayer) {
            const temp = new Map(this.player1Abilities);
            this.player1Abilities = new Map(this.player2Abilities);
            this.player2Abilities = temp;
            this.createParticleEffect(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, 30, '#00ffff');
        }
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
        
        // Update map powerup effects
        this.updateMapPowerUps();
        
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
        
        // Update auto-activating ability timers
        this.updateAutoAbilities();
        
        // Game continues infinitely (no win condition)
    }
    
    updateMapPowerUps() {
        // Fireball effect
        if (this.fireballActive) {
            this.fireballDuration++;
            if (this.fireballDuration >= this.fireballMaxDuration) {
                this.fireballActive = false;
                this.fireballDuration = 0;
                // Restore initial velocity
                this.ballVelX = this.initialBallVelX;
                this.ballVelY = this.initialBallVelY;
            }
        }
        
        // Split effect
        if (this.splitActive) {
            this.splitDuration++;
            if (this.splitDuration >= this.splitMaxDuration) {
                this.splitActive = false;
                this.splitDuration = 0;
                // Restore initial velocity
                this.ballVelX = this.initialBallVelX;
                this.ballVelY = this.initialBallVelY;
            }
        }
        
        // Teleport effect
        if (this.teleportActive) {
            this.teleportDuration++;
            if (this.teleportDuration >= this.teleportMaxDuration) {
                this.teleportActive = false;
                this.teleportDuration = 0;
            }
        }
        
        // Mirror effect (visual, handled in rendering)
        if (this.mirrorActive) {
            this.mirrorDuration++;
            if (this.mirrorDuration >= this.mirrorMaxDuration) {
                this.mirrorActive = false;
                this.mirrorDuration = 0;
            }
        }
        
        // Danger Zone effect
        if (this.dangerZoneActive) {
            this.dangerZoneDuration++;
            if (this.dangerZoneDuration >= this.dangerZoneMaxDuration) {
                this.dangerZoneActive = false;
                this.dangerZoneDuration = 0;
            }
        }
        
        // Invisible Walls effect
        if (this.invisibleWallsActive) {
            this.invisibleWallsDuration++;
            if (this.invisibleWallsDuration >= this.invisibleWallMaxDuration) {
                this.invisibleWallsActive = false;
                this.invisibleWallsDuration = 0;
            }
            
            // Check if ball hits the invisible wall
            const ballCenterY = this.ballY + BALL_SIZE / 2;
            if (Math.abs(ballCenterY - this.invisibleWallY) < 10) {
                this.ballVelY *= -1;
                this.ballY = this.invisibleWallY + (this.ballVelY > 0 ? 5 : -20);
            }
        }
        
        // Shrink Paddles effect
        if (this.shrinkPaddlesActive) {
            this.shrinkPaddlesDuration++;
            if (this.shrinkPaddlesDuration >= this.shrinkPaddlesMaxDuration) {
                this.shrinkPaddlesActive = false;
                this.shrinkPaddlesDuration = 0;
            }
        }
        
        // Center Wall effect
        if (this.centerWallActive) {
            this.centerWallDuration++;
            if (this.centerWallDuration >= this.centerWallMaxDuration) {
                this.centerWallActive = false;
                this.centerWallDuration = 0;
            }
            
            // Check if ball is crossing the center wall (X position between 295-305)
            const ballCenterX = this.ballX + BALL_SIZE / 2;
            const ballCenterY = this.ballY + BALL_SIZE / 2;
            
            if (ballCenterX >= 295 && ballCenterX <= 305) {
                // Check if ball hit the wall (not in the gap)
                if (ballCenterY < this.centerWallGapY - this.centerWallGapSize / 2 ||
                    ballCenterY > this.centerWallGapY + this.centerWallGapSize / 2) {
                    // Reverse X velocity
                    this.ballVelX *= -1;
                    // Push ball to appropriate side of wall
                    if (this.ballVelX > 0) {
                        this.ballX = 306; // Push to right side
                    } else {
                        this.ballX = 284; // Push to left side
                    }
                }
            }
        }
        
        // Zigzag effect
        if (this.zigzagActive) {
            this.zigzagDuration++;
            this.zigzagTimer++;
            
            if (this.zigzagDuration >= this.zigzagMaxDuration) {
                this.zigzagActive = false;
                this.zigzagDuration = 0;
                this.zigzagTimer = 0;
            }
            
            if (this.zigzagTimer >= this.zigzagInterval) {
                // Change vertical direction randomly
                this.ballVelY = (Math.random() < 0.5 ? -1 : 1) * (2 + Math.random() * 4);
                this.zigzagTimer = 0;
            }
        }
    }
    
    updateAutoAbilities() {
        // Update all ability cooldowns
        for (const [key, cooldown] of this.abilityCooldowns.entries()) {
            if (cooldown > 0) {
                this.abilityCooldowns.set(key, cooldown - 1);
            }
        }

        // Gun ability timers
        if (this.player1Abilities.has('gun')) {
            this.gunTimer1++;
            if (this.gunTimer1 >= this.gunCooldown) {
                const level = this.player1Abilities.get('gun');
                this.fireGun(1, level);
                this.gunTimer1 = 0;
            }
        }
        
        if (this.player2Abilities.has('gun')) {
            this.gunTimer2++;
            if (this.gunTimer2 >= this.gunCooldown) {
                const level = this.player2Abilities.get('gun');
                this.fireGun(2, level);
                this.gunTimer2 = 0;
            }
        }
        
        // Blind ability timers (reduce duration each frame)
        if (this.player1BlindDuration > 0) {
            this.player1BlindDuration--;
        }
        if (this.player2BlindDuration > 0) {
            this.player2BlindDuration--;
        }
        
        // Slow opponent timers
        if (this.player1SlowDuration > 0) {
            this.player1SlowDuration--;
        }
        if (this.player2SlowDuration > 0) {
            this.player2SlowDuration--;
        }
        
        // Reverse controls timers
        if (this.player1ReverseControls && this.reverseTimer1++ >= this.reverseDuration) {
            this.player1ReverseControls = false;
            this.reverseTimer1 = 0;
        }
        if (this.player2ReverseControls && this.reverseTimer2++ >= this.reverseDuration) {
            this.player2ReverseControls = false;
            this.reverseTimer2 = 0;
        }
        
        // Ability stealer timers
        if (this.player1Abilities.has('ability_stealer')) {
            this.stealerTimer1++;
            if (this.stealerTimer1 >= this.stealerCooldown) {
                const level = this.player1Abilities.get('ability_stealer');
                this.fireAbilityStealer(1, level);
                this.stealerTimer1 = 0;
            }
        }
        
        if (this.player2Abilities.has('ability_stealer')) {
            this.stealerTimer2++;
            if (this.stealerTimer2 >= this.stealerCooldown) {
                const level = this.player2Abilities.get('ability_stealer');
                this.fireAbilityStealer(2, level);
                this.stealerTimer2 = 0;
            }
        }
        
        // Lag spike timers
        if (this.player1Abilities.has('lag_spike')) {
            this.lagSpikeTimer1++;
            if (this.lagSpikeTimer1 >= this.lagSpikeCooldown) {
                const level = this.player1Abilities.get('lag_spike');
                this.lagSpike(1, level);
                this.lagSpikeTimer1 = 0;
            }
        }
        
        if (this.player2Abilities.has('lag_spike')) {
            this.lagSpikeTimer2++;
            if (this.lagSpikeTimer2 >= this.lagSpikeCooldown) {
                const level = this.player2Abilities.get('lag_spike');
                this.lagSpike(2, level);
                this.lagSpikeTimer2 = 0;
            }
        }
        
        // JOSHUA and JAISAN duration timers
        if (this.joshua1Duration > 0) this.joshua1Duration--;
        else this.joshua1Active = false;
        
        if (this.joshua2Duration > 0) this.joshua2Duration--;
        else this.joshua2Active = false;
        
        if (this.jaisan1Duration > 0) this.jaisan1Duration--;
        else this.jaisan1Active = false;
        
        if (this.jaisan2Duration > 0) this.jaisan2Duration--;
        else this.jaisan2Active = false;
        
        // Shrink opponent durations (restore paddle heights when expired)
        if (this.paddle1ShrinkDuration > 0) {
            this.paddle1ShrinkDuration--;
        } else {
            // Restore to base height with paddle_growth passive applied
            const p1PaddleGrowthLevel = this.player1Abilities.get('paddle_growth') || 0;
            this.paddle1Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * p1PaddleGrowthLevel);
        }
        
        if (this.paddle2ShrinkDuration > 0) {
            this.paddle2ShrinkDuration--;
        } else {
            // Restore to base height with paddle_growth passive applied
            const p2PaddleGrowthLevel = this.player2Abilities.get('paddle_growth') || 0;
            this.paddle2Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + 15 * p2PaddleGrowthLevel);
        }
        
        // Magnet Ball - continuous passive attraction/repulsion effect
        // Player 1 magnet ball
        if (this.player1Abilities.get('magnet_ball') > 0) {
            this.player1MagnetActive = true;
            const magnetLevel = this.player1Abilities.get('magnet_ball');
            const magnetBranch = this.player1AbilityBranches.get('magnet_ball') || 0;
            
            // Base radius and strength (NERFED)
            let baseRadius = 200;
            let baseStrength = magnetLevel;
            
            // Branch 1: Graviton Sphere - increased radius and strength
            if (magnetBranch === 1 && magnetLevel >= 3) {
                baseRadius += (magnetLevel - 2) * 15;
                baseStrength += (magnetLevel - 2);
            }
            
            // If ball is within range and moving towards paddle
            if (this.ballX < baseRadius && this.ballVelX < 0) {
                const paddle1CenterY = this.paddle1Y + this.paddle1Height / 2;
                const ballCenterY = this.ballY + BALL_SIZE / 2;
                const dy = paddle1CenterY - ballCenterY;
                
                // Branch 2: Force Field - repel mode
                if (magnetBranch === 2 && magnetLevel >= 3 && this.player1MagnetRepelMode) {
                    // Repulsion mode - push ball away when close
                    const repelRadius = 15 + (magnetLevel - 2) * 3;
                    if (Math.abs(dy) < repelRadius) {
                        // Push away (weaker)
                        this.ballY -= (dy > 0) ? baseStrength : -baseStrength;
                    } else if (Math.abs(dy) > 5) {
                        // Normal attraction when far (weaker)
                        this.ballY += (dy > 0) ? Math.max(1, baseStrength / 2) : -Math.max(1, baseStrength / 2);
                    }
                } else if (Math.abs(dy) > 5) {
                    // Normal attraction mode
                    this.ballY += (dy > 0) ? baseStrength : -baseStrength;
                }
            }
        } else {
            this.player1MagnetActive = false;
        }
        
        // Player 2 magnet ball
        if (this.player2Abilities.get('magnet_ball') > 0) {
            this.player2MagnetActive = true;
            const magnetLevel = this.player2Abilities.get('magnet_ball');
            const magnetBranch = this.player2AbilityBranches.get('magnet_ball') || 0;
            
            // Base radius and strength (NERFED)
            let baseRadius = 200;
            let baseStrength = magnetLevel;
            
            // Branch 1: Graviton Sphere - increased radius and strength
            if (magnetBranch === 1 && magnetLevel >= 3) {
                baseRadius += (magnetLevel - 2) * 15;
                baseStrength += (magnetLevel - 2);
            }
            
            // If ball is within range and moving towards paddle
            if (this.ballX > CANVAS_WIDTH - baseRadius && this.ballVelX > 0) {
                const paddle2CenterY = this.paddle2Y + this.paddle2Height / 2;
                const ballCenterY = this.ballY + BALL_SIZE / 2;
                const dy = paddle2CenterY - ballCenterY;
                
                // Branch 2: Force Field - repel mode
                if (magnetBranch === 2 && magnetLevel >= 3 && this.player2MagnetRepelMode) {
                    // Repulsion mode - push ball away when close
                    const repelRadius = 15 + (magnetLevel - 2) * 3;
                    if (Math.abs(dy) < repelRadius) {
                        // Push away (weaker)
                        this.ballY -= (dy > 0) ? baseStrength : -baseStrength;
                    } else if (Math.abs(dy) > 5) {
                        // Normal attraction when far (weaker)
                        this.ballY += (dy > 0) ? Math.max(1, baseStrength / 2) : -Math.max(1, baseStrength / 2);
                    }
                } else if (Math.abs(dy) > 5) {
                    // Normal attraction mode
                    this.ballY += (dy > 0) ? baseStrength : -baseStrength;
                }
            }
        } else {
            this.player2MagnetActive = false;
        }
    }
    
    handlePaddleMovement() {
        // Player 1 movement
        let p1Speed = this.paddleSpeed;
        
        // Apply speed_boost passive (30% per level, capped at 60%)
        const p1SpeedLevel = this.player1Abilities.get('speed_boost') || 0;
        if (p1SpeedLevel > 0) {
            const speedBonus = Math.min(0.6, 0.3 * p1SpeedLevel);
            p1Speed *= (1.0 + speedBonus);
        }
        
        // Apply slow_opponent passive if opponent has it (0.7^level multiplier)
        const p1SlowLevel = this.player2Abilities.get('slow_opponent') || 0;
        if (p1SlowLevel > 0 && p1SpeedLevel === 0) { // Immune to slow if has speed boost
            p1Speed *= Math.pow(0.7, p1SlowLevel);
        }
        
        const moveUp1 = this.player1ReverseControls ? this.keybinds.player1Down : this.keybinds.player1Up;
        const moveDown1 = this.player1ReverseControls ? this.keybinds.player1Up : this.keybinds.player1Down;
        
        if (this.keys[moveUp1] && this.paddle1Y > 0) {
            this.paddle1Y -= p1Speed;
        }
        if (this.keys[moveDown1] && this.paddle1Y < CANVAS_HEIGHT - this.paddle1Height) {
            this.paddle1Y += p1Speed;
        }
        
        // Player 2 movement (or AI)
        if (!this.singlePlayer) {
            let p2Speed = this.paddleSpeed;
            
            // Apply speed_boost passive (30% per level, capped at 60%)
            const p2SpeedLevel = this.player2Abilities.get('speed_boost') || 0;
            if (p2SpeedLevel > 0) {
                const speedBonus = Math.min(0.6, 0.3 * p2SpeedLevel);
                p2Speed *= (1.0 + speedBonus);
            }
            
            // Apply slow_opponent passive if opponent has it (0.7^level multiplier)
            const p2SlowLevel = this.player1Abilities.get('slow_opponent') || 0;
            if (p2SlowLevel > 0 && p2SpeedLevel === 0) { // Immune to slow if has speed boost
                p2Speed *= Math.pow(0.7, p2SlowLevel);
            }
            
            const moveUp2 = this.player2ReverseControls ? this.keybinds.player2Down : this.keybinds.player2Up;
            const moveDown2 = this.player2ReverseControls ? this.keybinds.player2Up : this.keybinds.player2Down;
            
            if (this.keys[moveUp2] && this.paddle2Y > 0) {
                this.paddle2Y -= p2Speed;
            }
            if (this.keys[moveDown2] && this.paddle2Y < CANVAS_HEIGHT - this.paddle2Height) {
                this.paddle2Y += p2Speed;
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
            // Player 2 scores - apply double_points bonus
            const doublePointsLevel = this.player2Abilities.get('double_points') || 0;
            const points = 1 + doublePointsLevel; // Level 1 = 2 pts, Level 2 = 3 pts, etc.

            this.scorePlayer2 += points;
            this.totalPointsThisLevel2 += points;

            // Visual feedback - explosion effect at the boundary
            this.createParticleEffect(10, CANVAS_HEIGHT / 2, 30, '#ff6464');
            this.createImpactEffect(10, CANVAS_HEIGHT / 2);

            this.checkLevelUp(2);
            this.resetBallPosition('right');
        } else if (this.ballX > CANVAS_WIDTH) {
            // Player 1 scores - apply double_points bonus
            const doublePointsLevel = this.player1Abilities.get('double_points') || 0;
            const points = 1 + doublePointsLevel; // Level 1 = 2 pts, Level 2 = 3 pts, etc.

            this.scorePlayer1 += points;
            this.totalPointsThisLevel1 += points;

            // Visual feedback - explosion effect at the boundary
            this.createParticleEffect(CANVAS_WIDTH - 10, CANVAS_HEIGHT / 2, 30, '#00ffff');
            this.createImpactEffect(CANVAS_WIDTH - 10, CANVAS_HEIGHT / 2);

            this.checkLevelUp(1);
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
        let aiSpeed = this.getAISpeed();
        
        // Apply slow effect to AI
        if (this.player2SlowDuration > 0) {
            aiSpeed *= 0.5;
        }
        
        const paddle2CenterY = this.paddle2Y + this.paddle2Height / 2;
        const ballCenterY = this.ballY + BALL_SIZE / 2;
        
        // AI tracks the ball
        if (Math.abs(ballCenterY - paddle2CenterY) > 5) {
            let targetY;
            
            if (this.player2ReverseControls) {
                // Reversed: move opposite direction
                targetY = paddle2CenterY > ballCenterY ? ballCenterY : ballCenterY;
                if (ballCenterY < paddle2CenterY) {
                    this.paddle2Y = Math.min(CANVAS_HEIGHT - this.paddle2Height, this.paddle2Y + aiSpeed);
                } else {
                    this.paddle2Y = Math.max(0, this.paddle2Y - aiSpeed);
                }
            } else {
                // Normal: track ball
                if (ballCenterY < paddle2CenterY) {
                    this.paddle2Y = Math.max(0, this.paddle2Y - aiSpeed);
                } else {
                    this.paddle2Y = Math.min(CANVAS_HEIGHT - this.paddle2Height, this.paddle2Y + aiSpeed);
                }
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
        
        // Apply JOSHUA passive effect (if active, speed is halved compared to JAISAN)
        if (this.joshua2Active && !this.jaisan2Active) {
            speed *= 0.5; // JOSHUA becomes pathetic against JAISAN
        }
        
        // Apply JAISAN passive effect (if active, speed is 10x)
        if (this.jaisan2Active) {
            speed *= 10.0; // 10x speed when JAISAN active!
        }
        
        // Speed boost passive - increases speed by 30% per level (capped at 60%)
        const speedLevel = this.player2Abilities.get('speed_boost') || 0;
        if (speedLevel > 0) {
            const speedBonus = Math.min(0.6, 0.3 * speedLevel); // Cap at 60%
            speed *= (1.0 + speedBonus);
        }
        
        // Slow opponent passive - reduces AI speed by 0.7^level
        const slowLevel = this.player1Abilities.get('slow_opponent') || 0;
        if (slowLevel > 0 && !this.joshua2Active && !this.jaisan2Active && speedLevel === 0) {
            speed *= Math.pow(0.7, slowLevel);
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
        
        // Remove expired power-ups (after 30 seconds)
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            const pu = this.powerUps[i];
            pu.life = (pu.life || 0) + 1;
            if (pu.life > 1800) {
                this.powerUps.splice(i, 1);
            }
        }
    }
    
    spawnPowerUp() {
        const rand = Math.random();
        let type;
        
        // Map power-ups with rarity (from Java version)
        if (rand < 0.25) {
            type = 'fireball';
        } else if (rand < 0.50) {
            type = 'split';
        } else if (rand < 0.75) {
            type = 'teleport';
        } else if (rand < 0.85) {
            type = 'mirror';
        } else if (rand < 0.90) {
            type = 'dangerzone';
        } else if (rand < 0.93) {
            type = 'invisiblewalls';
        } else if (rand < 0.96) {
            type = 'shrinkpaddles';
        } else {
            type = 'centerwall';
        }
        
        // Random position (but NOT moving)
        const x = 100 + Math.random() * 400;
        const y = 50 + Math.random() * 300;
        
        this.powerUps.push({
            x: x,
            y: y,
            type: type,
            size: 20,
            active: true,
            life: 0
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
        // JOSHUA auto-deflect - ball reaches paddle with 2x speed multiplier
        if (this.joshua1Active && this.ballX < 30 && this.ballVelX < 0 && !this.jaisan2Active) {
            this.ballVelX = Math.abs(this.ballVelX) * 2; // Double speed on deflect
            this.ballX = 30;
        }
        if (this.joshua2Active && this.ballX > 555 && this.ballVelX > 0 && !this.jaisan1Active) {
            this.ballVelX = -Math.abs(this.ballVelX) * 2; // Double speed on deflect
            this.ballX = 555;
        }
        
        // JAISAN ULTIMATE auto-deflect - ALWAYS wins against JOSHUA!
        if (this.jaisan1Active && this.ballX < 30 && this.ballVelX < 0) {
            this.ballVelX = Math.abs(this.ballVelX) * 3; // TRIPLE speed on deflect (better than JOSHUA!)
            this.ballX = 30;
            // If opponent has JOSHUA, reduce their JOSHUA duration
            if (this.joshua2Active) {
                this.joshua2Duration = Math.min(this.joshua2Duration, 50); // Reduce JOSHUA to 0.5s
            }
        }
        if (this.jaisan2Active && this.ballX > 555 && this.ballVelX > 0) {
            this.ballVelX = -Math.abs(this.ballVelX) * 3; // TRIPLE speed on deflect
            this.ballX = 555;
            // If opponent has JOSHUA, reduce their JOSHUA duration
            if (this.joshua1Active) {
                this.joshua1Duration = Math.min(this.joshua1Duration, 50); // Reduce JOSHUA to 0.5s
            }
        }
        
        // Apply paddle height shrinking from map powerup
        const paddle1Height = this.shrinkPaddlesActive ? Math.max(20, PADDLE_HEIGHT * 0.5) : this.paddle1Height;
        const paddle2Height = this.shrinkPaddlesActive ? Math.max(20, PADDLE_HEIGHT * 0.5) : this.paddle2Height;
        
        // Ball-paddle 1 collision (with proper height)
        if (this.ballX < 20 && this.ballX + BALL_SIZE > 10 &&
            this.ballY < this.paddle1Y + paddle1Height && this.ballY + BALL_SIZE > this.paddle1Y) {
            
            if (this.ballVelX < 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = 20;
                
                // Create impact effect
                this.createImpactEffect(this.ballX, this.ballY);
            }
        }
        
        // Ball-paddle 2 collision (with proper height)
        if (this.ballX + BALL_SIZE > CANVAS_WIDTH - 20 && this.ballX < CANVAS_WIDTH - 10 &&
            this.ballY < this.paddle2Y + paddle2Height && this.ballY + BALL_SIZE > this.paddle2Y) {

            if (this.ballVelX > 0) {
                this.ballVelX = -this.ballVelX;
                this.ballX = CANVAS_WIDTH - 20 - BALL_SIZE;

                // Create impact effect
                this.createImpactEffect(this.ballX, this.ballY);
            }
        }

        // Shadow clone collisions
        this.shadowClones.forEach((cloneData, key) => {
            const player = parseInt(key.split('-')[1]);
            const isPlayer1 = player === 1;
            const baseY = isPlayer1 ? this.paddle1Y : this.paddle2Y;
            const baseHeight = isPlayer1 ? this.paddle1Height : this.paddle2Height;
            const branch = (isPlayer1 ? this.player1AbilityBranches : this.player2AbilityBranches).get('shadow_clone') || 1;

            // Check collision for each clone
            for (let i = 0; i < cloneData.count; i++) {
                let cloneX, cloneY, cloneHeight = baseHeight;

                if (branch === 1) {
                    // Trail Clone
                    const offset = (i + 1) * 15;
                    cloneX = isPlayer1 ? 10 - offset : CANVAS_WIDTH - 20 + offset;
                    cloneY = baseY;
                } else {
                    // Afterimage Defense
                    cloneX = isPlayer1 ? 10 : CANVAS_WIDTH - 20;
                    const randOffset = (i % 2 === 0 ? 1 : -1) * Math.random() * 20;
                    cloneY = Math.max(0, Math.min(CANVAS_HEIGHT - baseHeight, baseY + randOffset));
                }

                // Check ball collision with this clone
                if (this.ballX < cloneX + PADDLE_WIDTH && this.ballX + BALL_SIZE > cloneX &&
                    this.ballY < cloneY + cloneHeight && this.ballY + BALL_SIZE > cloneY) {

                    // Ball reflects (clone acts like paddle)
                    if (isPlayer1 && this.ballVelX < 0) {
                        this.ballVelX = -this.ballVelX;
                        this.ballX = cloneX + PADDLE_WIDTH;
                        this.createImpactEffect(this.ballX, this.ballY);
                    } else if (!isPlayer1 && this.ballVelX > 0) {
                        this.ballVelX = -this.ballVelX;
                        this.ballX = cloneX - BALL_SIZE;
                        this.createImpactEffect(this.ballX, this.ballY);
                    }
                }
            }
        });

        // Bullet-paddle 1 collision
        for (let i = this.bullets.length - 1; i >= 0; i--) {
            const bullet = this.bullets[i];
            if (bullet.owner !== 2) continue; // Only enemy bullets hit player 1
            
            if (bullet.x > 10 && bullet.x < 20 && 
                bullet.y > this.paddle1Y && bullet.y < this.paddle1Y + paddle1Height) {
                this.bullets.splice(i, 1);
                this.createParticleEffect(bullet.x, bullet.y, 10, '#ff6464');
                if (bullet.type === 'stealer') {
                    this.powerSiphon(2, 1); // Player 2 steals from player 1
                }
            }
        }
        
        // Bullet-paddle 2 collision
        for (let i = this.bullets.length - 1; i >= 0; i--) {
            const bullet = this.bullets[i];
            if (bullet.owner !== 1) continue; // Only enemy bullets hit player 2
            
            if (bullet.x > CANVAS_WIDTH - 20 && bullet.x < CANVAS_WIDTH - 10 && 
                bullet.y > this.paddle2Y && bullet.y < this.paddle2Y + paddle2Height) {
                this.bullets.splice(i, 1);
                this.createParticleEffect(bullet.x, bullet.y, 10, '#00ffff');
                if (bullet.type === 'stealer') {
                    this.powerSiphon(1, 2); // Player 1 steals from player 2
                }
            }
        }
        
        // Ball collision with power-ups
        for (let i = this.powerUps.length - 1; i >= 0; i--) {
            const pu = this.powerUps[i];
            
            // Check collision with ball (using distance)
            const dx = this.ballX + BALL_SIZE / 2 - pu.x;
            const dy = this.ballY + BALL_SIZE / 2 - pu.y;
            const distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < BALL_SIZE / 2 + pu.size / 2) {
                // Determine which player's side the ball is on
                const player = this.ballX < CANVAS_WIDTH / 2 ? 1 : 2;
                this.applyPowerUp(player, pu.type);
                this.powerUps.splice(i, 1);
            }
        }
    }
    
    powerSiphon(stealer, target) {
        const targetAbilities = target === 1 ? this.player1Abilities : this.player2Abilities;
        if (targetAbilities.size > 0) {
            const abilityArray = Array.from(targetAbilities.keys());
            const stolenAbility = abilityArray[Math.floor(Math.random() * abilityArray.length)];
            const stolenLevel = targetAbilities.get(stolenAbility);
            
            targetAbilities.delete(stolenAbility);
            
            const stealerAbilities = stealer === 1 ? this.player1Abilities : this.player2Abilities;
            stealerAbilities.set(stolenAbility, stolenLevel);
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
        console.log(`Player ${player} collected power-up: ${type}`);
        
        if (type === 'fireball') {
            // Fireball: Ball speeds up significantly (1.8x multiplier) and active for 500ms
            this.ballVelX *= 1.8;
            this.ballVelY *= 1.8;
            this.fireballActive = true;
            this.fireballDuration = 0;
            this.fireballMaxDuration = 500;
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 30, '#ff4400');
        } 
        else if (type === 'split') {
            // Split: Ball speeds up significantly (1.5x multiplier) for 300ms (just visual effect)
            this.ballVelX *= 1.5;
            this.ballVelY *= 1.5;
            this.splitActive = true;
            this.splitDuration = 0;
            this.splitMaxDuration = 300;
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 40, '#ffff00');
        } 
        else if (type === 'teleport') {
            // Teleport: Ball randomly teleports with random velocity
            this.ballX = 100 + Math.random() * 400;
            this.ballY = 50 + Math.random() * 300;
            this.ballVelX = (Math.random() < 0.5 ? -1 : 1) * (3 + Math.random() * 3);
            this.ballVelY = Math.random() * 6 - 3;
            this.teleportActive = true;
            this.teleportDuration = 0;
            this.teleportMaxDuration = 300;
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 50, '#aa00ff');
        } 
        else if (type === 'mirror') {
            // Mirror: Game flips horizontally for 1000ms (visual effect)
            this.mirrorActive = true;
            this.mirrorDuration = 0;
            this.mirrorMaxDuration = 1000;
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 35, '#00aaff');
        } 
        else if (type === 'dangerzone') {
            // Danger Zone: Ball becomes chaotic (random velocity), active for 1000ms
            this.ballVelX = (Math.random() - 0.5) * 10;
            this.ballVelY = (Math.random() - 0.5) * 10;
            this.dangerZoneActive = true;
            this.dangerZoneDuration = 0;
            this.dangerZoneMaxDuration = 1000;
            this.createParticleEffect(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, 60, '#ff6400');
        } 
        else if (type === 'invisiblewalls') {
            // Invisible Walls: Horizontal wall appears at random Y for 800ms, ball bounces off it
            this.invisibleWallsActive = true;
            this.invisibleWallsDuration = 0;
            this.invisibleWallMaxDuration = 800;
            this.invisibleWallY = 150 + Math.random() * 100; // Random Y between 150-250
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 40, '#00ffff');
        } 
        else if (type === 'shrinkpaddles') {
            // Shrink Paddles: Both paddles become tiny for 800ms
            this.shrinkPaddlesActive = true;
            this.shrinkPaddlesDuration = 0;
            this.shrinkPaddlesMaxDuration = 800;
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 35, '#ff00ff');
        } 
        else if (type === 'centerwall') {
            // Center Wall: Obstacle appears in center with gap for 800ms, ball bounces off it
            this.centerWallActive = true;
            this.centerWallDuration = 0;
            this.centerWallMaxDuration = 800;
            this.centerWallGapY = 100 + Math.random() * 200; // Random gap position
            this.centerWallGapSize = 80; // Gap size
            this.createParticleEffect(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, 50, '#aa00aa');
        }
        else if (type === 'zigzag') {
            // Zigzag: Ball changes vertical direction randomly every 0.3s for 400ms
            this.zigzagActive = true;
            this.zigzagDuration = 0;
            this.zigzagMaxDuration = 400;
            this.zigzagTimer = 0;
            this.zigzagInterval = 30; // Change direction every 0.3 seconds (30 frames at 100fps)
            this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 45, '#32cd32');
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
            // Draw paddles (with shrink effect if active)
            const paddle1Height = this.shrinkPaddlesActive ? Math.max(20, PADDLE_HEIGHT * 0.5) : this.paddle1Height;
            const paddle2Height = this.shrinkPaddlesActive ? Math.max(20, PADDLE_HEIGHT * 0.5) : this.paddle2Height;
            
            this.drawPaddle(10, this.paddle1Y, paddle1Height, '#00ffff', this.player1BlindDuration);
            this.drawPaddle(CANVAS_WIDTH - 20, this.paddle2Y, paddle2Height, '#ff6464', this.player2BlindDuration);
            
            // Draw invisible walls if active
            if (this.invisibleWallsActive) {
                this.ctx.strokeStyle = 'rgba(100, 200, 255, 0.5)';
                this.ctx.lineWidth = 3;
                this.ctx.setLineDash([5, 5]);
                this.ctx.beginPath();
                this.ctx.moveTo(0, this.invisibleWallY);
                this.ctx.lineTo(CANVAS_WIDTH, this.invisibleWallY);
                this.ctx.stroke();
                this.ctx.setLineDash([]);
            }
            
            // Draw center wall if active
            if (this.centerWallActive) {
                const wallX = CANVAS_WIDTH / 2;
                const gapStart = this.centerWallGapY - this.centerWallGapSize / 2;
                const gapEnd = this.centerWallGapY + this.centerWallGapSize / 2;
                
                this.ctx.fillStyle = 'rgba(128, 0, 128, 0.5)';
                // Top part of wall
                this.ctx.fillRect(wallX - 5, 0, 10, gapStart);
                // Bottom part of wall
                this.ctx.fillRect(wallX - 5, gapEnd, 10, CANVAS_HEIGHT - gapEnd);
            }
            
            // Draw ball
            this.drawBall();
            
            // Draw bullets
            this.drawBullets();

            // Draw shadow clones
            this.drawShadowClones();

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
    
    drawPaddle(x, y, height, color, blindDuration) {
        if (!height) height = PADDLE_HEIGHT;
        
        const gradient = this.ctx.createLinearGradient(x, y, x + PADDLE_WIDTH, y);
        gradient.addColorStop(0, color);
        gradient.addColorStop(1, 'rgba(255, 255, 255, 0.3)');
        
        // If blinded, draw with reduced opacity
        if (blindDuration > 0) {
            this.ctx.globalAlpha = 0.3;
        }
        
        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(x, y, PADDLE_WIDTH, height);
        
        // Glow effect
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.strokeRect(x - 2, y - 2, PADDLE_WIDTH + 4, height + 4);
        
        this.ctx.globalAlpha = 1.0;
    }
    
    drawBullets() {
        for (const bullet of this.bullets) {
            let color, glowColor;

            if (bullet.type === 'stealer') {
                // Stealer bullets are purple
                color = bullet.owner === 1 ? '#aa00ff' : '#ff00aa';
                glowColor = bullet.owner === 1 ? '#aa00ff' : '#ff00aa';
            } else {
                // Regular gun bullets are cyan/red
                color = bullet.owner === 1 ? '#00ffff' : '#ff6464';
                glowColor = bullet.owner === 1 ? '#00ffff' : '#ff6464';
            }

            this.ctx.fillStyle = color;
            this.ctx.fillRect(bullet.x - 3, bullet.y - 3, 6, 6);

            // Glow
            this.ctx.shadowColor = glowColor;
            this.ctx.shadowBlur = 10;
            this.ctx.fillRect(bullet.x - 3, bullet.y - 3, 6, 6);
            this.ctx.shadowBlur = 0;
        }
    }

    drawShadowClones() {
        // Draw shadow clones for both players
        this.shadowClones.forEach((cloneData, key) => {
            const player = parseInt(key.split('-')[1]);
            const isPlayer1 = player === 1;
            const baseY = isPlayer1 ? this.paddle1Y : this.paddle2Y;
            const baseHeight = isPlayer1 ? this.paddle1Height : this.paddle2Height;
            const branch = (isPlayer1 ? this.player1AbilityBranches : this.player2AbilityBranches).get('shadow_clone') || 1;

            // Decrease duration each frame
            cloneData.duration--;

            // Remove expired clones
            if (cloneData.duration <= 0) {
                this.shadowClones.delete(key);
                return;
            }

            // Calculate opacity based on remaining duration
            const opacity = Math.max(0.1, cloneData.duration / (600 + cloneData.level * 50));

            // Branch 1: Trail Clone - clones appear behind paddle and follow movement
            if (branch === 1) {
                for (let i = 0; i < cloneData.count; i++) {
                    const offset = (i + 1) * 15; // Spacing between clones
                    const x = isPlayer1 ? 10 - offset : CANVAS_WIDTH - 20 + offset;

                    this.ctx.globalAlpha = opacity * (1 - i * 0.1);
                    this.drawPaddle(x, baseY, baseHeight, isPlayer1 ? '#00ffff' : '#ff6464');
                }
            }
            // Branch 2: Afterimage Defense - independent shadows with slight offset
            else if (branch === 2) {
                for (let i = 0; i < cloneData.count; i++) {
                    // Random vertical offset for each shadow
                    const randOffset = (i % 2 === 0 ? 1 : -1) * Math.random() * 20;
                    const cloneY = Math.max(0, Math.min(CANVAS_HEIGHT - baseHeight, baseY + randOffset));
                    const x = isPlayer1 ? 10 : CANVAS_WIDTH - 20;

                    this.ctx.globalAlpha = opacity * (1 - i * 0.15);
                    this.drawPaddle(x, cloneY, baseHeight, isPlayer1 ? '#00ffff' : '#ff6464');
                }
            }

            this.ctx.globalAlpha = 1.0;
        });
    }
    
    drawBall() {
        // Draw aura/glow around ball first (semi-transparent)
        this.ctx.fillStyle = 'rgba(0, 255, 255, 0.1)';
        this.ctx.beginPath();
        this.ctx.arc(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2 + 8, 0, Math.PI * 2);
        this.ctx.fill();
        
        this.ctx.fillStyle = 'rgba(0, 255, 255, 0.05)';
        this.ctx.beginPath();
        this.ctx.arc(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2 + 15, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Draw main ball with white core
        const gradient = this.ctx.createRadialGradient(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 0,
            this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2);
        
        gradient.addColorStop(0, '#ffffff');
        gradient.addColorStop(0.5, '#cccccc');
        gradient.addColorStop(1, '#00ffff');
        
        this.ctx.fillStyle = gradient;
        this.ctx.beginPath();
        this.ctx.arc(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Glow
        this.ctx.shadowColor = '#00ffff';
        this.ctx.shadowBlur = 15;
        this.ctx.strokeStyle = '#00ffff';
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        this.ctx.arc(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, BALL_SIZE / 2, 0, Math.PI * 2);
        this.ctx.stroke();
        this.ctx.shadowBlur = 0;
    }
    
    drawPowerUps() {
        for (const pu of this.powerUps) {
            // Get color based on type (matching Java version)
            let color;
            switch (pu.type) {
                case 'dangerzone': color = '#ff6400'; break; // Orange
                case 'teleport': color = '#8a2be2'; break; // Purple
                case 'invisiblewalls': color = '#64c8ff'; break; // Cyan
                case 'centerwall': color = '#800080'; break; // Purple
                case 'fireball': color = '#ff4500'; break; // Red-orange
                case 'zigzag': color = '#32cd32'; break; // Lime green
                case 'split': color = '#ffd700'; break; // Gold
                case 'mirror': color = '#00bfff'; break; // Deep sky blue
                case 'shrinkpaddles': color = '#ff00ff'; break; // Magenta
                default: color = '#ff00ff'; break;
            }
            
            // Draw main circle
            this.ctx.fillStyle = color;
            this.ctx.beginPath();
            this.ctx.arc(pu.x, pu.y, pu.size / 2, 0, Math.PI * 2);
            this.ctx.fill();
            
            // Draw rotating glow ring
            this.ctx.save();
            this.ctx.translate(pu.x, pu.y);
            this.ctx.rotate((this.frameCount * 0.05) % (Math.PI * 2));
            this.ctx.strokeStyle = color;
            this.ctx.lineWidth = 2;
            this.ctx.globalAlpha = 0.6;
            this.ctx.beginPath();
            this.ctx.arc(0, 0, pu.size / 2 + 3, 0, Math.PI * 2);
            this.ctx.stroke();
            this.ctx.restore();
            
            // Outer aura
            const rgb = parseInt(color.slice(1, 3), 16) + ',' + parseInt(color.slice(3, 5), 16) + ',' + parseInt(color.slice(5, 7), 16);
            this.ctx.fillStyle = `rgba(${rgb}, 0.2)`;
            this.ctx.beginPath();
            this.ctx.arc(pu.x, pu.y, pu.size / 2 + 8, 0, Math.PI * 2);
            this.ctx.fill();
            
            this.ctx.globalAlpha = 1.0;
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

        // Draw Learning AI Progress Indicator (matching Java version)
        if (this.singlePlayer && this.learningAIEnabled && this.aiDifficulty === 4) {
            const progressPercent = Math.round(this.learningProgress * 100);

            // Determine color and text based on progress
            let progressColor = '#64ff64'; // Green
            let progressText = 'AI: Learning';

            if (this.learningProgress < 0.3) {
                progressColor = '#64ff64'; // Green
                progressText = 'AI: Learning';
            } else if (this.learningProgress < 0.6) {
                progressColor = '#ffff64'; // Yellow
                progressText = 'AI: Adapting';
            } else if (this.learningProgress < 0.85) {
                progressColor = '#ffb450'; // Orange
                progressText = 'AI: Competent';
            } else {
                progressColor = '#ff7878'; // Light Red
                progressText = 'AI: Skilled';
            }

            // Draw progress text
            this.ctx.font = 'bold 11px Arial';
            this.ctx.fillStyle = progressColor;
            this.ctx.textAlign = 'right';
            this.ctx.fillText(`${progressText} ${progressPercent}%`, CANVAS_WIDTH - 10, 95);

            // Draw progress bar background
            const barX = CANVAS_WIDTH - 130;
            const barY = 100;
            const barWidth = 120;
            const barHeight = 7;

            this.ctx.fillStyle = '#333333';
            this.ctx.fillRect(barX, barY, barWidth, barHeight);

            // Draw progress bar fill
            this.ctx.fillStyle = progressColor;
            this.ctx.fillRect(barX, barY, barWidth * this.learningProgress, barHeight);
        }

        this.lastFrameTime = Date.now();

        // Draw ability menu on left and right
        this.drawAbilityMenu();
    }
    
    drawAbilityMenu() {
        // Player 1 abilities (left side)
        this.drawPlayerAbilities(1, 10, 50, true);
        
        // Player 2 abilities (right side)
        this.drawPlayerAbilities(2, CANVAS_WIDTH - 150, 50, false);
    }
    
    drawPlayerAbilities(player, x, y, alignLeft) {
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
        const level = player === 1 ? this.level1 : this.level2;
        
        // Header
        this.ctx.font = 'bold 14px Arial';
        this.ctx.fillStyle = player === 1 ? '#00ffff' : '#ff6464';
        this.ctx.textAlign = alignLeft ? 'left' : 'right';
        this.ctx.fillText(`LV ${level}`, alignLeft ? x : x + 140, y);
        
        // List abilities with levels
        let abilityIndex = 0;
        const maxAbilitiesToShow = 6;
        
        for (const [abilityName, abilityLevel] of abilities) {
            if (abilityIndex >= maxAbilitiesToShow) break;
            
            const branch = branches.get(abilityName) || 0;
            const displayName = this.getAbilityShortName(abilityName);
            let text = `${displayName}`;
            
            if (branch > 0) {
                text += ` (${branch})`;
            }
            
            this.ctx.font = '11px Arial';
            this.ctx.fillStyle = '#aaa';
            this.ctx.textAlign = alignLeft ? 'left' : 'right';
            
            // Check if this ability has a cooldown active
            let cooldownActive = false;
            let cooldownPercent = 0;
            
            if (abilityName === 'gun') {
                const timer = player === 1 ? this.gunTimer1 : this.gunTimer2;
                if (timer > 0) {
                    cooldownActive = true;
                    cooldownPercent = (timer / this.gunCooldown) * 100;
                }
            } else if (abilityName === 'ability_stealer') {
                const timer = player === 1 ? this.stealerTimer1 : this.stealerTimer2;
                if (timer > 0) {
                    cooldownActive = true;
                    cooldownPercent = (timer / this.stealerCooldown) * 100;
                }
            } else if (abilityName === 'lag_spike') {
                const timer = player === 1 ? this.lagSpikeTimer1 : this.lagSpikeTimer2;
                if (timer > 0) {
                    cooldownActive = true;
                    cooldownPercent = (timer / this.lagSpikeCooldown) * 100;
                }
            }
            
            if (cooldownActive) {
                text += ` (${Math.ceil(cooldownPercent / 20)})`;
                this.ctx.fillStyle = '#ff6464';
            }
            
            this.ctx.fillText(text, alignLeft ? x : x + 140, y + 20 + (abilityIndex * 15));
            abilityIndex++;
        }
    }
    
    getAbilityShortName(ability) {
        const shortNames = {
            'speed_boost': 'Speed+',
            'paddle_growth': 'Growth',
            'double_points': 'Points',
            'slow_opponent': 'SlowOp',
            'gun': 'Gun',
            'ability_stealer': 'Stealer',
            'lag_spike': 'Lag',
            'reverse_controls': 'Reverse',
            'joshua': 'Joshua',
            'blind': 'Blind',
            'shrink_opponent': 'Shrink',
            'ghost_ball': 'Ghost',
            'jaisan': 'Jaisan',
            'ability_swap': 'Swap',
            'gravity_hammer': 'Hammer',
            'magnet_ball': 'Magnet',
            'shadow_clone': 'Clone',
            'portal_pong': 'Portal',
            'power_siphon': 'Siphon',
            'time_loop': 'Loop'
        };
        return shortNames[ability] || ability;
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

    checkLevelUp(player) {
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
        const totalPoints = player === 1 ? this.totalPointsThisLevel1 : this.totalPointsThisLevel2;
        const pointsNeeded = player === 1 ? this.pointsToNextLevel1 : this.pointsToNextLevel2;
        
        if (totalPoints >= pointsNeeded) {
            // Level up
            if (player === 1) {
                this.level1++;
                this.totalPointsThisLevel1 = 0;
                this.pointsToNextLevel1++;
            } else {
                this.level2++;
                this.totalPointsThisLevel2 = 0;
                this.pointsToNextLevel2++;
            }
            
            // Check for branchable abilities
            const branchableAbilities = [];
            for (let [ability, level] of abilities) {
                if (level === 2 && !branches.has(ability)) {
                    branchableAbilities.push(ability);
                }
            }
            
            // 70% chance to offer branch if available
            if (branchableAbilities.length > 0 && Math.random() < 0.7) {
                const ability = branchableAbilities[Math.floor(Math.random() * branchableAbilities.length)];
                this.showBranchChoice(player, ability);
            } else {
                // Otherwise offer a new ability
                this.showAbilityChoice(player);
            }
        }
    }

    showBranchChoice(player, ability) {
        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI' : 'Player 2');
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;

        if (this.singlePlayer && player === 2) {
            // AI randomly chooses
            const branch = Math.random() < 0.5 ? 1 : 2;
            branches.set(ability, branch);
            abilities.set(ability, 3);
            console.log(`AI: ${ability} evolved to Branch ${branch}!`);
            return;
        }

        // Show branch selection modal
        const levelUpModal = document.getElementById('levelUpModal');
        const branchContainer = document.getElementById('branchContainer');
        const noActionContainer = document.getElementById('noActionContainer');
        const levelUpTitle = document.getElementById('levelUpTitle');
        const branchPrompt = document.getElementById('branchPrompt');
        const branch1Btn = document.getElementById('branch1Btn');
        const branch2Btn = document.getElementById('branch2Btn');

        levelUpTitle.textContent = `${playerName} - ${ability.toUpperCase()} EVOLUTION!`;
        branchPrompt.textContent = 'Choose an evolution path:';

        branch1Btn.textContent = ' Branch 1 ';
        branch2Btn.textContent = ' Branch 2 ';

        branchContainer.style.display = 'block';
        noActionContainer.style.display = 'none';

        branch1Btn.onclick = () => {
            branches.set(ability, 1);
            abilities.set(ability, 3);
            levelUpModal.style.display = 'none';
        };

        branch2Btn.onclick = () => {
            branches.set(ability, 2);
            abilities.set(ability, 3);
            levelUpModal.style.display = 'none';
        };

        levelUpModal.style.display = 'block';
    }

    showAbilityChoice(player) {
        const playerName = player === 1 ? 'Player 1' : (this.singlePlayer ? 'AI' : 'Player 2');
        const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
        const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;

        // Get available abilities (exclude those at level 2 waiting for branch)
        const available = [];
        for (let ability of this.allAbilities) {
            const currentLevel = abilities.get(ability) || 0;

            // Skip if at level 2 and waiting for branch
            if (currentLevel === 2 && !branches.has(ability)) {
                continue;
            }

            // Apply rarity system
            let chance = 1.0;
            if (ability === 'jaisan') chance = 0.001;
            else if (ability === 'joshua') chance = 0.01;
            else if (ability === 'ghost_ball' || ability === 'time_loop') chance = 0.05;
            else if (['gun', 'slow_opponent', 'reverse_controls', 'double_points', 'lag_spike'].includes(ability)) chance = 0.12;
            else if (ability === 'ability_stealer' || ability === 'blind') chance = 0.30;
            // else chance = 1.0 for common abilities

            if (Math.random() < chance) {
                available.push(ability);
            }
        }

        // If no abilities available due to rarity, give common one
        if (available.length === 0) {
            available.push(Math.random() < 0.5 ? 'speed_boost' : 'paddle_growth');
        }

        // Pick 3 random abilities
        const choices = [];
        for (let i = 0; i < 3 && available.length > 0; i++) {
            const idx = Math.floor(Math.random() * available.length);
            choices.push(available[idx]);
            available.splice(idx, 1);
        }

        if (this.singlePlayer && player === 2) {
            // AI randomly chooses
            const chosen = choices[Math.floor(Math.random() * choices.length)];
            const newLevel = (abilities.get(chosen) || 0) + 1;
            abilities.set(chosen, newLevel);
            console.log(`AI chose: ${chosen} (Level ${newLevel})`);
            return;
        }

        // Show choice with modal dialog using list selection
        this.showListSelection(
            `${playerName} - LEVEL UP!`,
            choices,
            (selected) => {
                const newLevel = (abilities.get(selected) || 0) + 1;
                abilities.set(selected, newLevel);
                console.log(`Got: ${selected} (Level ${newLevel})`);
            }
        );
    }
}

// Initialize game when page loads
window.addEventListener('DOMContentLoaded', () => {
    window.game = new PongGame();
    // Show settings modal instead of auto-starting
    document.getElementById('settingsModal').style.display = 'block';
});

// Modal functions
function closeSettings() {
    const modal = document.getElementById('settingsModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Safe UI entrypoint for Start buttons (adds logging and error capture)
window.startGameFromUI = function() {
    try {
        console.log('startGameFromUI called');
        if (window.game && typeof window.game.startGame === 'function') {
            window.game.startGame();
            console.log('startGame invoked on game instance');
        } else {
            console.warn('window.game or startGame not available yet');
            alert('Game not ready yet. Please wait a moment and try again.');
        }
    } catch (err) {
        console.error('Error while starting game:', err);
        alert('An error occurred while starting the game. See console for details.');
    }
};

// Simple on-page debug logger (writes to #debugConsole if present)
function debugLog(...args) {
    try {
        const el = document.getElementById('debugConsole');
        const text = args.map(a => (typeof a === 'string' ? a : JSON.stringify(a))).join(' ');
        console.log(...args);
        if (el) {
            if (el.style.display === 'none') el.style.display = 'block';
            const time = new Date().toLocaleTimeString();
            const entry = document.createElement('div');
            entry.textContent = `[${time}] ${text}`;
            el.appendChild(entry);
            // keep panel scrolled to bottom
            el.scrollTop = el.scrollHeight;
        }
    } catch (e) {
        // swallow
        console.log(...args);
    }
}

// Capture uncaught errors and display them on-page
window.addEventListener('error', function (ev) {
    debugLog('Uncaught error:', ev.message || ev.error || ev);
});

window.addEventListener('unhandledrejection', function (ev) {
    debugLog('Unhandled Promise rejection:', ev.reason || ev);
});
