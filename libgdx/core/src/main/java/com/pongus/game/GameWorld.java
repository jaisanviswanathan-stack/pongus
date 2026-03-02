package com.pongus.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.pongus.game.entity.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Shared mutable game state for Pongus.
 * All fields are public — abilities and systems read/write directly.
 * No getters/setters overhead.
 */
public class GameWorld {

    // === VIRTUAL CANVAS SIZE (matches original Swing game) ===
    public static final int VIRTUAL_WIDTH = 600;
    public static final int VIRTUAL_HEIGHT = 400;

    // === RENDERING INFRASTRUCTURE ===
    public OrthographicCamera camera;
    public FitViewport viewport;
    public ShapeRenderer sr;
    public SpriteBatch batch;
    public BitmapFont font;
    public GlyphLayout glyphLayout;

    // === DIALOG SYSTEM ===
    public DialogSystem dialog;

    // === PROGRESSION SYSTEM ===
    public PlayerProfile profile;
    public String arenaModifier = "";
    public float arenaGravityScale = 1.0f;
    public boolean isOnlineMatch = false;
    public boolean isHost = false;
    public String activeSynergyKey = "";

    // Reusable temp objects (avoid GC in render loop)
    public final Vector2 tmpVec = new Vector2();
    public final Vector3 tmpVec3 = new Vector3();
    public final Color tmpColor = new Color();
    public final Rectangle tmpBallRect = new Rectangle();
    public final Rectangle tmpPaddle1Rect = new Rectangle();
    public final Rectangle tmpPaddle2Rect = new Rectangle();

    // Elapsed time accumulator for animations (replaces System.currentTimeMillis())
    public float elapsedTime = 0f;

    // === GAME STATE ===
    // Ball
    public int ballX = 250, ballY = 150;
    public int ballVelX = 2, ballVelY = 2;
    public double ballXR = 0, ballYR = 0; // sub-pixel remainder for smooth movement
    public double ballSpeed = 3.0;        // increases by 0.3 on each paddle hit
    public double ballSpeedMultiplier = 1.0;

    // Paddles
    public int paddle1Y = 100, paddle2Y = 100;
    public int paddleHeight = 60; // base paddle height
    public double pad1R = 0, pad2R = 0; // sub-pixel remainder for paddles

    // Scores
    public int scorePlayer1 = 0, scorePlayer2 = 0;

    // Match timer (Phase 2)
    public static final int MATCH_DURATION_TICKS = 12000; // 2 minutes at 10ms/tick
    public int matchTimer = 0;
    public boolean matchTimerActive = false;
    public boolean overtimeActive = false;
    public boolean suddenDeathMode = false;
    // Second ball for sudden death
    public int ball2X = 300, ball2Y = 180;
    public int ball2VelX = -2, ball2VelY = 2;
    public double ball2Speed = 3.0;
    public double ball2XR = 0, ball2YR = 0;
    public boolean ball2Active = false;

    // Match result animation state (Phase 4)
    public int matchResultPhase = 0;   // 0=hidden, 1=trophies animating, 2=chest reveal, 3=arena unlock, 4=done
    public float matchResultTimer = 0f;
    public int matchResultTrophyDisplay = 0;  // animated value
    public int matchResultTrophyTarget = 0;   // final value
    public int matchResultTrophyDelta = 0;    // +28 or -8
    public boolean matchResultWon = false;
    public String matchResultChestType = "";  // chest type earned (or "" if none)
    public boolean matchResultNewArena = false;
    public String matchResultArenaName = "";

    // Input flags (set by InputProcessor, read by update)
    public boolean up1 = false, down1 = false, up2 = false, down2 = false;

    // Game mode
    public boolean singlePlayer = true;
    public int aiDifficulty = 1; // 1=Normal, 2=Hard, 3=Impossible
    public boolean isPaused = false;

    // Delta time (matches original: 1.0 = one 10ms tick)
    public double dt = 1.0;

    // AI tuning
    public double aiSpeedMultiplier = 1.0;

    // === STUB GAME STATE ===

