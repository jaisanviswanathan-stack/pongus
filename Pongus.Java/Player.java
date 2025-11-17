import java.util.ArrayList;
import java.util.HashMap;

/**
 * Player class - encapsulates all player state and behavior
 * Eliminates the need for duplicate player1/player2 variables
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class Player {
    // Player identification
    private final int playerNumber; // 1 or 2
    private final boolean isAI;

    // Paddle position and movement
    private int paddleY;
    private boolean movingUp;
    private boolean movingDown;

    // Keybinds
    private int upKey;
    private int downKey;
    private int abilityKey;

    // Score and progression
    private int score;
    private int level;
    private int xp;
    private int pointsToNextLevel;
    private int totalPointsThisLevel;
    private int comboCount;

    // Abilities
    private HashMap<String, Integer> abilities;
    private HashMap<String, Integer> abilityBranches;
    private HashMap<String, Integer> abilityTimers;

    // Ability cooldowns
    private int gunTimer;
    private int stealerTimer;
    private int lagTriggerTimer;
    private int reverseTimer;
    private int joshuaTimer;
    private int jaisanTimer;
    private int blindTimer;
    private int shrinkTimer;
    private int ghostTimer;
    private int dashTimer;
    private int flashTimer;
    private int freezeTimer;
    private int hammerTimer;
    private int portalTimer;
    private int siphonTimer;
    private int timeLoopTimer;
    private int hijackerTimer;
    private int virusTimer;
    private int nullifierTimer;
    private int mimicTimer;
    private int wraithTimer;
    private int poltergeistTimer;
    private int chaosTimer;
    private int puppetTimer;
    private int realityTimer;
    private int shockwaveTimer;
    private int portalMoveTimer;
    private int soulReapTimer;

    // Effect timers (active effects on this player)
    private int stunTimer;
    private int lagEffectTimer;
    private int lagTeleportTimer;
    private int reverseEffectTimer;
    private int blindEffectTimer;
    private int shrinkEffectTimer;
    private int ghostEffectTimer;
    private int dashActive;
    private int flashActive;
    private int freezeActive;
    private int gravityActive;
    private int spectralActive;
    private int nullifierActive;
    private int hijackedDuration;
    private int virusSlowTimer;
    private int hammerDuration;
    private int gravityWellDuration;
    private int portalDuration;
    private int siphonDuration;
    private int timeLoopSlowMoTimer;
    private int chaosEffectTimer;
    private int puppetEffectTimer;
    private int copiedAbilityDuration;
    private int overloadStunTimer;
    private int overloadDisableTimer;
    private int overloadShrinkTimer;
    private int stunImmunityTimer;
    private int shadowCollisionCooldown;

    // Active ability flags
    private boolean joshuaActive;
    private boolean jaisanActive;
    private boolean ghostHasPhased;
    private boolean hammerActive;
    private boolean gravityWellActive;
    private boolean magnetActive;
    private boolean magnetRepelMode;
    private boolean shadowCloneActive;
    private boolean siphonActive;

    // Saved states
    private int savedPaddleY;

    // Hijacked/copied abilities
    private String hijackedAbility;
    private String copiedAbility;

    // Disabled abilities (from virus)
    private ArrayList<String> disabledAbilities;
    private HashMap<String, Integer> virusDisableDurations;

    // Mimicked abilities
    private HashMap<String, Integer> mimickedAbilities;
    private HashMap<String, Integer> mimickedDurations;

    // Combo and debuff tracking
    private int currentDebuff; // For chaos engine

    // Portal system
    private int portalPlacementStage; // 0 = ready, 1 = entrance placed, 2 = both placed
    private Integer portalEntranceX, portalEntranceY;
    private Integer portalExitX, portalExitY;
    private Integer portal3X, portal3Y; // Dimensional Rift (branch 2)

    // Gravity well position
    private int gravityWellX, gravityWellY;

    // Shadow clone system
    private ArrayList<Integer> shadowPositions;
    private int independentShadowY;
    private boolean shadowMovingUp;

    // Overload charge (Power Siphon branch 2)
    private int overloadCharge;

    // Progression tracking
    private int joshuaPoints;
    private int jaisanPoints;
    private HashMap<String, Integer> evolvedAbilityPoints;
    private int lastUnderdogTrigger;

    // Projectiles owned by this player
    private ArrayList<Object> bullets; // Will be typed properly when Bullet class exists
    private ArrayList<Object> lasers;
    private ArrayList<Object> explosions;

    /**
     * Constructor
     */
    public Player(int playerNumber, boolean isAI) {
        this.playerNumber = playerNumber;
        this.isAI = isAI;

        // Initialize position
        this.paddleY = 100;
        this.movingUp = false;
        this.movingDown = false;

        // Initialize score/level
        this.score = 0;
        this.level = 1;
        this.xp = 0;
        this.pointsToNextLevel = 5;
        this.totalPointsThisLevel = 0;
        this.comboCount = 0;

        // Initialize collections
        this.abilities = new HashMap<>();
        this.abilityBranches = new HashMap<>();
        this.abilityTimers = new HashMap<>();
        this.disabledAbilities = new ArrayList<>();
        this.virusDisableDurations = new HashMap<>();
        this.mimickedAbilities = new HashMap<>();
        this.mimickedDurations = new HashMap<>();
        this.shadowPositions = new ArrayList<>();
        this.evolvedAbilityPoints = new HashMap<>();
        this.bullets = new ArrayList<>();
        this.lasers = new ArrayList<>();
        this.explosions = new ArrayList<>();

        // Initialize flags
        this.joshuaActive = false;
        this.jaisanActive = false;
        this.ghostHasPhased = false;
        this.hammerActive = false;
        this.gravityWellActive = false;
        this.magnetActive = false;
        this.magnetRepelMode = false;
        this.shadowCloneActive = true;
        this.siphonActive = false;

        // Initialize portal stage
        this.portalPlacementStage = 0;

        // Initialize shadow system
        this.independentShadowY = 200;
        this.shadowMovingUp = false;

        // Initialize overload
        this.overloadCharge = 0;

        // Initialize progression
        this.joshuaPoints = 0;
        this.jaisanPoints = 0;
        this.lastUnderdogTrigger = 0;
    }

    // ==================== GETTERS ====================
    public int getPlayerNumber() { return playerNumber; }
    public boolean isAI() { return isAI; }
    public int getPaddleY() { return paddleY; }
    public boolean isMovingUp() { return movingUp; }
    public boolean isMovingDown() { return movingDown; }
    public int getUpKey() { return upKey; }
    public int getDownKey() { return downKey; }
    public int getAbilityKey() { return abilityKey; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getPointsToNextLevel() { return pointsToNextLevel; }
    public int getTotalPointsThisLevel() { return totalPointsThisLevel; }
    public int getComboCount() { return comboCount; }
    public HashMap<String, Integer> getAbilities() { return abilities; }
    public HashMap<String, Integer> getAbilityBranches() { return abilityBranches; }
    public int getStunTimer() { return stunTimer; }
    public int getLagEffectTimer() { return lagEffectTimer; }
    public int getReverseEffectTimer() { return reverseEffectTimer; }
    public int getBlindEffectTimer() { return blindEffectTimer; }
    public int getShrinkEffectTimer() { return shrinkEffectTimer; }
    public int getGhostEffectTimer() { return ghostEffectTimer; }
    public boolean isJoshuaActive() { return joshuaActive; }
    public boolean isJaisanActive() { return jaisanActive; }
    public boolean isGhostHasPhased() { return ghostHasPhased; }
    public int getGunTimer() { return gunTimer; }
    public int getStealerTimer() { return stealerTimer; }
    public int getBlindTimer() { return blindTimer; }
    public int getShrinkTimer() { return shrinkTimer; }
    public int getGhostTimer() { return ghostTimer; }
    public int getDashTimer() { return dashTimer; }
    public int getFlashTimer() { return flashTimer; }
    public int getFreezeTimer() { return freezeTimer; }
    public int getHammerTimer() { return hammerTimer; }
    public int getPortalTimer() { return portalTimer; }
    public int getSiphonTimer() { return siphonTimer; }
    public int getTimeLoopTimer() { return timeLoopTimer; }
    public int getDashActive() { return dashActive; }
    public int getFlashActive() { return flashActive; }
    public int getFreezeActive() { return freezeActive; }
    public int getGravityActive() { return gravityActive; }
    public int getSpectralActive() { return spectralActive; }
    public boolean isHammerActive() { return hammerActive; }
    public int getHammerDuration() { return hammerDuration; }
    public boolean isGravityWellActive() { return gravityWellActive; }
    public int getGravityWellDuration() { return gravityWellDuration; }
    public int getGravityWellX() { return gravityWellX; }
    public int getGravityWellY() { return gravityWellY; }
    public boolean isMagnetActive() { return magnetActive; }
    public boolean isMagnetRepelMode() { return magnetRepelMode; }
    public ArrayList<Integer> getShadowPositions() { return shadowPositions; }
    public boolean isShadowCloneActive() { return shadowCloneActive; }
    public int getIndependentShadowY() { return independentShadowY; }
    public boolean isShadowMovingUp() { return shadowMovingUp; }
    public int getShadowCollisionCooldown() { return shadowCollisionCooldown; }
    public int getPortalPlacementStage() { return portalPlacementStage; }
    public Integer getPortalEntranceX() { return portalEntranceX; }
    public Integer getPortalEntranceY() { return portalEntranceY; }
    public Integer getPortalExitX() { return portalExitX; }
    public Integer getPortalExitY() { return portalExitY; }
    public Integer getPortal3X() { return portal3X; }
    public Integer getPortal3Y() { return portal3Y; }
    public int getPortalDuration() { return portalDuration; }
    public boolean isSiphonActive() { return siphonActive; }
    public int getSiphonDuration() { return siphonDuration; }
    public String getCopiedAbility() { return copiedAbility; }
    public int getCopiedAbilityDuration() { return copiedAbilityDuration; }
    public int getOverloadCharge() { return overloadCharge; }
    public int getOverloadStunTimer() { return overloadStunTimer; }
    public int getOverloadDisableTimer() { return overloadDisableTimer; }
    public int getOverloadShrinkTimer() { return overloadShrinkTimer; }
    public int getTimeLoopSlowMoTimer() { return timeLoopSlowMoTimer; }
    public String getHijackedAbility() { return hijackedAbility; }
    public int getHijackedDuration() { return hijackedDuration; }
    public ArrayList<String> getDisabledAbilities() { return disabledAbilities; }
    public HashMap<String, Integer> getVirusDisableDurations() { return virusDisableDurations; }
    public int getVirusSlowTimer() { return virusSlowTimer; }
    public int getHijackerTimer() { return hijackerTimer; }
    public int getVirusTimer() { return virusTimer; }
    public int getChaosTimer() { return chaosTimer; }
    public int getChaosEffectTimer() { return chaosEffectTimer; }
    public int getCurrentDebuff() { return currentDebuff; }
    public int getPuppetTimer() { return puppetTimer; }
    public int getPuppetEffectTimer() { return puppetEffectTimer; }
    public int getNullifierTimer() { return nullifierTimer; }
    public int getNullifierActive() { return nullifierActive; }
    public int getMimicTimer() { return mimicTimer; }
    public HashMap<String, Integer> getMimickedAbilities() { return mimickedAbilities; }
    public HashMap<String, Integer> getMimickedDurations() { return mimickedDurations; }
    public int getWraithTimer() { return wraithTimer; }
    public int getPoltergeistTimer() { return poltergeistTimer; }
    public int getRealityTimer() { return realityTimer; }
    public int getShockwaveTimer() { return shockwaveTimer; }
    public int getPortalMoveTimer() { return portalMoveTimer; }
    public int getSoulReapTimer() { return soulReapTimer; }
    public int getStunImmunityTimer() { return stunImmunityTimer; }
    public int getSavedPaddleY() { return savedPaddleY; }
    public int getLagTriggerTimer() { return lagTriggerTimer; }
    public int getLagTeleportTimer() { return lagTeleportTimer; }
    public int getReverseTimer() { return reverseTimer; }
    public int getJoshuaTimer() { return joshuaTimer; }
    public int getJaisanTimer() { return jaisanTimer; }
    public int getJoshuaPoints() { return joshuaPoints; }
    public int getJaisanPoints() { return jaisanPoints; }
    public HashMap<String, Integer> getEvolvedAbilityPoints() { return evolvedAbilityPoints; }
    public int getLastUnderdogTrigger() { return lastUnderdogTrigger; }
    public ArrayList<Object> getBullets() { return bullets; }
    public ArrayList<Object> getLasers() { return lasers; }
    public ArrayList<Object> getExplosions() { return explosions; }

    // ==================== SETTERS ====================
    public void setPaddleY(int paddleY) { this.paddleY = paddleY; }
    public void setMovingUp(boolean movingUp) { this.movingUp = movingUp; }
    public void setMovingDown(boolean movingDown) { this.movingDown = movingDown; }
    public void setUpKey(int upKey) { this.upKey = upKey; }
    public void setDownKey(int downKey) { this.downKey = downKey; }
    public void setAbilityKey(int abilityKey) { this.abilityKey = abilityKey; }
    public void setScore(int score) { this.score = score; }
    public void setLevel(int level) { this.level = level; }
    public void setXp(int xp) { this.xp = xp; }
    public void setPointsToNextLevel(int pointsToNextLevel) { this.pointsToNextLevel = pointsToNextLevel; }
    public void setTotalPointsThisLevel(int totalPointsThisLevel) { this.totalPointsThisLevel = totalPointsThisLevel; }
    public void setComboCount(int comboCount) { this.comboCount = comboCount; }
    public void setStunTimer(int stunTimer) { this.stunTimer = stunTimer; }
    public void setLagEffectTimer(int lagEffectTimer) { this.lagEffectTimer = lagEffectTimer; }
    public void setReverseEffectTimer(int reverseEffectTimer) { this.reverseEffectTimer = reverseEffectTimer; }
    public void setBlindEffectTimer(int blindEffectTimer) { this.blindEffectTimer = blindEffectTimer; }
    public void setShrinkEffectTimer(int shrinkEffectTimer) { this.shrinkEffectTimer = shrinkEffectTimer; }
    public void setGhostEffectTimer(int ghostEffectTimer) { this.ghostEffectTimer = ghostEffectTimer; }
    public void setJoshuaActive(boolean joshuaActive) { this.joshuaActive = joshuaActive; }
    public void setJaisanActive(boolean jaisanActive) { this.jaisanActive = jaisanActive; }
    public void setGhostHasPhased(boolean ghostHasPhased) { this.ghostHasPhased = ghostHasPhased; }
    public void setGunTimer(int gunTimer) { this.gunTimer = gunTimer; }
    public void setStealerTimer(int stealerTimer) { this.stealerTimer = stealerTimer; }
    public void setBlindTimer(int blindTimer) { this.blindTimer = blindTimer; }
    public void setShrinkTimer(int shrinkTimer) { this.shrinkTimer = shrinkTimer; }
    public void setGhostTimer(int ghostTimer) { this.ghostTimer = ghostTimer; }
    public void setDashTimer(int dashTimer) { this.dashTimer = dashTimer; }
    public void setFlashTimer(int flashTimer) { this.flashTimer = flashTimer; }
    public void setFreezeTimer(int freezeTimer) { this.freezeTimer = freezeTimer; }
    public void setHammerTimer(int hammerTimer) { this.hammerTimer = hammerTimer; }
    public void setPortalTimer(int portalTimer) { this.portalTimer = portalTimer; }
    public void setSiphonTimer(int siphonTimer) { this.siphonTimer = siphonTimer; }
    public void setTimeLoopTimer(int timeLoopTimer) { this.timeLoopTimer = timeLoopTimer; }
    public void setDashActive(int dashActive) { this.dashActive = dashActive; }
    public void setFlashActive(int flashActive) { this.flashActive = flashActive; }
    public void setFreezeActive(int freezeActive) { this.freezeActive = freezeActive; }
    public void setGravityActive(int gravityActive) { this.gravityActive = gravityActive; }
    public void setSpectralActive(int spectralActive) { this.spectralActive = spectralActive; }
    public void setHammerActive(boolean hammerActive) { this.hammerActive = hammerActive; }
    public void setHammerDuration(int hammerDuration) { this.hammerDuration = hammerDuration; }
    public void setGravityWellActive(boolean gravityWellActive) { this.gravityWellActive = gravityWellActive; }
    public void setGravityWellDuration(int gravityWellDuration) { this.gravityWellDuration = gravityWellDuration; }
    public void setGravityWellX(int gravityWellX) { this.gravityWellX = gravityWellX; }
    public void setGravityWellY(int gravityWellY) { this.gravityWellY = gravityWellY; }
    public void setMagnetActive(boolean magnetActive) { this.magnetActive = magnetActive; }
    public void setMagnetRepelMode(boolean magnetRepelMode) { this.magnetRepelMode = magnetRepelMode; }
    public void setShadowCloneActive(boolean shadowCloneActive) { this.shadowCloneActive = shadowCloneActive; }
    public void setIndependentShadowY(int independentShadowY) { this.independentShadowY = independentShadowY; }
    public void setShadowMovingUp(boolean shadowMovingUp) { this.shadowMovingUp = shadowMovingUp; }
    public void setShadowCollisionCooldown(int shadowCollisionCooldown) { this.shadowCollisionCooldown = shadowCollisionCooldown; }
    public void setPortalPlacementStage(int portalPlacementStage) { this.portalPlacementStage = portalPlacementStage; }
    public void setPortalEntranceX(Integer portalEntranceX) { this.portalEntranceX = portalEntranceX; }
    public void setPortalEntranceY(Integer portalEntranceY) { this.portalEntranceY = portalEntranceY; }
    public void setPortalExitX(Integer portalExitX) { this.portalExitX = portalExitX; }
    public void setPortalExitY(Integer portalExitY) { this.portalExitY = portalExitY; }
    public void setPortal3X(Integer portal3X) { this.portal3X = portal3X; }
    public void setPortal3Y(Integer portal3Y) { this.portal3Y = portal3Y; }
    public void setPortalDuration(int portalDuration) { this.portalDuration = portalDuration; }
    public void setSiphonActive(boolean siphonActive) { this.siphonActive = siphonActive; }
    public void setSiphonDuration(int siphonDuration) { this.siphonDuration = siphonDuration; }
    public void setCopiedAbility(String copiedAbility) { this.copiedAbility = copiedAbility; }
    public void setCopiedAbilityDuration(int copiedAbilityDuration) { this.copiedAbilityDuration = copiedAbilityDuration; }
    public void setOverloadCharge(int overloadCharge) { this.overloadCharge = overloadCharge; }
    public void setOverloadStunTimer(int overloadStunTimer) { this.overloadStunTimer = overloadStunTimer; }
    public void setOverloadDisableTimer(int overloadDisableTimer) { this.overloadDisableTimer = overloadDisableTimer; }
    public void setOverloadShrinkTimer(int overloadShrinkTimer) { this.overloadShrinkTimer = overloadShrinkTimer; }
    public void setTimeLoopSlowMoTimer(int timeLoopSlowMoTimer) { this.timeLoopSlowMoTimer = timeLoopSlowMoTimer; }
    public void setHijackedAbility(String hijackedAbility) { this.hijackedAbility = hijackedAbility; }
    public void setHijackedDuration(int hijackedDuration) { this.hijackedDuration = hijackedDuration; }
    public void setVirusSlowTimer(int virusSlowTimer) { this.virusSlowTimer = virusSlowTimer; }
    public void setHijackerTimer(int hijackerTimer) { this.hijackerTimer = hijackerTimer; }
    public void setVirusTimer(int virusTimer) { this.virusTimer = virusTimer; }
    public void setChaosTimer(int chaosTimer) { this.chaosTimer = chaosTimer; }
    public void setChaosEffectTimer(int chaosEffectTimer) { this.chaosEffectTimer = chaosEffectTimer; }
    public void setCurrentDebuff(int currentDebuff) { this.currentDebuff = currentDebuff; }
    public void setPuppetTimer(int puppetTimer) { this.puppetTimer = puppetTimer; }
    public void setPuppetEffectTimer(int puppetEffectTimer) { this.puppetEffectTimer = puppetEffectTimer; }
    public void setNullifierTimer(int nullifierTimer) { this.nullifierTimer = nullifierTimer; }
    public void setNullifierActive(int nullifierActive) { this.nullifierActive = nullifierActive; }
    public void setMimicTimer(int mimicTimer) { this.mimicTimer = mimicTimer; }
    public void setWraithTimer(int wraithTimer) { this.wraithTimer = wraithTimer; }
    public void setPoltergeistTimer(int poltergeistTimer) { this.poltergeistTimer = poltergeistTimer; }
    public void setRealityTimer(int realityTimer) { this.realityTimer = realityTimer; }
    public void setShockwaveTimer(int shockwaveTimer) { this.shockwaveTimer = shockwaveTimer; }
    public void setPortalMoveTimer(int portalMoveTimer) { this.portalMoveTimer = portalMoveTimer; }
    public void setSoulReapTimer(int soulReapTimer) { this.soulReapTimer = soulReapTimer; }
    public void setStunImmunityTimer(int stunImmunityTimer) { this.stunImmunityTimer = stunImmunityTimer; }
    public void setSavedPaddleY(int savedPaddleY) { this.savedPaddleY = savedPaddleY; }
    public void setLagTriggerTimer(int lagTriggerTimer) { this.lagTriggerTimer = lagTriggerTimer; }
    public void setLagTeleportTimer(int lagTeleportTimer) { this.lagTeleportTimer = lagTeleportTimer; }
    public void setReverseTimer(int reverseTimer) { this.reverseTimer = reverseTimer; }
    public void setJoshuaTimer(int joshuaTimer) { this.joshuaTimer = joshuaTimer; }
    public void setJaisanTimer(int jaisanTimer) { this.jaisanTimer = jaisanTimer; }
    public void setJoshuaPoints(int joshuaPoints) { this.joshuaPoints = joshuaPoints; }
    public void setJaisanPoints(int jaisanPoints) { this.jaisanPoints = jaisanPoints; }
    public void setLastUnderdogTrigger(int lastUnderdogTrigger) { this.lastUnderdogTrigger = lastUnderdogTrigger; }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if player is stunned
     */
    public boolean isStunned() {
        return stunTimer > 0 || stunImmunityTimer > 0 || overloadStunTimer > 0;
    }

    /**
     * Check if controls are reversed
     */
    public boolean hasReversedControls() {
        return reverseEffectTimer > 0;
    }

    /**
     * Check if player is blinded
     */
    public boolean isBlinded() {
        return blindEffectTimer > 0;
    }

    /**
     * Check if ability is available (not on cooldown and not disabled)
     */
    public boolean isAbilityReady(String abilityName) {
        if (disabledAbilities.contains(abilityName)) {
            return false;
        }
        if (overloadDisableTimer > 0) {
            return false;
        }
        // Check specific cooldown based on ability name
        // This will be implemented more elegantly with the ability system
        return true;
    }

    /**
     * Add score to this player
     */
    public void addScore(int points) {
        score += points;
        totalPointsThisLevel += points;
        xp += points * GameConstants.XP_PER_POINT;
    }

    /**
     * Increment combo count
     */
    public void incrementCombo() {
        comboCount++;
    }

    /**
     * Reset combo count
     */
    public void resetCombo() {
        comboCount = 0;
    }

    /**
     * Level up this player
     */
    public void levelUp() {
        level++;
        totalPointsThisLevel = 0;
        // Exponential leveling
        pointsToNextLevel = (int)(5 * Math.pow(1.5, level - 1));
    }

    /**
     * Update all timers (decrement active timers)
     */
    public void updateTimers() {
        // Cooldown timers
        if (gunTimer > 0) gunTimer--;
        if (stealerTimer > 0) stealerTimer--;
        if (lagTriggerTimer > 0) lagTriggerTimer--;
        if (reverseTimer > 0) reverseTimer--;
        if (joshuaTimer > 0) joshuaTimer--;
        if (jaisanTimer > 0) jaisanTimer--;
        if (blindTimer > 0) blindTimer--;
        if (shrinkTimer > 0) shrinkTimer--;
        if (ghostTimer > 0) ghostTimer--;
        if (dashTimer > 0) dashTimer--;
        if (flashTimer > 0) flashTimer--;
        if (freezeTimer > 0) freezeTimer--;
        if (hammerTimer > 0) hammerTimer--;
        if (portalTimer > 0) portalTimer--;
        if (siphonTimer > 0) siphonTimer--;
        if (timeLoopTimer > 0) timeLoopTimer--;
        if (hijackerTimer > 0) hijackerTimer--;
        if (virusTimer > 0) virusTimer--;
        if (nullifierTimer > 0) nullifierTimer--;
        if (mimicTimer > 0) mimicTimer--;
        if (wraithTimer > 0) wraithTimer--;
        if (poltergeistTimer > 0) poltergeistTimer--;
        if (chaosTimer > 0) chaosTimer--;
        if (puppetTimer > 0) puppetTimer--;
        if (realityTimer > 0) realityTimer--;
        if (shockwaveTimer > 0) shockwaveTimer--;
        if (portalMoveTimer > 0) portalMoveTimer--;
        if (soulReapTimer > 0) soulReapTimer--;

        // Effect timers
        if (stunTimer > 0) stunTimer--;
        if (lagEffectTimer > 0) lagEffectTimer--;
        if (lagTeleportTimer > 0) lagTeleportTimer--;
        if (reverseEffectTimer > 0) reverseEffectTimer--;
        if (blindEffectTimer > 0) blindEffectTimer--;
        if (shrinkEffectTimer > 0) shrinkEffectTimer--;
        if (ghostEffectTimer > 0) ghostEffectTimer--;
        if (dashActive > 0) dashActive--;
        if (flashActive > 0) flashActive--;
        if (freezeActive > 0) freezeActive--;
        if (gravityActive > 0) gravityActive--;
        if (spectralActive > 0) spectralActive--;
        if (nullifierActive > 0) nullifierActive--;
        if (hijackedDuration > 0) hijackedDuration--;
        if (virusSlowTimer > 0) virusSlowTimer--;
        if (hammerDuration > 0) hammerDuration--;
        if (gravityWellDuration > 0) gravityWellDuration--;
        if (portalDuration > 0) portalDuration--;
        if (siphonDuration > 0) siphonDuration--;
        if (timeLoopSlowMoTimer > 0) timeLoopSlowMoTimer--;
        if (chaosEffectTimer > 0) chaosEffectTimer--;
        if (puppetEffectTimer > 0) puppetEffectTimer--;
        if (copiedAbilityDuration > 0) copiedAbilityDuration--;
        if (overloadStunTimer > 0) overloadStunTimer--;
        if (overloadDisableTimer > 0) overloadDisableTimer--;
        if (overloadShrinkTimer > 0) overloadShrinkTimer--;
        if (stunImmunityTimer > 0) stunImmunityTimer--;
        if (shadowCollisionCooldown > 0) shadowCollisionCooldown--;

        // Deactivate abilities when duration ends
        if (hammerDuration == 0) hammerActive = false;
        if (gravityWellDuration == 0) gravityWellActive = false;
        if (siphonDuration == 0) siphonActive = false;
        if (copiedAbilityDuration == 0) copiedAbility = null;
        if (hijackedDuration == 0) hijackedAbility = null;
    }

    /**
     * Get the paddle X position based on player number
     */
    public int getPaddleX() {
        return playerNumber == 1 ? GameConstants.PADDLE_1_X : GameConstants.PADDLE_2_X;
    }

    /**
     * Get effective paddle height accounting for growth/shrink
     */
    public int getPaddleHeight(boolean shrinkPaddlesActive) {
        int baseHeight = GameConstants.PADDLE_BASE_HEIGHT;

        // Paddle growth ability
        int paddleGrowthLevel = abilities.getOrDefault("paddle_growth", 0);
        baseHeight += paddleGrowthLevel * GameConstants.PADDLE_GROWTH_PER_LEVEL;

        // Shrink effect from opponent
        if (shrinkEffectTimer > 0 || overloadShrinkTimer > 0) {
            baseHeight -= GameConstants.PADDLE_SHRINK_AMOUNT;
        }

        // Map modifier: shrink paddles
        if (shrinkPaddlesActive) {
            baseHeight -= GameConstants.PADDLE_SHRINK_AMOUNT;
        }

        // Ensure minimum height
        return Math.max(baseHeight, 20);
    }

    /**
     * Get effective paddle speed accounting for bonuses/penalties
     */
    public double getPaddleSpeed(Player opponent, boolean dangerZoneActive, int dangerZoneCenterX, int dangerZoneCenterY, int dangerZoneRadius) {
        double speed = GameConstants.PADDLE_BASE_SPEED;

        // Speed boost ability
        int speedBoostLevel = abilities.getOrDefault("speed_boost", 0);
        if (speedBoostLevel > 0) {
            speed *= GameConstants.PADDLE_SPEED_BOOST_MULTIPLIER;
        }

        // Slow opponent effect
        int slowOpponentLevel = opponent.getAbilities().getOrDefault("slow_opponent", 0);
        if (slowOpponentLevel > 0) {
            speed *= 0.5; // Slowed to 50%
        }

        // Virus effect
        if (virusSlowTimer > 0) {
            speed *= 0.6; // Slowed to 60%
        }

        // Gravity well effect
        if (opponent.getGravityActive() > 0) {
            speed *= 0.7; // Feel heavier, move slower
        }

        // Frozen
        if (freezeActive > 0) {
            speed = 0; // Cannot move
        }

        // Stunned
        if (isStunned()) {
            speed = 0; // Cannot move
        }

        // Puppet master (opponent controls this paddle)
        if (puppetEffectTimer > 0) {
            speed = 0; // Will be controlled by opponent
        }

        // Danger zone speed boost
        if (dangerZoneActive) {
            int paddleCenterX = getPaddleX() + GameConstants.PADDLE_BASE_WIDTH / 2;
            int paddleCenterY = paddleY + getPaddleHeight(false) / 2;
            double distanceToCenter = Math.sqrt(
                Math.pow(paddleCenterX - dangerZoneCenterX, 2) +
                Math.pow(paddleCenterY - dangerZoneCenterY, 2)
            );
            if (distanceToCenter < dangerZoneRadius) {
                speed *= GameConstants.DANGER_ZONE_SPEED_MULTIPLIER;
            }
        }

        return speed;
    }

    /**
     * Get ability level, accounting for mimicked/copied abilities
     */
    public int getEffectiveAbilityLevel(String abilityName) {
        // Check native ability
        int nativeLevel = abilities.getOrDefault(abilityName, 0);

        // Check mimicked ability
        int mimickedLevel = mimickedAbilities.getOrDefault(abilityName, 0);

        // Check copied ability (from Soul Reaper)
        if (abilityName.equals(copiedAbility) && copiedAbilityDuration > 0) {
            return 3; // Copied abilities work at level 3
        }

        // Return highest level
        return Math.max(nativeLevel, mimickedLevel);
    }

    /**
     * Get ability branch (returns 0 if no branch chosen)
     */
    public int getAbilityBranch(String abilityName) {
        return abilityBranches.getOrDefault(abilityName, 0);
    }
}
