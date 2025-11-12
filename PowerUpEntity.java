/**
 * PowerUp entity - represents a power-up on the field
 * (Named PowerUpEntity to avoid confusion with inner class in original)
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class PowerUpEntity {
    private int x;
    private int y;
    private String type;
    private int duration;

    /**
     * Constructor
     */
    public PowerUpEntity(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.duration = 0; // Duration of effect when collected
    }

    /**
     * Check if ball collected this power-up
     */
    public boolean isCollectedBy(Ball ball) {
        int ballCenterX = ball.getCenterX();
        int ballCenterY = ball.getCenterY();
        int powerUpCenterX = x + GameConstants.POWERUP_SIZE / 2;
        int powerUpCenterY = y + GameConstants.POWERUP_SIZE / 2;

        double distance = Math.sqrt(
            Math.pow(ballCenterX - powerUpCenterX, 2) +
            Math.pow(ballCenterY - powerUpCenterY, 2)
        );

        return distance < GameConstants.POWERUP_COLLECTION_RADIUS;
    }

    /**
     * Get power-up color based on type
     */
    public String getColor() {
        switch (type) {
            case "zigzag": return "ORANGE";
            case "fireball": return "RED";
            case "split": return "PURPLE";
            case "teleport": return "CYAN";
            case "mirror": return "PINK";
            case "dangerzone": return "YELLOW";
            case "gravity": return "GREEN";
            case "invisiblewalls": return "GRAY";
            case "shrinkpaddles": return "BLUE";
            case "centerwall": return "BROWN";
            default: return "WHITE";
        }
    }

    // ==================== GETTERS ====================
    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public int getDuration() { return duration; }

    // ==================== SETTERS ====================
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setType(String type) { this.type = type; }
    public void setDuration(int duration) { this.duration = duration; }
}