    // Levels & Story Mode
    public int level1 = 1, level2 = 1;
    public boolean storyModeActive = false;
    public int storyModeLevel = 1;
    public String[] storyBossNames = {"Rookie", "Veteran", "Champion", "Legend", "Boss"};
    public int[] storyLevelScoreToWin = {5, 7, 10, 12, 15};

    // Learning AI
    public boolean learningAIEnabled = false;
    public double learningProgress = 0;
    public double playerSkillLevel = 0.5;
    public int totalRallies = 0;
    public double aiAdaptationMultiplier = 1.0;
    public int learningDataPoints = 0;
    public int maxLearningData = 100;
    public double learnedReactionSpeed = 1.0;
    public double learnedAggressiveness = 0.5;

    // Map modifiers
    public boolean centerWallActive = false;
    public int centerWallGapY = 200, centerWallGapSize = 80;
    public boolean dangerZoneActive = false;
    public int dangerZoneCenterX = 300, dangerZoneCenterY = 200, dangerZoneRadius = 80;
    public int dangerZoneTime = 0, maxDangerTime = 300;
    public boolean gravityActive = false;
    public boolean invisibleWallsActive = false;
    public int invisibleWallY = 200;
    public boolean shrinkPaddlesActive = false;
    public boolean mirrorActive = false;
    public int mirrorDuration = 0, maxMirrorDuration = 500;

    // Ball effects
    public boolean fireballActive = false;
    public boolean teleportActive = false;
    public boolean zigzagActive = false;
    public boolean splitActive = false;
    public boolean multiballActive = false;
    public int multiballDuration = 0, maxMultiballDuration = 300;
    public boolean poltergeistActive = false;
    public boolean wraithActive = false;
    public int player1GhostEffectTimer = 0, player2GhostEffectTimer = 0;
    public int player1SpectralActive = 0, player2SpectralActive = 0;

    // Portals
    public Integer player1PortalEntranceX = null, player1PortalEntranceY = null;
    public Integer player1PortalExitX = null, player1PortalExitY = null;
    public int player1PortalPlacementStage = 0;
    public Integer player2PortalEntranceX = null, player2PortalEntranceY = null;
    public Integer player2PortalExitX = null, player2PortalExitY = null;
    public int player2PortalPlacementStage = 0;
    public int portalBoostTimer = 0;

    // Magnets
    public boolean player1MagnetActive = false, player2MagnetActive = false;
    public int player1MagnetTimer = 0, player2MagnetTimer = 0, magnetCooldown = 300;
    public int player1MagnetEffectTimer = 0, player2MagnetEffectTimer = 0, magnetEffectDuration = 60;

    // Barriers
    public boolean player1BarrierActive = false, player2BarrierActive = false;
    public int player1BarrierX = 50, player1BarrierY = 150;
    public int player2BarrierX = 540, player2BarrierY = 150;
    public int barrierWidth = 10, barrierHeight = 60;
    public int player1BarrierWidth = 10, player2BarrierWidth = 10;

    // Traps
    public boolean player1TrapActive = false, player2TrapActive = false;
    public int player1TrapX = 200, player1TrapY = 200;
    public int player2TrapX = 400, player2TrapY = 200;
    public int trapSize = 30;

    // Haki
    public boolean player1ArmamentActive = false, player2ArmamentActive = false;
    public boolean player1ObservationActive = false, player2ObservationActive = false;
    public boolean player1HakiPhaseActive = false, player2HakiPhaseActive = false;
    public int player1HakiDuration = 0, player2HakiDuration = 0;
    public int player1HakiSpeedBoost = 0, player2HakiSpeedBoost = 0;

    // Bankai
    public boolean player1BankaiActive = false, player2BankaiActive = false;
    public boolean player1ZangetsuActive = false, player2ZangetsuActive = false;
    public boolean player1HollowActive = false, player2HollowActive = false;
    public boolean player1HollowPhased = false, player2HollowPhased = false;
    public int player1BankaiStacks = 0, player2BankaiStacks = 0;
    public boolean player1BankaiArmed = false, player2BankaiArmed = false;
    public int player1BankaiAuraDuration = 0, player2BankaiAuraDuration = 0;

