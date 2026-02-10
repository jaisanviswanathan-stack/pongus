/**
 * a ping pong game
 *
 * @author Jaisan Viswanathan
 * @version 156
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PingPongGame extends JPanel implements KeyListener, ActionListener, MouseListener {
 // MAIN MENU STATE
 boolean showingMainMenu = true;
 int menuSelectedLevel = -1;
 int menuAnimationTimer = 0;
 int[] levelNodeX = new int[17];
 int[] levelNodeY = new int[17];
 Rectangle[] menuButtons = new Rectangle[]{new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()};
 JFrame gameFrame;

 // ONLINE MULTIPLAYER
 boolean isOnlineGame = false;
 boolean isHost = false;
 Object networkClient = null; // TODO: NetworkClient class
 String serverUrl = "http://localhost:3000"; // Default local server
 int remotePaddleY = 100; // Opponent's paddle position
 int networkSendCounter = 0;
 int networkSendInterval = 3; // Send every 3 frames

 // Player name strings (Player class not yet in compile path)
 String player1Name = "Player 1";
 String player2Name = "Player 2";

 int ballX = 250, ballY = 150, ballVelX = 2, ballVelY = 2;
 int initialBallVelX = 2, initialBallVelY = 2; // Store initial velocity for power-up restoration
 int ballNotHitTimer = 0; // Timer since ball was last hit by a player
 int ballResetThreshold = 1000; // 10 seconds at 100fps - reset ball if not hit
 // Legacy paddle variables - kept for compatibility during migration
 int paddle1Y = 100, paddle2Y = 100;
 boolean up1 = false, down1 = false, up2 = false, down2 = false;
 public boolean singlePlayer = true;
 public int aiDifficulty = 1; // 1 = Normal, 2 = Hard, 3 = Impossible
 javax.swing.Timer timer;
 boolean isPaused = true;
 double player2SpeedBonus = 1.0;
 
 // Cheat mode
 String cheatPassword = "zanyscarf16";
 boolean cheatModeEnabled = false;

 // STORY MODE - Progressive ability unlocking with auto-save!
 boolean storyModeActive = false;
 int storyModeLevel = 1; // Current level (1-17)
 int storyModeMaxLevel = 17;
 ArrayList<String> unlockedAbilities = new ArrayList<>(); // Abilities unlocked so far
 String storySaveFile = "pongus_story_save.txt";

 // Story Mode Level Structure - defeat bosses, earn ALL their abilities as reward!
 String[] storyAbilityPool = {
 "speed_boost", "paddle_growth", "double_points", "slow_opponent", "gun",
 "ability_stealer", "lag_spike", "reverse_controls", "blind", "shrink_opponent",
 "ghost_ball", "ability_swap", "gravity_hammer", "magnet_ball", "shadow_clone",
 "portal_pong", "power_siphon", "time_loop", "haki", "barrier", "trap",
 "screen_warp", "bankai"
 };
 // Bosses ordered from weakest to strongest
 String[] storyBossNames = {
 "Speed Demon", "Point Hunter", "The Wall", "Slow Mo Joe",
 "Gunslinger", "Glitch Lord", "The Thief", "Mind Bender", "The Phantom",
 "Ghost", "Hammer Time", "Shadow Ninja", "Portal Master", "Time Lord",
 "Haki Master", "Bankai Lord", "ULTIMATE CHAMPION"
 };
 int[] storyLevelScoreToWin = {5, 5, 5, 7, 7, 7, 7, 10, 10, 10, 10, 12, 12, 12, 12, 15, 20};
 int storyBossScore = 0;
 int storyPlayerScore = 0;

 // Learning AI system - CHESS COACH STYLE (adjusts to player skill)
 boolean learningAIEnabled = false; // Enabled only in Learning AI mode (difficulty 4)
 ArrayList<Integer> playerPositionHistory = new ArrayList<>(); // Track player paddle positions
 ArrayList<Integer> playerResponseTimes = new ArrayList<>(); // Track how fast player responds
 ArrayList<Double> playerAggressiveness = new ArrayList<>(); // Track player's tendency to move toward ball
 int learningDataPoints = 0;
 int maxLearningData = 1000; // Keep last 1000 data points for gradual learning
 double learnedAveragePosition = 200.0; // Player's preferred center position
 double learnedReactionSpeed = 0.3; // How reactive the player is (starts very slow)
 double learnedAggressiveness = 0.5; // How much player moves toward ball (0-1)
 double learningProgress = 0.0; // 0.0 = just started, 1.0 = fully learned (much slower now)
 int learningFrameCounter = 0;
 int lastPlayerY = 100;
 int lastBallY = 150;
 boolean wasMovingUp = false;
 boolean wasMovingDown = false;
 // NEW: Track player's successful strategies
 ArrayList<Integer> successfulHitPositions = new ArrayList<>(); // Where player was when they hit successfully
 ArrayList<Double> playerTimingPatterns = new ArrayList<>(); // When player likes to strike
 int consecutivePlayerWins = 0; // Track if player is winning
 int totalRallies = 0; // Track total rallies for gradual progression
 double aiAdaptationMultiplier = 1.0; // Gets better over time (much slower progression)
 // Skill-based adjustment - AI matches player's actual skill level
 double playerSkillLevel = 0.5; // 0.0 = beginner, 1.0 = expert (measured from performance)
 int playerSuccessfulHits = 0; // Track player's successful returns
 int aiSuccessfulHits = 0; // Track AI's successful returns
 
 // CUSTOM KEYBINDS - Player configurable controls
 int player1UpKey = KeyEvent.VK_W;
 int player1DownKey = KeyEvent.VK_S;
 int player1AbilityKey = KeyEvent.VK_Q;
 int player2UpKey = KeyEvent.VK_QUOTE;
 int player2DownKey = KeyEvent.VK_SLASH;
 int player2AbilityKey = KeyEvent.VK_SEMICOLON;
 
 // Danger zone variables
 int dangerZoneCenterX = 300, dangerZoneCenterY = 200, dangerZoneRadius = 80;
 int dangerZoneTime = 0, maxDangerTime = 100;
 boolean dangerZoneActive = false;
 int dangerZoneDuration = 0, maxDangerZoneDuration = 1000;
 
 // Map modifiers
 boolean gravityActive = false, invisibleWallsActive = false, shrinkPaddlesActive = false, centerWallActive = false;
 int gravityDuration = 0, invisibleWallsDuration = 0, invisibleWallY = 200;
 int shrinkPaddlesDuration = 0, centerWallDuration = 0, centerWallGapY = 200, centerWallGapSize = 80;
 
 // New power-up effects
 boolean teleportActive = false, fireballActive = false, zigzagActive = false, splitActive = false;
 boolean mirrorActive = false; // Mirror effect - flips the entire game horizontally
 int mirrorDuration = 0, maxMirrorDuration = 1000; // 10 seconds
 int teleportDuration = 0, maxTeleportDuration = 300; // 3 seconds
 int fireballDuration = 0, maxFireballDuration = 500; // 5 seconds
 int zigzagDuration = 0, maxZigzagDuration = 400; // 4 seconds
 int splitDuration = 0, maxSplitDuration = 300; // 3 seconds
 int zigzagTimer = 0, zigzagInterval = 30; // Change direction every 0.3 seconds
 
 // Special effects
 int jumpscareTimer = 0, jumpscareInterval = 500;
 boolean jumpscareActive = false;
 int jumpscareDuration = 0, maxJumpscareDuration = 100;
 String jumpscareImage = "";
 boolean multiballActive = false;
 int multiballDuration = 0, maxMultiballDuration = 500;
 boolean shrekBallActive = false;
 int shrekBallDuration = 0, maxShrekBallDuration = 1000, shrekJumpscareTimer = 0;
 BufferedImage shrekSprite = null;
 
 // Permanent abilities
 HashMap<String, Integer> player1Abilities = new HashMap<>();
 HashMap<String, Integer> player2Abilities = new HashMap<>();
 // Ability branches - tracks which branch was chosen at level 3 (1 or 2, 0 = not chosen yet)
 HashMap<String, Integer> player1AbilityBranches = new HashMap<>();
 HashMap<String, Integer> player2AbilityBranches = new HashMap<>();
 String[] allAbilities = {"speed_boost", "paddle_growth", "double_points", "slow_opponent", "gun", "ability_stealer", "lag_spike", "reverse_controls", "joshua", "blind", "shrink_opponent", "ghost_ball", "jaisan", "ability_swap", "gravity_hammer", "magnet_ball", "shadow_clone", "portal_pong", "power_siphon", "time_loop", "haki", "barrier", "trap", "screen_warp", "bankai"};
 
 // Gun ability tracking
 int player1GunTimer = 0, player2GunTimer = 0, gunCooldown = 300; // 3 seconds
 ArrayList<Bullet> bullets = new ArrayList<>();
 ArrayList<VegetaBullet> vegetaBullets = new ArrayList<>();
 ArrayList<Explosion> explosions = new ArrayList<>();
 int player1StunTimer = 0, player2StunTimer = 0, stunDuration = 100;
 
 // Ability Stealer tracking
 int player1StealerTimer = 0, player2StealerTimer = 0, stealerCooldown = 500; // 5 seconds
 ArrayList<StealerBullet> stealerBullets = new ArrayList<>();
 ArrayList<HijackerBullet> hijackerBullets = new ArrayList<>();
 ArrayList<VirusBullet> virusBullets = new ArrayList<>();
 
 // Lag Spike tracking
 int player1LagTriggerTimer = 0, player2LagTriggerTimer = 0;
 int player1LagEffectTimer = 0, player2LagEffectTimer = 0;
 int lagTriggerDelay = 200; // 2 seconds after hitting ball
 int player1LagTeleportTimer = 0, player2LagTeleportTimer = 0;
 int player1SavedPaddleY = 0, player2SavedPaddleY = 0;
 
 // Reverse Controls tracking
 int player1ReverseTimer = 0, player2ReverseTimer = 0;
 int player1ReverseEffectTimer = 0, player2ReverseEffectTimer = 0;
 int reverseCooldown = 1000; // 10 seconds
 int reverseEffectDuration = 150; // 1.5 seconds (buffed from 1s) // 1 second
 
 // Joshua ability tracking
 int player1JoshuaTimer = 0, player2JoshuaTimer = 0;
 int joshuaCooldown = 1000; // 10 seconds
 boolean player1JoshuaActive = false, player2JoshuaActive = false;
 int player1JoshuaDuration = 0, player2JoshuaDuration = 0;
 
 // Jaisan ability tracking (even more OP than JOSHUA)
 int player1JaisanTimer = 0, player2JaisanTimer = 0;
 int jaisanCooldown = 500; // 5 seconds
 boolean player1JaisanActive = false, player2JaisanActive = false;
 int player1JaisanDuration = 0, player2JaisanDuration = 0;
 
 // Blind ability tracking
 int player1BlindTimer = 0, player2BlindTimer = 0;
 int player1BlindEffectTimer = 0, player2BlindEffectTimer = 0;
 int blindCooldown = 600; // 6 seconds
 
 // Shrink Opponent tracking
 int player1ShrinkTimer = 0, player2ShrinkTimer = 0;
 int player1ShrinkEffectTimer = 0, player2ShrinkEffectTimer = 0;
 int shrinkCooldown = 500; // 5 seconds
 
 // Ghost Ball tracking
 int player1GhostTimer = 0, player2GhostTimer = 0;
 int player1GhostEffectTimer = 0, player2GhostEffectTimer = 0;
 int ghostCooldown = 1000; // 10 seconds
 int ghostDuration = 250; // 2.5 seconds (increased from 1 second)
 boolean player1GhostHasPhased = false, player2GhostHasPhased = false; // Track if already phased this activation
 int player1StunImmunityTimer = 0, player2StunImmunityTimer = 0, stunImmunityDuration = 200; // 2 seconds
 
 // Branch ability tracking
 // Sonic (speed_boost branch 1) - dash ability
 int player1DashTimer = 0, player2DashTimer = 0, dashCooldown = 300; // 3 seconds
 int player1DashActive = 0, player2DashActive = 0, dashDuration = 30; // 0.3 seconds visual effect
 
 // Flash (speed_boost branch 2) - slow time around ball
 int player1FlashTimer = 0, player2FlashTimer = 0, flashCooldown = 500; // 5 seconds
 int player1FlashActive = 0, player2FlashActive = 0, flashDuration = 100; // 1 second
 
 // Freeze Ray (slow_opponent branch 1) - freeze opponent
 int player1FreezeTimer = 0, player2FreezeTimer = 0, freezeCooldown = 500; // 5 seconds
 int player1FreezeActive = 0, player2FreezeActive = 0, freezeDuration = 150; // 1.5 seconds
 
 // Gravity Well (slow_opponent branch 2) - opponent paddle feels heavier
 int player1GravityActive = 0, player2GravityActive = 0;
 
 // Point Leech (double_points branch 1) - steal points on score
 // Combo Master (double_points branch 2) - combo system
 int player1ComboCount = 0, player2ComboCount = 0;
 
 // Spectral Echo (ghost_ball branch 2) - creates ghost trail
 int player1SpectralActive = 0, player2SpectralActive = 0;
 
 // OLD BRANCH VARIABLES (kept for compatibility with existing code)
 // Wraith/Poltergeist (old ghost_ball branches - replaced but code still references them)
 boolean wraithActive = false;
 int wraithDuration = 0, maxWraithDuration = 300;
 int player1WraithTimer = 0, player2WraithTimer = 0, wraithCooldown = 800;
 ArrayList<Ball> wraithBalls = new ArrayList<>();
 boolean poltergeistActive = false;
 int poltergeistDuration = 0, maxPoltergeistDuration = 400, poltergeistMovementTimer = 0;
 int player1PoltergeistTimer = 0, player2PoltergeistTimer = 0, poltergeistCooldown = 1000;
 
 // Nullifier/Mimic (old ability_stealer branches - replaced but code still references them)
 int player1NullifierTimer = 0, player2NullifierTimer = 0, nullifierCooldown = 400;
 int player1NullifierActive = 0, player2NullifierActive = 0;
 int player1MimicTimer = 0, player2MimicTimer = 0, mimicCooldown = 600;
 HashMap<String, Integer> player1MimickedAbilities = new HashMap<>();
 HashMap<String, Integer> player2MimickedAbilities = new HashMap<>();
 HashMap<String, Integer> player1MimickedDurations = new HashMap<>();
 HashMap<String, Integer> player2MimickedDurations = new HashMap<>();
 
 // NEW ABILITY BRANCHES
 // Hijacker (ability_stealer branch 1) - hijacks opponent's active ability and uses it
 int player1HijackerTimer = 0, player2HijackerTimer = 0, hijackerCooldown = 600; // 6 seconds
 String player1HijackedAbility = null, player2HijackedAbility = null;
 int player1HijackedDuration = 0, player2HijackedDuration = 0;
 
 // Virus (ability_stealer branch 2) - disables random abilities and slows paddle
 int player1VirusTimer = 0, player2VirusTimer = 0, virusCooldown = 800; // 8 seconds
 ArrayList<String> player1DisabledAbilities = new ArrayList<>();
 ArrayList<String> player2DisabledAbilities = new ArrayList<>();
 HashMap<String, Integer> player1VirusDisableDurations = new HashMap<>();
 HashMap<String, Integer> player2VirusDisableDurations = new HashMap<>();
 int player1VirusSlowTimer = 0, player2VirusSlowTimer = 0;
 
 // NEW ABILITIES
 // Gravity Hammer - slam ball downward with force
 int player1HammerTimer = 0, player2HammerTimer = 0, hammerCooldown = 400; // 4 seconds
 boolean player1HammerActive = false, player2HammerActive = false;
 int player1HammerDuration = 0, player2HammerDuration = 0, hammerDuration = 50; // 0.5 seconds
 // Branch 1: Thor's Hammer - shockwave effect
 int player1ShockwaveTimer = 0, player2ShockwaveTimer = 0;
 // Branch 2: Gravity Well - area pull effect
 boolean player1GravityWellActive = false, player2GravityWellActive = false;
 int player1GravityWellDuration = 0, player2GravityWellDuration = 0;
 int player1GravityWellX = 0, player1GravityWellY = 0;
 int player2GravityWellX = 0, player2GravityWellY = 0;
 
 // Magnet Ball - ball attracted to your paddle
 boolean player1MagnetActive = false, player2MagnetActive = false;
 // Branch 2: Force Field - toggle between attract and repel
 boolean player1MagnetRepelMode = false, player2MagnetRepelMode = false;
 
 // Shadow Clone - ghost paddles that follow behind
 ArrayList<Integer> player1ShadowPositions = new ArrayList<>();
 ArrayList<Integer> player2ShadowPositions = new ArrayList<>();
 int shadowTrailLength = 8; // Reduced to 8 frames for shadows to appear faster and more visibly
 boolean player1ShadowCloneActive = true; // Shadow clones are always active by default
 boolean player2ShadowCloneActive = true; // Shadow clones are always active by default
 
 // Independent shadow positions for Branch 2 (Afterimage Defense)
 int player1IndependentShadowY = 200;
 int player2IndependentShadowY = 200;
 boolean player1ShadowMovingUp = false;
 boolean player2ShadowMovingUp = false;
 
 // Shadow Clone collision cooldown - prevent multiple hits from jittering
 int player1ShadowCollisionCooldown = 0;
 int player2ShadowCollisionCooldown = 0;
 int shadowCollisionCooldownFrames = 15; // 0.15 seconds between shadow hits
 
 // Portal Pong - REVAMPED for offense!
 int player1PortalTimer = 0, player2PortalTimer = 0;
 int player1PortalPlacementStage = 0; // 0 = ready, 1 = entrance placed, 2 = both placed
 int player2PortalPlacementStage = 0;
 Integer player1PortalEntranceX = null, player1PortalEntranceY = null;
 Integer player1PortalExitX = null, player1PortalExitY = null;
 Integer player2PortalEntranceX = null, player2PortalEntranceY = null;
 Integer player2PortalExitX = null, player2PortalExitY = null;
 int player1PortalDuration = 0, player2PortalDuration = 0;
 int portalTrailTimer = 0; // Visual effect timer
 String lastPortalUser = ""; // Track who last teleported for VFX
 int portalBoostTimer = 0; // Brief period after teleport with visual effect
 boolean ballTeleportCooldown = false; // Prevent infinite portal loops
 int ballTeleportTimer = 0;
 // Branch 2: Dimensional Rift - third portal and movement
 Integer player1Portal3X = null, player1Portal3Y = null;
 Integer player2Portal3X = null, player2Portal3Y = null;
 int player1PortalMoveTimer = 0, player2PortalMoveTimer = 0;
 
 // Power Siphon - drain opponent ability cooldowns
 int player1SiphonTimer = 0, player2SiphonTimer = 0, siphonCooldown = 600; // 6 seconds
 boolean player1SiphonActive = false, player2SiphonActive = false;
 int player1SiphonDuration = 0, player2SiphonDuration = 0, siphonDuration = 200; // 2 seconds
 // Branch 1: Soul Reaper - copy opponent ability temporarily
 String player1CopiedAbility = null, player2CopiedAbility = null;
 int player1CopiedAbilityDuration = 0, player2CopiedAbilityDuration = 0;
 int player1SoulReapTimer = 0, player2SoulReapTimer = 0; // Cooldown tracker
 // Branch 2: Overload - charge system
 int player1OverloadCharge = 0, player2OverloadCharge = 0; // 0-100 charge
 int maxOverloadCharge = 100;
 int player1OverloadStunTimer = 0, player2OverloadStunTimer = 0; // Stun from overload
 int player1OverloadDisableTimer = 0, player2OverloadDisableTimer = 0; // Ability disable
 int player1OverloadShrinkTimer = 0, player2OverloadShrinkTimer = 0; // Paddle shrink
 
 // Time Loop - rewind game state
 int player1TimeLoopTimer = 0, player2TimeLoopTimer = 0, timeLoopCooldown = 500; // 5 seconds base
 ArrayList<GameState> gameStateHistory = new ArrayList<>();
 int maxHistorySize = 30; // Store 0.3 seconds of history (at 100fps)
 int player1TimeLoopSlowMoTimer = 0, player2TimeLoopSlowMoTimer = 0; // Slow motion effect after time loop
 int timeLoopSlowMoDuration = 100; // 1 second of slow motion
 // Branch 2: Temporal Echo - ghost ball
 boolean temporalEchoActive = false;
 int echoGhostBallX = 0, echoGhostBallY = 0, echoGhostBallVelX = 0, echoGhostBallVelY = 0;
 int temporalEchoDuration = 0;
 
 // Void Pulse (ghost_ball branch 1) - disables opponent abilities when ball phases through
 // When ghost ball phases through paddle, disables random abilities for duration
 
 // Reality Break (ghost_ball branch 2) - distorts game physics periodically
 int player1RealityTimer = 0, player2RealityTimer = 0, realityCooldown = 1200; // 12 seconds
 boolean realityBreakActive = false;
 int realityBreakDuration = 0, maxRealityBreakDuration = 300; // 3 seconds base
 int realityBreakEffect = 0; // Which distortion is active
 
 // Chaos Engine (reverse_controls branch 1) - rotating random debuffs
 int player1ChaosTimer = 0, player2ChaosTimer = 0, chaosCooldown = 300; // 3 seconds
 int player1ChaosEffectTimer = 0, player2ChaosEffectTimer = 0, chaosEffectDuration = 400; // 4 seconds
 int player1CurrentDebuff = 0, player2CurrentDebuff = 0; // 1=inverted gravity, 2=double speed, 3=reversed momentum
 
 // Puppet Master (reverse_controls branch 2) - takes control of opponent paddle
 int player1PuppetTimer = 0, player2PuppetTimer = 0, puppetCooldown = 500; // 5 seconds
 int player1PuppetEffectTimer = 0, player2PuppetEffectTimer = 0, puppetEffectDuration = 200; // 2 seconds base
 
 int scorePlayer1 = 0, scorePlayer2 = 0;
 int level1 = 1, level2 = 1;
 int pointsToNextLevel1 = 1, pointsToNextLevel2 = 1;
 int totalPointsThisLevel1 = 0, totalPointsThisLevel2 = 0;
 double ballSpeedMultiplier = 1.0, aiSpeedMultiplier = 1.0;
 double ballSpeed = 3.0; // Track current ball speed (increases by 0.1 on paddle hits)
 double initialBallSpeed = 3.0; // Store initial speed for fireball restoration
 ArrayList<PowerUp> powerUps = new ArrayList<>();
 int powerUpSpawnInterval = 500, powerUpTimer = 0;
 int zigzagSpawnTimer = 0; // Timer for zigzag spawning (200 frames = 2 seconds)

 // Underdog boost tracking - triggers at 10, 20, 30, 40, etc. (every 10 points behind)
 int player1LastUnderdogTrigger = 0;
 int player2LastUnderdogTrigger = 0;
 
 // JOSHUA level up tracking - levels up every 10 points scored
 int player1JoshuaPoints = 0;
 int player2JoshuaPoints = 0;
 
 // JAISAN level up tracking - levels up every 10 points scored (same as JOSHUA)
 int player1JaisanPoints = 0;
 int player2JaisanPoints = 0;
 
 // Evolved abilities auto-level tracking - levels up every 20 points scored
 HashMap<String, Integer> player1EvolvedAbilityPoints = new HashMap<>();
 HashMap<String, Integer> player2EvolvedAbilityPoints = new HashMap<>();

 // HAKI ability tracking (One Piece themed)
 // Base: Conqueror's Haki - stun opponent briefly (6s cooldown)
 // Branch 1: Armament Haki - stun + black paddle + phasing ball + debuff immunity (10s cooldown)
 // Branch 2: Observation Haki - stun + opponent-only slow-mo + trajectory + auto-block (9s cooldown)
 int player1HakiTimer = 0, player2HakiTimer = 0;
 int hakiBaseCooldown = 600; // 6 seconds for base
 int hakiArmamentCooldown = 1000; // 10 seconds for armament branch
 int hakiObservationCooldown = 900; // 9 seconds for observation branch
 int player1HakiEffectTimer = 0, player2HakiEffectTimer = 0;
 int hakiStunDuration = 80; // 0.8 seconds base stun
 boolean player1ArmamentActive = false, player2ArmamentActive = false;
 int player1ArmamentDuration = 0, player2ArmamentDuration = 0;
 int armamentBaseDuration = 300; // 3 seconds
 boolean player1HakiPhaseActive = false, player2HakiPhaseActive = false; // Ball phases through opponent paddle once
 boolean player1ObservationActive = false, player2ObservationActive = false;
 int player1ObservationDuration = 0, player2ObservationDuration = 0;
 int observationBaseDuration = 200; // 2 seconds
 boolean player1ObsAutoBlockUsed = false, player2ObsAutoBlockUsed = false; // Auto-block once per activation
 double player1ObsSlowFactor = 1.0, player2ObsSlowFactor = 1.0; // Opponent paddle slow factor (1.0 = normal)

 // BARRIER ability tracking (Defensive)
 // Base: Creates a temporary barrier that blocks ball once
 // Branch 1: Mirror Shield - reflects ball at double speed
 // Branch 2: Absorption Shield - catches ball and lets you relaunch
 int player1BarrierTimer = 0, player2BarrierTimer = 0, barrierCooldown = 800; // 8 seconds
 boolean player1BarrierActive = false, player2BarrierActive = false;
 int player1BarrierX = 0, player2BarrierX = 0;
 int player1BarrierY = 0, player2BarrierY = 0;
 int barrierWidth = 10, barrierHeight = 80;
 int player1BarrierDuration = 0, player2BarrierDuration = 0;
 int barrierBaseDuration = 500; // 5 seconds or until hit
 boolean player1BallAbsorbed = false, player2BallAbsorbed = false;
 int player1AbsorbedBallTimer = 0, player2AbsorbedBallTimer = 0;

 // TRAP ability tracking (Offensive) - BUFFED!
 // Base: Place a BIG trap that stuns opponent + redirects ball toward them!
 // Branch 1: Sticky Trap - Ball FREEZES for 2.5s + 3s reversed controls!
 // Branch 2: Explosive Trap - MASSIVE shockwave stuns 1.5s + shrinks 2.5s + 2.5x ball speed!
 int player1TrapTimer = 0, player2TrapTimer = 0, trapCooldown = 350; // 3.5 seconds
 boolean player1TrapActive = false, player2TrapActive = false;
 int player1TrapX = 0, player2TrapX = 0;
 int player1TrapY = 0, player2TrapY = 0;
 int trapSize = 80; // Big trap!
 int player1TrapDuration = 0, player2TrapDuration = 0;
 int trapBaseDuration = 1000; // 10 seconds or until triggered
 int player1StickyTimer = 0, player2StickyTimer = 0; // Ball slow effect from sticky trap
 int stickyDuration = 300; // 3 seconds of slow
 // Ball freeze tracking for sticky trap
 boolean ballFrozenByTrap = false;
 int ballFreezeTimer = 0;
 int frozenBallX = 0, frozenBallY = 0;

 // SCREEN WARP ability tracking (Chaos)
 // Base: Briefly distorts opponent's view
 // Branch 1: Full Inversion - flip opponent's screen vertically
 // Branch 2: Tunnel Vision - shrink opponent's visible area
 int player1WarpTimer = 0, player2WarpTimer = 0, warpCooldown = 900; // 9 seconds
 int player1WarpEffectTimer = 0, player2WarpEffectTimer = 0;
 int warpBaseDuration = 200; // 2 seconds base
 int player1InversionTimer = 0, player2InversionTimer = 0; // Screen flip effect
 int player1TunnelTimer = 0, player2TunnelTimer = 0; // Tunnel vision effect

 // BANKAI ability tracking (Bleach themed)
 // Base: Enter Bankai mode - paddle turns BLACK, gain permanent stacking boosts!
 // During Bankai: Press ability key to SWING ZANGETSU - ball goes 5x speed!
 // After 3 uses: Fire a GETSUGA TENSHO sword projectile at opponent!
 // Branch 1: Zangetsu - more attack power, bigger sword swings
 // Branch 2: Hollow Form - defense + ball phases through once
 int player1BankaiTimer = 0, player2BankaiTimer = 0, bankaiCooldown = 1200; // 12 seconds
 boolean player1BankaiActive = false, player2BankaiActive = false;
 int player1BankaiDuration = 0, player2BankaiDuration = 0;
 int bankaiBaseDuration = 800; // 8 seconds of Bankai mode
 boolean player1ZangetsuActive = false, player2ZangetsuActive = false;
 boolean player1HollowActive = false, player2HollowActive = false;
 boolean player1HollowPhased = false, player2HollowPhased = false; // Track if hollow phase was used

 // BANKAI SWORD SWING - Press ability key during Bankai to swing!
 int player1SwordSwingTimer = 0, player2SwordSwingTimer = 0; // Animation timer
 int swordSwingDuration = 20; // 0.2 second swing animation
 int player1SwordSwingCooldown = 0, player2SwordSwingCooldown = 0; // Cooldown between swings
 int swordSwingCooldownTime = 80; // 0.8 seconds between swings
 int player1TotalSwordSwings = 0, player2TotalSwordSwings = 0; // Total swings this game

 // GETSUGA TENSHO - Sword projectile fired after 3 swings!
 ArrayList<GetsugaTensho> getsugaProjectiles = new ArrayList<>();

 // BANKAI PERMANENT BOOSTS - These stack every time you activate Bankai!
 int player1BankaiStacks = 0, player2BankaiStacks = 0; // Number of times Bankai activated
 int player1PermanentSpeedBonus = 0, player2PermanentSpeedBonus = 0; // +1 paddle speed per stack
 int player1PermanentPaddleBonus = 0, player2PermanentPaddleBonus = 0; // +10 paddle height per stack
 double player1BallSpeedMultiplier = 1.0, player2BallSpeedMultiplier = 1.0; // Ball speed when YOU hit it
 boolean player1HasBankaiImmunity = false, player2HasBankaiImmunity = false; // Immunity to debuffs (5+ stacks)

 // No-score timer - triggers ability upgrade if 2 minutes pass without scoring
 int noScoreTimer = 0;
 int noScoreThreshold = 12000; // 2 minutes in frames (100 fps × 120 seconds)

 // Learning AI - tracks and learns from player behavior (CHESS COACH STYLE)
 void updateLearningAI() {
 if (!learningAIEnabled || !singlePlayer) return;
 
 learningFrameCounter++;
 
 // Sample every 5 frames for smoother, more gradual learning
 if (learningFrameCounter % 5 == 0) {
 int paddle1CenterY = paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2;
 int ballCenterY = ballY + 7;
 
 // Track player's average position preference
 playerPositionHistory.add(paddle1CenterY);
 if (playerPositionHistory.size() > maxLearningData) {
 playerPositionHistory.remove(0);
 }
 
 // Calculate average position
 if (!playerPositionHistory.isEmpty()) {
 double sum = 0;
 for (int pos : playerPositionHistory) sum += pos;
 learnedAveragePosition = sum / playerPositionHistory.size();
 }
 
 // Track reaction speed - VERY GRADUAL increase
 int positionChange = Math.abs(paddle1Y - lastPlayerY);
 if (positionChange > 0 && Math.abs(ballCenterY - paddle1CenterY) > 15) {
 playerResponseTimes.add(positionChange);
 if (playerResponseTimes.size() > maxLearningData) {
 playerResponseTimes.remove(0);
 }
 
 // Calculate average reaction speed - AI learns SLOWLY to match
 if (!playerResponseTimes.isEmpty()) {
 double sum = 0;
 for (int response : playerResponseTimes) sum += response;
 double avgResponse = sum / playerResponseTimes.size();
 // Start at 0.3, SLOWLY approach player speed (capped at 70% of player speed initially)
 double targetSpeed = Math.min(2.0, avgResponse / 3.0);
 learnedReactionSpeed = learnedReactionSpeed * 0.995 + targetSpeed * 0.005; // Very slow adjustment
 }
 }
 
 // Track aggressiveness
 boolean isMovingUp = paddle1Y < lastPlayerY;
 boolean isMovingDown = paddle1Y > lastPlayerY;
 
 if (isMovingUp || isMovingDown) {
 boolean ballAbove = ballCenterY < paddle1CenterY;
 boolean ballBelow = ballCenterY > paddle1CenterY;
 
 double aggressiveness = 0.5;
 if ((isMovingUp && ballAbove) || (isMovingDown && ballBelow)) {
 aggressiveness = 0.8 + Math.random() * 0.2;
 } else if ((isMovingUp && ballBelow) || (isMovingDown && ballAbove)) {
 aggressiveness = 0.1 + Math.random() * 0.2;
 }
 
 playerAggressiveness.add(aggressiveness);
 if (playerAggressiveness.size() > maxLearningData) {
 playerAggressiveness.remove(0);
 }
 
 // Calculate average aggressiveness
 if (!playerAggressiveness.isEmpty()) {
 double sum = 0;
 for (double agg : playerAggressiveness) sum += agg;
 learnedAggressiveness = sum / playerAggressiveness.size();
 }
 }
 
 // Track successful player hits to measure skill level
 if (ballX < 30 && ballVelX < 0 && Math.abs(ballCenterY - paddle1CenterY) < 50) {
 playerSuccessfulHits++;
 successfulHitPositions.add(paddle1CenterY);
 if (successfulHitPositions.size() > 200) {
 successfulHitPositions.remove(0);
 }
 }
 
 // Track AI hits
 if (ballX > 570 && ballVelX > 0) {
 aiSuccessfulHits++;
 }
 
 lastPlayerY = paddle1Y;
 lastBallY = ballY;
 wasMovingUp = isMovingUp;
 wasMovingDown = isMovingDown;
 
 learningDataPoints = Math.min(maxLearningData, 
 Math.max(playerPositionHistory.size(), 
 Math.max(playerResponseTimes.size(), playerAggressiveness.size())));
 
 // Calculate learning progress MUCH MORE SLOWLY (0.0 to 1.0)
 // Now requires 600+ data points instead of 200
 learningProgress = Math.min(1.0, learningDataPoints / 600.0);
 
 // Measure player skill level based on success rate
 totalRallies = playerSuccessfulHits + aiSuccessfulHits;
 if (totalRallies > 20) {
 playerSkillLevel = (double) playerSuccessfulHits / totalRallies;
 // Keep skill level between 0.2 and 0.9
 playerSkillLevel = Math.max(0.2, Math.min(0.9, playerSkillLevel));
 }
 
 // AI adaptation: SLOWLY matches player skill (like chess coach)
 // If player is winning too much (>60%), AI gets slightly harder
 // If player is losing too much (<40%), AI gets slightly easier
 if (totalRallies > 30) {
 if (playerSkillLevel > 0.6) {
 // Player is winning - AI slowly gets better (max 1.4x)
 aiAdaptationMultiplier = Math.min(1.4, aiAdaptationMultiplier + 0.002);
 } else if (playerSkillLevel < 0.4) {
 // Player is struggling - AI stays easier (min 0.7x)
 aiAdaptationMultiplier = Math.max(0.7, aiAdaptationMultiplier - 0.002);
 } else {
 // Balanced - AI slowly approaches 1.0x
 if (aiAdaptationMultiplier > 1.0) {
 aiAdaptationMultiplier -= 0.001;
 } else if (aiAdaptationMultiplier < 1.0) {
 aiAdaptationMultiplier += 0.001;
 }
 }
 }
 }
 }
 
 // Apply learned behavior to AI movement - CHESS COACH STYLE (matches player skill gradually)
 int getLearnedAIMovement(int paddle2CenterY, int targetY, int baseSpeed) {
 if (!learningAIEnabled || learningDataPoints < 30) {
 // Very early learning - AI is very weak
 return 0; 
 }
 
 // Learning strength increases VERY gradually (much slower curve)
 double learningStrength = Math.min(1.0, Math.pow(learningProgress, 1.2));
 
 // ALWAYS match player skill level - if player is struggling, AI stays easier
 double skillAdjustment = 0.5 + (playerSkillLevel * 0.5); // 0.5x to 1.0x based on player success
 
 // PHASE 1: Early Learning (0-30% progress) - AI is weak, learning basics
 if (learningProgress < 0.3) {
 // AI moves slowly and makes lots of mistakes
 int slowSpeed = (int)(baseSpeed * 0.5 * skillAdjustment);
 
 if (Math.random() < 0.4) {
 // Often moves wrong direction or freezes
 if (Math.random() < 0.5) {
 return (Math.random() < 0.5) ? slowSpeed : -slowSpeed;
 }
 return 0;
 }
 
 if (Math.abs(targetY - paddle2CenterY) > 60) {
 return (paddle2CenterY < targetY) ? slowSpeed : -slowSpeed;
 }
 return 0;
 }
 
 // PHASE 2: Learning Phase (30-60% progress) - AI starts adapting slowly
 if (learningProgress < 0.6) {
 // Gradually mimic player's reaction speed (capped at 80% of learned speed)
 int adjustedSpeed = (int)(baseSpeed * learnedReactionSpeed * 0.8 * skillAdjustment);
 adjustedSpeed = Math.max(2, Math.min(10, adjustedSpeed));
 
 // Start mimicking position preference
 int ballCenterY = ballY + 7;
 if (Math.abs(ballX - 300) < 150 && ballVelX < 0) {
 int preferredY = (int)(learnedAveragePosition * learningStrength + 200 * (1 - learningStrength));
 if (Math.abs(paddle2CenterY - preferredY) > 50) {
 return (paddle2CenterY < preferredY) ? adjustedSpeed : -adjustedSpeed;
 }
 }
 
 // Still makes some mistakes (20% chance)
 if (Math.random() < 0.2) {
 return 0;
 }
 
 // Basic movement toward ball
 double distanceToBall = Math.abs(targetY - paddle2CenterY);
 if (distanceToBall < 70) {
 return (targetY < paddle2CenterY) ? -adjustedSpeed : adjustedSpeed;
 }
 return 0;
 }
 
 // PHASE 3: Competent Phase (60-100% progress) - AI is skilled but fair
 // AI plays at player's level, NOT above it (like a good chess coach)
 
 // Match player's speed, but don't exceed it significantly
 int adjustedSpeed = (int)(baseSpeed * learnedReactionSpeed * skillAdjustment * aiAdaptationMultiplier);
 adjustedSpeed = Math.max(3, Math.min(13, adjustedSpeed));
 
 // Mimic player's positioning style
 int ballCenterY = ballY + 7;
 if (ballVelX > 0 && ballX > 300) {
 // Ball coming to AI - use learned positioning but keep it fair
 if (!successfulHitPositions.isEmpty() && learningProgress > 0.75) {
 double avgSuccessPos = 0;
 for (int pos : successfulHitPositions) avgSuccessPos += pos;
 avgSuccessPos /= successfulHitPositions.size();
 
 // Slightly counter player's preferred positions (not perfect)
 if (avgSuccessPos < 150 && Math.random() < 0.6) {
 targetY += 20;
 } else if (avgSuccessPos > 250 && Math.random() < 0.6) {
 targetY -= 20;
 }
 }
 } else if (Math.abs(ballX - 300) < 150) {
 // Ball in middle - mimic player's position
 int preferredY = (int)(learnedAveragePosition * learningStrength);
 if (Math.abs(paddle2CenterY - preferredY) > 30) {
 return (paddle2CenterY < preferredY) ? adjustedSpeed : -adjustedSpeed;
 }
 }
 
 // Match player's aggression level (not exceed it)
 double distanceToBall = Math.abs(targetY - paddle2CenterY);
 double aggressionThreshold;
 
 if (learnedAggressiveness > 0.7) {
 // Player is aggressive - AI matches aggression
 aggressionThreshold = 40;
 } else if (learnedAggressiveness < 0.4) {
 // Player is defensive - AI stays patient
 aggressionThreshold = 75;
 } else {
 // Player is balanced
 aggressionThreshold = 55;
 }
 
 // Move toward ball
 if (distanceToBall < aggressionThreshold) {
 if (targetY < paddle2CenterY - 12) {
 return -adjustedSpeed;
 } else if (targetY > paddle2CenterY + 12) {
 return adjustedSpeed;
 }
 }
 
 // At high learning, move more consistently (but still fair)
 if (learningProgress > 0.85 && distanceToBall > 20) {
 return (targetY < paddle2CenterY) ? -adjustedSpeed : adjustedSpeed;
 }
 
 return 0;
 }

 class Bullet {
 int x, y, velocityX, velocityY, owner;
 Bullet(int x, int y, int velocityX, int velocityY, int owner) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.velocityY = velocityY; this.owner = owner;
 }
 }
 
 class VegetaBullet {
 int x, y, velocityX, velocityY, owner, gunLevel;
 VegetaBullet(int x, int y, int velocityX, int velocityY, int owner) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.velocityY = velocityY; this.owner = owner;
 this.gunLevel = 1; // Default level
 }
 VegetaBullet(int x, int y, int velocityX, int velocityY, int owner, int gunLevel) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.velocityY = velocityY; this.owner = owner;
 this.gunLevel = gunLevel;
 }
 }
 
 class Explosion {
 int x, y, radius, duration, owner;
 boolean isJiren = false;
 int jirenLevel = 0;
 boolean hasStolen = false;
 boolean hasHit = false; // Track if Vegeta explosion has hit player
 Explosion(int x, int y, int radius, int owner, int duration) {
 this.x = x; this.y = y; this.radius = radius; this.owner = owner; this.duration = duration;
 }
 Explosion(int x, int y, int radius, int owner, int duration, boolean isJiren, int jirenLevel) {
 this.x = x; this.y = y; this.radius = radius; this.owner = owner; this.duration = duration;
 this.isJiren = isJiren;
 this.jirenLevel = jirenLevel;
 }
 }
 
 class StealerBullet {
 int x, y, velocityX, owner;
 StealerBullet(int x, int y, int velocityX, int owner) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.owner = owner;
 }
 }
 
 class HijackerBullet {
 int x, y, velocityX, owner;
 HijackerBullet(int x, int y, int velocityX, int owner) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.owner = owner;
 }
 }
 
 class VirusBullet {
 int x, y, velocityX, owner;
 VirusBullet(int x, int y, int velocityX, int owner) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.owner = owner;
 }
 }
 
 class FreezaLaser {
 int startX, startY, endX, endY, owner, duration, remainingTime, level;
 int thickness;
 boolean hasStolen = false; // Track if this laser has already stolen abilities
 FreezaLaser(int startX, int startY, int endX, int endY, int owner, int duration, int level) {
 this.startX = startX; this.startY = startY; this.endX = endX; this.endY = endY;
 this.owner = owner; this.duration = duration; this.remainingTime = duration;
 this.level = level;
 // Thickness increases with level (marginally thicker)
 this.thickness = 3 + level; // Base 3px + level
 }
 }
 
 class JirenBullet {
 int x, y, velocityX, owner, level;
 JirenBullet(int x, int y, int velocityX, int owner, int level) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.owner = owner;
 this.level = level;
 }
 }

 // GETSUGA TENSHO - Massive sword wave projectile from Bankai!
 class GetsugaTensho {
 int x, y, velocityX, owner, width, height;
 boolean isEvolved = false; // Evolved version does more
 GetsugaTensho(int x, int y, int velocityX, int owner, boolean isEvolved) {
 this.x = x; this.y = y; this.velocityX = velocityX; this.owner = owner;
 this.isEvolved = isEvolved;
 this.width = isEvolved ? 80 : 50; // Evolved is bigger
 this.height = isEvolved ? 120 : 80;
 }
 }

 class Laser {
 int startX, startY, endX, endY, owner, duration, remainingTime;
 boolean hasStunned = false; // Track if this laser has already stunned
 Laser(int startX, int startY, int endX, int endY, int owner, int duration) {
 this.startX = startX; this.startY = startY; this.endX = endX; this.endY = endY;
 this.owner = owner; this.duration = duration; this.remainingTime = duration;
 }
 }
 
 class Ball {
 int x, y, velX, velY;
 Ball(int x, int y, int velX, int velY) {
 this.x = x; this.y = y; this.velX = velX; this.velY = velY;
 }
 }
 ArrayList<Laser> activeLasers = new ArrayList<>();
 ArrayList<FreezaLaser> freezaLasers = new ArrayList<>();
 ArrayList<JirenBullet> jirenBullets = new ArrayList<>();

 class PowerUp {
 int x, y;
 String type;
 PowerUp(int x, int y, String type) { this.x = x; this.y = y; this.type = type; }
 }

 void configureKeybinds(boolean isSinglePlayer) {
 String[] options = {"Use Default Keybinds", "Customize Keybinds"};
 int choice = JOptionPane.showOptionDialog(null, 
 "Keybind Configuration:\n\n" +
 "Default Keybinds:\n" +
 "Player 1: W (Up), S (Down), Q (Ability)\n" +
 (isSinglePlayer ? "" : "Player 2: ^ (Up), v (Down), / (Ability)\n") +
 "\nWould you like to customize your keybinds?",
 "Configure Controls", 
 JOptionPane.DEFAULT_OPTION, 
 JOptionPane.QUESTION_MESSAGE, 
 null, 
 options, 
 options[0]);
 
 if (choice == 1) {
 // Customize Player 1 keybinds
 customizePlayerKeybinds(1);
 
 // Customize Player 2 keybinds if 2-player mode
 if (!isSinglePlayer) {
 customizePlayerKeybinds(2);
 }
 }
 }
 
 void customizePlayerKeybinds(int player) {
 String playerName = "Player " + player;
 JOptionPane.showMessageDialog(null, 
 "Configuring keybinds for " + playerName + "\n\n" +
 "You'll be prompted to press keys for:\n" +
 "1. Move Up\n" +
 "2. Move Down\n" +
 "3. Use Ability\n\n" +
 "Press OK to continue.",
 playerName + " Keybinds",
 JOptionPane.INFORMATION_MESSAGE);
 
 // Configure Up key
 int upKey = promptForKey(playerName + " - Press key for MOVE UP:");
 if (upKey != -1) {
 if (player == 1) player1UpKey = upKey;
 else player2UpKey = upKey;
 }
 
 // Configure Down key
 int downKey = promptForKey(playerName + " - Press key for MOVE DOWN:");
 if (downKey != -1) {
 if (player == 1) player1DownKey = downKey;
 else player2DownKey = downKey;
 }
 
 // Configure Ability key
 int abilityKey = promptForKey(playerName + " - Press key for USE ABILITY:");
 if (abilityKey != -1) {
 if (player == 1) player1AbilityKey = abilityKey;
 else player2AbilityKey = abilityKey;
 }
 
 // Show configured keybinds
 String upKeyName = KeyEvent.getKeyText(player == 1 ? player1UpKey : player2UpKey);
 String downKeyName = KeyEvent.getKeyText(player == 1 ? player1DownKey : player2DownKey);
 String abilityKeyName = KeyEvent.getKeyText(player == 1 ? player1AbilityKey : player2AbilityKey);
 
 JOptionPane.showMessageDialog(null,
 playerName + " Keybinds Configured!\n\n" +
 "Move Up: " + upKeyName + "\n" +
 "Move Down: " + downKeyName + "\n" +
 "Use Ability: " + abilityKeyName,
 "Keybinds Set",
 JOptionPane.INFORMATION_MESSAGE);
 }
 
 int promptForKey(String message) {
 final int[] keyPressed = {-1};
 final boolean[] waiting = {true};
 
 JDialog dialog = new JDialog();
 dialog.setTitle("Press a Key");
 dialog.setModal(true);
 dialog.setSize(400, 150);
 dialog.setLocationRelativeTo(null);
 dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 
 JLabel label = new JLabel("<html><center>" + message + "<br><br>Waiting for key press...</center></html>");
 label.setHorizontalAlignment(JLabel.CENTER);
 label.setFont(new Font("Arial", Font.BOLD, 14));
 dialog.add(label);
 
 dialog.addKeyListener(new KeyAdapter() {
 @Override
 public void keyPressed(KeyEvent e) {
 keyPressed[0] = e.getKeyCode();
 waiting[0] = false;
 label.setText("<html><center>Key captured: " + KeyEvent.getKeyText(e.getKeyCode()) + "</center></html>");
 Timer timer = new Timer(800, evt -> dialog.dispose());
 timer.setRepeats(false);
 timer.start();
 }
 });
 
 dialog.setVisible(true);
 return keyPressed[0];
 }

 // Menu-mode constructor - opens directly to main menu
 public PingPongGame() {
 this.singlePlayer = true;
 this.aiDifficulty = 1;

 // Load story progress so the map shows correct state
 loadStoryProgress();
 initLevelNodePositions();
 menuSelectedLevel = storyModeLevel - 1;

 // Create window
 JFrame frame = new JFrame("Pongus");
 gameFrame = frame;
 frame.setSize(600, 400);
 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 frame.setResizable(true);
 frame.add(this);
 frame.addKeyListener(this);
 this.addKeyListener(this);
 this.addMouseListener(this);
 setFocusable(true);
 frame.setVisible(true);
 requestFocusInWindow();

 // Menu state - game paused, showing menu
 showingMainMenu = true;
 isPaused = true;

 // Load shrek sprite
 loadShrekSprite();

 // Start timer for menu animations
 timer = new javax.swing.Timer(10, this);
 timer.start();
 }

 public PingPongGame(boolean isSinglePlayer, int difficulty) {
 this.singlePlayer = isSinglePlayer;
 this.aiDifficulty = difficulty;
 player2Name = isSinglePlayer ? "AI" : "Player 2";

 // Enable Learning AI for difficulty 4
 if (difficulty == 4) {
 learningAIEnabled = true;
 }

 // Configure Keybinds
 configureKeybinds(isSinglePlayer);
 
 JFrame frame = new JFrame("Pongus");
 gameFrame = frame;
 frame.setSize(600, 400);
 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 frame.setResizable(true);
 frame.add(this);
 frame.addKeyListener(this);
 this.addKeyListener(this);
 this.addMouseListener(this);
 setFocusable(true);
 frame.setVisible(true);
 requestFocusInWindow();
 showingMainMenu = false;
 initLevelNodePositions();
 loadShrekSprite();
 timer = new javax.swing.Timer(10, this);
 timer.start();
 spawnMapPowerUp();
 }

 // Online multiplayer constructor - Host (now disabled)
 public PingPongGame(boolean isSinglePlayer, int difficulty, boolean online, boolean host) {
 this(isSinglePlayer, difficulty);
 // Online mode removed — start as local (host flag ignored)
 this.singlePlayer = false;
 }

 // Online multiplayer constructor - Client (now disabled)
 public PingPongGame(boolean isSinglePlayer, int difficulty, boolean online, boolean host, String ip) {
 this(isSinglePlayer, difficulty);
 // Online mode removed — start as local (connect ignored)
 this.singlePlayer = false;
 }

 // Multiplayer removed: server hosting logic stripped

 // Multiplayer removed: local IP helper stripped

 // Multiplayer removed: public IP helper stripped

 // Multiplayer removed: port forwarding helper stripped

 // Multiplayer removed: client connect logic stripped

 // Multiplayer removed: network listener stripped

 // Multiplayer removed: incoming data handler stripped

 // Multiplayer removed: outgoing data sender stripped

 // ============== MAIN MENU FUNCTIONS ==============

 void loadShrekSprite() {
 try {
  BufferedImage raw = null;

  // Try classpath resource (inside JAR)
  java.io.InputStream is = PingPongGame.class.getResourceAsStream("/assets/swek.png");
  if (is == null) is = PingPongGame.class.getResourceAsStream("assets/swek.png");
  if (is == null) is = PingPongGame.class.getResourceAsStream("/swek.png");
  if (is == null) is = PingPongGame.class.getResourceAsStream("swek.png");
  if (is != null) {
  raw = ImageIO.read(is);
  is.close();
  }

  // Try relative to JAR location
  if (raw == null) {
  try {
   String jarPath = PingPongGame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
   File jarDir = new File(jarPath).getParentFile();
   File f = new File(jarDir, "assets/swek.png");
   if (f.exists()) raw = ImageIO.read(f);
  } catch (Exception ignored) {}
  }

  // Try relative to CWD
  if (raw == null) {
  String[] paths = {"assets/swek.png", "swek.png"};
  for (String path : paths) {
   File f = new File(path);
   if (f.exists()) { raw = ImageIO.read(f); break; }
  }
  }

  if (raw != null) {
  shrekSprite = raw;
  }
 } catch (Exception e) {
  System.out.println("Could not load shrek sprite: " + e.getMessage());
 }
 }

 void initLevelNodePositions() {
 // Serpentine path: 5-4-4-4 nodes across 4 rows
 // Row 1: Levels 1-5 (left to right)
 int[] row1X = {70, 190, 300, 410, 530};
 for (int i = 0; i < 5; i++) { levelNodeX[i] = row1X[i]; levelNodeY[i] = 70; }
 // Row 2: Levels 6-9 (right to left)
 int[] row2X = {530, 410, 300, 190};
 for (int i = 0; i < 4; i++) { levelNodeX[5 + i] = row2X[i]; levelNodeY[5 + i] = 140; }
 // Row 3: Levels 10-13 (left to right)
 int[] row3X = {190, 300, 410, 530};
 for (int i = 0; i < 4; i++) { levelNodeX[9 + i] = row3X[i]; levelNodeY[9 + i] = 210; }
 // Row 4: Levels 14-17 (right to left)
 int[] row4X = {530, 410, 300, 190};
 for (int i = 0; i < 4; i++) { levelNodeX[13 + i] = row4X[i]; levelNodeY[13 + i] = 280; }
 }

 void drawMainMenu(Graphics2D g2d) {
 // Background gradient
 GradientPaint bg = new GradientPaint(0, 0, new Color(5, 10, 30), 0, 400, new Color(15, 25, 60));
 g2d.setPaint(bg);
 g2d.fillRect(0, 0, 600, 400);

 // Starfield-like dots for ambiance
 g2d.setColor(new Color(255, 255, 255, 30));
 for (int i = 0; i < 50; i++) {
  int sx = (i * 137 + 29) % 600;
  int sy = (i * 97 + 13) % 310;
  g2d.fillOval(sx, sy, 2, 2);
 }

 // Title
 g2d.setFont(new Font("Arial", Font.BOLD, 36));
 g2d.setColor(new Color(255, 215, 0));
 String title = "PONGUS";
 int titleWidth = g2d.getFontMetrics().stringWidth(title);
 g2d.drawString(title, (600 - titleWidth) / 2, 38);

 // Subtitle
 g2d.setFont(new Font("Arial", Font.PLAIN, 12));
 g2d.setColor(new Color(180, 180, 180));
 String subtitle = "Story Progress: Level " + storyModeLevel + " / " + storyModeMaxLevel;
 int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
 g2d.drawString(subtitle, (600 - subWidth) / 2, 52);

 // Draw connecting lines between nodes (behind nodes)
 g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 for (int i = 0; i < storyModeMaxLevel - 1; i++) {
  if (i < storyModeLevel - 1) {
  g2d.setColor(new Color(100, 200, 100, 180)); // Green for completed path
  } else if (i == storyModeLevel - 1) {
  g2d.setColor(new Color(255, 215, 0, 150)); // Gold for current edge
  } else {
  g2d.setColor(new Color(60, 60, 60, 120)); // Dark for locked
  }
  g2d.drawLine(levelNodeX[i], levelNodeY[i], levelNodeX[i + 1], levelNodeY[i + 1]);
 }
 g2d.setStroke(new BasicStroke(1));

 // Draw level nodes
 for (int i = 0; i < storyModeMaxLevel; i++) {
  int nx = levelNodeX[i];
  int ny = levelNodeY[i];
  int r = 14;

  if (i < storyModeLevel - 1) {
  // COMPLETED - green
  g2d.setColor(new Color(50, 180, 50));
  g2d.fillOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setColor(new Color(100, 255, 100));
  g2d.drawOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setFont(new Font("Arial", Font.BOLD, 11));
  g2d.setColor(Color.WHITE);
  String num = String.valueOf(i + 1);
  int nw = g2d.getFontMetrics().stringWidth(num);
  g2d.drawString(num, nx - nw / 2, ny + 4);
  } else if (i == storyModeLevel - 1) {
  // CURRENT - pulsing gold glow
  int pulse = (int)(Math.sin(menuAnimationTimer * 0.05) * 5 + 5);
  g2d.setColor(new Color(255, 215, 0, 50));
  g2d.fillOval(nx - r - pulse, ny - r - pulse, (r + pulse) * 2, (r + pulse) * 2);
  g2d.setColor(new Color(255, 200, 0));
  g2d.fillOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setColor(new Color(255, 255, 100));
  g2d.drawOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setFont(new Font("Arial", Font.BOLD, 12));
  g2d.setColor(Color.BLACK);
  String num = String.valueOf(i + 1);
  int nw = g2d.getFontMetrics().stringWidth(num);
  g2d.drawString(num, nx - nw / 2, ny + 5);
  } else {
  // LOCKED - dark gray
  g2d.setColor(new Color(50, 50, 50));
  g2d.fillOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setColor(new Color(90, 90, 90));
  g2d.drawOval(nx - r, ny - r, r * 2, r * 2);
  g2d.setFont(new Font("Arial", Font.BOLD, 11));
  g2d.setColor(new Color(100, 100, 100));
  String num = String.valueOf(i + 1);
  int nw = g2d.getFontMetrics().stringWidth(num);
  g2d.drawString(num, nx - nw / 2, ny + 4);
  }

  // Selection highlight ring
  if (i == menuSelectedLevel) {
  g2d.setColor(new Color(255, 255, 255, 200));
  g2d.setStroke(new BasicStroke(2));
  g2d.drawOval(nx - r - 4, ny - r - 4, (r + 4) * 2, (r + 4) * 2);
  g2d.setStroke(new BasicStroke(1));
  }
 }

 // Boss name for selected level
 if (menuSelectedLevel >= 0 && menuSelectedLevel < storyModeMaxLevel) {
  g2d.setFont(new Font("Arial", Font.BOLD, 14));
  String bossInfo = "Level " + (menuSelectedLevel + 1) + ": " + storyBossNames[menuSelectedLevel];
  if (menuSelectedLevel < storyModeLevel - 1) {
  g2d.setColor(new Color(100, 255, 100));
  bossInfo += " [COMPLETE]";
  } else if (menuSelectedLevel == storyModeLevel - 1) {
  g2d.setColor(new Color(255, 215, 0));
  bossInfo += " - Score " + storyLevelScoreToWin[menuSelectedLevel] + " to win!";
  }
  int infoW = g2d.getFontMetrics().stringWidth(bossInfo);
  g2d.drawString(bossInfo, (600 - infoW) / 2, 310);
 }

 // Bottom button bar
 String[] buttonLabels = {"Play Story", "1P", "2P", "Settings"};
 int[] buttonWidths = {120, 60, 60, 90};
 int totalBtnWidth = 120 + 60 + 60 + 90 + 30; // 3 gaps of 10px
 int startX = (600 - totalBtnWidth) / 2;
 int btnY = 340;
 int btnH = 35;

 for (int i = 0; i < 4; i++) {
  int bx = startX;
  int bw = buttonWidths[i];
  menuButtons[i] = new Rectangle(bx, btnY, bw, btnH);

  // Button background
  Color btnColor;
  if (i == 0) btnColor = new Color(160, 120, 0); // Gold for Play Story
  else btnColor = new Color(40, 60, 120);
  g2d.setColor(btnColor);
  g2d.fillRoundRect(bx, btnY, bw, btnH, 8, 8);
  g2d.setColor(new Color(255, 255, 255, 120));
  g2d.drawRoundRect(bx, btnY, bw, btnH, 8, 8);

  // Button text
  g2d.setFont(new Font("Arial", Font.BOLD, 14));
  g2d.setColor(Color.WHITE);
  int textW = g2d.getFontMetrics().stringWidth(buttonLabels[i]);
  g2d.drawString(buttonLabels[i], bx + (bw - textW) / 2, btnY + 23);

  startX += bw + 10;
 }

 // Unlocked abilities at the very bottom
 g2d.setFont(new Font("Arial", Font.PLAIN, 10));
 g2d.setColor(new Color(150, 150, 150));
 String abilities = (unlockedAbilities == null || unlockedAbilities.isEmpty()) ?
  "No abilities unlocked yet - defeat bosses to earn them!" :
  "Abilities: " + String.join(", ", unlockedAbilities);
 if (abilities.length() > 90) abilities = abilities.substring(0, 87) + "...";
 int abW = g2d.getFontMetrics().stringWidth(abilities);
 g2d.drawString(abilities, (600 - abW) / 2, 393);
 }

 // ============== STORY MODE FUNCTIONS ==============

 void saveStoryProgress() {
 try {
 java.io.PrintWriter writer = new java.io.PrintWriter(storySaveFile);
 writer.println("storyLevel=" + storyModeLevel);
 writer.println("unlockedAbilities=" + String.join(",", unlockedAbilities));
 writer.close();
 } catch (Exception e) {
 System.out.println("Could not save story progress: " + e.getMessage());
 }
 }

 void loadStoryProgress() {
 try {
 java.io.File file = new java.io.File(storySaveFile);
 if (file.exists()) {
 java.util.Scanner scanner = new java.util.Scanner(file);
 while (scanner.hasNextLine()) {
 String line = scanner.nextLine();
 if (line.startsWith("storyLevel=")) {
 storyModeLevel = Integer.parseInt(line.substring(11));
 } else if (line.startsWith("unlockedAbilities=")) {
 String abilities = line.substring(18);
 unlockedAbilities.clear();
 if (!abilities.isEmpty()) {
 for (String ability : abilities.split(",")) {
 if (!ability.trim().isEmpty()) {
 unlockedAbilities.add(ability.trim());
 }
 }
 }
 }
 }
 scanner.close();
 }
 } catch (Exception e) {
 System.out.println("Could not load story progress: " + e.getMessage());
 storyModeLevel = 1;
 unlockedAbilities.clear();
 }
 }

 void initializeStoryMode() {
 loadStoryProgress();

 // Player starts with NO abilities - earn them by defeating bosses!

 storyModeActive = true;
 storyPlayerScore = 0;
 storyBossScore = 0;

 // Show the story map!
 returnToMainMenu();
 }

 void showStoryMap() {
 // Build level list entries for scrollable list
 ArrayList<String> levelEntries = new ArrayList<>();
 int defaultSelection = 0;
 for (int i = 0; i < storyModeMaxLevel; i++) {
 String status;
 if (i < storyModeLevel - 1) {
 status = "[COMPLETE]";
 } else if (i == storyModeLevel - 1) {
 status = ">> CURRENT - Score " + storyLevelScoreToWin[i] + " to win!";
 defaultSelection = i;
 } else {
 status = "[LOCKED]";
 }
 levelEntries.add(String.format("Level %2d: %-20s %s", i + 1, storyBossNames[i], status));
 }

 // Build abilities text
 String abilitiesText;
 if (unlockedAbilities.isEmpty()) {
 abilitiesText = "YOUR ABILITIES: None yet - defeat bosses to earn them!";
 } else {
 abilitiesText = "YOUR ABILITIES: " + String.join(", ", unlockedAbilities);
 }

 // Create scrollable list
 JList<String> levelList = new JList<>(levelEntries.toArray(new String[0]));
 levelList.setFont(new Font("Monospaced", Font.PLAIN, 13));
 levelList.setSelectedIndex(defaultSelection);
 levelList.ensureIndexIsVisible(defaultSelection);
 // Only allow selecting playable levels
 levelList.setSelectionModel(new javax.swing.DefaultListSelectionModel() {
 @Override
 public void setSelectionInterval(int i0, int i1) {
 if (i0 < storyModeLevel) super.setSelectionInterval(i0, i1);
 }
 });

 JScrollPane scrollPane = new JScrollPane(levelList);
 scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));

 // Build panel
 JPanel panel = new JPanel(new java.awt.BorderLayout(5, 5));
 JLabel title = new JLabel("=== STORY MODE - WORLD MAP ===");
 title.setFont(new Font("Arial", Font.BOLD, 16));
 title.setHorizontalAlignment(JLabel.CENTER);
 panel.add(title, java.awt.BorderLayout.NORTH);
 panel.add(scrollPane, java.awt.BorderLayout.CENTER);
 JLabel abilities = new JLabel("<html><b>" + abilitiesText + "</b></html>");
 abilities.setFont(new Font("Arial", Font.PLAIN, 12));
 panel.add(abilities, java.awt.BorderLayout.SOUTH);

 String[] buttons = {"Play Selected Level", "Back to Main Menu"};
 int choice = JOptionPane.showOptionDialog(null,
 panel,
 "STORY MODE MAP",
 JOptionPane.DEFAULT_OPTION,
 JOptionPane.PLAIN_MESSAGE,
 null,
 buttons,
 buttons[0]);

 if (choice != 0) {
 // Back to main menu or closed
 storyModeActive = false;
 return;
 }

 int selectedIndex = levelList.getSelectedIndex();
 if (selectedIndex < 0 || selectedIndex >= storyModeLevel) {
 // Invalid selection, show map again
 returnToMainMenu();
 return;
 }

 int selectedLevel = selectedIndex + 1;
 storyModeLevel = selectedLevel;

 // Show level intro
 String currentBoss = storyBossNames[selectedLevel - 1];
 int scoreNeeded = storyLevelScoreToWin[selectedLevel - 1];

 JOptionPane.showMessageDialog(null,
 "=== LEVEL " + selectedLevel + " ===\n\n" +
 "BOSS: " + currentBoss + "\n" +
 "Score " + scoreNeeded + " points to win!\n\n" +
 "Defeat the boss to earn a new ability!\n\n" +
 "Get ready!",
 "Battle Start!", JOptionPane.INFORMATION_MESSAGE);

 startStoryLevel();
 }

 void startStoryLevel() {
 // Reset scores
 scorePlayer1 = 0;
 scorePlayer2 = 0;
 storyPlayerScore = 0;
 storyBossScore = 0;

 // Clear player abilities and give only unlocked ones
 player1Abilities.clear();
 player2Abilities.clear();
 player1AbilityBranches.clear();
 player2AbilityBranches.clear();

 // Give player their unlocked abilities at level 1
 for (String ability : unlockedAbilities) {
 player1Abilities.put(ability, 1);
 }

 // Boss gets abilities based on level
 configureBossAbilities();

 // Reset ball
 ballX = 295;
 ballY = 200;
 ballVelX = 3;
 ballVelY = 2;
 ballSpeed = 5.0;

 // Set AI difficulty based on story level (ordered weakest to strongest)
 if (storyModeLevel <= 3) aiDifficulty = 1;
 else if (storyModeLevel <= 7) aiDifficulty = 2;
 else if (storyModeLevel <= 14) aiDifficulty = 3;
 else aiDifficulty = 4;
 singlePlayer = true;
 }

 void configureBossAbilities() {
 // Boss gets themed abilities based on level - evolved with branches!
 // Ordered from weakest to strongest
 switch (storyModeLevel) {
 case 1: // Speed Demon
 player2Abilities.put("speed_boost", 3);
 player2AbilityBranches.put("speed_boost", 1); // Sonic - dash
 break;
 case 2: // Point Hunter
 player2Abilities.put("double_points", 3);
 player2AbilityBranches.put("double_points", 1); // Point Leech
 break;
 case 3: // The Wall
 player2Abilities.put("paddle_growth", 3);
 player2AbilityBranches.put("paddle_growth", 1); // Titan Mode
 player2Abilities.put("shield", 2);
 player2Abilities.put("barrier", 3);
 player2AbilityBranches.put("barrier", 1);
 break;
 case 4: // Slow Mo Joe
 player2Abilities.put("slow_opponent", 3);
 player2AbilityBranches.put("slow_opponent", 1); // Freeze Ray
 break;
 case 5: // Gunslinger
 player2Abilities.put("gun", 3);
 player2AbilityBranches.put("gun", 1); // Goku laser
 break;
 case 6: // Glitch Lord
 player2Abilities.put("lag_spike", 3);
 player2AbilityBranches.put("lag_spike", 1); // Quantum Glitch
 break;
 case 7: // The Thief
 player2Abilities.put("ability_stealer", 3);
 player2AbilityBranches.put("ability_stealer", 1); // Hijacker
 player2Abilities.put("ability_swap", 2);
 player2Abilities.put("trap", 3);
 player2AbilityBranches.put("trap", 1);
 break;
 case 8: // Mind Bender
 player2Abilities.put("reverse_controls", 3);
 player2AbilityBranches.put("reverse_controls", 1); // Chaos Engine
 player2Abilities.put("screen_warp", 3);
 player2AbilityBranches.put("screen_warp", 1);
 break;
 case 9: // The Phantom
 player2Abilities.put("blind", 3);
 player2AbilityBranches.put("blind", 1); // Total Darkness
 player2Abilities.put("shrink_opponent", 3);
 player2AbilityBranches.put("shrink_opponent", 1); // Micro Ray
 break;
 case 10: // Ghost
 player2Abilities.put("ghost_ball", 3);
 player2AbilityBranches.put("ghost_ball", 1); // Void Pulse
 break;
 case 11: // Hammer Time
 player2Abilities.put("gravity_hammer", 3);
 player2AbilityBranches.put("gravity_hammer", 1);
 player2Abilities.put("magnet_ball", 3);
 player2AbilityBranches.put("magnet_ball", 1);
 break;
 case 12: // Shadow Ninja
 player2Abilities.put("shadow_clone", 3);
 player2AbilityBranches.put("shadow_clone", 1);
 break;
 case 13: // Portal Master
 player2Abilities.put("portal_pong", 3);
 player2AbilityBranches.put("portal_pong", 1);
 player2Abilities.put("power_siphon", 3);
 player2AbilityBranches.put("power_siphon", 1);
 break;
 case 14: // Time Lord
 player2Abilities.put("time_loop", 3);
 player2AbilityBranches.put("time_loop", 1);
 break;
 case 15: // Haki Master
 player2Abilities.put("haki", 3);
 player2AbilityBranches.put("haki", 1); // Armament Haki
 break;
 case 16: // Bankai Lord
 player2Abilities.put("bankai", 4);
 player2AbilityBranches.put("bankai", 1);
 break;
 case 17: // ULTIMATE CHAMPION - every ability at level 1!
 for (String ability : storyAbilityPool) {
 player2Abilities.put(ability, 1);
 }
 break;
 }
 }

 void handleStoryModeVictory() {
 String currentBoss = storyBossNames[storyModeLevel - 1];

 // Boss reward mapping - player gets ALL boss abilities automatically
 String[][] bossRewards = {
 {"speed_boost"},                          // 1: Speed Demon
 {"double_points"},                        // 2: Point Hunter
 {"paddle_growth", "shield", "barrier"},    // 3: The Wall
 {"slow_opponent"},                        // 4: Slow Mo Joe
 {"gun"},                                  // 5: Gunslinger
 {"lag_spike"},                            // 6: Glitch Lord
 {"ability_stealer", "ability_swap", "trap"}, // 7: The Thief
 {"reverse_controls", "screen_warp"},       // 8: Mind Bender
 {"blind", "shrink_opponent"},              // 9: The Phantom
 {"ghost_ball"},                           // 10: Ghost
 {"gravity_hammer", "magnet_ball"},         // 11: Hammer Time
 {"shadow_clone"},                         // 12: Shadow Ninja
 {"portal_pong", "power_siphon"},           // 13: Portal Master
 {"time_loop"},                            // 14: Time Lord
 {"haki"},                                 // 15: Haki Master
 {"bankai"},                               // 16: Bankai Lord
 {}                                        // 17: ULTIMATE CHAMPION - no reward (final boss)
 };

 // Get rewards for this boss
 String[] rewards = bossRewards[storyModeLevel - 1];

 // Filter out abilities player already has
 ArrayList<String> newAbilities = new ArrayList<>();
 for (String ability : rewards) {
 if (!unlockedAbilities.contains(ability)) {
 unlockedAbilities.add(ability);
 newAbilities.add(ability);
 }
 }

 if (storyModeLevel == storyModeMaxLevel) {
 // ULTIMATE CHAMPION - just victory message
 JOptionPane.showMessageDialog(null,
 "=== LEVEL " + storyModeLevel + " COMPLETE! ===\n" +
 "You defeated " + currentBoss + "!\n\n" +
 "You are the TRUE PONGUS MASTER!",
 "Victory!", JOptionPane.INFORMATION_MESSAGE);
 } else if (newAbilities.size() > 0) {
 // Show all unlocked abilities
 StringBuilder sb = new StringBuilder();
 sb.append("=== LEVEL " + storyModeLevel + " COMPLETE! ===\n");
 sb.append("You defeated " + currentBoss + "!\n\n");
 sb.append("Abilities unlocked:\n");
 for (String ability : newAbilities) {
 sb.append("  + " + ability.toUpperCase().replace("_", " ") + "\n");
 }
 JOptionPane.showMessageDialog(null, sb.toString(),
 "New Abilities!", JOptionPane.INFORMATION_MESSAGE);
 } else {
 // Already have all boss's abilities
 JOptionPane.showMessageDialog(null,
 "=== LEVEL " + storyModeLevel + " COMPLETE! ===\n" +
 "You defeated " + currentBoss + "!\n\n" +
 "You already have this boss's abilities!",
 "Victory!", JOptionPane.INFORMATION_MESSAGE);
 }

 storyModeLevel++;
 saveStoryProgress(); // Auto-save!

 if (storyModeLevel > storyModeMaxLevel) {
 // Beat the game!
 JOptionPane.showMessageDialog(null,
 "=== CONGRATULATIONS! ===\n\n" +
 "You have defeated the ULTIMATE CHAMPION!\n\n" +
 "ALL ABILITIES UNLOCKED!\n\n" +
 "You are the TRUE PONGUS MASTER!",
 "STORY COMPLETE!", JOptionPane.INFORMATION_MESSAGE);
 returnToMainMenu();
 } else {
 returnToMainMenu();
 }
 }

 void handleStoryModeDefeat() {
 String currentBoss = storyBossNames[storyModeLevel - 1];
 JOptionPane.showMessageDialog(null,
 "Defeated by " + currentBoss + "!\n\n" +
 "Return to the map to try again.",
 "Defeat!", JOptionPane.INFORMATION_MESSAGE);
 saveStoryProgress(); // Save progress
 // Return to map
 returnToMainMenu();
 }

 // ============== END STORY MODE FUNCTIONS ==============

 void spawnMapPowerUp() {
 int x = 100 + (int)(Math.random() * 400);
 int y = 50 + (int)(Math.random() * 300);
 // Map power-ups - zigzag excluded (has its own spawn logic)
 double rand = Math.random();
 String type;

 // Zigzag has its own separate spawn system (see zigzagSpawnTimer)
 if (rand < 0.25) {
 type = "fireball";
 } else if (rand < 0.50) {
 type = "split";
 } else if (rand < 0.75) {
 type = "teleport";
 } else if (rand < 0.85) {
 type = "mirror";
 } else if (rand < 0.90) {
 type = "dangerzone";
 } else if (rand < 0.93) {
 type = "invisiblewalls";
 } else if (rand < 0.96) {
 type = "shrinkpaddles";
 } else {
 type = "centerwall";
 }

 powerUps.add(new PowerUp(x, y, type));
 }

 String getAbilityShortName(String ability) {
 switch (ability) {
 case "speed_boost": return "Speed+";
 case "paddle_growth": return "BigPad";
 case "double_points": return "2xPts";
 case "slow_opponent": return "SlowOp";
 case "gun": return "Gun";
 case "ability_stealer": return "Stealer";
 case "lag_spike": return "Lag";
 case "reverse_controls": return "Reverse";
 case "joshua": return "JOSHUA";
 case "jaisan": return "JAISAN";
 case "immunity": return "Immune";
 case "blind": return "Blind";
 case "shrink_opponent": return "Shrink";
 case "ghost_ball": return "Ghost";
 case "ability_swap": return "SWAP";
 case "gravity_hammer": return "Hammer";
 case "magnet_ball": return "Magnet";
 case "shadow_clone": return "Shadow";
 case "portal_pong": return "Portal";
 case "power_siphon": return "Siphon";
 case "time_loop": return "TimeLoop";
 case "haki": return "Haki";
 case "barrier": return "Barrier";
 case "trap": return "Trap";
 case "screen_warp": return "Warp";
 case "bankai": return "Bankai";
 default: return "?";
 }
 }
 
 String getAbilityDescription(String ability) {
 switch (ability) {
 case "speed_boost": return "Speed Boost - +30% speed per level (max 60% at Lv2), then +15% dodge chance per level [COMMON]";
 case "paddle_growth": return "Paddle Growth - +20% size per level [COMMON]";
 case "double_points": return "Double Points - Score multiplier [RARE 10%]";
 case "slow_opponent": return "Slow Opponent - Enemy slower (RARE 12% - devastating at Lv3!)";
 case "gun": return "Gun - Shoot every 1s (stuns longer & shotgun spread per level) (RARE 10%)";
 case "ability_stealer": return "Ability Stealer - Shoot every 5s! Each hit removes 1 ability (UNCOMMON 30%)";
 case "lag_spike": return "Lag Spike - Opponent's paddle teleports randomly for 1s after 6s cooldown (-0.2s cooldown per level) (RARE 10%)";
 case "reverse_controls": return "Reverse Controls - Reverse opponent every 5s for 1s (+0.2s per level) (RARE 15%)";
 case "joshua": return "JOSHUA - GOD MODE: Ultra speed, giant paddle, auto-deflect (ULTRA RARE 1% +2s per level)";
 case "jaisan": return "JAISAN - BEYOND GOD: All JOSHUA powers + invincibility + reality warping (LEGENDARY 0.1% +3s per level)";
 case "immunity": return "Immunity - Reduces duration of stun/debuffs by 20% per level (max 60% reduction) [COMMON]";
 case "blind": return "Blind - Makes opponent's paddle nearly invisible every 6s for 3s (+0.3s per level) (UNCOMMON 35%)";
 case "shrink_opponent": return "Shrink Opponent - Enemy paddle shrinks every 4s for 1.5s (+0.3s per level) (UNCOMMON 35%)";
 case "ghost_ball": return "Ghost Ball - Ball phases through opponent paddle every 10s for 2.5s (VERY RARE 5% +0.2s per level)";
 case "ability_swap": return "Ability Swap - SWAP all your abilities with opponent! Strategic chaos (ONLY from random button, 5% higher chance)";
 case "gravity_hammer": return "Gravity Hammer - Press Q/Slash to slam ball downward! Cooldown 4s (RARE 10%)";
 case "magnet_ball": return "Magnet Ball - Ball attracted to your paddle (RARE 10% +stronger per level)";
 case "shadow_clone": return "Shadow Clone - Independent ghost paddle with bad AI\n" +
 "(Now 15% - Less Rare!)\n" +
 "Lv1-2: 1 independent shadow with poor positioning\n" +
 "Lv3+: Choose evolution branch!";
 case "portal_pong": return "Portal Pong - Press Q/Slash twice to place 2 portals! " +
 "Ball teleports BOTH WAYS between portals with SPEED BOOST!\n" +
 "Lv1: +2 speed boost\n" +
 "Lv2: +3 speed, angle toward opponent\n" +
 "Lv3+: +4 speed, aggressive angles, longer duration! (RARE 8%)";
 case "power_siphon": return "Power Siphon - ABSORB opponent's energy!\n" +
 "Passive: Hitting ball weakens opponent's abilities\n" +
 "Active: Press Q/Slash to unleash devastating energy burst! (RARE 10%)\n" +
 "Lv1: Small drain | Lv2: Moderate drain | Lv3+: Choose evolution!";
 case "time_loop": return "Time Loop - Press Q/Slash to rewind time! 0.3s+0.2s/lvl rewind, 20s-0.3s/lvl cooldown. Triggers slow-mo! (VERY RARE 5%)";
 case "haki": return "HAKI - Conqueror's spirit! Press Q/Slash to unleash!\n" +
 "Base: Stun opponent with Conqueror's Haki (0.8s stun)\n" +
 "Lv3+ Branch 1: ARMAMENT - Black paddle, +50% hit power!\n" +
 "Lv3+ Branch 2: OBSERVATION - See ball trajectory, brief slow-mo! (RARE 8%)";
 case "barrier": return "BARRIER - Summon a defensive shield!\n" +
 "Base: Create barrier that blocks ball once (8s cooldown)\n" +
 "Lv3+ Branch 1: MIRROR SHIELD - Reflects ball at 2x speed!\n" +
 "Lv3+ Branch 2: ABSORPTION - Catch ball and relaunch! (RARE 10%)";
 case "trap": return "TRAP - Set deadly traps on the field!\n" +
 "Base: BIG trap stuns 1.2s + redirects ball at opponent! (3.5s cooldown)\n" +
 "Lv3+ Branch 1: STICKY TRAP - Ball FREEZES 2.5s + 3s reverse controls!\n" +
 "Lv3+ Branch 2: EXPLOSIVE TRAP - 1.5s STUN + 2.5s shrink + 2.5x speed! (RARE 12%)";
 case "screen_warp": return "SCREEN WARP - Reverse opponent's controls!\n" +
 "Base: REVERSE opponent controls for 2s (9s cooldown)\n" +
 "Lv3+ Branch 1: INVERSION - Reverse controls + paddle jitter!\n" +
 "Lv3+ Branch 2: TUNNEL VISION - Ball becomes INVISIBLE near edges! (VERY RARE 5%)";
 case "bankai": return "BANKAI - ULTIMATE POWER! BLACK PADDLE MODE!\n" +
 "Activate: Enter Bankai (black paddle) + PERMANENT stat boosts!\n" +
 "During Bankai: Press ability to SWING ZANGETSU - 5x ball speed!\n" +
 "Every 3 swings: Fire GETSUGA TENSHO projectile at opponent!\n" +
 "Lv3+ Branch 1: ZANGETSU - 7x ball speed + bigger Getsuga!\n" +
 "Lv3+ Branch 2: HOLLOW FORM - Ball phases through once! (ULTRA RARE 2%)";
 default: return "Unknown";
 }
 }

 double getPlayerSpeed(int player) {
 double speed = 5.0;
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> opponentAbilities = (player == 1) ? player2Abilities : player1Abilities;

 // BANKAI PERMANENT SPEED BONUS - stacks forever!
 int permanentSpeedBonus = (player == 1) ? player1PermanentSpeedBonus : player2PermanentSpeedBonus;
 speed += permanentSpeedBonus;
 
 // Check if opponent has JAISAN (to suppress JOSHUA)
 boolean opponentJaisanActive = (player == 1) ? player2JaisanActive : player1JaisanActive;
 
 // JOSHUA mode - god-tier speed (but suppressed by opponent's JAISAN!)
 boolean joshuaActive = (player == 1) ? player1JoshuaActive : player2JoshuaActive;
 if (joshuaActive && !opponentJaisanActive) {
 speed *= 5.0; // 5x speed when JOSHUA active
 } else if (joshuaActive && opponentJaisanActive) {
 speed *= 0.5; // JOSHUA becomes PATHETIC against JAISAN - half speed!
 }
 
 // JAISAN mode - BEYOND god-tier speed (DOMINATES JOSHUA!)
 boolean jaisanActive = (player == 1) ? player1JaisanActive : player2JaisanActive;
 if (jaisanActive) speed *= 10.0; // 10x speed when JAISAN active! (was 8x, now even more OP)
 
 // Speed boost - capped at 60% (2 levels = 60%)
 int speedLevel = getEffectiveAbilityLevel(player, "speed_boost");
 boolean hasSpeedBoost = speedLevel > 0;
 if (hasSpeedBoost) {
 double speedBonus = Math.min(0.6, 0.3 * speedLevel); // Cap at 60% (0.6)
 speed *= (1.0 + speedBonus);
 }
 
 // Slow opponent effect - NEGATED if player has speed boost active!
 int slowLevel = getEffectiveAbilityLevel(player == 1 ? 2 : 1, "slow_opponent");
 if (slowLevel > 0 && !joshuaActive && !jaisanActive && !hasSpeedBoost) {
 speed *= Math.pow(0.7, slowLevel); // Immune to slow when JOSHUA/JAISAN/SPEED_BOOST active
 }
 
 return speed;
 }
 
 // Get effective ability level - includes hijacked abilities and checks Virus disable
 int getEffectiveAbilityLevel(int player, String abilityName) {
 // Check if abilities are disabled by Overload (Power Siphon Branch 2)
 int overloadDisableTimer = (player == 1) ? player1OverloadDisableTimer : player2OverloadDisableTimer;
 if (overloadDisableTimer > 0) {
 return 0; // ALL abilities disabled by Overload!
 }
 
 // Check if abilities are disabled by opponent's Virus
 ArrayList<String> disabledList = (player == 1) ? player1DisabledAbilities : player2DisabledAbilities;
 if (disabledList.contains(abilityName)) {
 return 0; // This ability is disabled by Virus!
 }
 
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 
 // Check if player has hijacked this ability
 String hijackedAbility = (player == 1) ? player1HijackedAbility : player2HijackedAbility;
 int hijackedDuration = (player == 1) ? player1HijackedDuration : player2HijackedDuration;
 
 // Check if player has copied this ability (Soul Reaper)
 String copiedAbility = (player == 1) ? player1CopiedAbility : player2CopiedAbility;
 int copiedDuration = (player == 1) ? player1CopiedAbilityDuration : player2CopiedAbilityDuration;
 
 int regularLevel = abilities.getOrDefault(abilityName, 0);
 int hijackedLevel = 0;
 int copiedLevel = 0;
 
 if (hijackedAbility != null && hijackedAbility.equals(abilityName) && hijackedDuration > 0) {
 // Using opponent's hijacked ability
 HashMap<String, Integer> opponentAbilities = (player == 1) ? player2Abilities : player1Abilities;
 hijackedLevel = opponentAbilities.getOrDefault(abilityName, 0);
 }
 
 if (copiedAbility != null && copiedAbility.equals(abilityName) && copiedDuration > 0) {
 // Using opponent's copied ability (Soul Reaper)
 HashMap<String, Integer> opponentAbilities = (player == 1) ? player2Abilities : player1Abilities;
 copiedLevel = opponentAbilities.getOrDefault(abilityName, 0);
 }
 
 return Math.max(Math.max(regularLevel, hijackedLevel), copiedLevel);
 }
 
 // Get dodge chance from speed boost levels beyond level 2
 double getDodgeChance(int player) {
 int speedLevel = getEffectiveAbilityLevel(player, "speed_boost");
 
 // After level 2 (60% speed cap), each level gives +15% dodge chance
 if (speedLevel > 2) {
 return Math.min(0.75, (speedLevel - 2) * 0.15); // Max 75% dodge at level 7
 }
 return 0.0;
 }
 
 // Get dash cooldown - decreases by 0.3 seconds (30 frames) per level after level 3
 int getDashCooldown(int player) {
 int speedLevel = getEffectiveAbilityLevel(player, "speed_boost");
 int baseCooldown = 300; // 3 seconds base cooldown
 
 // Level 3 = base cooldown, each level after reduces by 30 frames (0.3 seconds)
 if (speedLevel > 3) {
 int reduction = (speedLevel - 3) * 30; // 30 frames = 0.3 seconds
 return Math.max(30, baseCooldown - reduction); // Minimum 0.3 second cooldown
 }
 return baseCooldown;
 }
 
 // Auto-level evolved abilities every 20 points
 void checkEvolvedAbilityLevelUps(int player, int pointsGained) {
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (player == 1) ? player1AbilityBranches : player2AbilityBranches;
 HashMap<String, Integer> evolvedPoints = (player == 1) ? player1EvolvedAbilityPoints : player2EvolvedAbilityPoints;
 String playerName = (player == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 
 // Check all abilities that have been evolved (level 3+)
 for (String ability : abilities.keySet()) {
 int level = abilities.get(ability);
 int branch = branches.getOrDefault(ability, 0);
 
 // Only auto-level abilities that are evolved (level 3+) and have a branch
 if (level >= 3 && branch > 0) {
 // Initialize points tracker if needed
 if (!evolvedPoints.containsKey(ability)) {
 evolvedPoints.put(ability, 0);
 }
 
 // Add points
 evolvedPoints.put(ability, evolvedPoints.get(ability) + pointsGained);
 
 // Determine points needed for level up
 int pointsNeeded = 20; // Default: 20 points
 
 // SPECIAL CASE: Shadow Clone Branch 1 (Multi Shadow Clone) levels every 6 points!
 if (ability.equals("shadow_clone") && branch == 1) {
 pointsNeeded = 6;
 }
 // SPECIAL CASE: Shadow Clone Branch 2 (Afterimage Defense) levels every 15 points!
 else if (ability.equals("shadow_clone") && branch == 2) {
 pointsNeeded = 15;
 }
 
 // Check if ready to level up
 if (evolvedPoints.get(ability) >= pointsNeeded) {
 abilities.put(ability, level + 1);
 evolvedPoints.put(ability, 0);
 
 // No popup - just silently level up to avoid gameplay interruption
 // Players can see their level in the ability display
 }
 }
 }
 }
 
 // Predict where ball will be when it reaches a target X position
 int predictBallYPosition(int currentBallX, int currentBallY, int currentVelX, int currentVelY, int targetX) {
 // If ball is moving away from target, return current position
 if ((targetX < currentBallX && currentVelX > 0) || (targetX > currentBallX && currentVelX < 0)) {
 return currentBallY;
 }
 
 // Calculate how many frames until ball reaches target X
 if (currentVelX == 0) return currentBallY; // Avoid division by zero
 
 int framesUntilTarget = Math.abs((targetX - currentBallX) / currentVelX);
 
 // Simulate ball movement accounting for wall bounces
 int simulatedY = currentBallY;
 int simulatedVelY = currentVelY;
 
 for (int i = 0; i < framesUntilTarget; i++) {
 simulatedY += simulatedVelY;
 
 // Check for top/bottom wall bounces
 if (simulatedY <= 7) {
 simulatedY = 7;
 simulatedVelY = Math.abs(simulatedVelY);
 } else if (simulatedY >= 393) {
 simulatedY = 393;
 simulatedVelY = -Math.abs(simulatedVelY);
 }
 }
 
 return simulatedY;
 }
 
 int getPaddleHeight(int player, boolean shrunk) {
 int baseHeight = shrunk ? 30 : 60;
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (player == 1) ? player1AbilityBranches : player2AbilityBranches;

 // BANKAI PERMANENT PADDLE BONUS - stacks forever!
 int permanentPaddleBonus = (player == 1) ? player1PermanentPaddleBonus : player2PermanentPaddleBonus;
 baseHeight += permanentPaddleBonus;

 // Check if opponent has JAISAN (to suppress JOSHUA)
 boolean opponentJaisanActive = (player == 1) ? player2JaisanActive : player1JaisanActive;
 
 // JOSHUA mode - massive paddle (but pathetic against JAISAN!)
 boolean joshuaActive = (player == 1) ? player1JoshuaActive : player2JoshuaActive;
 if (joshuaActive && !opponentJaisanActive) {
 baseHeight = 200; // Huge paddle when JOSHUA active
 } else if (joshuaActive && opponentJaisanActive) {
 baseHeight = 40; // JOSHUA becomes TINY and PATHETIC against JAISAN!
 }
 
 // JAISAN mode - DOMINATES EVERYTHING!
 boolean jaisanActive = (player == 1) ? player1JaisanActive : player2JaisanActive;
 if (jaisanActive) baseHeight = 350; // MASSIVE paddle when JAISAN active! (was 300, now even bigger)

 // BANKAI - Zangetsu mode - 2x paddle size
 boolean zangetsuActive = (player == 1) ? player1ZangetsuActive : player2ZangetsuActive;
 if (zangetsuActive && !jaisanActive && !joshuaActive) {
 baseHeight = baseHeight * 2; // Double paddle size during Zangetsu
 }

 // Shrink Opponent effect (doesn't affect JOSHUA or JAISAN)
 boolean shrinkActive = (player == 1) ? player1ShrinkEffectTimer > 0 : player2ShrinkEffectTimer > 0;
 if (shrinkActive && !joshuaActive && !jaisanActive) baseHeight = 20; // Tiny paddle when shrunk
 
 // Overload Shrink effect (from Power Siphon Branch 2)
 boolean overloadShrink = (player == 1) ? player1OverloadShrinkTimer > 0 : player2OverloadShrinkTimer > 0;
 if (overloadShrink && !joshuaActive && !jaisanActive) baseHeight = (int)(baseHeight * 0.5); // 50% size
 
 int growthLevel = getEffectiveAbilityLevel(player, "paddle_growth");
 if (growthLevel > 0 && !joshuaActive && !jaisanActive && !shrinkActive) {
 baseHeight = (int)(baseHeight * (1.0 + 0.2 * growthLevel));
 
 // Elastic Expansion (paddle_growth branch 2) - temporarily stretch larger when ball nearby
 if (growthLevel >= 3 && branches.getOrDefault("paddle_growth", 0) == 2) {
 int ballCenterY = ballY + 7;
 int paddleCenterY = (player == 1 ? paddle1Y : paddle2Y) + baseHeight / 2;
 double distance = Math.abs(ballCenterY - paddleCenterY);
 
 // If ball is within 50 pixels vertically and coming toward player
 boolean ballComingToward = (player == 1 && ballVelX < 0 && ballX < 300) || 
 (player == 2 && ballVelX > 0 && ballX > 300);
 if (distance < 50 && ballComingToward) {
 // Expand by 100%!
 baseHeight = (int)(baseHeight * 2.0);
 }
 }
 }
 return baseHeight;
 }

 void offerAbilityChoice(int player) {
 offerAbilityChoice(player, 1);
 }
 
 void offerAbilityChoice(int player, int abilityLevel) {
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (player == 1) ? player1AbilityBranches : player2AbilityBranches;
 String playerName = (player == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 
 // Check if player has any abilities at level 2 that can branch
 ArrayList<String> branchableAbilities = new ArrayList<>();
 for (String ability : abilities.keySet()) {
 if (abilities.get(ability) == 2 && branches.getOrDefault(ability, 0) == 0) {
 branchableAbilities.add(ability);
 }
 }
 
 // If player has branchable abilities, offer branch choice
 if (!branchableAbilities.isEmpty() && Math.random() < 0.7) { // 70% chance to offer branch
 String ability = branchableAbilities.get((int)(Math.random() * branchableAbilities.size()));
 
 if (player == 2 && singlePlayer) {
 // AI randomly chooses branch
 int branch = (Math.random() < 0.5) ? 1 : 2;
 branches.put(ability, branch);
 abilities.put(ability, 3);
 JOptionPane.showMessageDialog(null, 
 " AI: " + getAbilityShortName(ability) + " " + getBranchName(ability, branch) + " ", 
 "AI Branch Evolution!", 
 JOptionPane.INFORMATION_MESSAGE);
 return;
 }
 
 // Player branch choice
 String[] branchOptions = {getBranch1Description(ability), getBranch2Description(ability)};
 int branchChoice = JOptionPane.showOptionDialog(null,
 playerName + " - " + getAbilityShortName(ability).toUpperCase() + " EVOLUTION!\n\n" +
 "Your " + getAbilityShortName(ability) + " ability has reached Level 3!\n" +
 "Choose an evolution path:\n\n" +
 " BRANCH 1: " + getBranchName(ability, 1) + "\n" +
 getBranch1Description(ability) + "\n\n" +
 " BRANCH 2: " + getBranchName(ability, 2) + "\n" +
 getBranch2Description(ability),
 " ABILITY EVOLUTION ",
 JOptionPane.DEFAULT_OPTION,
 JOptionPane.QUESTION_MESSAGE,
 null,
 new String[]{" Choose Branch 1", " Choose Branch 2"},
 null);
 
 if (branchChoice >= 0) {
 int selectedBranch = branchChoice + 1;
 branches.put(ability, selectedBranch);
 abilities.put(ability, 3);
 JOptionPane.showMessageDialog(null,
 " " + playerName + ": " + getAbilityShortName(ability) + " evolved into " + 
 getBranchName(ability, selectedBranch) + "! ",
 "Evolution Complete!",
 JOptionPane.INFORMATION_MESSAGE);
 }
 return;
 }
 
 // Regular ability choice (levels 1-2 or level 3+ after branching)
 ArrayList<String> available = new ArrayList<>();
 for (String ability : allAbilities) {
 // Skip abilities that are at level 2 and haven't branched yet
 if (abilities.getOrDefault(ability, 0) == 2 && branches.getOrDefault(ability, 0) == 0) {
 continue; // This ability needs to branch before leveling more
 }
 
 // Ability Swap - NEVER appears in regular selection, only through random choice
 if (ability.equals("ability_swap")) {
 continue; // Skip - only available via random button
 }

 // STORY MODE - Exclude joshua and jaisan entirely
 if (storyModeActive && (ability.equals("joshua") || ability.equals("jaisan"))) {
 continue; // Not available in story mode
 }

 // RARITY SYSTEM - Each ability has a chance to appear
 // LEGENDARY (0.1%): jaisan
 // ULTRA RARE (1-2%): joshua, bankai
 // VERY RARE (5%): ghost_ball, screen_warp
 // RARE (8-15%): gun, reverse_controls, slow_opponent, double_points, lag_spike, gravity_hammer, magnet_ball, shadow_clone, portal_pong, power_siphon, haki, barrier, trap, time_loop
 // UNCOMMON (30-35%): ability_stealer, blind
 // COMMON (100%): speed_boost, paddle_growth
 
 double chance = 1.0; // Default: 100% (common)
 
 // LEGENDARY - 0.1%
 if (ability.equals("jaisan")) {
 chance = 0.001;
 }
 // ULTRA RARE - 1%
 else if (ability.equals("joshua")) {
 chance = 0.01;
 }
 // VERY RARE - 5%
 else if (ability.equals("ghost_ball")) {
 chance = 0.05;
 }
 // RARE - 10-15%
 else if (ability.equals("gun")) {
 chance = 0.10; // 10%
 }
 else if (ability.equals("slow_opponent")) {
 chance = 0.12; // 12% - RARE because devastating at level 3
 }
 else if (ability.equals("reverse_controls")) {
 chance = 0.15; // 15%
 }
 else if (ability.equals("double_points")) {
 chance = 0.10; // 10% - RARE
 }
 // UNCOMMON - 30-35%
 else if (ability.equals("ability_stealer")) {
 chance = 0.30; // 30%
 }
 else if (ability.equals("lag_spike")) {
 chance = 0.10; // 10% - RARE
 }
 else if (ability.equals("blind")) {
 chance = 0.35; // 35%
 }
 else if (ability.equals("shrink_opponent")) {
 chance = 0.35; // 35%
 }
 // NEW ABILITIES
 // RARE 10%
 else if (ability.equals("gravity_hammer")) {
 chance = 0.10; // 10% RARE
 }
 else if (ability.equals("magnet_ball")) {
 chance = 0.10; // 10% RARE
 }
 else if (ability.equals("shadow_clone")) {
 chance = 0.15; // 15% - Made less rare (was 10%)
 }
 // RARE 5-10%
 else if (ability.equals("portal_pong")) {
 chance = 0.08; // 8% RARE
 }
 else if (ability.equals("power_siphon")) {
 chance = 0.10; // 10% RARE
 }
 else if (ability.equals("time_loop")) {
 chance = 0.20; // 20% - Made less rare (was 5%)
 }
 // NEW ANIME & CHAOS ABILITIES
 else if (ability.equals("haki")) {
 chance = 0.08; // 8% RARE - One Piece themed
 }
 else if (ability.equals("barrier")) {
 chance = 0.10; // 10% RARE - Defensive
 }
 else if (ability.equals("trap")) {
 chance = 0.12; // 12% RARE - Offensive
 }
 else if (ability.equals("screen_warp")) {
 chance = 0.05; // 5% VERY RARE - Chaos
 }
 else if (ability.equals("bankai")) {
 chance = 0.02; // 2% ULTRA RARE - Bleach themed
 }
 // COMMON - 100% (always appear)
 // speed_boost, paddle_growth
 
 // BOOST CHANCE: If player already has this ability, make it 3x more likely to appear
 if (abilities.containsKey(ability) && abilities.get(ability) > 0) {
 chance = Math.min(1.0, chance * 3.0); // 3x more likely, capped at 100%
 }
 
 if (Math.random() < chance) {
 available.add(ability);
 }
 }
 
 if (available.isEmpty()) {
 available.add("speed_boost"); // Fallback
 }
 
 if (player == 2 && singlePlayer) {
 int randomIndex = (int)(Math.random() * available.size());
 String chosenAbility = available.get(randomIndex);
 int currentLevel = abilities.getOrDefault(chosenAbility, 0);
 
 // If it's a comeback bonus (abilityLevel > 1) and they don't have the ability, set it to that level
 // Otherwise, increment by 1
 if (abilityLevel > 1 && currentLevel == 0) {
 abilities.put(chosenAbility, abilityLevel);
 } else {
 abilities.put(chosenAbility, currentLevel + 1);
 }
 
 if (chosenAbility.equals("gun") && currentLevel == 0) player2GunTimer = gunCooldown;
 if (chosenAbility.equals("ability_stealer") && currentLevel == 0) player2StealerTimer = stealerCooldown;
 
 String levelText = (abilityLevel > 1 && currentLevel == 0) ? " at Lv" + abilityLevel : (currentLevel > 0 ? " Lv" + (currentLevel + 1) : "");
 JOptionPane.showMessageDialog(null, " AI: " + getAbilityDescriptionWithBranch(chosenAbility, branches.getOrDefault(chosenAbility, 0)) + levelText + " ", "AI Leveled Up!", JOptionPane.INFORMATION_MESSAGE);
 return;
 }
 
 ArrayList<String> selectedAbilities = new ArrayList<>();
 int numToSelect = Math.min(3, available.size()); // Don't try to select more than available
 for (int i = 0; i < numToSelect && !available.isEmpty(); i++) {
 int randomIndex = (int)(Math.random() * available.size());
 selectedAbilities.add(available.get(randomIndex));
 available.remove(randomIndex);
 }
 
 // If we still don't have enough, add common abilities to fill
 while (selectedAbilities.size() < 3) {
 selectedAbilities.add("speed_boost");
 }
 
 StringBuilder message = new StringBuilder();
 if (abilityLevel > 1) {
 message.append(playerName).append(" - COMEBACK BONUS!\n\n");
 message.append("Choose a Level ").append(abilityLevel).append(" ability:\n\n");
 } else {
 message.append(playerName).append(" LEVELED UP!\n\nChoose ability:\n\n");
 }
 
 for (int i = 0; i < selectedAbilities.size(); i++) {
 String ability = selectedAbilities.get(i);
 int currentLevel = abilities.getOrDefault(ability, 0);
 int branch = branches.getOrDefault(ability, 0);
 message.append((i + 1)).append(". ").append(getAbilityDescriptionWithBranch(ability, branch));
 if (abilityLevel > 1) {
 message.append(" [NEW at Lv").append(abilityLevel).append("]");
 } else if (currentLevel > 0) {
 message.append(" [Lv").append(currentLevel).append(" ").append(currentLevel + 1).append("]");
 if (branch > 0) {
 message.append(" [").append(getBranchName(ability, branch)).append("]");
 }
 }
 message.append("\n\n");
 }
 
 message.append("4. RANDOM ABILITY (Equal chance for ALL abilities including JOSHUA!)");
 
 String[] options = new String[4];
 for (int i = 0; i < 3; i++) options[i] = "Choose #" + (i + 1);
 options[3] = " Random!";
 int choice = JOptionPane.showOptionDialog(null, message.toString(), abilityLevel > 1 ? " COMEBACK BONUS " : " LEVEL UP ", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
 if (choice >= 0 && choice <= 3) {
 String chosenAbility;
 
 // If random was chosen (option 4), pick completely random from all abilities
 if (choice == 3) {
 // Create list of ALL abilities with equal chance (no rarity filtering)
 ArrayList<String> allAvailableAbilities = new ArrayList<>();
 for (String ability : allAbilities) {
 // Skip abilities that are at level 2 and haven't branched yet
 if (abilities.getOrDefault(ability, 0) == 2 && branches.getOrDefault(ability, 0) == 0) {
 continue;
 }
 allAvailableAbilities.add(ability);
 }
 
 if (allAvailableAbilities.isEmpty()) {
 allAvailableAbilities.add("speed_boost"); // Fallback
 }
 
 // Weighted random selection
 // ability_swap has 5% higher chance than other abilities
 ArrayList<String> weightedList = new ArrayList<>();
 for (String ability : allAvailableAbilities) {
 if (ability.equals("jaisan")) {
 // Add Jaisan only once (lowest weight)
 weightedList.add(ability);
 } else if (ability.equals("ability_swap")) {
 // Add ability_swap 25 times (5% higher than normal abilities which get 20)
 for (int i = 0; i < 25; i++) {
 weightedList.add(ability);
 }
 } else {
 // Add other abilities 20 times each
 for (int i = 0; i < 20; i++) {
 weightedList.add(ability);
 }
 }
 }
 
 if (weightedList.isEmpty()) {
 weightedList.add("speed_boost"); // Ultimate fallback
 }
 
 chosenAbility = weightedList.get((int)(Math.random() * weightedList.size()));
 
 // Show what was randomly selected
 JOptionPane.showMessageDialog(null, 
 " Random selection picked:\n" + getAbilityShortName(chosenAbility) + "!", 
 "Random Result!", 
 JOptionPane.INFORMATION_MESSAGE);
 } else {
 // Ensure choice is within bounds
 if (choice < selectedAbilities.size()) {
 chosenAbility = selectedAbilities.get(choice);
 } else {
 chosenAbility = "speed_boost"; // Fallback
 }
 }
 
 int currentLevel = abilities.getOrDefault(chosenAbility, 0);
 
 // Ability Swap - IMMEDIATELY swap ALL abilities between players!
 // Handle this BEFORE adding ability_swap to current player's abilities
 if (chosenAbility.equals("ability_swap")) {
 // Create copies of current abilities BEFORE any modifications
 HashMap<String, Integer> player1AbilitiesCopy = new HashMap<>(player1Abilities);
 HashMap<String, Integer> player1BranchesCopy = new HashMap<>(player1AbilityBranches);
 HashMap<String, Integer> player2AbilitiesCopy = new HashMap<>(player2Abilities);
 HashMap<String, Integer> player2BranchesCopy = new HashMap<>(player2AbilityBranches);
 
 // Swap abilities
 player1Abilities.clear();
 player1Abilities.putAll(player2AbilitiesCopy);
 player1AbilityBranches.clear();
 player1AbilityBranches.putAll(player2BranchesCopy);
 
 player2Abilities.clear();
 player2Abilities.putAll(player1AbilitiesCopy);
 player2AbilityBranches.clear();
 player2AbilityBranches.putAll(player1BranchesCopy);
 
 String swapperName = (player == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 String opponentName = (player == 1) ? (singlePlayer ? "AI" : "Player 2") : "Player 1";
 
 JOptionPane.showMessageDialog(null, 
 " ABILITY SWAP ACTIVATED! \n\n" +
 swapperName + " and " + opponentName + " have SWAPPED all their abilities!\n" +
 "Strategic chaos!", 
 " ABILITY SWAP! ", 
 JOptionPane.WARNING_MESSAGE);
 return; // Don't add ability_swap to abilities or show normal level up message
 }
 
 // For all other abilities, add them normally
 // If it's a comeback bonus (abilityLevel > 1) and they don't have the ability, set it to that level
 // Otherwise, increment by 1
 if (abilityLevel > 1 && currentLevel == 0) {
 abilities.put(chosenAbility, abilityLevel);
 } else {
 abilities.put(chosenAbility, currentLevel + 1);
 }
 
 if (chosenAbility.equals("gun") && currentLevel == 0) {
 if (player == 1) player1GunTimer = gunCooldown;
 else player2GunTimer = gunCooldown;
 }
 if (chosenAbility.equals("ability_stealer") && currentLevel == 0) {
 if (player == 1) player1StealerTimer = stealerCooldown;
 else player2StealerTimer = stealerCooldown;
 }
 
 int branch = branches.getOrDefault(chosenAbility, 0);
 String levelText = (abilityLevel > 1 && currentLevel == 0) ? " at Lv" + abilityLevel : (currentLevel > 0 ? " Lv" + (currentLevel + 1) : "");
 String branchText = (branch > 0) ? " [" + getBranchName(chosenAbility, branch) + "]" : "";
 JOptionPane.showMessageDialog(null, " " + playerName + ": " + getAbilityDescriptionWithBranch(chosenAbility, branch) + levelText + branchText + " ", "Unlocked!", JOptionPane.INFORMATION_MESSAGE);
 }
 }
 
 String getBranchName(String ability, int branch) {
 if (branch == 1) {
 switch (ability) {
 case "gun": return "Goku";
 case "speed_boost": return "Sonic";
 case "shield": return "Fortress";
 case "ball_control": return "Sniper";
 case "power_magnet": return "Black Hole";
 case "sticky_paddle": return "Glue Trap";
 case "slow_opponent": return "Freeze Ray";
 case "double_points": return "Point Leech";
 case "ability_stealer": return "Frieza";
 case "ghost_ball": return "Void Pulse";
 case "joshua": return "Miles";
 case "jaisan": return "JVsayanMUI";
 case "lag_spike": return "Quantum Glitch";
 case "reverse_controls": return "Chaos Engine";
 case "immunity": return "Fortress Shield";
 case "blind": return "Total Darkness";
 case "shrink_opponent": return "Micro Ray";
 case "paddle_growth": return "Titan Mode";
 case "power_boost": return "Power Amplifier";
 case "shadow_clone": return "Multi Shadow Clone";
 case "gravity_hammer": return "Thor's Hammer";
 case "magnet_ball": return "Graviton Sphere";
 case "portal_pong": return "Wormhole Master";
 case "power_siphon": return "Soul Reaper";
 case "time_loop": return "Chronos Rewind";
 case "haki": return "Armament Haki";
 case "barrier": return "Mirror Shield";
 case "trap": return "Sticky Trap";
 case "screen_warp": return "Full Inversion";
 case "bankai": return "Zangetsu";
 default: return "Branch 1";
 }
 } else {
 switch (ability) {
 case "gun": return "Vegeta";
 case "speed_boost": return "Flash";
 case "shield": return "Reflector";
 case "ball_control": return "Hacker";
 case "power_magnet": return "Collector";
 case "sticky_paddle": return "Sludge Zone";
 case "slow_opponent": return "Gravity Well";
 case "double_points": return "Combo Master";
 case "ability_stealer": return "Jiren";
 case "ghost_ball": return "Reality Break";
 case "joshua": return "Hosh Josh";
 case "jaisan": return "KVsayanMUI";
 case "lag_spike": return "Time Distortion";
 case "reverse_controls": return "Puppet Master";
 case "immunity": return "Adaptive Armor";
 case "blind": return "Sensory Overload";
 case "shrink_opponent": return "Compression Field";
 case "paddle_growth": return "Elastic Expansion";
 case "power_boost": return "Power Fusion";
 case "shadow_clone": return "Afterimage Defense";
 case "gravity_hammer": return "Gravity Well";
 case "magnet_ball": return "Force Field";
 case "portal_pong": return "Dimensional Rift";
 case "power_siphon": return "Overload";
 case "time_loop": return "Temporal Echo";
 case "haki": return "Observation Haki";
 case "barrier": return "Absorption Shield";
 case "trap": return "Explosive Trap";
 case "screen_warp": return "Tunnel Vision";
 case "bankai": return "Hollow Form";
 default: return "Branch 2";
 }
 }
 }
 
 String getBranch1Description(String ability) {
 switch (ability) {
 case "gun": return "GOKU - Shoots a laser beam instead of bullets. Laser lasts 0.5s base (+0.5s per level). Continuously stuns opponents while active. Stun duration increases with level.";
 case "speed_boost": return "SONIC - Keeps your +30% speed bonus. Press Q to INSTANTLY dash your paddle! Uses PREDICTIVE AI to calculate where the ball will be, accounting for wall bounces. If you hit the ball while dashing, ball speed DOUBLES! Perfect for clutch saves and devastating counterattacks. Cooldown: 3 seconds.";
 case "shield": return "FORTRESS - Each level adds more shield layers, allowing you to block more goals before the shield breaks. Stack multiple shields for ultimate defense!";
 case "ball_control": return "SNIPER - Gain precise control over ball angles when hitting. Better targeting for trick shots and strategic positioning.";
 case "power_magnet": return "BLACK HOLE - Instead of attracting nearby power-ups, instantly absorbs ALL power-ups currently on the field! Ultimate power-up control.";
 case "sticky_paddle": return "GLUE TRAP - Ball sticks to your paddle for a longer duration, giving you more time to aim your shots and control the game pace.";
 case "slow_opponent": return "FREEZE RAY - Completely freezes opponent's paddle for 1.5 seconds every 5 seconds! They can't move at all during freeze. [BUFFED!]";
 case "double_points": return "POINT LEECH - Score multiplier stays. Additionally, steals 1 point from opponent every time YOU score! Turn their points into yours.";
 case "ability_stealer": return "FRIEZA - Shoots a purple death beam laser (same mechanics as Goku). Cooldown: 7s base, -0.4s per level. Laser duration: 0.2s base, +0.2s per level. Laser gets marginally thicker per level. When laser hits opponent, steals abilities based on level: Level 3 = 1 ability, Level 4+ = 2 abilities. Continuous ability drain!";
 case "ghost_ball": return "VOID PULSE - Ball keeps phasing through paddle. NEW: When ball phases through opponent's paddle, it disables 1 of their random abilities for 2 seconds! Levels 4-5: disables 2 abilities, Level 6+: disables 3 abilities. Duration increases by +0.5s per level AND cooldown decreases by 0.2s per level (min 2s cooldown). Ability lockdown on every phase!";
 case "joshua": return "MILES - Adds auto-aim laser that locks onto opponent + infinite shield layers! Combined with existing JOSHUA god-mode effects. The ultimate precision assassin!";
 case "jaisan": return "JVsayanMUI - TRANSCENDENT POWER: Adds reality-bending time manipulation! Ball moves in EXTREME slow motion for opponent (10% speed) while you move at 300% speed. Automatic perfect deflection on every hit. Opponent's abilities are nullified. UNSTOPPABLE!";
 case "lag_spike": return "QUANTUM GLITCH - Teleportation distance 3x increased! Opponent's paddle teleports 3x more frequently to EXTREME random locations across the entire screen. Pure chaos intensifies per level! [SUPER BUFFED!]";
 case "reverse_controls": return "CHAOS ENGINE - Every 3s, applies a RANDOM debuff to opponent for 4s: inverted gravity (paddle drifts up), double speed (paddle moves 2x faster = harder control), or reversed momentum (paddle slides past where they want). Unpredictable chaos every time! Level increases debuff strength.";
 case "immunity": return "FORTRESS SHIELD - Immunity effect gets stronger. Reduces ALL debuff durations by an additional 20% per level on top of canceling them!";
 case "blind": return "TOTAL DARKNESS - Blind duration increases to 5 seconds base (+0.7s per level). Opponent's paddle stays COMPLETELY invisible and they lose depth perception!";
 case "shrink_opponent": return "MICRO RAY - Opponent's paddle shrinks even SMALLER! Reduces their paddle to 50% size instead of 70%. Makes blocking nearly impossible!";
 case "paddle_growth": return "TITAN MODE - Your paddle grows even LARGER! +30% size per level instead of +20%. Becomes an unstoppable wall!";
 case "power_boost": return "POWER AMPLIFIER - Doubles the duration of ALL power-ups you collect! Speed lasts 2x as long, giant paddle lasts 2x as long, etc.";
 case "shadow_clone": return "MULTI SHADOW CLONE - Multiple clones with same bad AI!\n" +
 " Each shadow has the same slow, poor positioning as base shadow clone\n" +
 " Lv3: 1 shadow | Lv4: 2 shadows | Lv5: 3 shadows, etc.\n" +
 " Shadows wander slowly with 100px prediction errors\n" +
 " All shadows move at half your paddle speed\n" +
 " More clones = more coverage despite bad AI\n" +
 " SPECIAL: Auto-levels every 6 points! (3x faster than normal)\n" +
 "More clones with the same bad AI!"+
 "Scale infinitely with faster leveling!";
 case "gravity_hammer": return "THOR'S HAMMER - Devastating downward slam with enhanced power!\n" +
 " Each level INCREASES slam force and speed\n" +
 " Ball accelerates FASTER downward (2x base speed per level)\n" +
 " Creates SHOCKWAVE on impact with floor/paddle\n" +
 " Shockwave stuns opponent briefly (0.5s base + 0.2s per level)\n" +
 " Cooldown DECREASES per level (4s base - 0.2s per level)\n" +
 " Visual thunder effect on activation\n" +
 "The ultimate offensive finisher!";
 case "magnet_ball": return "GRAVITON SPHERE - Creates a gravitational field! (NERFED)\n" +
 " Magnetic pull RADIUS increases per level (+15px per level)\n" +
 " Pull STRENGTH increases per level (+1 per level)\n" +
 " Ball curves toward your paddle when in range\n" +
 " Works passively - no activation needed\n" +
 " Base radius: 200px (reduced from 300px)\n" +
 " Stacks with base magnet effect\n" +
 "A balanced gravitational assist!";
 case "portal_pong": return "WORMHOLE MASTER - Advanced portal manipulation!\n" +
 " Place portals ANYWHERE on the field (not just your side)\n" +
 " Portals last LONGER (5s base + 2s per level)\n" +
 " Speed boost through portals INCREASES (+5 speed per level)\n" +
 " Ball angle becomes MORE aggressive toward opponent\n" +
 " Can REPLACE portals instantly by placing new ones\n" +
 " Cooldown DECREASES (8s base - 0.5s per level)\n" +
 "Master space-time to dominate the field!";
 case "power_siphon": return "SOUL REAPER - Consume opponent's life force!\n" +
 " PASSIVE: Every ball hit DRAINS 1s from ALL opponent cooldowns\n" +
 " ACTIVE: Press Q/Slash to REAP their soul for 4s (+0.5s per level)\n" +
 " During Soul Reap: You gain TEMPORARY COPY of ONE random opponent ability!\n" +
 " Copied ability lasts 5s and has NO cooldown while active\n" +
 " Opponent's copied ability is DISABLED during your use\n" +
 " Visual: Dark purple aura surrounds your paddle\n" +
 " Cooldown: 12s base - 0.8s per level\n" +
 " Ball hits during Soul Reap extend duration by +0.5s each\n" +
 "Become a mirror of your enemy's power!";
 case "time_loop": return "CHRONOS REWIND - Extended time manipulation!\n" +
 " Rewind duration DOUBLES (0.6s base + 0.4s per level, was 0.3s + 0.2s)\n" +
 " Cooldown FASTER (15s base - 0.5s per level, was 20s - 0.3s)\n" +
 " Slow-mo effect STRONGER (ball at 20% speed, was 30%)\n" +
 " Slow-mo lasts LONGER (2s base + 0.5s per level, was 1s)\n" +
 " Can see GHOST TRAIL of ball's future path during slow-mo\n" +
 " Rewind also restores 10% paddle position\n" +
 "Bend time itself to your advantage!";
 case "haki": return "ARMAMENT HAKI - The ultimate offensive Haki!\n" +
 " STILL STUNS opponent with Conqueror's Haki on activation!\n" +
 " Paddle turns BLACK for 3s (+0.5s per level)\n" +
 " Hits deal +50% ball speed increase!\n" +
 " Ball PHASES THROUGH opponent's paddle once (unblockable!)\n" +
 " Immune to all debuffs while active\n" +
 " Cooldown: 10s base - 0.3s per level\n" +
 "An unstoppable force of will!";
 case "barrier": return "MIRROR SHIELD - Perfect reflection defense!\n" +
 " Barrier reflects ball at 2x speed!\n" +
 " Reflected ball is HOMING toward opponent goal\n" +
 " Barrier lasts 5s (+1s per level) or until hit\n" +
 " Can place barrier anywhere in your half\n" +
 " Cooldown: 8s base - 0.5s per level\n" +
 "Turn their attack into your weapon!";
 case "trap": return "STICKY TRAP - Ball FREEZES in place!\n" +
 " Ball completely STOPS for 2.5 seconds!\n" +
 " Opponent controls REVERSED for 3 seconds!\n" +
 " Ball releases at high speed after freeze\n" +
 " BIG 80px trap, lasts 10s (+1.5s per level)\n" +
 " Cooldown: 3.5s base - 0.3s per level\n" +
 "Total ball control!";
 case "screen_warp": return "FULL INVERSION - Total control chaos!\n" +
 " Opponent's controls REVERSED for 2s (+0.3s per level)\n" +
 " PLUS: Random paddle JITTER makes aiming impossible!\n" +
 " Paddle shakes randomly while trying to move\n" +
 " Stack with other chaos effects!\n" +
 " Cooldown: 9s base - 0.5s per level\n" +
 "Watch them struggle to control their paddle!";
 case "bankai": return "ZANGETSU - Ichigo's ultimate sword!\n" +
 " Paddle turns BLACK with dark aura\n" +
 " Swing Zangetsu: Press ability during Bankai = 7x BALL SPEED!\n" +
 " Every 3 swings: Fire GETSUGA TENSHO projectile!\n" +
 " Evolved Getsuga: STEALS A POINT on hit!\n" +
 " PERMANENT boosts stack each Bankai activation!\n" +
 " Duration: 8s (+1s per level), Cooldown: 12s\n" +
 "BANKAI - TENSA ZANGETSU!";
 default: return "Enhanced version";
 }
 }

 String getBranch2Description(String ability) {
 switch (ability) {
 case "gun": return "VEGETA - Shoots explosive Big Bang Attack projectiles! Each level adds: +1 projectile in spread pattern (Lv3=1 shot, Lv4=2, Lv5=3...), +faster projectile speed, +larger explosion radius (+10px per level), +stronger stun on hit! Devastating area damage that scales infinitely!";
 case "speed_boost": return "FLASH - Keeps your +30% speed bonus. Every 5 seconds, slows time around the ball for 1 second! Ball moves in slow-motion while you move normally.";
 case "shield": return "REFLECTOR - Shield blocks goals as normal. NEW: When ball hits shield, it reflects back with a SPEED BOOST! Turn defense into instant offense!";
 case "ball_control": return "HACKER - Ball control stays. NEW: Your ball can now phase through map obstacles like invisible walls and center walls! Ultimate environmental control.";
 case "power_magnet": return "COLLECTOR - Keeps power-up attraction. NEW: Doubles the effectiveness of ALL power-ups you collect! 2x duration, 2x power!";
 case "sticky_paddle": return "SLUDGE ZONE - Creates a zone around your paddle that dramatically slows any ball passing through it! More area of effect than sticky paddle.";
 case "slow_opponent": return "GRAVITY WELL - Opponent's paddle permanently feels 3x heavier and moves MUCH slower! Continuous heavy debuff instead of periodic effect. [BUFFED!]";
 case "double_points": return "COMBO MASTER - Score multiplier stays. NEW: Build combos by hitting the ball multiple times in a row! +1 bonus point per hit in combo streak!";
 case "ability_stealer": return "JIREN - Shoots purple explosive projectiles with overwhelming power! Each level adds: +1 projectile in spread pattern (Lv3=1, Lv4=2, Lv5=3...), +faster speed, +larger explosion radius (+10px per level). Steals abilities on explosion hit: Lv3=1 ability, Lv4-5=2 abilities, Lv6+=3 abilities! Ultimate ability domination!";
 case "ghost_ball": return "REALITY BREAK - Ball keeps phasing through paddle. NEW: Every 12s, distorts game physics for 3 seconds (+0.5s per level): ball speed fluctuates wildly (50%-200%), paddle hitboxes shift randomly ( 20 pixels), power-ups flicker in/out of existence! Reality itself becomes unstable! Chaos scales per level.";
 case "joshua": return "HOSH JOSH - Adds teleport dash ability (blink to ball instantly) + triple shot that shoots 3 balls! Combined with JOSHUA god-mode! The pinnacle of power!";
 case "jaisan": return "KVsayanMUI - ABSOLUTE DOMINANCE: Adds dimensional rift control! Your paddle exists in MULTIPLE positions simultaneously (3 ghost paddles that all deflect). Ball speed DOUBLES when you hit it. Opponent's paddle randomly freezes for 1s every 3 seconds. TOTAL CHAOS!";
 case "lag_spike": return "TIME DISTORTION - Instead of teleporting paddle, slows opponent's paddle movement by 50% for 2 seconds! Different strategy, same disruption!";
 case "reverse_controls": return "PUPPET MASTER - Every 5s, takes FULL CONTROL of opponent's paddle for 2 seconds (+0.3s per level)! Their paddle moves automatically away from the ball or into bad positions. They become a spectator of their own paddle! Ultimate humiliation!";
 case "immunity": return "ADAPTIVE ARMOR - Instead of just canceling debuffs, reflects 50% of debuff effects back to the attacker! Counter-offensive immunity!";
 case "blind": return "SENSORY OVERLOAD - Instead of hiding paddle, floods opponent's screen with fake paddles, fake balls, and visual distortions! Impossible to track what's real. +0.5s duration per level!";
 case "shrink_opponent": return "COMPRESSION FIELD - Shrink effect lasts 2x longer! Instead of 1.5s, effect lasts 3s. More time with tiny opponent!";
 case "paddle_growth": return "ELASTIC EXPANSION - Your paddle can temporarily stretch 100% LARGER when ball is within 50 pixels! Auto-expands for 0.8s. Almost impossible to miss! [NOW WORKING!]";
 case "power_boost": return "POWER FUSION - Combines multiple power-up effects! When you collect 2+ power-ups close together, their effects merge and amplify!";
 case "shadow_clone": return "AFTERIMAGE DEFENSE - Aggressive AI-controlled shadow backup!\n" +
 " Shadow moves INDEPENDENTLY with AI\n" +
 " ALWAYS tries to defend incoming balls aggressively\n" +
 " Predicts ball trajectory (70% accuracy)\n" +
 " Acts as backup when you miss - catches what you can't!\n" +
 " Patrols center area when ball is away\n" +
 " Shadow speed: Same as your paddle\n" +
 " Auto-levels every 15 points\n" +
 "Your aggressive defensive partner!";
 case "gravity_hammer": return "GRAVITY WELL - Area-of-effect gravitational pull!\n" +
 " Creates a GRAVITY FIELD when activated\n" +
 " Ball pulled toward CENTER of your paddle area\n" +
 " Field RADIUS increases per level (+30px per level)\n" +
 " Pull STRENGTH increases per level\n" +
 " Field lasts 2s base (+0.5s per level)\n" +
 " Cooldown: 6s (decreases 0.3s per level)\n" +
 " Can curve incoming shots into perfect returns\n" +
 "Master gravitational control for defense!";
 case "magnet_ball": return "FORCE FIELD - Defensive magnetic repulsion! (NERFED)\n" +
 " TOGGLE MODE: Press Q/Slash to switch attraction/repulsion\n" +
 " REPULSION: Pushes ball away when close (15px base radius)\n" +
 " Good for defensive plays and buying time\n" +
 " Repulsion radius increases per level (+3px per level)\n" +
 " Repulsion strength reduced (no longer 2x multiplier)\n" +
 " Attraction strength halved when in repel mode\n" +
 " No cooldown for toggling modes\n" +
 "Balanced defensive flexibility!";
 case "portal_pong": return "DIMENSIONAL RIFT - Unstable portal chaos!\n" +
 " Portals RANDOMLY MOVE every 2 seconds\n" +
 " Movement range increases per level\n" +
 " Speed boost is RANDOM (2x to 5x normal)\n" +
 " Ball exit angle is UNPREDICTABLE\n" +
 " Can place 3 PORTALS instead of 2 (creates portal network)\n" +
 " Ball can chain between multiple portals\n" +
 " Cooldown: 10s (decreases 0.4s per level)\n" +
 "Embrace the chaos of dimensional instability!";
 case "power_siphon": return "OVERLOAD - Explosive energy discharge!\n" +
 " PASSIVE: Build up CHARGE with every ball hit (max 100 charge)\n" +
 " Charge bar fills: +20 per hit, decays -2 per second\n" +
 " ACTIVE: Press Q/Slash at ANY charge to OVERLOAD!\n" +
 " OVERLOAD EFFECTS scale with charge amount:\n" +
 " 25+ Charge: Opponent stunned 1s, ball speed +50%\n" +
 " 50+ Charge: Opponent stunned 2s, ball speed +100%, abilities disabled 2s\n" +
 " 75+ Charge: Opponent stunned 3s, ball speed +150%, abilities disabled 4s\n" +
 " 100 FULL: MEGA OVERLOAD - 4s stun, +200% ball speed, ALL abilities disabled 6s, opponent paddle shrinks!\n" +
 " Using Overload consumes ALL charge\n" +
 " No cooldown - spam when ready!\n" +
 " Higher levels = faster charge gain (+5 per hit per level)\n" +
 "Build power, then UNLEASH CHAOS!";
 case "time_loop": return "TEMPORAL ECHO - Create parallel timelines!\n" +
 " Rewind creates a GHOST BALL in alternate timeline\n" +
 " Ghost ball follows ORIGINAL trajectory\n" +
 " 2 BALLS active simultaneously for 3s (+0.5s per level)\n" +
 " Both balls can score points\n" +
 " Opponent must defend against BOTH\n" +
 " Cooldown: 25s (decreases 0.8s per level)\n" +
 " No slow-mo effect (instant chaos instead)\n" +
 "Split reality and overwhelm your opponent!";
 case "haki": return "OBSERVATION HAKI - The ultimate defensive Haki!\n" +
 " STILL STUNS opponent with Conqueror's Haki on activation!\n" +
 " See ball trajectory for next 3 bounces\n" +
 " Ball slows to 60% speed for 2s (+0.3s per level)\n" +
 " Opponent's paddle moves at 50% speed!\n" +
 " AUTO-BLOCK: Paddle teleports to save ONE goal per activation!\n" +
 " Cooldown: 9s base - 0.4s per level\n" +
 "The eyes that see all futures!";
 case "barrier": return "ABSORPTION SHIELD - Catch and release!\n" +
 " Barrier CATCHES the ball completely\n" +
 " Ball held for 1s, then you aim and release!\n" +
 " Released ball has 2x speed + your chosen angle\n" +
 " Can hold ball to stall (max 3s before auto-release)\n" +
 " Cooldown: 8s base - 0.6s per level\n" +
 "Control the battlefield!";
 case "trap": return "EXPLOSIVE TRAP - Devastating shockwave!\n" +
 " Ball EXPLODES on trap contact!\n" +
 " STUNS opponent for 1.5 seconds!\n" +
 " SHRINKS opponent paddle for 2.5 seconds!\n" +
 " Ball ricochets at 2.5X SPEED in random direction!\n" +
 " BIG 80px trap, lasts 10s (+1.5s per level)\n" +
 " Cooldown: 3.5s base - 0.3s per level\n" +
 "BOOM! Total devastation!";
 case "screen_warp": return "TUNNEL VISION - Ball vanishes at edges!\n" +
 " Ball becomes INVISIBLE when near screen edges\n" +
 " Hidden zones: left edge, top/bottom walls\n" +
 " Opponent must predict ball trajectory!\n" +
 " Effect lasts 2s (+0.4s per level)\n" +
 " Cooldown: 9s base - 0.6s per level\n" +
 "They can't hit what they can't see!";
 case "bankai": return "HOLLOW FORM - Inner demon awakens!\n" +
 " Gain hollow mask - paddle turns WHITE with void aura\n" +
 " Ball phases through opponent paddle ONCE\n" +
 " After phase, ball returns to normal\n" +
 " Move at 1.5x speed during Hollow Form\n" +
 " Regenerate: ball hit extends duration by +0.5s\n" +
 " Duration: 4s (+0.5s per level), Cooldown: 15s - 1s per level\n" +
 "Let the hollow consume you!";
 default: return "Alternative version";
 }
 }

 String getAbilityDescriptionWithBranch(String ability, int branch) {
 if (branch == 0) {
 return getAbilityDescription(ability);
 }
 
 String baseName = getAbilityShortName(ability) + " [" + getBranchName(ability, branch) + "]";
 
 if (branch == 1) {
 switch (ability) {
 case "gun": return baseName + " - Laser stun (duration increases)";
 case "speed_boost": return baseName + " - +30% speed + Dash every 3s";
 case "shield": return baseName + " - Extra shield layers";
 case "ball_control": return baseName + " - Precise targeting";
 case "power_magnet": return baseName + " - Absorbs all power-ups";
 case "sticky_paddle": return baseName + " - Ball sticks longer";
 case "slow_opponent": return baseName + " - Freeze opponent";
 case "double_points": return baseName + " - Steal 1 point on score";
 case "ability_stealer": return baseName + " - Steals 1-2 abilities";
 case "ghost_ball": return baseName + " - Enhanced phasing";
 case "joshua": return baseName + " - Auto-aim + infinite shield";
 case "shadow_clone": return baseName + " - +1 shadow per level";
 case "gravity_hammer": return baseName + " - Stronger slam + stun";
 case "magnet_ball": return baseName + " - Larger pull radius";
 case "portal_pong": return baseName + " - Better portals + faster";
 case "power_siphon": return baseName + " - Copy random ability";
 case "time_loop": return baseName + " - Longer rewind + slow-mo";
 case "haki": return baseName + " - Stun + phasing ball + power hits";
 case "barrier": return baseName + " - 2x speed reflect + homing";
 case "trap": return baseName + " - Ball slows to 30%";
 case "screen_warp": return baseName + " - Flip their screen";
 case "bankai": return baseName + " - Giant paddle + 2x speed";
 default: return baseName;
 }
 } else {
 switch (ability) {
 case "gun": return baseName + " - 2 extra bullets per level";
 case "speed_boost": return baseName + " - +30% speed + Slow time";
 case "shield": return baseName + " - Reflects with speed boost";
 case "ball_control": return baseName + " - Phase through obstacles";
 case "power_magnet": return baseName + " - 2x power-up effect";
 case "sticky_paddle": return baseName + " - Dramatic slowdown zone";
 case "slow_opponent": return baseName + " - Heavy paddle";
 case "double_points": return baseName + " - Combo bonus points";
 case "ability_stealer": return baseName + " - Disable top ability";
 case "ghost_ball": return baseName + " - Ghost trail";
 case "joshua": return baseName + " - Teleport dash + triple shot";
 case "shadow_clone": return baseName + " - Auto-defending shadow";
 case "gravity_hammer": return baseName + " - Gravity field pull";
 case "magnet_ball": return baseName + " - Toggle attract/repel";
 case "portal_pong": return baseName + " - 3 chaotic portals";
 case "power_siphon": return baseName + " - Charge & explode!";
 case "time_loop": return baseName + " - Dual timeline balls";
 case "haki": return baseName + " - Stun + slow opponent + auto-block";
 case "barrier": return baseName + " - Catch ball + relaunch";
 case "trap": return baseName + " - Explosive + 50% speed";
 case "screen_warp": return baseName + " - 40% visible area";
 case "bankai": return baseName + " - Ball phases through once";
 default: return baseName;
 }
 }
 }

 int getNumberOfShadows(int player) {
 HashMap<String, Integer> abilities = (player == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (player == 1) ? player1AbilityBranches : player2AbilityBranches;
 
 if (!abilities.containsKey("shadow_clone")) return 0;
 
 int branch = branches.getOrDefault("shadow_clone", 0);
 int level = abilities.get("shadow_clone");
 
 // Branch 1: Multi Shadow Clone - starts with 1, +1 shadow per level after 3
 if (branch == 1) {
 if (level <= 2) return 1; // Levels 1-2 have 1 shadow
 return level - 2; // Level 3 = 1 shadow, Level 4 = 2 shadows, Level 5 = 3 shadows, etc.
 }
 // Branch 2: Afterimage Defense - always 1 shadow
 else if (branch == 2) {
 return 1;
 }
 // No branch yet - 1 shadow at all levels
 return 1;
 }

 public void paintComponent(Graphics g) {
 super.paintComponent(g);
 Graphics2D g2d = (Graphics2D) g;
 g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

 // Calculate scaling to maintain aspect ratio (no stretching)
 double scaleX = getWidth() / 600.0;
 double scaleY = getHeight() / 400.0;
 double scale = Math.min(scaleX, scaleY); // Use smaller scale to fit without stretching

 // Center the game if there's extra space
 double translateX = (getWidth() - 600 * scale) / 2;
 double translateY = (getHeight() - 400 * scale) / 2;

 g2d.translate(translateX, translateY);
 g2d.scale(scale, scale);

 // Main menu rendering
 if (showingMainMenu) {
 drawMainMenu(g2d);
 return;
 }

 // Apply mirror transform if active - flips entire game horizontally
 if (mirrorActive) {
 g2d.translate(600, 0); // Move to right edge
 g2d.scale(-1, 1); // Flip horizontally
 }

 // Background
 GradientPaint gradient = new GradientPaint(0, 0, new Color(10, 20, 40), 0, 400, new Color(20, 40, 80));
 g2d.setPaint(gradient);
 g2d.fillRect(0, 0, 600, 400);
 
 // Center line
 g2d.setColor(new Color(100, 150, 255, 100));
 for (int i = 0; i < 400; i += 20) g2d.fillRect(293, i, 14, 10);
 g2d.setColor(new Color(150, 200, 255));
 for (int i = 0; i < 400; i += 20) g2d.fillRect(295, i, 10, 10);
 
 // Draw center wall
 if (centerWallActive) {
 g2d.setColor(new Color(128, 0, 128, 180));
 g2d.fillRect(295, 0, 10, centerWallGapY - centerWallGapSize / 2);
 g2d.fillRect(295, centerWallGapY + centerWallGapSize / 2, 10, 400 - (centerWallGapY + centerWallGapSize / 2));
 }
 
 // Draw danger zone
 if (dangerZoneActive) {
 int pulseEffect = (int)(Math.sin(System.currentTimeMillis() / 300.0) * 5);
 for (int i = 3; i >= 1; i--) {
 g2d.setColor(new Color(255, 100, 0, 15 * i));
 g2d.fillOval(dangerZoneCenterX - dangerZoneRadius - pulseEffect - i * 5, dangerZoneCenterY - dangerZoneRadius - pulseEffect - i * 5, (dangerZoneRadius + pulseEffect + i * 5) * 2, (dangerZoneRadius + pulseEffect + i * 5) * 2);
 }
 RadialGradientPaint dangerGradient = new RadialGradientPaint(dangerZoneCenterX, dangerZoneCenterY, dangerZoneRadius, new float[]{0.0f, 0.7f, 1.0f}, new Color[]{new Color(255, 50, 0, 0), new Color(255, 100, 0, 30), new Color(255, 150, 0, 60)});
 g2d.setPaint(dangerGradient);
 g2d.fillOval(dangerZoneCenterX - dangerZoneRadius - pulseEffect, dangerZoneCenterY - dangerZoneRadius - pulseEffect, (dangerZoneRadius + pulseEffect) * 2, (dangerZoneRadius + pulseEffect) * 2);
 }
 
 // Draw gravity effect
 if (gravityActive) {
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.setColor(new Color(150, 0, 255, 100));
 for (int i = 0; i < 600; i += 40) {
 g2d.drawString(" ", i, 30 + (int)(Math.sin((System.currentTimeMillis() + i * 10) / 200.0) * 10));
 g2d.drawString(" ", i, 200 + (int)(Math.sin((System.currentTimeMillis() + i * 10) / 200.0) * 10));
 g2d.drawString(" ", i, 370 + (int)(Math.sin((System.currentTimeMillis() + i * 10) / 200.0) * 10));
 }
 }
 
 // Draw invisible walls
 if (invisibleWallsActive) {
 for (int i = 0; i < 600; i += 20) {
 int alpha = (int)(50 + 30 * Math.sin((System.currentTimeMillis() + i * 5) / 150.0));
 g2d.setColor(new Color(100, 200, 255, alpha));
 g2d.fillRect(i, invisibleWallY - 2, 15, 4);
 }
 }

 // Draw paddles
 int paddle1Height = getPaddleHeight(1, shrinkPaddlesActive);
 int paddle2Height = getPaddleHeight(2, shrinkPaddlesActive);
 
 // Check if Elastic Expansion is active (for visual effects)
 boolean elastic1Active = false;
 boolean elastic2Active = false;
 if (getEffectiveAbilityLevel(1, "paddle_growth") >= 3 && player1AbilityBranches.getOrDefault("paddle_growth", 0) == 2) {
 int ballCenterY = ballY + 7;
 int paddleCenterY = paddle1Y + paddle1Height / 2;
 double distance = Math.abs(ballCenterY - paddleCenterY);
 boolean ballComingToward = ballVelX < 0 && ballX < 300;
 if (distance < 50 && ballComingToward) elastic1Active = true;
 }
 if (getEffectiveAbilityLevel(2, "paddle_growth") >= 3 && player2AbilityBranches.getOrDefault("paddle_growth", 0) == 2) {
 int ballCenterY = ballY + 7;
 int paddleCenterY = paddle2Y + paddle2Height / 2;
 double distance = Math.abs(ballCenterY - paddleCenterY);
 boolean ballComingToward = ballVelX > 0 && ballX > 300;
 if (distance < 50 && ballComingToward) elastic2Active = true;
 }
 
 // NEW ABILITIES RENDERING
 
 // Render Shadow Clones for Player 1
 if (getEffectiveAbilityLevel(1, "shadow_clone") > 0 && !player1ShadowPositions.isEmpty()) {
 int numShadows = getNumberOfShadows(1);
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int branch = player1AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 2) {
 // Branch 2: Afterimage Defense - single independent shadow with special effects
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
 // Glowing aura
 g2d.setColor(new Color(100, 150, 255, 100));
 g2d.fillRoundRect(5, player1IndependentShadowY - 5, 20, paddleHeight1 + 10, 10, 10);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
 // Main shadow body
 g2d.setColor(new Color(100, 150, 255));
 g2d.fillRoundRect(10, player1IndependentShadowY, 10, paddleHeight1, 5, 5);
 // Highlight
 g2d.setColor(new Color(200, 220, 255, 200));
 g2d.fillRoundRect(11, player1IndependentShadowY + 2, 3, paddleHeight1 - 4, 2, 2);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
 } else {
 // Branch 1 or no branch: Independent shadows with varying opacity
 for (int i = 0; i < Math.min(numShadows, player1ShadowPositions.size()); i++) {
 int shadowY = player1ShadowPositions.get(i);
 
 // Varying opacity based on shadow number
 float opacity = 0.5f - (i * 0.1f);
 opacity = Math.max(0.2f, opacity);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
 
 // Blue glow effect
 g2d.setColor(new Color(100, 100, 255, (int)(100 * opacity)));
 g2d.fillRoundRect(7, shadowY - 2, 16, paddleHeight1 + 4, 8, 8);
 
 // Main shadow body
 g2d.setColor(new Color(100, 100, 255));
 g2d.fillRoundRect(10, shadowY, 10, paddleHeight1, 5, 5);
 }
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
 }
 }

 // Render Shadow Clones for Player 2
 if (getEffectiveAbilityLevel(2, "shadow_clone") > 0 && !player2ShadowPositions.isEmpty()) {
 int numShadows = getNumberOfShadows(2);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 int branch = player2AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 2) {
 // Branch 2: Afterimage Defense - single independent shadow with special effects
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
 // Glowing aura
 g2d.setColor(new Color(255, 150, 100, 100));
 g2d.fillRoundRect(575, player2IndependentShadowY - 5, 20, paddleHeight2 + 10, 10, 10);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
 // Main shadow body
 g2d.setColor(new Color(255, 150, 100));
 g2d.fillRoundRect(580, player2IndependentShadowY, 10, paddleHeight2, 5, 5);
 // Highlight
 g2d.setColor(new Color(255, 220, 200, 200));
 g2d.fillRoundRect(586, player2IndependentShadowY + 2, 3, paddleHeight2 - 4, 2, 2);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
 } else {
 // Branch 1 or no branch: Independent shadows with varying opacity
 for (int i = 0; i < Math.min(numShadows, player2ShadowPositions.size()); i++) {
 int shadowY = player2ShadowPositions.get(i);
 
 // Varying opacity based on shadow number
 float opacity = 0.5f - (i * 0.1f);
 opacity = Math.max(0.2f, opacity);
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
 
 // Red glow effect
 g2d.setColor(new Color(255, 100, 100, (int)(100 * opacity)));
 g2d.fillRoundRect(577, shadowY - 2, 16, paddleHeight2 + 4, 8, 8);
 
 // Main shadow body
 g2d.setColor(new Color(255, 100, 100));
 g2d.fillRoundRect(580, shadowY, 10, paddleHeight2, 5, 5);
 }
 g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
 }
 }
 
 // Render Player 1 Portals - BIDIRECTIONAL (both portals look identical)
 if (player1PortalEntranceX != null && player1PortalEntranceY != null && player1PortalPlacementStage >= 1) {
 // Portal #1 (blue/cyan, pulsing)
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 100.0));
 int radius = 25 + pulseSize;
 
 // Outer glow
 g2d.setColor(new Color(0, 100, 255, 100));
 g2d.fillOval(player1PortalEntranceX - radius - 5, player1PortalEntranceY - radius - 5, 
 (radius + 5) * 2, (radius + 5) * 2);
 
 // Main portal
 g2d.setColor(new Color(0, 150, 255, 200));
 g2d.fillOval(player1PortalEntranceX - radius, player1PortalEntranceY - radius, 
 radius * 2, radius * 2);
 
 // Portal ring
 g2d.setColor(Color.CYAN);
 g2d.setStroke(new BasicStroke(3));
 g2d.drawOval(player1PortalEntranceX - radius, player1PortalEntranceY - radius, 
 radius * 2, radius * 2);
 
 // Swirl effect
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 10.0 + i * 45) % 360;
 int swirRadius = 15;
 int px = player1PortalEntranceX + (int)(Math.cos(Math.toRadians(angle)) * swirRadius);
 int py = player1PortalEntranceY + (int)(Math.sin(Math.toRadians(angle)) * swirRadius);
 g2d.fillOval(px - 2, py - 2, 4, 4);
 }
 }
 if (player1PortalExitX != null && player1PortalExitY != null && player1PortalPlacementStage == 2) {
 // Portal #2 (identical blue/cyan, pulsing)
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 100.0 + Math.PI));
 int radius = 25 + pulseSize;
 
 // Outer glow
 g2d.setColor(new Color(0, 100, 255, 100));
 g2d.fillOval(player1PortalExitX - radius - 5, player1PortalExitY - radius - 5, 
 (radius + 5) * 2, (radius + 5) * 2);
 
 // Main portal
 g2d.setColor(new Color(0, 150, 255, 200));
 g2d.fillOval(player1PortalExitX - radius, player1PortalExitY - radius, 
 radius * 2, radius * 2);
 
 // Portal ring
 g2d.setColor(Color.CYAN);
 g2d.setStroke(new BasicStroke(3));
 g2d.drawOval(player1PortalExitX - radius, player1PortalExitY - radius, 
 radius * 2, radius * 2);
 
 // Swirl effect
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 10.0 + i * 45) % 360;
 int swirRadius = 15;
 int px = player1PortalExitX + (int)(Math.cos(Math.toRadians(angle)) * swirRadius);
 int py = player1PortalExitY + (int)(Math.sin(Math.toRadians(angle)) * swirRadius);
 g2d.fillOval(px - 2, py - 2, 4, 4);
 }
 }

 // Render Player 2 Portals - BIDIRECTIONAL (both portals look identical)
 if (player2PortalEntranceX != null && player2PortalEntranceY != null && player2PortalPlacementStage >= 1) {
 // Portal #1 (purple, pulsing)
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 100.0));
 int radius = 25 + pulseSize;
 
 // Outer glow
 g2d.setColor(new Color(150, 0, 255, 100));
 g2d.fillOval(player2PortalEntranceX - radius - 5, player2PortalEntranceY - radius - 5, 
 (radius + 5) * 2, (radius + 5) * 2);
 
 // Main portal
 g2d.setColor(new Color(180, 0, 255, 200));
 g2d.fillOval(player2PortalEntranceX - radius, player2PortalEntranceY - radius, 
 radius * 2, radius * 2);
 
 // Portal ring
 g2d.setColor(new Color(200, 100, 255));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawOval(player2PortalEntranceX - radius, player2PortalEntranceY - radius, 
 radius * 2, radius * 2);
 
 // Swirl effect
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 10.0 + i * 45) % 360;
 int swirRadius = 15;
 int px = player2PortalEntranceX + (int)(Math.cos(Math.toRadians(angle)) * swirRadius);
 int py = player2PortalEntranceY + (int)(Math.sin(Math.toRadians(angle)) * swirRadius);
 g2d.fillOval(px - 2, py - 2, 4, 4);
 }
 }
 if (player2PortalExitX != null && player2PortalExitY != null && player2PortalPlacementStage == 2) {
 // Portal #2 (identical purple, pulsing)
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 100.0 + Math.PI));
 int radius = 25 + pulseSize;
 
 // Outer glow
 g2d.setColor(new Color(150, 0, 255, 100));
 g2d.fillOval(player2PortalExitX - radius - 5, player2PortalExitY - radius - 5, 
 (radius + 5) * 2, (radius + 5) * 2);
 
 // Main portal
 g2d.setColor(new Color(180, 0, 255, 200));
 g2d.fillOval(player2PortalExitX - radius, player2PortalExitY - radius, 
 radius * 2, radius * 2);
 
 // Portal ring
 g2d.setColor(new Color(200, 100, 255));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawOval(player2PortalExitX - radius, player2PortalExitY - radius, 
 radius * 2, radius * 2);
 
 // Swirl effect
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 10.0 + i * 45) % 360;
 int swirRadius = 15;
 int px = player2PortalExitX + (int)(Math.cos(Math.toRadians(angle)) * swirRadius);
 int py = player2PortalExitY + (int)(Math.sin(Math.toRadians(angle)) * swirRadius);
 g2d.fillOval(px - 2, py - 2, 4, 4);
 }
 }
 
 // Portal boost visual effect
 if (portalBoostTimer > 0) {
 int alpha = (int)(255 * (portalBoostTimer / 30.0));
 g2d.setColor(new Color(255, 255, 0, alpha));
 int effectRadius = 30 - portalBoostTimer;
 g2d.setStroke(new BasicStroke(3));
 g2d.drawOval(ballX - effectRadius, ballY - effectRadius, effectRadius * 2, effectRadius * 2);
 
 // Speed lines
 for (int i = 0; i < 5; i++) {
 int lineLength = 20 + i * 5;
 g2d.drawLine(ballX, ballY, 
 ballX - (ballVelX > 0 ? lineLength : -lineLength), 
 ballY);
 }
 }
 g2d.setStroke(new BasicStroke(1));
 
 // Magnet Ball effect - show attraction lines
 if (player1MagnetActive && ballX < 300 && ballVelX < 0) {
 g2d.setColor(new Color(100, 200, 255, 100));
 int paddle1CenterY = paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2;
 g2d.setStroke(new BasicStroke(2));
 for (int i = 0; i < 3; i++) {
 g2d.drawLine(20, paddle1CenterY, ballX + 7 - i * 5, ballY + 7 - i * 5);
 }
 g2d.setStroke(new BasicStroke(1));
 }
 if (player2MagnetActive && ballX > 300 && ballVelX > 0) {
 g2d.setColor(new Color(255, 100, 100, 100));
 int paddle2CenterY = paddle2Y + getPaddleHeight(2, shrinkPaddlesActive) / 2;
 g2d.setStroke(new BasicStroke(2));
 for (int i = 0; i < 3; i++) {
 g2d.drawLine(580, paddle2CenterY, ballX + 7 - i * 5, ballY + 7 - i * 5);
 }
 g2d.setStroke(new BasicStroke(1));
 }

 // RENDER NEW ABILITIES

 // BARRIER rendering
 if (player1BarrierActive) {
 int barrierBranch = player1AbilityBranches.getOrDefault("barrier", 0);
 // Glow effect
 g2d.setColor(new Color(0, 200, 255, 100));
 g2d.fillRoundRect(player1BarrierX - 5, player1BarrierY - 5, barrierWidth + 10, barrierHeight + 10, 10, 10);
 // Main barrier
 if (barrierBranch == 1) {
 g2d.setColor(new Color(200, 200, 255, 200)); // Mirror - silver
 } else if (barrierBranch == 2) {
 g2d.setColor(new Color(100, 255, 100, 200)); // Absorption - green
 } else {
 g2d.setColor(new Color(0, 150, 255, 200)); // Base - blue
 }
 g2d.fillRoundRect(player1BarrierX, player1BarrierY, barrierWidth, barrierHeight, 5, 5);
 g2d.setColor(Color.WHITE);
 g2d.drawRoundRect(player1BarrierX, player1BarrierY, barrierWidth, barrierHeight, 5, 5);
 }
 if (player2BarrierActive) {
 int barrierBranch = player2AbilityBranches.getOrDefault("barrier", 0);
 g2d.setColor(new Color(255, 100, 100, 100));
 g2d.fillRoundRect(player2BarrierX - 5, player2BarrierY - 5, barrierWidth + 10, barrierHeight + 10, 10, 10);
 if (barrierBranch == 1) {
 g2d.setColor(new Color(255, 200, 200, 200));
 } else if (barrierBranch == 2) {
 g2d.setColor(new Color(255, 255, 100, 200));
 } else {
 g2d.setColor(new Color(255, 100, 100, 200));
 }
 g2d.fillRoundRect(player2BarrierX, player2BarrierY, barrierWidth, barrierHeight, 5, 5);
 g2d.setColor(Color.WHITE);
 g2d.drawRoundRect(player2BarrierX, player2BarrierY, barrierWidth, barrierHeight, 5, 5);
 }

 // TRAP rendering
 if (player1TrapActive) {
 int trapBranch = player1AbilityBranches.getOrDefault("trap", 0);
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 200.0));
 // Trap glow
 g2d.setColor(new Color(255, 0, 255, 80));
 g2d.fillOval(player1TrapX - trapSize/2 - 10 - pulseSize, player1TrapY - trapSize/2 - 10 - pulseSize,
 trapSize + 20 + pulseSize*2, trapSize + 20 + pulseSize*2);
 // Main trap
 if (trapBranch == 1) {
 g2d.setColor(new Color(0, 200, 0, 150)); // Sticky - green
 } else if (trapBranch == 2) {
 g2d.setColor(new Color(255, 100, 0, 150)); // Explosive - orange
 } else {
 g2d.setColor(new Color(200, 0, 200, 150)); // Base - purple
 }
 g2d.fillOval(player1TrapX - trapSize/2, player1TrapY - trapSize/2, trapSize, trapSize);
 g2d.setColor(Color.WHITE);
 g2d.setStroke(new BasicStroke(2));
 g2d.drawOval(player1TrapX - trapSize/2, player1TrapY - trapSize/2, trapSize, trapSize);
 g2d.setStroke(new BasicStroke(1));
 }
 if (player2TrapActive) {
 int trapBranch = player2AbilityBranches.getOrDefault("trap", 0);
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 200.0));
 g2d.setColor(new Color(255, 255, 0, 80));
 g2d.fillOval(player2TrapX - trapSize/2 - 10 - pulseSize, player2TrapY - trapSize/2 - 10 - pulseSize,
 trapSize + 20 + pulseSize*2, trapSize + 20 + pulseSize*2);
 if (trapBranch == 1) {
 g2d.setColor(new Color(0, 255, 200, 150));
 } else if (trapBranch == 2) {
 g2d.setColor(new Color(255, 50, 50, 150));
 } else {
 g2d.setColor(new Color(255, 255, 0, 150));
 }
 g2d.fillOval(player2TrapX - trapSize/2, player2TrapY - trapSize/2, trapSize, trapSize);
 g2d.setColor(Color.WHITE);
 g2d.setStroke(new BasicStroke(2));
 g2d.drawOval(player2TrapX - trapSize/2, player2TrapY - trapSize/2, trapSize, trapSize);
 g2d.setStroke(new BasicStroke(1));
 }

 // HAKI ARMAMENT - Draw black aura around paddle
 if (player1ArmamentActive) {
 g2d.setColor(new Color(30, 0, 50, 150));
 g2d.fillRoundRect(5, paddle1Y - 10, 25, paddle1Height + 20, 10, 10);
 g2d.setColor(new Color(100, 0, 150));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawRoundRect(5, paddle1Y - 5, 20, paddle1Height + 10, 5, 5);
 g2d.setStroke(new BasicStroke(1));
 }
 if (player2ArmamentActive) {
 g2d.setColor(new Color(30, 0, 50, 150));
 g2d.fillRoundRect(570, paddle2Y - 10, 25, paddle2Height + 20, 10, 10);
 g2d.setColor(new Color(100, 0, 150));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawRoundRect(575, paddle2Y - 5, 20, paddle2Height + 10, 5, 5);
 g2d.setStroke(new BasicStroke(1));
 }

 // HAKI OBSERVATION - Draw trajectory prediction lines
 if (player1ObservationActive) {
 g2d.setColor(new Color(255, 215, 0, 150)); // Gold
 g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
 int predX = ballX, predY = ballY, predVelX = ballVelX, predVelY = ballVelY;
 for (int i = 0; i < 50 && predX > 0 && predX < 600; i++) {
 int nextX = predX + predVelX;
 int nextY = predY + predVelY;
 if (nextY < 0 || nextY > 400) predVelY = -predVelY;
 g2d.drawLine(predX, predY, nextX, nextY);
 predX = nextX;
 predY = nextY;
 }
 g2d.setStroke(new BasicStroke(1));
 }
 if (player2ObservationActive) {
 g2d.setColor(new Color(255, 100, 100, 150));
 g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
 int predX = ballX, predY = ballY, predVelX = ballVelX, predVelY = ballVelY;
 for (int i = 0; i < 50 && predX > 0 && predX < 600; i++) {
 int nextX = predX + predVelX;
 int nextY = predY + predVelY;
 if (nextY < 0 || nextY > 400) predVelY = -predVelY;
 g2d.drawLine(predX, predY, nextX, nextY);
 predX = nextX;
 predY = nextY;
 }
 g2d.setStroke(new BasicStroke(1));
 }

 // BANKAI - Zangetsu effect (large black aura)
 if (player1ZangetsuActive) {
 // Black energy aura
 g2d.setColor(new Color(0, 0, 0, 100));
 g2d.fillRoundRect(0, paddle1Y - 30, 60, paddle1Height + 60, 20, 20);
 g2d.setColor(new Color(255, 50, 0, 200)); // Red energy
 g2d.setStroke(new BasicStroke(3));
 g2d.drawRoundRect(5, paddle1Y - 25, 50, paddle1Height + 50, 15, 15);
 g2d.setStroke(new BasicStroke(1));
 g2d.setColor(Color.WHITE);
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 g2d.drawString("BANKAI", 5, paddle1Y - 30);
 }
 if (player2ZangetsuActive) {
 g2d.setColor(new Color(0, 0, 0, 100));
 g2d.fillRoundRect(540, paddle2Y - 30, 60, paddle2Height + 60, 20, 20);
 g2d.setColor(new Color(255, 50, 0, 200));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawRoundRect(545, paddle2Y - 25, 50, paddle2Height + 50, 15, 15);
 g2d.setStroke(new BasicStroke(1));
 g2d.setColor(Color.WHITE);
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 g2d.drawString("BANKAI", 545, paddle2Y - 30);
 }

 // BANKAI - Hollow Form effect (white ethereal aura)
 if (player1HollowActive) {
 g2d.setColor(new Color(255, 255, 255, 80));
 g2d.fillRoundRect(0, paddle1Y - 15, 40, paddle1Height + 30, 15, 15);
 g2d.setColor(new Color(200, 200, 255, 200));
 g2d.setStroke(new BasicStroke(2));
 g2d.drawRoundRect(5, paddle1Y - 10, 30, paddle1Height + 20, 10, 10);
 g2d.setStroke(new BasicStroke(1));
 if (!player1HollowPhased) {
 g2d.setColor(Color.CYAN);
 g2d.setFont(new Font("Arial", Font.BOLD, 8));
 g2d.drawString("HOLLOW", 2, paddle1Y - 15);
 }
 }
 if (player2HollowActive) {
 g2d.setColor(new Color(255, 255, 255, 80));
 g2d.fillRoundRect(560, paddle2Y - 15, 40, paddle2Height + 30, 15, 15);
 g2d.setColor(new Color(255, 200, 200, 200));
 g2d.setStroke(new BasicStroke(2));
 g2d.drawRoundRect(565, paddle2Y - 10, 30, paddle2Height + 20, 10, 10);
 g2d.setStroke(new BasicStroke(1));
 if (!player2HollowPhased) {
 g2d.setColor(Color.CYAN);
 g2d.setFont(new Font("Arial", Font.BOLD, 8));
 g2d.drawString("HOLLOW", 562, paddle2Y - 15);
 }
 }

 // SCREEN WARP effects - applied at the end as overlays
 // (These effects will be drawn later after all other rendering)

 // Gravity Hammer active visual
 if (player1HammerActive && player1HammerDuration > 0) {
 g2d.setColor(new Color(255, 200, 0, 100));
 g2d.fillRect(0, 0, 300, 400);
 g2d.setColor(Color.ORANGE);
 g2d.setFont(new Font("Arial", Font.BOLD, 30));
 g2d.drawString("HAMMER!", 80, 200);
 g2d.setFont(new Font("Arial", Font.PLAIN, 10));
 }
 if (player2HammerActive && player2HammerDuration > 0) {
 g2d.setColor(new Color(255, 200, 0, 100));
 g2d.fillRect(300, 0, 300, 400);
 g2d.setColor(Color.ORANGE);
 g2d.setFont(new Font("Arial", Font.BOLD, 30));
 g2d.drawString("HAMMER!", 380, 200);
 g2d.setFont(new Font("Arial", Font.PLAIN, 10));
 }
 
 // Power Siphon active visual
 if (player1SiphonActive && player1SiphonDuration > 0) {
 g2d.setColor(new Color(200, 0, 255, 100));
 for (int i = 1; i <= 5; i++) {
 g2d.drawOval(ballX + 7 - i * 10, ballY + 7 - i * 10, i * 20, i * 20);
 }
 // Drain lines to opponent
 g2d.setStroke(new BasicStroke(2));
 for (int i = 0; i < 3; i++) {
 int offsetY = (int)(Math.sin(System.currentTimeMillis() / 100.0 + i) * 20);
 g2d.drawLine(ballX + 7, ballY + 7, 580, paddle2Y + getPaddleHeight(2, shrinkPaddlesActive) / 2 + offsetY);
 }
 g2d.setStroke(new BasicStroke(1));
 }
 if (player2SiphonActive && player2SiphonDuration > 0) {
 g2d.setColor(new Color(200, 0, 255, 100));
 for (int i = 1; i <= 5; i++) {
 g2d.drawOval(ballX + 7 - i * 10, ballY + 7 - i * 10, i * 20, i * 20);
 }
 // Drain lines to opponent
 g2d.setStroke(new BasicStroke(2));
 for (int i = 0; i < 3; i++) {
 int offsetY = (int)(Math.sin(System.currentTimeMillis() / 100.0 + i) * 20);
 g2d.drawLine(ballX + 7, ballY + 7, 20, paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2 + offsetY);
 }
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Draw paddle 1 (completely invisible if player 1 is blinded)
 if (player1BlindEffectTimer == 0) {
 // BANKAI MODE - BLACK PADDLE WITH DARK AURA!
 if (player1BankaiActive) {
 // Dark aura
 g2d.setColor(new Color(50, 0, 80, 150));
 g2d.fillRoundRect(3, paddle1Y - 8, 24, paddle1Height + 16, 12, 12);
 // Black paddle with red edge
 GradientPaint bankaiGradient = new GradientPaint(10, paddle1Y, new Color(20, 0, 30), 20, paddle1Y, Color.BLACK);
 g2d.setPaint(bankaiGradient);
 g2d.fillRoundRect(10, paddle1Y, 10, paddle1Height, 5, 5);
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawRoundRect(10, paddle1Y, 10, paddle1Height, 5, 5);
 // "BANKAI" text
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawString("BAN", 3, paddle1Y - 12);
 g2d.drawString("KAI", 5, paddle1Y - 2);
 // Stack count
 if (player1BankaiStacks > 0) {
 g2d.setColor(Color.RED);
 g2d.drawString("x" + player1BankaiStacks, 8, paddle1Y + paddle1Height + 12);
 }
 } else {
 // Elastic Expansion glow
 if (elastic1Active) {
 g2d.setColor(new Color(0, 255, 100, 100));
 g2d.fillRoundRect(5, paddle1Y - 5, 20, paddle1Height + 10, 10, 10);
 }
 g2d.setColor(new Color(0, 255, 255, 80));
 g2d.fillRoundRect(7, paddle1Y - 3, 16, paddle1Height + 6, 8, 8);
 GradientPaint paddle1Gradient = new GradientPaint(10, paddle1Y, new Color(0, 200, 255), 20, paddle1Y, new Color(0, 150, 200));
 g2d.setPaint(paddle1Gradient);
 g2d.fillRoundRect(10, paddle1Y, 10, paddle1Height, 5, 5);
 }
 }
 // When blinded: paddle is COMPLETELY INVISIBLE (no outline, nothing)

 // Draw paddle 2 (completely invisible if player 2 is blinded)
 if (player2BlindEffectTimer == 0) {
 // BANKAI MODE - BLACK PADDLE WITH DARK AURA!
 if (player2BankaiActive) {
 // Dark aura
 g2d.setColor(new Color(50, 0, 80, 150));
 g2d.fillRoundRect(573, paddle2Y - 8, 24, paddle2Height + 16, 12, 12);
 // Black paddle with red edge
 GradientPaint bankaiGradient = new GradientPaint(580, paddle2Y, new Color(20, 0, 30), 590, paddle2Y, Color.BLACK);
 g2d.setPaint(bankaiGradient);
 g2d.fillRoundRect(580, paddle2Y, 10, paddle2Height, 5, 5);
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawRoundRect(580, paddle2Y, 10, paddle2Height, 5, 5);
 // "BANKAI" text
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawString("BAN", 573, paddle2Y - 12);
 g2d.drawString("KAI", 575, paddle2Y - 2);
 if (player2BankaiStacks > 0) {
 g2d.setColor(Color.RED);
 g2d.drawString("x" + player2BankaiStacks, 578, paddle2Y + paddle2Height + 12);
 }
 } else {
 // Elastic Expansion glow
 if (elastic2Active) {
 g2d.setColor(new Color(255, 100, 0, 100));
 g2d.fillRoundRect(575, paddle2Y - 5, 20, paddle2Height + 10, 10, 10);
 }
 g2d.setColor(new Color(255, 100, 100, 80));
 g2d.fillRoundRect(577, paddle2Y - 3, 16, paddle2Height + 6, 8, 8);
 GradientPaint paddle2Gradient = new GradientPaint(580, paddle2Y, new Color(255, 100, 100), 590, paddle2Y, new Color(200, 50, 50));
 g2d.setPaint(paddle2Gradient);
 g2d.fillRoundRect(580, paddle2Y, 10, paddle2Height, 5, 5);
 }
 }
 // When blinded: paddle is COMPLETELY INVISIBLE (no outline, nothing)

 // Spectral Echo effect - ghost trail
 if (player1SpectralActive > 0 || player2SpectralActive > 0) {
 // Draw ghost trail (fading copies of the ball along its path)
 for (int i = 1; i <= 5; i++) {
 int trailX = ballX - (ballVelX * i);
 int trailY = ballY - (ballVelY * i);
 int alpha = 150 - (i * 30); // Fade out
 g2d.setColor(new Color(150, 150, 255, alpha));
 g2d.fillOval(trailX, trailY, 15, 15);
 }
 }
 
 // Draw ball with danger zone effects
 float dangerRatio = dangerZoneTime / (float)maxDangerTime;
 if (dangerZoneTime > 20) {
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 100.0 + i * Math.PI / 4);
 int particleDistance = 10 + (int)(Math.random() * 8);
 int particleX = ballX + 7 + (int)(Math.cos(angle) * particleDistance);
 int particleY = ballY + 7 + (int)(Math.sin(angle) * particleDistance);
 int flameAlpha = (int)(150 * dangerRatio * Math.random());
 g2d.setColor(new Color(255, (int)(Math.random() * 200), 0, flameAlpha));
 int particleSize = 3 + (int)(Math.random() * 4);
 g2d.fillOval(particleX - particleSize/2, particleY - particleSize/2, particleSize, particleSize);
 }
 }
 
 // Fireball effect - intense flame trail
 if (fireballActive) {
 for (int i = 0; i < 12; i++) {
 double angle = (System.currentTimeMillis() / 80.0 + i * Math.PI / 6);
 int particleDistance = 12 + (int)(Math.random() * 10);
 int particleX = ballX + 7 + (int)(Math.cos(angle) * particleDistance);
 int particleY = ballY + 7 + (int)(Math.sin(angle) * particleDistance);
 int flameAlpha = (int)(180 * Math.random());
 g2d.setColor(new Color(255, (int)(Math.random() * 100), 0, flameAlpha));
 int particleSize = 4 + (int)(Math.random() * 5);
 g2d.fillOval(particleX - particleSize/2, particleY - particleSize/2, particleSize, particleSize);
 }
 // Bright core glow
 g2d.setColor(new Color(255, 200, 0, 100));
 g2d.fillOval(ballX - 10, ballY - 10, 35, 35);
 }
 
 // Teleport effect - purple sparkles
 if (teleportActive) {
 for (int i = 0; i < 8; i++) {
 double angle = (System.currentTimeMillis() / 120.0 + i * Math.PI / 4);
 int particleDistance = 15 + (int)(Math.sin(System.currentTimeMillis() / 100.0 + i) * 8);
 int particleX = ballX + 7 + (int)(Math.cos(angle) * particleDistance);
 int particleY = ballY + 7 + (int)(Math.sin(angle) * particleDistance);
 g2d.setColor(new Color(138, 43, 226, 150));
 g2d.fillOval(particleX - 2, particleY - 2, 5, 5);
 }
 }
 
 // Zigzag effect - green lightning
 if (zigzagActive) {
 g2d.setColor(new Color(50, 255, 50, 120));
 g2d.setStroke(new BasicStroke(2));
 int prevX = ballX + 7, prevY = ballY + 7;
 for (int i = 0; i < 4; i++) {
 int nextX = prevX + (int)(Math.random() * 20 - 10);
 int nextY = prevY + (int)(Math.random() * 20 - 10);
 g2d.drawLine(prevX, prevY, nextX, nextY);
 prevX = nextX;
 prevY = nextY;
 }
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Split effect - show ghost balls
 if (splitActive) {
 // Draw 2 ghost balls offset from main ball
 for (int i = 0; i < 2; i++) {
 int offsetX = (i == 0 ? -20 : 20);
 int offsetY = (int)(Math.sin(System.currentTimeMillis() / 100.0 + i * Math.PI) * 10);
 g2d.setColor(new Color(255, 255, 255, 80));
 g2d.fillOval(ballX + offsetX, ballY + offsetY, 15, 15);
 }
 }
 
 // Ghost ball effect - make ball semi-transparent with ghostly aura
 boolean ghostActive = (player1GhostEffectTimer > 0 || player2GhostEffectTimer > 0);
 if (ghostActive) {
 // Ghostly aura particles
 for (int i = 0; i < 6; i++) {
 double angle = (System.currentTimeMillis() / 150.0 + i * Math.PI / 3);
 int particleDistance = 15 + (int)(Math.sin(System.currentTimeMillis() / 200.0) * 5);
 int particleX = ballX + 7 + (int)(Math.cos(angle) * particleDistance);
 int particleY = ballY + 7 + (int)(Math.sin(angle) * particleDistance);
 g2d.setColor(new Color(200, 200, 255, 80));
 g2d.fillOval(particleX - 3, particleY - 3, 6, 6);
 }
 // Semi-transparent glow
 g2d.setColor(new Color(150, 150, 255, 40));
 g2d.fillOval(ballX - 8, ballY - 8, 31, 31);
 }
 
 // Wraith effect - draw all 3 balls
 if (wraithActive) {
 for (Ball wb : wraithBalls) {
 // Wraith aura
 for (int i = 0; i < 5; i++) {
 double angle = (System.currentTimeMillis() / 120.0 + i * Math.PI * 2 / 5);
 int particleDistance = 12 + (int)(Math.sin(System.currentTimeMillis() / 150.0) * 4);
 int particleX = wb.x + 7 + (int)(Math.cos(angle) * particleDistance);
 int particleY = wb.y + 7 + (int)(Math.sin(angle) * particleDistance);
 g2d.setColor(new Color(100, 255, 100, 100));
 g2d.fillOval(particleX - 2, particleY - 2, 5, 5);
 }
 // Draw wraith ball
 g2d.setColor(new Color(200, 255, 200, 80));
 g2d.fillOval(wb.x - 5, wb.y - 5, 25, 25);
 g2d.setColor(new Color(100, 255, 100));
 g2d.fillOval(wb.x, wb.y, 15, 15);
 g2d.setColor(new Color(200, 255, 200));
 g2d.fillOval(wb.x + 3, wb.y + 3, 9, 9);
 }
 }
 
 // Poltergeist effect - chaotic visual distortion
 if (poltergeistActive) {
 // Erratic particle cloud
 for (int i = 0; i < 10; i++) {
 int randomDist = 10 + (int)(Math.random() * 25);
 double randomAngle = Math.random() * Math.PI * 2;
 int particleX = ballX + 7 + (int)(Math.cos(randomAngle) * randomDist);
 int particleY = ballY + 7 + (int)(Math.sin(randomAngle) * randomDist);
 g2d.setColor(new Color(255, 100, 255, 80 + (int)(Math.random() * 80)));
 g2d.fillOval(particleX - 3, particleY - 3, 6, 6);
 }
 // Chaotic glow
 g2d.setColor(new Color(255, 50, 255, 60));
 g2d.fillOval(ballX - 10, ballY - 10, 35, 35);
 }
 
 g2d.setColor(new Color(255, 255, 255, 60));
 g2d.fillOval(ballX - 5, ballY - 5, 25, 25);
 
 Color ballCenter, ballMid, ballOuter;
 if (ghostActive) {
 // Ghost ball colors - ethereal blue/purple
 ballCenter = new Color(220, 220, 255, 180);
 ballMid = new Color(150, 150, 255, 150);
 ballOuter = new Color(100, 100, 200, 120);
 } else if (fireballActive) {
 // Fireball colors - bright orange/red
 ballCenter = new Color(255, 255, 200);
 ballMid = new Color(255, 150, 0);
 ballOuter = new Color(255, 50, 0);
 } else if (shrekBallActive) {
 ballCenter = new Color(100, 255, 100);
 ballMid = new Color(0, 200, 0);
 ballOuter = new Color(0, 150, 0);
 } else if (multiballActive) {
 ballCenter = new Color(255, 255, 100);
 ballMid = new Color(255, 200, 0);
 ballOuter = new Color(200, 150, 0);
 } else if (dangerRatio > 0.5) {
 ballCenter = new Color(255, 255, 200);
 ballMid = new Color(255, 150, 0);
 ballOuter = new Color(200, 50, 0);
 } else {
 ballCenter = Color.WHITE;
 ballMid = new Color(200, 200, 255);
 ballOuter = new Color(100, 100, 200);
 }
 
 // TUNNEL VISION - Check if ball should be hidden
 boolean ballHiddenByTunnel = false;
 // Player 1's tunnel vision - ball hidden when in player 1's blind zones (left side edges)
 if (player1TunnelTimer > 0) {
 if (ballX < 100 || ballY < 80 || ballY > 320) {
 ballHiddenByTunnel = true;
 }
 }
 // Player 2's tunnel vision - ball hidden when in player 2's blind zones (right side edges)
 if (player2TunnelTimer > 0) {
 if (ballX > 500 || ballY < 80 || ballY > 320) {
 ballHiddenByTunnel = true;
 }
 }

 if (!ballHiddenByTunnel) {
 if (shrekBallActive) {
 // Draw shrek sprite with dance animation
 int sx = ballX - 5;
 int sy = ballY - 5;
 int ss = 35;
 double t = System.currentTimeMillis() / 100.0;
 // Dance: wobble rotation + bounce + scale pulse
 int bounceY = (int)(Math.abs(Math.sin(t * 1.5)) * 6);
 double wobble = Math.sin(t * 2.0) * 0.3; // rotation in radians
 double scalePulse = 1.0 + Math.sin(t * 3.0) * 0.15;
 int danceSS = (int)(ss * scalePulse);
 int cx = sx + ss / 2;
 int cy = sy + ss / 2 - bounceY;
 java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
 g2d.translate(cx, cy);
 g2d.rotate(wobble);
 if (shrekSprite != null) {
  g2d.drawImage(shrekSprite, -danceSS / 2, -danceSS / 2, danceSS, danceSS, null);
 }
 g2d.setTransform(oldTransform);
 if (shrekSprite == null) {
  // Green shrek face fallback
  g2d.setColor(new Color(80, 170, 50));
  g2d.fillOval(sx, sy, ss, ss);
  // Ears
  g2d.setColor(new Color(70, 150, 40));
  g2d.fillOval(sx - 3, sy + 2, 8, 8);
  g2d.fillOval(sx + 20, sy + 2, 8, 8);
  // Eyes
  g2d.setColor(new Color(200, 180, 100));
  g2d.fillOval(sx + 5, sy + 7, 6, 5);
  g2d.fillOval(sx + 14, sy + 7, 6, 5);
  g2d.setColor(Color.BLACK);
  g2d.fillOval(sx + 7, sy + 9, 3, 3);
  g2d.fillOval(sx + 16, sy + 9, 3, 3);
  // Mouth
  g2d.setColor(new Color(60, 130, 30));
  g2d.setStroke(new BasicStroke(2));
  g2d.drawArc(sx + 5, sy + 14, 15, 8, 200, 140);
  g2d.setStroke(new BasicStroke(1));
 }
 } else {
 RadialGradientPaint ballGradient = new RadialGradientPaint(ballX + 7, ballY + 7, 10, new float[]{0.0f, 0.6f, 1.0f}, new Color[]{ballCenter, ballMid, ballOuter});
 g2d.setPaint(ballGradient);
 g2d.fillOval(ballX, ballY, 15, 15);
 }

 // ARMAMENT HAKI - Draw dark aura around ball when phase is active
 if (player1HakiPhaseActive || player2HakiPhaseActive) {
 g2d.setColor(new Color(60, 0, 80, 120));
 g2d.fillOval(ballX - 5, ballY - 5, 25, 25);
 g2d.setColor(new Color(130, 0, 200, 180));
 g2d.setStroke(new BasicStroke(2));
 g2d.drawOval(ballX - 3, ballY - 3, 21, 21);
 g2d.setStroke(new BasicStroke(1));
 }
 }

 // STORY MODE HEADER
 if (storyModeActive) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(255, 215, 0)); // Gold color
 String bossName = storyBossNames[storyModeLevel - 1];
 int scoreNeeded = storyLevelScoreToWin[storyModeLevel - 1];
 g2d.drawString("STORY MODE - LEVEL " + storyModeLevel, 230, 15);
 g2d.setColor(new Color(255, 100, 100));
 g2d.drawString("VS " + bossName, 255, 30);
 g2d.setColor(Color.WHITE);
 g2d.drawString("First to " + scoreNeeded + " wins!", 250, 45);
 }

 // Draw score
 g2d.setFont(new Font("Arial", Font.BOLD, 48));
 g2d.setColor(new Color(0, 255, 255));
 g2d.drawString(String.valueOf(scorePlayer1), 245, storyModeActive ? 75 : 55);
 g2d.setColor(new Color(255, 100, 100));
 g2d.drawString(String.valueOf(scorePlayer2), 325, storyModeActive ? 75 : 55);

 // Draw levels
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(0, 255, 255));
 g2d.drawString("LV " + level1, 15, 80);
 g2d.setColor(new Color(255, 100, 100));
 g2d.drawString((singlePlayer ? "AI " : "") + "LV " + level2, 540, 80);
 
 // Learning AI Progress Indicator
 if (learningAIEnabled && singlePlayer) {
 g2d.setFont(new Font("Arial", Font.BOLD, 11));
 int progressPercent = (int)(learningProgress * 100);
 
 // Color changes based on learning progress (much slower)
 Color progressColor;
 String progressText;
 if (learningProgress < 0.3) {
 progressColor = new Color(100, 255, 100); // Green - AI is learning
 progressText = "AI: Learning ";
 } else if (learningProgress < 0.6) {
 progressColor = new Color(255, 255, 100); // Yellow - AI is adapting
 progressText = "AI: Adapting ";
 } else if (learningProgress < 0.85) {
 progressColor = new Color(255, 180, 80); // Orange - AI is competent
 progressText = "AI: Competent ";
 } else {
 progressColor = new Color(255, 120, 120); // Light Red - AI is skilled
 progressText = "AI: Skilled ";
 }
 
 g2d.setColor(progressColor);
 g2d.drawString(progressText + progressPercent + "%", 450, 95);
 
 // Progress bar
 g2d.setColor(new Color(50, 50, 50));
 g2d.fillRect(450, 100, 120, 7);
 g2d.setColor(progressColor);
 g2d.fillRect(450, 100, (int)(120 * learningProgress), 7);
 
 // Player skill level indicator (chess coach style)
 if (totalRallies > 20) {
 g2d.setFont(new Font("Arial", Font.PLAIN, 9));
 int skillPercent = (int)(playerSkillLevel * 100);
 Color skillColor;
 if (playerSkillLevel > 0.6) {
 skillColor = new Color(100, 255, 100); // Green - player is winning
 } else if (playerSkillLevel < 0.4) {
 skillColor = new Color(255, 100, 100); // Red - player needs help
 } else {
 skillColor = new Color(255, 255, 100); // Yellow - balanced
 }
 g2d.setColor(skillColor);
 g2d.drawString("Your Win Rate: " + skillPercent + "%", 450, 115);
 }
 
 // AI adaptation info
 if (learningProgress > 0.3) {
 g2d.setFont(new Font("Arial", Font.PLAIN, 9));
 g2d.setColor(new Color(200, 200, 255));
 g2d.drawString("AI Speed: " + String.format("%.2f", aiAdaptationMultiplier) + "x", 450, 125);
 }
 }
 
 // Draw abilities list
 g2d.setFont(new Font("Arial", Font.BOLD, 9));
 g2d.setColor(new Color(100, 200, 255));
 int abilityY = 120;
 for (String ability : player1Abilities.keySet()) {
 int level = player1Abilities.get(ability);
 int branch = player1AbilityBranches.getOrDefault(ability, 0);
 
 // Show branch name if ability is branched
 String displayText = "";
 if (branch > 0) {
 displayText += getBranchName(ability, branch);
 } else {
 displayText += getAbilityShortName(ability);
 }
 
 if (level > 1) displayText += " Lv" + level;
 if (ability.equals("gun")) {
 if (player1GunTimer >= gunCooldown) displayText += " ";
 else displayText += " (" + ((gunCooldown - player1GunTimer) / 10) + ")";
 }
 if (ability.equals("ability_stealer")) {
 if (player1StealerTimer >= stealerCooldown) displayText += " ";
 else displayText += " (" + ((stealerCooldown - player1StealerTimer) / 10) + ")";
 }
 if (ability.equals("ability_stealer") && level >= 3 && branch == 1) {
 // Hijacker timer
 if (player1HijackerTimer >= hijackerCooldown) displayText += " [H: ]";
 else displayText += " [H:" + ((hijackerCooldown - player1HijackerTimer) / 10) + "]";
 }
 if (ability.equals("ability_stealer") && level >= 3 && branch == 2) {
 // Virus timer
 if (player1VirusTimer >= virusCooldown) displayText += " [V: ]";
 else displayText += " [V:" + ((virusCooldown - player1VirusTimer) / 10) + "]";
 }
 if (ability.equals("lag_spike")) {
 if (player1LagTriggerTimer > 0) displayText += " (" + (player1LagTriggerTimer / 10.0) + "s)";
 }
 if (ability.equals("reverse_controls")) {
 if (player1ReverseTimer >= reverseCooldown) displayText += " ";
 else displayText += " (" + ((reverseCooldown - player1ReverseTimer) / 10) + ")";
 }
 if (ability.equals("reverse_controls") && level >= 3 && branch == 1) {
 // Chaos Field timer
 if (player1ChaosTimer >= chaosCooldown) displayText += " [C: ]";
 else displayText += " [C:" + ((chaosCooldown - player1ChaosTimer) / 10) + "]";
 }
 if (ability.equals("reverse_controls") && level >= 3 && branch == 2) {
 // Puppet Master timer
 if (player1PuppetTimer >= puppetCooldown) displayText += " [P: ]";
 else displayText += " [P:" + ((puppetCooldown - player1PuppetTimer) / 10) + "]";
 }
 if (ability.equals("slow_opponent") && level >= 3 && branch == 1) {
 // Freeze Ray timer
 if (player1FreezeTimer >= freezeCooldown) displayText += " ";
 else displayText += " (" + ((freezeCooldown - player1FreezeTimer) / 10) + ")";
 }
 if (ability.equals("blind")) {
 if (player1BlindTimer >= blindCooldown) displayText += " ";
 else displayText += " (" + ((blindCooldown - player1BlindTimer) / 10) + ")";
 }
 if (ability.equals("shrink_opponent")) {
 if (player1ShrinkTimer >= shrinkCooldown) displayText += " ";
 else displayText += " (" + ((shrinkCooldown - player1ShrinkTimer) / 10) + ")";
 }
 if (ability.equals("ghost_ball")) {
 if (player1GhostTimer >= ghostCooldown) displayText += " ";
 else displayText += " (" + ((ghostCooldown - player1GhostTimer) / 10) + ")";
 }
 if (ability.equals("ghost_ball") && level >= 3 && branch == 2) {
 // Reality Break timer
 if (player1RealityTimer >= realityCooldown) displayText += " [R: ]";
 else displayText += " [R:" + ((realityCooldown - player1RealityTimer) / 10) + "]";
 }
 if (ability.equals("speed_boost") && level >= 3 && branch == 1) {
 // Sonic Dash timer
 int currentDashCooldown = getDashCooldown(1);
 if (player1DashTimer >= currentDashCooldown) displayText += " ";
 else displayText += " (" + ((currentDashCooldown - player1DashTimer) / 10) + ")";
 }
 if (ability.equals("speed_boost") && level >= 3 && branch == 2) {
 // Flash timer
 if (player1FlashTimer >= flashCooldown) displayText += " ";
 else displayText += " (" + ((flashCooldown - player1FlashTimer) / 10) + ")";
 }
 if (ability.equals("joshua")) {
 if (player1JoshuaActive) displayText += " [ACTIVE]";
 else if (player1JoshuaTimer >= joshuaCooldown) displayText += " ";
 else displayText += " (" + ((joshuaCooldown - player1JoshuaTimer) / 10) + ")";
 }
 if (ability.equals("jaisan")) {
 if (player1JaisanActive) displayText += " [ACTIVE]";
 else if (player1JaisanTimer >= jaisanCooldown) displayText += " ";
 else displayText += " (" + ((jaisanCooldown - player1JaisanTimer) / 10) + ")";
 }
 // NEW ABILITIES - Haki, Barrier, Trap, Screen Warp, Bankai
 if (ability.equals("haki")) {
 int hakiBr = player1AbilityBranches.getOrDefault("haki", 0);
 int hakiLv = getEffectiveAbilityLevel(1, "haki");
 int p1HakiCD = (hakiBr == 1) ? hakiArmamentCooldown - (Math.max(0, hakiLv - 2) * 30) :
                (hakiBr == 2) ? hakiObservationCooldown - (Math.max(0, hakiLv - 2) * 40) :
                hakiBaseCooldown;
 if (player1HakiTimer >= p1HakiCD) displayText += " ";
 else displayText += " (" + ((p1HakiCD - player1HakiTimer) / 10) + ")";
 }
 if (ability.equals("barrier")) {
 if (player1BarrierActive) displayText += " [ACTIVE]";
 else if (player1BarrierTimer >= barrierCooldown) displayText += " ";
 else displayText += " (" + ((barrierCooldown - player1BarrierTimer) / 10) + ")";
 }
 if (ability.equals("trap")) {
 if (player1TrapActive) displayText += " [SET]";
 else if (player1TrapTimer >= trapCooldown) displayText += " ";
 else displayText += " (" + ((trapCooldown - player1TrapTimer) / 10) + ")";
 }
 if (ability.equals("screen_warp")) {
 if (player1WarpTimer >= warpCooldown) displayText += " ";
 else displayText += " (" + ((warpCooldown - player1WarpTimer) / 10) + ")";
 }
 if (ability.equals("bankai")) {
 if (player1BankaiActive) displayText += " [BANKAI!] x" + player1BankaiStacks;
 else if (player1BankaiTimer >= bankaiCooldown) displayText += " ";
 else displayText += " (" + ((bankaiCooldown - player1BankaiTimer) / 10) + ")";
 }
 g2d.drawString(displayText, 10, abilityY);
 abilityY += 11;
 }
 
 g2d.setColor(new Color(255, 150, 150));
 abilityY = 120;
 for (String ability : player2Abilities.keySet()) {
 int level = player2Abilities.get(ability);
 int branch = player2AbilityBranches.getOrDefault(ability, 0);
 
 // Show branch name if ability is branched
 String displayText = "";
 if (branch > 0) {
 displayText += getBranchName(ability, branch);
 } else {
 displayText += getAbilityShortName(ability);
 }
 
 if (level > 1) displayText += " Lv" + level;
 if (ability.equals("gun")) {
 if (player2GunTimer >= gunCooldown) displayText += " ";
 else displayText += " (" + ((gunCooldown - player2GunTimer) / 10) + ")";
 }
 if (ability.equals("ability_stealer")) {
 if (player2StealerTimer >= stealerCooldown) displayText += " ";
 else displayText += " (" + ((stealerCooldown - player2StealerTimer) / 10) + ")";
 }
 if (ability.equals("ability_stealer") && level >= 3 && branch == 1) {
 // Hijacker timer
 if (player2HijackerTimer >= hijackerCooldown) displayText += " [H: ]";
 else displayText += " [H:" + ((hijackerCooldown - player2HijackerTimer) / 10) + "]";
 }
 if (ability.equals("ability_stealer") && level >= 3 && branch == 2) {
 // Virus timer
 if (player2VirusTimer >= virusCooldown) displayText += " [V: ]";
 else displayText += " [V:" + ((virusCooldown - player2VirusTimer) / 10) + "]";
 }
 if (ability.equals("lag_spike")) {
 if (player2LagTriggerTimer > 0) displayText += " (" + (player2LagTriggerTimer / 10.0) + "s)";
 }
 if (ability.equals("reverse_controls")) {
 if (player2ReverseTimer >= reverseCooldown) displayText += " ";
 else displayText += " (" + ((reverseCooldown - player2ReverseTimer) / 10) + ")";
 }
 if (ability.equals("reverse_controls") && level >= 3 && branch == 1) {
 // Chaos Field timer
 if (player2ChaosTimer >= chaosCooldown) displayText += " [C: ]";
 else displayText += " [C:" + ((chaosCooldown - player2ChaosTimer) / 10) + "]";
 }
 if (ability.equals("reverse_controls") && level >= 3 && branch == 2) {
 // Puppet Master timer
 if (player2PuppetTimer >= puppetCooldown) displayText += " [P: ]";
 else displayText += " [P:" + ((puppetCooldown - player2PuppetTimer) / 10) + "]";
 }
 if (ability.equals("slow_opponent") && level >= 3 && branch == 1) {
 // Freeze Ray timer
 if (player2FreezeTimer >= freezeCooldown) displayText += " ";
 else displayText += " (" + ((freezeCooldown - player2FreezeTimer) / 10) + ")";
 }
 if (ability.equals("blind")) {
 if (player2BlindTimer >= blindCooldown) displayText += " ";
 else displayText += " (" + ((blindCooldown - player2BlindTimer) / 10) + ")";
 }
 if (ability.equals("shrink_opponent")) {
 if (player2ShrinkTimer >= shrinkCooldown) displayText += " ";
 else displayText += " (" + ((shrinkCooldown - player2ShrinkTimer) / 10) + ")";
 }
 if (ability.equals("ghost_ball")) {
 if (player2GhostTimer >= ghostCooldown) displayText += " ";
 else displayText += " (" + ((ghostCooldown - player2GhostTimer) / 10) + ")";
 }
 if (ability.equals("ghost_ball") && level >= 3 && branch == 2) {
 // Reality Break timer
 if (player2RealityTimer >= realityCooldown) displayText += " [R: ]";
 else displayText += " [R:" + ((realityCooldown - player2RealityTimer) / 10) + "]";
 }
 if (ability.equals("speed_boost") && level >= 3 && branch == 1) {
 // Sonic Dash timer
 int currentDashCooldown = getDashCooldown(2);
 if (player2DashTimer >= currentDashCooldown) displayText += " ";
 else displayText += " (" + ((currentDashCooldown - player2DashTimer) / 10) + ")";
 }
 if (ability.equals("speed_boost") && level >= 3 && branch == 2) {
 // Flash timer
 if (player2FlashTimer >= flashCooldown) displayText += " ";
 else displayText += " (" + ((flashCooldown - player2FlashTimer) / 10) + ")";
 }
 if (ability.equals("joshua")) {
 if (player2JoshuaActive) displayText += " [ACTIVE]";
 else if (player2JoshuaTimer >= joshuaCooldown) displayText += " ";
 else displayText += " (" + ((joshuaCooldown - player2JoshuaTimer) / 10) + ")";
 }
 if (ability.equals("jaisan")) {
 if (player2JaisanActive) displayText += " [ACTIVE]";
 else if (player2JaisanTimer >= jaisanCooldown) displayText += " ";
 else displayText += " (" + ((jaisanCooldown - player2JaisanTimer) / 10) + ")";
 }
 // NEW ABILITIES - Haki, Barrier, Trap, Screen Warp, Bankai
 if (ability.equals("haki")) {
 int hakiBr2 = player2AbilityBranches.getOrDefault("haki", 0);
 int hakiLv2 = getEffectiveAbilityLevel(2, "haki");
 int p2HakiCD = (hakiBr2 == 1) ? hakiArmamentCooldown - (Math.max(0, hakiLv2 - 2) * 30) :
                (hakiBr2 == 2) ? hakiObservationCooldown - (Math.max(0, hakiLv2 - 2) * 40) :
                hakiBaseCooldown;
 if (player2HakiTimer >= p2HakiCD) displayText += " ";
 else displayText += " (" + ((p2HakiCD - player2HakiTimer) / 10) + ")";
 }
 if (ability.equals("barrier")) {
 if (player2BarrierActive) displayText += " [ACTIVE]";
 else if (player2BarrierTimer >= barrierCooldown) displayText += " ";
 else displayText += " (" + ((barrierCooldown - player2BarrierTimer) / 10) + ")";
 }
 if (ability.equals("trap")) {
 if (player2TrapActive) displayText += " [SET]";
 else if (player2TrapTimer >= trapCooldown) displayText += " ";
 else displayText += " (" + ((trapCooldown - player2TrapTimer) / 10) + ")";
 }
 if (ability.equals("screen_warp")) {
 if (player2WarpTimer >= warpCooldown) displayText += " ";
 else displayText += " (" + ((warpCooldown - player2WarpTimer) / 10) + ")";
 }
 if (ability.equals("bankai")) {
 if (player2BankaiActive) displayText += " [BANKAI!] x" + player2BankaiStacks;
 else if (player2BankaiTimer >= bankaiCooldown) displayText += " ";
 else displayText += " (" + ((bankaiCooldown - player2BankaiTimer) / 10) + ")";
 }
 int width = g2d.getFontMetrics().stringWidth(displayText);
 g2d.drawString(displayText, 580 - width, abilityY);
 abilityY += 11;
 }
 
 // Draw power-ups
 for (PowerUp powerUp : powerUps) drawPowerUp(g2d, powerUp);
 
 // Draw bullets
 for (Bullet bullet : bullets) {
 g2d.setColor(bullet.owner == 1 ? new Color(0, 255, 255) : new Color(255, 100, 100));
 g2d.fillOval(bullet.x - 5, bullet.y - 5, 10, 10);
 g2d.setColor(Color.WHITE);
 g2d.fillOval(bullet.x - 2, bullet.y - 2, 4, 4);
 }

 // Draw ZANGETSU SWORD SWING animation
 if (player1SwordSwingTimer > 0) {
 int swingProgress = swordSwingDuration - player1SwordSwingTimer;
 double swingAngle = -Math.PI/4 + (swingProgress / (double)swordSwingDuration) * Math.PI/2;
 int swordLength = 80;
 int startX = 25;
 int startY = paddle1Y + paddle1Height / 2;
 int endX = startX + (int)(Math.cos(swingAngle) * swordLength);
 int endY = startY + (int)(Math.sin(swingAngle) * swordLength);

 // Sword trail effect
 g2d.setStroke(new BasicStroke(8));
 g2d.setColor(new Color(50, 0, 80, 150));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(4));
 g2d.setColor(new Color(0, 0, 0));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(2));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(1));

 // "ZANGETSU!" text
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawString("ZANGETSU!", 30, paddle1Y - 20);
 }
 if (player2SwordSwingTimer > 0) {
 int swingProgress = swordSwingDuration - player2SwordSwingTimer;
 double swingAngle = Math.PI - (-Math.PI/4 + (swingProgress / (double)swordSwingDuration) * Math.PI/2);
 int swordLength = 80;
 int startX = 575;
 int startY = paddle2Y + paddle2Height / 2;
 int endX = startX + (int)(Math.cos(swingAngle) * swordLength);
 int endY = startY + (int)(Math.sin(swingAngle) * swordLength);

 g2d.setStroke(new BasicStroke(8));
 g2d.setColor(new Color(50, 0, 80, 150));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(4));
 g2d.setColor(new Color(0, 0, 0));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(2));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawLine(startX, startY, endX, endY);
 g2d.setStroke(new BasicStroke(1));

 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(150, 0, 0));
 g2d.drawString("ZANGETSU!", 480, paddle2Y - 20);
 }

 // Draw GETSUGA TENSHO projectiles
 for (GetsugaTensho gt : getsugaProjectiles) {
 int pulseSize = (int)(5 * Math.sin(System.currentTimeMillis() / 50.0));

 // Outer dark aura
 g2d.setColor(new Color(30, 0, 50, 150));
 g2d.fillOval(gt.x - gt.width/2 - 10 - pulseSize, gt.y - gt.height/2 - 10 - pulseSize,
 gt.width + 20 + pulseSize*2, gt.height + 20 + pulseSize*2);

 // Main crescent shape (black with red edge)
 int[] xPoints = {gt.x - gt.width/2, gt.x, gt.x + gt.width/2, gt.x};
 int[] yPoints = {gt.y, gt.y - gt.height/2, gt.y, gt.y + gt.height/2};
 g2d.setColor(Color.BLACK);
 g2d.fillPolygon(xPoints, yPoints, 4);
 g2d.setColor(new Color(150, 0, 0));
 g2d.setStroke(new BasicStroke(3));
 g2d.drawPolygon(xPoints, yPoints, 4);
 g2d.setStroke(new BasicStroke(1));

 // "GETSUGA TENSHO!" text for evolved version
 if (gt.isEvolved) {
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.setColor(new Color(255, 50, 50));
 g2d.drawString("GETSUGA", gt.x - 25, gt.y - gt.height/2 - 15);
 g2d.drawString("TENSHO!", gt.x - 25, gt.y - gt.height/2 - 3);
 }
 }

 // Draw Vegeta bullets (Vegeta branch - Big Bang Attack)
 for (VegetaBullet vb : vegetaBullets) {
 // Purple/yellow energy sphere with pulsing effect
 int pulseSize = (int)(3 * Math.sin(System.currentTimeMillis() / 100.0));
 
 // Outer glow (purple)
 g2d.setColor(new Color(200, 0, 255, 100));
 g2d.fillOval(vb.x - 15 - pulseSize, vb.y - 15 - pulseSize, 30 + pulseSize * 2, 30 + pulseSize * 2);
 
 // Middle layer (mix of purple and yellow)
 g2d.setColor(new Color(220, 100, 255, 180));
 g2d.fillOval(vb.x - 12, vb.y - 12, 24, 24);
 
 // Inner core (bright yellow)
 g2d.setColor(new Color(255, 255, 100));
 g2d.fillOval(vb.x - 8, vb.y - 8, 16, 16);
 
 // Very bright center
 g2d.setColor(Color.WHITE);
 g2d.fillOval(vb.x - 4, vb.y - 4, 8, 8);
 
 // Energy particles around the sphere
 for (int i = 0; i < 4; i++) {
 double angle = (System.currentTimeMillis() / 80.0 + i * Math.PI / 2);
 int particleX = vb.x + (int)(Math.cos(angle) * 18);
 int particleY = vb.y + (int)(Math.sin(angle) * 18);
 g2d.setColor(new Color(255, 200, 255, 150));
 g2d.fillOval(particleX - 3, particleY - 3, 6, 6);
 }
 }
 
 // Draw explosions (Vegeta branch)
 for (Explosion exp : explosions) {
 float alpha = exp.duration / 30.0f; // Fade out over duration
 
 // Outer explosion ring (purple)
 g2d.setColor(new Color(200, 0, 255, (int)(80 * alpha)));
 g2d.fillOval(exp.x - exp.radius, exp.y - exp.radius, exp.radius * 2, exp.radius * 2);
 
 // Middle ring (bright purple/yellow mix)
 g2d.setColor(new Color(255, 150, 255, (int)(150 * alpha)));
 g2d.fillOval(exp.x - exp.radius * 3 / 4, exp.y - exp.radius * 3 / 4, exp.radius * 3 / 2, exp.radius * 3 / 2);
 
 // Inner explosion core (yellow)
 g2d.setColor(new Color(255, 255, 100, (int)(200 * alpha)));
 g2d.fillOval(exp.x - exp.radius / 2, exp.y - exp.radius / 2, exp.radius, exp.radius);
 
 // Very bright center
 g2d.setColor(new Color(255, 255, 255, (int)(250 * alpha)));
 g2d.fillOval(exp.x - exp.radius / 4, exp.y - exp.radius / 4, exp.radius / 2, exp.radius / 2);
 
 // Explosion particles
 for (int i = 0; i < 12; i++) {
 double angle = i * Math.PI / 6;
 int distance = (int)(exp.radius * (1.2 - alpha * 0.3)); // Particles move outward
 int particleX = exp.x + (int)(Math.cos(angle) * distance);
 int particleY = exp.y + (int)(Math.sin(angle) * distance);
 g2d.setColor(new Color(255, 200, 100, (int)(180 * alpha)));
 g2d.fillOval(particleX - 4, particleY - 4, 8, 8);
 }
 }
 
 // Draw lasers (Goku branch)
 for (Laser laser : activeLasers) {
 Color laserColor = laser.owner == 1 ? new Color(0, 255, 255) : new Color(255, 100, 100);
 
 // Get gun level for thickness calculation
 int gunLevel = (laser.owner == 1) ? player1Abilities.getOrDefault("gun", 3) : player2Abilities.getOrDefault("gun", 3);
 // Base thickness at level 3 (much thicker now), then 1.5x per level
 float thicknessMultiplier = (float)Math.pow(1.5, gunLevel - 3);
 
 // Outer glow - scales with level (base 80 instead of 48)
 g2d.setStroke(new BasicStroke(80 * thicknessMultiplier));
 g2d.setColor(new Color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue(), 30));
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Middle layer - scales with level (base 50 instead of 24)
 g2d.setStroke(new BasicStroke(50 * thicknessMultiplier));
 g2d.setColor(new Color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue(), 150));
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Core beam - scales with level (base 25 instead of 12)
 g2d.setStroke(new BasicStroke(25 * thicknessMultiplier));
 g2d.setColor(Color.WHITE);
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Reset stroke
 g2d.setStroke(new BasicStroke(1));
 
 // Warning indicator when laser is about to fire (last 10 frames of cooldown)
 if (laser.owner == 1 && player1GunTimer >= gunCooldown - 10 && player1GunTimer < gunCooldown) {
 g2d.setColor(new Color(255, 255, 0, 100 + (10 - (gunCooldown - player1GunTimer)) * 15));
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.drawString(" ", 580, laser.startY);
 } else if (laser.owner == 2 && player2GunTimer >= gunCooldown - 10 && player2GunTimer < gunCooldown) {
 g2d.setColor(new Color(255, 255, 0, 100 + (10 - (gunCooldown - player2GunTimer)) * 15));
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.drawString(" ", 10, laser.startY);
 }
 }
 
 // Draw stealer bullets
 for (StealerBullet sb : stealerBullets) {
 g2d.setColor(sb.owner == 1 ? new Color(255, 0, 255, 200) : new Color(255, 0, 255, 200));
 g2d.fillOval(sb.x - 8, sb.y - 8, 16, 16);
 g2d.setColor(new Color(200, 0, 200));
 g2d.fillOval(sb.x - 5, sb.y - 5, 10, 10);
 g2d.setColor(new Color(255, 100, 255));
 g2d.fillOval(sb.x - 2, sb.y - 2, 4, 4);
 }
 
 // Draw hijacker bullets (purple/blue theme)
 for (HijackerBullet hb : hijackerBullets) {
 g2d.setColor(hb.owner == 1 ? new Color(100, 50, 255, 220) : new Color(100, 50, 255, 220));
 g2d.fillOval(hb.x - 10, hb.y - 10, 20, 20);
 g2d.setColor(new Color(150, 100, 255));
 g2d.fillOval(hb.x - 6, hb.y - 6, 12, 12);
 g2d.setColor(new Color(200, 150, 255));
 g2d.fillOval(hb.x - 3, hb.y - 3, 6, 6);
 // Add a glowing effect
 g2d.setColor(new Color(150, 100, 255, 50));
 g2d.fillOval(hb.x - 15, hb.y - 15, 30, 30);
 }
 
 // Draw virus bullets (green/toxic theme)
 for (VirusBullet vb : virusBullets) {
 g2d.setColor(vb.owner == 1 ? new Color(50, 255, 50, 220) : new Color(50, 255, 50, 220));
 g2d.fillOval(vb.x - 10, vb.y - 10, 20, 20);
 g2d.setColor(new Color(100, 255, 100));
 g2d.fillOval(vb.x - 6, vb.y - 6, 12, 12);
 g2d.setColor(new Color(150, 255, 150));
 g2d.fillOval(vb.x - 3, vb.y - 3, 6, 6);
 // Add toxic aura effect
 g2d.setColor(new Color(50, 200, 50, 30));
 for (int i = 0; i < 3; i++) {
 g2d.drawOval(vb.x - 12 - i*4, vb.y - 12 - i*4, 24 + i*8, 24 + i*8);
 }
 }
 
 // Draw Frieza lasers (purple death beams - similar to Goku but purple)
 for (FreezaLaser laser : freezaLasers) {
 Color laserColor = new Color(200, 0, 255); // Purple/violet
 
 // Calculate thickness based on level (marginally thicker)
 int baseThickness = laser.thickness;
 
 // Outer glow - purple aura
 g2d.setStroke(new BasicStroke(baseThickness * 8));
 g2d.setColor(new Color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue(), 30));
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Middle layer - brighter purple
 g2d.setStroke(new BasicStroke(baseThickness * 5));
 g2d.setColor(new Color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue(), 150));
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Core beam - white/pink center
 g2d.setStroke(new BasicStroke(baseThickness * 2.5f));
 g2d.setColor(new Color(255, 150, 255));
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Inner core - pure white
 g2d.setStroke(new BasicStroke(baseThickness));
 g2d.setColor(Color.WHITE);
 g2d.drawLine(laser.startX, laser.startY, laser.endX, laser.endY);
 
 // Reset stroke
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Draw Jiren bullets (purple explosive projectiles - similar to Vegeta but purple)
 for (JirenBullet jb : jirenBullets) {
 // Purple explosive energy ball
 g2d.setColor(new Color(150, 0, 200, 100));
 g2d.fillOval(jb.x - 20, jb.y - 20, 40, 40);
 
 g2d.setColor(new Color(180, 0, 220, 180));
 g2d.fillOval(jb.x - 15, jb.y - 15, 30, 30);
 
 g2d.setColor(new Color(200, 50, 255, 220));
 g2d.fillOval(jb.x - 10, jb.y - 10, 20, 20);
 
 g2d.setColor(new Color(255, 100, 255));
 g2d.fillOval(jb.x - 5, jb.y - 5, 10, 10);
 
 // White core
 g2d.setColor(Color.WHITE);
 g2d.fillOval(jb.x - 2, jb.y - 2, 4, 4);
 }
 
 // Draw stun indicators
 if (player1StunTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 255, 0));
 g2d.drawString("STUNNED!", 10, paddle1Y - 10);
 } else if (player1StunImmunityTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.setColor(new Color(100, 255, 100));
 g2d.drawString("IMMUNE (" + (player1StunImmunityTimer / 100.0) + "s)", 10, paddle1Y - 10);
 }
 
 // Draw lag effect (now actually freezes paddle)
 if (player1LagEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 0, 255));
 g2d.drawString("LAGGING!", 10, paddle1Y - 10);
 // Visual glitch effect
 for (int i = 0; i < 5; i++) {
 int offsetX = (int)(Math.random() * 10 - 5);
 int offsetY = (int)(Math.random() * 10 - 5);
 g2d.setColor(new Color(255, 0, 255, 50));
 g2d.fillRoundRect(7 + offsetX, paddle1Y - 3 + offsetY, 16, paddle1Height + 6, 8, 8);
 }
 }
 
 // Draw reverse controls indicator
 if (player1ReverseEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(255, 150, 0));
 g2d.drawString("REVERSED!", 10, paddle1Y + paddle1Height + 20);
 }
 
 // Draw JOSHUA mode indicator
 if (player1JoshuaActive) {
 g2d.setFont(new Font("Arial", Font.BOLD, 24));
 g2d.setColor(new Color(255, 215, 0));
 g2d.drawString(" JOSHUA MODE ", 10, paddle1Y - 35);
 // Golden aura effect
 for (int i = 0; i < 3; i++) {
 g2d.setColor(new Color(255, 215, 0, 30));
 g2d.fillRoundRect(7 - i * 5, paddle1Y - 3 - i * 5, 16 + i * 10, paddle1Height + 6 + i * 10, 8, 8);
 }
 }
 
 // Draw Sonic dash indicator
 if (player1DashActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(0, 200, 255));
 g2d.drawString("DASH!", 10, paddle1Y - 10);
 // Speed lines effect
 for (int i = 0; i < 5; i++) {
 int lineX = 7 - i * 15;
 g2d.setColor(new Color(0, 200, 255, 150 - i * 30));
 g2d.setStroke(new BasicStroke(2));
 g2d.drawLine(lineX, paddle1Y + i * 15, lineX - 10, paddle1Y + i * 15);
 }
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Draw Flash indicator (slowing time)
 if (player1FlashActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 255, 100));
 g2d.drawString(" FLASH!", 10, paddle1Y - 10);
 // Time distortion effect around ball
 g2d.setColor(new Color(255, 255, 0, 30));
 for (int i = 0; i < 3; i++) {
 g2d.drawOval(ballX - 30 - i * 15, ballY - 30 - i * 15, 75 + i * 30, 75 + i * 30);
 }
 }
 
 // Draw Freeze Ray indicator
 if (player2FreezeActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(100, 200, 255));
 g2d.drawString("FROZEN!", 10, paddle1Y - 25);
 // Ice effect
 g2d.setColor(new Color(150, 220, 255, 100));
 g2d.fillRoundRect(7, paddle1Y, 10, paddle1Height, 5, 5);
 }
 
 
 // Draw blind effect indicator
 if (player1BlindEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(50, 50, 50));
 g2d.drawString("BLINDED!", 10, paddle1Y - 40);
 }
 
 // Draw shrink effect indicator
 if (player1ShrinkEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(255, 0, 255));
 g2d.drawString("SHRUNK!", 10, paddle1Y - 55);
 }
 
 // Draw ghost ball indicator
 if (player1GhostEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(200, 200, 200));
 g2d.drawString("GHOST BALL!", 10, paddle1Y - 70);
 }
 
 // Draw MIRROR effect - large indicator
 if (mirrorActive) {
 // Large center text
 g2d.setFont(new Font("Arial", Font.BOLD, 36));
 g2d.setColor(new Color(0, 191, 255, 150));
 String mirrorText = " MIRRORED ";
 int textWidth = g2d.getFontMetrics().stringWidth(mirrorText);
 g2d.drawString(mirrorText, (600 - textWidth) / 2, 200);
 
 // Border effect
 g2d.setStroke(new BasicStroke(5));
 g2d.setColor(new Color(0, 191, 255, 100));
 g2d.drawRect(5, 5, 590, 390);
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Draw TIME LOOP SLOW MOTION effect
 if (player1TimeLoopSlowMoTimer > 0 || player2TimeLoopSlowMoTimer > 0) {
 // Subtle blue tint overlay
 g2d.setColor(new Color(100, 200, 255, 30));
 g2d.fillRect(0, 0, 600, 400);
 
 // "SLOW MOTION" text
 g2d.setFont(new Font("Arial", Font.BOLD, 24));
 g2d.setColor(new Color(100, 200, 255, 180));
 String slowMoText = " SLOW MOTION ";
 int textWidth = g2d.getFontMetrics().stringWidth(slowMoText);
 g2d.drawString(slowMoText, (600 - textWidth) / 2, 30);
 
 // Reset font
 g2d.setFont(new Font("Arial", Font.PLAIN, 12));
 g2d.setStroke(new BasicStroke(1));
 
 // Timer
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(0, 191, 255));
 g2d.drawString("Time: " + ((maxMirrorDuration - mirrorDuration) / 100.0) + "s", 250, 230);
 }
 
 if (player2StunTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 255, 0));
 g2d.drawString("STUNNED!", 480, paddle2Y - 10);
 } else if (player2StunImmunityTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.setColor(new Color(100, 255, 100));
 g2d.drawString("IMMUNE (" + (player2StunImmunityTimer / 100.0) + "s)", 480, paddle2Y - 10);
 }
 
 // Draw lag effect (now actually freezes paddle)
 if (player2LagEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 0, 255));
 g2d.drawString("LAGGING!", 480, paddle2Y - 10);
 // Visual glitch effect
 for (int i = 0; i < 5; i++) {
 int offsetX = (int)(Math.random() * 10 - 5);
 int offsetY = (int)(Math.random() * 10 - 5);
 g2d.setColor(new Color(255, 0, 255, 50));
 g2d.fillRoundRect(577 + offsetX, paddle2Y - 3 + offsetY, 16, paddle2Height + 6, 8, 8);
 }
 }
 
 // Draw reverse controls indicator
 if (player2ReverseEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(255, 150, 0));
 g2d.drawString("REVERSED!", 480, paddle2Y + paddle2Height + 20);
 }
 
 // Draw JOSHUA mode indicator
 if (player2JoshuaActive) {
 g2d.setFont(new Font("Arial", Font.BOLD, 24));
 g2d.setColor(new Color(255, 215, 0));
 g2d.drawString(" JOSHUA MODE ", 380, paddle2Y - 35);
 // Golden aura effect
 for (int i = 0; i < 3; i++) {
 g2d.setColor(new Color(255, 215, 0, 30));
 g2d.fillRoundRect(577 - i * 5, paddle2Y - 3 - i * 5, 16 + i * 10, paddle2Height + 6 + i * 10, 8, 8);
 }
 }
 
 // Draw Sonic dash indicator
 if (player2DashActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 100, 100));
 g2d.drawString("DASH!", 530, paddle2Y - 10);
 // Speed lines effect
 for (int i = 0; i < 5; i++) {
 int lineX = 593 + i * 15;
 g2d.setColor(new Color(255, 100, 100, 150 - i * 30));
 g2d.setStroke(new BasicStroke(2));
 g2d.drawLine(lineX, paddle2Y + i * 15, lineX + 10, paddle2Y + i * 15);
 }
 g2d.setStroke(new BasicStroke(1));
 }
 
 // Draw Flash indicator (slowing time)
 if (player2FlashActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 16));
 g2d.setColor(new Color(255, 255, 100));
 g2d.drawString(" FLASH!", 490, paddle2Y - 10);
 // Time distortion effect already drawn around ball
 }
 
 // Draw Freeze Ray indicator
 if (player1FreezeActive > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(100, 200, 255));
 g2d.drawString("FROZEN!", 500, paddle2Y - 25);
 // Ice effect
 g2d.setColor(new Color(150, 220, 255, 100));
 g2d.fillRoundRect(583, paddle2Y, 10, paddle2Height, 5, 5);
 }
 
 
 // Draw blind effect indicator
 if (player2BlindEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(50, 50, 50));
 g2d.drawString("BLINDED!", 480, paddle2Y - 40);
 }
 
 // Draw shrink effect indicator
 if (player2ShrinkEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(255, 0, 255));
 g2d.drawString("SHRUNK!", 480, paddle2Y - 55);
 }
 
 // Draw ghost ball indicator
 if (player2GhostEffectTimer > 0) {
 g2d.setFont(new Font("Arial", Font.BOLD, 14));
 g2d.setColor(new Color(200, 200, 200));
 g2d.drawString("GHOST BALL!", 460, paddle2Y - 70);
 }
 
 // Ability cooldown indicators for Player 1 - now with countdown timers
 g2d.setFont(new Font("Arial", Font.PLAIN, 10));
 int yOffset = 350;
 if (getEffectiveAbilityLevel(1, "gravity_hammer") > 0) {
 String timerText;
 if (player1HammerTimer >= hammerCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Hammer ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (hammerCooldown - player1HammerTimer) / 100.0;
 timerText = "Hammer (" + String.format("%.1f", timeLeft) + "s)";
 }
 g2d.drawString(timerText, 10, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(1, "portal_pong") > 0) {
 String timerText;
 int portalLevel = player1Abilities.getOrDefault("portal_pong", 1);
 int portalCooldown = 1600 - (Math.min(portalLevel - 1, 2) * 200); // 16s, 14s, 12s
 if (player1PortalTimer >= portalCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Portal READY";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (portalCooldown - player1PortalTimer) / 100.0;
 timerText = "Portal (" + String.format("%.1f", timeLeft) + "s)";
 }
 g2d.drawString(timerText, 10, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(1, "power_siphon") > 0) {
 String timerText;
 if (player1SiphonTimer >= siphonCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Siphon ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (siphonCooldown - player1SiphonTimer) / 100.0;
 timerText = "Siphon (" + String.format("%.1f", timeLeft) + "s)";
 }
 g2d.drawString(timerText, 10, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(1, "time_loop") > 0) {
 String timerText;
 int timeLoopLevel = getEffectiveAbilityLevel(1, "time_loop");
 int currentCooldown = timeLoopCooldown - (timeLoopLevel - 1) * 30; // Reduce cooldown by 0.3s per level
 if (player1TimeLoopTimer >= currentCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "TimeLoop ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (currentCooldown - player1TimeLoopTimer) / 100.0;
 timerText = "TimeLoop (" + String.format("%.1f", timeLeft) + "s)";
 }
 g2d.drawString(timerText, 10, yOffset);
 }
 
 // Same for Player 2 on the right side - now with countdown timers
 yOffset = 350;
 if (getEffectiveAbilityLevel(2, "gravity_hammer") > 0) {
 String timerText;
 if (player2HammerTimer >= hammerCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Hammer ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (hammerCooldown - player2HammerTimer) / 100.0;
 timerText = "Hammer (" + String.format("%.1f", timeLeft) + "s)";
 }
 int width = g2d.getFontMetrics().stringWidth(timerText);
 g2d.drawString(timerText, 590 - width, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(2, "portal_pong") > 0) {
 String timerText;
 int portalLevel = player2Abilities.getOrDefault("portal_pong", 1);
 int portalCooldown = 1600 - (Math.min(portalLevel - 1, 2) * 200); // 16s, 14s, 12s
 if (player2PortalTimer >= portalCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Portal READY";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (portalCooldown - player2PortalTimer) / 100.0;
 timerText = "Portal (" + String.format("%.1f", timeLeft) + "s)";
 }
 int width = g2d.getFontMetrics().stringWidth(timerText);
 g2d.drawString(timerText, 590 - width, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(2, "power_siphon") > 0) {
 String timerText;
 if (player2SiphonTimer >= siphonCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "Siphon ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (siphonCooldown - player2SiphonTimer) / 100.0;
 timerText = "Siphon (" + String.format("%.1f", timeLeft) + "s)";
 }
 int width = g2d.getFontMetrics().stringWidth(timerText);
 g2d.drawString(timerText, 590 - width, yOffset);
 yOffset += 15;
 }
 if (getEffectiveAbilityLevel(2, "time_loop") > 0) {
 String timerText;
 int timeLoopLevel = getEffectiveAbilityLevel(2, "time_loop");
 int currentCooldown = timeLoopCooldown - (timeLoopLevel - 1) * 30; // Reduce cooldown by 0.3s per level
 if (player2TimeLoopTimer >= currentCooldown) {
 g2d.setColor(Color.GREEN);
 timerText = "TimeLoop ";
 } else {
 g2d.setColor(Color.RED);
 double timeLeft = (currentCooldown - player2TimeLoopTimer) / 100.0;
 timerText = "TimeLoop (" + String.format("%.1f", timeLeft) + "s)";
 }
 int width = g2d.getFontMetrics().stringWidth(timerText);
 g2d.drawString(timerText, 590 - width, yOffset);
 }
 
 // Draw legend
 drawLegend(g2d);
 
 // Draw pause overlay
 if (isPaused) {
 g2d.setColor(new Color(0, 0, 0, 150));
 g2d.fillRect(0, 0, 600, 400);
 g2d.setFont(new Font("Arial", Font.BOLD, 60));
 g2d.setColor(Color.WHITE);
 g2d.drawString("PAUSED", 185, 200);
 g2d.setFont(new Font("Arial", Font.PLAIN, 20));
 g2d.drawString("Press P or ESC to resume", 165, 230);
 }
 
 // Draw jumpscare
 if (jumpscareActive) {
 int flashIntensity = (int)(Math.sin(jumpscareDuration * 0.5) * 100 + 155);
 g2d.setColor(new Color(flashIntensity, 0, 0, 200));
 g2d.fillRect(0, 0, 600, 400);
 for (int i = 0; i < 10; i++) {
 int x = (int)(Math.random() * 600), y = (int)(Math.random() * 400);
 int w = (int)(Math.random() * 100 + 50), h = (int)(Math.random() * 50 + 20);
 g2d.setColor(new Color(0, 0, 0, (int)(Math.random() * 200)));
 g2d.fillRect(x, y, w, h);
 }
 g2d.setFont(new Font("Arial", Font.BOLD, 180));
 g2d.setColor(Color.BLACK);
 if (jumpscareImage.isEmpty()) {
 String[] scaryFaces = {" ", " ", " "};
 jumpscareImage = scaryFaces[(int)(Math.random() * scaryFaces.length)];
 }
 int shakeX = 200 + (int)(Math.random() * 20 - 10);
 int shakeY = 250 + (int)(Math.random() * 20 - 10);
 g2d.drawString(jumpscareImage, shakeX, shakeY);
 g2d.setFont(new Font("Arial", Font.BOLD, 40));
 g2d.setColor(new Color(255, 0, 0));
 int textShakeX = 180 + (int)(Math.random() * 10 - 5);
 int textShakeY = 320 + (int)(Math.random() * 10 - 5);
 g2d.drawString("BOO!", textShakeX, textShakeY);
 }
 
 // Draw Shrek ball effect
 if (shrekBallActive && shrekJumpscareTimer < 50) {
 int greenFlash = (int)(Math.sin(shrekJumpscareTimer * 0.8) * 100 + 155);
 g2d.setColor(new Color(0, greenFlash, 0, 200));
 g2d.fillRect(0, 0, 600, 400);
 // Shrek face pop-up in center
 if (shrekSprite != null) {
  int imgSize = 200 + (int)(Math.sin(shrekJumpscareTimer * 0.6) * 30);
  int imgX = 300 - imgSize / 2 + (int)(Math.random() * 20 - 10);
  int imgY = 180 - imgSize / 2 + (int)(Math.random() * 20 - 10);
  g2d.drawImage(shrekSprite, imgX, imgY, imgSize, imgSize, null);
 }
 g2d.setFont(new Font("Arial", Font.BOLD, 90));
 g2d.setColor(new Color(0, 0, 0, 200));
 int shakeX = 102 + (int)(Math.random() * 15 - 7);
 int shakeY = 222 + (int)(Math.random() * 15 - 7);
 g2d.drawString("GET OUT!", shakeX, shakeY);
 g2d.setColor(Color.WHITE);
 g2d.drawString("GET OUT!", shakeX - 3, shakeY - 3);
 }
 }
 
 void drawPowerUp(Graphics2D g2d, PowerUp powerUp) {
 int pulseSize = (int)(Math.sin(System.currentTimeMillis() / 200.0) * 3 + 3);
 Color glowColor, mainColor1, mainColor2, mainColor3;
 
 switch (powerUp.type) {
 case "paddle": glowColor = new Color(255, 255, 0, 80); mainColor1 = Color.YELLOW; mainColor2 = new Color(255, 200, 0); mainColor3 = new Color(200, 150, 0); break;
 case "slow": glowColor = new Color(0, 255, 255, 80); mainColor1 = Color.CYAN; mainColor2 = new Color(0, 200, 255); mainColor3 = new Color(0, 150, 200); break;
 case "speed": glowColor = new Color(255, 0, 0, 80); mainColor1 = Color.RED; mainColor2 = new Color(255, 100, 100); mainColor3 = new Color(200, 0, 0); break;
 case "reverse": glowColor = new Color(200, 0, 255, 80); mainColor1 = new Color(200, 0, 255); mainColor2 = new Color(150, 0, 200); mainColor3 = new Color(100, 0, 150); break;
 case "giant": glowColor = new Color(0, 255, 0, 80); mainColor1 = Color.GREEN; mainColor2 = new Color(0, 200, 0); mainColor3 = new Color(0, 150, 0); break;
 case "shrink": glowColor = new Color(255, 150, 0, 80); mainColor1 = Color.ORANGE; mainColor2 = new Color(255, 150, 0); mainColor3 = new Color(200, 100, 0); break;
 case "multiball": glowColor = new Color(255, 105, 180, 80); mainColor1 = new Color(255, 105, 180); mainColor2 = new Color(255, 150, 200); mainColor3 = new Color(200, 80, 150); break;
 case "jackpot": 
 glowColor = new Color(255, 215, 0, 120); mainColor1 = new Color(255, 215, 0); mainColor2 = new Color(255, 180, 0); mainColor3 = new Color(200, 140, 0);
 int extraGlow = (int)(Math.sin(System.currentTimeMillis() / 100.0) * 8 + 8);
 g2d.setColor(new Color(255, 215, 0, 40));
 g2d.fillOval(powerUp.x - extraGlow, powerUp.y - extraGlow, 20 + extraGlow * 2, 20 + extraGlow * 2);
 break;
 case "jumpscare": glowColor = new Color(255, 0, 0, 80); mainColor1 = new Color(255, 0, 0); mainColor2 = new Color(200, 0, 0); mainColor3 = new Color(150, 0, 0); break;
 case "shrek": glowColor = new Color(0, 255, 0, 80); mainColor1 = new Color(100, 255, 100); mainColor2 = new Color(0, 200, 0); mainColor3 = new Color(0, 150, 0); break;
 case "dangerzone":
 case "teleport":
 case "invisiblewalls":
 case "shrinkpaddles":
 case "centerwall":
 case "fireball":
 case "zigzag":
 case "split":
 case "mirror":
 int mapGlow = (int)(Math.sin(System.currentTimeMillis() / 150.0) * 6 + 6);
 Color mapColor = powerUp.type.equals("dangerzone") ? new Color(255, 100, 0) : 
 powerUp.type.equals("teleport") ? new Color(138, 43, 226) : // Purple
 powerUp.type.equals("invisiblewalls") ? new Color(100, 200, 255) : 
 powerUp.type.equals("centerwall") ? new Color(128, 0, 128) :
 powerUp.type.equals("fireball") ? new Color(255, 69, 0) : // Red-orange
 powerUp.type.equals("zigzag") ? new Color(50, 205, 50) : // Lime green
 powerUp.type.equals("split") ? new Color(255, 215, 0) : // Gold
 powerUp.type.equals("mirror") ? new Color(0, 191, 255) : // Deep sky blue
 new Color(200, 0, 100); // shrinkpaddles - pink
 g2d.setColor(new Color(mapColor.getRed(), mapColor.getGreen(), mapColor.getBlue(), 30));
 g2d.fillOval(powerUp.x - mapGlow, powerUp.y - mapGlow, 20 + mapGlow * 2, 20 + mapGlow * 2);
 glowColor = new Color(mapColor.getRed(), mapColor.getGreen(), mapColor.getBlue(), 100);
 mainColor1 = mapColor;
 mainColor2 = mapColor.darker();
 mainColor3 = mapColor.darker().darker();
 break;
 default: glowColor = new Color(255, 255, 255, 80); mainColor1 = Color.WHITE; mainColor2 = Color.LIGHT_GRAY; mainColor3 = Color.GRAY;
 }
 
 g2d.setColor(glowColor);
 g2d.fillOval(powerUp.x - pulseSize, powerUp.y - pulseSize, 20 + pulseSize * 2, 20 + pulseSize * 2);
 RadialGradientPaint powerGradient = new RadialGradientPaint(powerUp.x + 10, powerUp.y + 10, 12, new float[]{0.0f, 0.8f, 1.0f}, new Color[]{mainColor1, mainColor2, mainColor3});
 g2d.setPaint(powerGradient);
 g2d.fillOval(powerUp.x, powerUp.y, 20, 20);
 }
 
 void drawLegend(Graphics2D g2d) {
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 int y = 340;
 g2d.setColor(new Color(255, 255, 255, 200));
 g2d.drawString("POWER-UPS:", 10, y);
 y += 13;
 drawLegendItem(g2d, 10, y, Color.YELLOW, "Paddle", false);
 drawLegendItem(g2d, 10, y+11, Color.CYAN, "Slow", false);
 drawLegendItem(g2d, 80, y, Color.RED, "Speed", false);
 drawLegendItem(g2d, 80, y+11, new Color(200, 0, 255), "Reverse", false);
 drawLegendItem(g2d, 150, y, Color.GREEN, "Giant", false);
 drawLegendItem(g2d, 150, y+11, Color.ORANGE, "Shrink", false);
 g2d.setColor(new Color(255, 200, 100));
 g2d.drawString("MAP MODS:", 230, 340);
 drawLegendItem(g2d, 230, y, new Color(255, 150, 0), "Danger", true);
 drawLegendItem(g2d, 230, y+11, new Color(138, 43, 226), "Teleport", true);
 drawLegendItem(g2d, 310, y, new Color(100, 200, 255), "Walls", true);
 drawLegendItem(g2d, 310, y+11, new Color(200, 0, 100), "Tiny", true);
 drawLegendItem(g2d, 230, y+22, new Color(128, 0, 128), "Center", true);
 drawLegendItem(g2d, 310, y+22, new Color(255, 69, 0), "Fire", true);
 drawLegendItem(g2d, 390, y, new Color(50, 205, 50), "ZigZag", true);
 drawLegendItem(g2d, 390, y+11, new Color(255, 215, 0), "Split", true);
 g2d.setColor(new Color(255, 150, 255));
 drawLegendItem(g2d, 460, y, new Color(255, 105, 180), "Multi*", false);
 g2d.setColor(new Color(255, 215, 0));
 drawLegendItem(g2d, 460, y+11, new Color(255, 215, 0), "JACK**", false);
 g2d.setFont(new Font("Arial", Font.BOLD, 9));
 g2d.setColor(new Color(255, 0, 0, 200));
 drawLegendItem(g2d, 530, y, new Color(255, 0, 0), " SCARE", false);
 g2d.setColor(new Color(100, 200, 50));
 drawLegendItem(g2d, 530, y+11, new Color(100, 200, 50), " SHREK", false);
 g2d.setFont(new Font("Arial", Font.PLAIN, 9));
 g2d.setColor(new Color(200, 200, 200, 150));
 g2d.drawString("Press P or ESC to pause", 470, 390);
 if (cheatModeEnabled) {
 g2d.setFont(new Font("Arial", Font.BOLD, 10));
 g2d.setColor(new Color(255, 215, 0, 200));
 g2d.drawString("CHEAT MODE: M=Menu | B=Balls", 10, 390);
 }
 // Learning AI indicator
 if (singlePlayer) {
 g2d.setFont(new Font("Arial", Font.PLAIN, 9));
 if (learningAIEnabled) {
 double learningProgress = Math.min(1.0, learningDataPoints / 100.0);
 int alpha = (int)(150 + 105 * Math.sin(System.currentTimeMillis() / 200.0)); // Pulsing effect
 g2d.setColor(new Color(0, 255, 255, alpha));
 g2d.drawString(" LEARNING AI: " + learningDataPoints + "/" + maxLearningData + 
 " (" + (int)(learningProgress * 100) + "%)", 10, 15);
 // Show learned stats if enough data
 if (learningDataPoints >= 20) {
 g2d.setFont(new Font("Arial", Font.PLAIN, 8));
 g2d.setColor(new Color(100, 255, 255, 150));
 g2d.drawString("Speed: " + String.format("%.1fx", learnedReactionSpeed) + 
 " | Aggression: " + (int)(learnedAggressiveness * 100) + "%", 10, 27);
 }
 } else {
 g2d.setColor(new Color(150, 150, 150, 100));
 g2d.drawString("Press L to enable Learning AI", 10, 15);
 }
 }

 // SCREEN WARP OVERLAY EFFECTS

 // Screen Inversion effect (flip screen vertically for affected player)
 // Note: In a real implementation, we'd use AffineTransform to flip
 // For simplicity, we'll draw a visual indicator
 if (player1InversionTimer > 0) {
 g2d.setColor(new Color(255, 0, 255, 100));
 g2d.fillRect(0, 0, 300, 400);
 g2d.setColor(Color.WHITE);
 g2d.setFont(new Font("Arial", Font.BOLD, 20));
 g2d.drawString("INVERTED!", 100, 200);
 }
 if (player2InversionTimer > 0) {
 g2d.setColor(new Color(0, 255, 255, 100));
 g2d.fillRect(300, 0, 300, 400);
 g2d.setColor(Color.WHITE);
 g2d.setFont(new Font("Arial", Font.BOLD, 20));
 g2d.drawString("INVERTED!", 400, 200);
 }

 // Tunnel Vision effect (darken edges)
 if (player1TunnelTimer > 0) {
 // Darken the edges of player 1's view
 g2d.setColor(new Color(0, 0, 0, 200));
 g2d.fillRect(0, 0, 100, 400); // Left edge
 g2d.fillRect(0, 0, 300, 80); // Top edge
 g2d.fillRect(0, 320, 300, 80); // Bottom edge
 g2d.setColor(Color.YELLOW);
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.drawString("TUNNEL VISION!", 100, 200);
 }
 if (player2TunnelTimer > 0) {
 g2d.setColor(new Color(0, 0, 0, 200));
 g2d.fillRect(500, 0, 100, 400); // Right edge
 g2d.fillRect(300, 0, 300, 80); // Top edge
 g2d.fillRect(300, 320, 300, 80); // Bottom edge
 g2d.setColor(Color.YELLOW);
 g2d.setFont(new Font("Arial", Font.BOLD, 12));
 g2d.drawString("TUNNEL VISION!", 400, 200);
 }

 // Base screen warp distortion effect
 if (player1WarpEffectTimer > 0) {
 // Wavy distortion overlay for player 1
 g2d.setColor(new Color(100, 0, 200, 80));
 for (int i = 0; i < 10; i++) {
 int offset = (int)(10 * Math.sin(System.currentTimeMillis() / 100.0 + i));
 g2d.fillRect(0, i * 40 + offset, 300, 20);
 }
 }
 if (player2WarpEffectTimer > 0) {
 g2d.setColor(new Color(200, 0, 100, 80));
 for (int i = 0; i < 10; i++) {
 int offset = (int)(10 * Math.sin(System.currentTimeMillis() / 100.0 + i));
 g2d.fillRect(300, i * 40 + offset, 300, 20);
 }
 }
 }

 void drawLegendItem(Graphics2D g2d, int x, int y, Color color, String text, boolean isMapMod) {
 RadialGradientPaint gradient = new RadialGradientPaint(x + 5, y - 5, 7, new float[]{0.0f, 1.0f}, new Color[]{color, new Color(color.getRed()/2, color.getGreen()/2, color.getBlue()/2)});
 g2d.setPaint(gradient);
 g2d.fillOval(x, y - 9, 10, 10);
 g2d.setColor(isMapMod ? new Color(255, 200, 100) : Color.WHITE);
 g2d.drawString(text, x + 13, y);
 }

 public void actionPerformed(ActionEvent e) {
 if (showingMainMenu) {
 menuAnimationTimer++;
 repaint();
 return;
 }
 if (isPaused) return;

 // Online networking removed

 // Update learning AI (tracks player behavior)
 updateLearningAI();
 
 // Jumpscare timer
 if (jumpscareActive) {
 jumpscareDuration++;
 if (jumpscareDuration >= maxJumpscareDuration) {
 jumpscareActive = false;
 jumpscareDuration = 0;
 jumpscareImage = "";
 }
 repaint();
 return;
 }
 jumpscareTimer++;
 if (jumpscareTimer >= jumpscareInterval) {
 if (Math.random() < 0.01) jumpscareActive = true;
 jumpscareTimer = 0;
 }
 
 // Multiball effect
 if (multiballActive) {
 multiballDuration++;
 if (multiballDuration >= maxMultiballDuration) {
 multiballActive = false;
 multiballDuration = 0;
 }
 }
 
 // Shrek ball effect
 if (shrekBallActive) {
 shrekBallDuration++;
 shrekJumpscareTimer++;
 if (shrekJumpscareTimer >= 200) shrekJumpscareTimer = 0;
 if (shrekBallDuration >= maxShrekBallDuration) {
 shrekBallActive = false;
 shrekBallDuration = 0;
 shrekJumpscareTimer = 0;
 }
 }
 
 // Update stun timers
 if (player1StunTimer > 0) {
 player1StunTimer--;
 // When stun ends, start immunity timer
 if (player1StunTimer == 0) {
 player1StunImmunityTimer = stunImmunityDuration;
 }
 } else if (player1StunImmunityTimer > 0) {
 player1StunImmunityTimer--;
 }
 
 if (player2StunTimer > 0) {
 player2StunTimer--;
 // When stun ends, start immunity timer
 if (player2StunTimer == 0) {
 player2StunImmunityTimer = stunImmunityDuration;
 }
 } else if (player2StunImmunityTimer > 0) {
 player2StunImmunityTimer--;
 }
 
 // Gun ability - auto shoot
 if (getEffectiveAbilityLevel(1, "gun") > 0) {
 int gunBranch = player1AbilityBranches.getOrDefault("gun", 0);
 
 if (gunBranch == 1) {
 // GOKU BRANCH - Laser beam
 int gunLevel = player1Abilities.get("gun");
 // Calculate cooldown: 2s base (200 frames) - 0.1s per level (10 frames per level)
 int gokuCooldown = 700 - (gunLevel - 3) * 10; // Starts at 200 for level 3
 
 player1GunTimer++;
 if (player1GunTimer >= gokuCooldown) {
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 
 // Calculate laser duration: 0.5s base (50 frames) + 0.5s per level (50 frames per level)
 int laserDuration = 50 * gunLevel; // Level 3 = 150 frames (1.5s), Level 4 = 200 frames (2s), etc.
 
 // Create laser from paddle to right edge
 activeLasers.add(new Laser(20, paddleCenterY, 580, paddleCenterY, 1, laserDuration));
 
 player1GunTimer = 0;
 }
 } else if (gunBranch == 2) {
 // VEGETA BRANCH - Big Bang Attack (explosive projectile)
 player1GunTimer++;
 if (player1GunTimer >= gunCooldown) {
 int gunLevel = player1Abilities.get("gun");
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 
 // Projectile speed increases with level: 6 + (level-3) * 0.5
 int speed = 6 + (gunLevel >= 3 ? (gunLevel - 3) : 0);
 
 // Level 3: Single shot
 // Level 4+: Multi-shot (number of projectiles = level - 2)
 int numShots = Math.max(1, gunLevel - 2);
 
 if (numShots == 1) {
 // Single powerful shot
 vegetaBullets.add(new VegetaBullet(20, paddleCenterY, speed, 0, 1, gunLevel));
 } else {
 // Multiple shots in a spread pattern
 for (int i = 0; i < numShots; i++) {
 int yOffset = 0;
 if (numShots > 1) {
 // Spread pattern: center shot plus shots above and below
 yOffset = (i - numShots / 2) * 15; // 15 pixel spacing
 }
 vegetaBullets.add(new VegetaBullet(20, paddleCenterY + yOffset, speed, 0, 1, gunLevel));
 }
 }
 
 player1GunTimer = 0;
 }
 } else {
 // Original gun behavior (no branch selected yet)
 player1GunTimer++;
 if (player1GunTimer >= gunCooldown) {
 int gunLevel = player1Abilities.get("gun");
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 
 // Always shoot one horizontal bullet
 bullets.add(new Bullet(20, paddleCenterY, 8, 0, 1));
 
 // Add diagonal bullets for levels 2+
 for (int i = 1; i < gunLevel; i++) {
 // Calculate angle for spread
 double angleOffset = (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * 15; // 15 degree increments
 double angleRad = Math.toRadians(angleOffset);
 
 int velX = (int)(8 * Math.cos(angleRad));
 int velY = (int)(8 * Math.sin(angleRad));
 
 bullets.add(new Bullet(20, paddleCenterY, velX, velY, 1));
 }
 player1GunTimer = 0;
 }
 }
 }
 if (getEffectiveAbilityLevel(2, "gun") > 0) {
 int gunBranch = player2AbilityBranches.getOrDefault("gun", 0);
 
 if (gunBranch == 1) {
 // GOKU BRANCH - Laser beam
 int gunLevel = player2Abilities.get("gun");
 // Calculate cooldown: 2s base (200 frames) - 0.1s per level (10 frames per level)
 int gokuCooldown = 200 - (gunLevel - 3) * 10; // Starts at 200 for level 3
 
 player2GunTimer++;
 if (player2GunTimer >= gokuCooldown) {
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Calculate laser duration: 0.5s base (50 frames) + 0.5s per level (50 frames per level)
 int laserDuration = 50 * gunLevel; // Level 3 = 150 frames (1.5s), Level 4 = 200 frames (2s), etc.
 
 // Create laser from paddle to left edge
 activeLasers.add(new Laser(580, paddleCenterY, 20, paddleCenterY, 2, laserDuration));
 
 player2GunTimer = 0;
 }
 } else if (gunBranch == 2) {
 // VEGETA BRANCH - Big Bang Attack (explosive projectile)
 player2GunTimer++;
 if (player2GunTimer >= gunCooldown) {
 int gunLevel = player2Abilities.get("gun");
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Projectile speed increases with level: 6 + (level-3) * 0.5
 int speed = 6 + (gunLevel >= 3 ? (gunLevel - 3) : 0);
 
 // Level 3: Single shot
 // Level 4+: Multi-shot (number of projectiles = level - 2)
 int numShots = Math.max(1, gunLevel - 2);
 
 if (numShots == 1) {
 // Single powerful shot
 vegetaBullets.add(new VegetaBullet(580, paddleCenterY, -speed, 0, 2, gunLevel));
 } else {
 // Multiple shots in a spread pattern
 for (int i = 0; i < numShots; i++) {
 int yOffset = 0;
 if (numShots > 1) {
 // Spread pattern: center shot plus shots above and below
 yOffset = (i - numShots / 2) * 15; // 15 pixel spacing
 }
 vegetaBullets.add(new VegetaBullet(580, paddleCenterY + yOffset, -speed, 0, 2, gunLevel));
 }
 }
 
 player2GunTimer = 0;
 }
 } else {
 // Original gun behavior (no branch selected yet)
 player2GunTimer++;
 if (player2GunTimer >= gunCooldown) {
 int gunLevel = player2Abilities.get("gun");
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Always shoot one horizontal bullet
 bullets.add(new Bullet(580, paddleCenterY, -8, 0, 2));
 
 // Add diagonal bullets for levels 2+
 for (int i = 1; i < gunLevel; i++) {
 // Calculate angle for spread
 double angleOffset = (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * 15; // 15 degree increments
 double angleRad = Math.toRadians(angleOffset);
 
 int velX = (int)(-8 * Math.cos(angleRad));
 int velY = (int)(-8 * Math.sin(angleRad));
 
 bullets.add(new Bullet(580, paddleCenterY, velX, velY, 2));
 }
 player2GunTimer = 0;
 }
 }
 }
 
 // Ability Stealer - Branch system (Frieza and Jiren)
 if (getEffectiveAbilityLevel(1, "ability_stealer") > 0) {
 int stealerBranch = player1AbilityBranches.getOrDefault("ability_stealer", 0);
 
 // Original ability stealer - shoots bullets (works only for base/no branch)
 if (stealerBranch == 0) {
 player1StealerTimer++;
 if (player1StealerTimer >= stealerCooldown) {
 int stealerLevel = Math.max(player1Abilities.getOrDefault("ability_stealer", 0),
 player1MimickedAbilities.getOrDefault("ability_stealer", 0));
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 stealerBullets.add(new StealerBullet(20, paddleCenterY, 6, 1));
 for (int i = 1; i < stealerLevel; i++) {
 double angleOffset = (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * 20;
 double angleRad = Math.toRadians(angleOffset);
 int velX = (int)(6 * Math.cos(angleRad));
 stealerBullets.add(new StealerBullet(20, paddleCenterY + (int)(6 * Math.sin(angleRad) * 10), velX, 1));
 }
 player1StealerTimer = 0;
 }
 }
 
 // Branch-specific abilities
 if (stealerBranch == 1) {
 // FRIEZA - Death beam laser (purple, same as Goku)
 int stealerLevel = player1Abilities.get("ability_stealer");
 // Calculate cooldown: 7s base (700 frames) - 0.4s per level (40 frames per level)
 int freezaCooldown = 700 - (stealerLevel - 3) * 40; // Starts at 700 for level 3
 
 player1StealerTimer++;
 if (player1StealerTimer >= freezaCooldown) {
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 
 // Calculate laser duration: 0.2s base (20 frames) + 0.2s per level (20 frames per level)
 int laserDuration = 20 * stealerLevel; // Level 3 = 60 frames (0.6s), Level 4 = 80 frames (0.8s), etc.
 
 // Create Frieza purple death beam laser
 freezaLasers.add(new FreezaLaser(20, paddleCenterY, 580, paddleCenterY, 1, laserDuration, stealerLevel));
 
 player1StealerTimer = 0;
 }
 } else if (stealerBranch == 2) {
 // JIREN - Explosive projectile (purple, same as Vegeta, but steals on explosion)
 player1StealerTimer++;
 int stealerLevel = player1Abilities.get("ability_stealer");
 
 if (player1StealerTimer >= stealerCooldown) {
 int paddleCenterY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 
 // Projectile speed increases with level: 6 + (level-3) * 0.5
 int speed = 6 + (stealerLevel >= 3 ? (stealerLevel - 3) : 0);
 
 // Level 3: Single shot
 // Level 4+: Multi-shot (number of projectiles = level - 2)
 int numShots = Math.max(1, stealerLevel - 2);
 
 if (numShots == 1) {
 // Single powerful shot
 jirenBullets.add(new JirenBullet(20, paddleCenterY, speed, 1, stealerLevel));
 } else {
 // Multiple shots in a spread pattern
 for (int i = 0; i < numShots; i++) {
 int yOffset = 0;
 if (numShots > 1) {
 // Spread pattern: center shot plus shots above and below
 yOffset = (i - numShots / 2) * 15; // 15 pixel spacing
 }
 jirenBullets.add(new JirenBullet(20, paddleCenterY + yOffset, speed, 1, stealerLevel));
 }
 }
 
 player1StealerTimer = 0;
 }
 }
 }
 
 if (getEffectiveAbilityLevel(2, "ability_stealer") > 0) {
 int stealerBranch = player2AbilityBranches.getOrDefault("ability_stealer", 0);
 
 // Original ability stealer - shoots bullets (works only for base/no branch)
 if (stealerBranch == 0) {
 player2StealerTimer++;
 if (player2StealerTimer >= stealerCooldown) {
 int stealerLevel = Math.max(player2Abilities.getOrDefault("ability_stealer", 0),
 player2MimickedAbilities.getOrDefault("ability_stealer", 0));
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 stealerBullets.add(new StealerBullet(580, paddleCenterY, -6, 2));
 for (int i = 1; i < stealerLevel; i++) {
 double angleOffset = (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * 20;
 double angleRad = Math.toRadians(angleOffset);
 int velX = (int)(-6 * Math.cos(angleRad));
 stealerBullets.add(new StealerBullet(580, paddleCenterY + (int)(-6 * Math.sin(angleRad) * 10), velX, 2));
 }
 player2StealerTimer = 0;
 }
 }
 
 // Branch-specific abilities
 if (stealerBranch == 1) {
 // FRIEZA - Death beam laser (purple, same as Goku)
 int stealerLevel = player2Abilities.get("ability_stealer");
 // Calculate cooldown: 7s base (700 frames) - 0.4s per level (40 frames per level)
 int freezaCooldown = 700 - (stealerLevel - 3) * 40; // Starts at 700 for level 3
 
 player2StealerTimer++;
 if (player2StealerTimer >= freezaCooldown) {
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Calculate laser duration: 0.2s base (20 frames) + 0.2s per level (20 frames per level)
 int laserDuration = 20 * stealerLevel; // Level 3 = 60 frames (0.6s), Level 4 = 80 frames (0.8s), etc.
 
 // Create Frieza purple death beam laser
 freezaLasers.add(new FreezaLaser(580, paddleCenterY, 20, paddleCenterY, 2, laserDuration, stealerLevel));
 
 player2StealerTimer = 0;
 }
 } else if (stealerBranch == 2) {
 // JIREN - Explosive projectile (purple, same as Vegeta, but steals on explosion)
 player2StealerTimer++;
 int stealerLevel = player2Abilities.get("ability_stealer");
 
 if (player2StealerTimer >= stealerCooldown) {
 int paddleCenterY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Projectile speed increases with level: 6 + (level-3) * 0.5
 int speed = 6 + (stealerLevel >= 3 ? (stealerLevel - 3) : 0);
 
 // Level 3: Single shot
 // Level 4+: Multi-shot (number of projectiles = level - 2)
 int numShots = Math.max(1, stealerLevel - 2);
 
 if (numShots == 1) {
 // Single powerful shot
 jirenBullets.add(new JirenBullet(580, paddleCenterY, -speed, 2, stealerLevel));
 } else {
 // Multiple shots in a spread pattern
 for (int i = 0; i < numShots; i++) {
 int yOffset = 0;
 if (numShots > 1) {
 // Spread pattern: center shot plus shots above and below
 yOffset = (i - numShots / 2) * 15; // 15 pixel spacing
 }
 jirenBullets.add(new JirenBullet(580, paddleCenterY + yOffset, -speed, 2, stealerLevel));
 }
 }
 
 player2StealerTimer = 0;
 }
 }
 }
 
 // Update bullets
 for (int i = bullets.size() - 1; i >= 0; i--) {
 Bullet bullet = bullets.get(i);
 bullet.x += bullet.velocityX;
 bullet.y += bullet.velocityY;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (bullet.owner == 1 && bullet.x >= 580 && bullet.x <= 590 && bullet.y >= paddle2Y && bullet.y <= paddle2Y + paddleHeight2) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(2);
 if (Math.random() < dodgeChance) {
 // Dodged!
 bullets.remove(i);
 continue;
 }
 
 // Stun duration increases with gun level
 int gunLevel = player1Abilities.getOrDefault("gun", 1);
 int newStunDuration = stunDuration + (gunLevel - 1) * 50; // +0.5s per level
 
 // Only stun if player 2 is not immune AND not already stunned
 if (player2StunImmunityTimer == 0 && player2StunTimer == 0) {
 player2StunTimer = newStunDuration;
 }
 bullets.remove(i);
 continue;
 } else if (bullet.owner == 2 && bullet.x >= 10 && bullet.x <= 20 && bullet.y >= paddle1Y && bullet.y <= paddle1Y + paddleHeight1) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(1);
 if (Math.random() < dodgeChance) {
 // Dodged!
 bullets.remove(i);
 continue;
 }
 
 // Stun duration increases with gun level
 int gunLevel = player2Abilities.getOrDefault("gun", 1);
 int newStunDuration = stunDuration + (gunLevel - 1) * 50; // +0.5s per level
 
 // Only stun if player 1 is not immune AND not already stunned
 if (player1StunImmunityTimer == 0 && player1StunTimer == 0) {
 player1StunTimer = newStunDuration;
 }
 bullets.remove(i);
 continue;
 }
 if (bullet.x < 0 || bullet.x > 600 || bullet.y < 0 || bullet.y > 400) bullets.remove(i);
 }
 
 // Update Vegeta bullets (Branch 2 - Big Bang Attack)
 for (int i = vegetaBullets.size() - 1; i >= 0; i--) {
 VegetaBullet vb = vegetaBullets.get(i);
 vb.x += vb.velocityX;
 vb.y += vb.velocityY;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 boolean shouldExplode = false;
 
 // Check collision with player 2 paddle
 if (vb.owner == 1 && vb.x >= 580 && vb.x <= 590 && vb.y >= paddle2Y && vb.y <= paddle2Y + paddleHeight2) {
 shouldExplode = true;
 }
 // Check collision with player 1 paddle
 else if (vb.owner == 2 && vb.x >= 10 && vb.x <= 20 && vb.y >= paddle1Y && vb.y <= paddle1Y + paddleHeight1) {
 shouldExplode = true;
 }
 // Check collision with opposing wall
 else if (vb.owner == 1 && vb.x >= 595) {
 shouldExplode = true;
 }
 else if (vb.owner == 2 && vb.x <= 5) {
 shouldExplode = true;
 }
 
 if (shouldExplode) {
 // Create explosion at bullet location
 // Explosion radius scales with gun level: 60 + (level-3) * 10
 int explosionRadius = 60 + Math.max(0, (vb.gunLevel - 3) * 10);
 explosions.add(new Explosion(vb.x, vb.y, explosionRadius, vb.owner, 30)); // 30 frames duration
 vegetaBullets.remove(i);
 continue;
 }
 
 // Remove if out of bounds
 if (vb.x < -10 || vb.x > 610 || vb.y < -10 || vb.y > 410) vegetaBullets.remove(i);
 }
 
 // Update explosions
 for (int i = explosions.size() - 1; i >= 0; i--) {
 Explosion exp = explosions.get(i);
 exp.duration--;
 
 // Check if explosion hits opponent paddle
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (exp.owner == 1) {
 // Check if explosion radius hits player 2 paddle
 int paddle2CenterY = paddle2Y + paddleHeight2 / 2;
 double distance = Math.sqrt(Math.pow(exp.x - 585, 2) + Math.pow(exp.y - paddle2CenterY, 2));
 
 if (distance <= exp.radius) {
 if (exp.isJiren && !exp.hasStolen) {
 // Jiren explosion - steal abilities
 // Level 3: 1 ability, Level 4-5: 2 abilities, Level 6+: 3 abilities
 int abilitiesToSteal = 1;
 if (exp.jirenLevel >= 6) abilitiesToSteal = 3;
 else if (exp.jirenLevel >= 4) abilitiesToSteal = 2;
 
 HashMap<String, Integer> targetAbilities = player2Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int actualSteals = Math.min(abilitiesToSteal, abilityList.size());
 StringBuilder removedText = new StringBuilder();
 
 for (int j = 0; j < actualSteals; j++) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText.append(getAbilityShortName(removedAbility))
 .append(" (Lv").append(level).append(" ").append(level - 1).append(")");
 } else {
 targetAbilities.remove(removedAbility);
 removedText.append(getAbilityShortName(removedAbility));
 }
 
 if (j < actualSteals - 1) removedText.append(", ");
 abilityList.remove(removedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " Player 1's Jiren Explosion hit!\n" + player2Name + " lost: " + removedText + " ", 
 "Abilities Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 }
 }
 exp.hasStolen = true;
 } else if (!exp.isJiren && !exp.hasHit) {
 // Regular explosion (Vegeta) - stun player 2 (only once per explosion)
 if (player2StunImmunityTimer == 0 && player2StunTimer == 0) {
 player2StunTimer = stunDuration + 50; // Slightly longer stun than regular gun
 exp.hasHit = true; // Mark this explosion as having hit
 }
 // Apply knockback (move paddle away from explosion)
 if (!exp.hasHit) {
 int knockbackDirection = (paddle2CenterY < exp.y) ? -1 : 1;
 paddle2Y += knockbackDirection * 30;
 // Clamp paddle position
 if (paddle2Y < 0) paddle2Y = 0;
 if (paddle2Y > 400 - paddleHeight2) paddle2Y = 400 - paddleHeight2;
 exp.hasHit = true;
 }
 }
 }
 } else if (exp.owner == 2) {
 // Check if explosion radius hits player 1 paddle
 int paddle1CenterY = paddle1Y + paddleHeight1 / 2;
 double distance = Math.sqrt(Math.pow(exp.x - 15, 2) + Math.pow(exp.y - paddle1CenterY, 2));
 
 if (distance <= exp.radius) {
 if (exp.isJiren && !exp.hasStolen) {
 // Jiren explosion - steal abilities
 // Level 3: 1 ability, Level 4-5: 2 abilities, Level 6+: 3 abilities
 int abilitiesToSteal = 1;
 if (exp.jirenLevel >= 6) abilitiesToSteal = 3;
 else if (exp.jirenLevel >= 4) abilitiesToSteal = 2;
 
 HashMap<String, Integer> targetAbilities = player1Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int actualSteals = Math.min(abilitiesToSteal, abilityList.size());
 StringBuilder removedText = new StringBuilder();
 
 for (int j = 0; j < actualSteals; j++) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText.append(getAbilityShortName(removedAbility))
 .append(" (Lv").append(level).append(" ").append(level - 1).append(")");
 } else {
 targetAbilities.remove(removedAbility);
 removedText.append(getAbilityShortName(removedAbility));
 }
 
 if (j < actualSteals - 1) removedText.append(", ");
 abilityList.remove(removedAbility);
 }
 
 JOptionPane.showMessageDialog(null, 
 " AI's Jiren Explosion hit!\nPlayer 1 lost: " + removedText + " ", 
 "Abilities Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 }
 }
 exp.hasStolen = true;
 } else if (!exp.isJiren && !exp.hasHit) {
 // Regular explosion (Vegeta) - stun player 1 (only once per explosion)
 if (player1StunImmunityTimer == 0 && player1StunTimer == 0) {
 player1StunTimer = stunDuration + 50; // Slightly longer stun than regular gun
 exp.hasHit = true; // Mark this explosion as having hit
 }
 // Apply knockback (move paddle away from explosion)
 if (!exp.hasHit) {
 int knockbackDirection = (paddle1CenterY < exp.y) ? -1 : 1;
 paddle1Y += knockbackDirection * 30;
 // Clamp paddle position
 if (paddle1Y < 0) paddle1Y = 0;
 if (paddle1Y > 400 - paddleHeight1) paddle1Y = 400 - paddleHeight1;
 exp.hasHit = true;
 }
 }
 }
 }
 
 if (exp.duration <= 0) {
 explosions.remove(i);
 }
 }
 
 // Update lasers (Goku branch)
 for (int i = activeLasers.size() - 1; i >= 0; i--) {
 Laser laser = activeLasers.get(i);
 laser.remainingTime--;
 
 if (laser.remainingTime <= 0) {
 activeLasers.remove(i);
 continue;
 }
 
 // Check laser collision with opponent paddle every frame
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (laser.owner == 1) {
 // Check if laser intersects player 2's paddle
 if (laser.endX >= 580 && laser.startY >= paddle2Y - 5 && laser.startY <= paddle2Y + paddleHeight2 + 5) {
 // Can stun every frame if not immune and not already stunned
 if (player2StunImmunityTimer == 0 && player2StunTimer == 0) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(2);
 if (Math.random() >= dodgeChance) { // Only stun if NOT dodged
 // Stun duration increases with gun level
 int gunLevel = player1Abilities.getOrDefault("gun", 1);
 int baseStunDuration = stunDuration + (gunLevel - 3) * 50; // +0.5s per level after 3
 player2StunTimer = baseStunDuration;
 }
 }
 }
 } else if (laser.owner == 2) {
 // Check if laser intersects player 1's paddle
 if (laser.endX <= 20 && laser.startY >= paddle1Y - 5 && laser.startY <= paddle1Y + paddleHeight1 + 5) {
 // Can stun every frame if not immune and not already stunned
 if (player1StunImmunityTimer == 0 && player1StunTimer == 0) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(1);
 if (Math.random() >= dodgeChance) { // Only stun if NOT dodged
 // Stun duration increases with gun level
 int gunLevel = player2Abilities.getOrDefault("gun", 1);
 int baseStunDuration = stunDuration + (gunLevel - 3) * 50; // +0.5s per level after 3
 player1StunTimer = baseStunDuration;
 }
 }
 }
 }
 }
 
 // Update Frieza lasers (Ability Stealer Branch 1)
 for (int i = freezaLasers.size() - 1; i >= 0; i--) {
 FreezaLaser laser = freezaLasers.get(i);
 laser.remainingTime--;
 
 if (laser.remainingTime <= 0) {
 freezaLasers.remove(i);
 continue;
 }
 
 // Check laser collision with opponent paddle once per laser
 if (!laser.hasStolen) {
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (laser.owner == 1) {
 // Check if laser intersects player 2's paddle
 if (laser.endX >= 580 && laser.startY >= paddle2Y - 5 && laser.startY <= paddle2Y + paddleHeight2 + 5) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(2);
 if (Math.random() >= dodgeChance) { // Only steal if NOT dodged
 // Steal abilities based on level
 int abilitiesToSteal = (laser.level >= 4) ? 2 : 1;
 
 HashMap<String, Integer> targetAbilities = player2Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int actualSteals = Math.min(abilitiesToSteal, abilityList.size());
 StringBuilder removedText = new StringBuilder();
 
 for (int j = 0; j < actualSteals; j++) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText.append(getAbilityShortName(removedAbility))
 .append(" (Lv").append(level).append(" ").append(level - 1).append(")");
 } else {
 targetAbilities.remove(removedAbility);
 removedText.append(getAbilityShortName(removedAbility));
 }
 
 if (j < actualSteals - 1) removedText.append(", ");
 abilityList.remove(removedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " Player 1's Frieza Death Beam hit!\n" + player2Name + " lost: " + removedText + " ", 
 "Abilities Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 }
 }
 laser.hasStolen = true;
 }
 }
 } else if (laser.owner == 2) {
 // Check if laser intersects player 1's paddle
 if (laser.endX <= 20 && laser.startY >= paddle1Y - 5 && laser.startY <= paddle1Y + paddleHeight1 + 5) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(1);
 if (Math.random() >= dodgeChance) { // Only steal if NOT dodged
 // Steal abilities based on level
 int abilitiesToSteal = (laser.level >= 4) ? 2 : 1;
 
 HashMap<String, Integer> targetAbilities = player1Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int actualSteals = Math.min(abilitiesToSteal, abilityList.size());
 StringBuilder removedText = new StringBuilder();
 
 for (int j = 0; j < actualSteals; j++) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText.append(getAbilityShortName(removedAbility))
 .append(" (Lv").append(level).append(" ").append(level - 1).append(")");
 } else {
 targetAbilities.remove(removedAbility);
 removedText.append(getAbilityShortName(removedAbility));
 }
 
 if (j < actualSteals - 1) removedText.append(", ");
 abilityList.remove(removedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + "'s Frieza Death Beam hit!\nPlayer 1 lost: " + removedText + " ", 
 "Abilities Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 }
 }
 laser.hasStolen = true;
 }
 }
 }
 }
 }
 
 // Update Jiren bullets (Ability Stealer Branch 2)
 for (int i = jirenBullets.size() - 1; i >= 0; i--) {
 JirenBullet jb = jirenBullets.get(i);
 jb.x += jb.velocityX;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (jb.owner == 1 && jb.x >= 580 && jb.y >= paddle2Y && jb.y <= paddle2Y + paddleHeight2) {
 // Check dodge chance
 double dodgeChance = getDodgeChance(2);
 if (Math.random() >= dodgeChance) {
 // Create Jiren explosion (will steal abilities when explosion hits)
 // Explosion radius scales with level: 60 + (level-3) * 10
 int explosionRadius = 60 + Math.max(0, (jb.level - 3) * 10);
 explosions.add(new Explosion(jb.x, jb.y, explosionRadius, 1, 30, true, jb.level));
 }
 jirenBullets.remove(i);
 continue;
 } else if (jb.owner == 2 && jb.x <= 20 && jb.y >= paddle1Y && jb.y <= paddle1Y + paddleHeight1) {
 // Check dodge chance
 double dodgeChance = getDodgeChance(1);
 if (Math.random() >= dodgeChance) {
 // Create Jiren explosion (will steal abilities when explosion hits)
 // Explosion radius scales with level: 60 + (level-3) * 10
 int explosionRadius = 60 + Math.max(0, (jb.level - 3) * 10);
 explosions.add(new Explosion(jb.x, jb.y, explosionRadius, 2, 30, true, jb.level));
 }
 jirenBullets.remove(i);
 continue;
 }
 
 // Check collision with walls - create explosion
 boolean shouldExplode = false;
 if (jb.owner == 1 && jb.x >= 595) {
 shouldExplode = true;
 } else if (jb.owner == 2 && jb.x <= 5) {
 shouldExplode = true;
 }
 
 if (shouldExplode) {
 // Create Jiren explosion at wall
 int explosionRadius = 60 + Math.max(0, (jb.level - 3) * 10);
 explosions.add(new Explosion(jb.x, jb.y, explosionRadius, jb.owner, 30, true, jb.level));
 jirenBullets.remove(i);
 continue;
 }
 
 // Remove if off screen
 if (jb.x < -20 || jb.x > 620) {
 jirenBullets.remove(i);
 }
 }
 
 // Update stealer bullets
 for (int i = stealerBullets.size() - 1; i >= 0; i--) {
 StealerBullet sb = stealerBullets.get(i);
 sb.x += sb.velocityX;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 if (sb.owner == 1 && sb.x >= 580 && sb.x <= 590 && sb.y >= paddle2Y && sb.y <= paddle2Y + paddleHeight2) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(2);
 if (Math.random() < dodgeChance) {
 // Dodged!
 stealerBullets.remove(i);
 continue;
 }
 
 // Check if using Mimic branch
 int stealerBranch = player1AbilityBranches.getOrDefault("ability_stealer", 0);
 if (stealerBranch == 2) {
 // MIMIC - Copy opponent abilities
 HashMap<String, Integer> targetAbilities = player2Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int stealerLevel = player1Abilities.getOrDefault("ability_stealer", 0);
 if (stealerLevel == 0) {
 stealerLevel = player1MimickedAbilities.getOrDefault("ability_stealer", 3); // Fallback to level 3
 }
 int copiesToMake = Math.min(stealerLevel - 2, abilityList.size()); // Level 3 = 1 copy, Level 4 = 2, etc.
 
 for (int j = 0; j < copiesToMake; j++) {
 String copiedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(copiedAbility);
 player1MimickedAbilities.put(copiedAbility, level);
 player1MimickedDurations.put(copiedAbility, 1500 + (stealerLevel - 3) * 300); // 15s base + 3s per level
 abilityList.remove(copiedAbility); // Don't copy same ability twice
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " Player 1's Mimic hit!\nCopied " + copiesToMake + " abilities from " + player2Name + "! ", 
 "Abilities Mimicked!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 } else {
 // Default stealer behavior - remove abilities
 HashMap<String, Integer> targetAbilities = player2Abilities;
 
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 
 if (!abilityList.isEmpty()) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 String removedText;
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText = getAbilityShortName(removedAbility) + " (Lv" + level + " " + (level - 1) + ")";
 } else {
 targetAbilities.remove(removedAbility);
 removedText = getAbilityShortName(removedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " Player 1's Ability Stealer hit!\n" + player2Name + " lost: " + removedText + " ", 
 "Ability Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 } else {
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + "'s JOSHUA cannot be stolen! ", 
 "Protected!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 }
 stealerBullets.remove(i);
 continue;
 } else if (sb.owner == 2 && sb.x >= 10 && sb.x <= 20 && sb.y >= paddle1Y && sb.y <= paddle1Y + paddleHeight1) {
 // Check dodge chance from speed boost
 double dodgeChance = getDodgeChance(1);
 if (Math.random() < dodgeChance) {
 // Dodged!
 stealerBullets.remove(i);
 continue;
 }
 
 // Check if using Mimic branch
 int stealerBranch = player2AbilityBranches.getOrDefault("ability_stealer", 0);
 if (stealerBranch == 2) {
 // MIMIC - Copy opponent abilities
 HashMap<String, Integer> targetAbilities = player1Abilities;
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 abilityList.remove("jaisan");
 
 if (!abilityList.isEmpty()) {
 int stealerLevel = player2Abilities.getOrDefault("ability_stealer", 0);
 if (stealerLevel == 0) {
 stealerLevel = player2MimickedAbilities.getOrDefault("ability_stealer", 3); // Fallback to level 3
 }
 int copiesToMake = Math.min(stealerLevel - 2, abilityList.size());
 
 for (int j = 0; j < copiesToMake; j++) {
 String copiedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(copiedAbility);
 player2MimickedAbilities.put(copiedAbility, level);
 player2MimickedDurations.put(copiedAbility, 1500 + (stealerLevel - 3) * 300);
 abilityList.remove(copiedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + "'s Mimic hit!\nCopied " + copiesToMake + " abilities from Player 1! ", 
 "Abilities Mimicked!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 } else {
 // Default stealer behavior
 HashMap<String, Integer> targetAbilities = player1Abilities;
 
 if (!targetAbilities.isEmpty()) {
 ArrayList<String> abilityList = new ArrayList<>(targetAbilities.keySet());
 abilityList.remove("joshua");
 
 if (!abilityList.isEmpty()) {
 String removedAbility = abilityList.get((int)(Math.random() * abilityList.size()));
 int level = targetAbilities.get(removedAbility);
 
 String removedText;
 if (level > 1) {
 targetAbilities.put(removedAbility, level - 1);
 removedText = getAbilityShortName(removedAbility) + " (Lv" + level + " " + (level - 1) + ")";
 } else {
 targetAbilities.remove(removedAbility);
 removedText = getAbilityShortName(removedAbility);
 }
 
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + "'s Ability Stealer hit!\nPlayer 1 lost: " + removedText + " ", 
 "Ability Stolen!", 
 JOptionPane.WARNING_MESSAGE);
 } else {
 JOptionPane.showMessageDialog(null, 
 " Player 1's JOSHUA cannot be stolen! ", 
 "Protected!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 }
 stealerBullets.remove(i);
 continue;
 }
 if (sb.x < 0 || sb.x > 600) stealerBullets.remove(i);
 }
 
 // Update hijacker bullets
 for (int i = hijackerBullets.size() - 1; i >= 0; i--) {
 HijackerBullet hb = hijackerBullets.get(i);
 hb.x += hb.velocityX;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 // Check collision with player 1
 if (hb.owner == 2 && hb.x <= 20 && hb.x >= 10 && hb.y >= paddle1Y && hb.y <= paddle1Y + paddleHeight1) {
 // Hijack a random player 1 ability
 int stealerLevel = Math.max(player2Abilities.getOrDefault("ability_stealer", 0),
 player2MimickedAbilities.getOrDefault("ability_stealer", 0));
 if (!player1Abilities.isEmpty()) {
 ArrayList<String> availableAbilities = new ArrayList<>(player1Abilities.keySet());
 player2HijackedAbility = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player2HijackedDuration = 400 + (stealerLevel - 3) * 100; // 4s + 1s per level
 JOptionPane.showMessageDialog(null, 
 " " + (singlePlayer ? "AI" : "Player 2") + "'s Hijacker hit! Hijacked: " + getAbilityShortName(player2HijackedAbility) + " ", 
 "Ability Hijacked!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 hijackerBullets.remove(i);
 continue;
 }
 
 // Check collision with player 2
 if (hb.owner == 1 && hb.x >= 580 && hb.x <= 590 && hb.y >= paddle2Y && hb.y <= paddle2Y + paddleHeight2) {
 // Hijack a random player 2 ability
 int stealerLevel = Math.max(player1Abilities.getOrDefault("ability_stealer", 0),
 player1MimickedAbilities.getOrDefault("ability_stealer", 0));
 if (!player2Abilities.isEmpty()) {
 ArrayList<String> availableAbilities = new ArrayList<>(player2Abilities.keySet());
 player1HijackedAbility = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player1HijackedDuration = 400 + (stealerLevel - 3) * 100; // 4s + 1s per level
 JOptionPane.showMessageDialog(null, 
 " Player 1's Hijacker hit! Hijacked: " + getAbilityShortName(player1HijackedAbility) + " ", 
 "Ability Hijacked!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 hijackerBullets.remove(i);
 continue;
 }
 
 if (hb.x < 0 || hb.x > 600) hijackerBullets.remove(i);
 }
 
 // Update disabled ability timers (from Void Pulse and Virus)
 ArrayList<String> toRemoveP1 = new ArrayList<>();
 for (String ability : player1VirusDisableDurations.keySet()) {
 int duration = player1VirusDisableDurations.get(ability);
 duration--;
 if (duration <= 0) {
 toRemoveP1.add(ability);
 } else {
 player1VirusDisableDurations.put(ability, duration);
 }
 }
 for (String ability : toRemoveP1) {
 player1VirusDisableDurations.remove(ability);
 player1DisabledAbilities.remove(ability);
 }
 
 ArrayList<String> toRemoveP2 = new ArrayList<>();
 for (String ability : player2VirusDisableDurations.keySet()) {
 int duration = player2VirusDisableDurations.get(ability);
 duration--;
 if (duration <= 0) {
 toRemoveP2.add(ability);
 } else {
 player2VirusDisableDurations.put(ability, duration);
 }
 }
 for (String ability : toRemoveP2) {
 player2VirusDisableDurations.remove(ability);
 player2DisabledAbilities.remove(ability);
 }
 
 // Update virus slow timers
 if (player1VirusSlowTimer > 0) player1VirusSlowTimer--;
 if (player2VirusSlowTimer > 0) player2VirusSlowTimer--;
 
 // Update virus bullets
 for (int i = virusBullets.size() - 1; i >= 0; i--) {
 VirusBullet vb = virusBullets.get(i);
 vb.x += vb.velocityX;
 
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 
 // Check collision with player 1
 if (vb.owner == 2 && vb.x <= 20 && vb.x >= 10 && vb.y >= paddle1Y && vb.y <= paddle1Y + paddleHeight1) {
 // Infect player 1 with virus
 int stealerLevel = Math.max(player2Abilities.getOrDefault("ability_stealer", 0),
 player2MimickedAbilities.getOrDefault("ability_stealer", 0));
 int numDisables = stealerLevel < 4 ? 1 : 2;
 int disableDuration = 300 + (stealerLevel - 3) * 50; // 3s + 0.5s per level
 
 ArrayList<String> availableAbilities = new ArrayList<>(player1Abilities.keySet());
 // Remove already disabled abilities from the selection pool
 availableAbilities.removeAll(player1DisabledAbilities);
 int disabledCount = 0;
 for (int j = 0; j < numDisables && !availableAbilities.isEmpty(); j++) {
 String abilityToDisable = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player1DisabledAbilities.add(abilityToDisable);
 player1VirusDisableDurations.put(abilityToDisable, disableDuration);
 availableAbilities.remove(abilityToDisable);
 disabledCount++;
 }
 
 // Slow paddle at higher levels
 if (stealerLevel >= 6) {
 player1VirusSlowTimer = disableDuration;
 }
 
 if (disabledCount > 0) {
 JOptionPane.showMessageDialog(null, 
 " " + (singlePlayer ? "AI" : "Player 2") + "'s Virus hit! Disabled " + disabledCount + " abilities! ", 
 "Infected!", 
 JOptionPane.WARNING_MESSAGE);
 }
 virusBullets.remove(i);
 continue;
 }
 
 // Check collision with player 2
 if (vb.owner == 1 && vb.x >= 580 && vb.x <= 590 && vb.y >= paddle2Y && vb.y <= paddle2Y + paddleHeight2) {
 // Infect player 2 with virus
 int stealerLevel = Math.max(player1Abilities.getOrDefault("ability_stealer", 0),
 player1MimickedAbilities.getOrDefault("ability_stealer", 0));
 int numDisables = stealerLevel < 4 ? 1 : 2;
 int disableDuration = 300 + (stealerLevel - 3) * 50; // 3s + 0.5s per level
 
 ArrayList<String> availableAbilities = new ArrayList<>(player2Abilities.keySet());
 // Remove already disabled abilities from the selection pool
 availableAbilities.removeAll(player2DisabledAbilities);
 int disabledCount = 0;
 for (int j = 0; j < numDisables && !availableAbilities.isEmpty(); j++) {
 String abilityToDisable = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player2DisabledAbilities.add(abilityToDisable);
 player2VirusDisableDurations.put(abilityToDisable, disableDuration);
 availableAbilities.remove(abilityToDisable);
 disabledCount++;
 }
 
 // Slow paddle at higher levels
 if (stealerLevel >= 6) {
 player2VirusSlowTimer = disableDuration;
 }
 
 if (disabledCount > 0) {
 JOptionPane.showMessageDialog(null, 
 " Player 1's Virus hit! Disabled " + disabledCount + " abilities! ", 
 "Infected!", 
 JOptionPane.WARNING_MESSAGE);
 }
 virusBullets.remove(i);
 continue;
 }
 
 if (vb.x < 0 || vb.x > 600) virusBullets.remove(i);
 }
 
 // Calculate paddle heights and boundaries BEFORE movement to avoid gaps
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 int paddle1MaxY = 400 - paddleHeight1;
 int paddle2MaxY = 400 - paddleHeight2;
 
 // Paddle movement (only if not stunned AND not frozen)
 if (player1StunTimer == 0 && player2FreezeActive == 0) {
 // SCREEN WARP effects - reverses/messes with controls
 boolean warpReversed = player1WarpEffectTimer > 0 || player1InversionTimer > 0;
 boolean reversedControls = player1ReverseEffectTimer > 0 || warpReversed;
 
 // Puppet Master - opponent controls paddle
 if (player1PuppetEffectTimer > 0) {
 // AI moves paddle away from ball or to bad positions
 int targetY = ballY + 7;
 if (ballVelX < 0) {
 // Ball coming toward player 1 - move away from it
 if (paddle1Y + paddleHeight1/2 < targetY) {
 paddle1Y = Math.max(0, paddle1Y - 8);
 } else {
 paddle1Y = Math.min(paddle1MaxY, paddle1Y + 8);
 }
 } else {
 // Ball going away - move to random bad position
 if (Math.random() < 0.5) {
 paddle1Y = Math.max(0, paddle1Y - 5);
 } else {
 paddle1Y = Math.min(paddle1MaxY, paddle1Y + 5);
 }
 }
 } else {
 // Normal movement (or with debuffs)
 // Gravity Well (slow_opponent branch 2) - makes movement slower
 int moveSpeed = (int)getPlayerSpeed(1);
 if (getEffectiveAbilityLevel(2, "slow_opponent") >= 3 && player2AbilityBranches.getOrDefault("slow_opponent", 0) == 2) {
 int slowLevel = player2Abilities.get("slow_opponent") - 2;
 moveSpeed = Math.max(1, moveSpeed / 3 - slowLevel);
 }
 // Observation Haki - opponent (player 2) slows player 1's paddle
 if (player2ObservationActive) {
 moveSpeed = Math.max(1, (int)(moveSpeed * player1ObsSlowFactor));
 }

 // Chaos Engine debuff effects
 if (player1ChaosEffectTimer > 0) {
 if (player1CurrentDebuff == 1) {
 // Inverted Gravity - drift upward
 paddle1Y = Math.max(0, paddle1Y - 2);
 } else if (player1CurrentDebuff == 2) {
 // Double Speed - harder to control
 moveSpeed *= 2;
 }
 // Type 3 (Reversed Momentum) handled below
 }
 
 boolean moveUp = (reversedControls ? down1 : up1);
 boolean moveDown = (reversedControls ? up1 : down1);
 
 if (moveUp && paddle1Y > 0) {
 paddle1Y -= moveSpeed;
 // Chaos Engine Type 3 - Reversed Momentum
 if (player1ChaosEffectTimer > 0 && player1CurrentDebuff == 3) {
 paddle1Y = Math.min(paddle1MaxY, paddle1Y + moveSpeed / 2); // Slide opposite direction
 }
 }
 if (moveDown && paddle1Y < paddle1MaxY) {
 paddle1Y += moveSpeed;
 // Chaos Engine Type 3 - Reversed Momentum
 if (player1ChaosEffectTimer > 0 && player1CurrentDebuff == 3) {
 paddle1Y = Math.max(0, paddle1Y - moveSpeed / 2); // Slide opposite direction
 }
 }

 // SCREEN WARP - Inversion causes random jitter
 if (player1InversionTimer > 0) {
 int jitter = (int)(Math.random() * 20) - 10; // Random -10 to +10
 paddle1Y = Math.max(0, Math.min(paddle1MaxY, paddle1Y + jitter));
 }
 }
 }

 // Sonic Dash (speed_boost branch 1) - INSTANT dash to ball on Q press
 if (getEffectiveAbilityLevel(1, "speed_boost") >= 3 && player1AbilityBranches.getOrDefault("speed_boost", 0) == 1) {
 player1DashTimer++;
 // Dash happens instantly in keyPressed, this just counts down visual effect
 if (player1DashActive > 0) {
 player1DashActive--;
 }
 }
 
 if (getEffectiveAbilityLevel(2, "speed_boost") >= 3 && player2AbilityBranches.getOrDefault("speed_boost", 0) == 1) {
 player2DashTimer++;
 // Dash happens instantly in keyPressed, this just counts down visual effect
 if (player2DashActive > 0) {
 player2DashActive--;
 }
 }
 
 // Flash (speed_boost branch 2) - slows time around ball ONLY when ball coming toward you
 if (getEffectiveAbilityLevel(1, "speed_boost") >= 3 && player1AbilityBranches.getOrDefault("speed_boost", 0) == 2) {
 player1FlashTimer++;
 
 // Trigger Flash ONLY when ball is moving toward player 1 (ballVelX < 0)
 if (player1FlashTimer >= flashCooldown && ballVelX < 0) {
 player1FlashActive = flashDuration;
 player1FlashTimer = 0;
 }
 
 // Execute Flash effect - slow ball when active
 if (player1FlashActive > 0) {
 player1FlashActive--;
 }
 }
 
 if (getEffectiveAbilityLevel(2, "speed_boost") >= 3 && player2AbilityBranches.getOrDefault("speed_boost", 0) == 2) {
 player2FlashTimer++;
 
 // Trigger Flash ONLY when ball is moving toward player 2 (ballVelX > 0)
 if (player2FlashTimer >= flashCooldown && ballVelX > 0) {
 player2FlashActive = flashDuration;
 player2FlashTimer = 0;
 }
 
 // Execute Flash effect - slow ball when active
 if (player2FlashActive > 0) {
 player2FlashActive--;
 }
 }
 
 // Freeze Ray (slow_opponent branch 1) - freezes opponent every 8 seconds
 if (getEffectiveAbilityLevel(1, "slow_opponent") >= 3 && player1AbilityBranches.getOrDefault("slow_opponent", 0) == 1) {
 player1FreezeTimer++;
 
 // Trigger Freeze
 if (player1FreezeTimer >= freezeCooldown) {
 player1FreezeActive = freezeDuration;
 player1FreezeTimer = 0;
 }
 
 // Player 2 is frozen when active
 if (player1FreezeActive > 0) {
 player1FreezeActive--;
 }
 }
 
 if (getEffectiveAbilityLevel(2, "slow_opponent") >= 3 && player2AbilityBranches.getOrDefault("slow_opponent", 0) == 1) {
 player2FreezeTimer++;
 
 // Trigger Freeze
 if (player2FreezeTimer >= freezeCooldown) {
 player2FreezeActive = freezeDuration;
 player2FreezeTimer = 0;
 }
 
 // Player 1 is frozen when active
 if (player2FreezeActive > 0) {
 player2FreezeActive--;
 }
 }
 
 // Gravity Well (slow_opponent branch 2) - passive effect, makes opponent's paddle heavier
 // This is applied in the movement code below
 
 // Spectral Echo (ghost_ball branch 2) - passive effect, creates ghost trail
 // This is drawn in the rendering code
 if (getEffectiveAbilityLevel(1, "ghost_ball") >= 3 && player1AbilityBranches.getOrDefault("ghost_ball", 0) == 2) {
 player1SpectralActive = 1;
 } else {
 player1SpectralActive = 0;
 }
 
 if (getEffectiveAbilityLevel(2, "ghost_ball") >= 3 && player2AbilityBranches.getOrDefault("ghost_ball", 0) == 2) {
 player2SpectralActive = 1;
 } else {
 player2SpectralActive = 0;
 }
 
 // Lag effect - teleport paddle randomly
 if (player1LagEffectTimer > 0) {
 player1LagTeleportTimer++;
 if (player1LagTeleportTimer >= 15) { // Teleport every 0.15 seconds
 // Save original position first time
 if (player1LagTeleportTimer == 15) {
 player1SavedPaddleY = paddle1Y;
 }
 // Random teleport
 paddle1Y = (int)(Math.random() * (400 - paddleHeight1));
 player1LagTeleportTimer = 0;
 }
 } else if (player1LagTeleportTimer > 0) {
 // Lag ended, teleport back to saved position
 paddle1Y = player1SavedPaddleY;
 player1LagTeleportTimer = 0;
 }
 
 paddle1Y = Math.max(0, Math.min(paddle1MaxY, paddle1Y));
 
 // AI or Player 2 movement (only if not stunned AND not frozen)
 if (player2StunTimer == 0 && player1FreezeActive == 0) {
 if (singlePlayer) {
 // Check if this is Learning AI mode (difficulty 4)
 if (aiDifficulty == 4 && learningAIEnabled) {
 // LEARNING AI MODE - Starts weak, learns and improves
 int baseAiSpeed = (int)(2 * player2SpeedBonus); // Starts slower than normal
 
 // Apply learned speed (gets faster as it learns)
 int aiSpeed = (int)(baseAiSpeed * aiAdaptationMultiplier);
 
 // Apply ability modifiers
 if (getEffectiveAbilityLevel(2, "speed_boost") > 0) {
 int speedLevel = player2Abilities.get("speed_boost");
 aiSpeed = (int)(aiSpeed * (1.0 + 0.3 * speedLevel));
 }
 if (getEffectiveAbilityLevel(1, "slow_opponent") > 0) {
 int slowLevel = player1Abilities.get("slow_opponent");
 aiSpeed = (int)(aiSpeed * Math.pow(0.7, slowLevel));
 }
 
 // Gravity Well effect
 if (getEffectiveAbilityLevel(1, "slow_opponent") >= 3 && player1AbilityBranches.getOrDefault("slow_opponent", 0) == 2) {
 int slowLevel = player1Abilities.get("slow_opponent") - 2;
 aiSpeed = Math.max(1, aiSpeed - slowLevel * 2);
 }
 
 // Blind effect
 if (player2BlindEffectTimer > 0) {
 aiSpeed = (int)(aiSpeed * 0.4);
 }
 
 int ballCenterY = ballY + 7;
 int paddle2CenterY = paddle2Y + (paddleHeight2 / 2);
 int targetY = ballCenterY;
 
 // Get learned AI movement
 int learnedMovement = getLearnedAIMovement(paddle2CenterY, targetY, aiSpeed);
 
 // SCREEN WARP effects on AI
 boolean warpReversed = player2WarpEffectTimer > 0 || player2InversionTimer > 0;
 boolean reversedControls = player2ReverseEffectTimer > 0 || warpReversed;

 if (learnedMovement != 0) {
 // Apply learned movement
 if (reversedControls) learnedMovement = -learnedMovement;
 paddle2Y += learnedMovement;
 }
 // If no learned movement yet, AI barely moves (learning phase)
 else if (learningProgress < 0.1) {
 // Very early - AI is extremely weak
 if (Math.random() < 0.5 && Math.abs(targetY - paddle2CenterY) > 80) {
 int weakMove = (int)(aiSpeed * 0.5);
 if (reversedControls) {
 paddle2Y += (paddle2CenterY < targetY) ? -weakMove : weakMove;
 } else {
 paddle2Y += (paddle2CenterY < targetY) ? weakMove : -weakMove;
 }
 }
 }
 
 } else {
 // NORMAL/HARD/IMPOSSIBLE AI - TRULY HUMAN-LIKE AI - plays like a real person with realistic flaws
 int aiSpeed = (int)(3 * aiSpeedMultiplier * player2SpeedBonus);
 if (getEffectiveAbilityLevel(2, "speed_boost") > 0) {
 int speedLevel = player2Abilities.get("speed_boost");
 aiSpeed = (int)(aiSpeed * (1.0 + 0.3 * speedLevel));
 }
 if (getEffectiveAbilityLevel(1, "slow_opponent") > 0) {
 int slowLevel = player1Abilities.get("slow_opponent");
 aiSpeed = (int)(aiSpeed * Math.pow(0.7, slowLevel));
 }
 
 // Gravity Well (slow_opponent branch 2) - makes AI movement slower
 if (getEffectiveAbilityLevel(1, "slow_opponent") >= 3 && player1AbilityBranches.getOrDefault("slow_opponent", 0) == 2) {
 int slowLevel = player1Abilities.get("slow_opponent") - 2;
 aiSpeed = Math.max(1, aiSpeed - slowLevel * 2);
 }
 
 // Human-like attributes - much more imperfect and realistic
 double reactionDelay = 0.35; // Better reactions (buffed from 0.5)
 double overshootChance = 0.25; // 25% chance (buffed from 0.35)
 double mistakeChance = 0.18; // 18% chance (buffed from 0.25)
 double slowReactionChance = 0.2; // 20% chance (buffed from 0.3)
 int deadZone = 28; // Tighter (buffed from 35)
 int predictionError = 32; // Better (buffed from 40)
 
 if (aiDifficulty == 2) { // Hard - better player but still human (buffed)
 aiSpeed = (int)(aiSpeed * 1.45); // Faster (was 1.3)
 reactionDelay = 0.18; // Better (was 0.25)
 overshootChance = 0.15; // Less (was 0.2)
 mistakeChance = 0.08; // Fewer (was 0.12)
 slowReactionChance = 0.1; // Faster (was 0.15)
 deadZone = 15; // Tighter (was 20)
 predictionError = 18; // Better (was 25)
 } else if (aiDifficulty == 3) { // Impossible - skilled player (buffed)
 aiSpeed = (int)(aiSpeed * 1.7); // Even faster (was 1.5)
 reactionDelay = 0.05; // Lightning-fast (was 0.1)
 overshootChance = 0.04; // Minimal (was 0.08)
 mistakeChance = 0.02; // Very few (was 0.05)
 slowReactionChance = 0.02; // Nearly instant (was 0.05)
 deadZone = 8; // Pixel-perfect (was 12)
 predictionError = 10; // Excellent (was 15)
 }
 
 // Blind effect - severely impairs tracking
 if (player2BlindEffectTimer > 0) {
 reactionDelay = 0.8;
 mistakeChance = 0.6;
 deadZone = 60;
 aiSpeed = (int)(aiSpeed * 0.4);
 predictionError = 80;
 }
 
 int ballCenterY = ballY + 7;
 int paddle2CenterY = paddle2Y + (paddleHeight2 / 2);
 
 // Sometimes AI reacts slowly this frame (like a real player distracted)
 if (Math.random() < slowReactionChance) {
 aiSpeed = aiSpeed / 2;
 }
 
 // Human-like target - aims for ball with realistic delays and errors
 int targetY = ballCenterY;
 
 // REALISTIC REACTION DELAY - AI tracks where ball WAS, not where it is
 targetY += (int)(ballVelY * reactionDelay * -8);
 
 // RANDOM PREDICTION ERRORS - humans don't predict perfectly
 targetY += (int)((Math.random() - 0.5) * predictionError * 2);
 
 // When ball is far away, AI gets distracted and positions poorly
 if (ballX < 200 && Math.random() < 0.4) {
 // Sometimes drifts to center or poor position
 if (Math.random() < 0.5) {
 targetY = 200 + (int)((Math.random() - 0.5) * 100);
 }
 
 // Look for power-ups but sometimes makes bad choices
 for (PowerUp p : powerUps) {
 if (p.x > 350 && Math.abs(p.y - paddle2CenterY) < 120) {
 switch (p.type) {
 case "paddle": case "speed": case "jackpot":
 if (Math.random() < 0.5) targetY = p.y + (int)((Math.random() - 0.5) * 40); // Imperfect aim
 break;
 case "shrink": case "slow": case "reverse":
 // Sometimes accidentally goes for bad power-ups!
 if (Math.random() < 0.15) targetY = p.y;
 break;
 }
 }
 }
 }
 
 // HUMAN MISTAKES - realistic human behavior
 if (Math.random() < mistakeChance) {
 // Types of mistakes:
 double mistakeType = Math.random();
 if (mistakeType < 0.3) {
 // Move wrong direction briefly (panic move)
 targetY = paddle2CenterY + (targetY < paddle2CenterY ? 60 : -60);
 } else if (mistakeType < 0.6) {
 // Freeze / hesitate (indecision)
 aiSpeed = 0;
 } else {
 // Over-commit to bad position
 targetY += (int)((Math.random() - 0.5) * 80);
 }
 }
 
 // When ball is close and fast, AI sometimes panics and makes errors
 if (ballX > 450 && Math.abs(ballVelX) > 4 && Math.random() < 0.2) {
 // Panic positioning error
 targetY += (int)((Math.random() - 0.5) * 60);
 }
 
 // Move toward target with large dead zone (realistic human positioning)
 // SCREEN WARP effects on AI
 boolean warpReversed = player2WarpEffectTimer > 0 || player2InversionTimer > 0;
 boolean reversedControls = player2ReverseEffectTimer > 0 || warpReversed;

 if (targetY < paddle2CenterY - deadZone) {
 // Need to move up
 if (reversedControls) {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + aiSpeed);
 } else {
 paddle2Y = Math.max(0, paddle2Y - aiSpeed);
 // Human momentum - overshooting
 if (Math.random() < overshootChance) {
 paddle2Y = Math.max(0, paddle2Y - (int)(aiSpeed * 0.6));
 }
 }
 } else if (targetY > paddle2CenterY + deadZone) {
 // Need to move down
 if (reversedControls) {
 paddle2Y = Math.max(0, paddle2Y - aiSpeed);
 } else {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + aiSpeed);
 // Human momentum - overshooting
 if (Math.random() < overshootChance) {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + (int)(aiSpeed * 0.6));
 }
 }
 }
 // else: within dead zone, don't move (humans don't micro-adjust constantly)
 } // End of Learning AI / Normal AI block

 // AI ABILITY ACTIVATION - simulate pressing ability key
 // AI uses abilities strategically based on game state
 boolean aiShouldUseAbility = false;
 double aiUseChance = 0.02; // Base 2% chance per frame (~every 50 frames / 0.5s)

 if (aiDifficulty >= 2) aiUseChance = 0.03; // Hard: more frequent
 if (aiDifficulty >= 3) aiUseChance = 0.05; // Impossible: very frequent
 if (aiDifficulty >= 4) aiUseChance = 0.04; // Learning: moderate

 // Increase chance when ball is coming toward AI
 if (ballVelX > 0) {
 aiUseChance *= 2.0; // Double chance when ball approaching
 if (ballX > 400) aiUseChance *= 1.5; // Even more when ball is close
 }

 // Use abilities more aggressively when behind
 if (scorePlayer1 > scorePlayer2 + 2) {
 aiUseChance *= 1.5;
 }

 if (Math.random() < aiUseChance) {
 aiShouldUseAbility = true;
 }

 if (aiShouldUseAbility) {
 // Simulate pressing player2AbilityKey via keyPressed
 KeyEvent fakeKey = new KeyEvent(this, KeyEvent.KEY_PRESSED,
 System.currentTimeMillis(), 0, player2AbilityKey, KeyEvent.CHAR_UNDEFINED);
 keyPressed(fakeKey);
 }

 } else {
 // Two-player mode for player 2
 // Puppet Master - opponent controls paddle
 if (player2PuppetEffectTimer > 0) {
 // AI moves paddle away from ball or to bad positions
 int targetY = ballY + 7;
 if (ballVelX > 0) {
 // Ball coming toward player 2 - move away from it
 if (paddle2Y + paddleHeight2/2 < targetY) {
 paddle2Y = Math.max(0, paddle2Y - 8);
 } else {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + 8);
 }
 } else {
 // Ball going away - move to random bad position
 if (Math.random() < 0.5) {
 paddle2Y = Math.max(0, paddle2Y - 5);
 } else {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + 5);
 }
 }
 } else {
 // Normal movement (or with debuffs)
 // SCREEN WARP effects - reverses/messes with controls
 boolean warpReversed = player2WarpEffectTimer > 0 || player2InversionTimer > 0;
 boolean reversedControls = player2ReverseEffectTimer > 0 || warpReversed;

 // Gravity Well (slow_opponent branch 2) - makes movement slower
 int moveSpeed = (int)getPlayerSpeed(2);
 if (getEffectiveAbilityLevel(1, "slow_opponent") >= 3 && player1AbilityBranches.getOrDefault("slow_opponent", 0) == 2) {
 int slowLevel = player1Abilities.get("slow_opponent") - 2;
 moveSpeed = Math.max(1, moveSpeed / 3 - slowLevel);
 }
 // Observation Haki - opponent (player 1) slows player 2's paddle
 if (player1ObservationActive) {
 moveSpeed = Math.max(1, (int)(moveSpeed * player2ObsSlowFactor));
 }

 // Chaos Engine debuff effects
 if (player2ChaosEffectTimer > 0) {
 if (player2CurrentDebuff == 1) {
 // Inverted Gravity - drift upward
 paddle2Y = Math.max(0, paddle2Y - 2);
 } else if (player2CurrentDebuff == 2) {
 // Double Speed - harder to control
 moveSpeed *= 2;
 }
 }
 
 boolean moveUp = (reversedControls ? down2 : up2);
 boolean moveDown = (reversedControls ? up2 : down2);
 
 if (moveUp && paddle2Y > 0) {
 paddle2Y -= moveSpeed;
 // Chaos Engine Type 3 - Reversed Momentum
 if (player2ChaosEffectTimer > 0 && player2CurrentDebuff == 3) {
 paddle2Y = Math.min(paddle2MaxY, paddle2Y + moveSpeed / 2);
 }
 }
 if (moveDown && paddle2Y < paddle2MaxY) {
 paddle2Y += moveSpeed;
 // Chaos Engine Type 3 - Reversed Momentum
 if (player2ChaosEffectTimer > 0 && player2CurrentDebuff == 3) {
 paddle2Y = Math.max(0, paddle2Y - moveSpeed / 2);
 }
 }

 // SCREEN WARP - Inversion causes random jitter
 if (player2InversionTimer > 0) {
 int jitter = (int)(Math.random() * 20) - 10; // Random -10 to +10
 paddle2Y = Math.max(0, Math.min(paddle2MaxY, paddle2Y + jitter));
 }
 }
 }
 }

 // Lag effect - teleport paddle randomly
 if (player2LagEffectTimer > 0) {
 player2LagTeleportTimer++;
 if (player2LagTeleportTimer >= 15) { // Teleport every 0.15 seconds
 // Save original position first time
 if (player2LagTeleportTimer == 15) {
 player2SavedPaddleY = paddle2Y;
 }
 // Random teleport
 paddle2Y = (int)(Math.random() * (400 - paddleHeight2));
 player2LagTeleportTimer = 0;
 }
 } else if (player2LagTeleportTimer > 0) {
 // Lag ended, teleport back to saved position
 paddle2Y = player2SavedPaddleY;
 player2LagTeleportTimer = 0;
 }
 paddle2Y = Math.max(0, Math.min(paddle2MaxY, paddle2Y));
 
 // NEW ABILITIES LOGIC
 
 // Gravity Hammer - slam ball downward
 if (player1HammerActive && player1HammerDuration > 0) {
 player1HammerDuration--;
 if (player1HammerDuration == 0) player1HammerActive = false;
 }
 if (player2HammerActive && player2HammerDuration > 0) {
 player2HammerDuration--;
 if (player2HammerDuration == 0) player2HammerActive = false;
 }
 player1HammerTimer++;
 player2HammerTimer++;
 
 // Magnet Ball - attract ball to paddle (NERFED)
 if (getEffectiveAbilityLevel(1, "magnet_ball") > 0) {
 player1MagnetActive = true;
 int magnetLevel = player1Abilities.get("magnet_ball");
 int magnetBranch = player1AbilityBranches.getOrDefault("magnet_ball", 0);
 
 // Branch 1: Graviton Sphere - increased radius and strength (NERFED)
 int baseRadius = 200; // Reduced from 300
 int baseStrength = magnetLevel; // Reduced from 1 + magnetLevel
 
 if (magnetBranch == 1 && magnetLevel >= 3) {
 // Graviton Sphere: increased pull radius and strength (NERFED)
 baseRadius += (magnetLevel - 2) * 15; // Reduced from +20px per level
 baseStrength += (magnetLevel - 2); // Reduced from 2x multiplier to +1 per level
 }
 
 // If ball is within range
 if (ballX < baseRadius) {
 int paddle1CenterY = paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2;
 int ballCenterY = ballY + 7;
 int dy = paddle1CenterY - ballCenterY;
 
 // Branch 2: Force Field - toggle repel mode (NERFED)
 if (magnetBranch == 2 && magnetLevel >= 3 && player1MagnetRepelMode) {
 // Repulsion mode - push ball away when close
 int repelRadius = 15 + (magnetLevel - 2) * 3; // Reduced from 20 + 5 per level
 if (Math.abs(dy) < repelRadius && ballVelX < 0) {
 // Push away (weaker)
 ballY -= (dy > 0) ? baseStrength : -baseStrength; // Reduced from baseStrength * 2
 } else if (Math.abs(dy) > 5 && ballVelX < 0) {
 // Normal attraction when far (weaker)
 ballY += (dy > 0) ? Math.max(1, baseStrength / 2) : -Math.max(1, baseStrength / 2); // Half strength
 }
 } else if (Math.abs(dy) > 5 && ballVelX < 0) {
 // Normal attraction mode
 ballY += (dy > 0) ? baseStrength : -baseStrength;
 }
 }
 }
 if (getEffectiveAbilityLevel(2, "magnet_ball") > 0) {
 player2MagnetActive = true;
 int magnetLevel = player2Abilities.get("magnet_ball");
 int magnetBranch = player2AbilityBranches.getOrDefault("magnet_ball", 0);
 
 // Branch 1: Graviton Sphere - increased radius and strength (NERFED)
 int baseRadius = 200; // Reduced from 300
 int baseStrength = magnetLevel; // Reduced from 1 + magnetLevel
 
 if (magnetBranch == 1 && magnetLevel >= 3) {
 // Graviton Sphere: increased pull radius and strength (NERFED)
 baseRadius += (magnetLevel - 2) * 15; // Reduced from +20px per level
 baseStrength += (magnetLevel - 2); // Reduced from 2x multiplier to +1 per level
 }
 
 // If ball is within range
 if (ballX > 600 - baseRadius) {
 int paddle2CenterY = paddle2Y + getPaddleHeight(2, shrinkPaddlesActive) / 2;
 int ballCenterY = ballY + 7;
 int dy = paddle2CenterY - ballCenterY;
 
 // Branch 2: Force Field - toggle repel mode (NERFED)
 if (magnetBranch == 2 && magnetLevel >= 3 && player2MagnetRepelMode) {
 // Repulsion mode - push ball away when close
 int repelRadius = 15 + (magnetLevel - 2) * 3; // Reduced from 20 + 5 per level
 if (Math.abs(dy) < repelRadius && ballVelX > 0) {
 // Push away (weaker)
 ballY -= (dy > 0) ? baseStrength : -baseStrength; // Reduced from baseStrength * 2
 } else if (Math.abs(dy) > 5 && ballVelX > 0) {
 // Normal attraction when far (weaker)
 ballY += (dy > 0) ? Math.max(1, baseStrength / 2) : -Math.max(1, baseStrength / 2); // Half strength
 }
 } else if (Math.abs(dy) > 5 && ballVelX > 0) {
 // Normal attraction mode
 ballY += (dy > 0) ? baseStrength : -baseStrength;
 }
 }
 }
 
 // Gravity Hammer Branch 2: SINGULARITY - Powerful black hole effect!
 if (player1GravityWellActive && player1GravityWellDuration > 0) {
 int hammerLevel = player1Abilities.getOrDefault("gravity_hammer", 1);
 // MUCH larger radius and stronger pull!
 int wellRadius = 150 + (hammerLevel >= 3 ? (hammerLevel - 2) * 50 : 0); // Base 150px + 50px per level
 int wellStrength = 4 + (hammerLevel >= 3 ? (hammerLevel - 2) * 2 : 0); // Much stronger pull
 
 // Calculate distance from ball to gravity well center
 int dx = player1GravityWellX - ballX;
 int dy = player1GravityWellY - ballY;
 double distance = Math.sqrt(dx * dx + dy * dy);
 
 // If ball is within radius, pull toward center AND slow it down!
 if (distance < wellRadius && distance > 0) {
 // Pull toward center (stronger when closer)
 double pullMultiplier = 1.0 + (wellRadius - distance) / wellRadius; // Stronger near center
 ballX += (int)((dx / distance) * wellStrength * pullMultiplier);
 ballY += (int)((dy / distance) * wellStrength * pullMultiplier);
 
 // SLOW DOWN the ball when in the well (like a black hole!)
 double slowFactor = 0.85 - (distance / wellRadius) * 0.15; // 70-85% speed based on distance
 ballVelX = (int)(ballVelX * slowFactor);
 ballVelY = (int)(ballVelY * slowFactor);
 
 // Prevent ball from stopping completely
 if (Math.abs(ballVelX) < 1) ballVelX = ballVelX >= 0 ? 1 : -1;
 if (Math.abs(ballVelY) < 1 && Math.abs(dy) > 10) ballVelY = ballVelY >= 0 ? 1 : -1;
 }
 player1GravityWellDuration--;
 if (player1GravityWellDuration <= 0) {
 player1GravityWellActive = false;
 }
 }
 
 if (player2GravityWellActive && player2GravityWellDuration > 0) {
 int hammerLevel = player2Abilities.getOrDefault("gravity_hammer", 1);
 int wellRadius = 150 + (hammerLevel >= 3 ? (hammerLevel - 2) * 50 : 0);
 int wellStrength = 4 + (hammerLevel >= 3 ? (hammerLevel - 2) * 2 : 0);
 
 int dx = player2GravityWellX - ballX;
 int dy = player2GravityWellY - ballY;
 double distance = Math.sqrt(dx * dx + dy * dy);
 
 if (distance < wellRadius && distance > 0) {
 // Pull toward center (stronger when closer)
 double pullMultiplier = 1.0 + (wellRadius - distance) / wellRadius;
 ballX += (int)((dx / distance) * wellStrength * pullMultiplier);
 ballY += (int)((dy / distance) * wellStrength * pullMultiplier);
 
 // SLOW DOWN the ball
 double slowFactor = 0.85 - (distance / wellRadius) * 0.15;
 ballVelX = (int)(ballVelX * slowFactor);
 ballVelY = (int)(ballVelY * slowFactor);
 
 // Prevent ball from stopping
 if (Math.abs(ballVelX) < 1) ballVelX = ballVelX >= 0 ? 1 : -1;
 if (Math.abs(ballVelY) < 1 && Math.abs(dy) > 10) ballVelY = ballVelY >= 0 ? 1 : -1;
 }
 player2GravityWellDuration--;
 if (player2GravityWellDuration <= 0) {
 player2GravityWellActive = false;
 }
 }
 
 // Decrement shadow collision cooldowns
 if (player1ShadowCollisionCooldown > 0) player1ShadowCollisionCooldown--;
 if (player2ShadowCollisionCooldown > 0) player2ShadowCollisionCooldown--;
 
 // Shadow Clone - update shadow positions
 if (getEffectiveAbilityLevel(1, "shadow_clone") > 0) {
 int branch = player1AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 1) {
 // Branch 1: Multi Shadow Clone - multiple clones with same bad AI as base
 // Update positions every frame with smooth interpolation to prevent jittering
 int numShadows = getNumberOfShadows(1);
 
 // Initialize shadow positions if empty
 if (player1ShadowPositions.isEmpty()) {
 for (int i = 0; i < numShadows; i++) {
 player1ShadowPositions.add(200); // Start at center
 }
 }
 
 // Ensure we have the right number of shadows
 while (player1ShadowPositions.size() < numShadows) {
 player1ShadowPositions.add(200);
 }
 while (player1ShadowPositions.size() > numShadows) {
 player1ShadowPositions.remove(player1ShadowPositions.size() - 1);
 }
 
 // Update each shadow smoothly (not teleporting)
 for (int i = 0; i < numShadows; i++) {
 int currentY = player1ShadowPositions.get(i);
 int targetY;
 
 // Really dumb AI - just tries to stay somewhat centered, with lots of mistakes
 if (ballVelX < 0 && ballX < 300) {
 // Ball coming toward player 1's side - make a bad guess
 // Only predict when ball is close, and with terrible accuracy
 int prediction = ballY + (int)(ballVelY * (ballX / Math.abs(ballVelX)) * 0.6);
 int randomError = (int)(Math.random() * 100 - 50);
 int improvedPrediction = prediction + randomError; // Random offset of +/-100
 // Spread out shadows vertically based on index
 int spreadOffset = (i - numShadows / 2) * 80; // 80 pixels between each shadow
 targetY = Math.max(50, Math.min(350, improvedPrediction + spreadOffset));
 } else {
 // Just wander around slowly when ball is far
 // Offset each shadow so they spread out more
 int baseSpread = (i - numShadows / 2) * 100; // 100 pixels between each shadow
 targetY = 200 + baseSpread + (int)(60 * Math.sin((System.currentTimeMillis() + i * 1500) / 2000.0));
 }
 
 // Smoothly interpolate to target position instead of jumping
 int moveSpeed = Math.max(3, (int)getPlayerSpeed(1) * 2 / 3); // Faster movement (buffed)
 int newY = currentY;
 if (currentY < targetY - 5) {
 newY = Math.min(currentY + moveSpeed, targetY);
 } else if (currentY > targetY + 5) {
 newY = Math.max(currentY - moveSpeed, targetY);
 }
 
 // Keep in bounds
 newY = Math.max(0, Math.min(400 - getPaddleHeight(1, shrinkPaddlesActive), newY));
 player1ShadowPositions.set(i, newY);
 }
 } else if (branch == 2) {
 // Branch 2: Afterimage Defense - independent AI shadow (AGGRESSIVE BACKUP)
 // Shadow AI: Always tries to defend aggressively
 int targetY = player1IndependentShadowY;
 
 // If ball is coming toward player 1's side - ALWAYS defend aggressively
 if (ballVelX < 0 && ballX < 400) {
 // Predict ball position with high accuracy (85% accuracy - buffed from 70%)
 int prediction = ballY + (int)(ballVelY * (ballX / Math.abs(ballVelX)) * 0.85); // 85% accuracy
 prediction += (int)(Math.random() * 40 - 20); // +/- 20px random error (buffed from +/-30)
 prediction = Math.max(0, Math.min(400 - getPaddleHeight(1, shrinkPaddlesActive), prediction));
 
 // ALWAYS go for the ball, regardless of player position
 targetY = prediction;
 } else {
 // Ball is going away - stay near center, ready to defend
 targetY = 150 + (int)(50 * Math.sin(System.currentTimeMillis() / 2000.0)); // Patrol center area
 }
 
 // Speed increases with level (10% faster per level after 3)
 int shadowLevel = player1Abilities.getOrDefault("shadow_clone", 1);
 double speedMultiplier = shadowLevel >= 3 ? 1.0 + ((shadowLevel - 3) * 0.1) : 0.66; // 66% base, +10% per level
 int shadowSpeed = Math.max(1, (int)(getPlayerSpeed(1) * speedMultiplier));
 if (player1IndependentShadowY < targetY - 15) { // Larger deadzone = less reactive
 player1IndependentShadowY += shadowSpeed;
 player1ShadowMovingUp = false;
 } else if (player1IndependentShadowY > targetY + 15) {
 player1IndependentShadowY -= shadowSpeed;
 player1ShadowMovingUp = true;
 }
 
 // Keep shadow in bounds
 player1IndependentShadowY = Math.max(0, Math.min(400 - getPaddleHeight(1, shrinkPaddlesActive), player1IndependentShadowY));
 
 // Add to position history for rendering (but only keep recent)
 player1ShadowPositions.add(player1IndependentShadowY);
 if (player1ShadowPositions.size() > 5) {
 player1ShadowPositions.remove(0);
 }
 } else {
 // No branch yet (Levels 1-2) - independent shadow with REALLY BAD AI
 int targetY = player1IndependentShadowY;
 
 // Really dumb AI - just tries to stay somewhat centered, with lots of mistakes
 if (ballVelX < 0 && ballX < 300) {
 // Ball coming toward player 1's side - make a bad guess
 // Only predict when ball is close, and with terrible accuracy
 int prediction = ballY + (int)(ballVelY * (ballX / Math.abs(ballVelX)) * 0.6);
 int randomError = (int)(Math.random() * 100 - 50);
 int improvedPrediction = prediction + randomError; // Random offset of +/-100
 targetY = Math.max(50, Math.min(350, improvedPrediction));
 } else {
 // Just wander around slowly when ball is far
 targetY = 150 + (int)(80 * Math.sin(System.currentTimeMillis() / 2000.0)); // Slower patrol
 }
 
 // Move very slowly
 int shadowSpeed = Math.max(1, (int)getPlayerSpeed(1) / 2); // Half speed
 if (player1IndependentShadowY < targetY - 10) {
 player1IndependentShadowY += shadowSpeed;
 player1ShadowMovingUp = false;
 } else if (player1IndependentShadowY > targetY + 10) {
 player1IndependentShadowY -= shadowSpeed;
 player1ShadowMovingUp = true;
 }
 
 // Keep shadow in bounds
 player1IndependentShadowY = Math.max(0, Math.min(400 - getPaddleHeight(1, shrinkPaddlesActive), player1IndependentShadowY));
 
 // Add to position history for rendering
 player1ShadowPositions.add(player1IndependentShadowY);
 if (player1ShadowPositions.size() > 5) {
 player1ShadowPositions.remove(0);
 }
 }
 }
 
 if (getEffectiveAbilityLevel(2, "shadow_clone") > 0) {
 int branch = player2AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 1) {
 // Branch 1: Multi Shadow Clone - multiple clones with same bad AI as base
 // Update positions every frame with smooth interpolation to prevent jittering
 int numShadows = getNumberOfShadows(2);
 
 // Initialize shadow positions if empty
 if (player2ShadowPositions.isEmpty()) {
 for (int i = 0; i < numShadows; i++) {
 player2ShadowPositions.add(200); // Start at center
 }
 }
 
 // Ensure we have the right number of shadows
 while (player2ShadowPositions.size() < numShadows) {
 player2ShadowPositions.add(200);
 }
 while (player2ShadowPositions.size() > numShadows) {
 player2ShadowPositions.remove(player2ShadowPositions.size() - 1);
 }
 
 // Update each shadow smoothly (not teleporting)
 for (int i = 0; i < numShadows; i++) {
 int currentY = player2ShadowPositions.get(i);
 int targetY;
 
 // Really dumb AI - just tries to stay somewhat centered, with lots of mistakes
 if (ballVelX > 0 && ballX > 300) {
 // Ball coming toward player 2's side - make a bad guess
 // Only predict when ball is close, and with terrible accuracy
 int prediction = ballY + (int)(ballVelY * (ballX / Math.abs(ballVelX)) * 0.6);
 int randomError = (int)(Math.random() * 100 - 50);
 int improvedPrediction = prediction + randomError; // Random offset of +/-100
 // Spread out shadows vertically based on index
 int spreadOffset = (i - numShadows / 2) * 80; // 80 pixels between each shadow
 targetY = Math.max(50, Math.min(350, improvedPrediction + spreadOffset));
 } else {
 // Just wander around slowly when ball is far
 // Offset each shadow so they spread out more
 int baseSpread = (i - numShadows / 2) * 100; // 100 pixels between each shadow
 targetY = 200 + baseSpread + (int)(60 * Math.sin((System.currentTimeMillis() + i * 1500) / 2000.0));
 }
 
 // Smoothly interpolate to target position instead of jumping
 int moveSpeed = Math.max(3, (int)getPlayerSpeed(2) * 2 / 3); // Faster movement (buffed)
 int newY = currentY;
 if (currentY < targetY - 5) {
 newY = Math.min(currentY + moveSpeed, targetY);
 } else if (currentY > targetY + 5) {
 newY = Math.max(currentY - moveSpeed, targetY);
 }
 
 // Keep in bounds
 newY = Math.max(0, Math.min(400 - getPaddleHeight(2, shrinkPaddlesActive), newY));
 player2ShadowPositions.set(i, newY);
 }
 } else if (branch == 2) {
 // Branch 2: Afterimage Defense - independent AI shadow (AGGRESSIVE BACKUP)
 int targetY = player2IndependentShadowY;
 
 // If ball is coming toward player 2's side - ALWAYS defend aggressively
 if (ballVelX > 0 && ballX > 200) {
 // Predict ball position with moderate accuracy (70% accuracy)
 int prediction = ballY + (int)(ballVelY * ((600 - ballX) / Math.abs(ballVelX)) * 0.7); // 70% accuracy
 prediction += (int)(Math.random() * 60 - 30); // +/- 30px random error (reduced)
 prediction = Math.max(0, Math.min(400 - getPaddleHeight(2, shrinkPaddlesActive), prediction));
 
 // ALWAYS go for the ball, regardless of player position
 targetY = prediction;
 } else {
 // Ball is going away - stay near center, ready to defend
 targetY = 150 + (int)(50 * Math.sin(System.currentTimeMillis() / 2000.0)); // Patrol center area
 }
 
 // Speed increases with level (10% faster per level after 3)
 int shadowLevel = player2Abilities.getOrDefault("shadow_clone", 1);
 double speedMultiplier = shadowLevel >= 3 ? 1.0 + ((shadowLevel - 3) * 0.1) : 0.66; // 66% base, +10% per level
 int shadowSpeed = Math.max(1, (int)(getPlayerSpeed(2) * speedMultiplier));
 if (player2IndependentShadowY < targetY - 15) { // Larger deadzone = less reactive
 player2IndependentShadowY += shadowSpeed;
 player2ShadowMovingUp = false;
 } else if (player2IndependentShadowY > targetY + 15) {
 player2IndependentShadowY -= shadowSpeed;
 player2ShadowMovingUp = true;
 }
 
 // Keep shadow in bounds
 player2IndependentShadowY = Math.max(0, Math.min(400 - getPaddleHeight(2, shrinkPaddlesActive), player2IndependentShadowY));
 
 // Add to position history for rendering (but only keep recent)
 player2ShadowPositions.add(player2IndependentShadowY);
 if (player2ShadowPositions.size() > 5) {
 player2ShadowPositions.remove(0);
 }
 } else {
 // No branch yet (Levels 1-2) - independent shadow with REALLY BAD AI
 int targetY = player2IndependentShadowY;
 
 // Really dumb AI - just tries to stay somewhat centered, with lots of mistakes
 if (ballVelX > 0 && ballX > 300) {
 // Ball coming toward player 2's side - make a bad guess
 // Only predict when ball is close, and with terrible accuracy
 int prediction = ballY + (int)(ballVelY * (ballX / Math.abs(ballVelX)) * 0.6);
 int randomError = (int)(Math.random() * 100 - 50);
 int improvedPrediction = prediction + randomError; // Random offset of +/-100
 targetY = Math.max(50, Math.min(350, improvedPrediction));
 } else {
 // Just wander around slowly when ball is far
 targetY = 150 + (int)(80 * Math.sin(System.currentTimeMillis() / 2000.0)); // Slower patrol
 }
 
 // Move very slowly
 int shadowSpeed = Math.max(1, (int)getPlayerSpeed(2) / 2); // Half speed
 if (player2IndependentShadowY < targetY - 10) {
 player2IndependentShadowY += shadowSpeed;
 player2ShadowMovingUp = false;
 } else if (player2IndependentShadowY > targetY + 10) {
 player2IndependentShadowY -= shadowSpeed;
 player2ShadowMovingUp = true;
 }
 
 // Keep shadow in bounds
 player2IndependentShadowY = Math.max(0, Math.min(400 - getPaddleHeight(2, shrinkPaddlesActive), player2IndependentShadowY));
 
 // Add to position history for rendering
 player2ShadowPositions.add(player2IndependentShadowY);
 if (player2ShadowPositions.size() > 5) {
 player2ShadowPositions.remove(0);
 }
 }
 }
 
 // Portal Pong - update timers
 if (player1PortalDuration > 0) {
 player1PortalDuration--;
 
 // Branch 2: Dimensional Rift - portals move randomly
 int portalBranch = player1AbilityBranches.getOrDefault("portal_pong", 0);
 int portalLevel = player1Abilities.getOrDefault("portal_pong", 1);
 if (portalBranch == 2 && portalLevel >= 3) {
 player1PortalMoveTimer--;
 if (player1PortalMoveTimer <= 0) {
 // Move portals randomly
 int moveRange = 30 + (portalLevel - 2) * 10; // Increases with level
 if (player1PortalEntranceX != null && player1PortalEntranceY != null) {
 player1PortalEntranceX = Math.max(50, Math.min(550, player1PortalEntranceX + (int)(Math.random() * moveRange * 2 - moveRange)));
 player1PortalEntranceY = Math.max(20, Math.min(380, player1PortalEntranceY + (int)(Math.random() * moveRange * 2 - moveRange)));
 }
 if (player1PortalExitX != null && player1PortalExitY != null) {
 player1PortalExitX = Math.max(50, Math.min(550, player1PortalExitX + (int)(Math.random() * moveRange * 2 - moveRange)));
 player1PortalExitY = Math.max(20, Math.min(380, player1PortalExitY + (int)(Math.random() * moveRange * 2 - moveRange)));
 }
 player1PortalMoveTimer = 200; // Reset timer - move every 2 seconds
 }
 }
 
 if (player1PortalDuration == 0) {
 // Portals expired
 player1PortalEntranceX = null;
 player1PortalEntranceY = null;
 player1PortalExitX = null;
 player1PortalExitY = null;
 player1PortalPlacementStage = 0;
 }
 }
 if (player2PortalDuration > 0) {
 player2PortalDuration--;
 
 int portalBranch = player2AbilityBranches.getOrDefault("portal_pong", 0);
 int portalLevel = player2Abilities.getOrDefault("portal_pong", 1);
 if (portalBranch == 2 && portalLevel >= 3) {
 player2PortalMoveTimer--;
 if (player2PortalMoveTimer <= 0) {
 int moveRange = 30 + (portalLevel - 2) * 10;
 if (player2PortalEntranceX != null && player2PortalEntranceY != null) {
 player2PortalEntranceX = Math.max(50, Math.min(550, player2PortalEntranceX + (int)(Math.random() * moveRange * 2 - moveRange)));
 player2PortalEntranceY = Math.max(20, Math.min(380, player2PortalEntranceY + (int)(Math.random() * moveRange * 2 - moveRange)));
 }
 if (player2PortalExitX != null && player2PortalExitY != null) {
 player2PortalExitX = Math.max(50, Math.min(550, player2PortalExitX + (int)(Math.random() * moveRange * 2 - moveRange)));
 player2PortalExitY = Math.max(20, Math.min(380, player2PortalExitY + (int)(Math.random() * moveRange * 2 - moveRange)));
 }
 player2PortalMoveTimer = 200;
 }
 }
 
 if (player2PortalDuration == 0) {
 player2PortalEntranceX = null;
 player2PortalEntranceY = null;
 player2PortalExitX = null;
 player2PortalExitY = null;
 player2PortalPlacementStage = 0;
 }
 }
 if (player1PortalTimer < 2000) player1PortalTimer++;
 if (player2PortalTimer < 2000) player2PortalTimer++;
 if (portalBoostTimer > 0) portalBoostTimer--;
 if (portalTrailTimer > 0) portalTrailTimer--;
 
 // Teleport cooldown (prevent infinite portal loops)
 if (ballTeleportCooldown) {
 ballTeleportTimer++;
 if (ballTeleportTimer > 20) {
 ballTeleportCooldown = false;
 ballTeleportTimer = 0;
 }
 }
 
 // Power Siphon - drain opponent cooldowns when active
 if (player1SiphonActive && player1SiphonDuration > 0) {
 player1SiphonDuration--;
 if (player1SiphonDuration == 0) player1SiphonActive = false;
 }
 if (player2SiphonActive && player2SiphonDuration > 0) {
 player2SiphonDuration--;
 if (player2SiphonDuration == 0) player2SiphonActive = false;
 }
 player1SiphonTimer++;
 player2SiphonTimer++;
 
 // Power Siphon - NEW BRANCH TIMERS!
 // Soul Reaper cooldown and duration
 player1SoulReapTimer++;
 player2SoulReapTimer++;
 if (player1CopiedAbilityDuration > 0) {
 player1CopiedAbilityDuration--;
 if (player1CopiedAbilityDuration == 0) player1CopiedAbility = null;
 }
 if (player2CopiedAbilityDuration > 0) {
 player2CopiedAbilityDuration--;
 if (player2CopiedAbilityDuration == 0) player2CopiedAbility = null;
 }
 
 // Overload charge decay and effect timers
 if (player1OverloadCharge > 0) player1OverloadCharge = Math.max(0, player1OverloadCharge - 2); // Decay 2 per frame
 if (player2OverloadCharge > 0) player2OverloadCharge = Math.max(0, player2OverloadCharge - 2);
 if (player1OverloadDisableTimer > 0) player1OverloadDisableTimer--;
 if (player2OverloadDisableTimer > 0) player2OverloadDisableTimer--;
 if (player1OverloadShrinkTimer > 0) player1OverloadShrinkTimer--;
 if (player2OverloadShrinkTimer > 0) player2OverloadShrinkTimer--;
 
 // Time Loop - save game states (history size scales with level)
 if (getEffectiveAbilityLevel(1, "time_loop") > 0 || getEffectiveAbilityLevel(2, "time_loop") > 0) {
 int timeLoopLevel = Math.max(getEffectiveAbilityLevel(1, "time_loop"), getEffectiveAbilityLevel(2, "time_loop"));
 int currentMaxHistory = 30 + (timeLoopLevel - 1) * 20; // 0.3s base + 0.2s per level (20 frames = 0.2s at 100fps)
 
 gameStateHistory.add(new GameState(ballX, ballY, ballVelX, ballVelY, paddle1Y, paddle2Y, scorePlayer1, scorePlayer2));
 if (gameStateHistory.size() > currentMaxHistory) {
 gameStateHistory.remove(0);
 }
 }
 player1TimeLoopTimer++;
 player2TimeLoopTimer++;
 
 // Update time loop slow motion timers
 if (player1TimeLoopSlowMoTimer > 0) player1TimeLoopSlowMoTimer--;
 if (player2TimeLoopSlowMoTimer > 0) player2TimeLoopSlowMoTimer--;

 // NEW ABILITY TIMER UPDATES

 // Haki timers
 player1HakiTimer++;
 player2HakiTimer++;
 if (player1ArmamentActive && player1ArmamentDuration > 0) {
 player1ArmamentDuration--;
 if (player1ArmamentDuration == 0) {
 player1ArmamentActive = false;
 player1HakiPhaseActive = false; // Clear phasing when armament ends
 }
 }
 if (player2ArmamentActive && player2ArmamentDuration > 0) {
 player2ArmamentDuration--;
 if (player2ArmamentDuration == 0) {
 player2ArmamentActive = false;
 player2HakiPhaseActive = false; // Clear phasing when armament ends
 }
 }
 if (player1ObservationActive && player1ObservationDuration > 0) {
 player1ObservationDuration--;
 if (player1ObservationDuration == 0) {
 player1ObservationActive = false;
 player2ObsSlowFactor = 1.0; // Reset opponent slow
 }
 }
 if (player2ObservationActive && player2ObservationDuration > 0) {
 player2ObservationDuration--;
 if (player2ObservationDuration == 0) {
 player2ObservationActive = false;
 player1ObsSlowFactor = 1.0; // Reset opponent slow
 }
 }

 // Barrier timers
 player1BarrierTimer++;
 player2BarrierTimer++;
 if (player1BarrierActive && player1BarrierDuration > 0) {
 player1BarrierDuration--;
 if (player1BarrierDuration == 0) player1BarrierActive = false;
 }
 if (player2BarrierActive && player2BarrierDuration > 0) {
 player2BarrierDuration--;
 if (player2BarrierDuration == 0) player2BarrierActive = false;
 }

 // Trap timers
 player1TrapTimer++;
 player2TrapTimer++;
 if (player1TrapActive && player1TrapDuration > 0) {
 player1TrapDuration--;
 if (player1TrapDuration == 0) player1TrapActive = false;
 }
 if (player2TrapActive && player2TrapDuration > 0) {
 player2TrapDuration--;
 if (player2TrapDuration == 0) player2TrapActive = false;
 }
 // Sticky effect timers
 if (player1StickyTimer > 0) player1StickyTimer--;
 if (player2StickyTimer > 0) player2StickyTimer--;

 // Screen Warp timers
 player1WarpTimer++;
 player2WarpTimer++;
 if (player1WarpEffectTimer > 0) player1WarpEffectTimer--;
 if (player2WarpEffectTimer > 0) player2WarpEffectTimer--;
 if (player1InversionTimer > 0) player1InversionTimer--;
 if (player2InversionTimer > 0) player2InversionTimer--;
 if (player1TunnelTimer > 0) player1TunnelTimer--;
 if (player2TunnelTimer > 0) player2TunnelTimer--;

 // Bankai timers
 player1BankaiTimer++;
 player2BankaiTimer++;
 if (player1BankaiActive && player1BankaiDuration > 0) {
 player1BankaiDuration--;
 if (player1BankaiDuration == 0) {
 player1BankaiActive = false;
 player1ZangetsuActive = false;
 player1HollowActive = false;
 }
 }
 if (player2BankaiActive && player2BankaiDuration > 0) {
 player2BankaiDuration--;
 if (player2BankaiDuration == 0) {
 player2BankaiActive = false;
 player2ZangetsuActive = false;
 player2HollowActive = false;
 }
 }

 // Sword swing timers
 if (player1SwordSwingTimer > 0) player1SwordSwingTimer--;
 if (player2SwordSwingTimer > 0) player2SwordSwingTimer--;
 if (player1SwordSwingCooldown > 0) player1SwordSwingCooldown--;
 if (player2SwordSwingCooldown > 0) player2SwordSwingCooldown--;

 // GETSUGA TENSHO projectile movement and collision
 for (int i = getsugaProjectiles.size() - 1; i >= 0; i--) {
 GetsugaTensho gt = getsugaProjectiles.get(i);
 gt.x += gt.velocityX;

 // Remove if off screen
 if (gt.x < -100 || gt.x > 700) {
 getsugaProjectiles.remove(i);
 continue;
 }

 // Collision with opponent paddle
 int targetPaddleY = (gt.owner == 1) ? paddle2Y : paddle1Y;
 int targetPaddleX = (gt.owner == 1) ? 575 : 5;
 int targetPaddleHeight = getPaddleHeight(gt.owner == 1 ? 2 : 1, shrinkPaddlesActive);
 Rectangle getsugaRect = new Rectangle(gt.x - gt.width/2, gt.y - gt.height/2, gt.width, gt.height);
 Rectangle paddleRect = new Rectangle(targetPaddleX, targetPaddleY, 10, targetPaddleHeight);

 if (getsugaRect.intersects(paddleRect)) {
 // HIT! Stun opponent and shrink their paddle!
 if (gt.owner == 1) {
 if (player2StunImmunityTimer == 0) player2StunTimer = 150; // 1.5s stun
 player2ShrinkEffectTimer = 300; // 3s shrink
 if (gt.isEvolved) {
 // Evolved Getsuga: Also reverse controls and steal a point!
 player2ReverseEffectTimer = 200;
 if (scorePlayer2 > 0) { scorePlayer2--; scorePlayer1++; }
 }
 } else {
 if (player1StunImmunityTimer == 0) player1StunTimer = 150;
 player1ShrinkEffectTimer = 300;
 if (gt.isEvolved) {
 player1ReverseEffectTimer = 200;
 if (scorePlayer1 > 0) { scorePlayer1--; scorePlayer2++; }
 }
 }
 getsugaProjectiles.remove(i);
 }
 }

 // No-score timer (for 2-minute upgrade feature)
 noScoreTimer++;

 // Ball not hit timer - reset ball if not hit by any player for 10 seconds
 if (!ballFrozenByTrap) { // Don't count while ball is frozen
 ballNotHitTimer++;
 if (ballNotHitTimer >= ballResetThreshold) {
 // Reset ball to center
 ballX = 295;
 ballY = 200;
 ballSpeed = 5.0;
 // Random direction
 ballVelX = (Math.random() > 0.5) ? 3 : -3;
 ballVelY = (int)(Math.random() * 4) - 2;
 if (ballVelY == 0) ballVelY = 1;
 ballNotHitTimer = 0;
 }
 }

 // Prevent ball from getting stuck going horizontally
 if (Math.abs(ballVelY) < 1 && Math.abs(ballVelX) > 0) {
 ballVelY = (Math.random() > 0.5) ? 2 : -2;
 }

 // Ball movement
 int adjustedVelX = (int)(ballVelX * ballSpeedMultiplier);
 int adjustedVelY = (int)(ballVelY * ballSpeedMultiplier);
 
 // Flash (speed_boost branch 2) - slows ball when active
 if (player1FlashActive > 0 || player2FlashActive > 0) {
 adjustedVelX = adjustedVelX / 3; // Slow ball to 33% speed
 adjustedVelY = adjustedVelY / 3;
 }
 
 // Observation Haki - ball at 60% speed (NOT modifying ballVelX/Y directly)
 if (player1ObservationActive || player2ObservationActive) {
 adjustedVelX = (int)(adjustedVelX * 0.6);
 adjustedVelY = (int)(adjustedVelY * 0.6);
 if (Math.abs(adjustedVelX) < 1) adjustedVelX = adjustedVelX >= 0 ? 1 : -1;
 if (Math.abs(adjustedVelY) < 1) adjustedVelY = adjustedVelY >= 0 ? 1 : -1;
 }

 // Time Loop slow motion effect - slows ball after time loop is activated
 if (player1TimeLoopSlowMoTimer > 0 || player2TimeLoopSlowMoTimer > 0) {
 // Check if either player has Chronos Rewind (Branch 1)
 int p1Branch = player1AbilityBranches.getOrDefault("time_loop", 0);
 int p2Branch = player2AbilityBranches.getOrDefault("time_loop", 0);
 int p1Level = player1Abilities.getOrDefault("time_loop", 0);
 int p2Level = player2Abilities.getOrDefault("time_loop", 0);
 
 boolean chronosActive = (p1Branch == 1 && p1Level >= 3 && player1TimeLoopSlowMoTimer > 0) ||
 (p2Branch == 1 && p2Level >= 3 && player2TimeLoopSlowMoTimer > 0);
 
 if (chronosActive) {
 // Chronos Rewind: stronger slow-mo (20% speed)
 adjustedVelX = adjustedVelX / 5;
 adjustedVelY = adjustedVelY / 5;
 } else {
 // Base time loop: 50% speed
 adjustedVelX = adjustedVelX / 2;
 adjustedVelY = adjustedVelY / 2;
 }
 }
 
 ballX += adjustedVelX;
 ballY += adjustedVelY;
 int ballCenterX = ballX + 7;
 int ballCenterY = ballY + 7;
 
 // Danger zone effect
 if (dangerZoneActive) {
 dangerZoneDuration++;
 if (dangerZoneDuration >= maxDangerZoneDuration) {
 dangerZoneActive = false;
 dangerZoneDuration = 0;
 dangerZoneTime = 0;
 }
 double distanceFromCenter = Math.sqrt(Math.pow(ballCenterX - dangerZoneCenterX, 2) + Math.pow(ballCenterY - dangerZoneCenterY, 2));
 if (distanceFromCenter <= dangerZoneRadius) {
 dangerZoneTime = Math.min(maxDangerTime, dangerZoneTime + 1);
 double speedBoost = 1 + (dangerZoneTime / (double)maxDangerTime) * 0.5;
 ballX += (int)(adjustedVelX * (speedBoost - 1));
 ballY += (int)(adjustedVelY * (speedBoost - 1));
 double angleToCenter = Math.atan2(ballCenterY - dangerZoneCenterY, ballCenterX - dangerZoneCenterX);
 double curveFactor = (dangerZoneTime / (double)maxDangerTime) * 2;
 ballVelY += (int)(Math.sin(angleToCenter + Math.PI / 2) * curveFactor);
 }
 }
 
 // Temporal Echo - Branch 2 of Time Loop
 if (temporalEchoActive && temporalEchoDuration > 0) {
 // Move ghost ball with original trajectory
 echoGhostBallX += echoGhostBallVelX;
 echoGhostBallY += echoGhostBallVelY;
 
 // Ghost ball wall bounces
 if (echoGhostBallY <= 0 || echoGhostBallY >= 385) {
 echoGhostBallVelY *= -1;
 }
 
 // Ghost ball scoring
 Rectangle echoRect = new Rectangle(echoGhostBallX, echoGhostBallY, 15, 15);
 if (echoGhostBallX < 0) {
 // Ghost ball scored for player 2
 scorePlayer2++;
 temporalEchoActive = false;
 temporalEchoDuration = 0;
 } else if (echoGhostBallX > 600) {
 // Ghost ball scored for player 1
 scorePlayer1++;
 temporalEchoActive = false;
 temporalEchoDuration = 0;
 }
 
 // Ghost ball paddle collisions
 Rectangle echoP1Rect = new Rectangle(20, paddle1Y, 10, getPaddleHeight(1, shrinkPaddlesActive));
 Rectangle echoP2Rect = new Rectangle(580, paddle2Y, 10, getPaddleHeight(2, shrinkPaddlesActive));
 
 if (echoRect.intersects(echoP1Rect) && echoGhostBallVelX < 0) {
 echoGhostBallVelX = Math.abs(echoGhostBallVelX) + 1;
 }
 if (echoRect.intersects(echoP2Rect) && echoGhostBallVelX > 0) {
 echoGhostBallVelX = -(Math.abs(echoGhostBallVelX) + 1);
 }
 
 temporalEchoDuration--;
 if (temporalEchoDuration <= 0) {
 temporalEchoActive = false;
 }
 }
 
 // Teleport effect - show particles/trail
 if (teleportActive) {
 teleportDuration++;
 if (teleportDuration >= maxTeleportDuration) {
 teleportActive = false;
 teleportDuration = 0;
 }
 }
 
 // Fireball effect - ball moves faster and leaves trail
 if (fireballActive) {
 fireballDuration++;
 if (fireballDuration >= maxFireballDuration) {
 fireballActive = false;
 fireballDuration = 0;
 // Restore initial speed and velocity
 ballSpeed = initialBallSpeed;
 // Update velocity components to match restored speed while preserving direction
 double magnitude = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 if (magnitude > 0) {
 ballVelX = (int)((ballVelX / magnitude) * ballSpeed);
 ballVelY = (int)((ballVelY / magnitude) * ballSpeed);
 }
 }
 }
 
 // Zigzag effect - ball changes vertical direction randomly
 if (zigzagActive) {
 zigzagDuration++;
 zigzagTimer++;
 if (zigzagDuration >= maxZigzagDuration) {
 zigzagActive = false;
 zigzagDuration = 0;
 zigzagTimer = 0;
 }
 if (zigzagTimer >= zigzagInterval) {
 ballVelY = (Math.random() < 0.5 ? -1 : 1) * (2 + (int)(Math.random() * 4));
 zigzagTimer = 0;
 }
 }
 
 // Split effect - handled in rendering
 if (splitActive) {
 splitDuration++;
 if (splitDuration >= maxSplitDuration) {
 splitActive = false;
 splitDuration = 0;
 }
 }
 
 // Mirror effect - game stays flipped
 if (mirrorActive) {
 mirrorDuration++;
 if (mirrorDuration >= maxMirrorDuration) {
 mirrorActive = false;
 mirrorDuration = 0;
 // The transform automatically stops when mirrorActive becomes false
 // No popup - mirror effect just ends silently
 }
 }
 
 // Invisible walls effect
 if (invisibleWallsActive) {
 invisibleWallsDuration++;
 if (invisibleWallsDuration >= 800) {
 invisibleWallsActive = false;
 invisibleWallsDuration = 0;
 }
 if (Math.abs(ballCenterY - invisibleWallY) < 10) {
 ballVelY *= -1;
 ballY = invisibleWallY + (ballVelY > 0 ? 5 : -20);
 }
 }
 
 // Shrink paddles effect
 if (shrinkPaddlesActive) {
 shrinkPaddlesDuration++;
 if (shrinkPaddlesDuration >= 800) {
 shrinkPaddlesActive = false;
 shrinkPaddlesDuration = 0;
 }
 }
 
 // Center wall effect
 if (centerWallActive) {
 centerWallDuration++;
 if (centerWallDuration >= 800) {
 centerWallActive = false;
 centerWallDuration = 0;
 }
 // Check if ball is crossing the center wall (X position 295-305)
 if (ballCenterX >= 295 && ballCenterX <= 305) {
 // Check if ball hit the wall (not the gap)
 if (ballCenterY < centerWallGapY - centerWallGapSize / 2 ||
 ballCenterY > centerWallGapY + centerWallGapSize / 2) {
 // Reverse X velocity
 ballVelX *= -1;
 // Push ball to appropriate side of wall
 if (ballVelX > 0) {
 ballX = 306; // Push to right side
 } else {
 ballX = 284; // Push to left side
 }
 }
 }
 }
 
 // Wall bounces - supplementary angle (normal reflection physics)
 if (ballY <= 0 || ballY >= 385) {
 // Reverse Y velocity for supplementary angle bounce
 ballVelY *= -1;

 // Move ball away from wall to prevent getting stuck
 if (ballY <= 0) {
 ballY = 1;
 } else if (ballY >= 385) {
 ballY = 384;
 }

 // Thor's Hammer shockwave - stun opponent when ball hits floor/ceiling after slam
 if (player1ShockwaveTimer > 0 && player2StunImmunityTimer == 0) {
 player2StunTimer = player1ShockwaveTimer;
 player1ShockwaveTimer = 0; // Reset after triggering
 }
 if (player2ShockwaveTimer > 0 && player1StunImmunityTimer == 0) {
 player1StunTimer = player2ShockwaveTimer;
 player2ShockwaveTimer = 0;
 }
 }
 
 // Portal teleportation - ENHANCED WITH OFFENSIVE BOOST (BIDIRECTIONAL)
 if (!ballTeleportCooldown) {
 // Player 1 portals - BIDIRECTIONAL (ball can enter either portal)
 if (player1PortalEntranceX != null && player1PortalExitX != null && 
 player1PortalPlacementStage == 2 && player1PortalDuration > 0) {
 
 int portalRadius = 30; // Slightly larger for easier use
 double distPortal1 = Math.sqrt(
 Math.pow(ballX - player1PortalEntranceX, 2) + 
 Math.pow(ballY - player1PortalEntranceY, 2)
 );
 double distPortal2 = Math.sqrt(
 Math.pow(ballX - player1PortalExitX, 2) + 
 Math.pow(ballY - player1PortalExitY, 2)
 );
 
 // Check if ball enters either portal
 if (distPortal1 < portalRadius) {
 // TELEPORT from portal 1 to portal 2!
 ballX = player1PortalExitX;
 ballY = player1PortalExitY;
 
 // OFFENSIVE BOOST - Speed increase based on level and branch
 int portalLevel = player1Abilities.getOrDefault("portal_pong", 1);
 int portalBranch = player1AbilityBranches.getOrDefault("portal_pong", 0);
 
 int speedBoost;
 if (portalBranch == 1 && portalLevel >= 3) {
 // Branch 1: Wormhole Master - increased speed boost
 speedBoost = 2 + (portalLevel - 2) * 5; // +5 per level
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Branch 2: Dimensional Rift - random speed boost
 speedBoost = 2 + (int)(Math.random() * 8); // Random 2-10 boost
 } else {
 speedBoost = 2 + Math.min(portalLevel - 1, 2); // Base: 2, 3, or 4
 }
 
 // REVERSE horizontal direction to send ball back to opponent (Player 2 side = positive)
 ballVelX = (Math.abs(ballVelX) + speedBoost); // Always positive (toward Player 2)
 ballVelY += (ballVelY > 0 ? 1 : -1) * (speedBoost / 2);
 
 // Level 2+: Angle manipulation toward opponent
 if (portalLevel >= 2) {
 // Calculate angle toward opponent's goal (right side for P1)
 int targetX = 580; // Opponent's paddle area
 int targetY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Bias the ball velocity toward opponent
 int deltaY = targetY - ballY;
 if (Math.abs(deltaY) > 50) {
 ballVelY += (deltaY > 0 ? 2 : -2);
 }
 }
 
 // Level 3+: Even more aggressive angle
 if (portalLevel >= 3) {
 // Add some randomness for unpredictability
 ballVelY += (int)(Math.random() * 4) - 2;
 ballVelX += 2; // Extra speed toward opponent!
 }
 
 ballTeleportCooldown = true;
 portalBoostTimer = 30; // Visual effect for 0.3s
 lastPortalUser = "P1";
 portalTrailTimer = 20;
 } else if (distPortal2 < portalRadius) {
 // TELEPORT from portal 2 to portal 1!
 ballX = player1PortalEntranceX;
 ballY = player1PortalEntranceY;
 
 // OFFENSIVE BOOST - Speed increase based on level
 int portalLevel = player1Abilities.getOrDefault("portal_pong", 1);
 int speedBoost = 2 + Math.min(portalLevel - 1, 2); // 2, 3, or 4 speed boost
 
 // REVERSE horizontal direction to send ball back to opponent (Player 2 side = positive)
 ballVelX = (Math.abs(ballVelX) + speedBoost); // Always positive (toward Player 2)
 ballVelY += (ballVelY > 0 ? 1 : -1) * (speedBoost / 2);
 
 // Level 2+: Angle manipulation toward opponent
 if (portalLevel >= 2) {
 // Calculate angle toward opponent's goal (right side for P1)
 int targetX = 580; // Opponent's paddle area
 int targetY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 
 // Bias the ball velocity toward opponent
 int deltaY = targetY - ballY;
 if (Math.abs(deltaY) > 50) {
 ballVelY += (deltaY > 0 ? 2 : -2);
 }
 }
 
 // Level 3+: Even more aggressive angle
 if (portalLevel >= 3) {
 // Add some randomness for unpredictability
 ballVelY += (int)(Math.random() * 4) - 2;
 ballVelX += 2; // Extra speed toward opponent!
 }
 
 ballTeleportCooldown = true;
 portalBoostTimer = 30; // Visual effect for 0.3s
 lastPortalUser = "P1";
 portalTrailTimer = 20;
 }
 }
 
 // Player 2 portals - BIDIRECTIONAL (ball can enter either portal)
 if (player2PortalEntranceX != null && player2PortalExitX != null && 
 player2PortalPlacementStage == 2 && player2PortalDuration > 0) {
 
 int portalRadius = 30;
 double distPortal1 = Math.sqrt(
 Math.pow(ballX - player2PortalEntranceX, 2) + 
 Math.pow(ballY - player2PortalEntranceY, 2)
 );
 double distPortal2 = Math.sqrt(
 Math.pow(ballX - player2PortalExitX, 2) + 
 Math.pow(ballY - player2PortalExitY, 2)
 );
 
 // Check if ball enters either portal
 if (distPortal1 < portalRadius) {
 // TELEPORT from portal 1 to portal 2!
 ballX = player2PortalExitX;
 ballY = player2PortalExitY;
 
 int portalLevel = player2Abilities.getOrDefault("portal_pong", 1);
 int speedBoost = 2 + Math.min(portalLevel - 1, 2);
 
 // REVERSE horizontal direction to send ball back to opponent (Player 1 side = negative)
 ballVelX = -(Math.abs(ballVelX) + speedBoost); // Always negative (toward Player 1)
 ballVelY += (ballVelY > 0 ? 1 : -1) * (speedBoost / 2);
 
 if (portalLevel >= 2) {
 int targetX = 20; // Opponent's paddle area
 int targetY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 int deltaY = targetY - ballY;
 if (Math.abs(deltaY) > 50) {
 ballVelY += (deltaY > 0 ? 2 : -2);
 }
 }
 
 if (portalLevel >= 3) {
 ballVelY += (int)(Math.random() * 4) - 2;
 ballVelX -= 2; // Extra speed toward opponent!
 }
 
 ballTeleportCooldown = true;
 portalBoostTimer = 30;
 lastPortalUser = "P2";
 portalTrailTimer = 20;
 } else if (distPortal2 < portalRadius) {
 // TELEPORT from portal 2 to portal 1!
 ballX = player2PortalEntranceX;
 ballY = player2PortalEntranceY;
 
 int portalLevel = player2Abilities.getOrDefault("portal_pong", 1);
 int speedBoost = 2 + Math.min(portalLevel - 1, 2);
 
 // REVERSE horizontal direction to send ball back to opponent (Player 1 side = negative)
 ballVelX = -(Math.abs(ballVelX) + speedBoost); // Always negative (toward Player 1)
 ballVelY += (ballVelY > 0 ? 1 : -1) * (speedBoost / 2);
 
 if (portalLevel >= 2) {
 int targetX = 20; // Opponent's paddle area
 int targetY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 int deltaY = targetY - ballY;
 if (Math.abs(deltaY) > 50) {
 ballVelY += (deltaY > 0 ? 2 : -2);
 }
 }
 
 if (portalLevel >= 3) {
 ballVelY += (int)(Math.random() * 4) - 2;
 ballVelX -= 2; // Extra speed toward opponent!
 }
 
 ballTeleportCooldown = true;
 portalBoostTimer = 30;
 lastPortalUser = "P2";
 portalTrailTimer = 20;
 }
 }
 }
 
 // Paddle collisions (ghost ball phases through)
 Rectangle ballRect = new Rectangle(ballX, ballY, 15, 15);
 Rectangle paddle1Rect = new Rectangle(10, paddle1Y, 10, paddleHeight1);
 Rectangle paddle2Rect = new Rectangle(580, paddle2Y, 10, paddleHeight2);
 
 // Shadow Clone collision detection for Player 1 (unless ghost ball is active)
 if (getEffectiveAbilityLevel(1, "shadow_clone") > 0 && ballVelX < 0 && player1GhostEffectTimer == 0 && player1ShadowCollisionCooldown == 0) {
 int numShadows = getNumberOfShadows(1);
 paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int branch = player1AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 2) {
 // Branch 2: Afterimage Defense - check independent shadow
 Rectangle shadowRect = new Rectangle(10, player1IndependentShadowY, 10, paddleHeight1);
 if (ballRect.intersects(shadowRect)) {
 ballSpeed += 0.1; // Increase ball speed on shadow clone hit
 ballVelX = Math.abs(ballVelX) + 3;
 ballX = 20;
 
 // PROPER PONG PHYSICS for shadow
 // ballCenterY already declared, just recalculate
 ballCenterY = ballY + 7;
 int shadowCenterY = player1IndependentShadowY + paddleHeight1 / 2;
 int hitOffset = ballCenterY - shadowCenterY;
 double maxDeflection = 10.0;
 double normalizedOffset = (double) hitOffset / (paddleHeight1 / 2.0);
 normalizedOffset = Math.max(-1.0, Math.min(1.0, normalizedOffset));
 
 // SET vertical velocity based on hit position
 ballVelY = (int)(normalizedOffset * maxDeflection);
 if (Math.abs(hitOffset) < paddleHeight1 * 0.1) ballVelY = (Math.random() > 0.5) ? 1 : -1;
 
 // Add extra spin based on shadow movement
 if (player1ShadowMovingUp) ballVelY -= 2;
 else ballVelY += 2;
 
 // NORMALIZE SPEED: Keep total velocity constant
 double currentSpeed = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 double targetSpeed = Math.abs(ballVelX);
 if (currentSpeed > targetSpeed * 1.2) {
 double ratio = (targetSpeed * 1.2) / currentSpeed;
 ballVelX = (int)(ballVelX * ratio);
 ballVelY = (int)(ballVelY * ratio);
 }
 
 // CRITICAL: Ensure minimum horizontal velocity
 if (Math.abs(ballVelX) < 2) {
 ballVelX = ballVelX >= 0 ? 2 : -2;
 }
 
 // Set cooldown to prevent multiple hits
 player1ShadowCollisionCooldown = shadowCollisionCooldownFrames;
 }
 } else {
 // Branch 1 or no branch: Independent shadows - check each shadow
 for (int i = 0; i < Math.min(numShadows, player1ShadowPositions.size()); i++) {
 int shadowY = player1ShadowPositions.get(i);
 Rectangle shadowRect = new Rectangle(10, shadowY, 10, paddleHeight1);
 if (ballRect.intersects(shadowRect)) {
 ballSpeed += 0.1; // Increase ball speed on shadow clone hit
 ballVelX = Math.abs(ballVelX) + 3;
 ballX = 20;
 
 // PROPER PONG PHYSICS for shadow
 // ballCenterY already declared, just recalculate
 ballCenterY = ballY + 7;
 int shadowCenterY = shadowY + paddleHeight1 / 2;
 int hitOffset = ballCenterY - shadowCenterY;
 double maxDeflection = 10.0;
 double normalizedOffset = (double) hitOffset / (paddleHeight1 / 2.0);
 normalizedOffset = Math.max(-1.0, Math.min(1.0, normalizedOffset));
 
 // SET vertical velocity based on hit position
 ballVelY = (int)(normalizedOffset * maxDeflection);
 if (Math.abs(hitOffset) < paddleHeight1 * 0.1) ballVelY = (Math.random() > 0.5) ? 1 : -1;
 
 // NORMALIZE SPEED: Keep total velocity constant
 double currentSpeed = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 double targetSpeed = Math.abs(ballVelX);
 if (currentSpeed > targetSpeed * 1.2) {
 double ratio = (targetSpeed * 1.2) / currentSpeed;
 ballVelX = (int)(ballVelX * ratio);
 ballVelY = (int)(ballVelY * ratio);
 }
 
 // CRITICAL: Ensure minimum horizontal velocity
 if (Math.abs(ballVelX) < 2) {
 ballVelX = ballVelX >= 0 ? 2 : -2;
 }
 
 // Set cooldown to prevent multiple hits
 player1ShadowCollisionCooldown = shadowCollisionCooldownFrames;
 break;
 }
 }
 }
 }
 
 // Shadow Clone collision detection for Player 2 (unless ghost ball is active)
 if (getEffectiveAbilityLevel(2, "shadow_clone") > 0 && ballVelX > 0 && player2GhostEffectTimer == 0 && player2ShadowCollisionCooldown == 0) {
 int numShadows = getNumberOfShadows(2);
 paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 int branch = player2AbilityBranches.getOrDefault("shadow_clone", 0);
 
 if (branch == 2) {
 // Branch 2: Afterimage Defense - check independent shadow
 Rectangle shadowRect = new Rectangle(580, player2IndependentShadowY, 10, paddleHeight2);
 if (ballRect.intersects(shadowRect)) {
 ballSpeed += 0.1; // Increase ball speed on shadow clone hit
 ballVelX = -(Math.abs(ballVelX) + 3);
 ballX = 565;
 
 // PROPER PONG PHYSICS for shadow
 // ballCenterY already declared, just recalculate
 ballCenterY = ballY + 7;
 int shadowCenterY = player2IndependentShadowY + paddleHeight2 / 2;
 int hitOffset = ballCenterY - shadowCenterY;
 double maxDeflection = 10.0;
 double normalizedOffset = (double) hitOffset / (paddleHeight2 / 2.0);
 normalizedOffset = Math.max(-1.0, Math.min(1.0, normalizedOffset));
 
 // SET vertical velocity based on hit position
 ballVelY = (int)(normalizedOffset * maxDeflection);
 if (Math.abs(hitOffset) < paddleHeight2 * 0.1) ballVelY = (Math.random() > 0.5) ? 1 : -1;
 
 // Add extra spin based on shadow movement
 if (player2ShadowMovingUp) ballVelY -= 2;
 else ballVelY += 2;
 
 // NORMALIZE SPEED: Keep total velocity constant
 double currentSpeed = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 double targetSpeed = Math.abs(ballVelX);
 if (currentSpeed > targetSpeed * 1.2) {
 double ratio = (targetSpeed * 1.2) / currentSpeed;
 ballVelX = (int)(ballVelX * ratio);
 ballVelY = (int)(ballVelY * ratio);
 }
 
 // CRITICAL: Ensure minimum horizontal velocity
 if (Math.abs(ballVelX) < 2) {
 ballVelX = ballVelX >= 0 ? 2 : -2;
 }
 
 // Set cooldown to prevent multiple hits
 player2ShadowCollisionCooldown = shadowCollisionCooldownFrames;
 }
 } else {
 // Branch 1 or no branch: Independent shadows - check each shadow
 for (int i = 0; i < Math.min(numShadows, player2ShadowPositions.size()); i++) {
 int shadowY = player2ShadowPositions.get(i);
 Rectangle shadowRect = new Rectangle(580, shadowY, 10, paddleHeight2);
 if (ballRect.intersects(shadowRect)) {
 ballSpeed += 0.1; // Increase ball speed on shadow clone hit
 ballVelX = -(Math.abs(ballVelX) + 3);
 ballX = 565;
 
 // PROPER PONG PHYSICS for shadow
 // ballCenterY already declared, just recalculate
 ballCenterY = ballY + 7;
 int shadowCenterY = shadowY + paddleHeight2 / 2;
 int hitOffset = ballCenterY - shadowCenterY;
 double maxDeflection = 10.0;
 double normalizedOffset = (double) hitOffset / (paddleHeight2 / 2.0);
 normalizedOffset = Math.max(-1.0, Math.min(1.0, normalizedOffset));
 
 // SET vertical velocity based on hit position
 ballVelY = (int)(normalizedOffset * maxDeflection);
 if (Math.abs(hitOffset) < paddleHeight2 * 0.1) ballVelY = (Math.random() > 0.5) ? 1 : -1;
 
 // NORMALIZE SPEED: Keep total velocity constant
 double currentSpeed = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 double targetSpeed = Math.abs(ballVelX);
 if (currentSpeed > targetSpeed * 1.2) {
 double ratio = (targetSpeed * 1.2) / currentSpeed;
 ballVelX = (int)(ballVelX * ratio);
 ballVelY = (int)(ballVelY * ratio);
 }
 
 // CRITICAL: Ensure minimum horizontal velocity
 if (Math.abs(ballVelX) < 2) {
 ballVelX = ballVelX >= 0 ? 2 : -2;
 }
 
 // Set cooldown to prevent multiple hits
 player2ShadowCollisionCooldown = shadowCollisionCooldownFrames;
 break;
 }
 }
 }
 }

 // BARRIER COLLISION
 // Player 1's barrier
 if (player1BarrierActive) {
 Rectangle barrierRect = new Rectangle(player1BarrierX, player1BarrierY, barrierWidth, barrierHeight);
 if (ballRect.intersects(barrierRect)) {
 int barrierBranch = player1AbilityBranches.getOrDefault("barrier", 0);
 if (barrierBranch == 1 && getEffectiveAbilityLevel(1, "barrier") >= 3) {
 // Mirror Shield - reflect at 2x speed
 ballVelX = -ballVelX * 2;
 ballVelY = (int)(ballVelY * 1.5);
 } else if (barrierBranch == 2 && getEffectiveAbilityLevel(1, "barrier") >= 3) {
 // Absorption Shield - catch ball (simplified: just reverse with delay)
 ballVelX = -ballVelX;
 player1BallAbsorbed = true;
 } else {
 // Base barrier - simple bounce
 ballVelX = -ballVelX;
 }
 player1BarrierActive = false; // Barrier breaks after one hit
 }
 }
 // Player 2's barrier
 if (player2BarrierActive) {
 Rectangle barrierRect = new Rectangle(player2BarrierX, player2BarrierY, barrierWidth, barrierHeight);
 if (ballRect.intersects(barrierRect)) {
 int barrierBranch = player2AbilityBranches.getOrDefault("barrier", 0);
 if (barrierBranch == 1 && getEffectiveAbilityLevel(2, "barrier") >= 3) {
 ballVelX = -ballVelX * 2;
 ballVelY = (int)(ballVelY * 1.5);
 } else if (barrierBranch == 2 && getEffectiveAbilityLevel(2, "barrier") >= 3) {
 ballVelX = -ballVelX;
 player2BallAbsorbed = true;
 } else {
 ballVelX = -ballVelX;
 }
 player2BarrierActive = false;
 }
 }

 // TRAP COLLISION - BUFFED!
 // Player 1's trap
 if (player1TrapActive && !ballFrozenByTrap) {
 Rectangle trapRect = new Rectangle(player1TrapX - trapSize/2, player1TrapY - trapSize/2, trapSize, trapSize);
 if (ballRect.intersects(trapRect)) {
 int trapBranch = player1AbilityBranches.getOrDefault("trap", 0);
 int trapLevel = getEffectiveAbilityLevel(1, "trap");
 if (trapBranch == 1 && trapLevel >= 3) {
 // STICKY TRAP - Ball FREEZES for 2.5s + 3s reversed controls!
 ballFrozenByTrap = true;
 ballFreezeTimer = 250; // 2.5 seconds frozen
 frozenBallX = ballX;
 frozenBallY = ballY;
 player2ReverseEffectTimer = 300; // 3 seconds reversed controls
 player2StickyTimer = stickyDuration;
 } else if (trapBranch == 2 && trapLevel >= 3) {
 // EXPLOSIVE TRAP - Shockwave stuns 1.5s + shrinks 2.5s + 2.5x speed!
 if (player2StunImmunityTimer == 0) player2StunTimer = 150; // 1.5s stun
 player2ShrinkEffectTimer = 250; // 2.5s shrink
 double angle = Math.random() * Math.PI * 2;
 int speed = (int)(Math.sqrt(ballVelX*ballVelX + ballVelY*ballVelY) * 2.5); // 2.5x speed!
 ballVelX = (int)(Math.cos(angle) * speed);
 ballVelY = (int)(Math.sin(angle) * speed);
 if (Math.abs(ballVelX) < 3) ballVelX = ballVelX >= 0 ? 3 : -3;
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 } else {
 // Base trap - stun + redirect ball TOWARD opponent's side!
 if (player2StunImmunityTimer == 0) player2StunTimer = 120; // 1.2s stun
 // Redirect ball toward player 2's side (right)
 int speed = Math.max(5, (int)Math.sqrt(ballVelX*ballVelX + ballVelY*ballVelY));
 ballVelX = Math.abs(speed); // Force ball right toward opponent
 ballVelY = (int)((Math.random() - 0.5) * speed); // Randomize vertical slightly
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 }
 player1TrapActive = false; // Trap triggers once
 }
 }
 // Player 2's trap
 if (player2TrapActive && !ballFrozenByTrap) {
 Rectangle trapRect = new Rectangle(player2TrapX - trapSize/2, player2TrapY - trapSize/2, trapSize, trapSize);
 if (ballRect.intersects(trapRect)) {
 int trapBranch = player2AbilityBranches.getOrDefault("trap", 0);
 int trapLevel = getEffectiveAbilityLevel(2, "trap");
 if (trapBranch == 1 && trapLevel >= 3) {
 // STICKY TRAP - Ball FREEZES for 2.5s + 3s reversed controls!
 ballFrozenByTrap = true;
 ballFreezeTimer = 250; // 2.5 seconds frozen
 frozenBallX = ballX;
 frozenBallY = ballY;
 player1ReverseEffectTimer = 300; // 3 seconds reversed controls
 player1StickyTimer = stickyDuration;
 } else if (trapBranch == 2 && trapLevel >= 3) {
 // EXPLOSIVE TRAP - Shockwave stuns 1.5s + shrinks 2.5s + 2.5x speed!
 if (player1StunImmunityTimer == 0) player1StunTimer = 150; // 1.5s stun
 player1ShrinkEffectTimer = 250; // 2.5s shrink
 double angle = Math.random() * Math.PI * 2;
 int speed = (int)(Math.sqrt(ballVelX*ballVelX + ballVelY*ballVelY) * 2.5);
 ballVelX = (int)(Math.cos(angle) * speed);
 ballVelY = (int)(Math.sin(angle) * speed);
 if (Math.abs(ballVelX) < 3) ballVelX = ballVelX >= 0 ? 3 : -3;
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 } else {
 // Base trap - stun + redirect ball TOWARD opponent's side!
 if (player1StunImmunityTimer == 0) player1StunTimer = 120; // 1.2s stun
 // Redirect ball toward player 1's side (left)
 int speed = Math.max(5, (int)Math.sqrt(ballVelX*ballVelX + ballVelY*ballVelY));
 ballVelX = -Math.abs(speed); // Force ball left toward opponent
 ballVelY = (int)((Math.random() - 0.5) * speed); // Randomize vertical slightly
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 }
 player2TrapActive = false;
 }
 }

 // Handle frozen ball (from Sticky Trap)
 if (ballFrozenByTrap) {
 ballFreezeTimer--;
 ballX = frozenBallX;
 ballY = frozenBallY;
 if (ballFreezeTimer <= 0) {
 ballFrozenByTrap = false;
 // Ball releases at high speed toward the trapper's opponent
 double angle = Math.random() * Math.PI * 2;
 ballVelX = (int)(Math.cos(angle) * 7);
 ballVelY = (int)(Math.sin(angle) * 7);
 }
 }

 // STICKY EFFECT - slows ball
 if (player1StickyTimer > 0 || player2StickyTimer > 0) {
 ballVelX = (int)(ballVelX * 0.3);
 ballVelY = (int)(ballVelY * 0.3);
 // Ensure minimum speed
 if (Math.abs(ballVelX) < 1) ballVelX = ballVelX >= 0 ? 1 : -1;
 }

 // OBSERVATION HAKI - slow-mo is now handled via adjustedVel in ball movement section
 // (no longer modifies ballVelX/ballVelY directly to avoid integer division bug)

 // Player 1 paddle collision (unless player 1 has ghost effect active)
 // ARMAMENT HAKI PHASE - Ball phases through opponent paddle once (from player 2's armament)
 if (ballRect.intersects(paddle1Rect) && ballVelX < 0 && player2HakiPhaseActive) {
 player2HakiPhaseActive = false; // Used up
 // Ball continues without bouncing - unblockable!
 }
 // HOLLOW FORM - Ball phases through paddle once
 else if (ballRect.intersects(paddle1Rect) && ballVelX < 0 && player2HollowActive && !player2HollowPhased) {
 // Ball phases through - mark as used
 player2HollowPhased = true;
 // Ball continues without bouncing
 } else if (ballRect.intersects(paddle1Rect) && ballVelX < 0) {
 if (player1GhostEffectTimer == 0) {
 // Normal collision - increase horizontal speed gradually
 // Increase ball speed by 0.3 on paddle hit
 ballSpeed += 0.3;
 ballNotHitTimer = 0; // Reset the ball-not-hit timer

 // Update velocity components to match new speed while preserving direction
 double magnitude = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 if (magnitude > 0) {
 ballVelX = -(int)((ballVelX / magnitude) * ballSpeed);  // Negate to reverse direction
 ballVelY = (int)((ballVelY / magnitude) * ballSpeed);
 }
 // Prevent perfectly horizontal ball
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 ballX = 20;
 
 
 // DASH SPEED BOOST - If player just dashed, double the ball speed!
 if (player1DashActive > 0) {
 ballVelX *= 2;
 ballVelY *= 2;
 }

 // BANKAI - ZANGETSU SPEED BOOST - Double ball speed during Zangetsu!
 if (player1ZangetsuActive) {
 ballVelX *= 2;
 ballVelY = (int)(ballVelY * 1.5);
 }

 // BANKAI PERMANENT BALL SPEED MULTIPLIER - stacks forever!
 if (player1BallSpeedMultiplier > 1.0) {
 ballVelX = (int)(ballVelX * player1BallSpeedMultiplier);
 ballVelY = (int)(ballVelY * player1BallSpeedMultiplier);
 }

 // ARMAMENT HAKI - Extra hit power
 if (player1ArmamentActive) {
 ballVelX = (int)(ballVelX * 1.5);
 }

 // Trigger lag spike for opponent 6 seconds later (reduced by 0.2s per level)
 if (getEffectiveAbilityLevel(1, "lag_spike") > 0) {
 int lagLevel = player1Abilities.getOrDefault("lag_spike", 0);
 if (lagLevel > 0 && player1LagTriggerTimer == 0) {
 player1LagTriggerTimer = 700 - (lagLevel - 1) * 20; // 7s base - 0.2s per level
 }
 }
 
 // Gravity Hammer - slam ball downward if active AND increase speed!
 if (player1HammerActive) {
 int hammerBranch = player1AbilityBranches.getOrDefault("gravity_hammer", 0);
 int hammerLevel = player1Abilities.getOrDefault("gravity_hammer", 1);
 
 if (hammerBranch == 1 && hammerLevel >= 3) {
 // Branch 1: Thor's Hammer - enhanced slam with increased force
 int slamForce = 8 + (hammerLevel - 2) * 5; // Stronger slam
 ballVelY = Math.abs(ballVelY) + slamForce;
 // INCREASE ball speed for powerful impact!
 ballVelX = (int)(Math.abs(ballVelX) * 1.5); // 50% speed boost!
 // Trigger shockwave (stun) when ball hits bottom or opponent paddle
 player1ShockwaveTimer = 50 + (hammerLevel - 2) * 20; // 0.5s + 0.2s per level
 } else {
 // Base hammer effect - now increases speed!
 ballVelY = Math.abs(ballVelY) + 8 + hammerLevel * 3;
 // INCREASE horizontal speed for impact (no more reducing!)
 ballVelX = (int)(Math.abs(ballVelX) * 1.3); // 30% speed boost
 }
 }
 
 // Power Siphon - NEW BRANCH EFFECTS!
 int siphonBranch = player1AbilityBranches.getOrDefault("power_siphon", 0);
 int siphonLevel = player1Abilities.getOrDefault("power_siphon", 1);
 
 if (siphonBranch == 1 && siphonLevel >= 3) {
 // Branch 1: Soul Reaper - extend copied ability duration on hit
 if (player1CopiedAbilityDuration > 0) {
 player1CopiedAbilityDuration += 50; // +0.5s per hit
 }
 // Also small passive drain
 int drainAmount = 50;
 player2GunTimer = Math.max(0, player2GunTimer - drainAmount);
 player2StealerTimer = Math.max(0, player2StealerTimer - drainAmount);
 player2HammerTimer = Math.max(0, player2HammerTimer - drainAmount);
 player2PortalTimer = Math.max(0, player2PortalTimer - drainAmount);
 player2SiphonTimer = Math.max(0, player2SiphonTimer - drainAmount);
 } else if (siphonBranch == 2 && siphonLevel >= 3) {
 // Branch 2: Overload - gain charge on hit!
 int chargeGain = 20 + (siphonLevel - 2) * 5; // 20 base + 5 per level
 player1OverloadCharge = Math.min(maxOverloadCharge, player1OverloadCharge + chargeGain);
 } else if (player1SiphonActive) {
 // Base power siphon - cooldown drain
 int drainAmount = 50;
 player2GunTimer = Math.max(0, player2GunTimer - drainAmount);
 player2StealerTimer = Math.max(0, player2StealerTimer - drainAmount);
 player2HammerTimer = Math.max(0, player2HammerTimer - drainAmount);
 player2PortalTimer = Math.max(0, player2PortalTimer - drainAmount);
 player2SiphonTimer = Math.max(0, player2SiphonTimer - drainAmount);
 }
 } else {
 // Ball is phasing through - only allow once per activation
 if (!player1GhostHasPhased) {
 player1GhostHasPhased = true;
 // DON'T deactivate ghost immediately - let ball pass through first
 // Ball needs to get past paddle X position before collision can resume
 // Ghost will deactivate naturally when ball is safely past
 
 // VOID PULSE - Ball phased through! Disable opponent abilities
 if (getEffectiveAbilityLevel(2, "ghost_ball") >= 3 && 
 player2AbilityBranches.getOrDefault("ghost_ball", 0) == 1) {
 int ghostLevel = Math.max(player2Abilities.getOrDefault("ghost_ball", 0), 
 player2MimickedAbilities.getOrDefault("ghost_ball", 0));
 int numDisables = ghostLevel < 4 ? 1 : ghostLevel < 6 ? 2 : 3;
 int disableDuration = 200 + (ghostLevel - 3) * 50; // 2s + 0.5s per level
 
 // Disable random abilities
 ArrayList<String> availableAbilities = new ArrayList<>(player1Abilities.keySet());
 // Remove already disabled abilities from the selection pool
 availableAbilities.removeAll(player1DisabledAbilities);
 for (int i = 0; i < numDisables && !availableAbilities.isEmpty(); i++) {
 String abilityToDisable = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player1DisabledAbilities.add(abilityToDisable);
 player1VirusDisableDurations.put(abilityToDisable, disableDuration);
 availableAbilities.remove(abilityToDisable);
 }
 }
 }
 // Let the ball continue moving through - no collision response
 }
 }
 
 // Player 2 paddle collision (unless player 2 has ghost effect active)
 // ARMAMENT HAKI PHASE - Ball phases through opponent paddle once (from player 1's armament)
 if (ballRect.intersects(paddle2Rect) && ballVelX > 0 && player1HakiPhaseActive) {
 player1HakiPhaseActive = false; // Used up
 // Ball continues without bouncing - unblockable!
 }
 // HOLLOW FORM - Ball phases through paddle once
 else if (ballRect.intersects(paddle2Rect) && ballVelX > 0 && player1HollowActive && !player1HollowPhased) {
 // Ball phases through - mark as used
 player1HollowPhased = true;
 // Ball continues without bouncing
 } else if (ballRect.intersects(paddle2Rect) && ballVelX > 0) {
 if (player2GhostEffectTimer == 0) {
 // Increase ball speed by 0.3 on paddle hit
 ballSpeed += 0.3;
 ballNotHitTimer = 0; // Reset the ball-not-hit timer

 // Update velocity components to match new speed while preserving direction
 double magnitude = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 if (magnitude > 0) {
 ballVelX = -(int)((ballVelX / magnitude) * ballSpeed);
 ballVelY = (int)((ballVelY / magnitude) * ballSpeed);
 }
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 ballX = 565;
 
 // DASH SPEED BOOST - If player just dashed, double the ball speed!
 if (player2DashActive > 0) {
 ballVelX *= 2;
 ballVelY *= 2;
 }

 // BANKAI - ZANGETSU SPEED BOOST - Double ball speed during Zangetsu!
 if (player2ZangetsuActive) {
 ballVelX *= 2;
 ballVelY = (int)(ballVelY * 1.5);
 }

 // BANKAI PERMANENT BALL SPEED MULTIPLIER - stacks forever!
 if (player2BallSpeedMultiplier > 1.0) {
 ballVelX = (int)(ballVelX * player2BallSpeedMultiplier);
 ballVelY = (int)(ballVelY * player2BallSpeedMultiplier);
 }

 // ARMAMENT HAKI - Extra hit power
 if (player2ArmamentActive) {
 ballVelX = (int)(ballVelX * 1.5);
 }

 // Trigger lag spike for opponent 6 seconds later (reduced by 0.2s per level)
 if (getEffectiveAbilityLevel(2, "lag_spike") > 0) {
 int lagLevel = player2Abilities.getOrDefault("lag_spike", 0);
 if (lagLevel > 0 && player2LagTriggerTimer == 0) {
 player2LagTriggerTimer = 700 - (lagLevel - 1) * 20; // 7s base - 0.2s per level
 }
 }
 
 // Gravity Hammer - slam ball downward if active AND increase speed!
 if (player2HammerActive) {
 int hammerBranch = player2AbilityBranches.getOrDefault("gravity_hammer", 0);
 int hammerLevel = player2Abilities.getOrDefault("gravity_hammer", 1);
 
 if (hammerBranch == 1 && hammerLevel >= 3) {
 // Branch 1: Thor's Hammer - enhanced slam
 int slamForce = 8 + (hammerLevel - 2) * 5; // Stronger slam
 ballVelY = Math.abs(ballVelY) + slamForce;
 // INCREASE ball speed for powerful impact!
 ballVelX = -(int)(Math.abs(ballVelX) * 1.5); // 50% speed boost!
 // Trigger shockwave
 player2ShockwaveTimer = 50 + (hammerLevel - 2) * 20;
 } else {
 // Base hammer effect - now increases speed!
 ballVelY = Math.abs(ballVelY) + 8 + hammerLevel * 3;
 // INCREASE horizontal speed for impact
 ballVelX = -(int)(Math.abs(ballVelX) * 1.3); // 30% speed boost
 }
 }
 }
 
 // Power Siphon - NEW BRANCH EFFECTS!
 int siphonBranch = player2AbilityBranches.getOrDefault("power_siphon", 0);
 int siphonLevel = player2Abilities.getOrDefault("power_siphon", 1);
 
 if (siphonBranch == 1 && siphonLevel >= 3) {
 // Branch 1: Soul Reaper
 if (player2CopiedAbilityDuration > 0) {
 player2CopiedAbilityDuration += 50;
 }
 int drainAmount = 50;
 player1GunTimer = Math.max(0, player1GunTimer - drainAmount);
 player1StealerTimer = Math.max(0, player1StealerTimer - drainAmount);
 player1HammerTimer = Math.max(0, player1HammerTimer - drainAmount);
 player1PortalTimer = Math.max(0, player1PortalTimer - drainAmount);
 player1SiphonTimer = Math.max(0, player1SiphonTimer - drainAmount);
 } else if (siphonBranch == 2 && siphonLevel >= 3) {
 // Branch 2: Overload - gain charge!
 int chargeGain = 20 + (siphonLevel - 2) * 5;
 player2OverloadCharge = Math.min(maxOverloadCharge, player2OverloadCharge + chargeGain);
 } else if (player2SiphonActive) {
 int drainAmount = 50;
 player1GunTimer = Math.max(0, player1GunTimer - drainAmount);
 player1StealerTimer = Math.max(0, player1StealerTimer - drainAmount);
 player1HammerTimer = Math.max(0, player1HammerTimer - drainAmount);
 player1PortalTimer = Math.max(0, player1PortalTimer - drainAmount);
 player1SiphonTimer = Math.max(0, player1SiphonTimer - drainAmount);
 }
 } else {
 // Ball is phasing through - only allow once per activation
 if (!player2GhostHasPhased) {
 player2GhostHasPhased = true;
 // DON'T deactivate ghost immediately - let ball pass through first
 // Ball needs to get past paddle X position before collision can resume
 // Ghost will deactivate naturally when ball is safely past
 
 // VOID PULSE - Ball phased through! Disable opponent abilities
 if (getEffectiveAbilityLevel(1, "ghost_ball") >= 3 && 
 player1AbilityBranches.getOrDefault("ghost_ball", 0) == 1) {
 int ghostLevel = Math.max(player1Abilities.getOrDefault("ghost_ball", 0), 
 player1MimickedAbilities.getOrDefault("ghost_ball", 0));
 int numDisables = ghostLevel < 4 ? 1 : ghostLevel < 6 ? 2 : 3;
 int disableDuration = 200 + (ghostLevel - 3) * 50; // 2s + 0.5s per level
 
 // Disable random abilities
 ArrayList<String> availableAbilities = new ArrayList<>(player2Abilities.keySet());
 // Remove already disabled abilities from the selection pool
 availableAbilities.removeAll(player2DisabledAbilities);
 for (int i = 0; i < numDisables && !availableAbilities.isEmpty(); i++) {
 String abilityToDisable = availableAbilities.get((int)(Math.random() * availableAbilities.size()));
 player2DisabledAbilities.add(abilityToDisable);
 player2VirusDisableDurations.put(abilityToDisable, disableDuration);
 availableAbilities.remove(abilityToDisable);
 }
 }
 }
 // Let the ball continue moving through - no collision response
 }
 
 // Update lag spike timers
 if (player1LagTriggerTimer > 0) {
 player1LagTriggerTimer--;
 if (player1LagTriggerTimer == 0) {
 // Activate lag effect on opponent
 player2LagEffectTimer = 100; // 1 second duration
 }
 }
 if (player2LagTriggerTimer > 0) {
 player2LagTriggerTimer--;
 if (player2LagTriggerTimer == 0) {
 // Activate lag effect on opponent
 player1LagEffectTimer = 100; // 1 second duration
 }
 }
 
 // Update lag effect timers (visual only)
 if (player1LagEffectTimer > 0) {
 player1LagEffectTimer--;
 }
 if (player2LagEffectTimer > 0) {
 player2LagEffectTimer--;
 }
 
 // Reverse controls ability - Branch system
 if (getEffectiveAbilityLevel(1, "reverse_controls") > 0) {
 int reverseBranch = player1AbilityBranches.getOrDefault("reverse_controls", 0);
 
 if (reverseBranch == 1) {
 // CHAOS ENGINE - Random rotating debuffs
 player1ChaosTimer++;
 if (player1ChaosTimer >= chaosCooldown) {
 int reverseLevel = Math.max(player1Abilities.getOrDefault("reverse_controls", 0), 
 player1MimickedAbilities.getOrDefault("reverse_controls", 0));
 player2ChaosEffectTimer = chaosEffectDuration + (reverseLevel - 3) * 40; // 4s + 0.4s per level
 player2CurrentDebuff = 1 + (int)(Math.random() * 3); // Random 1-3
 player1ChaosTimer = 0;
 }
 } else if (reverseBranch == 2) {
 // PUPPET MASTER - Takes control of opponent paddle
 player1PuppetTimer++;
 if (player1PuppetTimer >= puppetCooldown) {
 int reverseLevel = Math.max(player1Abilities.getOrDefault("reverse_controls", 0), 
 player1MimickedAbilities.getOrDefault("reverse_controls", 0));
 player2PuppetEffectTimer = puppetEffectDuration + (reverseLevel - 3) * 30; // 2s + 0.3s per level
 player1PuppetTimer = 0;
 }
 } else {
 // No branch - original reverse controls
 player1ReverseTimer++;
 if (player1ReverseTimer >= reverseCooldown) {
 int reverseLevel = player1Abilities.get("reverse_controls");
 player2ReverseEffectTimer = reverseEffectDuration + (reverseLevel - 1) * 20;
 player1ReverseTimer = 0;
 }
 }
 }
 
 if (getEffectiveAbilityLevel(2, "reverse_controls") > 0) {
 int reverseBranch = player2AbilityBranches.getOrDefault("reverse_controls", 0);
 
 if (reverseBranch == 1) {
 // CHAOS ENGINE - Random rotating debuffs
 player2ChaosTimer++;
 if (player2ChaosTimer >= chaosCooldown) {
 int reverseLevel = Math.max(player2Abilities.getOrDefault("reverse_controls", 0), 
 player2MimickedAbilities.getOrDefault("reverse_controls", 0));
 player1ChaosEffectTimer = chaosEffectDuration + (reverseLevel - 3) * 40;
 player1CurrentDebuff = 1 + (int)(Math.random() * 3);
 player2ChaosTimer = 0;
 }
 } else if (reverseBranch == 2) {
 // PUPPET MASTER - Takes control of opponent paddle
 player2PuppetTimer++;
 if (player2PuppetTimer >= puppetCooldown) {
 int reverseLevel = Math.max(player2Abilities.getOrDefault("reverse_controls", 0), 
 player2MimickedAbilities.getOrDefault("reverse_controls", 0));
 player1PuppetEffectTimer = puppetEffectDuration + (reverseLevel - 3) * 30;
 player2PuppetTimer = 0;
 }
 } else {
 // No branch - original reverse controls
 player2ReverseTimer++;
 if (player2ReverseTimer >= reverseCooldown) {
 int reverseLevel = player2Abilities.get("reverse_controls");
 player1ReverseEffectTimer = reverseEffectDuration + (reverseLevel - 1) * 20;
 player2ReverseTimer = 0;
 }
 }
 }
 
 // Update effect timers
 if (player1ReverseEffectTimer > 0) player1ReverseEffectTimer--;
 if (player2ReverseEffectTimer > 0) player2ReverseEffectTimer--;
 if (player1ChaosEffectTimer > 0) player1ChaosEffectTimer--;
 if (player2ChaosEffectTimer > 0) player2ChaosEffectTimer--;
 if (player1PuppetEffectTimer > 0) player1PuppetEffectTimer--;
 if (player2PuppetEffectTimer > 0) player2PuppetEffectTimer--;
 
 // JOSHUA ability - activates every 3 seconds for massive boost
 if (getEffectiveAbilityLevel(1, "joshua") > 0) {
 // Only increment timer when NOT active
 if (!player1JoshuaActive) {
 player1JoshuaTimer++;
 if (player1JoshuaTimer >= joshuaCooldown) {
 int joshuaLevel = player1Abilities.get("joshua");
 player1JoshuaActive = true;
 player1JoshuaDuration = 1000 + (joshuaLevel - 1) * 200; // 10s base + 2s per level
 player1JoshuaTimer = 0;
 }
 }
 }
 if (getEffectiveAbilityLevel(2, "joshua") > 0) {
 // Only increment timer when NOT active
 if (!player2JoshuaActive) {
 player2JoshuaTimer++;
 if (player2JoshuaTimer >= joshuaCooldown) {
 int joshuaLevel = player2Abilities.get("joshua");
 player2JoshuaActive = true;
 player2JoshuaDuration = 1000 + (joshuaLevel - 1) * 200; // 10s base + 2s per level
 player2JoshuaTimer = 0;
 }
 }
 }
 
 // Update JOSHUA duration
 if (player1JoshuaDuration > 0) {
 player1JoshuaDuration--;
 if (player1JoshuaDuration == 0) player1JoshuaActive = false;
 }
 if (player2JoshuaDuration > 0) {
 player2JoshuaDuration--;
 if (player2JoshuaDuration == 0) player2JoshuaActive = false;
 }
 
 // JAISAN ability - even more OP than JOSHUA
 if (getEffectiveAbilityLevel(1, "jaisan") > 0) {
 // Only increment timer when NOT active
 if (!player1JaisanActive) {
 player1JaisanTimer++;
 if (player1JaisanTimer >= jaisanCooldown) {
 int jaisanLevel = player1Abilities.get("jaisan");
 player1JaisanActive = true;
 player1JaisanDuration = 1000 + (jaisanLevel - 1) * 300; // 10s base + 3s per level (more than JOSHUA!)
 player1JaisanTimer = 0;
 }
 }
 }
 if (getEffectiveAbilityLevel(2, "jaisan") > 0) {
 // Only increment timer when NOT active
 if (!player2JaisanActive) {
 player2JaisanTimer++;
 if (player2JaisanTimer >= jaisanCooldown) {
 int jaisanLevel = player2Abilities.get("jaisan");
 player2JaisanActive = true;
 player2JaisanDuration = 1000 + (jaisanLevel - 1) * 300; // 10s base + 3s per level
 player2JaisanTimer = 0;
 }
 }
 }
 
 // Update JAISAN duration
 if (player1JaisanDuration > 0) {
 player1JaisanDuration--;
 if (player1JaisanDuration == 0) player1JaisanActive = false;
 }
 if (player2JaisanDuration > 0) {
 player2JaisanDuration--;
 if (player2JaisanDuration == 0) player2JaisanActive = false;
 }
 
 // JOSHUA auto-deflect - ball bounces back automatically
 // BUT: If opponent has JAISAN active, JOSHUA is suppressed!
 if (player1JoshuaActive && ballX < 30 && ballVelX < 0 && !player2JaisanActive) {
 ballVelX = Math.abs(ballVelX) * 2; // Double speed on deflect
 ballX = 30;
 }
 if (player2JoshuaActive && ballX > 555 && ballVelX > 0 && !player1JaisanActive) {
 ballVelX = -Math.abs(ballVelX) * 2; // Double speed on deflect
 ballX = 555;
 }
 
 // JAISAN ULTIMATE auto-deflect - ALWAYS wins against JOSHUA!
 if (player1JaisanActive && ballX < 30 && ballVelX < 0) {
 ballVelX = Math.abs(ballVelX) * 3; // TRIPLE speed on deflect (better than JOSHUA!)
 ballX = 30;
 // If opponent has JOSHUA, make their JOSHUA look pathetic
 if (player2JoshuaActive) {
 player2JoshuaDuration = Math.min(player2JoshuaDuration, 50); // Reduce JOSHUA to 0.5s
 }
 }
 if (player2JaisanActive && ballX > 555 && ballVelX > 0) {
 ballVelX = -Math.abs(ballVelX) * 3; // TRIPLE speed on deflect
 ballX = 555;
 // If opponent has JOSHUA, make their JOSHUA look pathetic
 if (player1JoshuaActive) {
 player1JoshuaDuration = Math.min(player1JoshuaDuration, 50); // Reduce JOSHUA to 0.5s
 }
 }
 
 // Blind ability - makes opponent's paddle nearly invisible
 if (getEffectiveAbilityLevel(1, "blind") > 0) {
 player1BlindTimer++;
 if (player1BlindTimer >= blindCooldown) {
 int blindLevel = player1Abilities.get("blind");
 // Apply blind (immunity will reduce duration later)
 player2BlindEffectTimer = 300 + (blindLevel - 1) * 30; // 3s + 0.3s per level
 player1BlindTimer = 0;
 }
 }
 if (getEffectiveAbilityLevel(2, "blind") > 0) {
 player2BlindTimer++;
 if (player2BlindTimer >= blindCooldown) {
 int blindLevel = player2Abilities.get("blind");
 // Apply blind (immunity will reduce duration later)
 player1BlindEffectTimer = 300 + (blindLevel - 1) * 30; // 3s + 0.3s per level
 player2BlindTimer = 0;
 }
 }
 // Update blind timers
 if (player1BlindEffectTimer > 0) {
 player1BlindEffectTimer--;
 }
 if (player2BlindEffectTimer > 0) {
 player2BlindEffectTimer--;
 }
 
 // Shrink Opponent ability - makes enemy paddle tiny
 if (getEffectiveAbilityLevel(1, "shrink_opponent") > 0) {
 player1ShrinkTimer++;
 if (player1ShrinkTimer >= shrinkCooldown) {
 int shrinkLevel = player1Abilities.get("shrink_opponent");
 // Apply shrink (immunity will reduce duration later via natural decay)
 player2ShrinkEffectTimer = 150 + (shrinkLevel - 1) * 30; // 1.5s + 0.3s per level
 player1ShrinkTimer = 0;
 }
 }
 if (getEffectiveAbilityLevel(2, "shrink_opponent") > 0) {
 player2ShrinkTimer++;
 if (player2ShrinkTimer >= shrinkCooldown) {
 int shrinkLevel = player2Abilities.get("shrink_opponent");
 // Apply shrink (immunity will reduce duration later via natural decay)
 player1ShrinkEffectTimer = 150 + (shrinkLevel - 1) * 30; // 1.5s + 0.3s per level
 player2ShrinkTimer = 0;
 }
 }
 // Update shrink timers
 if (player1ShrinkEffectTimer > 0) {
 player1ShrinkEffectTimer--;
 }
 if (player2ShrinkEffectTimer > 0) {
 player2ShrinkEffectTimer--;
 }
 
 // Ghost Ball ability - Branch system
 // ALL branches keep the original phasing ability!
 if (getEffectiveAbilityLevel(1, "ghost_ball") > 0) {
 int ghostBranch = player1AbilityBranches.getOrDefault("ghost_ball", 0);
 
 // Original ghost ball phasing ability (works for all branches)
 player1GhostTimer++;
 
 // Calculate cooldown based on branch
 int effectiveCooldown = ghostCooldown;
 if (ghostBranch == 1) {
 // VOID PULSE - Cooldown reduces by 0.2s (-20 frames) per level
 int ghostLevel = Math.max(player1Abilities.getOrDefault("ghost_ball", 0), 
 player1MimickedAbilities.getOrDefault("ghost_ball", 0));
 effectiveCooldown = ghostCooldown - (ghostLevel - 3) * 20; // -0.2s per level starting from level 3
 effectiveCooldown = Math.max(200, effectiveCooldown); // Minimum 2 second cooldown
 }
 
 if (player1GhostTimer >= effectiveCooldown) {
 int ghostLevel = Math.max(player1Abilities.getOrDefault("ghost_ball", 0), 
 player1MimickedAbilities.getOrDefault("ghost_ball", 0));
 if (player2Abilities.getOrDefault("immunity", 0) == 0) {
 player2GhostEffectTimer = 250 + (ghostLevel - 1) * 20; // 2.5s + 0.2s per level
 player2GhostHasPhased = false; // Reset phase flag for new activation
 }
 player1GhostTimer = 0;
 }
 
 // Branch-specific additions
 if (ghostBranch == 1) {
 // VOID PULSE - When ball phases through, disables opponent abilities
 // Effect is triggered when phase actually happens (checked in paddle collision)
 } else if (ghostBranch == 2) {
 // REALITY BREAK - Distorts game physics periodically
 player1RealityTimer++;
 if (player1RealityTimer >= realityCooldown && !realityBreakActive) {
 int ghostLevel = Math.max(player1Abilities.getOrDefault("ghost_ball", 0), 
 player1MimickedAbilities.getOrDefault("ghost_ball", 0));
 realityBreakActive = true;
 realityBreakDuration = maxRealityBreakDuration + (ghostLevel - 3) * 50; // 3s + 0.5s per level
 realityBreakEffect = (int)(Math.random() * 3); // 0=speed flux, 1=hitbox shift, 2=powerup flicker
 player1RealityTimer = 0;
 }
 }
 }
 
 if (getEffectiveAbilityLevel(2, "ghost_ball") > 0) {
 int ghostBranch = player2AbilityBranches.getOrDefault("ghost_ball", 0);
 
 // Original ghost ball phasing ability (works for all branches)
 player2GhostTimer++;
 
 // Calculate cooldown based on branch
 int effectiveCooldown = ghostCooldown;
 if (ghostBranch == 1) {
 // VOID PULSE - Cooldown reduces by 0.2s (-20 frames) per level
 int ghostLevel = Math.max(player2Abilities.getOrDefault("ghost_ball", 0), 
 player2MimickedAbilities.getOrDefault("ghost_ball", 0));
 effectiveCooldown = ghostCooldown - (ghostLevel - 3) * 20; // -0.2s per level starting from level 3
 effectiveCooldown = Math.max(200, effectiveCooldown); // Minimum 2 second cooldown
 }
 
 if (player2GhostTimer >= effectiveCooldown) {
 int ghostLevel = Math.max(player2Abilities.getOrDefault("ghost_ball", 0), 
 player2MimickedAbilities.getOrDefault("ghost_ball", 0));
 if (player1Abilities.getOrDefault("immunity", 0) == 0) {
 player1GhostEffectTimer = 250 + (ghostLevel - 1) * 20;
 player1GhostHasPhased = false; // Reset phase flag for new activation
 }
 player2GhostTimer = 0;
 }
 
 // Branch-specific additions
 if (ghostBranch == 1) {
 // VOID PULSE - When ball phases through, disables opponent abilities
 // Effect is triggered when phase actually happens (checked in paddle collision)
 } else if (ghostBranch == 2) {
 // REALITY BREAK - Distorts game physics periodically
 player2RealityTimer++;
 if (player2RealityTimer >= realityCooldown && !realityBreakActive) {
 int ghostLevel = Math.max(player2Abilities.getOrDefault("ghost_ball", 0), 
 player2MimickedAbilities.getOrDefault("ghost_ball", 0));
 realityBreakActive = true;
 realityBreakDuration = maxRealityBreakDuration + (ghostLevel - 3) * 50;
 realityBreakEffect = (int)(Math.random() * 3);
 player2RealityTimer = 0;
 }
 }
 }
 
 // Update Reality Break distortions
 if (realityBreakActive) {
 realityBreakDuration--;
 if (realityBreakDuration <= 0) {
 realityBreakActive = false;
 } else {
 // Apply reality distortions based on effect type
 if (realityBreakEffect == 0) {
 // Speed fluctuation - ball speed varies wildly
 if (Math.random() < 0.1) { // Every ~10 frames change speed
 double speedMult = 0.5 + Math.random() * 1.5; // 50%-200%
 int newVelX = (int)(ballVelX * speedMult);
 int newVelY = (int)(ballVelY * speedMult);
 // Ensure velocities never become 0 - maintain direction and minimum speed
 if (newVelX == 0) newVelX = (ballVelX > 0) ? 2 : -2;
 if (newVelY == 0) newVelY = (ballVelY > 0) ? 2 : -2;
 ballVelX = newVelX;
 ballVelY = newVelY;
 }
 } else if (realityBreakEffect == 1) {
 // Hitbox shifts handled in collision detection
 // (paddles appear to be Ã‚Â±20 pixels from actual position)
 } else if (realityBreakEffect == 2) {
 // Power-up flickering handled in rendering
 // (power-ups randomly invisible)
 }
 }
 }
 
 if (player1GhostEffectTimer > 0) {
 player1GhostEffectTimer--;
 // If ball has phased and is now past the paddle, deactivate ghost
 if (player1GhostHasPhased && ballX > 30) {
 player1GhostEffectTimer = 0;
 }
 }
 if (player2GhostEffectTimer > 0) {
 player2GhostEffectTimer--;
 // If ball has phased and is now past the paddle, deactivate ghost
 if (player2GhostHasPhased && ballX < 570) {
 player2GhostEffectTimer = 0;
 }
 }
 
 // OBSERVATION HAKI - Auto-block: teleport paddle to block ball once per activation
 // Player 1 auto-block (when ball is about to score on left side)
 if (player1ObservationActive && !player1ObsAutoBlockUsed && ballX < 30 && ballVelX < 0) {
 int ballCenterYNow = ballY + 7;
 int paddleH = getPaddleHeight(1, shrinkPaddlesActive);
 paddle1Y = ballCenterYNow - paddleH / 2; // Teleport paddle to ball
 paddle1Y = Math.max(0, Math.min(400 - paddleH, paddle1Y)); // Clamp to bounds
 player1ObsAutoBlockUsed = true;
 }
 // Player 2 auto-block (when ball is about to score on right side)
 if (player2ObservationActive && !player2ObsAutoBlockUsed && ballX > 570 && ballVelX > 0) {
 int ballCenterYNow = ballY + 7;
 int paddleH = getPaddleHeight(2, shrinkPaddlesActive);
 paddle2Y = ballCenterYNow - paddleH / 2; // Teleport paddle to ball
 paddle2Y = Math.max(0, Math.min(400 - paddleH, paddle2Y)); // Clamp to bounds
 player2ObsAutoBlockUsed = true;
 }

 // Scoring
 if (ballX < 0 || ballX > 600) {
 // Reset map modifiers on score
 shrinkPaddlesActive = false;
 shrinkPaddlesDuration = 0;
 
 if (ballX < 0) {
 // Check shield for player 1
 if (getEffectiveAbilityLevel(1, "shield") > 0) {
 int shieldLevel = player1Abilities.get("shield");
 player1Abilities.put("shield", shieldLevel - 1);
 if (player1Abilities.get("shield") == 0) player1Abilities.remove("shield");
 JOptionPane.showMessageDialog(null, " Shield activated! Goal blocked! ", "Shield!", JOptionPane.INFORMATION_MESSAGE);
 ballX = 250; ballY = 150; ballVelX = 3; ballVelY = 3;
 initialBallVelX = ballVelX; initialBallVelY = ballVelY; // Store initial velocity
 dangerZoneTime = 0;
 repaint();
 return;
 }

 int pointsGained = 1 + player2Abilities.getOrDefault("double_points", 0);
 
 // Combo Master (double_points branch 2) - combo system
 if (getEffectiveAbilityLevel(2, "double_points") >= 3 && player2AbilityBranches.getOrDefault("double_points", 0) == 2) {
 player2ComboCount++;
 int comboBonus = Math.min(player2ComboCount / 3, 5); // +1 point per 3 consecutive scores, max +5
 pointsGained += comboBonus;
 if (comboBonus > 0) {
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + " COMBO x" + player2ComboCount + "! +" + comboBonus + " bonus! ", 
 "Combo Master!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 } else {
 player2ComboCount = 0; // Reset combo if not using Combo Master
 }
 
 // Point Leech (double_points branch 1) - steal points from opponent
 if (getEffectiveAbilityLevel(2, "double_points") >= 3 && player2AbilityBranches.getOrDefault("double_points", 0) == 1) {
 int leechLevel = player2Abilities.get("double_points") - 2; // Level 3 = 1, Level 4 = 2, etc.
 int stolenPoints = Math.min(leechLevel, scorePlayer1); // Can't steal more than opponent has
 if (stolenPoints > 0) {
 scorePlayer1 -= stolenPoints;
 pointsGained += stolenPoints;
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " " + player2Name + " Point Leech! Stole " + stolenPoints + " point(s) from Player 1! ", 
 "Point Leech!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 
 scorePlayer2 += pointsGained;
 totalPointsThisLevel2 += pointsGained;
 noScoreTimer = 0; // Reset no-score timer
 ballNotHitTimer = 0; // Reset ball-not-hit timer

 // STORY MODE - Check for boss victory (player defeat)
 if (storyModeActive) {
 storyBossScore = scorePlayer2;
 storyPlayerScore = scorePlayer1;
 int scoreNeeded = storyLevelScoreToWin[storyModeLevel - 1];
 if (storyBossScore >= scoreNeeded) {
 handleStoryModeDefeat();
 return;
 }
 }

 // Reset opponent's combo
 player1ComboCount = 0;
 
 // JOSHUA auto-level: Level up every 10 points scored
 if (getEffectiveAbilityLevel(2, "joshua") > 0) {
 player2JoshuaPoints += pointsGained;
 if (player2JoshuaPoints >= 10) {
 int currentJoshuaLevel = player2Abilities.get("joshua");
 
 // Check if JOSHUA is at level 2 and hasn't branched yet
 if (currentJoshuaLevel == 2 && player2AbilityBranches.getOrDefault("joshua", 0) == 0) {
 if (singlePlayer) {
 // AI randomly chooses branch
 int branch = (Math.random() < 0.5) ? 1 : 2;
 player2AbilityBranches.put("joshua", branch);
 player2Abilities.put("joshua", 3);
 JOptionPane.showMessageDialog(null, 
 " AI: JOSHUA " + getBranchName("joshua", branch) + " ", 
 "AI JOSHUA Evolution!", 
 JOptionPane.INFORMATION_MESSAGE);
 } else {
 // Player 2 branch choice
 String[] branchOptions = {getBranch1Description("joshua"), getBranch2Description("joshua")};
 int branchChoice = JOptionPane.showOptionDialog(null,
 "Player 2 - JOSHUA EVOLUTION!\n\n" +
 "Your JOSHUA ability has reached Level 3!\n" +
 "Choose an evolution path:\n\n" +
 " BRANCH 1: " + getBranchName("joshua", 1) + "\n" +
 getBranch1Description("joshua") + "\n\n" +
 " BRANCH 2: " + getBranchName("joshua", 2) + "\n" +
 getBranch2Description("joshua"),
 " JOSHUA EVOLUTION ",
 JOptionPane.DEFAULT_OPTION,
 JOptionPane.QUESTION_MESSAGE,
 null,
 new String[]{" Choose Branch 1", " Choose Branch 2"},
 null);
 
 if (branchChoice >= 0) {
 int selectedBranch = branchChoice + 1;
 player2AbilityBranches.put("joshua", selectedBranch);
 player2Abilities.put("joshua", 3);
 JOptionPane.showMessageDialog(null,
 " Player 2: JOSHUA evolved into " + 
 getBranchName("joshua", selectedBranch) + "! ",
 "JOSHUA Evolution Complete!",
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 } else {
 // Normal level up
 player2Abilities.put("joshua", currentJoshuaLevel + 1);
 // No popup - silently level up JOSHUA to avoid interrupting gameplay
 }
 player2JoshuaPoints = 0;
 }
 }
 
 // Auto-level all evolved abilities every 20 points
 checkEvolvedAbilityLevelUps(2, pointsGained);
 
 // Check for underdog bonus for Player 1 (who is now DOWN)
 int scoreDiff = scorePlayer2 - scorePlayer1;
 // Triggers at 10, 20, 30, 40, etc. (every 10 points behind)
 if (scoreDiff >= 10) {
 // Calculate the current threshold (rounds down to nearest 10)
 int threshold = (scoreDiff / 10) * 10;
 // Only trigger if we've reached a new 10-point milestone
 if (threshold > player1LastUnderdogTrigger) {
 player1LastUnderdogTrigger = threshold;
 offerAbilityChoice(1, 2); // Give Level 2 ability to underdog
 JOptionPane.showMessageDialog(null, " Player 1 is " + scoreDiff + " points behind!\nUnderdog Bonus: Level 2 ability granted! ", "Underdog Bonus!", JOptionPane.INFORMATION_MESSAGE);
 }
 }
 // Reset opponent's underdog tracking if they're no longer behind
 if (scoreDiff < 10) {
 player1LastUnderdogTrigger = 0;
 }
 
 // Level up: 1st score, then 2 more, then 3 more, etc.
 if (totalPointsThisLevel2 >= pointsToNextLevel2) {
 level2++;
 totalPointsThisLevel2 = 0;
 pointsToNextLevel2++; // Next level needs 1 more point
 offerAbilityChoice(2, 1);
 }
 } else {
 // Check shield for player 2
 if (getEffectiveAbilityLevel(2, "shield") > 0) {
 int shieldLevel = player2Abilities.get("shield");
 player2Abilities.put("shield", shieldLevel - 1);
 if (player2Abilities.get("shield") == 0) player2Abilities.remove("shield");
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, " " + player2Name + " shield activated! Goal blocked! ", "Shield!", JOptionPane.INFORMATION_MESSAGE);
 ballX = 250; ballY = 150; ballVelX = -3; ballVelY = 3;
 initialBallVelX = ballVelX; initialBallVelY = ballVelY; // Store initial velocity
 dangerZoneTime = 0;
 repaint();
 return;
 }

 int pointsGained = 1 + player1Abilities.getOrDefault("double_points", 0);
 
 // Combo Master (double_points branch 2) - combo system
 if (getEffectiveAbilityLevel(1, "double_points") >= 3 && player1AbilityBranches.getOrDefault("double_points", 0) == 2) {
 player1ComboCount++;
 int comboBonus = Math.min(player1ComboCount / 3, 5); // +1 point per 3 consecutive scores, max +5
 pointsGained += comboBonus;
 if (comboBonus > 0) {
 JOptionPane.showMessageDialog(null, 
 " COMBO x" + player1ComboCount + "! +" + comboBonus + " bonus! ", 
 "Combo Master!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 } else {
 player1ComboCount = 0; // Reset combo if not using Combo Master
 }
 
 // Point Leech (double_points branch 1) - steal points from opponent
 if (getEffectiveAbilityLevel(1, "double_points") >= 3 && player1AbilityBranches.getOrDefault("double_points", 0) == 1) {
 int leechLevel = player1Abilities.get("double_points") - 2; // Level 3 = 1, Level 4 = 2, etc.
 int stolenPoints = Math.min(leechLevel, scorePlayer2); // Can't steal more than opponent has
 if (stolenPoints > 0) {
 scorePlayer2 -= stolenPoints;
 pointsGained += stolenPoints;
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, 
 " Point Leech! Stole " + stolenPoints + " point(s) from " + player2Name + "! ", 
 "Point Leech!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 
 scorePlayer1 += pointsGained;
 totalPointsThisLevel1 += pointsGained;
 noScoreTimer = 0; // Reset no-score timer
 ballNotHitTimer = 0; // Reset ball-not-hit timer

 // STORY MODE - Check for player victory!
 if (storyModeActive) {
 storyPlayerScore = scorePlayer1;
 storyBossScore = scorePlayer2;
 int scoreNeeded = storyLevelScoreToWin[storyModeLevel - 1];
 if (storyPlayerScore >= scoreNeeded) {
 handleStoryModeVictory();
 return;
 }
 }

 // Reset opponent's combo
 player2ComboCount = 0;
 
 // JOSHUA auto-level: Level up every 10 points scored
 if (getEffectiveAbilityLevel(1, "joshua") > 0) {
 player1JoshuaPoints += pointsGained;
 if (player1JoshuaPoints >= 10) {
 int currentJoshuaLevel = player1Abilities.get("joshua");
 
 // Check if JOSHUA is at level 2 and hasn't branched yet
 if (currentJoshuaLevel == 2 && player1AbilityBranches.getOrDefault("joshua", 0) == 0) {
 // Offer branch choice
 String[] branchOptions = {getBranch1Description("joshua"), getBranch2Description("joshua")};
 int branchChoice = JOptionPane.showOptionDialog(null,
 "Player 1 - JOSHUA EVOLUTION!\n\n" +
 "Your JOSHUA ability has reached Level 3!\n" +
 "Choose an evolution path:\n\n" +
 " BRANCH 1: " + getBranchName("joshua", 1) + "\n" +
 getBranch1Description("joshua") + "\n\n" +
 " BRANCH 2: " + getBranchName("joshua", 2) + "\n" +
 getBranch2Description("joshua"),
 " JOSHUA EVOLUTION ",
 JOptionPane.DEFAULT_OPTION,
 JOptionPane.QUESTION_MESSAGE,
 null,
 new String[]{" Choose Branch 1", " Choose Branch 2"},
 null);
 
 if (branchChoice >= 0) {
 int selectedBranch = branchChoice + 1;
 player1AbilityBranches.put("joshua", selectedBranch);
 player1Abilities.put("joshua", 3);
 JOptionPane.showMessageDialog(null,
 " Player 1: JOSHUA evolved into " + 
 getBranchName("joshua", selectedBranch) + "! ",
 "JOSHUA Evolution Complete!",
 JOptionPane.INFORMATION_MESSAGE);
 }
 } else {
 // Normal level up
 player1Abilities.put("joshua", currentJoshuaLevel + 1);
 // No popup - silently level up JOSHUA to avoid interrupting gameplay
 }
 player1JoshuaPoints = 0;
 }
 }
 
 // Auto-level all evolved abilities every 20 points
 checkEvolvedAbilityLevelUps(1, pointsGained);
 
 // Check for underdog bonus for Player 2 (who is now DOWN 10+ points)
 int scoreDiff = scorePlayer1 - scorePlayer2;
 // Triggers at 10, 20, 30, 40, etc. (every 10 points behind)
 if (scoreDiff >= 10) {
 // Calculate the current threshold (rounds down to nearest 10)
 int threshold = (scoreDiff / 10) * 10;
 // Only trigger if we've reached a new 10-point milestone
 if (threshold > player2LastUnderdogTrigger) {
 player2LastUnderdogTrigger = threshold;
 offerAbilityChoice(2, 2); // Give Level 2 ability to underdog
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, " " + player2Name + " is " + scoreDiff + " points behind!\nUnderdog Bonus: Level 2 ability granted! ", "Underdog Bonus!", JOptionPane.INFORMATION_MESSAGE);
 }
 }
 // Reset opponent's underdog tracking if they're no longer behind
 if (scoreDiff < 10) {
 player2LastUnderdogTrigger = 0;
 }
 
 // Level up: 1st score, then 2 more, then 3 more, etc.
 if (totalPointsThisLevel1 >= pointsToNextLevel1) {
 level1++;
 totalPointsThisLevel1 = 0;
 pointsToNextLevel1++; // Next level needs 1 more point
 if (singlePlayer) {
 ballSpeedMultiplier += 0.02;
 aiSpeedMultiplier += 0.01;
 powerUpSpawnInterval = Math.max(200, powerUpSpawnInterval - 30);
 }
 offerAbilityChoice(1, 1);
 }
 }
 // Reset haki effects on score
 player1HakiPhaseActive = false; player2HakiPhaseActive = false;
 player1ObsAutoBlockUsed = false; player2ObsAutoBlockUsed = false;
 player1ObsSlowFactor = 1.0; player2ObsSlowFactor = 1.0;
 player1ArmamentActive = false; player2ArmamentActive = false;
 player1ObservationActive = false; player2ObservationActive = false;

 ballX = 250;
 ballY = 150;
 ballSpeed = 3.0; // Reset speed on new rally
 // Winner of the point gets the ball
 ballVelX = ballX < 0 ? -3 : 3; // If ball went left, Player 2 won, so ball goes left
 ballVelY = 3;
 initialBallVelX = ballVelX; initialBallVelY = ballVelY; // Store initial velocity
 initialBallSpeed = 3.0; // Store initial speed for fireball restoration
 dangerZoneTime = 0;
 }
 
 // Power-up spawning
 powerUpTimer++;
 if (powerUpTimer > powerUpSpawnInterval) {
 int x = 100 + (int)(Math.random() * 400);
 int y = 50 + (int)(Math.random() * 300);
 double rand = Math.random();
 String type;
 if (rand < 0.000001) type = "jackpot";
 else if (rand < 0.001) type = "multiball";
 else if (rand < 0.05) type = "shrek";
 else if (rand < 0.10) type = "giant";
 else if (rand < 0.15) type = "shrink";
 else if (rand < 0.25) {
 String[] mapPowerUps = {"dangerzone", "gravity", "invisiblewalls", "shrinkpaddles", "centerwall"};
 type = mapPowerUps[(int)(Math.random() * mapPowerUps.length)];
 } else if (rand < 0.40) type = "speed";
 else if (rand < 0.60) type = "slow";
 else if (rand < 0.80) type = "paddle";
 else type = "reverse";
 powerUps.add(new PowerUp(x, y, type));
 powerUpTimer = 0;
 }
 
 // Power-up collection
 for (int i = powerUps.size() - 1; i >= 0; i--) {
 PowerUp powerUp = powerUps.get(i);
 Rectangle powerRect = new Rectangle(powerUp.x, powerUp.y, 20, 20);
 if (ballRect.intersects(powerRect)) {
 boolean hasPowerBoost = (ballVelX < 0 && getEffectiveAbilityLevel(1, "power_boost") > 0) || 
 (ballVelX > 0 && getEffectiveAbilityLevel(2, "power_boost") > 0);
 int boostMultiplier = hasPowerBoost ? 2 : 1;
 
 switch (powerUp.type) {
 case "paddle":
 int moveAmount = 20 * boostMultiplier;
 if (ballVelX < 0 && paddle1Y > 0) paddle1Y = Math.max(0, paddle1Y - moveAmount);
 else if (paddle2Y > 0) paddle2Y = Math.max(0, paddle2Y - moveAmount);
 break;
 case "slow":
 int slowAmount = 2 * boostMultiplier;
 ballVelX = ballVelX > 0 ? Math.max(2, ballVelX - slowAmount) : Math.min(-2, ballVelX + slowAmount);
 break;
 case "speed":
 int speedAmount = 2 * boostMultiplier;
 ballVelX = ballVelX > 0 ? ballVelX + speedAmount : ballVelX - speedAmount;
 break;
 case "reverse":
 ballVelX *= -1;
 break;
 case "giant":
 int giantOffset = 50 * boostMultiplier;
 if (ballVelX < 0) paddle1Y = Math.max(0, Math.min(340, ballY - giantOffset));
 else paddle2Y = Math.max(0, Math.min(340, ballY - giantOffset));
 break;
 case "shrink":
 int shrinkAmount = 40 * boostMultiplier;
 if (ballVelX < 0 && paddle2Y < 340) paddle2Y = Math.min(340, paddle2Y + shrinkAmount);
 else if (paddle1Y < 340) paddle1Y = Math.min(340, paddle1Y + shrinkAmount);
 break;
 case "multiball":
 for (int j = 0; j < 3; j++) ballVelY += (Math.random() - 0.5) * 2;
 multiballActive = true;
 multiballDuration = 0;
 break;
 case "jackpot":
 if (Math.random() < 0.5) {
 scorePlayer1 += 50;
 totalPointsThisLevel1 += 50;
 JOptionPane.showMessageDialog(null, " JACKPOT! \nPlayer 1 wins 50 points!", "MEGA JACKPOT!", JOptionPane.INFORMATION_MESSAGE);
 } else {
 scorePlayer2 += 50;
 totalPointsThisLevel2 += 50;
 String player2Name = singlePlayer ? "AI" : "Player 2";
 JOptionPane.showMessageDialog(null, " JACKPOT! \n" + player2Name + " wins 50 points!", "MEGA JACKPOT!", JOptionPane.INFORMATION_MESSAGE);
 }
 break;
 case "dangerzone":
 dangerZoneActive = true;
 dangerZoneDuration = 0;
 dangerZoneTime = 0;
 break;
 case "teleport":
 // Randomly teleport the ball to a new position with random velocity
 ballX = 100 + (int)(Math.random() * 400);
 ballY = 50 + (int)(Math.random() * 300);
 ballVelX = (Math.random() < 0.5 ? -1 : 1) * (3 + (int)(Math.random() * 3));
 ballVelY = (int)(Math.random() * 6) - 3;
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;
 teleportActive = true;
 teleportDuration = 0;
 break;
 case "fireball":
 // Ball becomes a fireball - speeds up significantly and ignores paddle size
 // Store current speed as the initial speed for this fireball effect
 initialBallSpeed = ballSpeed * 1.8;
 ballSpeed = initialBallSpeed;

 // Update velocity components to match new speed while preserving direction
 double magnitude = Math.sqrt(ballVelX * ballVelX + ballVelY * ballVelY);
 if (magnitude > 0) {
 ballVelX = (int)((ballVelX / magnitude) * ballSpeed);
 ballVelY = (int)((ballVelY / magnitude) * ballSpeed);
 }
 if (ballVelY == 0) ballVelY = (Math.random() > 0.5) ? 2 : -2;

 fireballActive = true;
 fireballDuration = 0;
 break;
 case "zigzag":
 // Ball starts changing vertical direction randomly
 zigzagActive = true;
 zigzagDuration = 0;
 zigzagTimer = 0;
 break;
 case "split":
 // Ball splits into multiple balls temporarily
 splitActive = true;
 splitDuration = 0;
 break;
 case "mirror":
 // MIRROR - Flips the entire game horizontally!
 mirrorActive = true;
 mirrorDuration = 0;
 // The graphics transform will handle the visual flip
 // No popup - visual effect is enough indication
 break;
 case "invisiblewalls":
 invisibleWallsActive = true;
 invisibleWallsDuration = 0;
 invisibleWallY = 150 + (int)(Math.random() * 100);
 break;
 case "shrinkpaddles":
 shrinkPaddlesActive = true;
 shrinkPaddlesDuration = 0;
 break;
 case "centerwall":
 centerWallActive = true;
 centerWallDuration = 0;
 centerWallGapY = 100 + (int)(Math.random() * 200);
 break;
 case "jumpscare":
 jumpscareActive = true;
 jumpscareDuration = 0;
 jumpscareTimer = 0;
 break;
 case "shrek":
 shrekBallActive = true;
 shrekBallDuration = 0;
 shrekJumpscareTimer = 0;
 break;
 }
 powerUps.remove(i);
 
 boolean hasMapPowerUp = false;
 for (PowerUp p : powerUps) {
 if (p.type.equals("dangerzone") || p.type.equals("teleport") || 
 p.type.equals("invisiblewalls") || p.type.equals("shrinkpaddles") || 
 p.type.equals("centerwall") || p.type.equals("fireball") ||
 p.type.equals("zigzag") || p.type.equals("split")) {
 hasMapPowerUp = true;
 break;
 }
 }
 if (!hasMapPowerUp) spawnMapPowerUp();
 }
 }

 // Zigzag spawning - separate timer, 4% chance every 2 seconds
 zigzagSpawnTimer++;
 if (zigzagSpawnTimer >= 200) { // 200 frames = 2 seconds
 zigzagSpawnTimer = 0;
 // Check if zigzag already exists
 boolean hasZigzag = false;
 for (PowerUp p : powerUps) {
 if (p.type.equals("zigzag")) {
 hasZigzag = true;
 break;
 }
 }
 // Only 4% chance to spawn zigzag
 if (!hasZigzag && Math.random() < 0.04) {
 int x = 100 + (int)(Math.random() * 400);
 int y = 50 + (int)(Math.random() * 300);
 powerUps.add(new PowerUp(x, y, "zigzag"));
 }
 }

 // 2-MINUTE NO-SCORE UPGRADE - Both players get ability upgrade if no one scores for 2 minutes
 if (noScoreTimer >= noScoreThreshold) {
 noScoreTimer = 0; // Reset timer

 // Pause game briefly and give both players an upgrade
 isPaused = true;
 JOptionPane.showMessageDialog(null,
 " 2 MINUTES WITHOUT SCORING! \n\nBoth players receive an ability upgrade!",
 "STALEMATE BONUS!",
 JOptionPane.INFORMATION_MESSAGE);

 // Give Player 1 an upgrade
 offerAbilityChoice(1, 1);

 // Give Player 2 an upgrade (or AI)
 if (singlePlayer) {
 // AI gets a random ability upgrade
 ArrayList<String> aiAbilities = new ArrayList<>(player2Abilities.keySet());
 if (!aiAbilities.isEmpty()) {
 String randomAbility = aiAbilities.get((int)(Math.random() * aiAbilities.size()));
 int currentLevel = player2Abilities.get(randomAbility);
 player2Abilities.put(randomAbility, currentLevel + 1);
 JOptionPane.showMessageDialog(null,
 "AI upgraded " + getAbilityShortName(randomAbility) + " to Level " + (currentLevel + 1) + "!",
 "AI Upgrade",
 JOptionPane.INFORMATION_MESSAGE);
 } else {
 // AI has no abilities, give them a random one
 String randomAbility = allAbilities[(int)(Math.random() * allAbilities.length)];
 player2Abilities.put(randomAbility, 1);
 JOptionPane.showMessageDialog(null,
 "AI gained " + getAbilityShortName(randomAbility) + "!",
 "AI New Ability",
 JOptionPane.INFORMATION_MESSAGE);
 }
 } else {
 // Give Player 2 an upgrade
 offerAbilityChoice(2, 1);
 }

 isPaused = false;
 }

 repaint();
 }

 public void keyPressed(KeyEvent e) {
 int code = e.getKeyCode();
 if (showingMainMenu) return; // Ignore keys while menu is showing
 if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
 isPaused = !isPaused;
 repaint();
 return;
 }
 if (code == KeyEvent.VK_M) {
 handleCheatMenu();
 return;
 }
 // Toggle Learning AI with L key (only in single player)
 if (code == KeyEvent.VK_L && singlePlayer) {
 learningAIEnabled = !learningAIEnabled;
 if (learningAIEnabled) {
 // Reset learning data when enabled
 playerPositionHistory.clear();
 playerResponseTimes.clear();
 playerAggressiveness.clear();
 learningDataPoints = 0;
 learningFrameCounter = 0;
 JOptionPane.showMessageDialog(this, 
 "Learning AI ENABLED!\n\nThe AI will now study your playstyle and adapt to mimic you.\n" +
 "It learns your:\n" +
 " Preferred positioning\n" +
 " Reaction speed\n" +
 " Aggressive vs defensive play\n\n" +
 "Data points collected: 0/" + maxLearningData,
 "Learning AI", JOptionPane.INFORMATION_MESSAGE);
 } else {
 JOptionPane.showMessageDialog(this, 
 "Learning AI DISABLED\n\nAI returned to standard behavior.\n" +
 "Total data points collected: " + learningDataPoints,
 "Learning AI", JOptionPane.INFORMATION_MESSAGE);
 }
 return;
 }
 // Save game with F5
 if (code == KeyEvent.VK_F5) {
 saveGame();
 return;
 }
 // Load game with F9
 if (code == KeyEvent.VK_F9) {
 loadGame();
 return;
 }
 // Delete game with F8
 if (code == KeyEvent.VK_F8) {
 deleteGame();
 return;
 }
 if (isPaused) return;
 
 // CUSTOM KEYBINDS - Player 1 movement
 if (code == player1UpKey) up1 = true;
 if (code == player1DownKey) down1 = true;
 
 // CUSTOM KEYBINDS - Player 2 movement
 if (code == player2UpKey) up2 = true;
 if (code == player2DownKey) down2 = true;
 
 // Sonic Dash activation - Player 1
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "speed_boost") >= 3 && 
 player1AbilityBranches.getOrDefault("speed_boost", 0) == 1 && player1DashTimer >= getDashCooldown(1)) {
 // PREDICT where ball will be when it reaches paddle X position
 int predictedY = predictBallYPosition(ballX, ballY, ballVelX, ballVelY, 20); // Player 1 paddle at x=20
 
 // Dash to predicted position with extended range
 int paddleHeight1 = getPaddleHeight(1, shrinkPaddlesActive);
 int targetY = predictedY - paddleHeight1 / 2; // Center paddle on predicted position
 paddle1Y = Math.max(0, Math.min(400 - paddleHeight1, targetY));
 
 // Visual effect
 player1DashActive = dashDuration;
 player1DashTimer = 0;
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "speed_boost") >= 3 && 
 player2AbilityBranches.getOrDefault("speed_boost", 0) == 1 && player2DashTimer >= getDashCooldown(2)) {
 // PREDICT where ball will be when it reaches paddle X position
 int predictedY = predictBallYPosition(ballX, ballY, ballVelX, ballVelY, 580); // Player 2 paddle at x=580
 
 // Dash to predicted position with extended range
 int paddleHeight2 = getPaddleHeight(2, shrinkPaddlesActive);
 int targetY = predictedY - paddleHeight2 / 2; // Center paddle on predicted position
 paddle2Y = Math.max(0, Math.min(400 - paddleHeight2, targetY));
 
 // Visual effect
 player2DashActive = dashDuration;
 player2DashTimer = 0;
 }
 
 // New Ability Activations with Q (Player 1) and / (Player 2)
 // Gravity Hammer
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "gravity_hammer") > 0 && player1HammerTimer >= hammerCooldown) {
 int hammerBranch = player1AbilityBranches.getOrDefault("gravity_hammer", 0);
 int hammerLevel = player1Abilities.getOrDefault("gravity_hammer", 1);
 
 if (hammerBranch == 2) {
 // Branch 2: Gravity Well - create area pull effect
 player1GravityWellActive = true;
 int baseDuration = 200; // 2 seconds base
 player1GravityWellDuration = baseDuration + (hammerLevel >= 3 ? (hammerLevel - 2) * 50 : 0);
 player1GravityWellX = 100; // Center of player 1's side
 player1GravityWellY = paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2;
 // Reduced cooldown for Branch 2
 int branch2Cooldown = 600 - (hammerLevel >= 3 ? (hammerLevel - 2) * 30 : 0); // 6s - 0.3s per level
 player1HammerTimer = -branch2Cooldown + hammerCooldown; // Offset to achieve desired cooldown
 } else {
 // Branch 1 or base: Thor's Hammer - slam effect
 player1HammerActive = true;
 player1HammerDuration = hammerDuration;
 // Reduced cooldown for Branch 1
 if (hammerBranch == 1 && hammerLevel >= 3) {
 int reducedCooldown = hammerCooldown - (hammerLevel - 2) * 20; // 4s - 0.2s per level
 player1HammerTimer = -reducedCooldown + hammerCooldown;
 } else {
 player1HammerTimer = 0;
 }
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "gravity_hammer") > 0 && player2HammerTimer >= hammerCooldown) {
 int hammerBranch = player2AbilityBranches.getOrDefault("gravity_hammer", 0);
 int hammerLevel = player2Abilities.getOrDefault("gravity_hammer", 1);
 
 if (hammerBranch == 2) {
 // Branch 2: Gravity Well - create area pull effect
 player2GravityWellActive = true;
 int baseDuration = 200; // 2 seconds base
 player2GravityWellDuration = baseDuration + (hammerLevel >= 3 ? (hammerLevel - 2) * 50 : 0);
 player2GravityWellX = 500; // Center of player 2's side
 player2GravityWellY = paddle2Y + getPaddleHeight(2, shrinkPaddlesActive) / 2;
 // Reduced cooldown for Branch 2
 int branch2Cooldown = 600 - (hammerLevel >= 3 ? (hammerLevel - 2) * 30 : 0); // 6s - 0.3s per level
 player2HammerTimer = -branch2Cooldown + hammerCooldown;
 } else {
 // Branch 1 or base: Thor's Hammer - slam effect
 player2HammerActive = true;
 player2HammerDuration = hammerDuration;
 // Reduced cooldown for Branch 1
 if (hammerBranch == 1 && hammerLevel >= 3) {
 int reducedCooldown = hammerCooldown - (hammerLevel - 2) * 20; // 4s - 0.2s per level
 player2HammerTimer = -reducedCooldown + hammerCooldown;
 } else {
 player2HammerTimer = 0;
 }
 }
 }
 
 // Magnet Ball - Branch 2: Force Field toggle
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "magnet_ball") > 0) {
 int magnetBranch = player1AbilityBranches.getOrDefault("magnet_ball", 0);
 if (magnetBranch == 2) {
 // Toggle between attract and repel modes
 player1MagnetRepelMode = !player1MagnetRepelMode;
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "magnet_ball") > 0) {
 int magnetBranch = player2AbilityBranches.getOrDefault("magnet_ball", 0);
 if (magnetBranch == 2) {
 // Toggle between attract and repel modes
 player2MagnetRepelMode = !player2MagnetRepelMode;
 }
 }
 
 // Portal Pong - Strategic Placement System
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "portal_pong") > 0) {
 int portalLevel = player1Abilities.getOrDefault("portal_pong", 1);
 int portalBranch = player1AbilityBranches.getOrDefault("portal_pong", 0);
 
 // Branch-specific cooldowns
 int cooldown;
 if (portalBranch == 1 && portalLevel >= 3) {
 // Wormhole Master: faster cooldown
 cooldown = 800 - (portalLevel - 2) * 50; // 8s - 0.5s per level
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: longer cooldown
 cooldown = 1000 - (portalLevel - 2) * 40; // 10s - 0.4s per level
 } else {
 cooldown = 800 - (Math.min(portalLevel - 1, 2) * 100); // Base: 8s, 7s, 6s
 }
 
 if (player1PortalTimer >= cooldown) {
 if (player1PortalPlacementStage == 0) {
 // Place entrance portal
 if (portalBranch == 1 && portalLevel >= 3) {
 // Wormhole Master: can place anywhere (at ball's current X position for offensive plays)
 player1PortalEntranceX = Math.max(50, Math.min(550, ballX));
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM placement!
 player1PortalEntranceX = 50 + (int)(Math.random() * 500); // Random X: 50-550
 } else {
 player1PortalEntranceX = 50; // Fixed near paddle
 }
 
 if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM Y placement!
 player1PortalEntranceY = 50 + (int)(Math.random() * 300); // Random Y: 50-350
 } else {
 player1PortalEntranceY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 }
 player1PortalPlacementStage = 1;
 } 
 else if (player1PortalPlacementStage == 1) {
 // Place exit portal
 if (portalBranch == 1 && portalLevel >= 3) {
 // Wormhole Master: place at ball's position again (or paddle for control)
 player1PortalExitX = Math.max(50, Math.min(550, ballX));
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM placement!
 player1PortalExitX = 50 + (int)(Math.random() * 500);
 } else {
 player1PortalExitX = 50;
 }
 
 if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM Y placement!
 player1PortalExitY = 50 + (int)(Math.random() * 300);
 } else {
 player1PortalExitY = paddle1Y + (getPaddleHeight(1, shrinkPaddlesActive) / 2);
 }
 player1PortalPlacementStage = 2; // Complete placement for all branches
 
 // Activate portals with branch-specific durations
 if (portalBranch == 1 && portalLevel >= 3) {
 player1PortalDuration = 500 + (portalLevel - 2) * 200; // 5s + 2s per level
 } else if (portalBranch == 2 && portalLevel >= 3) {
 player1PortalDuration = 600 + (portalLevel - 2) * 100; // 6s + 1s per level (moving portals)
 } else {
 player1PortalDuration = 600;
 }
 player1PortalTimer = 0;
 
 // Branch 2: Start portal movement timer
 if (portalBranch == 2 && portalLevel >= 3) {
 player1PortalMoveTimer = 200; // Move every 2 seconds
 }
 }
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "portal_pong") > 0) {
 int portalLevel = player2Abilities.getOrDefault("portal_pong", 1);
 int portalBranch = player2AbilityBranches.getOrDefault("portal_pong", 0);
 
 int cooldown;
 if (portalBranch == 1 && portalLevel >= 3) {
 cooldown = 800 - (portalLevel - 2) * 50;
 } else if (portalBranch == 2 && portalLevel >= 3) {
 cooldown = 1000 - (portalLevel - 2) * 40;
 } else {
 cooldown = 800 - (Math.min(portalLevel - 1, 2) * 100);
 }
 
 if (player2PortalTimer >= cooldown) {
 if (player2PortalPlacementStage == 0) {
 if (portalBranch == 1 && portalLevel >= 3) {
 player2PortalEntranceX = Math.max(50, Math.min(550, ballX));
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM placement!
 player2PortalEntranceX = 50 + (int)(Math.random() * 500);
 } else {
 player2PortalEntranceX = 550;
 }
 
 if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM Y placement!
 player2PortalEntranceY = 50 + (int)(Math.random() * 300);
 } else {
 player2PortalEntranceY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 }
 player2PortalPlacementStage = 1;
 } 
 else if (player2PortalPlacementStage == 1) {
 if (portalBranch == 1 && portalLevel >= 3) {
 player2PortalExitX = Math.max(50, Math.min(550, ballX));
 } else if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM placement!
 player2PortalExitX = 50 + (int)(Math.random() * 500);
 } else {
 player2PortalExitX = 550;
 }
 
 if (portalBranch == 2 && portalLevel >= 3) {
 // Dimensional Rift: RANDOM Y placement!
 player2PortalExitY = 50 + (int)(Math.random() * 300);
 } else {
 player2PortalExitY = paddle2Y + (getPaddleHeight(2, shrinkPaddlesActive) / 2);
 }
 player2PortalPlacementStage = 2; // Complete placement for all branches
 
 if (portalBranch == 1 && portalLevel >= 3) {
 player2PortalDuration = 500 + (portalLevel - 2) * 200;
 } else if (portalBranch == 2 && portalLevel >= 3) {
 player2PortalDuration = 600 + (portalLevel - 2) * 100; // 6s + 1s per level
 } else {
 player2PortalDuration = 600;
 }
 player2PortalTimer = 0;
 
 if (portalBranch == 2 && portalLevel >= 3) {
 player2PortalMoveTimer = 200;
 }
 }
 }
 }
 
 // Power Siphon
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "power_siphon") > 0) {
 int siphonBranch = player1AbilityBranches.getOrDefault("power_siphon", 0);
 int siphonLevel = player1Abilities.getOrDefault("power_siphon", 1);
 
 if (siphonBranch == 1 && siphonLevel >= 3) {
 // Branch 1: Soul Reaper - copy random opponent ability
 int soulReapCooldown = 1200 - (siphonLevel - 2) * 80; // 12s - 0.8s per level
 if (player1SoulReapTimer >= soulReapCooldown) {
 // Pick random ability from opponent
 ArrayList<String> opponentAbilities = new ArrayList<>(player2Abilities.keySet());
 opponentAbilities.remove("jaisan");
 opponentAbilities.remove("joshua");
 if (!opponentAbilities.isEmpty()) {
 String randomAbility = opponentAbilities.get((int)(Math.random() * opponentAbilities.size()));
 player1CopiedAbility = randomAbility;
 player1CopiedAbilityDuration = 500 + (siphonLevel - 2) * 50; // 5s + 0.5s per level
 }
 player1SoulReapTimer = 0;
 }
 } else if (siphonBranch == 2 && siphonLevel >= 3) {
 // Branch 2: Overload - discharge charge (no cooldown!)
 if (player1OverloadCharge >= 25) { // Minimum 25 charge to use
 int chargeLevel = player1OverloadCharge;
 
 // Apply effects based on charge
 if (chargeLevel >= 25) {
 player2StunTimer = 100; // 1s stun
 ballVelX = (int)(ballVelX * 1.5); // +50% speed
 ballVelY = (int)(ballVelY * 1.5);
 }
 if (chargeLevel >= 50) {
 player2StunTimer = 200; // 2s stun
 ballVelX = (int)(ballVelX * 1.33); // +100% total
 ballVelY = (int)(ballVelY * 1.33);
 player2OverloadDisableTimer = 200; // 2s ability disable
 }
 if (chargeLevel >= 75) {
 player2StunTimer = 300; // 3s stun
 ballVelX = (int)(ballVelX * 1.25); // +150% total
 ballVelY = (int)(ballVelY * 1.25);
 player2OverloadDisableTimer = 400; // 4s ability disable
 }
 if (chargeLevel >= 100) {
 // MEGA OVERLOAD!
 player2StunTimer = 400; // 4s stun
 ballVelX = (int)(ballVelX * 1.33); // +200% total
 ballVelY = (int)(ballVelY * 1.33);
 player2OverloadDisableTimer = 600; // 6s ability disable
 player2OverloadShrinkTimer = 300; // 3s paddle shrink
 }
 
 // Consume all charge
 player1OverloadCharge = 0;
 }
 } else if (player1SiphonTimer >= siphonCooldown) {
 // Base power siphon
 player1SiphonActive = true;
 player1SiphonDuration = siphonDuration;
 player1SiphonTimer = 0;
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "power_siphon") > 0) {
 int siphonBranch = player2AbilityBranches.getOrDefault("power_siphon", 0);
 int siphonLevel = player2Abilities.getOrDefault("power_siphon", 1);
 
 if (siphonBranch == 1 && siphonLevel >= 3) {
 // Branch 1: Soul Reaper
 int soulReapCooldown = 1200 - (siphonLevel - 2) * 80;
 if (player2SoulReapTimer >= soulReapCooldown) {
 ArrayList<String> opponentAbilities = new ArrayList<>(player1Abilities.keySet());
 opponentAbilities.remove("jaisan");
 opponentAbilities.remove("joshua");
 if (!opponentAbilities.isEmpty()) {
 String randomAbility = opponentAbilities.get((int)(Math.random() * opponentAbilities.size()));
 player2CopiedAbility = randomAbility;
 player2CopiedAbilityDuration = 500 + (siphonLevel - 2) * 50;
 }
 player2SoulReapTimer = 0;
 }
 } else if (siphonBranch == 2 && siphonLevel >= 3) {
 // Branch 2: Overload
 if (player2OverloadCharge >= 25) {
 int chargeLevel = player2OverloadCharge;
 
 if (chargeLevel >= 25) {
 player1StunTimer = 100;
 ballVelX = (int)(ballVelX * 1.5);
 ballVelY = (int)(ballVelY * 1.5);
 }
 if (chargeLevel >= 50) {
 player1StunTimer = 200;
 ballVelX = (int)(ballVelX * 1.33);
 ballVelY = (int)(ballVelY * 1.33);
 player1OverloadDisableTimer = 200;
 }
 if (chargeLevel >= 75) {
 player1StunTimer = 300;
 ballVelX = (int)(ballVelX * 1.25);
 ballVelY = (int)(ballVelY * 1.25);
 player1OverloadDisableTimer = 400;
 }
 if (chargeLevel >= 100) {
 player1StunTimer = 400;
 ballVelX = (int)(ballVelX * 1.33);
 ballVelY = (int)(ballVelY * 1.33);
 player1OverloadDisableTimer = 600;
 player1OverloadShrinkTimer = 300;
 }
 
 player2OverloadCharge = 0;
 }
 } else if (player2SiphonTimer >= siphonCooldown) {
 player2SiphonActive = true;
 player2SiphonDuration = siphonDuration;
 player2SiphonTimer = 0;
 }
 }
 
 // Time Loop
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "time_loop") > 0 && !gameStateHistory.isEmpty()) {
 int timeLoopLevel = getEffectiveAbilityLevel(1, "time_loop");
 int timeLoopBranch = player1AbilityBranches.getOrDefault("time_loop", 0);
 
 int currentCooldown;
 if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
 // Branch 1: Chronos Rewind - faster cooldown
 currentCooldown = 500 - (timeLoopLevel - 2) * 50; // 15s - 0.5s per level
 } else if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
 // Branch 2: Temporal Echo - longer cooldown
 currentCooldown = 500 - (timeLoopLevel - 2) * 80; // 25s - 0.8s per level
 } else {
 currentCooldown = timeLoopCooldown - (timeLoopLevel - 1) * 30; // Base: 20s - 0.3s per level
 }
 
 if (player1TimeLoopTimer >= currentCooldown) {
 GameState state = gameStateHistory.get(0);
 
 if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
 // Branch 2: Temporal Echo - create ghost ball instead of full rewind
 temporalEchoActive = true;
 echoGhostBallX = state.ballX;
 echoGhostBallY = state.ballY;
 echoGhostBallVelX = state.ballVelX;
 echoGhostBallVelY = state.ballVelY;
 temporalEchoDuration = 300 + (timeLoopLevel - 2) * 50; // 3s + 0.5s per level
 // Don't rewind actual ball - just create echo
 } else {
 // Branch 1 or base: Normal rewind
 ballX = state.ballX;
 ballY = state.ballY;
 ballVelX = state.ballVelX;
 ballVelY = state.ballVelY;
 
 if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
 // Branch 1: Chronos Rewind - enhanced effects
 // Double rewind duration
 int historyIndex = Math.min((timeLoopLevel - 2) * 20 + 30, gameStateHistory.size() - 1); // Go further back
 GameState deeperState = gameStateHistory.get(Math.max(0, historyIndex));
 ballX = deeperState.ballX;
 ballY = deeperState.ballY;
 ballVelX = deeperState.ballVelX;
 ballVelY = deeperState.ballVelY;
 
 // Also restore 10% paddle position
 paddle1Y = (int)(paddle1Y * 0.9 + state.paddle1Y * 0.1);
 
 // Stronger slow-mo: 2s base + 0.5s per level, ball at 20% speed
 player1TimeLoopSlowMoTimer = 200 + (timeLoopLevel - 2) * 50;
 } else {
 paddle1Y = state.paddle1Y;
 paddle2Y = state.paddle2Y;
 scorePlayer1 = state.player1Score;
 scorePlayer2 = state.player2Score;
 player1TimeLoopSlowMoTimer = timeLoopSlowMoDuration + (timeLoopLevel - 1) * 30;
 }
 }
 player1TimeLoopTimer = 0;
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "time_loop") > 0 && !gameStateHistory.isEmpty()) {
 int timeLoopLevel = getEffectiveAbilityLevel(2, "time_loop");
 int timeLoopBranch = player2AbilityBranches.getOrDefault("time_loop", 0);
 
 int currentCooldown;
 if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
 currentCooldown = 1500 - (timeLoopLevel - 2) * 50;
 } else if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
 currentCooldown = 2500 - (timeLoopLevel - 2) * 80;
 } else {
 currentCooldown = timeLoopCooldown - (timeLoopLevel - 1) * 30;
 }
 
 if (player2TimeLoopTimer >= currentCooldown) {
 GameState state = gameStateHistory.get(0);
 
 if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
 // Branch 2: Temporal Echo
 temporalEchoActive = true;
 echoGhostBallX = state.ballX;
 echoGhostBallY = state.ballY;
 echoGhostBallVelX = state.ballVelX;
 echoGhostBallVelY = state.ballVelY;
 temporalEchoDuration = 300 + (timeLoopLevel - 2) * 50;
 } else {
 ballX = state.ballX;
 ballY = state.ballY;
 ballVelX = state.ballVelX;
 ballVelY = state.ballVelY;
 
 if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
 // Branch 1: Chronos Rewind
 int historyIndex = Math.min((timeLoopLevel - 2) * 20 + 30, gameStateHistory.size() - 1);
 GameState deeperState = gameStateHistory.get(Math.max(0, historyIndex));
 ballX = deeperState.ballX;
 ballY = deeperState.ballY;
 ballVelX = deeperState.ballVelX;
 ballVelY = deeperState.ballVelY;
 paddle2Y = (int)(paddle2Y * 0.9 + state.paddle2Y * 0.1);
 player2TimeLoopSlowMoTimer = 200 + (timeLoopLevel - 2) * 50;
 } else {
 paddle1Y = state.paddle1Y;
 paddle2Y = state.paddle2Y;
 scorePlayer1 = state.player1Score;
 scorePlayer2 = state.player2Score;
 player2TimeLoopSlowMoTimer = timeLoopSlowMoDuration + (timeLoopLevel - 1) * 30;
 }
 }
 player2TimeLoopTimer = 0;
 }
 }

 // HAKI ability activation
 {
 int hakiLevel1 = getEffectiveAbilityLevel(1, "haki");
 int hakiBranch1 = player1AbilityBranches.getOrDefault("haki", 0);
 int p1HakiCD = (hakiBranch1 == 1) ? hakiArmamentCooldown - (Math.max(0, hakiLevel1 - 2) * 30) :
                (hakiBranch1 == 2) ? hakiObservationCooldown - (Math.max(0, hakiLevel1 - 2) * 40) :
                hakiBaseCooldown;
 if (code == player1AbilityKey && hakiLevel1 > 0 && player1HakiTimer >= p1HakiCD) {

 if (hakiBranch1 == 1 && hakiLevel1 >= 3) {
 // Branch 1: Armament Haki - stun + black paddle + phasing ball + debuff immunity
 player2StunTimer = hakiStunDuration + (hakiLevel1 - 1) * 20; // KEEPS base stun
 player1ArmamentActive = true;
 player1ArmamentDuration = armamentBaseDuration + (hakiLevel1 - 2) * 50;
 player1HakiPhaseActive = true; // Next hit phases through opponent paddle
 } else if (hakiBranch1 == 2 && hakiLevel1 >= 3) {
 // Branch 2: Observation Haki - stun + opponent slow-mo + trajectory + auto-block
 player2StunTimer = hakiStunDuration + (hakiLevel1 - 1) * 20; // KEEPS base stun
 player1ObservationActive = true;
 player1ObservationDuration = observationBaseDuration + (hakiLevel1 - 2) * 30;
 player2ObsSlowFactor = 0.5; // Opponent paddle moves at 50% speed
 player1ObsAutoBlockUsed = false; // Reset auto-block for this activation
 } else {
 // Base: Conqueror's Haki - stun opponent
 player2StunTimer = hakiStunDuration + (hakiLevel1 - 1) * 20;
 }
 player1HakiTimer = 0;
 }
 }
 {
 int hakiLevel2 = getEffectiveAbilityLevel(2, "haki");
 int hakiBranch2 = player2AbilityBranches.getOrDefault("haki", 0);
 int p2HakiCD = (hakiBranch2 == 1) ? hakiArmamentCooldown - (Math.max(0, hakiLevel2 - 2) * 30) :
                (hakiBranch2 == 2) ? hakiObservationCooldown - (Math.max(0, hakiLevel2 - 2) * 40) :
                hakiBaseCooldown;
 if (code == player2AbilityKey && hakiLevel2 > 0 && player2HakiTimer >= p2HakiCD) {

 if (hakiBranch2 == 1 && hakiLevel2 >= 3) {
 // Branch 1: Armament Haki - stun + black paddle + phasing ball + debuff immunity
 player1StunTimer = hakiStunDuration + (hakiLevel2 - 1) * 20; // KEEPS base stun
 player2ArmamentActive = true;
 player2ArmamentDuration = armamentBaseDuration + (hakiLevel2 - 2) * 50;
 player2HakiPhaseActive = true; // Next hit phases through opponent paddle
 } else if (hakiBranch2 == 2 && hakiLevel2 >= 3) {
 // Branch 2: Observation Haki - stun + opponent slow-mo + trajectory + auto-block
 player1StunTimer = hakiStunDuration + (hakiLevel2 - 1) * 20; // KEEPS base stun
 player2ObservationActive = true;
 player2ObservationDuration = observationBaseDuration + (hakiLevel2 - 2) * 30;
 player1ObsSlowFactor = 0.5; // Opponent paddle moves at 50% speed
 player2ObsAutoBlockUsed = false; // Reset auto-block for this activation
 } else {
 // Base: Conqueror's Haki - stun opponent
 player1StunTimer = hakiStunDuration + (hakiLevel2 - 1) * 20;
 }
 player2HakiTimer = 0;
 }
 }

 // BARRIER ability activation
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "barrier") > 0 && player1BarrierTimer >= barrierCooldown && !player1BarrierActive) {
 int barrierLevel = getEffectiveAbilityLevel(1, "barrier");
 player1BarrierActive = true;
 player1BarrierX = 80; // Place barrier in player 1's side
 player1BarrierY = paddle1Y + getPaddleHeight(1, shrinkPaddlesActive) / 2 - barrierHeight / 2;
 player1BarrierDuration = barrierBaseDuration + (barrierLevel - 1) * 100;
 player1BarrierTimer = 0;
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "barrier") > 0 && player2BarrierTimer >= barrierCooldown && !player2BarrierActive) {
 int barrierLevel = getEffectiveAbilityLevel(2, "barrier");
 player2BarrierActive = true;
 player2BarrierX = 510; // Place barrier in player 2's side
 player2BarrierY = paddle2Y + getPaddleHeight(2, shrinkPaddlesActive) / 2 - barrierHeight / 2;
 player2BarrierDuration = barrierBaseDuration + (barrierLevel - 1) * 100;
 player2BarrierTimer = 0;
 }

 // TRAP ability activation - places trap in center of field
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "trap") > 0 && player1TrapTimer >= trapCooldown && !player1TrapActive) {
 int trapLevel = getEffectiveAbilityLevel(1, "trap");
 player1TrapActive = true;
 player1TrapX = 180 + (int)(Math.random() * 230); // Wider center area coverage
 player1TrapY = 30 + (int)(Math.random() * 340);
 player1TrapDuration = trapBaseDuration + (trapLevel - 1) * 150;
 player1TrapTimer = 0;
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "trap") > 0 && player2TrapTimer >= trapCooldown && !player2TrapActive) {
 int trapLevel = getEffectiveAbilityLevel(2, "trap");
 player2TrapActive = true;
 player2TrapX = 180 + (int)(Math.random() * 230); // Wider center area coverage
 player2TrapY = 30 + (int)(Math.random() * 340);
 player2TrapDuration = trapBaseDuration + (trapLevel - 1) * 150;
 player2TrapTimer = 0;
 }

 // SCREEN WARP ability activation
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "screen_warp") > 0 && player1WarpTimer >= warpCooldown) {
 int warpLevel = getEffectiveAbilityLevel(1, "screen_warp");
 int warpBranch = player1AbilityBranches.getOrDefault("screen_warp", 0);

 if (warpBranch == 1 && warpLevel >= 3) {
 // Branch 1: Full Inversion - flip opponent's screen
 player2InversionTimer = warpBaseDuration + (warpLevel - 2) * 30;
 } else if (warpBranch == 2 && warpLevel >= 3) {
 // Branch 2: Tunnel Vision - shrink visible area
 player2TunnelTimer = warpBaseDuration + (warpLevel - 2) * 40;
 } else {
 // Base: Screen distortion
 player2WarpEffectTimer = warpBaseDuration + (warpLevel - 1) * 30;
 }
 player1WarpTimer = 0;
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "screen_warp") > 0 && player2WarpTimer >= warpCooldown) {
 int warpLevel = getEffectiveAbilityLevel(2, "screen_warp");
 int warpBranch = player2AbilityBranches.getOrDefault("screen_warp", 0);

 if (warpBranch == 1 && warpLevel >= 3) {
 player1InversionTimer = warpBaseDuration + (warpLevel - 2) * 30;
 } else if (warpBranch == 2 && warpLevel >= 3) {
 player1TunnelTimer = warpBaseDuration + (warpLevel - 2) * 40;
 } else {
 player1WarpEffectTimer = warpBaseDuration + (warpLevel - 1) * 30;
 }
 player2WarpTimer = 0;
 }

 // BANKAI ability activation - TWO MODES:
 // 1. If Bankai NOT active: Activate Bankai mode (paddle turns black, gain boosts)
 // 2. If Bankai IS active: SWING ZANGETSU - ball goes 5x speed if nearby!
 if (code == player1AbilityKey && getEffectiveAbilityLevel(1, "bankai") > 0) {
 if (!player1BankaiActive && player1BankaiTimer >= bankaiCooldown) {
 // ACTIVATE BANKAI MODE
 int bankaiLevel = getEffectiveAbilityLevel(1, "bankai");
 int bankaiBranch = player1AbilityBranches.getOrDefault("bankai", 0);

 player1BankaiActive = true;
 player1BankaiDuration = bankaiBaseDuration + (bankaiLevel - 1) * 100; // 8s + 1s per level

 // PERMANENT STACKING BOOSTS!
 player1BankaiStacks++;
 player1PermanentSpeedBonus += 1; // +1 paddle speed per activation

 if (bankaiBranch == 1 && bankaiLevel >= 3) {
 // Branch 1: Zangetsu - ATTACK FOCUS
 player1ZangetsuActive = true;
 player1BallSpeedMultiplier += 0.15; // +15% ball speed per stack
 player1PermanentPaddleBonus += 5; // Small paddle boost
 } else if (bankaiBranch == 2 && bankaiLevel >= 3) {
 // Branch 2: Hollow Form - DEFENSE FOCUS
 player1HollowActive = true;
 player1HollowPhased = false;
 player1PermanentPaddleBonus += 15; // +15 paddle height per stack
 player1BallSpeedMultiplier += 0.05; // Small ball speed boost
 } else {
 // Base Bankai - balanced
 player1PermanentPaddleBonus += 10;
 player1BallSpeedMultiplier += 0.10;
 }

 // At 5+ stacks, become immune to debuffs!
 if (player1BankaiStacks >= 5) {
 player1HasBankaiImmunity = true;
 }

 player1BankaiTimer = 0;
 } else if (player1BankaiActive && player1SwordSwingCooldown == 0) {
 // SWING ZANGETSU! Ball goes 5x speed if within range!
 player1SwordSwingTimer = swordSwingDuration;
 player1SwordSwingCooldown = swordSwingCooldownTime;
 player1TotalSwordSwings++;

 int bankaiLevel = getEffectiveAbilityLevel(1, "bankai");
 int bankaiBranch = player1AbilityBranches.getOrDefault("bankai", 0);

 // Check if ball is near player 1's paddle (within 100 pixels)
 int paddleHeight = getPaddleHeight(1, shrinkPaddlesActive);
 if (ballX < 120 && ballVelX < 0) { // Ball coming toward player 1
 // ZANGETSU SLASH - 5x ball speed!
 double multiplier = 5.0;
 if (bankaiBranch == 1 && bankaiLevel >= 3) multiplier = 7.0; // Zangetsu branch = 7x!
 ballVelX = (int)(Math.abs(ballVelX) * multiplier); // Send it flying!
 ballVelY = (int)(ballVelY * 1.5);
 }

 // After 3 swings, fire GETSUGA TENSHO!
 if (player1TotalSwordSwings % 3 == 0) {
 boolean isEvolved = bankaiBranch == 1 && bankaiLevel >= 4; // Evolved at level 4+ Zangetsu
 int startX = 25;
 int startY = paddle1Y + paddleHeight / 2;
 getsugaProjectiles.add(new GetsugaTensho(startX, startY, 15, 1, isEvolved));
 }
 }
 }
 if (code == player2AbilityKey && getEffectiveAbilityLevel(2, "bankai") > 0) {
 if (!player2BankaiActive && player2BankaiTimer >= bankaiCooldown) {
 // ACTIVATE BANKAI MODE
 int bankaiLevel = getEffectiveAbilityLevel(2, "bankai");
 int bankaiBranch = player2AbilityBranches.getOrDefault("bankai", 0);

 player2BankaiActive = true;
 player2BankaiDuration = bankaiBaseDuration + (bankaiLevel - 1) * 100;

 // PERMANENT STACKING BOOSTS!
 player2BankaiStacks++;
 player2PermanentSpeedBonus += 1;

 if (bankaiBranch == 1 && bankaiLevel >= 3) {
 player2ZangetsuActive = true;
 player2BallSpeedMultiplier += 0.15;
 player2PermanentPaddleBonus += 5;
 } else if (bankaiBranch == 2 && bankaiLevel >= 3) {
 player2HollowActive = true;
 player2HollowPhased = false;
 player2PermanentPaddleBonus += 15;
 player2BallSpeedMultiplier += 0.05;
 } else {
 player2PermanentPaddleBonus += 10;
 player2BallSpeedMultiplier += 0.10;
 }

 if (player2BankaiStacks >= 5) {
 player2HasBankaiImmunity = true;
 }

 player2BankaiTimer = 0;
 } else if (player2BankaiActive && player2SwordSwingCooldown == 0) {
 // SWING ZANGETSU!
 player2SwordSwingTimer = swordSwingDuration;
 player2SwordSwingCooldown = swordSwingCooldownTime;
 player2TotalSwordSwings++;

 int bankaiLevel = getEffectiveAbilityLevel(2, "bankai");
 int bankaiBranch = player2AbilityBranches.getOrDefault("bankai", 0);

 int paddleHeight = getPaddleHeight(2, shrinkPaddlesActive);
 if (ballX > 480 && ballVelX > 0) { // Ball coming toward player 2
 double multiplier = 5.0;
 if (bankaiBranch == 1 && bankaiLevel >= 3) multiplier = 7.0;
 ballVelX = -(int)(Math.abs(ballVelX) * multiplier);
 ballVelY = (int)(ballVelY * 1.5);
 }

 // After 3 swings, fire GETSUGA TENSHO!
 if (player2TotalSwordSwings % 3 == 0) {
 boolean isEvolved = bankaiBranch == 1 && bankaiLevel >= 4;
 int startX = 575;
 int startY = paddle2Y + paddleHeight / 2;
 getsugaProjectiles.add(new GetsugaTensho(startX, startY, -15, 2, isEvolved));
 }
 }
 }
 }

 void handleCheatMenu() {
 boolean wasPaused = isPaused;
 isPaused = true;
 repaint();
 
 if (!cheatModeEnabled) {
 String inputPassword = JOptionPane.showInputDialog(null, "Enter cheat password:", "Cheat Mode", JOptionPane.QUESTION_MESSAGE);
 if (inputPassword != null && inputPassword.equals(cheatPassword)) {
 cheatModeEnabled = true;
 JOptionPane.showMessageDialog(null, "Cheat Mode Activated!\nPress M to open menu!", "Success!", JOptionPane.INFORMATION_MESSAGE);
 } else if (inputPassword != null) {
 JOptionPane.showMessageDialog(null, "Wrong password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
 }
 } else {
 String[] menuOptions = {"Spawn Power-Up", "Give Player Ability", "Delete Player Ability"};
 int menuChoice = JOptionPane.showOptionDialog(null, "What would you like to do?", "Cheat Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, menuOptions, menuOptions[0]);
 
 if (menuChoice == 0) {
 String[] powerUpList = {"paddle", "slow", "speed", "reverse", "shrink", "multiball", "jackpot", "jumpscare", "shrek", "dangerzone", "teleport", "invisiblewalls", "shrinkpaddles", "centerwall", "fireball", "zigzag", "split"};
 String selection = (String) JOptionPane.showInputDialog(null, "Choose a power-up to spawn:", "Spawn Power-Up", JOptionPane.QUESTION_MESSAGE, null, powerUpList, powerUpList[0]);
 if (selection != null) {
 int x = 100 + (int)(Math.random() * 400);
 int y = 50 + (int)(Math.random() * 300);
 powerUps.add(new PowerUp(x, y, selection));
 JOptionPane.showMessageDialog(null, "Spawned " + selection + " power-up!", "Power-Up Spawned", JOptionPane.INFORMATION_MESSAGE);
 }
 } else if (menuChoice == 1) {
 String[] playerOptions = {"Player 1", singlePlayer ? "AI (Player 2)" : "Player 2"};
 int playerChoice = JOptionPane.showOptionDialog(null, "Which player?", "Select Player", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, playerOptions, playerOptions[0]);
 
 if (playerChoice >= 0) {
 int targetPlayer = playerChoice + 1;
 
 String[] abilityList = new String[allAbilities.length];
 for (int i = 0; i < allAbilities.length; i++) {
 abilityList[i] = allAbilities[i] + " - " + getAbilityDescription(allAbilities[i]);
 }
 String abilitySelection = (String) JOptionPane.showInputDialog(null, "Choose an ability:", "Select Ability", JOptionPane.QUESTION_MESSAGE, null, abilityList, abilityList[0]);
 
 if (abilitySelection != null) {
 String selectedAbility = abilitySelection.split(" - ")[0];
 
 String levelInput = JOptionPane.showInputDialog(null, "Enter ability level (1+):", "Select Level", JOptionPane.QUESTION_MESSAGE);
 
 if (levelInput != null) {
 try {
 int abilityLevel = Integer.parseInt(levelInput);
 if (abilityLevel >= 1) {
 HashMap<String, Integer> abilities = (targetPlayer == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (targetPlayer == 1) ? player1AbilityBranches : player2AbilityBranches;
 int currentLevel = abilities.getOrDefault(selectedAbility, 0);
 
 // If setting to level 3+ and no branch chosen, force branch selection
 if (abilityLevel >= 3 && branches.getOrDefault(selectedAbility, 0) == 0) {
 String playerName = (targetPlayer == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 
 // Set to level 2 first
 abilities.put(selectedAbility, 2);
 
 // Show branch selection
 String[] branchOptions = {getBranch1Description(selectedAbility), getBranch2Description(selectedAbility)};
 int branchChoice = JOptionPane.showOptionDialog(null,
 playerName + " - " + getAbilityShortName(selectedAbility).toUpperCase() + " EVOLUTION!\n\n" +
 "Choose an evolution path to reach Level 3:\n\n" +
 " BRANCH 1: " + getBranchName(selectedAbility, 1) + "\n" +
 getBranch1Description(selectedAbility) + "\n\n" +
 " BRANCH 2: " + getBranchName(selectedAbility, 2) + "\n" +
 getBranch2Description(selectedAbility),
 " ABILITY EVOLUTION ",
 JOptionPane.DEFAULT_OPTION,
 JOptionPane.QUESTION_MESSAGE,
 null,
 new String[]{" Choose Branch 1", " Choose Branch 2"},
 null);
 
 if (branchChoice >= 0) {
 int selectedBranch = branchChoice + 1;
 branches.put(selectedAbility, selectedBranch);
 abilities.put(selectedAbility, abilityLevel);
 
 // Ask for keybind customization if this ability uses keybinds
 if (needsKeybind(selectedAbility)) {
 askForKeybind(targetPlayer, selectedAbility, playerName);
 }
 
 JOptionPane.showMessageDialog(null,
 " " + playerName + " received:\n" + 
 getAbilityShortName(selectedAbility) + " " + getBranchName(selectedAbility, selectedBranch) + 
 " at Level " + abilityLevel + " ",
 "Evolution Complete!",
 JOptionPane.INFORMATION_MESSAGE);
 } else {
 // User cancelled - leave at level 2
 JOptionPane.showMessageDialog(null, "Evolution cancelled. Ability remains at Level 2.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
 }
 } else {
 // Normal level setting (level 1-2, or level 3+ with existing branch)
 abilities.put(selectedAbility, Math.max(currentLevel, abilityLevel));
 
 String playerName = (targetPlayer == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 
 // Ask for keybind customization if this ability uses keybinds and is new (level 1)
 if (currentLevel == 0 && needsKeybind(selectedAbility)) {
 askForKeybind(targetPlayer, selectedAbility, playerName);
 }
 
 int branch = branches.getOrDefault(selectedAbility, 0);
 String branchText = (branch > 0 && abilityLevel >= 3) ? " [" + getBranchName(selectedAbility, branch) + "]" : "";
 JOptionPane.showMessageDialog(null, 
 " " + playerName + " received:\n" + getAbilityDescription(selectedAbility) + branchText + " at Level " + abilityLevel + " ", 
 "Ability Granted!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 
 if (selectedAbility.equals("gun") && currentLevel == 0) {
 if (targetPlayer == 1) player1GunTimer = gunCooldown;
 else player2GunTimer = gunCooldown;
 }
 if (selectedAbility.equals("ability_stealer") && currentLevel == 0) {
 if (targetPlayer == 1) player1StealerTimer = stealerCooldown;
 else player2StealerTimer = stealerCooldown;
 }
 } else {
 JOptionPane.showMessageDialog(null, "Please enter a level of 1 or higher", "Invalid Level", JOptionPane.WARNING_MESSAGE);
 }
 } catch (NumberFormatException ex) {
 JOptionPane.showMessageDialog(null, "Please enter a valid number", "Invalid Input", JOptionPane.ERROR_MESSAGE);
 }
 }
 }
 }
 } else if (menuChoice == 2) {
 // Delete Player Ability
 String[] playerOptions = {"Player 1", singlePlayer ? "AI (Player 2)" : "Player 2"};
 int playerChoice = JOptionPane.showOptionDialog(null, "Which player's abilities to delete?", "Select Player", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, playerOptions, playerOptions[0]);
 
 if (playerChoice >= 0) {
 int targetPlayer = playerChoice + 1;
 HashMap<String, Integer> abilities = (targetPlayer == 1) ? player1Abilities : player2Abilities;
 HashMap<String, Integer> branches = (targetPlayer == 1) ? player1AbilityBranches : player2AbilityBranches;
 
 if (abilities.isEmpty()) {
 JOptionPane.showMessageDialog(null, "This player has no abilities to delete!", "No Abilities", JOptionPane.INFORMATION_MESSAGE);
 } else {
 // Create list of current abilities
 ArrayList<String> abilityNames = new ArrayList<>(abilities.keySet());
 String[] abilityList = new String[abilityNames.size()];
 for (int i = 0; i < abilityNames.size(); i++) {
 String ability = abilityNames.get(i);
 int level = abilities.get(ability);
 int branch = branches.getOrDefault(ability, 0);
 String branchText = (branch > 0) ? " [" + getBranchName(ability, branch) + "]" : "";
 abilityList[i] = getAbilityShortName(ability) + branchText + " Lv" + level;
 }
 
 String selection = (String) JOptionPane.showInputDialog(null, "Choose an ability to delete:", "Delete Ability", JOptionPane.QUESTION_MESSAGE, null, abilityList, abilityList[0]);
 
 if (selection != null) {
 // Find the selected ability
 for (String ability : abilityNames) {
 String displayName = getAbilityShortName(ability);
 if (selection.startsWith(displayName)) {
 abilities.remove(ability);
 branches.remove(ability);
 String playerName = (targetPlayer == 1) ? "Player 1" : (singlePlayer ? "AI" : "Player 2");
 JOptionPane.showMessageDialog(null, " Deleted " + displayName + " from " + playerName + "!", "Ability Deleted", JOptionPane.INFORMATION_MESSAGE);
 break;
 }
 }
 }
 }
 }
 }
 }
 
 isPaused = wasPaused;
 repaint();
 }

 public void keyReleased(KeyEvent e) {
 if (isPaused) return;
 int code = e.getKeyCode();
 // CUSTOM KEYBINDS - Player 1
 if (code == player1UpKey) up1 = false;
 if (code == player1DownKey) down1 = false;
 // CUSTOM KEYBINDS - Player 2
 if (code == player2UpKey) up2 = false;
 if (code == player2DownKey) down2 = false;
 }

 public void keyTyped(KeyEvent e) {}

 // ============== MOUSE LISTENER (for main menu) ==============

 public void mouseClicked(MouseEvent e) {
 if (!showingMainMenu) return;

 // Convert screen coordinates to virtual 600x400 coordinates
 double scaleX = getWidth() / 600.0;
 double scaleY = getHeight() / 400.0;
 double scale = Math.min(scaleX, scaleY);
 double translateX = (getWidth() - 600 * scale) / 2;
 double translateY = (getHeight() - 400 * scale) / 2;
 int vx = (int)((e.getX() - translateX) / scale);
 int vy = (int)((e.getY() - translateY) / scale);

 // Check level nodes (click within 18px radius)
 for (int i = 0; i < storyModeMaxLevel; i++) {
  int dx = vx - levelNodeX[i];
  int dy = vy - levelNodeY[i];
  if (dx * dx + dy * dy < 18 * 18) {
  // Only allow selecting completed or current levels
  if (i < storyModeLevel) {
   menuSelectedLevel = i;
   repaint();
  }
  return;
  }
 }

 // Check bottom buttons
 for (int i = 0; i < menuButtons.length; i++) {
  if (menuButtons[i] != null && menuButtons[i].contains(vx, vy)) {
  handleMenuButtonClick(i);
  return;
  }
 }
 }

 public void mousePressed(MouseEvent e) {}
 public void mouseReleased(MouseEvent e) {}
 public void mouseEntered(MouseEvent e) {}
 public void mouseExited(MouseEvent e) {}

 void handleMenuButtonClick(int buttonIndex) {
 switch (buttonIndex) {
  case 0: startStoryFromMenu(); break;
  case 1: startSinglePlayerFromMenu(); break;
  case 2: startMultiplayerFromMenu(); break;
  case 3: showSettingsFromMenu(); break;
 }
 }

 void startStoryFromMenu() {
 int selected = (menuSelectedLevel >= 0) ? menuSelectedLevel : storyModeLevel - 1;
 storyModeLevel = selected + 1;
 storyModeActive = true;
 storyPlayerScore = 0;
 storyBossScore = 0;

 // Show level intro
 String currentBoss = storyBossNames[selected];
 int scoreNeeded = storyLevelScoreToWin[selected];
 JOptionPane.showMessageDialog(gameFrame,
  "=== LEVEL " + (selected + 1) + " ===\n\n" +
  "BOSS: " + currentBoss + "\n" +
  "Score " + scoreNeeded + " points to win!\n\n" +
  "Get ready!",
  "Battle Start!", JOptionPane.INFORMATION_MESSAGE);

 startStoryLevel();
 showingMainMenu = false;
 isPaused = false;
 requestFocusInWindow();
 }

 void startSinglePlayerFromMenu() {
 String[] difficultyOptions = {"Normal", "Hard", "Impossible", "Learning AI"};
 int diffChoice = JOptionPane.showOptionDialog(gameFrame,
  "Choose AI Difficulty:",
  "AI Difficulty", JOptionPane.DEFAULT_OPTION,
  JOptionPane.QUESTION_MESSAGE, null, difficultyOptions, difficultyOptions[0]);
 if (diffChoice < 0) return; // Cancelled

 aiDifficulty = diffChoice + 1;
 singlePlayer = true;
 storyModeActive = false;
 learningAIEnabled = (aiDifficulty == 4);
 player2Name = "AI";

 resetGameState();
 showingMainMenu = false;
 isPaused = false;
 requestFocusInWindow();
 spawnMapPowerUp();
 }

 void startMultiplayerFromMenu() {
 singlePlayer = false;
 storyModeActive = false;
 aiDifficulty = 1;
 learningAIEnabled = false;
 player2Name = "Player 2";

 resetGameState();
 showingMainMenu = false;
 isPaused = false;
 requestFocusInWindow();
 spawnMapPowerUp();
 }

 void showSettingsFromMenu() {
 configureKeybinds(singlePlayer);
 }

 void returnToMainMenu() {
 showingMainMenu = true;
 isPaused = true;
 storyModeActive = false;
 loadStoryProgress();
 menuSelectedLevel = storyModeLevel - 1;
 menuAnimationTimer = 0;
 repaint();
 }

 void resetGameState() {
 scorePlayer1 = 0;
 scorePlayer2 = 0;
 level1 = 1;
 level2 = 1;
 pointsToNextLevel1 = 1;
 pointsToNextLevel2 = 1;
 totalPointsThisLevel1 = 0;
 totalPointsThisLevel2 = 0;
 ballX = 250; ballY = 150;
 ballVelX = 2; ballVelY = 2;
 initialBallVelX = 2; initialBallVelY = 2;
 ballSpeed = 3.0;
 paddle1Y = 100; paddle2Y = 100;
 player1Abilities.clear();
 player2Abilities.clear();
 player1AbilityBranches.clear();
 player2AbilityBranches.clear();
 powerUps.clear();
 bullets.clear();
 explosions.clear();
 noScoreTimer = 0;
 ballNotHitTimer = 0;
 }

 // Save game state to file
 private void saveGame() {
 // Ask for save name
 String saveName = JOptionPane.showInputDialog(this, "Enter a name for this save:", "Save Game", JOptionPane.QUESTION_MESSAGE);
 if (saveName == null || saveName.trim().isEmpty()) {
 JOptionPane.showMessageDialog(this, "Save cancelled - no name provided.", "Save Cancelled", JOptionPane.WARNING_MESSAGE);
 return;
 }

 // Clean the save name (remove invalid characters)
 saveName = saveName.replaceAll("[^a-zA-Z0-9_-]", "_");

 try {
 // Create saves directory if it doesn't exist
 File savesDir = new File("saves");
 if (!savesDir.exists()) {
 savesDir.mkdir();
 }

 File saveFile = new File("saves/" + saveName + ".sav");
 PrintWriter writer = new PrintWriter(new FileWriter(saveFile));

 // Save game state
 writer.println("# Pong Game Save File");
 writer.println("saveName=" + saveName);
 writer.println("scorePlayer1=" + scorePlayer1);
 writer.println("scorePlayer2=" + scorePlayer2);
 writer.println("level1=" + level1);
 writer.println("level2=" + level2);
 writer.println("singlePlayer=" + singlePlayer);
 writer.println("aiDifficulty=" + aiDifficulty);

 // Save player 1 abilities
 writer.println("player1AbilitiesCount=" + player1Abilities.size());
 for (String ability : player1Abilities.keySet()) {
 writer.println("p1ability=" + ability + ":" + player1Abilities.get(ability));
 }

 // Save player 1 branches
 writer.println("player1BranchesCount=" + player1AbilityBranches.size());
 for (String ability : player1AbilityBranches.keySet()) {
 writer.println("p1branch=" + ability + ":" + player1AbilityBranches.get(ability));
 }

 // Save player 2 abilities
 writer.println("player2AbilitiesCount=" + player2Abilities.size());
 for (String ability : player2Abilities.keySet()) {
 writer.println("p2ability=" + ability + ":" + player2Abilities.get(ability));
 }

 // Save player 2 branches
 writer.println("player2BranchesCount=" + player2AbilityBranches.size());
 for (String ability : player2AbilityBranches.keySet()) {
 writer.println("p2branch=" + ability + ":" + player2AbilityBranches.get(ability));
 }

 // Save keybinds
 writer.println("player1UpKey=" + player1UpKey);
 writer.println("player1DownKey=" + player1DownKey);
 writer.println("player2UpKey=" + player2UpKey);
 writer.println("player2DownKey=" + player2DownKey);

 writer.close();
 JOptionPane.showMessageDialog(this, "Game saved successfully as '" + saveName + "'!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
 } catch (IOException ex) {
 JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
 }
 }

 // Load game state from file
 private void loadGame() {
 // Check if saves directory exists
 File savesDir = new File("saves");
 if (!savesDir.exists() || !savesDir.isDirectory()) {
 JOptionPane.showMessageDialog(this, "No saves directory found!", "Load Error", JOptionPane.ERROR_MESSAGE);
 return;
 }

 // Get list of save files
 File[] saveFiles = savesDir.listFiles((dir, name) -> name.endsWith(".sav"));
 if (saveFiles == null || saveFiles.length == 0) {
 JOptionPane.showMessageDialog(this, "No save files found!", "Load Error", JOptionPane.ERROR_MESSAGE);
 return;
 }

 // Create list of save names
 String[] saveNames = new String[saveFiles.length];
 for (int i = 0; i < saveFiles.length; i++) {
 saveNames[i] = saveFiles[i].getName().replace(".sav", "");
 }

 // Show dialog to select save
 String selectedSave = (String) JOptionPane.showInputDialog(this,
 "Select a save to load:",
 "Load Game",
 JOptionPane.QUESTION_MESSAGE,
 null,
 saveNames,
 saveNames[0]);

 if (selectedSave == null) {
 return; // User cancelled
 }

 try {
 File saveFile = new File("saves/" + selectedSave + ".sav");
 if (!saveFile.exists()) {
 JOptionPane.showMessageDialog(this, "Save file not found!", "Load Error", JOptionPane.ERROR_MESSAGE);
 return;
 }

 // Clear existing abilities before loading
 player1Abilities.clear();
 player1AbilityBranches.clear();
 player2Abilities.clear();
 player2AbilityBranches.clear();

 BufferedReader reader = new BufferedReader(new FileReader(saveFile));
 String line;

 while ((line = reader.readLine()) != null) {
 if (line.startsWith("#") || line.trim().isEmpty()) continue;

 String[] parts = line.split("=", 2);
 if (parts.length != 2) continue;

 String key = parts[0];
 String value = parts[1];

 // Load basic game state
 if (key.equals("scorePlayer1")) scorePlayer1 = Integer.parseInt(value);
 else if (key.equals("scorePlayer2")) scorePlayer2 = Integer.parseInt(value);
 else if (key.equals("level1")) level1 = Integer.parseInt(value);
 else if (key.equals("level2")) level2 = Integer.parseInt(value);
 else if (key.equals("singlePlayer")) singlePlayer = Boolean.parseBoolean(value);
 else if (key.equals("aiDifficulty")) aiDifficulty = Integer.parseInt(value);

 // Load abilities
 else if (key.equals("p1ability")) {
 String[] abilityParts = value.split(":");
 player1Abilities.put(abilityParts[0], Integer.parseInt(abilityParts[1]));
 }
 else if (key.equals("p1branch")) {
 String[] branchParts = value.split(":");
 player1AbilityBranches.put(branchParts[0], Integer.parseInt(branchParts[1]));
 }
 else if (key.equals("p2ability")) {
 String[] abilityParts = value.split(":");
 player2Abilities.put(abilityParts[0], Integer.parseInt(abilityParts[1]));
 }
 else if (key.equals("p2branch")) {
 String[] branchParts = value.split(":");
 player2AbilityBranches.put(branchParts[0], Integer.parseInt(branchParts[1]));
 }
 // Load keybinds
 else if (key.equals("player1UpKey")) player1UpKey = Integer.parseInt(value);
 else if (key.equals("player1DownKey")) player1DownKey = Integer.parseInt(value);
 else if (key.equals("player2UpKey")) player2UpKey = Integer.parseInt(value);
 else if (key.equals("player2DownKey")) player2DownKey = Integer.parseInt(value);
 }

 reader.close();
 JOptionPane.showMessageDialog(this, "Game '" + selectedSave + "' loaded successfully!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
 repaint();
 } catch (IOException | NumberFormatException ex) {
 JOptionPane.showMessageDialog(this, "Error loading game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
 }
 }

 // Delete a saved game
 private void deleteGame() {
 // Check if saves directory exists
 File savesDir = new File("saves");
 if (!savesDir.exists() || !savesDir.isDirectory()) {
 JOptionPane.showMessageDialog(this, "No saves directory found!", "Delete Error", JOptionPane.ERROR_MESSAGE);
 return;
 }

 // Get list of save files
 File[] saveFiles = savesDir.listFiles((dir, name) -> name.endsWith(".sav"));
 if (saveFiles == null || saveFiles.length == 0) {
 JOptionPane.showMessageDialog(this, "No save files found!", "Delete Error", JOptionPane.ERROR_MESSAGE);
 return;
 }

 // Create list of save names
 String[] saveNames = new String[saveFiles.length];
 for (int i = 0; i < saveFiles.length; i++) {
 saveNames[i] = saveFiles[i].getName().replace(".sav", "");
 }

 // Show dialog to select save
 String selectedSave = (String) JOptionPane.showInputDialog(this,
 "Select a save to delete:",
 "Delete Game",
 JOptionPane.WARNING_MESSAGE,
 null,
 saveNames,
 saveNames[0]);

 if (selectedSave == null) {
 return; // User cancelled
 }

 // Confirm deletion
 int confirm = JOptionPane.showConfirmDialog(this,
 "Are you sure you want to delete '" + selectedSave + "'?\nThis action cannot be undone!",
 "Confirm Delete",
 JOptionPane.YES_NO_OPTION,
 JOptionPane.WARNING_MESSAGE);

 if (confirm != JOptionPane.YES_OPTION) {
 return;
 }

 // Delete the file
 File saveFile = new File("saves/" + selectedSave + ".sav");
 if (saveFile.delete()) {
 JOptionPane.showMessageDialog(this, "Save '" + selectedSave + "' deleted successfully!", "Delete Game", JOptionPane.INFORMATION_MESSAGE);
 } else {
 JOptionPane.showMessageDialog(this, "Failed to delete save file!", "Delete Error", JOptionPane.ERROR_MESSAGE);
 }
 }

 // Helper method to check if an ability needs a keybind
 private boolean needsKeybind(String ability) {
 return ability.equals("gravity_hammer") || 
 ability.equals("magnet_ball") || 
 ability.equals("portal_pong") || 
 ability.equals("power_siphon") || 
 ability.equals("time_loop") ||
 ability.equals("speed_boost"); // Dash ability at level 3+
 }
 
 // Helper method to ask for custom keybind
 private void askForKeybind(int player, String abilityName, String playerName) {
 String currentKey = (player == 1) ? KeyEvent.getKeyText(player1AbilityKey) : KeyEvent.getKeyText(player2AbilityKey);
 
 String message = playerName + " received an ability that uses a keybind!\n\n" +
 "Current ability key: " + currentKey + "\n\n" +
 "Would you like to change the keybind?";
 
 int choice = JOptionPane.showConfirmDialog(null, message, "Customize Keybind?", JOptionPane.YES_NO_OPTION);
 
 if (choice == JOptionPane.YES_OPTION) {
 String[] keyOptions = {
 "Q", "E", "R", "T", "F", "G", "Z", "X", "C", "V", "B",
 "1", "2", "3", "4", "5",
 "SPACE", "SHIFT", "CTRL",
 "/ (Slash)", ". (Period)", ", (Comma)",
 "Keep Current (" + currentKey + ")"
 };
 
 String selected = (String) JOptionPane.showInputDialog(
 null,
 "Choose a key for " + playerName + "'s ability:\n" + getAbilityShortName(abilityName),
 "Select Keybind",
 JOptionPane.QUESTION_MESSAGE,
 null,
 keyOptions,
 keyOptions[0]
 );
 
 if (selected != null && !selected.startsWith("Keep Current")) {
 int keyCode = getKeyCodeFromString(selected);
 if (player == 1) {
 player1AbilityKey = keyCode;
 JOptionPane.showMessageDialog(null, 
 playerName + "'s ability key set to: " + selected, 
 "Keybind Updated!", 
 JOptionPane.INFORMATION_MESSAGE);
 } else {
 player2AbilityKey = keyCode;
 JOptionPane.showMessageDialog(null, 
 playerName + "'s ability key set to: " + selected, 
 "Keybind Updated!", 
 JOptionPane.INFORMATION_MESSAGE);
 }
 }
 }
 }
 
 // Helper method to convert string to KeyEvent code
 private int getKeyCodeFromString(String keyStr) {
 switch(keyStr) {
 case "Q": return KeyEvent.VK_Q;
 case "E": return KeyEvent.VK_E;
 case "R": return KeyEvent.VK_R;
 case "T": return KeyEvent.VK_T;
 case "F": return KeyEvent.VK_F;
 case "G": return KeyEvent.VK_G;
 case "Z": return KeyEvent.VK_Z;
 case "X": return KeyEvent.VK_X;
 case "C": return KeyEvent.VK_C;
 case "V": return KeyEvent.VK_V;
 case "B": return KeyEvent.VK_B;
 case "1": return KeyEvent.VK_1;
 case "2": return KeyEvent.VK_2;
 case "3": return KeyEvent.VK_3;
 case "4": return KeyEvent.VK_4;
 case "5": return KeyEvent.VK_5;
 case "SPACE": return KeyEvent.VK_SPACE;
 case "SHIFT": return KeyEvent.VK_SHIFT;
 case "CTRL": return KeyEvent.VK_CONTROL;
 case "/ (Slash)": return KeyEvent.VK_SLASH;
 case ". (Period)": return KeyEvent.VK_PERIOD;
 case ", (Comma)": return KeyEvent.VK_COMMA;
 default: return KeyEvent.VK_Q; // Default fallback
 }
 }

 // ============== ONLINE MULTIPLAYER (disabled - NetworkClient not implemented) ==============

 static void startOnlineMultiplayer() {
 JOptionPane.showMessageDialog(null, "Online multiplayer is not yet available.", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
 }

 void sendOnlineGameState() {
 // Disabled - NetworkClient not implemented
 }

 void applyOnlineInput() {
 if (!isOnlineGame) return;
 if (isHost) paddle2Y = remotePaddleY;
 }

 // ============== END ONLINE MULTIPLAYER ==============

 public static void main(String[] args) {
 new PingPongGame(); // Open directly to main menu
 }
}

// Helper class for Time Loop - stores game state
class GameState {
 int ballX, ballY, ballVelX, ballVelY;
 int paddle1Y, paddle2Y;
 int player1Score, player2Score;
 
 GameState(int ballX, int ballY, int ballVelX, int ballVelY, int paddle1Y, int paddle2Y, int p1Score, int p2Score) {
 this.ballX = ballX;
 this.ballY = ballY;
 this.ballVelX = ballVelX;
 this.ballVelY = ballVelY;
 this.paddle1Y = paddle1Y;
 this.paddle2Y = paddle2Y;
 this.player1Score = p1Score;
 this.player2Score = p2Score;
 }
}