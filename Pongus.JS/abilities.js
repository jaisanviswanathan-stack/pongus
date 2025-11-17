// ===== ABILITIES SYSTEM - EXTENDED MODULE =====
// This module extends the main game with full 20+ ability system with branches

// Ability definitions with branches and descriptions
const ABILITY_DATA = {
    speed_boost: {
        name: 'Speed Boost',
        description: 'Increase ball speed',
        branches: {
            1: { name: 'Sonic Dash', description: 'Quick dash ability' },
            2: { name: 'Flash', description: 'Slow time around ball' }
        },
        cooldown: 300
    },
    paddle_growth: {
        name: 'Paddle Growth',
        description: 'Grow your paddle',
        branches: {
            1: { name: 'Expansion', description: 'Static growth' },
            2: { name: 'Elastic Expansion', description: 'Dynamic growth on approach' }
        },
        cooldown: 400
    },
    double_points: {
        name: 'Double Points',
        description: 'Score multiplier',
        branches: {
            1: { name: 'Point Leech', description: 'Steal on score' },
            2: { name: 'Combo Master', description: 'Consecutive hits bonus' }
        },
        cooldown: 500
    },
    slow_opponent: {
        name: 'Slow Opponent',
        description: 'Opponent penalty',
        branches: {
            1: { name: 'Freeze Ray', description: 'Freeze paddle' },
            2: { name: 'Gravity Well', description: 'Heavier paddle' }
        },
        cooldown: 1000
    },
    gun: {
        name: 'Gun',
        description: 'Shoot projectiles',
        branches: {
            1: { name: 'Goku Laser', description: 'Beam attack' },
            2: { name: 'Vegeta Big Bang', description: 'Explosive projectile' }
        },
        cooldown: 300
    },
    ability_stealer: {
        name: 'Ability Stealer',
        description: 'Steal opponent abilities',
        branches: {
            1: { name: 'Frieza Death Beam', description: 'Steal on hit' },
            2: { name: 'Jiren Explosion', description: 'Explosive steal' }
        },
        cooldown: 500
    },
    lag_spike: {
        name: 'Lag Spike',
        description: 'Cause gameplay stutter',
        branches: {
            1: { name: 'Stutter', description: 'Brief freeze' },
            2: { name: 'Teleport Jitter', description: 'Random teleport' }
        },
        cooldown: 600
    },
    reverse_controls: {
        name: 'Reverse Controls',
        description: 'Flip opponent controls',
        branches: {
            1: { name: 'Chaos Engine', description: 'Random debuffs' },
            2: { name: 'Puppet Master', description: 'Full control takeover' }
        },
        cooldown: 800
    },
    joshua: {
        name: 'JOSHUA',
        description: 'Ultimate ability - OP paddle control',
        branches: {
            1: { name: 'Perfect Track', description: 'Never misses' },
            2: { name: 'Overdrive', description: 'Unstoppable' }
        },
        cooldown: 500
    },
    blind: {
        name: 'Blind',
        description: 'Make opponent blind',
        branches: {
            1: { name: 'Darkness', description: 'Full black' },
            2: { name: 'Blur', description: 'Distorted vision' }
        },
        cooldown: 600
    },
    shrink_opponent: {
        name: 'Shrink Opponent',
        description: 'Shrink opponent paddle',
        branches: {
            1: { name: 'Compress', description: 'Size reduction' },
            2: { name: 'Micro Paddle', description: 'Extreme shrink' }
        },
        cooldown: 670
    },
    ghost_ball: {
        name: 'Ghost Ball',
        description: 'Phase through paddles',
        branches: {
            1: { name: 'Void Pulse', description: 'Disables abilities' },
            2: { name: 'Spectral Echo', description: 'Ghost trail' }
        },
        cooldown: 1000
    },
    jaisan: {
        name: 'JAISAN',
        description: 'Ultimate ability - More OP than JOSHUA',
        branches: {
            1: { name: 'Perfection', description: 'Ultimate control' },
            2: { name: 'Infinity', description: 'Unlimited power' }
        },
        cooldown: 500
    },
    ability_swap: {
        name: 'Ability Swap',
        description: 'Swap abilities with opponent',
        branches: {
            1: { name: 'Exchange', description: 'One-time swap' },
            2: { name: 'Cycle', description: 'Continuous swap' }
        },
        cooldown: 700
    },
    gravity_hammer: {
        name: 'Gravity Hammer',
        description: 'Slam ball downward',
        branches: {
            1: { name: 'Thor Hammer', description: 'Shockwave' },
            2: { name: 'Gravity Well', description: 'Area pull' }
        },
        cooldown: 400
    },
    magnet_ball: {
        name: 'Magnet Ball',
        description: 'Attract ball to paddle',
        branches: {
            1: { name: 'Attract Mode', description: 'Pull ball' },
            2: { name: 'Force Field', description: 'Attract/Repel toggle' }
        },
        cooldown: 500
    },
    shadow_clone: {
        name: 'Shadow Clone',
        description: 'Create paddle copies',
        branches: {
            1: { name: 'Trail Clone', description: 'Behind paddle' },
            2: { name: 'Afterimage Defense', description: 'Independent shadows' }
        },
        cooldown: 600
    },
    portal_pong: {
        name: 'Portal Pong',
        description: 'Create teleport portals',
        branches: {
            1: { name: 'Portal Teleport', description: 'Two portals' },
            2: { name: 'Dimensional Rift', description: 'Three portals + movement' }
        },
        cooldown: 800
    },
    power_siphon: {
        name: 'Power Siphon',
        description: 'Drain opponent cooldowns',
        branches: {
            1: { name: 'Soul Reaper', description: 'Copy ability' },
            2: { name: 'Overload', description: 'Charge system' }
        },
        cooldown: 600
    },
    time_loop: {
        name: 'Time Loop',
        description: 'Rewind game state',
        branches: {
            1: { name: 'Chronos Rewind', description: 'Go back further' },
            2: { name: 'Temporal Echo', description: 'Ghost ball rewind' }
        },
        cooldown: 500
    }
};

