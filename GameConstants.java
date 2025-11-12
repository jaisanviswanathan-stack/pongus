/**
 * Game constants and configuration values
 * Centralizes all magic numbers for easy tuning and maintenance
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class GameConstants {
    // ==================== SCREEN & DISPLAY ====================
    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 400;
    public static final int BOTTOM_BOUNDARY = 393;
    public static final int TOP_BOUNDARY = 0;
    public static final int LEFT_BOUNDARY = 0;
    public static final int RIGHT_BOUNDARY = 600;

    // ==================== TIMING ====================
    public static final int FPS = 100;
    public static final int FRAME_DELAY_MS = 10;

    /**
     * Convert seconds to frame count
     * @param seconds Time in seconds
     * @return Number of frames at 100 FPS
     */
    public static int toFrames(double seconds) {
        return (int)(seconds * FPS);
    }

    // ==================== PADDLE ====================
    public static final int PADDLE_1_X = 20;
    public static final int PADDLE_2_X = 580;
    public static final int PADDLE_BASE_HEIGHT = 80;
    public static final int PADDLE_BASE_WIDTH = 10;
    public static final int PADDLE_BASE_SPEED = 7;
    public static final int PADDLE_SPEED_BOOST_MULTIPLIER = 2; // Speed boost makes paddle 2x faster

    // Paddle growth
    public static final int PADDLE_GROWTH_PER_LEVEL = 15;
    public static final int PADDLE_SHRINK_AMOUNT = 30;

    // ==================== BALL ====================
    public static final int BALL_RADIUS = 7;
    public static final int BALL_DIAMETER = BALL_RADIUS * 2;
    public static final int BALL_BASE_SPEED = 3;
    public static final int BALL_INITIAL_X = 250;
    public static final int BALL_INITIAL_Y = 150;

    // Ball speed modifiers
    public static final double FIREBALL_SPEED_MULTIPLIER = 2.0;
    public static final double TELEPORT_SPEED_MULTIPLIER = 0.5;

    // ==================== PHYSICS ====================
    public static final double LEARNING_RATE = 0.005;
    public static final double LEARNING_RATE_DECAY = 0.995;
    public static final double AI_REACTION_SMOOTHING = 0.6;
    public static final double AI_AGGRESSION_SMOOTHING = 0.4;

    // Speed multipliers for different AI difficulties
    public static final double NORMAL_AI_SPEED = 1.0;
    public static final double HARD_AI_SPEED = 1.0;
    public static final double IMPOSSIBLE_AI_SPEED = 1.5;
    public static final double IMPOSSIBLE_AI_PREDICTION_MULTIPLIER = 1.5;

    // ==================== SCORING ====================
    public static final int XP_PER_POINT = 10;
    public static final int POINTS_PER_HIT = 1;
    public static final int DOUBLE_POINTS_MULTIPLIER = 2;
    public static final int COMBO_MASTER_MULTIPLIER = 3;

    // Underdog comeback system
    public static final int UNDERDOG_SCORE_DIFFERENCE = 3;
    public static final double UNDERDOG_SPEED_BONUS = 0.3;
    public static final int MAX_UNDERDOG_SPEED_BONUS = 2; // Cap at 2.0x

    // ==================== ABILITIES - COOLDOWNS ====================
    public static final int GUN_COOLDOWN = toFrames(3.0);           // 3 seconds
    public static final int GOKU_COOLDOWN = toFrames(6.0);          // 6 seconds (laser)
    public static final int VEGETA_COOLDOWN = toFrames(4.0);        // 4 seconds (barrage)

    public static final int STEALER_COOLDOWN = toFrames(5.0);       // 5 seconds
    public static final int FRIEZA_COOLDOWN = toFrames(8.0);        // 8 seconds (laser)
    public static final int JIREN_COOLDOWN = toFrames(6.0);         // 6 seconds (explosion)

    public static final int LAG_SPIKE_COOLDOWN = toFrames(8.0);     // 8 seconds
    public static final int REVERSE_CONTROLS_COOLDOWN = toFrames(6.0); // 6 seconds
    public static final int JOSHUA_COOLDOWN = toFrames(10.0);       // 10 seconds
    public static final int BLIND_COOLDOWN = toFrames(8.0);         // 8 seconds
    public static final int SHRINK_OPPONENT_COOLDOWN = toFrames(8.0); // 8 seconds
    public static final int GHOST_BALL_COOLDOWN = toFrames(6.0);    // 6 seconds
    public static final int JAISAN_COOLDOWN = toFrames(15.0);       // 15 seconds
    public static final int GRAVITY_HAMMER_COOLDOWN = toFrames(5.0); // 5 seconds
    public static final int MAGNET_BALL_COOLDOWN = toFrames(8.0);   // 8 seconds
    public static final int SHADOW_CLONE_COOLDOWN = toFrames(12.0); // 12 seconds
    public static final int PORTAL_COOLDOWN = toFrames(10.0);       // 10 seconds
    public static final int POWER_SIPHON_COOLDOWN = toFrames(10.0); // 10 seconds
    public static final int TIME_LOOP_COOLDOWN = toFrames(20.0);    // 20 seconds

    // ==================== ABILITIES - DURATIONS ====================
    public static final int SPEED_BOOST_DURATION = toFrames(6.0);   // 6 seconds
    public static final int PADDLE_GROWTH_DURATION = toFrames(8.0); // 8 seconds
    public static final int SLOW_OPPONENT_DURATION = toFrames(3.0); // 3 seconds
    public static final int LAG_SPIKE_DURATION = toFrames(2.0);     // 2 seconds
    public static final int REVERSE_CONTROLS_DURATION = toFrames(5.0); // 5 seconds
    public static final int JOSHUA_DURATION = toFrames(5.0);        // 5 seconds (god mode)
    public static final int BLIND_BASE_DURATION = toFrames(3.0);    // 3 seconds base
    public static final int BLIND_LEVEL_BONUS = toFrames(0.3);      // +0.3s per level
    public static final int SHRINK_BASE_DURATION = toFrames(1.5);   // 1.5 seconds base
    public static final int SHRINK_LEVEL_BONUS = toFrames(0.3);     // +0.3s per level
    public static final int GHOST_BASE_DURATION = toFrames(2.5);    // 2.5 seconds base
    public static final int GHOST_LEVEL_BONUS = toFrames(0.2);      // +0.2s per level
    public static final int JAISAN_DURATION = toFrames(10.0);       // 10 seconds (ultimate god mode)
    public static final int MAGNET_DURATION = toFrames(4.0);        // 4 seconds
    public static final int SHADOW_CLONE_DURATION = toFrames(8.0);  // 8 seconds
    public static final int POWER_SIPHON_DURATION = toFrames(6.0);  // 6 seconds
    public static final int TIME_LOOP_DURATION = toFrames(5.0);     // 5 seconds (rewind window)

    // Effect durations
    public static final int STUN_DURATION = toFrames(1.0);          // 1 second
    public static final int EXPLOSION_DURATION = toFrames(0.5);     // 0.5 seconds

    // ==================== MAP MODIFIERS ====================
    public static final int DANGER_ZONE_RADIUS = 80;
    public static final int DANGER_ZONE_MAX_TIME = 100;
    public static final int DANGER_ZONE_DURATION = toFrames(10.0);  // 10 seconds
    public static final double DANGER_ZONE_SPEED_MULTIPLIER = 3.0;

    public static final int INVISIBLE_WALL_DURATION = toFrames(8.0); // 8 seconds
    public static final int CENTER_WALL_DURATION = toFrames(8.0);    // 8 seconds
    public static final int CENTER_WALL_GAP_SIZE = 80;
    public static final int SHRINK_PADDLES_DURATION = toFrames(8.0); // 8 seconds

    public static final int MIRROR_DURATION = toFrames(10.0);        // 10 seconds
    public static final int TELEPORT_DURATION = toFrames(3.0);       // 3 seconds
    public static final int FIREBALL_DURATION = toFrames(5.0);       // 5 seconds
    public static final int ZIGZAG_DURATION = toFrames(4.0);         // 4 seconds
    public static final int ZIGZAG_INTERVAL = toFrames(0.3);         // Change direction every 0.3s
    public static final int SPLIT_DURATION = toFrames(3.0);          // 3 seconds

    // ==================== SPECIAL EFFECTS ====================
    public static final int JUMPSCARE_INTERVAL = toFrames(5.0);      // 5 seconds between jumpscares
    public static final int JUMPSCARE_DURATION = toFrames(1.0);      // 1 second display
    public static final int MULTIBALL_DURATION = toFrames(5.0);      // 5 seconds
    public static final int SHREK_BALL_DURATION = toFrames(10.0);    // 10 seconds

    // ==================== PROJECTILES ====================
    public static final int BULLET_SPEED = 10;
    public static final int BULLET_SIZE = 5;
    public static final int LASER_WIDTH = 8;
    public static final int LASER_DURATION = toFrames(0.5);          // 0.5 seconds
    public static final int EXPLOSION_RADIUS = 60;

    // Vegeta bullets
    public static final int VEGETA_BULLET_COUNT = 5;
    public static final int VEGETA_BULLET_SPREAD = 15;

    // Jiren bullets
    public static final int JIREN_BULLET_SPEED = 7;

    // ==================== POWER-UPS ====================
    public static final int POWERUP_SIZE = 20;
    public static final int POWERUP_SPAWN_INTERVAL = toFrames(5.0);  // Spawn every 5 seconds
    public static final int POWERUP_COLLECTION_RADIUS = 20;

    // ==================== LEARNING AI ====================
    public static final int MAX_LEARNING_DATA_POINTS = 1000;
    public static final double LEARNING_AI_INITIAL_SPEED = 0.3;
    public static final double LEARNING_AI_INITIAL_AGGRESSION = 0.5;
    public static final double LEARNING_AI_INITIAL_POSITION = 200.0;
    public static final double LEARNING_AI_MAX_PROGRESS = 1.0;
    public static final double LEARNING_AI_PROGRESS_RATE = 0.0001; // Very slow progression

    // Chess coach style - skill matching
    public static final double PLAYER_SKILL_INITIAL = 0.5;
    public static final double PLAYER_SKILL_MIN = 0.0;
    public static final double PLAYER_SKILL_MAX = 1.0;
    public static final double SKILL_ADJUSTMENT_RATE = 0.01;

    // ==================== ABILITY LEVELS ====================
    public static final int MAX_ABILITY_LEVEL = 5;
    public static final int BRANCH_CHOICE_LEVEL = 3; // Level at which players choose a branch

    // ==================== COLLISION & PROXIMITY ====================
    public static final int PADDLE_BALL_INTERACTION_RANGE = 60;
    public static final int PORTAL_RADIUS = 30;
    public static final int SHADOW_CLONE_OFFSET = 100;

    // ==================== UI ====================
    public static final int LEGEND_X = 10;
    public static final int LEGEND_Y_START = 300;
    public static final int LEGEND_LINE_HEIGHT = 12;

    public static final int SCORE_Y = 30;
    public static final int PLAYER1_SCORE_X = 50;
    public static final int PLAYER2_SCORE_X = 530;

    public static final int ABILITY_DISPLAY_Y = 50;
    public static final int ABILITY_DISPLAY_SPACING = 15;

    // ==================== CHEAT MODE ====================
    public static final String CHEAT_PASSWORD = "zanyscarf16";

    // ==================== ABILITY NAMES ====================
    public static final String[] ALL_ABILITIES = {
        "speed_boost", "paddle_growth", "double_points", "slow_opponent",
        "gun", "ability_stealer", "lag_spike", "reverse_controls",
        "joshua", "blind", "shrink_opponent", "ghost_ball",
        "jaisan", "ability_swap", "gravity_hammer", "magnet_ball",
        "shadow_clone", "portal_pong", "power_siphon", "time_loop"
    };

    // ==================== ABILITY RARITY ====================
    public static final int COMMON_WEIGHT = 10;
    public static final int UNCOMMON_WEIGHT = 5;
    public static final int RARE_WEIGHT = 2;
    public static final int LEGENDARY_WEIGHT = 1;

    // ==================== POWER-UP SPAWN WEIGHTS ====================
    public static final double ZIGZAG_SPAWN_CHANCE = 0.01;
    public static final double FIREBALL_SPAWN_CHANCE = 0.10;
    public static final double SPLIT_SPAWN_CHANCE = 0.10;
    public static final double TELEPORT_SPAWN_CHANCE = 0.10;
    public static final double MIRROR_SPAWN_CHANCE = 0.02;
    public static final double DANGER_ZONE_SPAWN_CHANCE = 0.05;
    public static final double INVISIBLE_WALLS_SPAWN_CHANCE = 0.05;
    public static final double SHRINK_PADDLES_SPAWN_CHANCE = 0.05;
    public static final double CENTER_WALL_SPAWN_CHANCE = 0.05;
}
