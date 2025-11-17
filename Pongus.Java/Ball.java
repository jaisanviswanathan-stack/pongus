/**
 * Ball entity - encapsulates ball state and behavior
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class Ball {
    // Position
    private int x;
    private int y;

    // Velocity
    private int velX;
    private int velY;

    // Modifiers
    private double speedMultiplier;

    // Effects
    private boolean fireballActive;
    private boolean teleportActive;
    private boolean zigzagActive;
    private boolean splitActive;

    /**
     * Constructor
     */
    public Ball() {
        reset();
    }

    /**
     * Constructor with position
     */
    public Ball(int x, int y, int velX, int velY) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.speedMultiplier = 1.0;
        this.fireballActive = false;
        this.teleportActive = false;
        this.zigzagActive = false;
        this.splitActive = false;
    }

    /**
     * Reset ball to starting position and velocity
     */
    public void reset() {
        this.x = GameConstants.BALL_INITIAL_X;
        this.y = GameConstants.BALL_INITIAL_Y;
        this.velX = GameConstants.BALL_BASE_SPEED;
        this.velY = GameConstants.BALL_BASE_SPEED;
        this.speedMultiplier = 1.0;
        this.fireballActive = false;
        this.teleportActive = false;
        this.zigzagActive = false;
        this.splitActive = false;
    }

    /**
     * Update ball position based on velocity
     */
    public void update() {
        x += (int)(velX * speedMultiplier);
        y += (int)(velY * speedMultiplier);
    }

    /**
     * Reverse X direction (bounce horizontally)
     */
    public void reverseX() {
        velX = -velX;
    }

    /**
     * Reverse Y direction (bounce vertically)
     */
    public void reverseY() {
        velY = -velY;
    }

    /**
     * Check if ball is out of bounds (left side)
     */
    public boolean isOutLeft() {
        return x < 0;
    }

    /**
     * Check if ball is out of bounds (right side)
     */
    public boolean isOutRight() {
        return x > GameConstants.RIGHT_BOUNDARY - GameConstants.BALL_DIAMETER;
    }

    /**
     * Check if ball hit top boundary
     */
    public boolean hitTopBoundary() {
        return y <= GameConstants.TOP_BOUNDARY;
    }

    /**
     * Check if ball hit bottom boundary
     */
    public boolean hitBottomBoundary() {
        return y >= GameConstants.BOTTOM_BOUNDARY;
    }

    /**
     * Apply speed boost from ability
     */
    public void applySpeedBoost(double multiplier) {
        this.speedMultiplier *= multiplier;
    }

    /**
     * Get center X position
     */
    public int getCenterX() {
        return x + GameConstants.BALL_RADIUS;
    }

    /**
     * Get center Y position
     */
    public int getCenterY() {
        return y + GameConstants.BALL_RADIUS;
    }

    /**
     * Set velocity with speed and angle
     */
    public void setVelocityFromAngle(double speed, double angle) {
        velX = (int)(speed * Math.cos(angle));
        velY = (int)(speed * Math.sin(angle));
    }

    /**
     * Get current speed
     */
    public double getSpeed() {
        return Math.sqrt(velX * velX + velY * velY);
    }

    /**
     * Increase speed by factor
     */
    public void increaseSpeed(double factor) {
        velX = (int)(velX * factor);
        velY = (int)(velY * factor);
    }

    // ==================== GETTERS ====================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getVelX() { return velX; }
    public int getVelY() { return velY; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public boolean isFireballActive() { return fireballActive; }
    public boolean isTeleportActive() { return teleportActive; }
    public boolean isZigzagActive() { return zigzagActive; }
    public boolean isSplitActive() { return splitActive; }

    // ==================== SETTERS ====================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVelX(int velX) { this.velX = velX; }
    public void setVelY(int velY) { this.velY = velY; }
    public void setSpeedMultiplier(double speedMultiplier) { this.speedMultiplier = speedMultiplier; }
    public void setFireballActive(boolean fireballActive) { this.fireballActive = fireballActive; }
    public void setTeleportActive(boolean teleportActive) { this.teleportActive = teleportActive; }
    public void setZigzagActive(boolean zigzagActive) { this.zigzagActive = zigzagActive; }
    public void setSplitActive(boolean splitActive) { this.splitActive = splitActive; }
}
