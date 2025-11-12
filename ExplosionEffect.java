/**
 * Explosion effect (from Jiren bullets)
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class ExplosionEffect {
    private int x;
    private int y;
    private int radius;
    private int duration;
    private int owner;
    private boolean isJiren; // Jiren explosions can steal abilities
    private boolean hasStolen;

    /**
     * Constructor
     */
    public ExplosionEffect(int x, int y, int owner, boolean isJiren) {
        this.x = x;
        this.y = y;
        this.radius = GameConstants.EXPLOSION_RADIUS;
        this.duration = GameConstants.EXPLOSION_DURATION;
        this.owner = owner;
        this.isJiren = isJiren;
        this.hasStolen = false;
    }

    /**
     * Update explosion (decrease duration)
     */
    public void update() {
        duration--;
    }

    /**
     * Check if explosion is still active
     */
    public boolean isActive() {
        return duration > 0;
    }

    /**
     * Check if paddle is within explosion radius
     */
    public boolean affects(Paddle paddle) {
        int paddleCenterX = paddle.getCenterX();
        int paddleCenterY = paddle.getCenterY();

        double distance = Math.sqrt(
            Math.pow(paddleCenterX - x, 2) +
            Math.pow(paddleCenterY - y, 2)
        );

        return distance < radius;
    }

    // ==================== GETTERS ====================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
    public int getDuration() { return duration; }
    public int getOwner() { return owner; }
    public boolean isJiren() { return isJiren; }
    public boolean hasStolen() { return hasStolen; }

    // ==================== SETTERS ====================
    public void setHasStolen(boolean hasStolen) { this.hasStolen = hasStolen; }
}