    // Hammer
    public boolean player1HammerActive = false, player2HammerActive = false;
    public int player1HammerDuration = 0, player2HammerDuration = 0;

    // Blind effect
    public int player1BlindEffectTimer = 0, player2BlindEffectTimer = 0;
    // Blind level (how many times drawn this match, capped at 3) — read by renderer
    public int player1BlindLevel = 0, player2BlindLevel = 0;

    // Shadow Clones
    public ArrayList<Integer> player1ShadowPositions = new ArrayList<Integer>();
    public ArrayList<Integer> player2ShadowPositions = new ArrayList<Integer>();
    public int player1IndependentShadowY = 100, player2IndependentShadowY = 100;

    // Tunnel Vision
    public int player1TunnelTimer = 0, player2TunnelTimer = 0;

    // Sword Swing
    public int player1SwordSwingTimer = 0, player2SwordSwingTimer = 0;
    public int swordSwingDuration = 15;

    // Stun & Lag & Reverse
    public int player1StunTimer = 0, player2StunTimer = 0;
    public int player1StunImmunityTimer = 0, player2StunImmunityTimer = 0;
    public int player1LagEffectTimer = 0, player2LagEffectTimer = 0;
    public int player1ReverseEffectTimer = 0, player2ReverseEffectTimer = 0;

    // Dash, Flash, Freeze
    public int player1DashActive = 0, player2DashActive = 0;
    public int player1FlashActive = 0, player2FlashActive = 0;
    public int player1FreezeActive = 0, player2FreezeActive = 0;
    public int player2FreezeActive_p1 = 0; // freeze from player 1 on player 2

    // Shrink effect
    public int player1ShrinkEffectTimer = 0, player2ShrinkEffectTimer = 0;

    // Inversion & Warp
    public int player1InversionTimer = 0, player2InversionTimer = 0;
    public int player1WarpEffectTimer = 0, player2WarpEffectTimer = 0;
    // Screen warp level (how many times drawn this match) — used for cooldown scaling
    public int player1ScreenWarpLevel = 0, player2ScreenWarpLevel = 0;

    // Time loop slow mo
    public int player1TimeLoopSlowMoTimer = 0, player2TimeLoopSlowMoTimer = 0;

    // Jumpscare
    public boolean jumpscareActive = false;
    public int jumpscareDuration = 0, maxJumpscareDuration = 50;
    public String jumpscareImage = "";

    // Cheat mode
    public boolean cheatModeEnabled = false;

    // Abilities (LinkedHashMap preserves insertion order for display)
    public LinkedHashMap<String, Integer> player1Abilities = new LinkedHashMap<String, Integer>();
    public LinkedHashMap<String, Integer> player2Abilities = new LinkedHashMap<String, Integer>();
    public LinkedHashMap<String, Integer> player1AbilityBranches = new LinkedHashMap<String, Integer>();
    public LinkedHashMap<String, Integer> player2AbilityBranches = new LinkedHashMap<String, Integer>();

    // In-match drawn cards tracking (Phase 3)
    // player1Abilities / player2Abilities already exist and track in-match levels.
    // These lists track draw order for display.
    public ArrayList<String> player1DrawnCards = new ArrayList<String>();
    public ArrayList<String> player2DrawnCards = new ArrayList<String>();

    // Card draw thresholds: draw at cumulative score 2, 5, 9, 14, 20... (each costs 1 more than last)
    public static final int[] DRAW_THRESHOLDS = {2, 5, 9, 14, 20, 27, 35};
    public int player1DrawIndex = 0;  // which threshold is next for player 1
    public int player2DrawIndex = 0;

    // Per-player toast notifications
    public String toastP1Text = "";
    public float toastP1Timer = 0f;   // counts down in seconds from 2.0
    public boolean toastP1LevelUp = false;
    public String toastP2Text = "";
    public float toastP2Timer = 0f;
    public boolean toastP2LevelUp = false;