// Extend PongGame class with ability system
PongGame.prototype.setupAbilities = function() {
    this.abilityLevels = new Map();
    this.abilityCooldowns = new Map();
    this.abilityEffectTimers = new Map();
    
    // Initialize all ability cooldowns to 0 (ready)
    Object.keys(ABILITY_DATA).forEach(ability => {
        this.abilityCooldowns.set(ability, 0);
    });
};

PongGame.prototype.grantAbility = function(player, abilityName, level = 1, branch = 0) {
    const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
    const branches = player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches;
    
    abilities.set(abilityName, level);
    if (branch > 0) {
        branches.set(abilityName, branch);
    }
};

PongGame.prototype.activateAbility = function(player) {
    const abilities = player === 1 ? this.player1Abilities : this.player2Abilities;
    if (abilities.size === 0) return;
    
    // Randomly pick an ability for now (or use UI selection later)
    const abilityArray = Array.from(abilities.keys());
    const ability = abilityArray[Math.floor(Math.random() * abilityArray.length)];
    const level = abilities.get(ability);
    
    this.executeAbility(player, ability, level);
};

PongGame.prototype.executeAbility = function(player, ability, level) {
    const cooldownKey = `${player}-${ability}`;
    const cooldowns = this.abilityCooldowns;
    
    if (cooldowns.get(cooldownKey) && cooldowns.get(cooldownKey) > 0) {
        return; // Ability on cooldown
    }
    
    console.log(`Executing ${ability} level ${level} for Player ${player}`);
    
    switch(ability) {
        case 'gun':
            this.fireGun(player, level);
            break;
        case 'speed_boost':
            this.applySpeedBoost(player, level);
            break;
        case 'paddle_growth':
            this.growPaddle(player, level);
            break;
        case 'slow_opponent':
            this.slowOpponent(player, level);
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
            this.activateJAISAN(player, level);
            break;
        case 'blind':
            this.blindOpponent(player, level);
            break;
        case 'shrink_opponent':
            this.shrinkOpponent(player, level);
            break;
        default:
            console.log(`Ability ${ability} not yet implemented`);
    }
    
    // Set cooldown
    const cooldownTime = ABILITY_DATA[ability]?.cooldown || 300;
    cooldowns.set(cooldownKey, cooldownTime);
};

