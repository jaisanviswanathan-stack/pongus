/**
 * Standard bullet projectile
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class BulletProjectile extends Projectile {
    private int size;

    /**
     * Constructor
     */
    public BulletProjectile(int x, int y, int velX, int velY, int owner) {
        super(x, y, velX, velY, owner);
        this.size = GameConstants.BULLET_SIZE;
    }

    /**
     * Constructor with custom size
     */
    public BulletProjectile(int x, int y, int velX, int velY, int owner, int size) {
        super(x, y, velX, velY, owner);
        this.size = size;
    }

    // ==================== GETTERS ====================
    public int getSize() { return size; }

    // ==================== SETTERS ====================
    public void setSize(int size) { this.size = size; }
}
