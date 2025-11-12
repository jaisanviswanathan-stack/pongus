/**
 * Base class for all projectiles (bullets, lasers, etc.)
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public abstract class Projectile {
    protected int x;
    protected int y;
    protected int velX;
    protected int velY;
    protected int owner; // 1 or 2
    protected boolean active;

    /**
     * Constructor
     */
    public Projectile(int x, int y, int velX, int velY, int owner) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.owner = owner;
        this.active = true;
    }

    /**
     * Update projectile position
     */
    public void update() {
        x += velX;
        y += velY;

        // Deactivate if out of bounds
        if (isOutOfBounds()) {
            active = false;
        }
    }

    /**
     * Check if projectile is out of bounds
     */
    public boolean isOutOfBounds() {
        return x < 0 || x > GameConstants.SCREEN_WIDTH ||
               y < 0 || y > GameConstants.SCREEN_HEIGHT;
    }

    /**
     * Check collision with paddle
     */
    public boolean collidesWith(Paddle paddle) {
        return x >= paddle.getX() &&
               x <= paddle.getX() + paddle.getWidth() &&
               y >= paddle.getY() &&
               y <= paddle.getY() + paddle.getHeight();
    }

    // ==================== GETTERS ====================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getVelX() { return velX; }
    public int getVelY() { return velY; }
    public int getOwner() { return owner; }
    public boolean isActive() { return active; }

    // ==================== SETTERS ====================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setVelX(int velX) { this.velX = velX; }
    public void setVelY(int velY) { this.velY = velY; }
    public void setActive(boolean active) { this.active = active; }
}