// Ability implementations

PongGame.prototype.fireGun = function(player, level) {
    const startX = player === 1 ? 20 : CANVAS_WIDTH - 20;
    const paddleY = player === 1 ? this.paddle1Y : this.paddle2Y;
    const paddleCenterY = paddleY + (player === 1 ? this.paddle1Height : this.paddle2Height) / 2;
    const direction = player === 1 ? 1 : -1;
    
    // Create main bullet
    this.bullets.push({
        x: startX,
        y: paddleCenterY,
        velX: 8 * direction,
        velY: 0,
        owner: player,
        level: level
    });
    
    // Additional bullets for higher levels
    for (let i = 1; i < level - 2; i++) {
        const angleOffset = (i % 2 === 0 ? 1 : -1) * ((i + 1) / 2) * 15;
        const angle = (angleOffset * Math.PI) / 180;
        this.bullets.push({
            x: startX,
            y: paddleCenterY,
            velX: 8 * direction * Math.cos(angle),
            velY: 8 * direction * Math.sin(angle),
            owner: player,
            level: level
        });
    }
    
    // Visual feedback
    this.createImpactEffect(startX, paddleCenterY, 5);
};

PongGame.prototype.applySpeedBoost = function(player, level) {
    const boostFactor = 1.0 + (level * 0.1);
    this.ballVelX *= boostFactor;
    this.ballVelY *= boostFactor;
    
    // Visual effect
    this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 15, '#00ff00');
};

PongGame.prototype.growPaddle = function(player, level) {
    const growthAmount = 10 + (level * 5);
    if (player === 1) {
        this.paddle1Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + growthAmount);
    } else {
        this.paddle2Height = Math.min(CANVAS_HEIGHT, PADDLE_HEIGHT + growthAmount);
    }
};

PongGame.prototype.slowOpponent = function(player, level) {
    const slowFactor = 1.0 - (level * 0.1);
    this.ballVelX *= slowFactor;
    this.ballVelY *= slowFactor;
};

PongGame.prototype.activateGhostBall = function(player, level) {
    this.ghostBallActive = true;
    this.effectTimers.set(`ghostBall-${player}`, 300 + level * 50);
    this.createParticleEffect(this.ballX + BALL_SIZE / 2, this.ballY + BALL_SIZE / 2, 20, '#8000ff');
};

PongGame.prototype.createPortal = function(player, level) {
    const branch = (player === 1 ? this.player1AbilityBranches : this.player2AbilityBranches).get('portal_pong') || 1;
    console.log(`Creating portal for Player ${player}, Branch ${branch}, Level ${level}`);
    
    const key = `portal-${player}`;
    this.portals.set(key, {
        x: this.ballX,
        y: this.ballY,
        active: true,
        duration: 1000 + level * 100,
        branch: branch
    });
};

PongGame.prototype.activateTimeLoop = function(player, level) {
    // Record current game state and allow rewinding
    this.gameStateHistory = this.gameStateHistory || [];
    this.gameStateHistory.push({
        ballX: this.ballX,
        ballY: this.ballY,
        ballVelX: this.ballVelX,
        ballVelY: this.ballVelY,
        paddle1Y: this.paddle1Y,
        paddle2Y: this.paddle2Y,
        scorePlayer1: this.scorePlayer1,
        scorePlayer2: this.scorePlayer2
    });
    
    if (this.gameStateHistory.length > 30) {
        this.gameStateHistory.shift();
    }
    
    // Slow motion effect
    this.effectTimers.set(`timeLoop-${player}`, 100 + level * 30);
    console.log(`Time Loop activated for Player ${player}`);
};

