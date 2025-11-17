import javax.swing.JOptionPane;

/**
 * Score Manager - handles scoring, combos, leveling, and XP
 *
 * @author Jaisan Viswanathan
 * @version 157
 */
public class ScoreManager {
    private boolean singlePlayer;

    public ScoreManager(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
    }

    /**
     * Award points to a player for hitting the ball
     */
    public void awardHitPoints(Player player, Player opponent) {
        int basePoints = GameConstants.POINTS_PER_HIT;

        // Double points ability
        int doublePointsLevel = player.getEffectiveAbilityLevel("double_points");
        int doublePointsBranch = player.getAbilityBranch("double_points");

        if (doublePointsLevel > 0) {
            basePoints *= GameConstants.DOUBLE_POINTS_MULTIPLIER;
        }

        // Point Leech (branch 1) - steal points from opponent
        if (doublePointsBranch == 1 && doublePointsLevel >= 3) {
            int stealAmount = doublePointsLevel - 2; // Level 3 = 1 point, Level 4 = 2, etc.
            int opponentScore = opponent.getScore();
            if (opponentScore >= stealAmount) {
                opponent.setScore(opponentScore - stealAmount);
                basePoints += stealAmount;
            }
        }

        // Combo Master (branch 2) - bonus for consecutive hits
        if (doublePointsBranch == 2 && doublePointsLevel >= 3) {
            player.incrementCombo();
            int comboBonus = Math.min(player.getComboCount() / 5, 2); // Max +2 points
            basePoints += comboBonus;
        }

        // Award points
        player.addScore(basePoints);

        // Increment combo for opponent (they failed to hit)
        opponent.resetCombo();

        // Update special ability point trackers
        updateSpecialAbilityPoints(player, basePoints);

        // Check for level up
        checkLevelUp(player);

        // Check for underdog comeback
        checkUnderdogComeback(player, opponent);
    }

    /**
     * Update special ability point trackers (Joshua, Jaisan, evolved abilities)
     */
    private void updateSpecialAbilityPoints(Player player, int points) {
        // Joshua points (levels up every 10 points)
        if (player.getEffectiveAbilityLevel("joshua") > 0) {
            player.setJoshuaPoints(player.getJoshuaPoints() + points);
        }

        // Jaisan points (levels up every 10 points)
        if (player.getEffectiveAbilityLevel("jaisan") > 0) {
            player.setJaisanPoints(player.getJaisanPoints() + points);
        }

        // Evolved ability points (level up every 20 points)
        for (String ability : GameConstants.ALL_ABILITIES) {
            int abilityLevel = player.getEffectiveAbilityLevel(ability);
            int branch = player.getAbilityBranch(ability);

            if (abilityLevel >= 3 && branch > 0) {
                // This is an evolved ability
                int currentPoints = player.getEvolvedAbilityPoints().getOrDefault(ability, 0);
                player.getEvolvedAbilityPoints().put(ability, currentPoints + points);

                // Check for level up
                if (currentPoints + points >= 20) {
                    player.getEvolvedAbilityPoints().put(ability, 0);
                    int newLevel = Math.min(abilityLevel + 1, GameConstants.MAX_ABILITY_LEVEL);
                    player.getAbilities().put(ability, newLevel);
                }
            }
        }
    }

    /**
     * Check if player should level up
     */
    private void checkLevelUp(Player player) {
        while (player.getTotalPointsThisLevel() >= player.getPointsToNextLevel()) {
            player.levelUp();
        }
    }

    /**
     * Check and apply underdog comeback mechanics
     */
    private void checkUnderdogComeback(Player player, Player opponent) {
        int scoreDiff = opponent.getScore() - player.getScore();

        // If player is behind by at least 3 points
        if (scoreDiff >= GameConstants.UNDERDOG_SCORE_DIFFERENCE) {
            // Trigger at every 10 points behind (10, 20, 30, etc.)
            int triggerThreshold = (scoreDiff / 10) * 10;

            if (triggerThreshold > player.getLastUnderdogTrigger()) {
                player.setLastUnderdogTrigger(triggerThreshold);

                // Give player a speed boost
                double bonusMultiplier = 1.0 + Math.min(scoreDiff * GameConstants.UNDERDOG_SPEED_BONUS,
                                                         GameConstants.MAX_UNDERDOG_SPEED_BONUS);

                // This would be applied through an ability system in the full implementation
                // For now, we track it
            }
        } else {
            player.setLastUnderdogTrigger(0);
        }
    }

    /**
     * Handle scoring when ball goes out of bounds
     */
    public boolean checkScoring(Ball ball, Player player1, Player player2) {
        if (ball.isOutLeft()) {
            // Player 2 scores
            player2.addScore(1);
            player2.resetCombo();
            player1.resetCombo();
            checkLevelUp(player2);
            checkUnderdogComeback(player2, player1);
            return true;
        } else if (ball.isOutRight()) {
            // Player 1 scores
            player1.addScore(1);
            player1.resetCombo();
            player2.resetCombo();
            checkLevelUp(player1);
            checkUnderdogComeback(player1, player2);
            return true;
        }

        return false;
    }

    /**
     * Check if game is over (first to X points)
     */
    public boolean checkGameOver(Player player1, Player player2, int winningScore) {
        if (player1.getScore() >= winningScore) {
            showGameOverDialog("Player 1", player1, player2);
            return true;
        } else if (player2.getScore() >= winningScore) {
            String winnerName = singlePlayer ? "AI" : "Player 2";
            showGameOverDialog(winnerName, player2, player1);
            return true;
        }

        return false;
    }

    /**
     * Show game over dialog
     */
    private void showGameOverDialog(String winnerName, Player winner, Player loser) {
        String message = String.format(
            "%s wins!\n\nFinal Score:\n%s: %d (Level %d)\nOpponent: %d (Level %d)",
            winnerName,
            winnerName, winner.getScore(), winner.getLevel(),
            loser.getScore(), loser.getLevel()
        );

        JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Calculate XP needed for next level
     */
    public int calculateXPNeeded(int level) {
        return (int)(5 * Math.pow(1.5, level - 1)) * GameConstants.XP_PER_POINT;
    }

    /**
     * Get player's level progress (0.0 to 1.0)
     */
    public double getLevelProgress(Player player) {
        double current = player.getTotalPointsThisLevel();
        double needed = player.getPointsToNextLevel();
        return Math.min(1.0, current / needed);
    }
}
