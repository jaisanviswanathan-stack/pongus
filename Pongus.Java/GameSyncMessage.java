import java.io.*;

/**
 * Serializable message protocol for syncing game state between online players
 * Contains all necessary information to keep both game instances in sync
 */
public class GameSyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // Ball state
    public int ballX;
    public int ballY;
    public int ballVelX;
    public int ballVelY;

    // Player paddle positions
    public int player1PaddleY;
    public int player2PaddleY;

    // Player input state
    public boolean player1Up;
    public boolean player1Down;
    public boolean player2Up;
    public boolean player2Down;

    // Scores
    public int player1Score;
    public int player2Score;

    // Game state
    public boolean isPaused;
    public int gameTime; // Frame count or milliseconds

    // Ability states
    public String[] player1AbilityNames;
    public int[] player1AbilityLevels;
    public String[] player2AbilityNames;
    public int[] player2AbilityLevels;

    // Active power-ups
    public boolean gravityActive;
    public boolean invisibleWallsActive;
    public boolean shrinkPaddlesActive;
    public boolean centerWallActive;
    public boolean mirrorActive;
    public boolean teleportActive;
    public boolean fireballActive;
    public boolean zigzagActive;
    public boolean splitActive;
    public boolean multiballActive;

    // Stun/freeze states
    public int player1StunTimer;
    public int player2StunTimer;
    public int player1FreezeActive;
    public int player2FreezeActive;

    // Message timestamp for ordering
    public long timestamp;

    // Message type for efficiency
    public static final int TYPE_FULL_SYNC = 1;      // Complete game state
    public static final int TYPE_BALL_UPDATE = 2;    // Just ball position/velocity
    public static final int TYPE_PADDLE_UPDATE = 3;  // Just paddle positions
    public static final int TYPE_INPUT_UPDATE = 4;   // Just input state
    public static final int TYPE_ABILITY_USE = 5;    // Ability was used
    public static final int TYPE_SCORE_UPDATE = 6;   // Score changed
    public static final int TYPE_GAME_START = 7;     // Game starting
    public static final int TYPE_GAME_END = 8;       // Game ended

    public int messageType;

    // For ability-specific messages
    public String abilityName;
    public int abilityPlayer; // 1 or 2

    public GameSyncMessage() {
        this.timestamp = System.currentTimeMillis();
        this.messageType = TYPE_FULL_SYNC;
    }

    public GameSyncMessage(int messageType) {
        this();
        this.messageType = messageType;
    }

    /**
     * Create a full game state sync message
     */
    public static GameSyncMessage createFullSync(
            int ballX, int ballY, int ballVelX, int ballVelY,
            int p1Y, int p2Y,
            boolean p1Up, boolean p1Down, boolean p2Up, boolean p2Down,
            int p1Score, int p2Score,
            boolean isPaused, int gameTime) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_FULL_SYNC);
        msg.ballX = ballX;
        msg.ballY = ballY;
        msg.ballVelX = ballVelX;
        msg.ballVelY = ballVelY;
        msg.player1PaddleY = p1Y;
        msg.player2PaddleY = p2Y;
        msg.player1Up = p1Up;
        msg.player1Down = p1Down;
        msg.player2Up = p2Up;
        msg.player2Down = p2Down;
        msg.player1Score = p1Score;
        msg.player2Score = p2Score;
        msg.isPaused = isPaused;
        msg.gameTime = gameTime;
        return msg;
    }

    /**
     * Create a ball position update message
     */
    public static GameSyncMessage createBallUpdate(int ballX, int ballY, int ballVelX, int ballVelY) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_BALL_UPDATE);
        msg.ballX = ballX;
        msg.ballY = ballY;
        msg.ballVelX = ballVelX;
        msg.ballVelY = ballVelY;
        return msg;
    }

    /**
     * Create a paddle position update message
     */
    public static GameSyncMessage createPaddleUpdate(int player1Y, int player2Y) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_PADDLE_UPDATE);
        msg.player1PaddleY = player1Y;
        msg.player2PaddleY = player2Y;
        return msg;
    }

    /**
     * Create an input state update message
     */
    public static GameSyncMessage createInputUpdate(
            boolean p1Up, boolean p1Down, boolean p2Up, boolean p2Down) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_INPUT_UPDATE);
        msg.player1Up = p1Up;
        msg.player1Down = p1Down;
        msg.player2Up = p2Up;
        msg.player2Down = p2Down;
        return msg;
    }

    /**
     * Create a score update message
     */
    public static GameSyncMessage createScoreUpdate(int p1Score, int p2Score) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_SCORE_UPDATE);
        msg.player1Score = p1Score;
        msg.player2Score = p2Score;
        return msg;
    }

    /**
     * Create an ability use message
     */
    public static GameSyncMessage createAbilityUse(String abilityName, int player) {
        GameSyncMessage msg = new GameSyncMessage(TYPE_ABILITY_USE);
        msg.abilityName = abilityName;
        msg.abilityPlayer = player;
        return msg;
    }

    /**
     * Create a game start message
     */
    public static GameSyncMessage createGameStart() {
        return new GameSyncMessage(TYPE_GAME_START);
    }

    /**
     * Create a game end message
     */
    public static GameSyncMessage createGameEnd() {
        return new GameSyncMessage(TYPE_GAME_END);
    }

    @Override
    public String toString() {
        return "GameSyncMessage{" +
                "type=" + messageType +
                ", timestamp=" + timestamp +
                ", ball=(" + ballX + "," + ballY + ")" +
                ", p1Y=" + player1PaddleY +
                ", p2Y=" + player2PaddleY +
                ", scores=" + player1Score + "-" + player2Score +
                '}';
    }
}