PongGame.prototype.createShadowClone = function(player, level) {
    const key = `shadow-${player}`;
    this.shadowClones.set(key, {
        count: 3 + level,
        level: level,
        positions: [],
        duration: 600 + level * 50
    });

    console.log(`Shadow Clone created for Player ${player}, Level ${level}, Count: ${3 + level}`);
};

PongGame.prototype.activateMagnetBall = function(player, level) {
    const key = `magnet-${player}`;
    this.effectTimers.set(key, 500 + level * 50);
    
    // Attract ball to player's paddle
    if (player === 1 && this.ballX < CANVAS_WIDTH / 2) {
        const paddleCenter = this.paddle1Y + this.paddle1Height / 2;
        const force = 0.2 * level;
        this.ballVelY += (paddleCenter - this.ballY) * force;
    } else if (player === 2 && this.ballX > CANVAS_WIDTH / 2) {
        const paddleCenter = this.paddle2Y + this.paddle2Height / 2;
        const force = 0.2 * level;
        this.ballVelY += (paddleCenter - this.ballY) * force;
    }
};

PongGame.prototype.activateGravityHammer = function(player, level) {
    if (player === 1) {
        this.ballVelY += 5 + level;
    } else {
        this.ballVelY += 5 + level;
    }
    this.createImpactEffect(this.ballX, this.ballY, 20);
};

PongGame.prototype.activateJOSHUA = function(player, level) {
    const target = player === 1 ? 2 : 1;
    const paddleY = target === 1 ? this.paddle1Y : this.paddle2Y;
    const paddleHeight = target === 1 ? this.paddle1Height : this.paddle2Height;
    
    // Perfect aim ability
    const paddleCenter = paddleY + paddleHeight / 2;
    this.ballVelY = (this.ballY - paddleCenter) * -0.1; // Reverse trajectory
};

PongGame.prototype.activateJAISAN = function(player, level) {
    // Even more OP - multiple effects
    this.activateJOSHUA(player, level);
    this.ballVelX *= 1.5;
    this.ballVelY *= 1.5;
    this.createParticleEffect(this.ballX, this.ballY, 30, '#ff00ff');
};

PongGame.prototype.blindOpponent = function(player, level) {
    const target = player === 1 ? 2 : 1;
    const key = `blind-${target}`;
    this.effectTimers.set(key, 300 + level * 50);
};

PongGame.prototype.shrinkOpponent = function(player, level) {
    const target = player === 1 ? 2 : 1;
    const shrinkAmount = 10 + level * 5;
    
    if (target === 1) {
        this.paddle1Height = Math.max(10, PADDLE_HEIGHT - shrinkAmount);
    } else {
        this.paddle2Height = Math.max(10, PADDLE_HEIGHT - shrinkAmount);
    }
};

PongGame.prototype.createParticleEffect = function(x, y, count, color) {
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
};

PongGame.prototype.createImpactEffect = function(x, y, count = 10) {
    for (let i = 0; i < count; i++) {
        const angle = (Math.PI * 2 * i) / count;
        const speed = 2 + Math.random() * 2;
        this.particles.push({
            x: x,
            y: y,
            velX: Math.cos(angle) * speed,
            velY: Math.sin(angle) * speed,
            life: 20,
            size: 3,
            color: 'rgba(255, 255, 255, 0.8)'
        });
    }
};

// Update cooldowns each frame
PongGame.prototype.updateAbilityCooldowns = function() {
    this.abilityCooldowns.forEach((value, key) => {
        if (value > 0) {
            this.abilityCooldowns.set(key, value - 1);
        }
    });
};