    // AI fake username for current match
    public String player2DisplayName = "Opponent";

    // Pre-match countdown
    public boolean showingCountdown = false;
    public float countdownTimer = 3.0f;

    // Slide deck (learn-to-play, opened via ? button)
    public boolean showingSlides = false;
    public int slideIndex = 0;

    // Practice mode (Phase 10)
    public boolean showingPractice = false;
    public int practiceSelectedDifficulty = 1; // 0=Easy, 1=Normal, 2=Hard, 3=Impossible
    public boolean practiceMode = false;

    // Touch control targets (Phase 11)
    public float touchTargetY1 = -1f;  // -1 = no touch active for P1
    public float touchTargetY2 = -1f;  // -1 = no touch active for P2

    // Ability cooldown timers
    public int player1GunTimer = 0, player2GunTimer = 0, gunCooldown = 400;
    public int player1LagTriggerTimer = 0, player2LagTriggerTimer = 0;
    public int lagSpikeCooldown = 700; // base: 700 ticks, -70 per level, min 280
    public int player1ReverseTimer = 0, player2ReverseTimer = 0, reverseCooldown = 200;
    public int player1ChaosTimer = 0, player2ChaosTimer = 0, chaosCooldown = 300;
    public int player1PuppetTimer = 0, player2PuppetTimer = 0, puppetCooldown = 300;
    public int player1FreezeTimer = 0, player2FreezeTimer = 0, freezeCooldown = 250;
    public int player1BlindTimer = 0, player2BlindTimer = 0, blindCooldown = 200;
    public int player1ShrinkTimer = 0, player2ShrinkTimer = 0, shrinkCooldown = 1000;
    public int player1GhostTimer = 0, player2GhostTimer = 0, ghostCooldown = 800; // period between invisible phases
    public int player1RealityTimer = 0, player2RealityTimer = 0, realityCooldown = 300;
    public int player1DashTimer = 0, player2DashTimer = 0;
    public int player1FlashTimer = 0, player2FlashTimer = 0, flashCooldown = 250;
    public int player1HakiTimer = 0, player2HakiTimer = 0;
    public int hakiBaseCooldown = 800, hakiArmamentCooldown = 720, hakiObservationCooldown = 720;
    public int player1BarrierTimer = 0, player2BarrierTimer = 0, barrierCooldown = 250;
    public int player1TrapTimer = 0, player2TrapTimer = 0, trapCooldown = 500;
    public int player1WarpTimer = 0, player2WarpTimer = 0, warpCooldown = 600;
    public int player1BankaiTimer = 0, player2BankaiTimer = 0, bankaiCooldown = 800;
    public int player1HammerTimer = 0, player2HammerTimer = 0, hammerCooldown = 400;
    public int player1PortalTimer = 0, player2PortalTimer = 0;
    public int player1TimeLoopTimer = 0, player2TimeLoopTimer = 0, timeLoopCooldown = 1000;

    // Additional timers and state
    public int noScoreTimer = 0, noScoreThreshold = 12000;
    public int ballNotHitTimer = 0;
    public int initialBallVelX = 2, initialBallVelY = 2;
    public double initialBallSpeed = 3.0;
    public int dti = 1; // integer delta for timer decrements

    // Power-up timers
    public int powerUpTimer = 0, powerUpSpawnInterval = 500;
    public int zigzagSpawnTimer = 0;

    // Effect durations & timers
    public int teleportDuration = 0, maxTeleportDuration = 200;
    public int fireballDuration = 0, maxFireballDuration = 300;
    public int zigzagDuration = 0, maxZigzagDuration = 400, zigzagTimer = 0, zigzagInterval = 30;
    public int splitDuration = 0, maxSplitDuration = 300;
    public int centerWallDuration = 0;
    public int invisibleWallsDuration = 0;
    public int shrinkPaddlesDuration = 0;
    public int dangerZoneDuration = 0;
    public int jumpscareTimer = 0;

