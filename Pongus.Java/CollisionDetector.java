import java.util.ArrayList;
import java.util.List;

/**
 * Collision detection system
 * Centralizes all collision logic
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class CollisionDetector {

    /**
     * Check ball-paddle collision
     */
    public static boolean checkBallPaddleCollision(Ball ball, Paddle paddle, Player paddleOwner) {
        // Ghost ball phases through if ghost effect is active
        if (paddleOwner.getGhostEffectTimer() > 0 && !paddleOwner.isGhostHasPhased()) {
            return false; // Ball will phase through
        }

        int ballX = ball.getX();
        int ballY = ball.getY();
        int ballSize = GameConstants.BALL_DIAMETER;

        return ballX + ballSize >= paddle.getX() &&
               ballX <= paddle.getX() + paddle.getWidth() &&
               ballY + ballSize >= paddle.getY() &&
               ballY <= paddle.getY() + paddle.getHeight();
    }

    /**
     * Check ball-shadow clone collision
     */
    public static boolean checkBallShadowCollision(Ball ball, int shadowY, int shadowHeight, int shadowX) {
        int ballX = ball.getX();
        int ballY = ball.getY();
        int ballSize = GameConstants.BALL_DIAMETER;

        return ballX + ballSize >= shadowX &&
               ballX <= shadowX + GameConstants.PADDLE_BASE_WIDTH &&
               ballY + ballSize >= shadowY &&
               ballY <= shadowY + shadowHeight;
    }

    /**
     * Check bullet-paddle collision
     */
    public static boolean checkBulletPaddleCollision(BulletProjectile bullet, Paddle paddle) {
        return bullet.getX() >= paddle.getX() &&
               bullet.getX() <= paddle.getX() + paddle.getWidth() &&
               bullet.getY() >= paddle.getY() &&
               bullet.getY() <= paddle.getY() + paddle.getHeight();
    }

    /**
     * Check laser-paddle collision (returns true if laser passes through paddle)
     */
    public static boolean checkLaserPaddleCollision(LaserProjectile laser, Paddle paddle) {
        int paddleCenterX = paddle.getCenterX();
        int paddleCenterY = paddle.getCenterY();

        return laser.intersects(paddleCenterX, paddleCenterY, paddle.getHeight() / 2);
    }

    /**
     * Check explosion-paddle collision
     */
    public static boolean checkExplosionPaddleCollision(ExplosionEffect explosion, Paddle paddle) {
        return explosion.affects(paddle);
    }

    /**
     * Check ball-power-up collision
     */
    public static List<PowerUpEntity> checkPowerUpCollisions(Ball ball, List<PowerUpEntity> powerUps) {
        List<PowerUpEntity> collected = new ArrayList<>();
        for (PowerUpEntity powerUp : powerUps) {
            if (powerUp.isCollectedBy(ball)) {
                collected.add(powerUp);
            }
        }
        return collected;
    }

    /**
     * Check if ball is in danger zone
     */
    public static boolean isInDangerZone(Ball ball, int dangerZoneCenterX, int dangerZoneCenterY, int dangerZoneRadius) {
        int ballCenterX = ball.getCenterX();
        int ballCenterY = ball.getCenterY();

        double distance = Math.sqrt(
            Math.pow(ballCenterX - dangerZoneCenterX, 2) +
            Math.pow(ballCenterY - dangerZoneCenterY, 2)
        );

        return distance < dangerZoneRadius;
    }

    /**
     * Check if ball hit invisible wall
     */
    public static boolean checkInvisibleWallCollision(Ball ball, int invisibleWallY) {
        int ballY = ball.getY();
        int ballSize = GameConstants.BALL_DIAMETER;

        return ballY <= invisibleWallY && ballY + ballSize >= invisibleWallY;
    }

    /**
     * Check if ball hit center wall
     */
    public static boolean checkCenterWallCollision(Ball ball, int centerWallGapY, int centerWallGapSize) {
        int ballX = ball.getCenterX();
        int ballY = ball.getY();
        int ballSize = GameConstants.BALL_DIAMETER;

        // Check if ball is in center area (horizontally)
        boolean inCenterX = ballX > 250 && ballX < 350;

        // Check if ball hits the wall sections (not in gap)
        boolean hitsTopSection = ballY < centerWallGapY;
        boolean hitsBottomSection = ballY + ballSize > centerWallGapY + centerWallGapSize;

        return inCenterX && (hitsTopSection || hitsBottomSection);
    }

    /**
     * Check if ball enters portal
     */
    public static Integer checkPortalEntry(Ball ball, Integer portalX, Integer portalY) {
        if (portalX == null || portalY == null) {
            return null;
        }

        int ballCenterX = ball.getCenterX();
        int ballCenterY = ball.getCenterY();

        double distance = Math.sqrt(
            Math.pow(ballCenterX - portalX, 2) +
            Math.pow(ballCenterY - portalY, 2)
        );

        if (distance < GameConstants.PORTAL_RADIUS) {
            return 1; // Ball entered portal
        }

        return null;
    }

    /**
     * Check if paddle is in gravity well range
     */
    public static boolean isInGravityWellRange(Paddle paddle, int gravityWellX, int gravityWellY) {
        int paddleCenterX = paddle.getCenterX();
        int paddleCenterY = paddle.getCenterY();

        double distance = Math.sqrt(
            Math.pow(paddleCenterX - gravityWellX, 2) +
            Math.pow(paddleCenterY - gravityWellY, 2)
        );

        return distance < 150; // Gravity well radius
    }

    /**
     * Calculate distance between two points
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Check if point is within rectangle
     */
    public static boolean pointInRect(int px, int py, int rectX, int rectY, int rectWidth, int rectHeight) {
        return px >= rectX && px <= rectX + rectWidth &&
               py >= rectY && py <= rectY + rectHeight;
    }

    /**
     * Check if two rectangles overlap
     */
    public static boolean rectsOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 &&
               x1 + w1 > x2 &&
               y1 < y2 + h2 &&
               y1 + h1 > y2;
    }
}
