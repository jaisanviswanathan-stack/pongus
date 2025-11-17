/**
 * Paddle entity - represents a player's paddle
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class Paddle {
    private int x;
    private int y;
    private int width;
    private int height;
    private Player owner;

    /**
     * Constructor
     */
    public Paddle(int x, int y, Player owner) {
        this.x = x;
        this.y = y;
        this.width = GameConstants.PADDLE_BASE_WIDTH;
        this.height = GameConstants.PADDLE_BASE_HEIGHT;
        this.owner = owner;
    }

    /**
     * Update paddle position based on player input
     */
    public void update(boolean shrinkPaddlesActive) {
        // Update height based on abilities and effects
        this.height = owner.getPaddleHeight(shrinkPaddlesActive);

        // Keep paddle within bounds
        if (y < 0) {
            y = 0;
        }
        if (y > GameConstants.BOTTOM_BOUNDARY - height) {
            y = GameConstants.BOTTOM_BOUNDARY - height;
        }
    }

    /**
     * Move paddle up
     */
    public void moveUp(double speed) {
        y -= speed;
        if (y < 0) y = 0;
    }

    /**
     * Move paddle down
     */
    public void moveDown(double speed) {
        y += speed;
        int maxY = GameConstants.BOTTOM_BOUNDARY - height;
        if (y > maxY) y = maxY;
    }

    /**
     * Get center Y position
     */
    public int getCenterY() {
        return y + height / 2;
    }

    /**
     * Get center X position
     */
    public int getCenterX() {
        return x + width / 2;
    }

    /**
     * Check if point is within paddle bounds
     */
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    /**
     * Check collision with ball
     */
    public boolean collidesWith(Ball ball) {
        int ballX = ball.getX();
        int ballY = ball.getY();
        int ballSize = GameConstants.BALL_DIAMETER;

        return ballX + ballSize >= x &&
               ballX <= x + width &&
               ballY + ballSize >= y &&
               ballY <= y + height;
    }

    // ==================== GETTERS ====================
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Player getOwner() { return owner; }

    // ==================== SETTERS ====================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}