    // Ball freeze (from sticky trap)
    public boolean ballFrozenByTrap = false;
    public int ballFreezeTimer = 0, frozenBallX = 0, frozenBallY = 0;

    // Sticky timer
    public int player1StickyTimer = 0, player2StickyTimer = 0, stickyDuration = 200;

    // Portal boost/trail
    public String lastPortalUser = "";
    public int portalTrailTimer = 0;
    public boolean ballTeleportCooldown = false;
    public int player1PortalDuration = 0, player2PortalDuration = 0;
    public int player1PortalMoveTimer = 0, player2PortalMoveTimer = 0;

    // Magnet
    public boolean player1MagnetRepelMode = false, player2MagnetRepelMode = false;

    // Gravity well
    public boolean player1GravityWellActive = false, player2GravityWellActive = false;
    public int player1GravityWellDuration = 0, player2GravityWellDuration = 0;
    public int player1GravityWellX = 100, player1GravityWellY = 200;
    public int player2GravityWellX = 500, player2GravityWellY = 200;

    // Barrier timers
    public int player1BarrierDuration = 0, player2BarrierDuration = 0;
    public int barrierBaseDuration = 300;
    public boolean player1BallAbsorbed = false, player2BallAbsorbed = false;

    // Trap timers
    public int player1TrapDuration = 0, player2TrapDuration = 0;
    public int trapBaseDuration = 500;

    // Haki timers
    public int hakiStunDuration = 80;
    public int player1ArmamentDuration = 0, player2ArmamentDuration = 0;
    public int armamentBaseDuration = 200;
    public int player1ObservationDuration = 0, player2ObservationDuration = 0;
    public int observationBaseDuration = 200;
    public boolean player1ObsAutoBlockUsed = false, player2ObsAutoBlockUsed = false;
    public double player1ObsSlowFactor = 1.0, player2ObsSlowFactor = 1.0;

    // Bankai timers
    public int player1BankaiDuration = 0, player2BankaiDuration = 0;
    public int bankaiBaseDuration = 800;
    public int player1SwordSwingCooldown = 0, player2SwordSwingCooldown = 0;
    public int swordSwingCooldownTime = 30;
    public int player1TotalSwordSwings = 0, player2TotalSwordSwings = 0;
    public int player1PermanentSpeedBonus = 0, player2PermanentSpeedBonus = 0;
    public int player1PermanentPaddleBonus = 0, player2PermanentPaddleBonus = 0;
    public double player1BallSpeedMultiplier = 1.0, player2BallSpeedMultiplier = 1.0;
    public boolean player1HasBankaiImmunity = false, player2HasBankaiImmunity = false;

    // Hammer timers
    public int hammerDuration = 50;

    // Warp timers
    public int warpBaseDuration = 200;

    // Reverse/Chaos/Puppet effect durations
    public int reverseEffectDuration = 200, chaosEffectDuration = 400, puppetEffectDuration = 200;
    public int player1ChaosEffectTimer = 0, player2ChaosEffectTimer = 0;
    public int player1PuppetEffectTimer = 0, player2PuppetEffectTimer = 0;
    public int player1CurrentDebuff = 0, player2CurrentDebuff = 0;

    // Dash
    public int dashDuration = 20;

    // Time loop
    public int timeLoopSlowMoDuration = 100;
    public ArrayList<GameStateSnapshot> gameStateHistory = new ArrayList<GameStateSnapshot>();

    // Temporal echo
    public boolean temporalEchoActive = false;
    public int echoGhostBallX = 0, echoGhostBallY = 0, echoGhostBallVelX = 0, echoGhostBallVelY = 0;
    public int temporalEchoDuration = 0;

    // Shockwave (Thor's Hammer)
    public int player1ShockwaveTimer = 0, player2ShockwaveTimer = 0;

    // Ghost ball
    public boolean player1GhostHasPhased = false, player2GhostHasPhased = false;

    // Reality break
    public boolean realityBreakActive = false;
    public int realityBreakDuration = 0, maxRealityBreakDuration = 300;
    public int realityBreakEffect = 0;

