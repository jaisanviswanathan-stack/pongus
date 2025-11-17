/**
 * Physics Engine - handles ball and paddle movement
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class PhysicsEngine {

    /**
     * Update ball position and handle boundary collisions
     */
    public void updateBall(Ball ball, boolean mirrorActive, boolean fireballActive, boolean teleportActive, boolean zigzagActive) {
        // Apply speed modifiers
        double speedMult = 1.0;

        if (fireballActive) {
            speedMult *= GameConstants.FIREBALL_SPEED_MULTIPLIER;
        }
        if (teleportActive) {
            speedMult *= GameConstants.TELEPORT_SPEED_MULTIPLIER;
        }

        ball.setSpeedMultiplier(speedMult);

        // Update position
        ball.update();

        // Handle top/bottom boundary collisions
        if (ball.hitTopBoundary() || ball.hitBottomBoundary()) {
            ball.reverseY();

            // Keep ball in bounds
            if (ball.getY() < 0) {
                ball.setY(0);
            }
            if (ball.getY() > GameConstants.BOTTOM_BOUNDARY) {
                ball.setY(GameConstants.BOTTOM_BOUNDARY);
            }
        }
    }

    /**
     * Update paddle position based on player input
     */
    public void updatePaddle(Paddle paddle, Player player, Player opponent, boolean shrinkPaddlesActive,
                             boolean dangerZoneActive, int dangerZoneCenterX, int dangerZoneCenterY, int dangerZoneRadius) {

        // Get paddle speed accounting for all modifiers
        double speed = player.getPaddleSpeed(opponent, dangerZoneActive, dangerZoneCenterX, dangerZoneCenterY, dangerZoneRadius);

        // Handle puppet master (opponent controls paddle)
        if (player.getPuppetEffectTimer() > 0) {
            // Opponent's movement controls this paddle
            if (opponent.isMovingUp()) {
                paddle.moveUp(speed);
            }
            if (opponent.isMovingDown()) {
                paddle.moveDown(speed);
            }
            player.setPaddleY(paddle.getY());
            return;
        }

        // Handle Chaos Engine debuff (inverted controls)
        boolean invertControls = false;
        if (player.getCurrentDebuff() == 1) { // Inverted gravity
            invertControls = true;
        }

        // Handle reverse controls
        boolean reversed = player.hasReversedControls() || invertControls;

        // Normal movement
        if (player.isMovingUp()) {
            if (reversed) {
                paddle.moveDown(speed);
            } else {
                paddle.moveUp(speed);
            }
        }

        if (player.isMovingDown()) {
            if (reversed) {
                paddle.moveUp(speed);
            } else {
                paddle.moveDown(speed);
            }
        }

        // Update player's paddle Y position
        player.setPaddleY(paddle.getY());

        // Update paddle (updates height based on effects)
        paddle.update(shrinkPaddlesActive);
    }

    /**
     * Calculate ball bounce angle based on where it hits paddle
     */
    public void handlePaddleCollision(Ball ball, Paddle paddle, boolean isLeftPaddle) {
        // Calculate hit position on paddle (0.0 = top, 1.0 = bottom)
        int ballCenterY = ball.getCenterY();
        int paddleTopY = paddle.getY();
        int paddleHeight = paddle.getHeight();

        double hitPosition = (double)(ballCenterY - paddleTopY) / paddleHeight;
        hitPosition = Math.max(0.0, Math.min(1.0, hitPosition)); // Clamp 0-1

        // Calculate bounce angle based on hit position
        // Top = -45°, Middle = 0°, Bottom = +45°
        double angle = (hitPosition - 0.5) * Math.PI / 2; // -45° to +45° in radians

        // Get current ball speed
        double currentSpeed = ball.getSpeed();
        currentSpeed = Math.max(currentSpeed, GameConstants.BALL_BASE_SPEED);

        // Set new velocity based on angle
        if (isLeftPaddle) {
            ball.setVelX((int)(Math.abs(Math.cos(angle)) * currentSpeed));
        } else {
            ball.setVelX(-(int)(Math.abs(Math.cos(angle)) * currentSpeed));
        }

        ball.setVelY((int)(Math.sin(angle) * currentSpeed));

        // Ensure minimum velocity
        if (Math.abs(ball.getVelX()) < 2) {
            ball.setVelX(ball.getVelX() > 0 ? 2 : -2);
        }
        if (Math.abs(ball.getVelY()) < 1) {
            ball.setVelY(ball.getVelY() > 0 ? 1 : -1);
        }
    }

    /**
     * Apply gravity effect to ball
     */
    public void applyGravity(Ball ball, boolean gravityActive) {
        if (gravityActive) {
            // Ball falls downward
            ball.setVelY(ball.getVelY() + 1); // Gravity acceleration

            // Terminal velocity
            if (ball.getVelY() > 15) {
                ball.setVelY(15);
            }
        }
    }

    /**
     * Apply magnet effect to ball
     */
    public void applyMagnet(Ball ball, Paddle paddle, boolean magnetActive, boolean repelMode) {
        if (!magnetActive) return;

        int ballCenterX = ball.getCenterX();
        int ballCenterY = ball.getCenterY();
        int paddleCenterX = paddle.getCenterX();
        int paddleCenterY = paddle.getCenterY();

        // Calculate direction to paddle
        double dx = paddleCenterX - ballCenterX;
        double dy = paddleCenterY - ballCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 150) { // Magnet range
            // Normalize direction
            dx /= distance;
            dy /= distance;

            // Apply force
            double force = repelMode ? -2.0 : 2.0;

            ball.setVelX((int)(ball.getVelX() + dx * force));
            ball.setVelY((int)(ball.getVelY() + dy * force));

            // Cap velocity
            if (Math.abs(ball.getVelX()) > 20) {
                ball.setVelX(ball.getVelX() > 0 ? 20 : -20);
            }
            if (Math.abs(ball.getVelY()) > 20) {
                ball.setVelY(ball.getVelY() > 0 ? 20 : -20);
            }
        }
    }

    /**
     * Apply gravity hammer effect - slam ball downward
     */
    public void applyGravityHammer(Ball ball, Paddle paddle) {
        // Ball gets slammed downward with force
        ball.setVelY(Math.abs(ball.getVelY()) + 10);

        // Also push ball away from paddle
        if (paddle.getX() < GameConstants.SCREEN_WIDTH / 2) {
            // Left paddle - push right
            ball.setVelX(Math.abs(ball.getVelX()) + 5);
        } else {
            // Right paddle - push left
            ball.setVelX(-Math.abs(ball.getVelX()) - 5);
        }
    }

    /**
     * Apply gravity well pull effect
     */
    public void applyGravityWell(Ball ball, int wellX, int wellY) {
        int ballCenterX = ball.getCenterX();
        int ballCenterY = ball.getCenterY();

        // Calculate direction to well
        double dx = wellX - ballCenterX;
        double dy = wellY - ballCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 150) { // Well range
            // Normalize and apply pull
            dx /= distance;
            dy /= distance;

            double pullStrength = 3.0;
            ball.setVelX((int)(ball.getVelX() + dx * pullStrength));
            ball.setVelY((int)(ball.getVelY() + dy * pullStrength));
        }
    }

    /**
     * Calculate AI prediction for ball position
     */
    public int predictBallY(Ball ball, int paddleX, double aiSpeed) {
        // Simple linear prediction
        int ballX = ball.getX();
        int ballY = ball.getY();
        int ballVelX = ball.getVelX();
        int ballVelY = ball.getVelY();

        // Predict where ball will be when it reaches paddle
        if ((paddleX < GameConstants.SCREEN_WIDTH / 2 && ballVelX < 0) ||
            (paddleX > GameConstants.SCREEN_WIDTH / 2 && ballVelX > 0)) {

            int framesToReach = Math.abs((paddleX - ballX) / ballVelX);
            int predictedY = ballY + ballVelY * framesToReach;

            // Account for bounces
            while (predictedY < 0 || predictedY > GameConstants.BOTTOM_BOUNDARY) {
                if (predictedY < 0) {
                    predictedY = -predictedY;
                }
                if (predictedY > GameConstants.BOTTOM_BOUNDARY) {
                    predictedY = 2 * GameConstants.BOTTOM_BOUNDARY - predictedY;
                }
            }

            return predictedY;
        }

        return ballY;
    }
}
