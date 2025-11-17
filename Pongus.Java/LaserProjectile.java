/**
 * Laser projectile (instant beam across screen)
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class LaserProjectile extends Projectile {
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int duration;
    private int width;
    private boolean hasStolen; // For Frieza laser ability stealing

    /**
     * Constructor
     */
    public LaserProjectile(int startX, int startY, int endX, int endY, int owner) {
        super(startX, startY, 0, 0, owner);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.duration = GameConstants.LASER_DURATION;
        this.width = GameConstants.LASER_WIDTH;
        this.hasStolen = false;
    }

    @Override
    public void update() {
        duration--;
        if (duration <= 0) {
            active = false;
        }
    }

    /**
     * Check if point intersects with laser beam
     */
    public boolean intersects(int px, int py, int radius) {
        // Point-to-line distance calculation
        double A = px - startX;
        double B = py - startY;
        double C = endX - startX;
        double D = endY - startY;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;

        double xx, yy;

        if (param < 0) {
            xx = startX;
            yy = startY;
        } else if (param > 1) {
            xx = endX;
            yy = endY;
        } else {
            xx = startX + param * C;
            yy = startY + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance < (radius + width / 2);
    }

    // ==================== GETTERS ====================
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }
    public int getDuration() { return duration; }
    public int getWidth() { return width; }
    public boolean hasStolen() { return hasStolen; }

    // ==================== SETTERS ====================
    public void setDuration(int duration) { this.duration = duration; }
    public void setHasStolen(boolean hasStolen) { this.hasStolen = hasStolen; }
}