    // Shadow collision cooldown
    public int player1ShadowCollisionCooldown = 0, player2ShadowCollisionCooldown = 0;
    public int shadowCollisionCooldownFrames = 10;
    public boolean player1ShadowMovingUp = false, player2ShadowMovingUp = false;

    // Level up
    public int totalPointsThisLevel1 = 0, totalPointsThisLevel2 = 0;
    public int pointsToNextLevel1 = 1, pointsToNextLevel2 = 1;

    // Combo & underdog
    public int player1ComboCount = 0, player2ComboCount = 0;
    public int player1LastUnderdogTrigger = 0, player2LastUnderdogTrigger = 0;

    // Story mode
    public int storyPlayerScore = 0, storyBossScore = 0;
    public int storyModeMaxLevel = 5;
    public int menuSelectedLevel = 0;
    public boolean showingMainMenu = true;
    public String player2Name = "AI";

    // Collection screen state (Phase 5)
    public boolean showingCollection = false;
    public int collectionScrollOffset = 0;     // how many cards scrolled past
    public int collectionSelectedSlot = -1;    // which deck slot is being filled (-1 = just browsing)
    public String collectionSelectedCard = ""; // currently highlighted card in collection
    public boolean collectionDeckDirty = false; // true if deck was changed, need to save

    // Arena Road screen state (Phase 6)
    public boolean showingArenaRoad = false;
    public int arenaRoadScrollOffset = 0;
    public int arenaRoadExpandedArena = -1; // -1=none, 0-7=which arena row is expanded

    // Main menu state
    public int menuAnimationTimer = 0;
    public int menuHoveredButton = -1;

    // Input keybinds
    public int player1UpKey = Input.Keys.W, player1DownKey = Input.Keys.S;
    public int player2UpKey = Input.Keys.UP, player2DownKey = Input.Keys.DOWN;
    public int player1AbilityKey = Input.Keys.Q, player2AbilityKey = Input.Keys.SLASH;

    // Learning AI data
    public ArrayList<Integer> playerPositionHistory = new ArrayList<Integer>();
    public ArrayList<Double> playerResponseTimes = new ArrayList<Double>();
    public ArrayList<Double> playerAggressiveness = new ArrayList<Double>();
    public int learningFrameCounter = 0;

    // Cheat password
    public String cheatPassword = "pongus";

    // All abilities list (double_points removed — breaks score math)
    public String[] allAbilities = {"paddle_growth", "speed_boost", "shadow_clone", "slow_opponent",
        "reverse_controls", "ghost_ball", "shrink_opponent", "lag_spike",
        "gravity_hammer", "portal_pong", "screen_warp", "time_loop",
        "haki", "barrier", "trap", "bankai", "gun", "blind",
        "magnet_ball"};

    // === AI STATE (human-like prediction model) ===
    public int aiPredictedY = 200;       // where AI thinks ball will land
    public int aiPredictionTimer = 999;  // ticks since last prediction update (start high to trigger first update)
    public boolean aiWasBallMovingToward = false; // used to detect direction change

    // === SPRITE TEXTURES (Phase 9) — null if PNG not loaded ===
    public com.badlogic.gdx.graphics.Texture texPaddle1 = null;
    public com.badlogic.gdx.graphics.Texture texPaddle2 = null;
    public com.badlogic.gdx.graphics.Texture texBall = null;
    public com.badlogic.gdx.graphics.Texture texBallFire = null;
    public com.badlogic.gdx.graphics.Texture texBallGhost = null;
    public com.badlogic.gdx.graphics.Texture[] texChests = new com.badlogic.gdx.graphics.Texture[4]; // silver,gold,magical,arena
    public com.badlogic.gdx.graphics.Texture[] texArenas = new com.badlogic.gdx.graphics.Texture[8]; // arena backgrounds (legacy path)
    public com.badlogic.gdx.graphics.Texture[] arenaBgTextures = new com.badlogic.gdx.graphics.Texture[8]; // arena backgrounds (backgrounds/ folder)
    public com.badlogic.gdx.graphics.Texture[] arenaIconTextures = new com.badlogic.gdx.graphics.Texture[8]; // arena badge icons (arenas/ folder)
    public java.util.HashMap<String, com.badlogic.gdx.graphics.Texture> cardTextureMap = new java.util.HashMap<String, com.badlogic.gdx.graphics.Texture>();
    public java.util.HashMap<String, com.badlogic.gdx.graphics.Texture> powerupTextures = new java.util.HashMap<String, com.badlogic.gdx.graphics.Texture>();

    // Projectile lists
    public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    public ArrayList<GetsugaTensho> getsugaProjectiles = new ArrayList<GetsugaTensho>();
    public ArrayList<VegetaBullet> vegetaBullets = new ArrayList<VegetaBullet>();
    public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
    public ArrayList<Laser> activeLasers = new ArrayList<Laser>();
    public ArrayList<FreezaLaser> freezaLasers = new ArrayList<FreezaLaser>();
    public ArrayList<JirenBullet> jirenBullets = new ArrayList<JirenBullet>();
    public ArrayList<GhostBall> wraithBalls = new ArrayList<GhostBall>();
    public ArrayList<PowerUp> powerUps = new ArrayList<PowerUp>();

    // === HELPER METHODS ===

    public int getEffectiveAbilityLevel(int player, String ability) {
        LinkedHashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
        return abilities.containsKey(ability) ? abilities.get(ability) : 0;
    }

    public int getPaddleHeight(int player, boolean shrinkActive) {
        int base = paddleHeight;
        LinkedHashMap<String, Integer> ownAbilities = (player == 1) ? player1Abilities : player2Abilities;
        LinkedHashMap<String, Integer> oppAbilities = (player == 1) ? player2Abilities : player1Abilities;
        // Paddle growth: +8px per level, cap at level 5 (+40px)
        int growthLevel = ownAbilities.containsKey("paddle_growth") ? ownAbilities.get("paddle_growth") : 0;
        if (growthLevel > 0) {
            int cappedGrowth = Math.min(growthLevel, 5);
            base += cappedGrowth * 8;
        }
        // Permanent bankai paddle bonus
        int permBonus = (player == 1) ? player1PermanentPaddleBonus : player2PermanentPaddleBonus;
        base += permBonus;
        // Shrink effect (from shrink_opponent ability trigger)
        int shrinkTimer = (player == 1) ? player1ShrinkEffectTimer : player2ShrinkEffectTimer;
        if (shrinkTimer > 0) base = Math.max(22, base * 2 / 5);
        // Map shrink paddles
        if (shrinkActive) base = Math.max(20, base * 2 / 3);
        return Math.max(20, base);
    }

    /** Shadow clone paddle height — scales with ability level. lv1=20, lv2=30, lv3=40, lv4=50 */
    public int getShadowSize(int player) {
        int level = getEffectiveAbilityLevel(player, "shadow_clone");
        return level > 0 ? 10 + level * 10 : 0;
    }

    public int getPlayerSpeed(int player) {
        int base = 5;
        LinkedHashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
        // Speed boost: +1 px per level, no cap.
        int speedLevel = abilities.containsKey("speed_boost") ? abilities.get("speed_boost") : 0;
        if (speedLevel > 0) {
            base += speedLevel;
        }
        // slow_opponent: opponent's speed is reduced 15% per level, capped at 50%
        // Applied in PongusGame movement update, not here
        // Permanent bankai speed bonus
        int permSpeed = (player == 1) ? player1PermanentSpeedBonus : player2PermanentSpeedBonus;
        base += permSpeed;
        // Haki speed boost
        if (player == 1 && player1HakiSpeedBoost > 0) base += player1HakiSpeedBoost;
        if (player == 2 && player2HakiSpeedBoost > 0) base += player2HakiSpeedBoost;
        return base;
    }

    /** Returns the cooldown tick multiplier for a player. Always 1.0 (CDR removed). */
    public double getCooldownTick(int player) {
        return 1.0;
    }
}
