package com.pongus.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pongus.game.ability.AbilityManager;
import com.pongus.game.entity.*;

import static com.pongus.game.Colors.*;

/**
 * Handles all rendering for Pongus: gameplay frame and main menu.
 * Extracted from PongusGame to keep rendering separate from game logic.
 */
public class GameRenderer {
    private final GameWorld w;
    final AbilityManager abilityManager;

    // === CLASH ROYALE STYLE MENU LAYOUT (600x400 canvas, Y-down) ===
    static final int CR_TOPBAR_H  = 34;   // top status bar
    static final int CR_ARENA_Y   = 34;   // arena panel top
    static final int CR_ARENA_H   = 116;  // arena panel height
    static final int CR_DECK_Y    = 132;  // deck row top
    static final int CR_CARD_W    = 50;   // card width
    static final int CR_CARD_H    = 62;   // card height
    static final int CR_BATTLE_Y  = 165;  // battle button top
    static final int CR_BATTLE_W  = 220;  // battle button width
    static final int CR_BATTLE_H  = 60;   // battle button height
    static final int CR_CHEST_Y   = 243;  // chest row top
    static final int CR_CHEST_W   = 58;   // chest slot width
    static final int CR_CHEST_H   = 65;   // chest slot height
    static final int CR_SECBTN_Y  = 326;  // secondary buttons top
    static final int CR_SECBTN_H  = 36;   // secondary buttons height
    // Secondary button bounds: Story | 2P Local | Settings
    static final int CR_SEC0_X = 56,  CR_SEC0_W = 110;
    static final int CR_SEC1_X = 245, CR_SEC1_W = 110;
    static final int CR_SEC2_X = 434, CR_SEC2_W = 110;

    // Activity cache — pre-compute display strings to avoid GWT GC stutter in render loop
    private String[] activityLines = new String[0];
    private int activityCacheSize = -1;

    public void invalidateActivityCache() { activityCacheSize = -1; }

    private void ensureActivityCache() {
        if (w.profile == null) { activityLines = new String[0]; return; }
        if (w.profile.matchHistory.size() == activityCacheSize) return;
        int n = Math.min(w.profile.matchHistory.size(), 3);
        activityLines = new String[n];
        for (int i = 0; i < n; i++) {
            String[] p = w.profile.matchHistory.get(i).split("\\|");
            if (p.length < 3) { activityLines[i] = "?"; continue; }
            activityLines[i] = (p[0].equals("W") ? "W  vs " : "L  vs ")
                + p[1] + "  " + (p[0].equals("W") ? "+" : "-") + p[2];
        }
        activityCacheSize = w.profile.matchHistory.size();
    }

    public GameRenderer(GameWorld w, AbilityManager abilityManager) {
        this.w = w;
        this.abilityManager = abilityManager;
    }

    // ==================== CONVENIENCE WRAPPERS ====================

    private void fillRoundRect(float x, float y, float width, float h, float radius) {
        RenderUtils.fillRoundRect(w.sr, x, y, width, h, radius);
    }

    private void drawThickLine(float x1, float y1, float x2, float y2, float thickness) {
        RenderUtils.drawThickLine(w.sr, x1, y1, x2, y2, thickness);
    }

    private void drawRadialGradient(float cx, float cy, float radius, Color inner, Color outer, int steps) {
        RenderUtils.drawRadialGradient(w.sr, cx, cy, radius, inner, outer, steps);
    }

    private String getAbilityShortName(String ability) {
        return abilityManager.getShortName(ability);
    }

    private String getBranchName(String ability, int branch) {
        String name = abilityManager.getBranchName(ability, branch);
        return (name != null) ? name : getAbilityShortName(ability);
    }

    private Color getRarityColor(int rarityIndex) {
        switch (rarityIndex) {
            case 1: return Colors.RARITY_UNCOMMON;
            case 2: return Colors.RARITY_RARE;
            case 3: return Colors.RARITY_EPIC;
            case 4: return Colors.RARITY_LEGENDARY;
            default: return Colors.RARITY_COMMON;
        }
    }

    // ==================== TOP-LEVEL RENDER METHODS ====================

    /** Renders the full gameplay frame. */
    void renderGameplay() {
        // === ARENA BACKGROUND TEXTURE PASS (before shapes so shapes render on top) ===
        {
            int bgIdx = (w.profile != null) ? com.pongus.game.ArenaConfig.getArenaIndex(w.profile.trophies) : 0;
            com.badlogic.gdx.graphics.Texture bg = w.arenaBgTextures[bgIdx];
            if (bg == null) bg = w.texArenas[bgIdx]; // legacy fallback
            if (bg != null) {
                w.batch.setProjectionMatrix(w.camera.combined);
                w.batch.begin();
                // flip=true on Y axis to correct for Y-down camera
                w.batch.draw(bg, 0, 0, 600, 400, 0, 0, bg.getWidth(), bg.getHeight(), false, true);
                w.batch.end();
            }
        }

        // === SHAPE RENDERING PASS (all filled shapes first) ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        drawBackground();
        drawCenterLine();
        drawScorePanel();
        drawMapModifiers();
        drawShadowClones();
        drawPortals();
        drawBarriers();
        drawTraps();
        drawHakiArmament();
        drawBankaiEffects();
        drawHammerEffect();
        drawPaddles();
        drawBallEffects();
        drawBall();
        if (w.ball2Active) {
            drawBall2();
        }
        drawProjectiles();
        drawStatusEffectsShapes();
        drawPowerUps();
        drawScreenOverlays();
        drawCooldownBars();
        drawMatchTimerBg();

        drawBlindOverlay();
        drawAbilityButtonShapes();

        w.sr.end();

        // === LINE RENDERING PASS ===
        w.sr.begin(ShapeRenderer.ShapeType.Line);
        drawObservationLines();
        drawMagnetLines();
        w.sr.end();

        // === SPRITE/TEXT RENDERING PASS (all textures and text) ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        drawScores();
        drawAbilityLists();
        drawAbilityButtonText();
        drawStatusIndicatorsText();
        drawCooldownTimers();
        drawSynergyBanner();
        drawMatchTimer();
        drawToasts();

        if (w.overtimeActive) {
            w.tmpColor.set(1f, 0.5f, 0f, 1f);
            w.font.setColor(w.tmpColor);
            w.glyphLayout.setText(w.font, "OVERTIME - NEXT POINT WINS");
            w.font.draw(w.batch, "OVERTIME - NEXT POINT WINS", 300 - w.glyphLayout.width / 2, 50);
            w.font.setColor(1f, 1f, 1f, 1f);
        }


        // === SPRITE OVERLAYS (Phase 9) — drawn in batch pass after all text ===
        drawPaddleSprites();
        drawBallSprite();
        drawPowerUpSprites();

        w.batch.end();

        // Blind overlay drawn again after all sprites so it covers paddle textures too
        if (w.player1BlindLevel > 0 || w.player2BlindLevel > 0) {
            w.sr.setProjectionMatrix(w.camera.combined);
            w.sr.begin(ShapeRenderer.ShapeType.Filled);
            drawBlindOverlay();
            w.sr.end();
        }

        // Slide deck overlay (self-contained shape + text passes, always last)
        renderSlideDeck();
    }

    static final int SLIDE_COUNT = 6;

    /** Full-screen learn-to-play slide deck, opened via ? button on main menu. */
    void renderSlideDeck() {
        if (!w.showingSlides) return;
        int si = w.slideIndex;
        String[] titles = {
            "Welcome to Pongus!",
            "Controls",
            "Cards & Deck",
            "Abilities",
            "Power-Ups",
            "How to Win"
        };
        String[][] lines = {
            { "Pongus is a fast-paced magical Pong game.", "Bounce the ball past your opponent to score.", "Unlock cards, upgrade abilities, and rise", "through the arenas!" },
            { "W / S  —  move your paddle up and down", "Q  —  activate your current ability", "On mobile: swipe left/right half to move", "your paddle." },
            { "You have a deck of up to 8 cards.", "As you score points you draw cards from", "your deck — they grant in-match abilities.", "Manage your deck in the Collection screen." },
            { "Each card you draw grants a unique power.", "Press Q to activate it (cooldown applies).", "Cards can be upgraded in your Collection", "using copies earned from chests." },
            { "Glowing orbs spawn on the field.", "When the ball hits one it activates a", "random effect — for you or your opponent.", "Effects include speed boosts, size changes & more!" },
            { "Lead by 15 points  —  instant win.", "Or have more points when the timer ends.", "If tied: Sudden Death — next point wins!", "Earn trophies to unlock new arenas." }
        };
        int[][] hdr = { {0,200,255}, {255,180,0}, {200,80,255}, {80,200,100}, {255,140,0}, {220,60,220} };

        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);
        w.sr.setColor(Colors.c(0, 0, 0, 220));
        w.sr.rect(0, 0, 600, 400);
        // Card background
        w.sr.setColor(Colors.c(14, 18, 48, 255));
        fillRoundRect(60, 70, 480, 260, 10);
        // Coloured header strip
        w.sr.setColor(Colors.c(hdr[si][0], hdr[si][1], hdr[si][2], 255));
        fillRoundRect(60, 290, 480, 40, 10);
        // Progress dots
        int dotSpacing = 480 / (SLIDE_COUNT + 1);
        for (int i = 0; i < SLIDE_COUNT; i++) {
            if (i < si)       w.sr.setColor(Colors.c(80, 200, 80));
            else if (i == si) w.sr.setColor(Colors.c(255, 220, 50));
            else              w.sr.setColor(Colors.c(60, 60, 60));
            w.sr.circle(60 + dotSpacing * (i + 1), 82, 6, 10);
        }
        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();
        w.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        w.glyphLayout.setText(w.font, titles[si]);
        w.font.draw(w.batch, titles[si], 300 - w.glyphLayout.width / 2, 318);
        w.font.setColor(Colors.c(200, 220, 255));
        String[] bodyLines = lines[si];
        int lineY = 278;
        for (String line : bodyLines) {
            w.font.draw(w.batch, line, 76, lineY);
            lineY -= 22;
        }
        // Nav hint
        float pulse = 0.5f + 0.5f * (float)Math.sin(w.menuAnimationTimer * 0.08f);
        String hint = (si < SLIDE_COUNT - 1) ? "Tap or SPACE to continue  (" + (si+1) + "/" + SLIDE_COUNT + ")"
                                             : "Tap or SPACE to close  (" + SLIDE_COUNT + "/" + SLIDE_COUNT + ")";
        w.font.setColor(Colors.c(255, (int)(210 * pulse), 50));
        w.glyphLayout.setText(w.font, hint);
        w.font.draw(w.batch, hint, 300 - w.glyphLayout.width / 2, 100);
        // ESC hint
        w.font.setColor(Colors.c(120, 120, 140));
        w.font.draw(w.batch, "ESC to close", 510, 395);
        w.batch.end();
    }

    /** Ability button backgrounds (called in Filled shape pass of renderGameplay). */
    void drawAbilityButtonShapes() {
        if (w.isPaused || w.showingCountdown) return;
        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) return;
        int btnW = 80, btnH = 24, btnGap = 3, bottomY = 394;
        // P1 buttons — one per key-press ability drawn, stacked upward from bottom-left
        int slot = 0;
        for (int i = 0; i < w.player1DrawnCards.size() && slot < 4; i++) {
            if (!abilityManager.needsKeyPress(w.player1DrawnCards.get(i))) continue;
            int by = bottomY - (slot + 1) * btnH - slot * btnGap;
            w.sr.setColor(slot == 0 ? Colors.c(0, 200, 240, 170) : Colors.c(0, 130, 170, 110));
            fillRoundRect(5, by, btnW, btnH, 5);
            slot++;
        }
        // P2 buttons — only in 2P local mode
        if (!w.singlePlayer) {
            slot = 0;
            for (int i = 0; i < w.player2DrawnCards.size() && slot < 4; i++) {
                if (!abilityManager.needsKeyPress(w.player2DrawnCards.get(i))) continue;
                int by = bottomY - (slot + 1) * btnH - slot * btnGap;
                w.sr.setColor(slot == 0 ? Colors.c(240, 80, 60, 170) : Colors.c(170, 50, 40, 110));
                fillRoundRect(515, by, btnW, btnH, 5);
                slot++;
            }
        }
    }

    /** Ability button labels (called in SpriteBatch pass of renderGameplay). */
    void drawAbilityButtonText() {
        if (w.isPaused || w.showingCountdown) return;
        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)) return;
        int btnW = 80, btnH = 24, btnGap = 3, bottomY = 394;
        w.font.getData().setScale(0.75f, -0.75f);
        // P1 buttons
        int slot = 0;
        for (int i = 0; i < w.player1DrawnCards.size() && slot < 4; i++) {
            if (!abilityManager.needsKeyPress(w.player1DrawnCards.get(i))) continue;
            int by = bottomY - (slot + 1) * btnH - slot * btnGap;
            String name = abilityManager.getShortName(w.player1DrawnCards.get(i));
            w.font.setColor(slot == 0 ? Colors.c(180, 240, 255) : Colors.c(120, 190, 210, 180));
            w.glyphLayout.setText(w.font, name);
            w.font.draw(w.batch, name, 5 + btnW / 2 - w.glyphLayout.width / 2, by + btnH / 2 + 5);
            slot++;
        }
        // P2 buttons (2P only)
        if (!w.singlePlayer) {
            slot = 0;
            for (int i = 0; i < w.player2DrawnCards.size() && slot < 4; i++) {
                if (!abilityManager.needsKeyPress(w.player2DrawnCards.get(i))) continue;
                int by = bottomY - (slot + 1) * btnH - slot * btnGap;
                String name = abilityManager.getShortName(w.player2DrawnCards.get(i));
                w.font.setColor(slot == 0 ? Colors.c(255, 180, 160) : Colors.c(210, 130, 110, 180));
                w.glyphLayout.setText(w.font, name);
                w.font.draw(w.batch, name, 515 + btnW / 2 - w.glyphLayout.width / 2, by + btnH / 2 + 5);
                slot++;
            }
        }
        w.font.getData().setScale(1f, -1f);
        w.font.setColor(Color.WHITE);
    }

    /** Pre-match countdown overlay (3, 2, 1, GO!) shown on top of the gameplay background. */
    void renderCountdown() {
        if (!w.showingCountdown) return;
        float ct = w.countdownTimer;

        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        // Semi-transparent dark overlay
        w.sr.setColor(0f, 0.02f, 0.12f, 0.88f);
        w.sr.rect(0, 0, 600, 400);

        // Top header bar
        w.sr.setColor(Colors.c(10, 15, 50, 255));
        w.sr.rect(0, 0, 600, 52);

        // Left player panel (blue)
        w.sr.setColor(Colors.c(18, 55, 160, 230));
        fillRoundRect(15, 60, 240, 195, 10);
        // Left accent strip at top of panel
        w.sr.setColor(Colors.c(60, 130, 255, 200));
        fillRoundRect(15, 60, 240, 18, 6);

        // Right player panel (red)
        w.sr.setColor(Colors.c(160, 25, 25, 230));
        fillRoundRect(345, 60, 240, 195, 10);
        // Right accent strip
        w.sr.setColor(Colors.c(255, 80, 60, 200));
        fillRoundRect(345, 60, 240, 18, 6);

        // VS circle outer glow
        w.sr.setColor(Colors.c(255, 210, 40, 80));
        w.sr.circle(300, 157, 30, 24);
        // VS circle background
        w.sr.setColor(Colors.c(15, 12, 40, 255));
        w.sr.circle(300, 157, 24, 24);
        // VS circle border
        w.sr.setColor(Colors.c(255, 210, 40, 200));
        w.sr.circle(300, 157, 24, 24);
        w.sr.setColor(Colors.c(15, 12, 40, 255));
        w.sr.circle(300, 157, 20, 24);

        // Countdown circle background
        int cdInt = (ct > 0.4f) ? (int) Math.ceil(ct) : 0;
        if (cdInt == 3)      w.sr.setColor(Colors.c(180, 40, 30, 180));
        else if (cdInt == 2) w.sr.setColor(Colors.c(200, 150, 20, 180));
        else if (cdInt == 1) w.sr.setColor(Colors.c(40, 160, 50, 180));
        else                 w.sr.setColor(Colors.c(40, 200, 60, 180));
        w.sr.circle(300, 310, 52, 36);
        // Countdown circle inner dark
        w.sr.setColor(Colors.c(10, 10, 30, 220));
        w.sr.circle(300, 310, 44, 36);

        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // "BATTLE" header
        w.font.getData().setScale(1.8f, -1.8f);
        w.font.setColor(Colors.c(255, 215, 40));
        w.glyphLayout.setText(w.font, "BATTLE");
        w.font.draw(w.batch, "BATTLE", 300 - w.glyphLayout.width / 2, 43);

        // "YOU" label (player 1 side)
        w.font.getData().setScale(0.95f, -0.95f);
        w.font.setColor(Colors.c(130, 190, 255));
        w.glyphLayout.setText(w.font, "YOU");
        w.font.draw(w.batch, "YOU", 135 - w.glyphLayout.width / 2, 100);

        // Player 1 name
        String p1name = (w.profile != null && w.profile.username != null && !w.profile.username.isEmpty())
            ? w.profile.username : "You";
        w.font.getData().setScale(1.3f, -1.3f);
        w.font.setColor(Color.WHITE);
        w.glyphLayout.setText(w.font, p1name);
        // Trim if too wide
        String p1disp = p1name;
        while (w.glyphLayout.width > 205 && p1disp.length() > 4) {
            p1disp = p1disp.substring(0, p1disp.length() - 1);
            w.glyphLayout.setText(w.font, p1disp + ".");
        }
        if (!p1disp.equals(p1name)) p1disp += ".";
        w.glyphLayout.setText(w.font, p1disp);
        w.font.draw(w.batch, p1disp, 135 - w.glyphLayout.width / 2, 145);

        // Player 1 trophies
        if (w.profile != null) {
            w.font.getData().setScale(0.9f, -0.9f);
            w.font.setColor(Colors.c(255, 210, 40));
            String tStr = "" + w.profile.trophies + " trophies";
            w.glyphLayout.setText(w.font, tStr);
            w.font.draw(w.batch, tStr, 135 - w.glyphLayout.width / 2, 175);
        }

        // "VS" label in center circle
        w.font.getData().setScale(1.1f, -1.1f);
        w.font.setColor(Colors.c(255, 210, 40));
        w.glyphLayout.setText(w.font, "VS");
        w.font.draw(w.batch, "VS", 300 - w.glyphLayout.width / 2, 165);

        // "OPPONENT" label (player 2 side)
        w.font.getData().setScale(0.95f, -0.95f);
        w.font.setColor(Colors.c(255, 145, 120));
        w.glyphLayout.setText(w.font, "OPPONENT");
        w.font.draw(w.batch, "OPPONENT", 465 - w.glyphLayout.width / 2, 100);

        // Player 2 name
        String p2name = (w.player2DisplayName != null && !w.player2DisplayName.isEmpty())
            ? w.player2DisplayName : "Opponent";
        w.font.getData().setScale(1.3f, -1.3f);
        w.font.setColor(Color.WHITE);
        w.glyphLayout.setText(w.font, p2name);
        String p2disp = p2name;
        while (w.glyphLayout.width > 205 && p2disp.length() > 4) {
            p2disp = p2disp.substring(0, p2disp.length() - 1);
            w.glyphLayout.setText(w.font, p2disp + ".");
        }
        if (!p2disp.equals(p2name)) p2disp += ".";
        w.glyphLayout.setText(w.font, p2disp);
        w.font.draw(w.batch, p2disp, 465 - w.glyphLayout.width / 2, 145);

        // Countdown number or GO!
        String countStr;
        if (ct <= 0.4f) {
            countStr = "GO!";
            w.font.setColor(Colors.c(80, 255, 100));
        } else {
            int cd = (int) Math.ceil(ct);
            countStr = "" + cd;
            if (cd == 3)      w.font.setColor(Colors.c(255, 100, 80));
            else if (cd == 2) w.font.setColor(Colors.c(255, 210, 50));
            else              w.font.setColor(Colors.c(80, 230, 80));
        }
        w.font.getData().setScale(3.5f, -3.5f);
        w.glyphLayout.setText(w.font, countStr);
        w.font.draw(w.batch, countStr, 300 - w.glyphLayout.width / 2, 350);

        // Reset font
        w.font.getData().setScale(1f, -1f);
        w.font.setColor(Color.WHITE);
        w.batch.end();
    }

    /** Renders the matchmaking "Finding opponent..." screen. */
    void renderMatchmaking() {
        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        w.sr.setColor(Colors.c(4, 8, 28, 255));
        w.sr.rect(0, 0, 600, 400);

        // Pulsing centre panel
        float pulse = (float)(Math.sin(w.elapsedTime * 3.0) * 0.5 + 0.5);
        w.sr.setColor(Colors.c(10, 22, 70, 220));
        fillRoundRect(150, 140, 300, 120, 12);

        // Animated spinner dots (3 dots cycling alpha)
        float t = w.elapsedTime * 2.5f;
        for (int i = 0; i < 3; i++) {
            float phase = (t - i * 0.4f) % 3.0f;
            float alpha = (phase < 1f) ? phase : (phase < 2f) ? 1f : Math.max(0f, 3f - phase);
            w.sr.setColor(Colors.c(80, 160, 255, (int)(alpha * 200)));
            w.sr.circle(270 + i * 30, 230, 6);
        }

        // Progress bar (8-second countdown)
        float progress = Math.min(1f, w.matchmakingTimer / 8f);
        w.sr.setColor(Colors.c(30, 50, 100, 180));
        w.sr.rect(160, 245, 280, 6);
        if (progress > 0.75f)     w.sr.setColor(Colors.c(255, 80, 60, 200));
        else if (progress > 0.4f) w.sr.setColor(Colors.c(255, 200, 50, 200));
        else                      w.sr.setColor(Colors.c(60, 180, 255, 200));
        w.sr.rect(160, 245, 280 * progress, 6);

        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // "FINDING OPPONENT" header
        w.font.setColor(Color.WHITE);
        w.font.getData().setScale(1.3f, -1.3f);
        w.glyphLayout.setText(w.font, "FINDING OPPONENT");
        w.font.draw(w.batch, "FINDING OPPONENT", 300 - w.glyphLayout.width / 2, 175);

        // Searching sub-text
        w.font.getData().setScale(0.85f, -0.85f);
        w.font.setColor(Colors.c(140, 180, 220));
        w.glyphLayout.setText(w.font, "Looking for a match...");
        w.font.draw(w.batch, "Looking for a match...", 300 - w.glyphLayout.width / 2, 215);

        // Cancel hint
        w.font.getData().setScale(0.75f, -0.75f);
        w.font.setColor(Colors.c(100, 110, 130));
        w.glyphLayout.setText(w.font, "Tap anywhere to cancel");
        w.font.draw(w.batch, "Tap anywhere to cancel", 300 - w.glyphLayout.width / 2, 300);

        w.font.getData().setScale(1f, -1f);
        w.font.setColor(Color.WHITE);
        w.batch.end();
    }

    /** Renders the Clash Royale-style main menu screen. */
    void renderMainMenu() {
        ensureActivityCache();
        int arenaIdx = (w.profile != null) ? ArenaConfig.getArenaIndex(w.profile.trophies) : 0;
        float pulse = 0.8f + 0.2f * (float)Math.sin(w.menuAnimationTimer * 0.05f);
        boolean dailyReady = (w.profile == null || w.profile.lastDailyChestMs == 0 ||
            (System.currentTimeMillis() - w.profile.lastDailyChestMs) >= 86400000L);

        // ===== SHAPE PASS: draw ALL filled shapes first =====
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        // --- Background ---
        w.sr.setColor(c(8, 10, 30));
        w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);

        // --- TOP BAR (y=0, h=CR_TOPBAR_H=34) ---
        w.sr.setColor(c(18, 24, 60));
        w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, CR_TOPBAR_H);
        // Glossy top highlight
        w.tmpColor.set(1f, 1f, 1f, 0.06f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, 3);
        // ? button (top-left, x=5-29, centered in bar)
        w.sr.setColor(c(60, 90, 180));
        w.sr.circle(17, 17, 11, 14);
        // Bottom edge separator
        w.tmpColor.set(CLR_GOLD.r, CLR_GOLD.g, CLR_GOLD.b, 0.4f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, CR_TOPBAR_H - 1, GameWorld.VIRTUAL_WIDTH, 1);
        // Trophy icon (small gold circle with outer ring)
        w.sr.setColor(CLR_GOLD_150);
        w.sr.circle(280, 17, 9, 14);
        w.sr.setColor(CLR_GOLD);
        w.sr.circle(280, 17, 7, 12);
        // Gold icon
        w.sr.setColor(Colors.c(255, 210, 40));
        w.sr.circle(390, 17, 5, 8);
        // Gem icon
        w.sr.setColor(Colors.c(80, 220, 255));
        w.sr.circle(448, 17, 5, 8);

        // --- ARENA PANEL (y=34, h=92) ---
        Color arenaColor = Colors.ARENA_THEMES[arenaIdx];
        // Base fill
        w.sr.setColor(arenaColor);
        w.sr.rect(0, CR_ARENA_Y, GameWorld.VIRTUAL_WIDTH, CR_ARENA_H);
        // Darken bottom to simulate gradient
        w.tmpColor.set(0f, 0f, 0f, 0.18f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, CR_ARENA_Y + 70, GameWorld.VIRTUAL_WIDTH, CR_ARENA_H - 70);
        // Lighten top strip for depth
        w.tmpColor.set(1f, 1f, 1f, 0.08f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, CR_ARENA_Y, GameWorld.VIRTUAL_WIDTH, 22);
        // Top accent line — brighter version of arena color
        w.tmpColor.set(Math.min(1f, arenaColor.r + 0.3f), Math.min(1f, arenaColor.g + 0.3f), Math.min(1f, arenaColor.b + 0.3f), 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, CR_ARENA_Y, GameWorld.VIRTUAL_WIDTH, 2);

        // Daily chest banner at bottom of arena panel
        if (dailyReady) {
            float dailyPulse = 0.6f + 0.4f * (float)Math.sin(w.menuAnimationTimer * 0.08f);
            w.sr.setColor(Colors.c((int)(40*dailyPulse), (int)(180*dailyPulse), (int)(40*dailyPulse)));
            w.sr.rect(0, CR_ARENA_Y + CR_ARENA_H - 16, 600, 16);
        }

        // --- BATTLE BUTTON (y=208, h=50, w=220, x=190) ---
        int battleX = (GameWorld.VIRTUAL_WIDTH - CR_BATTLE_W) / 2; // 190
        // Fixed subtle outer glow (doesn't pulse — avoids disappearing at dark phase)
        w.tmpColor.set(255/255f, 200/255f, 50/255f, 0.2f);
        w.sr.setColor(w.tmpColor);
        fillRoundRect(battleX - 6, CR_BATTLE_Y - 6, CR_BATTLE_W + 12, CR_BATTLE_H + 12, 12);
        // Main button — pulsing orange-gold
        w.tmpColor.set(
            Math.min(1f, CLR_ORANGE_GOLD.r * pulse),
            Math.min(1f, CLR_ORANGE_GOLD.g * pulse * 0.9f),
            CLR_ORANGE_GOLD.b * 0.2f, 1f);
        w.sr.setColor(w.tmpColor);
        fillRoundRect(battleX, CR_BATTLE_Y, CR_BATTLE_W, CR_BATTLE_H, 8);
        // Crisp top gloss strip
        w.sr.setColor(c(255, 255, 255, 50));
        fillRoundRect(battleX + 3, CR_BATTLE_Y + 3, CR_BATTLE_W - 6, 3, 2);

        // --- CHEST SLOTS (y=270, h=50) ---
        // 4 slots: width=58, gap=10, startX=169
        // Positions: 169, 237, 305, 373
        int[] chestX = {169, 237, 305, 373};
        Color[] chestColors = {
            c(80, 80, 85),   // silver
            c(200, 165, 0),  // gold
            c(100, 0, 180),  // magical
            c(0, 170, 200),  // arena
            c(35, 35, 40)    // empty
        };
        for (int i = 0; i < 4; i++) {
            String chestType = (w.profile != null) ? w.profile.chestSlots[i].type : null;
            Color chestColor;
            if (chestType == null) chestColor = chestColors[4];
            else if ("silver".equals(chestType)) chestColor = chestColors[0];
            else if ("gold".equals(chestType)) chestColor = chestColors[1];
            else if ("magical".equals(chestType)) chestColor = chestColors[2];
            else if ("arena".equals(chestType)) chestColor = chestColors[3];
            else chestColor = chestColors[4];

            w.sr.setColor(chestColor);
            fillRoundRect(chestX[i], CR_CHEST_Y, CR_CHEST_W, CR_CHEST_H, 6);
            // Border
            w.sr.setColor(c(255, 255, 255, 30));
            fillRoundRect(chestX[i], CR_CHEST_Y, CR_CHEST_W, 2, 3);
        }

        // --- SECONDARY BUTTONS (y=334, h=24) — 4 buttons: Collection, 2P Local, Practice, Settings ---
        int[][] secData4 = {{5, 138}, {148, 138}, {291, 138}, {434, 138}};
        for (int[] sd : secData4) {
            // Body
            w.sr.setColor(c(35, 55, 110));
            fillRoundRect(sd[0], CR_SECBTN_Y, sd[1], CR_SECBTN_H, 5);
            // Top gloss
            w.tmpColor.set(1f, 1f, 1f, 0.12f);
            w.sr.setColor(w.tmpColor);
            fillRoundRect(sd[0] + 2, CR_SECBTN_Y + 2, sd[1] - 4, CR_SECBTN_H / 2, 3);
            // Border
            w.tmpColor.set(100/255f, 150/255f, 255/255f, 0.4f);
            w.sr.setColor(w.tmpColor);
            fillRoundRect(sd[0], CR_SECBTN_Y, sd[1], 1, 3);
        }


        w.sr.end();

        // ===== TEXT PASS: draw ALL text in one SpriteBatch pass =====
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // --- TOP BAR text ---
        // ? button label
        w.font.setColor(Colors.c(200, 220, 255));
        w.glyphLayout.setText(w.font, "?");
        w.font.draw(w.batch, "?", 17 - w.glyphLayout.width / 2, 22);
        // "Click to learn to play" hint (right of ? button)
        w.font.setColor(Colors.c(140, 160, 200));
        w.font.draw(w.batch, "Click to learn to play", 32, 22);

        w.font.setColor(Color.WHITE);
        String trophyStr = (w.profile != null) ? ("" + w.profile.trophies) : "0";
        w.glyphLayout.setText(w.font, trophyStr);
        w.font.draw(w.batch, trophyStr, 292, 22); // right of trophy icon at x=280

        // Username — right-aligned in top bar
        String topBarName = (w.profile != null && w.profile.username != null && !w.profile.username.isEmpty())
            ? w.profile.username : "Player";
        w.font.setColor(Color.WHITE);
        w.glyphLayout.setText(w.font, topBarName);
        w.font.draw(w.batch, topBarName, GameWorld.VIRTUAL_WIDTH - w.glyphLayout.width - 8, 22);

        // Gold display
        w.font.setColor(Colors.c(255, 210, 40));
        w.font.draw(w.batch, (w.profile != null ? w.profile.gold : 0) + "g", 397, 21);
        // Gems display
        w.font.setColor(Colors.c(80, 220, 255));
        w.font.draw(w.batch, "" + (w.profile != null ? w.profile.gems : 0), 455, 21);

        // --- ARENA PANEL text ---
        w.font.setColor(c(255, 255, 255, 180));
        w.font.draw(w.batch, "RECENT ACTIVITY", 12, CR_ARENA_Y + 18);

        // Activity lines (pre-computed by ensureActivityCache)
        int lineY = CR_ARENA_Y + 33;
        for (int i = 0; i < activityLines.length; i++) {
            boolean won = activityLines[i].startsWith("W");
            w.font.setColor(won ? CLR_GREEN_BRIGHT : c(220, 60, 60));
            w.font.draw(w.batch, activityLines[i], 12, lineY);
            lineY += 13;
        }
        if (activityLines.length == 0) {
            w.font.setColor(c(150, 150, 150));
            w.font.draw(w.batch, "No recent matches", 12, CR_ARENA_Y + 33);
        }

        // Daily chest banner text
        if (dailyReady) {
            w.font.setColor(Colors.c(255, 255, 255));
            w.glyphLayout.setText(w.font, "FREE CHEST! Tap to claim");
            w.font.draw(w.batch, "FREE CHEST! Tap to claim", 300 - w.glyphLayout.width / 2, CR_ARENA_Y + CR_ARENA_H - 3);
        }

        // --- BATTLE BUTTON text ---
        int battleX2 = (GameWorld.VIRTUAL_WIDTH - CR_BATTLE_W) / 2;
        w.font.setColor(Color.WHITE);
        w.glyphLayout.setText(w.font, "BATTLE!");
        w.font.draw(w.batch, "BATTLE!",
            battleX2 + (CR_BATTLE_W - w.glyphLayout.width) / 2, CR_BATTLE_Y + 26);

        // --- CHEST SLOTS text + sprites ---
        int[] chestX2 = {169, 237, 305, 373};
        String[] chestTypeOrder = {"silver", "gold", "magical", "arena"};
        for (int i = 0; i < 4; i++) {
            String chestType = (w.profile != null) ? w.profile.chestSlots[i].type : null;
            if (chestType == null) {
                w.font.setColor(c(80, 80, 80));
                w.glyphLayout.setText(w.font, "Empty");
                w.font.draw(w.batch, "Empty",
                    chestX2[i] + (CR_CHEST_W - w.glyphLayout.width) / 2, CR_CHEST_Y + 36);
            } else {
                // Draw chest sprite if available, otherwise fall back to text label
                int chestIdx = -1;
                for (int ci = 0; ci < chestTypeOrder.length; ci++) {
                    if (chestTypeOrder[ci].equals(chestType)) { chestIdx = ci; break; }
                }
                if (chestIdx >= 0 && chestIdx < w.texChests.length && w.texChests[chestIdx] != null) {
                    int sw = CR_CHEST_W - 4, sh = CR_CHEST_H - 4;
                    int sx = chestX2[i] + 2;
                    // Draw flipped vertically (Y-down camera causes textures to appear upside-down)
                    w.batch.draw(w.texChests[chestIdx], sx, CR_CHEST_Y + 2 + sh, sw, -sh);
                }
                String timeStr = ChestSystem.getChestTimeRemaining(w.profile.chestSlots[i]);
                boolean ready = "Ready!".equals(timeStr);
                w.font.getData().setScale(0.75f, -0.75f);
                w.font.setColor(ready ? CLR_GREEN_BRIGHT : Color.WHITE);
                w.glyphLayout.setText(w.font, timeStr);
                w.font.draw(w.batch, timeStr,
                    chestX2[i] + (CR_CHEST_W - w.glyphLayout.width) / 2, CR_CHEST_Y + 62);
                w.font.getData().setScale(1f, -1f);
            }
        }

        // --- SECONDARY BUTTONS text (4 buttons) ---
        w.font.setColor(Color.WHITE);
        String[] secLabels4 = {"Collection", "2P Local", "Practice", "Settings"};
        int[] secX4 = {5, 148, 291, 434};
        int[] secW4 = {138, 138, 138, 138};
        for (int i = 0; i < 4; i++) {
            w.glyphLayout.setText(w.font, secLabels4[i]);
            w.font.draw(w.batch, secLabels4[i],
                secX4[i] + (secW4[i] - w.glyphLayout.width) / 2,
                CR_SECBTN_Y + (CR_SECBTN_H + w.glyphLayout.height) / 2);
        }


        w.batch.end();

        // Slide deck overlay — must be last so it renders on top of the main menu
        renderSlideDeck();
    }

    /** Check if a virtual-coordinate click hits a CR menu element.
     *  Returns: 0=Battle, 1=Collection, 2=2P Local, 3=Practice, 4=Settings,
     *           10-13=chest slots, 20=arena panel tap, 30=daily chest banner, -1=miss */
    public int getMenuButtonAt(float vx, float vy) {
        // ? (learn to play) button: top-left circle x=6-28, y=0-34
        if (vx >= 5 && vx <= 160 && vy >= 0 && vy <= CR_TOPBAR_H) return 40;

        // BATTLE button: x=190-410, y=208-258
        if (vx >= 190 && vx <= 410 && vy >= CR_BATTLE_Y && vy <= CR_BATTLE_Y + CR_BATTLE_H) return 0;

        // Secondary buttons (4 buttons): y=334-358
        if (vy >= CR_SECBTN_Y && vy <= CR_SECBTN_Y + CR_SECBTN_H) {
            if (vx >= 5   && vx <= 143)  return 1; // Collection
            if (vx >= 148 && vx <= 286)  return 2; // 2P Local
            if (vx >= 291 && vx <= 429)  return 3; // Practice
            if (vx >= 434 && vx <= 572)  return 4; // Settings
        }

        // Chest slots: y=270-320
        if (vy >= CR_CHEST_Y && vy <= CR_CHEST_Y + CR_CHEST_H) {
            int[] chestX = {169, 237, 305, 373};
            for (int i = 0; i < 4; i++) {
                if (vx >= chestX[i] && vx <= chestX[i] + CR_CHEST_W) return 10 + i;
            }
        }

        // Daily chest banner (bottom of arena panel — check FIRST)
        int dailyBannerY = CR_ARENA_Y + CR_ARENA_H - 16;
        if (vy >= dailyBannerY && vy <= CR_ARENA_Y + CR_ARENA_H && vx >= 0 && vx <= 600) return 30;

        // Arena panel tap
        if (vy >= CR_ARENA_Y && vy < dailyBannerY && vx >= 0 && vx <= 600) return 20;

        return -1;
    }

    // ==================== GAMEPLAY RENDERING METHODS ====================

    void drawBackground() {
        // Pure dark navy base gradient — skip if arena bg texture is loaded
        int bgIdx = (w.profile != null) ? com.pongus.game.ArenaConfig.getArenaIndex(w.profile.trophies) : 0;
        if (w.arenaBgTextures[bgIdx] == null) {
            w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT,
                CLR_BG_GAME2, CLR_BG_GAME2,
                CLR_BG_GAME1, CLR_BG_GAME1);
        }

        // Top HUD panel — only draw full black overlay when no arena bg texture
        int _bgIdx = (w.profile != null) ? com.pongus.game.ArenaConfig.getArenaIndex(w.profile.trophies) : 0;
        if (w.arenaBgTextures[_bgIdx] == null) {
            w.tmpColor.set(0f, 0f, 0f, 0.45f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, 92);
            w.tmpColor.set(100/255f, 150/255f, 255/255f, 35/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(0, 91, GameWorld.VIRTUAL_WIDTH, 1);
        }

        // Edge ownership strips — 2px, player colors
        w.sr.setColor(CLR_CYAN_80);
        w.sr.rect(0, 0, 2, GameWorld.VIRTUAL_HEIGHT);
        w.sr.setColor(CLR_LIGHT_RED_80);
        w.sr.rect(598, 0, 2, GameWorld.VIRTUAL_HEIGHT);
    }

    void drawCenterLine() {
        // Core dashes — 8px wide, pale blue, 12px dash / 8px gap
        w.sr.setColor(CLR_PALE_BLUE);
        for (int i = 0; i < GameWorld.VIRTUAL_HEIGHT; i += 20) w.sr.rect(296, i, 8, 12);
        // Bright center strip — 2px, white-ish, inset
        w.sr.setColor(CLR_WHITE_120);
        for (int i = 0; i < GameWorld.VIRTUAL_HEIGHT; i += 20) w.sr.rect(299, i + 2, 2, 8);
    }

    void drawScorePanel() {
        // No background boxes — scores use drop shadows instead
    }

    void drawMapModifiers() {
        // Center wall
        if (w.centerWallActive) {
            w.sr.setColor(CLR_VIOLET_180);
            w.sr.rect(295, 0, 10, w.centerWallGapY - w.centerWallGapSize / 2);
            w.sr.rect(295, w.centerWallGapY + w.centerWallGapSize / 2, 10,
                400 - (w.centerWallGapY + w.centerWallGapSize / 2));
        }
        // Danger zone
        if (w.dangerZoneActive) {
            int pulseEffect = (int)(Math.sin(w.elapsedTime * 3.33) * 5);
            for (int i = 3; i >= 1; i--) {
                w.tmpColor.set(1f, 100/255f, 0f, 15 * i / 255f);
                w.sr.setColor(w.tmpColor);
                int r = w.dangerZoneRadius + pulseEffect + i * 5;
                w.sr.circle(w.dangerZoneCenterX, w.dangerZoneCenterY, r, 24);
            }
            drawRadialGradient(w.dangerZoneCenterX, w.dangerZoneCenterY,
                w.dangerZoneRadius + pulseEffect, CLR_FIRE_FADE0, CLR_FIRE_FADE60, 8);
        }
        // Gravity visual (purple wave lines)
        if (w.gravityActive) {
            w.sr.setColor(CLR_PURPLE_GLOW);
            for (int i = 0; i < 600; i += 40) {
                float sinVal = (float)Math.sin((w.elapsedTime * 5 + i * 0.01f));
                w.sr.rect(i, 28 + sinVal * 10, 4, 4);
                w.sr.rect(i, 198 + sinVal * 10, 4, 4);
                w.sr.rect(i, 368 + sinVal * 10, 4, 4);
            }
        }
        // Invisible walls
        if (w.invisibleWallsActive) {
            for (int i = 0; i < 600; i += 20) {
                float alpha = (50 + 30 * (float)Math.sin(w.elapsedTime * 6.67f + i * 0.033f)) / 255f;
                w.tmpColor.set(100/255f, 200/255f, 1f, alpha);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(i, w.invisibleWallY - 2, 15, 4);
            }
        }
    }

    void drawShadowClones() {
        // Shadow clone: independent small paddle that tracks the ball, grows with level
        int lv1 = w.getEffectiveAbilityLevel(1, "shadow_clone");
        if (lv1 > 0) {
            int sh = w.getShadowSize(1);
            int sy = w.player1IndependentShadowY;
            // Glow halo
            w.tmpColor.set(0f, 180/255f, 255/255f, 40/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(4, sy - 2, 16, sh + 4);
            // Semi-transparent core (reads as ghost of the real paddle)
            w.tmpColor.set(80/255f, 180/255f, 255/255f, 160/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(8, sy, 8, sh);
            // Bright ball-facing edge
            w.sr.setColor(CLR_CYAN);
            w.sr.rect(16, sy + 2, 2, sh - 4);
        }
        int lv2 = w.getEffectiveAbilityLevel(2, "shadow_clone");
        if (lv2 > 0) {
            int sh = w.getShadowSize(2);
            int sy = w.player2IndependentShadowY;
            // Glow halo
            w.tmpColor.set(255/255f, 60/255f, 60/255f, 40/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(580, sy - 2, 16, sh + 4);
            // Semi-transparent core
            w.tmpColor.set(255/255f, 100/255f, 100/255f, 160/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(584, sy, 8, sh);
            // Bright ball-facing edge
            w.tmpColor.set(255/255f, 180/255f, 180/255f, 1f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(582, sy + 2, 2, sh - 4);
        }
    }

    void drawPortals() {
        // Player 1 portals (blue/cyan)
        if (w.player1PortalEntranceX != null && w.player1PortalEntranceY != null && w.player1PortalPlacementStage >= 1) {
            drawPortal(w.player1PortalEntranceX, w.player1PortalEntranceY, 0f, CLR_BLUE_100_A100, CLR_BLUE_150_A200, CLR_CYAN);
        }
        if (w.player1PortalExitX != null && w.player1PortalExitY != null && w.player1PortalPlacementStage == 2) {
            drawPortal(w.player1PortalExitX, w.player1PortalExitY, (float)Math.PI, CLR_BLUE_100_A100, CLR_BLUE_150_A200, CLR_CYAN);
        }
        // Player 2 portals (purple)
        if (w.player2PortalEntranceX != null && w.player2PortalEntranceY != null && w.player2PortalPlacementStage >= 1) {
            drawPortal(w.player2PortalEntranceX, w.player2PortalEntranceY, 0f, CLR_PURPLE_GLOW, CLR_PURPLE_GLOW2, CLR_PURPLE_LIGHT);
        }
        if (w.player2PortalExitX != null && w.player2PortalExitY != null && w.player2PortalPlacementStage == 2) {
            drawPortal(w.player2PortalExitX, w.player2PortalExitY, (float)Math.PI, CLR_PURPLE_GLOW, CLR_PURPLE_GLOW2, CLR_PURPLE_LIGHT);
        }
        // Portal boost effect
        if (w.portalBoostTimer > 0) {
            float alpha = w.portalBoostTimer / 30f;
            w.tmpColor.set(1f, 1f, 0f, alpha);
            w.sr.setColor(w.tmpColor);
            int effectR = 30 - w.portalBoostTimer;
            w.sr.circle(w.ballX + 7, w.ballY + 7, effectR, 16);
        }
    }

    private void drawPortal(int cx, int cy, float phaseOffset, Color outer, Color inner, Color ring) {
        int pulseSize = (int)(5 * Math.sin(w.elapsedTime * 10 + phaseOffset));
        int radius = 25 + pulseSize;
        w.sr.setColor(outer);
        w.sr.circle(cx, cy, radius + 5, 20);
        w.sr.setColor(inner);
        w.sr.circle(cx, cy, radius, 20);
        // Swirl particles
        w.sr.setColor(ring);
        for (int i = 0; i < 8; i++) {
            double angle = (w.elapsedTime * 100 + i * 45) % 360;
            int px = cx + (int)(Math.cos(Math.toRadians(angle)) * 15);
            int py = cy + (int)(Math.sin(Math.toRadians(angle)) * 15);
            w.sr.circle(px, py, 2, 6);
        }
    }

    void drawBarriers() {
        if (w.player1BarrierActive) {
            w.tmpColor.set(0f, 200/255f, 1f, 100/255f);
            w.sr.setColor(w.tmpColor);
            fillRoundRect(w.player1BarrierX - 5, w.player1BarrierY - 5, w.player1BarrierWidth + 10, w.barrierHeight + 10, 10);
            w.sr.setColor(CLR_BLUE_150_A200);
            fillRoundRect(w.player1BarrierX, w.player1BarrierY, w.player1BarrierWidth, w.barrierHeight, 5);
        }
        if (w.player2BarrierActive) {
            w.sr.setColor(CLR_LIGHT_RED_100);
            fillRoundRect(w.player2BarrierX - 5, w.player2BarrierY - 5, w.player2BarrierWidth + 10, w.barrierHeight + 10, 10);
            w.tmpColor.set(1f, 100/255f, 100/255f, 200/255f);
            w.sr.setColor(w.tmpColor);
            fillRoundRect(w.player2BarrierX, w.player2BarrierY, w.player2BarrierWidth, w.barrierHeight, 5);
        }
    }

    void drawTraps() {
        if (w.player1TrapActive) {
            int branch = w.player1AbilityBranches.containsKey("trap") ? w.player1AbilityBranches.get("trap") : 0;
            int pulse = (int)(5 * Math.sin(w.elapsedTime * 5));
            w.sr.setColor(CLR_MAGENTA_50);
            w.sr.circle(w.player1TrapX, w.player1TrapY, w.trapSize/2f + 10 + pulse, 16);
            if (branch == 1) w.tmpColor.set(0f, 200/255f, 0f, 150/255f);
            else if (branch == 2) w.tmpColor.set(1f, 100/255f, 0f, 150/255f);
            else w.tmpColor.set(200/255f, 0f, 200/255f, 150/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.player1TrapX, w.player1TrapY, w.trapSize/2f, 16);
        }
        if (w.player2TrapActive) {
            int branch = w.player2AbilityBranches.containsKey("trap") ? w.player2AbilityBranches.get("trap") : 0;
            int pulse = (int)(5 * Math.sin(w.elapsedTime * 5));
            w.sr.setColor(CLR_YELLOW_80);
            w.sr.circle(w.player2TrapX, w.player2TrapY, w.trapSize/2f + 10 + pulse, 16);
            if (branch == 1) w.tmpColor.set(0f, 1f, 200/255f, 150/255f);
            else if (branch == 2) w.tmpColor.set(1f, 50/255f, 50/255f, 150/255f);
            else w.tmpColor.set(1f, 1f, 0f, 150/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.player2TrapX, w.player2TrapY, w.trapSize/2f, 16);
        }
    }

    void drawHakiArmament() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        if (w.player1ArmamentActive) {
            w.tmpColor.set(30/255f, 0f, 50/255f, 150/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(5, w.paddle1Y - 10, 25, p1H + 20);
        }
        if (w.player2ArmamentActive) {
            w.tmpColor.set(30/255f, 0f, 50/255f, 150/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(570, w.paddle2Y - 10, 25, p2H + 20);
        }
    }

    void drawBankaiEffects() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        // Bankai aura — glowing rect slightly larger than paddle
        if (w.player1BankaiActive) {
            // Yellow/white aura for P1
            w.tmpColor.set(255/255f, 220/255f, 50/255f, 80/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(5, w.paddle1Y - 5, 20, p1H + 10);
            w.tmpColor.set(1f, 1f, 1f, 40/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(3, w.paddle1Y - 8, 24, p1H + 16);
        }
        if (w.player2BankaiActive) {
            // Red/orange aura for P2
            w.tmpColor.set(255/255f, 80/255f, 0f, 80/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(575, w.paddle2Y - 5, 20, p2H + 10);
            w.tmpColor.set(1f, 0f, 0f, 40/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(573, w.paddle2Y - 8, 24, p2H + 16);
        }
    }

    void drawHammerEffect() {
        if (w.player1HammerActive && w.player1HammerDuration > 0) {
            w.tmpColor.set(1f, 200/255f, 0f, 100/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(0, 0, 300, 400);
        }
        if (w.player2HammerActive && w.player2HammerDuration > 0) {
            w.tmpColor.set(1f, 200/255f, 0f, 100/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(300, 0, 300, 400);
        }
    }

    void drawPaddles() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);

        // Check elastic expansion
        boolean elastic1Active = false, elastic2Active = false;
        if (w.getEffectiveAbilityLevel(1, "paddle_growth") >= 3) {
            int br = w.player1AbilityBranches.containsKey("paddle_growth") ? w.player1AbilityBranches.get("paddle_growth") : 0;
            if (br == 2 && Math.abs(w.ballY + 7 - (w.paddle1Y + p1H/2)) < 50 && w.ballVelX < 0 && w.ballX < 300)
                elastic1Active = true;
        }
        if (w.getEffectiveAbilityLevel(2, "paddle_growth") >= 3) {
            int br = w.player2AbilityBranches.containsKey("paddle_growth") ? w.player2AbilityBranches.get("paddle_growth") : 0;
            if (br == 2 && Math.abs(w.ballY + 7 - (w.paddle2Y + p2H/2)) < 50 && w.ballVelX > 0 && w.ballX > 300)
                elastic2Active = true;
        }

        // Player 1 paddle
        {
            if (w.player1BankaiActive) {
                // Bankai: dark purple aura + dark core
                w.tmpColor.set(50/255f, 0f, 80/255f, 160/255f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(2, w.paddle1Y - 8, 24, p1H + 16);
                w.sr.setColor(CLR_BG_PURPLE);
                w.sr.rect(10, w.paddle1Y, 10, p1H);
                // Purple edge
                w.tmpColor.set(150/255f, 0f, 200/255f, 1f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(20, w.paddle1Y + 2, 2, p1H - 4);
            } else if (w.texPaddle1 != null) {
                // Custom sprite — no shape drawn, sprite handles all visuals in batch pass
            } else {
                if (elastic1Active) {
                    // Elastic: bright green outer flash
                    w.sr.setColor(CLR_SPRING_GREEN);
                    w.sr.rect(4, w.paddle1Y - 4, 18, p1H + 8);
                }
                // Glow
                w.tmpColor.set(0f, 180/255f, 255/255f, 50/255f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(0, w.paddle1Y - 2, 36, p1H + 4);
                // Core
                w.sr.setColor(CLR_SKY_BLUE);
                w.sr.rect(0, w.paddle1Y, 32, p1H);
                // Ball-facing bright edge (right side, 2px)
                w.sr.setColor(CLR_CYAN);
                w.sr.rect(32, w.paddle1Y, 2, p1H);
            }
        }

        // Player 2 paddle
        {
            if (w.player2BankaiActive) {
                // Bankai: dark purple aura + dark core
                w.tmpColor.set(50/255f, 0f, 80/255f, 160/255f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(574, w.paddle2Y - 8, 24, p2H + 16);
                w.sr.setColor(CLR_BG_PURPLE);
                w.sr.rect(580, w.paddle2Y, 10, p2H);
                // Purple edge
                w.tmpColor.set(150/255f, 0f, 200/255f, 1f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(578, w.paddle2Y + 2, 2, p2H - 4);
            } else if (w.texPaddle2 != null) {
                // Custom sprite — no shape drawn, sprite handles all visuals in batch pass
            } else {
                if (elastic2Active) {
                    // Elastic: bright orange outer flash
                    w.tmpColor.set(1f, 130/255f, 0f, 120/255f);
                    w.sr.setColor(w.tmpColor);
                    w.sr.rect(578, w.paddle2Y - 4, 18, p2H + 8);
                }
                // Glow
                w.tmpColor.set(255/255f, 60/255f, 60/255f, 50/255f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(564, w.paddle2Y - 2, 36, p2H + 4);
                // Core
                w.sr.setColor(CLR_LIGHT_RED);
                w.sr.rect(568, w.paddle2Y, 32, p2H);
                // Ball-facing bright edge (left side, 2px)
                w.tmpColor.set(255/255f, 170/255f, 170/255f, 1f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(566, w.paddle2Y, 2, p2H);
            }
        }
    }

    /** Draw paddle sprites in batch pass (Phase 9). Called from within w.batch.begin()/end(). */
    void drawPaddleSprites() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        if (w.texPaddle1 != null && !w.player1BankaiActive) {
            // Center the sprite on the left wall: half off-screen, half in-play
            w.batch.draw(w.texPaddle1, -24, w.paddle1Y, 72, p1H);
        }
        if (w.texPaddle2 != null && !w.player2BankaiActive) {
            w.batch.draw(w.texPaddle2, 548, w.paddle2Y, 80, p2H);
        }
    }

    /** Draw ball sprite in batch pass (Phase 9). Called from within w.batch.begin()/end(). */
    void drawBallSprite() {
        boolean ghostActive = (w.player1GhostEffectTimer > 0 || w.player2GhostEffectTimer > 0);
        if (ghostActive) return; // ball is invisible during ghost effect
        if (w.fireballActive && w.texBallFire != null) {
            w.batch.draw(w.texBallFire, w.ballX - 4, w.ballY - 4, 22, 22);
        } else if (w.texBall != null) {
            w.batch.draw(w.texBall, w.ballX - 4, w.ballY - 4, 22, 22);
        }
    }

    void drawBallEffects() {
        // Spectral echo ghost trail
        if (w.player1SpectralActive > 0 || w.player2SpectralActive > 0) {
            for (int i = 1; i <= 5; i++) {
                int tx = w.ballX - w.ballVelX * i;
                int ty = w.ballY - w.ballVelY * i;
                w.tmpColor.set(150/255f, 150/255f, 1f, (150 - i * 30) / 255f);
                w.sr.setColor(w.tmpColor);
                w.sr.circle(tx + 7, ty + 7, 7.5f, 12);
            }
        }
        // Danger zone particles
        float dangerRatio = w.dangerZoneTime / (float)w.maxDangerTime;
        if (w.dangerZoneTime > 20) {
            for (int i = 0; i < 8; i++) {
                double angle = w.elapsedTime * 10 + i * Math.PI / 4;
                int dist = 10 + (int)(Math.random() * 8);
                int px = w.ballX + 7 + (int)(Math.cos(angle) * dist);
                int py = w.ballY + 7 + (int)(Math.sin(angle) * dist);
                w.tmpColor.set(1f, (float)(Math.random() * 200/255f), 0f, (float)(150 * dangerRatio * Math.random()) / 255f);
                w.sr.setColor(w.tmpColor);
                int ps = 3 + (int)(Math.random() * 4);
                w.sr.circle(px, py, ps / 2f, 6);
            }
        }
        // Fireball
        if (w.fireballActive) {
            for (int i = 0; i < 12; i++) {
                double angle = w.elapsedTime * 12.5 + i * Math.PI / 6;
                int dist = 12 + (int)(Math.random() * 10);
                int px = w.ballX + 7 + (int)(Math.cos(angle) * dist);
                int py = w.ballY + 7 + (int)(Math.sin(angle) * dist);
                w.tmpColor.set(1f, (float)(Math.random() * 100/255f), 0f, (float)(180 * Math.random()) / 255f);
                w.sr.setColor(w.tmpColor);
                int ps = 4 + (int)(Math.random() * 5);
                w.sr.circle(px, py, ps / 2f, 6);
            }
            w.tmpColor.set(1f, 200/255f, 0f, 100/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.ballX + 7, w.ballY + 7, 17, 16);
        }
        // Teleport sparkles
        if (w.teleportActive) {
            w.sr.setColor(CLR_BLUE_VIOLET_150);
            for (int i = 0; i < 8; i++) {
                double angle = w.elapsedTime * 8.33 + i * Math.PI / 4;
                int dist = 15 + (int)(Math.sin(w.elapsedTime * 10 + i) * 8);
                int px = w.ballX + 7 + (int)(Math.cos(angle) * dist);
                int py = w.ballY + 7 + (int)(Math.sin(angle) * dist);
                w.sr.circle(px, py, 2.5f, 6);
            }
        }
        // Zigzag lightning
        if (w.zigzagActive) {
            w.sr.setColor(CLR_NEON_GREEN);
            int prevX = w.ballX + 7, prevY = w.ballY + 7;
            for (int i = 0; i < 4; i++) {
                int nx = prevX + (int)(Math.random() * 20 - 10);
                int ny = prevY + (int)(Math.random() * 20 - 10);
                drawThickLine(prevX, prevY, nx, ny, 2);
                prevX = nx; prevY = ny;
            }
        }
        // Wraith balls
        if (w.wraithActive) {
            for (int wi = 0; wi < w.wraithBalls.size(); wi++) {
                GhostBall wb = w.wraithBalls.get(wi);
                w.sr.setColor(CLR_GREEN_BRIGHT_100);
                for (int i = 0; i < 5; i++) {
                    double angle = w.elapsedTime * 8.33 + i * Math.PI * 2 / 5;
                    int dist = 12 + (int)(Math.sin(w.elapsedTime * 6.67) * 4);
                    w.sr.circle(wb.x + 7 + (int)(Math.cos(angle) * dist),
                              wb.y + 7 + (int)(Math.sin(angle) * dist), 2.5f, 6);
                }
                w.tmpColor.set(200/255f, 1f, 200/255f, 80/255f);
                w.sr.setColor(w.tmpColor);
                w.sr.circle(wb.x + 7, wb.y + 7, 12, 16);
                w.sr.setColor(CLR_GREEN_BRIGHT);
                w.sr.circle(wb.x + 7, wb.y + 7, 7.5f, 12);
            }
        }
        // Poltergeist
        if (w.poltergeistActive) {
            for (int i = 0; i < 10; i++) {
                int dist = 10 + (int)(Math.random() * 25);
                double angle = Math.random() * Math.PI * 2;
                int px = w.ballX + 7 + (int)(Math.cos(angle) * dist);
                int py = w.ballY + 7 + (int)(Math.sin(angle) * dist);
                w.tmpColor.set(1f, 100/255f, 1f, (80 + (int)(Math.random() * 80)) / 255f);
                w.sr.setColor(w.tmpColor);
                w.sr.circle(px, py, 3, 6);
            }
            w.tmpColor.set(1f, 50/255f, 1f, 60/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.ballX + 7, w.ballY + 7, 17, 16);
        }
    }

    void drawBall() {
        boolean ghostActive = (w.player1GhostEffectTimer > 0 || w.player2GhostEffectTimer > 0);
        if (ghostActive) return; // ball is invisible during ghost effect

        float dangerRatio = w.dangerZoneTime / (float)w.maxDangerTime;

        // Check tunnel vision hiding
        boolean ballHidden = false;
        if (w.player1TunnelTimer > 0 && (w.ballX < 100 || w.ballY < 80 || w.ballY > 320)) ballHidden = true;
        if (w.player2TunnelTimer > 0 && (w.ballX > 500 || w.ballY < 80 || w.ballY > 320)) ballHidden = true;

        if (ballHidden) return;

        // Ball color based on active effects
        Color ballCenter, ballOuter;
        if (w.fireballActive) { ballCenter = c(255, 250, 200); ballOuter = c(255, 60, 0, 200); }
        else if (dangerRatio > 0.5f) { ballCenter = c(255, 255, 200); ballOuter = c(200, 50, 0); }
        else { ballCenter = Color.WHITE; ballOuter = c(80, 100, 220, 180); }

        if (w.texBall == null || w.fireballActive) {
            // No custom texture — draw shape glow + solid ball
            w.tmpColor.set(ballCenter.r, ballCenter.g, ballCenter.b, 50/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.ballX + 7, w.ballY + 7, 16, 16);
            drawRadialGradient(w.ballX + 7, w.ballY + 7, 7.5f, ballCenter, ballOuter, 6);
        }

        // Haki ball aura
        if (w.player1HakiPhaseActive || w.player2HakiPhaseActive) {
            w.tmpColor.set(60/255f, 0f, 80/255f, 120/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(w.ballX + 7, w.ballY + 7, 12, 16);
        }
    }

    void drawProjectiles() {
        // Bullets
        for (int i = 0; i < w.bullets.size(); i++) {
            Bullet b = w.bullets.get(i);
            w.sr.setColor(b.owner == 1 ? CLR_CYAN : CLR_LIGHT_RED);
            w.sr.circle(b.x, b.y, 5, 10);
            w.sr.setColor(Color.WHITE);
            w.sr.circle(b.x, b.y, 2, 6);
        }
        // Vegeta bullets
        for (int i = 0; i < w.vegetaBullets.size(); i++) {
            VegetaBullet vb = w.vegetaBullets.get(i);
            w.sr.setColor(CLR_PURPLE_100);
            w.sr.circle(vb.x, vb.y, 15, 16);
            w.sr.setColor(CLR_PURPLE_LIGHT2);
            w.sr.circle(vb.x, vb.y, 12, 12);
            w.tmpColor.set(1f, 1f, 100/255f, 1f);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(vb.x, vb.y, 8, 10);
            w.sr.setColor(Color.WHITE);
            w.sr.circle(vb.x, vb.y, 4, 8);
        }
        // Explosions
        for (int i = 0; i < w.explosions.size(); i++) {
            Explosion exp = w.explosions.get(i);
            float a = exp.duration / 30f;
            w.tmpColor.set(200/255f, 0f, 1f, 80/255f * a);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(exp.x, exp.y, exp.radius, 16);
            w.tmpColor.set(1f, 1f, 100/255f, 200/255f * a);
            w.sr.setColor(w.tmpColor);
            w.sr.circle(exp.x, exp.y, exp.radius / 2f, 12);
        }
        // Lasers (Goku)
        for (int i = 0; i < w.activeLasers.size(); i++) {
            Laser l = w.activeLasers.get(i);
            Color lc = l.owner == 1 ? CLR_CYAN : CLR_LIGHT_RED;
            w.tmpColor.set(lc.r, lc.g, lc.b, 30/255f);
            w.sr.setColor(w.tmpColor);
            drawThickLine(l.startX, l.startY, l.endX, l.endY, 80);
            w.tmpColor.set(lc.r, lc.g, lc.b, 150/255f);
            w.sr.setColor(w.tmpColor);
            drawThickLine(l.startX, l.startY, l.endX, l.endY, 50);
            w.sr.setColor(Color.WHITE);
            drawThickLine(l.startX, l.startY, l.endX, l.endY, 25);
        }
        // Frieza lasers
        for (int i = 0; i < w.freezaLasers.size(); i++) {
            FreezaLaser fl = w.freezaLasers.get(i);
            w.tmpColor.set(CLR_PURPLE.r, CLR_PURPLE.g, CLR_PURPLE.b, 30/255f);
            w.sr.setColor(w.tmpColor);
            drawThickLine(fl.startX, fl.startY, fl.endX, fl.endY, fl.thickness * 8);
            w.sr.setColor(CLR_PINK_LIGHT);
            drawThickLine(fl.startX, fl.startY, fl.endX, fl.endY, fl.thickness * 2.5f);
            w.sr.setColor(Color.WHITE);
            drawThickLine(fl.startX, fl.startY, fl.endX, fl.endY, fl.thickness);
        }
        // Jiren bullets
        for (int i = 0; i < w.jirenBullets.size(); i++) {
            JirenBullet jb = w.jirenBullets.get(i);
            w.sr.setColor(CLR_PURPLE_BRIGHT);
            w.sr.circle(jb.x, jb.y, 20, 16);
            w.sr.setColor(CLR_PURPLE_BRIGHT3);
            w.sr.circle(jb.x, jb.y, 10, 12);
            w.sr.setColor(Color.WHITE);
            w.sr.circle(jb.x, jb.y, 2, 6);
        }
        // Sword swings
        if (w.player1SwordSwingTimer > 0) {
            int p = w.swordSwingDuration - w.player1SwordSwingTimer;
            double a = -Math.PI/4 + (p / (double)w.swordSwingDuration) * Math.PI/2;
            int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
            int ex = 25 + (int)(Math.cos(a) * 80);
            int ey = w.paddle1Y + p1H/2 + (int)(Math.sin(a) * 80);
            w.tmpColor.set(50/255f, 0f, 80/255f, 150/255f);
            w.sr.setColor(w.tmpColor);
            drawThickLine(25, w.paddle1Y + p1H/2, ex, ey, 8);
            w.sr.setColor(Color.BLACK);
            drawThickLine(25, w.paddle1Y + p1H/2, ex, ey, 4);
            w.sr.setColor(CLR_DARK_RED);
            drawThickLine(25, w.paddle1Y + p1H/2, ex, ey, 2);
        }
        if (w.player2SwordSwingTimer > 0) {
            int p = w.swordSwingDuration - w.player2SwordSwingTimer;
            double a = Math.PI - (-Math.PI/4 + (p / (double)w.swordSwingDuration) * Math.PI/2);
            int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
            int ex = 575 + (int)(Math.cos(a) * 80);
            int ey = w.paddle2Y + p2H/2 + (int)(Math.sin(a) * 80);
            w.tmpColor.set(50/255f, 0f, 80/255f, 150/255f);
            w.sr.setColor(w.tmpColor);
            drawThickLine(575, w.paddle2Y + p2H/2, ex, ey, 8);
            w.sr.setColor(Color.BLACK);
            drawThickLine(575, w.paddle2Y + p2H/2, ex, ey, 4);
            w.sr.setColor(CLR_DARK_RED);
            drawThickLine(575, w.paddle2Y + p2H/2, ex, ey, 2);
        }
    }

    void drawStatusEffectsShapes() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        // Lag glitch shapes
        if (w.player1LagEffectTimer > 0) {
            w.sr.setColor(CLR_MAGENTA_50);
            for (int i = 0; i < 5; i++) {
                int ox = (int)(Math.random() * 10 - 5);
                int oy = (int)(Math.random() * 10 - 5);
                w.sr.rect(7 + ox, w.paddle1Y - 3 + oy, 16, p1H + 6);
            }
        }
        if (w.player2LagEffectTimer > 0) {
            w.sr.setColor(CLR_MAGENTA_50);
            for (int i = 0; i < 5; i++) {
                int ox = (int)(Math.random() * 10 - 5);
                int oy = (int)(Math.random() * 10 - 5);
                w.sr.rect(577 + ox, w.paddle2Y - 3 + oy, 16, p2H + 6);
            }
        }
        // Freeze ice effect
        if (w.player2FreezeActive > 0) {
            w.tmpColor.set(150/255f, 220/255f, 1f, 100/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(7, w.paddle1Y, 10, p1H);
        }
        if (w.player1FreezeActive > 0) {
            w.tmpColor.set(150/255f, 220/255f, 1f, 100/255f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(583, w.paddle2Y, 10, p2H);
        }
        // Flash time distortion
        if (w.player1FlashActive > 0 || w.player2FlashActive > 0) {
            w.tmpColor.set(1f, 1f, 0f, 30/255f);
            w.sr.setColor(w.tmpColor);
            for (int i = 0; i < 3; i++) {
                w.sr.circle(w.ballX + 7, w.ballY + 7, 30 + i * 15, 20);
            }
        }
    }

    void drawPowerUps() {
        for (int i = 0; i < w.powerUps.size(); i++) {
            PowerUp pu = w.powerUps.get(i);
            int pulse = (int)(Math.sin(w.elapsedTime * 5) * 3 + 3);
            Color glow = CLR_WHITE_80, main = Color.WHITE;

            if ("paddle".equals(pu.type)) { glow = CLR_YELLOW_80; main = Color.YELLOW; }
            else if ("slow".equals(pu.type)) { glow = CLR_CYAN_80; main = Color.CYAN; }
            else if ("speed".equals(pu.type)) { glow = CLR_RED_80; main = Color.RED; }
            else if ("reverse".equals(pu.type)) { glow = CLR_PURPLE_80; main = CLR_PURPLE; }
            else if ("giant".equals(pu.type)) { glow = CLR_GREEN_80; main = Color.GREEN; }
            else if ("shrink".equals(pu.type)) { glow = CLR_ORANGE_80; main = Color.ORANGE; }
            else if ("multiball".equals(pu.type)) { glow = CLR_HOT_PINK_80; main = CLR_HOT_PINK; }
            else if ("dangerzone".equals(pu.type)) { glow = CLR_ORANGE_80; main = CLR_ORANGE_RED; }
            else if ("teleport".equals(pu.type)) { glow = CLR_PURPLE_80; main = CLR_BLUE_VIOLET; }
            else if ("fireball".equals(pu.type)) { glow = CLR_RED_80; main = CLR_ORANGE_RED2; }
            else if ("zigzag".equals(pu.type)) { glow = CLR_GREEN_80; main = CLR_LIME; }
            else if ("centerwall".equals(pu.type)) { glow = CLR_PURPLE_80; main = CLR_VIOLET; }
            else if ("invisiblewalls".equals(pu.type)) { glow = CLR_CYAN_80; main = CLR_LIGHT_BLUE; }
            else if ("shrinkpaddles".equals(pu.type)) { glow = CLR_CRIMSON_80; main = CLR_CRIMSON; }

            w.sr.setColor(glow);
            w.sr.circle(pu.x + 10, pu.y + 10, 10 + pulse, 12);
            drawRadialGradient(pu.x + 10, pu.y + 10, 10, main, Color.BLACK, 6);
        }
    }

    /** Draws power-up icon textures in the batch pass (on top of shape glows). */
    void drawPowerUpSprites() {
        for (int i = 0; i < w.powerUps.size(); i++) {
            PowerUp pu = w.powerUps.get(i);
            com.badlogic.gdx.graphics.Texture tex = w.powerupTextures.get(pu.type);
            if (tex != null) {
                w.batch.draw(tex, pu.x - 6, pu.y - 6, 32, 32);
            }
        }
    }

    /** Blind overlay: permanent darkness over opponent's half. Called at end of Filled pass. */
    void drawBlindOverlay() {
        // player1BlindLevel > 0 → P1 drew blind → darken P2's side (right half, x=300-600)
        if (w.player1BlindLevel > 0) {
            float alpha = w.player1BlindLevel == 1 ? 0.30f : w.player1BlindLevel == 2 ? 0.60f : 0.80f;
            w.sr.setColor(0f, 0f, 0f, alpha);
            w.sr.rect(300, 0, 300, 400);
        }
        // player2BlindLevel > 0 → P2 drew blind → darken P1's side (left half, x=0-300)
        if (w.player2BlindLevel > 0) {
            float alpha = w.player2BlindLevel == 1 ? 0.30f : w.player2BlindLevel == 2 ? 0.60f : 0.80f;
            w.sr.setColor(0f, 0f, 0f, alpha);
            w.sr.rect(0, 0, 300, 400);
        }
    }

    void drawScreenOverlays() {
        // Time loop slow mo
        if (w.player1TimeLoopSlowMoTimer > 0 || w.player2TimeLoopSlowMoTimer > 0) {
            w.sr.setColor(CLR_LIGHT_BLUE_30);
            w.sr.rect(0, 0, 600, 400);
        }
        // Inversion
        if (w.player1InversionTimer > 0) {
            w.sr.setColor(CLR_MAGENTA_100);
            w.sr.rect(0, 0, 300, 400);
        }
        if (w.player2InversionTimer > 0) {
            w.sr.setColor(CLR_CYAN_100);
            w.sr.rect(300, 0, 300, 400);
        }
        // Tunnel vision darkness
        if (w.player1TunnelTimer > 0) {
            w.sr.setColor(CLR_BLACK_200);
            w.sr.rect(0, 0, 100, 400);
            w.sr.rect(0, 0, 300, 80);
            w.sr.rect(0, 320, 300, 80);
        }
        if (w.player2TunnelTimer > 0) {
            w.sr.setColor(CLR_BLACK_200);
            w.sr.rect(500, 0, 100, 400);
            w.sr.rect(300, 0, 300, 80);
            w.sr.rect(300, 320, 300, 80);
        }
        // Warp distortion
        if (w.player1WarpEffectTimer > 0) {
            w.sr.setColor(CLR_INDIGO_80);
            for (int i = 0; i < 10; i++) {
                int offset = (int)(10 * Math.sin(w.elapsedTime * 10 + i));
                w.sr.rect(0, i * 40 + offset, 300, 20);
            }
        }
        if (w.player2WarpEffectTimer > 0) {
            w.sr.setColor(CLR_CRIMSON_80);
            for (int i = 0; i < 10; i++) {
                int offset = (int)(10 * Math.sin(w.elapsedTime * 10 + i));
                w.sr.rect(300, i * 40 + offset, 300, 20);
            }
        }
    }

    void drawObservationLines() {
        // Haki Observation - trajectory prediction (dashed lines approximated)
        if (w.player1ObservationActive) {
            w.sr.setColor(CLR_GOLD_150);
            int px = w.ballX, py = w.ballY, pvx = w.ballVelX, pvy = w.ballVelY;
            for (int i = 0; i < 50 && px > 0 && px < 600; i++) {
                int nx = px + pvx, ny = py + pvy;
                if (ny < 0 || ny > 400) pvy = -pvy;
                if (i % 2 == 0) w.sr.line(px, py, nx, ny); // dashed
                px = nx; py = ny;
            }
        }
        if (w.player2ObservationActive) {
            w.sr.setColor(CLR_LIGHT_RED_150);
            int px = w.ballX, py = w.ballY, pvx = w.ballVelX, pvy = w.ballVelY;
            for (int i = 0; i < 50 && px > 0 && px < 600; i++) {
                int nx = px + pvx, ny = py + pvy;
                if (ny < 0 || ny > 400) pvy = -pvy;
                if (i % 2 == 0) w.sr.line(px, py, nx, ny);
                px = nx; py = ny;
            }
        }
    }

    void drawMagnetLines() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        if (w.player1MagnetActive && w.ballX < 300 && w.ballVelX < 0) {
            w.sr.setColor(CLR_LIGHT_BLUE_100);
            for (int i = 0; i < 3; i++)
                w.sr.line(20, w.paddle1Y + p1H/2, w.ballX + 7 - i * 5, w.ballY + 7 - i * 5);
        }
        if (w.player2MagnetActive && w.ballX > 300 && w.ballVelX > 0) {
            w.sr.setColor(CLR_LIGHT_RED_100);
            for (int i = 0; i < 3; i++)
                w.sr.line(580, w.paddle2Y + p2H/2, w.ballX + 7 - i * 5, w.ballY + 7 - i * 5);
        }
    }

    void drawScores() {
        int scoreY = w.storyModeActive ? 75 : 55;
        // Story mode header
        if (w.storyModeActive) {
            w.font.setColor(CLR_GOLD);
            String header = "STORY MODE - LEVEL " + w.storyModeLevel;
            w.glyphLayout.setText(w.font, header);
            w.font.draw(w.batch, header, 300 - w.glyphLayout.width / 2, 15);
        }
        // Scores — big with neon glow
        w.font.getData().setScale(2f, -2f);
        String s1 = "" + w.scorePlayer1;
        w.glyphLayout.setText(w.font, s1);
        float s1x = 240 - w.glyphLayout.width / 2;
        // Cyan glow layers (outermost to innermost)
        w.font.setColor(CLR_CYAN.r, CLR_CYAN.g, CLR_CYAN.b, 0.15f);
        w.font.draw(w.batch, s1, s1x - 3, scoreY - 3);
        w.font.draw(w.batch, s1, s1x + 3, scoreY - 3);
        w.font.draw(w.batch, s1, s1x - 3, scoreY + 3);
        w.font.draw(w.batch, s1, s1x + 3, scoreY + 3);
        w.font.setColor(CLR_CYAN.r, CLR_CYAN.g, CLR_CYAN.b, 0.3f);
        w.font.draw(w.batch, s1, s1x - 1, scoreY - 1);
        w.font.draw(w.batch, s1, s1x + 1, scoreY - 1);
        w.font.draw(w.batch, s1, s1x - 1, scoreY + 1);
        w.font.draw(w.batch, s1, s1x + 1, scoreY + 1);
        // Bright center
        w.font.setColor(1f, 1f, 1f, 1f);
        w.font.draw(w.batch, s1, s1x, scoreY);

        // Center divider
        w.font.getData().setScale(1.4f, -1.4f);
        w.glyphLayout.setText(w.font, "-");
        float dx = 300 - w.glyphLayout.width / 2;
        w.font.setColor(CLR_WHITE_120);
        w.font.draw(w.batch, "-", dx, scoreY);

        w.font.getData().setScale(2f, -2f);
        String s2 = "" + w.scorePlayer2;
        w.glyphLayout.setText(w.font, s2);
        float s2x = 360 - w.glyphLayout.width / 2;
        // Red glow layers
        w.font.setColor(CLR_LIGHT_RED.r, CLR_LIGHT_RED.g, CLR_LIGHT_RED.b, 0.15f);
        w.font.draw(w.batch, s2, s2x - 3, scoreY - 3);
        w.font.draw(w.batch, s2, s2x + 3, scoreY - 3);
        w.font.draw(w.batch, s2, s2x - 3, scoreY + 3);
        w.font.draw(w.batch, s2, s2x + 3, scoreY + 3);
        w.font.setColor(CLR_LIGHT_RED.r, CLR_LIGHT_RED.g, CLR_LIGHT_RED.b, 0.3f);
        w.font.draw(w.batch, s2, s2x - 1, scoreY - 1);
        w.font.draw(w.batch, s2, s2x + 1, scoreY - 1);
        w.font.draw(w.batch, s2, s2x - 1, scoreY + 1);
        w.font.draw(w.batch, s2, s2x + 1, scoreY + 1);
        // Bright center
        w.font.setColor(1f, 1f, 1f, 1f);
        w.font.draw(w.batch, s2, s2x, scoreY);

        // Restore normal font scale
        w.font.getData().setScale(1f, -1f);
    }

    void drawLevels() {
        w.font.setColor(CLR_CYAN);
        w.font.draw(w.batch, "LV " + w.level1, 15, 80);
        w.font.setColor(CLR_LIGHT_RED);
        String lvText = (w.singlePlayer ? "AI " : "") + "LV " + w.level2;
        w.glyphLayout.setText(w.font, lvText);
        w.font.draw(w.batch, lvText, 585 - w.glyphLayout.width, 80);
    }

    void drawAbilityLists() {
        // Player 1 abilities
        w.font.setColor(CLR_LIGHT_BLUE);
        int ay = 120;
        for (String ability : w.player1Abilities.keySet()) {
            int level = w.player1Abilities.get(ability);
            int branch = w.player1AbilityBranches.containsKey(ability) ? w.player1AbilityBranches.get(ability) : 0;
            String text = branch > 0 ? getBranchName(ability, branch) : getAbilityShortName(ability);
            if (level > 1) text += " Lv" + level;
            w.font.draw(w.batch, text, 10, ay);
            ay += 11;
        }
        // Player 2 abilities
        w.font.setColor(CLR_PINK_RED);
        ay = 120;
        for (String ability : w.player2Abilities.keySet()) {
            int level = w.player2Abilities.get(ability);
            int branch = w.player2AbilityBranches.containsKey(ability) ? w.player2AbilityBranches.get(ability) : 0;
            String text = branch > 0 ? getBranchName(ability, branch) : getAbilityShortName(ability);
            if (level > 1) text += " Lv" + level;
            w.glyphLayout.setText(w.font, text);
            w.font.draw(w.batch, text, 585 - w.glyphLayout.width, ay);
            ay += 11;
        }
    }

    void drawStatusIndicatorsText() {
        int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        // Player 1 status
        if (w.player1StunTimer > 0) { w.font.setColor(CLR_YELLOW); w.font.draw(w.batch, "STUNNED!", 10, w.paddle1Y - 10); }
        if (w.player1LagEffectTimer > 0) { w.font.setColor(CLR_MAGENTA); w.font.draw(w.batch, "LAGGING!", 10, w.paddle1Y - 10); }
        if (w.player1ReverseEffectTimer > 0) { w.font.setColor(CLR_ORANGE); w.font.draw(w.batch, "REVERSED!", 10, w.paddle1Y + p1H + 20); }
        if (w.player2BlindLevel > 0) { w.font.setColor(CLR_GRAY_50); w.font.draw(w.batch, "BLINDED!", 10, w.paddle1Y - 40); }
        if (w.player1ShrinkEffectTimer > 0) { w.font.setColor(CLR_MAGENTA); w.font.draw(w.batch, "SHRUNK!", 10, w.paddle1Y - 55); }
        if (w.player1DashActive > 0) { w.font.setColor(CLR_SKY_BLUE); w.font.draw(w.batch, "DASH!", 10, w.paddle1Y - 10); }
        if (w.player1SwordSwingTimer > 0) { w.font.setColor(CLR_DARK_RED); w.font.draw(w.batch, "ZANGETSU!", 30, w.paddle1Y - 20); }
        if (w.player1HammerActive && w.player1HammerDuration > 0) { w.font.setColor(Color.ORANGE); w.font.draw(w.batch, "HAMMER!", 80, 200); }
        // Player 2 status
        if (w.player2StunTimer > 0) { w.font.setColor(CLR_YELLOW); w.font.draw(w.batch, "STUNNED!", 480, w.paddle2Y - 10); }
        if (w.player2LagEffectTimer > 0) { w.font.setColor(CLR_MAGENTA); w.font.draw(w.batch, "LAGGING!", 480, w.paddle2Y - 10); }
        if (w.player2ReverseEffectTimer > 0) { w.font.setColor(CLR_ORANGE); w.font.draw(w.batch, "REVERSED!", 480, w.paddle2Y + p2H + 20); }
        if (w.player1BlindLevel > 0) { w.font.setColor(CLR_GRAY_50); w.font.draw(w.batch, "BLINDED!", 480, w.paddle2Y - 40); }
        if (w.player2ShrinkEffectTimer > 0) { w.font.setColor(CLR_MAGENTA); w.font.draw(w.batch, "SHRUNK!", 480, w.paddle2Y - 55); }
        if (w.player2DashActive > 0) { w.font.setColor(CLR_LIGHT_RED); w.font.draw(w.batch, "DASH!", 530, w.paddle2Y - 10); }
        if (w.player2SwordSwingTimer > 0) { w.font.setColor(CLR_DARK_RED); w.font.draw(w.batch, "ZANGETSU!", 480, w.paddle2Y - 20); }
        if (w.player2HammerActive && w.player2HammerDuration > 0) { w.font.setColor(Color.ORANGE); w.font.draw(w.batch, "HAMMER!", 380, 200); }
        // Tunnel vision
        if (w.player1TunnelTimer > 0) { w.font.setColor(CLR_YELLOW); w.font.draw(w.batch, "TUNNEL VISION!", 100, 200); }
        if (w.player2TunnelTimer > 0) { w.font.setColor(CLR_YELLOW); w.font.draw(w.batch, "TUNNEL VISION!", 400, 200); }
        // Inversion
        if (w.player1InversionTimer > 0) { w.font.setColor(Color.WHITE); w.font.draw(w.batch, "INVERTED!", 100, 200); }
        if (w.player2InversionTimer > 0) { w.font.setColor(Color.WHITE); w.font.draw(w.batch, "INVERTED!", 400, 200); }
        // Slow mo
        if (w.player1TimeLoopSlowMoTimer > 0 || w.player2TimeLoopSlowMoTimer > 0) {
            w.font.setColor(CLR_LIGHT_BLUE_180);
            String sm = "SLOW MOTION";
            w.glyphLayout.setText(w.font, sm);
            w.font.draw(w.batch, sm, 300 - w.glyphLayout.width / 2, 30);
        }
        // Bankai labels
        if (w.player1BankaiActive) { w.font.setColor(CLR_DARK_RED); w.font.draw(w.batch, "BANKAI", 5, w.paddle1Y - 30); }
        if (w.player2BankaiActive) { w.font.setColor(CLR_DARK_RED); w.font.draw(w.batch, "BANKAI", 545, w.paddle2Y - 30); }
    }


    /** Draws cooldown progress bars (filled shapes pass) for Q/Slash-activated abilities. */
    void drawCooldownBars() {
        int y1 = 13, y2 = 13;
        // Player 1 bars (top-left, x=10, width=50)
        int shrinkLv1 = w.getEffectiveAbilityLevel(1, "shrink_opponent");
        int shrinkCD1 = Math.max(400, 900 - (Math.max(1, shrinkLv1) - 1) * 80);
        int shrinkLv2 = w.getEffectiveAbilityLevel(2, "shrink_opponent");
        int shrinkCD2 = Math.max(400, 900 - (Math.max(1, shrinkLv2) - 1) * 80);
        y1 = drawOneCooldownBar(1, "reverse_controls", w.player1ReverseTimer, 2000,      10, y1);
        y1 = drawOneCooldownBar(1, "ghost_ball",       w.player1GhostTimer,   w.ghostCooldown, 10, y1);
        y1 = drawOneCooldownBar(1, "blind",            w.player1BlindTimer,   2000,      10, y1);
        y1 = drawOneCooldownBar(1, "shrink_opponent",  w.player1ShrinkTimer,  shrinkCD1, 10, y1);
        y1 = drawOneCooldownBar(1, "gravity_hammer", w.player1HammerTimer,    w.hammerCooldown,    10, y1);
        y1 = drawOneCooldownBar(1, "portal_pong",    w.player1PortalTimer,    600,                 10, y1);
        y1 = drawOneCooldownBar(1, "time_loop",      w.player1TimeLoopTimer,  w.timeLoopCooldown,  10, y1);
        y1 = drawOneCooldownBar(1, "barrier",        w.player1BarrierTimer,   w.barrierCooldown,   10, y1);
        y1 = drawOneCooldownBar(1, "trap",           w.player1TrapTimer,      w.trapCooldown,      10, y1);
        y1 = drawOneCooldownBar(1, "screen_warp",    w.player1WarpTimer,      w.warpCooldown,      10, y1);
        // Player 2 bars (top-right, x=540, width=50)
        y2 = drawOneCooldownBar(2, "reverse_controls", w.player2ReverseTimer, 2000,      540, y2);
        y2 = drawOneCooldownBar(2, "ghost_ball",       w.player2GhostTimer,   w.ghostCooldown, 540, y2);
        y2 = drawOneCooldownBar(2, "blind",            w.player2BlindTimer,   2000,      540, y2);
        y2 = drawOneCooldownBar(2, "shrink_opponent",  w.player2ShrinkTimer,  shrinkCD2, 540, y2);
        y2 = drawOneCooldownBar(2, "gravity_hammer", w.player2HammerTimer,    w.hammerCooldown,    540, y2);
        y2 = drawOneCooldownBar(2, "portal_pong",    w.player2PortalTimer,    600,                 540, y2);
        y2 = drawOneCooldownBar(2, "time_loop",      w.player2TimeLoopTimer,  w.timeLoopCooldown,  540, y2);
        y2 = drawOneCooldownBar(2, "barrier",        w.player2BarrierTimer,   w.barrierCooldown,   540, y2);
        y2 = drawOneCooldownBar(2, "trap",           w.player2TrapTimer,      w.trapCooldown,      540, y2);
        y2 = drawOneCooldownBar(2, "screen_warp",    w.player2WarpTimer,      w.warpCooldown,      540, y2);
    }

    /** Draws a single cooldown bar. Returns next y offset (y+14 if drawn, y if skipped). */
    private int drawOneCooldownBar(int player, String ability, int timer, int maxCooldown, int x, int y) {
        if (w.getEffectiveAbilityLevel(player, ability) <= 0) return y;
        float pct = Math.min(1.0f, timer / (float) maxCooldown);
        // Track background
        w.tmpColor.set(0f, 0f, 0f, 0.7f);
        w.sr.setColor(w.tmpColor);
        fillRoundRect(x, y, 52, 10, 3);
        // Filled portion: dark-red → orange → green as cooldown fills
        if (pct >= 1.0f) {
            w.sr.setColor(CLR_GREEN);
        } else {
            w.tmpColor.set(0.85f - pct * 0.2f, pct * 0.75f, 0f, 0.95f);
            w.sr.setColor(w.tmpColor);
        }
        if (pct > 0.02f) fillRoundRect(x + 1, y + 1, Math.max(2, 50 * pct), 8, 3);
        // Shine on top of bar
        w.tmpColor.set(1f, 1f, 1f, 0.12f);
        w.sr.setColor(w.tmpColor);
        fillRoundRect(x + 1, y + 1, Math.max(2, 50 * pct), 3, 2);
        return y + 14;
    }

    void drawCooldownTimers() {
        // Learning AI indicator
        if (w.learningAIEnabled && w.singlePlayer) {
            int progressPercent = (int)(w.learningProgress * 100);
            Color progressColor;
            String progressText;
            if (w.learningProgress < 0.3) { progressColor = CLR_GREEN_BRIGHT; progressText = "AI: Learning "; }
            else if (w.learningProgress < 0.6) { progressColor = CLR_YELLOW; progressText = "AI: Adapting "; }
            else if (w.learningProgress < 0.85) { progressColor = CLR_ORANGE; progressText = "AI: Competent "; }
            else { progressColor = CLR_LIGHT_RED; progressText = "AI: Skilled "; }
            w.font.setColor(progressColor);
            w.font.draw(w.batch, progressText + progressPercent + "%", 450, 95);
        }
        // Cooldown labels removed — bars alone indicate status (consistent with reverse_controls style)
    }

    /** Draws "Abc RDY" or "Abc 75%" text label next to a cooldown bar. */
    private int drawCooldownLabel(int player, String ability, String label, int timer, int maxCooldown, int x, int y) {
        if (w.getEffectiveAbilityLevel(player, ability) <= 0) return y;
        float pct = Math.min(1.0f, timer / (float) maxCooldown);
        if (pct >= 1.0f) {
            w.font.setColor(CLR_GREEN_BRIGHT);
            w.font.draw(w.batch, label + " RDY", x, y + 9);
        } else {
            w.font.setColor(CLR_ORANGE);
            w.font.draw(w.batch, label + " " + (int)(pct * 100) + "%", x, y + 9);
        }
        return y + 14;
    }

    /** Draw synergy banner at the top-center if a synergy is active this match. */
    void drawSynergyBanner() {
        if (w.activeSynergyKey == null || w.activeSynergyKey.isEmpty()) return;
        // Find synergy name by key
        String synName = w.activeSynergyKey.toUpperCase();
        for (int i = 0; i < DeckManager.SYNERGIES.length; i++) {
            if (DeckManager.SYNERGIES[i].bonusKey.equals(w.activeSynergyKey)) {
                synName = DeckManager.SYNERGIES[i].name;
                break;
            }
        }
        w.font.setColor(c(255, 220, 50));
        String banner = "[" + synName + "]";
        w.glyphLayout.setText(w.font, banner);
        w.font.draw(w.batch, banner, (GameWorld.VIRTUAL_WIDTH - w.glyphLayout.width) / 2, 13);
    }

    private void drawBall2() {
        // Outer glow
        w.tmpColor.set(1f, 0.4f, 0f, 0.3f);
        w.sr.setColor(w.tmpColor);
        w.sr.circle(w.ball2X + 7, w.ball2Y + 7, 12);
        // Core
        w.tmpColor.set(1f, 0.6f, 0.1f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.circle(w.ball2X + 7, w.ball2Y + 7, 7);
    }

    private void drawMatchTimerBg() {
        if (!w.matchTimerActive && !w.overtimeActive) return;
        int remaining = Math.max(0, GameWorld.MATCH_DURATION_TICKS - w.matchTimer);
        boolean urgent = remaining < 3000 && !w.overtimeActive;
        if (urgent) {
            w.tmpColor.set(0.5f, 0f, 0f, 0.6f);
        } else {
            w.tmpColor.set(0f, 0f, 0f, 0.45f);
        }
        w.sr.setColor(w.tmpColor);
        fillRoundRect(256, 4, 88, 22, 6);
    }

    private void drawMatchTimer() {
        if (!w.matchTimerActive && !w.overtimeActive) return;
        int remaining = Math.max(0, GameWorld.MATCH_DURATION_TICKS - w.matchTimer);
        int totalSeconds = remaining / 100;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        String timeStr;
        if (secs < 10) {
            timeStr = minutes + ":0" + secs;
        } else {
            timeStr = minutes + ":" + secs;
        }
        if (w.overtimeActive) timeStr = "OVERTIME";
        boolean urgent = remaining < 3000 && !w.overtimeActive;
        w.glyphLayout.setText(w.font, timeStr);
        float tw = w.glyphLayout.width;
        if (urgent) {
            w.tmpColor.set(1f, 0.2f, 0.2f, 1f);
        } else {
            w.tmpColor.set(1f, 1f, 1f, 1f);
        }
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, timeStr, 300 - tw / 2, 22);
        w.font.setColor(1f, 1f, 1f, 1f);
    }

    private void drawToasts() {
        // P1 toast — slides up from bottom-left
        if (w.toastP1Timer > 0 && !w.toastP1Text.isEmpty()) {
            float alpha = Math.min(1f, w.toastP1Timer);
            float slideY = 380 - (2.0f - w.toastP1Timer) * 15;
            if (w.toastP1LevelUp) {
                w.tmpColor.set(1f, 0.85f, 0f, alpha);
            } else {
                w.tmpColor.set(0.4f, 1f, 0.4f, alpha);
            }
            w.font.setColor(w.tmpColor);
            w.font.draw(w.batch, w.toastP1Text, 14, slideY);
            w.font.setColor(1f, 1f, 1f, 1f);
        }
        // P2 toast — slides up from bottom-right
        if (w.toastP2Timer > 0 && !w.toastP2Text.isEmpty()) {
            float alpha = Math.min(1f, w.toastP2Timer);
            float slideY = 380 - (2.0f - w.toastP2Timer) * 15;
            w.glyphLayout.setText(w.font, w.toastP2Text);
            float tx = 586 - w.glyphLayout.width;
            if (w.toastP2LevelUp) {
                w.tmpColor.set(1f, 0.85f, 0f, alpha);
            } else {
                w.tmpColor.set(0.4f, 1f, 0.4f, alpha);
            }
            w.font.setColor(w.tmpColor);
            w.font.draw(w.batch, w.toastP2Text, tx, slideY);
            w.font.setColor(1f, 1f, 1f, 1f);
        }
    }

    void drawPauseOverlay() {
        w.sr.setColor(CLR_BLACK_150);
        w.sr.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
    }

    void drawPauseText() {
        w.font.setColor(Color.WHITE);
        String pauseStr = "PAUSED";
        w.glyphLayout.setText(w.font, pauseStr);
        w.font.draw(w.batch, pauseStr, GameWorld.VIRTUAL_WIDTH / 2f - w.glyphLayout.width / 2, GameWorld.VIRTUAL_HEIGHT / 2f - 10);
        w.font.setColor(CLR_GRAY_150);
        String hint = "P = Resume | ESC = Main Menu";
        w.glyphLayout.setText(w.font, hint);
        w.font.draw(w.batch, hint, GameWorld.VIRTUAL_WIDTH / 2f - w.glyphLayout.width / 2, GameWorld.VIRTUAL_HEIGHT / 2f + 10);
        String hint2 = "F5=Save  F9=Load  F8=Delete";
        w.glyphLayout.setText(w.font, hint2);
        w.font.draw(w.batch, hint2, GameWorld.VIRTUAL_WIDTH / 2f - w.glyphLayout.width / 2, GameWorld.VIRTUAL_HEIGHT / 2f + 25);
    }

    /** Renders the post-match result overlay (victory/defeat + trophy animation + chest + arena unlock). */
    void renderMatchResult() {
        if (w.matchResultPhase <= 0) return;

        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        // Semi-transparent dark overlay
        w.tmpColor.set(0f, 0f, 0f, 0.75f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 0, 600, 400);

        // Result panel background
        boolean won = w.matchResultWon;
        if (won) {
            w.tmpColor.set(0f, 0.35f, 0f, 0.9f);
        } else {
            w.tmpColor.set(0.35f, 0f, 0f, 0.9f);
        }
        w.sr.setColor(w.tmpColor);
        w.sr.rect(150, 80, 300, 240);

        // Trophy bar background
        w.tmpColor.set(0.8f, 0.65f, 0f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(160, 200, 280, 30);

        // Chest preview background glow (phase 2) — sprite drawn in batch pass
        if (w.matchResultPhase == 2 && !w.matchResultChestType.isEmpty()) {
            String ct = w.matchResultChestType;
            if ("gold".equals(ct)) { w.tmpColor.set(1f, 0.8f, 0f, 0.25f); }
            else if ("magical".equals(ct)) { w.tmpColor.set(0.7f, 0f, 1f, 0.25f); }
            else if ("arena".equals(ct)) { w.tmpColor.set(0f, 0.6f, 1f, 0.25f); }
            else { w.tmpColor.set(0.6f, 0.6f, 0.6f, 0.25f); }
            w.sr.setColor(w.tmpColor);
            w.sr.rect(220, 155, 160, 100);
        }

        // Arena unlock glow (phase 3)
        if (w.matchResultPhase == 3) {
            w.tmpColor.set(0.8f, 0.65f, 0f, 0.3f + 0.3f * (float)Math.sin(w.elapsedTime * 4));
            w.sr.setColor(w.tmpColor);
            w.sr.rect(100, 60, 400, 280);
        }

        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // Victory / Defeat title
        String title = won ? "VICTORY!" : "DEFEAT";
        w.tmpColor.set(won ? 0.3f : 1f, won ? 1f : 0.3f, 0.3f, 1f);
        w.font.setColor(w.tmpColor);
        w.glyphLayout.setText(w.font, title);
        w.font.draw(w.batch, title, 300 - w.glyphLayout.width / 2, 120);

        // Trophy count (animating in phase 1, static after)
        String trophyStr = "Trophies: " + w.matchResultTrophyDisplay;
        if (w.matchResultPhase == 1 && w.matchResultTrophyDisplay != w.matchResultTrophyTarget) {
            trophyStr = trophyStr + "  (animating...)";
        }
        w.tmpColor.set(1f, 0.85f, 0f, 1f);
        w.font.setColor(w.tmpColor);
        w.glyphLayout.setText(w.font, trophyStr);
        w.font.draw(w.batch, trophyStr, 300 - w.glyphLayout.width / 2, 210);

        // Trophy delta
        String deltaStr = (w.matchResultTrophyDelta > 0 ? "+" : "") + w.matchResultTrophyDelta;
        w.tmpColor.set(w.matchResultTrophyDelta > 0 ? 0.3f : 1f, w.matchResultTrophyDelta > 0 ? 1f : 0.3f, 0.3f, 1f);
        w.font.setColor(w.tmpColor);
        w.glyphLayout.setText(w.font, deltaStr);
        w.font.draw(w.batch, deltaStr, 300 - w.glyphLayout.width / 2, 240);

        // Phase 2: chest earned — sprite + label
        if (w.matchResultPhase == 2 && !w.matchResultChestType.isEmpty()) {
            String ct = w.matchResultChestType;
            int chestIdx = -1;
            String[] cto = {"silver", "gold", "magical", "arena"};
            for (int ci = 0; ci < cto.length; ci++) { if (cto[ci].equals(ct)) { chestIdx = ci; break; } }
            if (chestIdx >= 0 && chestIdx < w.texChests.length && w.texChests[chestIdx] != null) {
                int sz = 80;
                // Flip vertically same as menu chest
                w.batch.draw(w.texChests[chestIdx], 300 - sz / 2, 165 + sz, sz, -sz);
            }
            String chestName = ChestSystem.getChestDisplayName(ct);
            w.tmpColor.set(1f, 1f, 1f, 1f);
            w.font.setColor(w.tmpColor);
            w.glyphLayout.setText(w.font, chestName + " Chest Earned!");
            w.font.draw(w.batch, chestName + " Chest Earned!", 300 - w.glyphLayout.width / 2, 160);
        }

        // Phase 3: new arena
        if (w.matchResultPhase == 3) {
            w.tmpColor.set(1f, 0.85f, 0f, 1f);
            w.font.setColor(w.tmpColor);
            String arenaMsg = "NEW ARENA UNLOCKED!";
            w.glyphLayout.setText(w.font, arenaMsg);
            w.font.draw(w.batch, arenaMsg, 300 - w.glyphLayout.width / 2, 150);
            w.font.setColor(1f, 1f, 1f, 1f);
            w.glyphLayout.setText(w.font, w.matchResultArenaName);
            w.font.draw(w.batch, w.matchResultArenaName, 300 - w.glyphLayout.width / 2, 180);
        }

        // Tap to continue hint (phases 2+)
        if (w.matchResultPhase >= 2) {
            w.tmpColor.set(0.6f, 0.6f, 0.6f, 1f);
            w.font.setColor(w.tmpColor);
            String hint = "Tap to continue";
            w.glyphLayout.setText(w.font, hint);
            w.font.draw(w.batch, hint, 300 - w.glyphLayout.width / 2, 310);
        }

        w.font.setColor(1f, 1f, 1f, 1f);
        w.batch.end();
    }

    // ==================== COLLECTION SCREEN (Phase 5) ====================

    /** Returns rarity color using CardMetadata rarity constants (0=Common,1=Rare,2=Epic,3=Legendary). */
    private com.badlogic.gdx.graphics.Color getCardRarityColor(int cardRarity) {
        switch (cardRarity) {
            case CardMetadata.RARE:      return Colors.RARITY_RARE;
            case CardMetadata.EPIC:      return Colors.RARITY_EPIC;
            case CardMetadata.LEGENDARY: return Colors.RARITY_LEGENDARY;
            default:                     return Colors.RARITY_COMMON;
        }
    }

    private boolean isCardInDeck(String card) {
        if (w.profile == null || w.profile.deck == null) return false;
        for (String d : w.profile.deck) {
            if (card.equals(d)) return true;
        }
        return false;
    }

    /** Renders the collection / deck builder screen. */
    public void renderCollection() {
        if (w.profile == null) return;

        // --- SHAPES PASS ---
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        w.tmpColor.set(10/255f, 12/255f, 30/255f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 0, 600, 400);

        // Top bar
        w.tmpColor.set(15/255f, 20/255f, 50/255f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 0, 600, 34);

        // Left panel background
        w.tmpColor.set(8/255f, 10/255f, 25/255f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 34, 200, 326);

        // Divider line
        w.tmpColor.set(0.3f, 0.3f, 0.5f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(200, 34, 2, 366);

        // Bottom bar
        w.tmpColor.set(15/255f, 20/255f, 50/255f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(0, 360, 600, 40);

        // Back button shape
        w.tmpColor.set(0.5f, 0.1f, 0.1f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(530, 5, 60, 24);

        // Draw deck slots (left panel)
        int slotStartY = 44;
        int slotH = 36;
        for (int i = 0; i < 8; i++) {
            int slotY = slotStartY + i * (slotH + 4);
            if (slotY + slotH > 356) break;
            boolean isSelected = (w.collectionSelectedSlot == i);
            String card = w.profile.deck[i];
            if (card != null) {
                int rarity = CardMetadata.getRarity(card);
                com.badlogic.gdx.graphics.Color rc = getCardRarityColor(rarity);
                // Darken the rarity color for the background
                w.tmpColor.set(rc.r * 0.3f, rc.g * 0.3f, rc.b * 0.3f, 1f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(4, slotY, 192, slotH);
                // Rarity border
                w.tmpColor.set(rc.r, rc.g, rc.b, isSelected ? 1f : 0.5f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(4, slotY, 192, 2);
                w.sr.rect(4, slotY + slotH - 2, 192, 2);
                w.sr.rect(4, slotY, 2, slotH);
                w.sr.rect(194, slotY, 2, slotH);
            } else {
                // Empty slot
                w.tmpColor.set(0.15f, 0.15f, 0.25f, 1f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(4, slotY, 192, slotH);
                w.tmpColor.set(0.3f, 0.3f, 0.4f, 0.5f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(4, slotY, 192, 2);
                w.sr.rect(4, slotY + slotH - 2, 192, 2);
            }
            if (isSelected) {
                // Highlight selected slot
                w.tmpColor.set(1f, 1f, 0f, 0.2f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(4, slotY, 192, slotH);
            }
        }

        // Draw card grid (right panel) — 4 columns, rows of cards
        java.util.ArrayList<String> ownedList = new java.util.ArrayList<String>(w.profile.cardCopies.keySet());
        int CARD_W = 90, CARD_H = 70, CARD_PAD = 5;
        int gridX = 205, gridY = 38;
        int cols = 4;
        int visibleRows = 4;
        int startIdx = w.collectionScrollOffset * cols;
        for (int i = startIdx; i < Math.min(startIdx + cols * visibleRows, ownedList.size()); i++) {
            int col = (i - startIdx) % cols;
            int row = (i - startIdx) / cols;
            int cx = gridX + col * (CARD_W + CARD_PAD);
            int cy = gridY + row * (CARD_H + CARD_PAD);
            String card = ownedList.get(i);
            int rarity = CardMetadata.getRarity(card);
            com.badlogic.gdx.graphics.Color rc = getCardRarityColor(rarity);
            boolean isInDeck = isCardInDeck(card);
            boolean isHighlighted = card.equals(w.collectionSelectedCard);

            // Card background
            w.tmpColor.set(rc.r * 0.25f, rc.g * 0.25f, rc.b * 0.25f, 1f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(cx, cy, CARD_W, CARD_H);

            // Rarity border
            w.tmpColor.set(rc.r, rc.g, rc.b, isHighlighted ? 1f : 0.6f);
            w.sr.setColor(w.tmpColor);
            w.sr.rect(cx, cy, CARD_W, 2);
            w.sr.rect(cx, cy + CARD_H - 2, CARD_W, 2);
            w.sr.rect(cx, cy, 2, CARD_H);
            w.sr.rect(cx + CARD_W - 2, cy, 2, CARD_H);

            // In-deck green indicator
            if (isInDeck) {
                w.tmpColor.set(0f, 1f, 0.3f, 0.25f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(cx + 2, cy + 2, CARD_W - 4, CARD_H - 4);
            }

            // Highlight overlay
            if (isHighlighted) {
                w.tmpColor.set(1f, 1f, 0f, 0.2f);
                w.sr.setColor(w.tmpColor);
                w.sr.rect(cx, cy, CARD_W, CARD_H);
            }
        }

        w.sr.end();

        // --- TEXT PASS ---
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // Top bar text
        w.tmpColor.set(1f, 1f, 1f, 1f);
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, "Collection", 10, 24);

        // Gold display
        w.tmpColor.set(1f, 0.85f, 0f, 1f);
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, "Gold: " + w.profile.gold, 340, 24);

        // Back button text
        w.tmpColor.set(1f, 1f, 1f, 1f);
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, "BACK", 545, 24);

        // Left panel header
        w.tmpColor.set(0.7f, 0.7f, 1f, 1f);
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, "Your Deck", 10, 44);

        // Deck slot labels
        int slotStartY2 = 44;
        int slotH2 = 36;
        for (int i = 0; i < 8; i++) {
            int slotY = slotStartY2 + i * (slotH2 + 4);
            if (slotY + slotH2 > 356) break;
            String card = w.profile.deck[i];
            if (card != null) {
                int lv = w.profile.getPermLevel(card);
                w.tmpColor.set(1f, 1f, 1f, 1f);
                w.font.setColor(w.tmpColor);
                w.font.draw(w.batch, CardMetadata.getDisplayName(card), 10, slotY + 16);
                w.tmpColor.set(1f, 0.85f, 0f, 1f);
                w.font.setColor(w.tmpColor);
                w.font.draw(w.batch, "Lv" + lv, 160, slotY + 16);
            } else {
                w.tmpColor.set(0.4f, 0.4f, 0.5f, 1f);
                w.font.setColor(w.tmpColor);
                w.font.draw(w.batch, "Empty slot", 10, slotY + 16);
            }
        }

        // Deck count
        int filledSlots = 0;
        for (int i = 0; i < 8; i++) { if (w.profile.deck[i] != null) filledSlots++; }
        w.tmpColor.set(filledSlots >= 4 ? 0.3f : 1f, filledSlots >= 4 ? 1f : 0.3f, 0.3f, 1f);
        w.font.setColor(w.tmpColor);
        w.font.draw(w.batch, filledSlots + "/8 cards", 10, 358);

        // Card grid labels
        java.util.ArrayList<String> ownedList2 = new java.util.ArrayList<String>(w.profile.cardCopies.keySet());
        int CARD_W2 = 90, CARD_H2 = 70, CARD_PAD2 = 5;
        int gridX2 = 205, gridY2 = 38;
        int startIdx2 = w.collectionScrollOffset * 4;
        for (int i = startIdx2; i < Math.min(startIdx2 + 16, ownedList2.size()); i++) {
            int col = (i - startIdx2) % 4;
            int row = (i - startIdx2) / 4;
            int cx = gridX2 + col * (CARD_W2 + CARD_PAD2);
            int cy = gridY2 + row * (CARD_H2 + CARD_PAD2);
            String card = ownedList2.get(i);
            int lv = w.profile.getPermLevel(card);
            int copies = w.profile.getCopyCount(card);
            // Card icon sprite (Phase 9) — draw centered 32x32 icon in upper portion of card
            com.badlogic.gdx.graphics.Texture cardTex = w.cardTextureMap.get(card);
            if (cardTex != null) {
                w.batch.draw(cardTex, cx + (CARD_W2 - 32) / 2f, cy + 2, 32, 32);
            }
            // Card name
            w.tmpColor.set(1f, 1f, 1f, 1f);
            w.font.setColor(w.tmpColor);
            String name = CardMetadata.getDisplayName(card);
            // Truncate long names
            if (name.length() > 11) name = name.substring(0, 10) + ".";
            w.font.draw(w.batch, name, cx + 3, cy + 22);
            // Level badge
            w.tmpColor.set(1f, 0.85f, 0f, 1f);
            w.font.setColor(w.tmpColor);
            w.font.draw(w.batch, "Lv" + lv, cx + 3, cy + 38);
            // Copies
            w.tmpColor.set(0.6f, 0.8f, 1f, 1f);
            w.font.setColor(w.tmpColor);
            w.font.draw(w.batch, copies + "x", cx + CARD_W2 - 22, cy + 38);
            // In-deck checkmark
            if (isCardInDeck(card)) {
                w.tmpColor.set(0.3f, 1f, 0.3f, 1f);
                w.font.setColor(w.tmpColor);
                w.font.draw(w.batch, "IN DECK", cx + 3, cy + 54);
            }
        }

        // Scroll hint if more cards exist
        if (ownedList2.size() > 16) {
            w.tmpColor.set(0.5f, 0.5f, 0.7f, 1f);
            w.font.setColor(w.tmpColor);
            w.font.draw(w.batch, "Scroll: up/down arrow keys", 210, 358);
        }

        w.font.setColor(1f, 1f, 1f, 1f);
        w.batch.end();

        // Card detail popup overlay (own passes, always on top)
        renderCardDetailPopup();
    }

    /** Full-screen popup overlay showing card details, copies progress, upgrade + use buttons. */
    private void renderCardDetailPopup() {
        if (w.profile == null || w.collectionSelectedCard.isEmpty()) return;
        String sc = w.collectionSelectedCard;
        int lv = w.profile.getPermLevel(sc);
        int copies = w.profile.getCopyCount(sc);
        boolean atMax = (lv >= 10);
        boolean canUpgrade = w.profile.canUpgrade(sc);
        boolean inDeck = isCardInDeck(sc);
        int rarity = CardMetadata.getRarity(sc);
        com.badlogic.gdx.graphics.Color rc = getCardRarityColor(rarity);

        // Popup dimensions (Y-down: top=PY, bottom=PY+PH)
        final int PX = 110, PY = 68, PW = 380, PH = 250;
        // Button row inside popup
        final int BTN_Y = PY + PH - 44;
        final int BTN_H = 36;

        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(ShapeRenderer.ShapeType.Filled);

        // Dim background
        w.sr.setColor(Colors.c(0, 0, 0, 185));
        w.sr.rect(0, 0, 600, 400);

        // Popup background
        w.sr.setColor(Colors.c(14, 16, 42));
        w.sr.rect(PX, PY, PW, PH);

        // Header bar (rarity-tinted)
        w.tmpColor.set(rc.r * 0.42f, rc.g * 0.42f, rc.b * 0.42f, 1f);
        w.sr.setColor(w.tmpColor);
        w.sr.rect(PX, PY, PW, 30);

        // Left rarity accent bar
        w.sr.setColor(rc);
        w.sr.rect(PX, PY, 4, PH);

        // Close X button (top-right of header)
        w.sr.setColor(Colors.c(110, 30, 30));
        w.sr.rect(PX + PW - 28, PY + 5, 23, 21);

        // Divider under header
        w.sr.setColor(Colors.c(45, 50, 100));
        w.sr.rect(PX + 4, PY + 30, PW - 4, 1);

        // Progress bar track
        int barX = PX + 10, barY = PY + 158, barW = PW - 20, barH = 10;
        w.sr.setColor(Colors.c(20, 22, 55));
        w.sr.rect(barX, barY, barW, barH);
        if (!atMax) {
            int needed = CardMetadata.copiesNeededForUpgrade(sc, lv);
            float fill = Math.min(1f, (float) copies / needed);
            if (fill >= 1f) {
                w.sr.setColor(Colors.c(50, 220, 80));
            } else {
                w.sr.setColor(Colors.c(60, 130, 230));
            }
            if (fill > 0f) w.sr.rect(barX, barY, (int)(barW * fill), barH);
        } else {
            w.sr.setColor(Colors.c(255, 215, 0));
            w.sr.rect(barX, barY, barW, barH);
        }

        // Divider above buttons
        w.sr.setColor(Colors.c(45, 50, 100));
        w.sr.rect(PX + 4, BTN_Y - 4, PW - 4, 1);

        // Buttons
        if (canUpgrade) {
            // UPGRADE (green, left)
            w.sr.setColor(Colors.c(22, 110, 35));
            w.sr.rect(PX + 8, BTN_Y, 172, BTN_H);
            // USE / REMOVE (blue or red, right)
            if (inDeck) {
                w.sr.setColor(Colors.c(130, 28, 28));
            } else {
                w.sr.setColor(Colors.c(20, 68, 155));
            }
            w.sr.rect(PX + 188, BTN_Y, 184, BTN_H);
        } else {
            // Full-width USE / REMOVE
            if (inDeck) {
                w.sr.setColor(Colors.c(130, 28, 28));
            } else {
                w.sr.setColor(Colors.c(20, 68, 155));
            }
            w.sr.rect(PX + 8, BTN_Y, 364, BTN_H);
        }

        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();

        // Header: card name (left) + rarity label (right)
        w.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        w.font.draw(w.batch, CardMetadata.getDisplayName(sc), PX + 10, PY + 22);
        String[] rarNames = { "Common", "Rare", "Epic", "Legendary" };
        w.font.setColor(rc);
        String rarLabel = rarNames[Math.min(rarity, 3)];
        w.glyphLayout.setText(w.font, rarLabel);
        w.font.draw(w.batch, rarLabel, PX + PW - 34 - (int)w.glyphLayout.width, PY + 22);

        // Close X text
        w.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        w.font.draw(w.batch, "X", PX + PW - 18, PY + 22);

        // Level
        w.font.setColor(Colors.c(255, 215, 0));
        String lvStr = atMax ? "Level " + lv + "  (MAX)" : "Level " + lv + " / 10";
        w.font.draw(w.batch, lvStr, PX + 10, PY + 50);

        // Description (wrap at 50 chars)
        w.font.setColor(Colors.c(190, 200, 230));
        String desc = CardMetadata.getDescription(sc);
        if (desc.length() > 50) {
            int sp = desc.lastIndexOf(' ', 50);
            if (sp < 0) sp = 50;
            w.font.draw(w.batch, desc.substring(0, sp), PX + 10, PY + 68);
            w.font.draw(w.batch, desc.substring(sp + 1), PX + 10, PY + 84);
        } else {
            w.font.draw(w.batch, desc, PX + 10, PY + 68);
        }

        // Copies owned + in-deck badge on same line
        w.font.setColor(Colors.c(160, 185, 255));
        w.font.draw(w.batch, "Copies owned: " + copies, PX + 10, PY + 114);
        if (inDeck) {
            w.font.setColor(Colors.c(80, 225, 100));
            w.font.draw(w.batch, "IN DECK", PX + PW - 72, PY + 114);
        }

        // Progress label (above bar)
        if (!atMax) {
            int needed = CardMetadata.copiesNeededForUpgrade(sc, lv);
            int gold = CardMetadata.goldNeededForUpgrade(lv);
            if (copies >= needed) {
                w.font.setColor(Colors.c(80, 225, 80));
                w.font.draw(w.batch, "Ready to upgrade!  Cost: " + gold + "g", PX + 10, PY + 147);
            } else {
                w.font.setColor(Colors.c(140, 160, 215));
                w.font.draw(w.batch, copies + " / " + needed + " copies  (" + gold + "g to upgrade)", PX + 10, PY + 147);
            }
        } else {
            w.font.setColor(Colors.c(255, 215, 0));
            w.font.draw(w.batch, "MAX LEVEL reached!", PX + 10, PY + 147);
        }

        // Button labels
        w.font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        if (canUpgrade) {
            int gold = CardMetadata.goldNeededForUpgrade(lv);
            String upLabel = "UPGRADE  " + gold + "g";
            w.glyphLayout.setText(w.font, upLabel);
            w.font.draw(w.batch, upLabel, PX + 8 + (172 - (int)w.glyphLayout.width) / 2, BTN_Y + 24);
            String useLabel = inDeck ? "REMOVE" : "USE";
            w.glyphLayout.setText(w.font, useLabel);
            w.font.draw(w.batch, useLabel, PX + 188 + (184 - (int)w.glyphLayout.width) / 2, BTN_Y + 24);
        } else {
            String useLabel = inDeck ? "REMOVE FROM DECK" : "ADD TO DECK";
            w.glyphLayout.setText(w.font, useLabel);
            w.font.draw(w.batch, useLabel, PX + 8 + (364 - (int)w.glyphLayout.width) / 2, BTN_Y + 24);
        }

        w.font.setColor(1f, 1f, 1f, 1f);
        w.batch.end();
    }

    // ==================== HELPER: CENTERED TEXT ====================

    /** Draw text horizontally centered at the given virtual x, y (Y-down). */
    private void drawTextCentered(String text, float cx, float cy) {
        w.glyphLayout.setText(w.font, text);
        w.font.draw(w.batch, text, cx - w.glyphLayout.width / 2, cy);
    }

    // ==================== ARENA ROAD SCREEN ====================

    public void renderArenaRoad() {
        if (w.profile == null) return;
        int currentArena = com.pongus.game.ArenaConfig.getArenaIndex(w.profile.trophies);
        int rowH = 68;
        int expandedExtra = 44; // extra height when expanded

        // === SHAPE PASS (Filled) ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        // Background
        w.sr.setColor(Colors.c(8, 10, 25));
        w.sr.rect(0, 0, 600, 400);
        // Top bar
        w.sr.setColor(Colors.c(20, 22, 55));
        w.sr.rect(0, 0, 600, 34);
        // Divider under top bar
        w.sr.setColor(Colors.c(60, 60, 120));
        w.sr.rect(0, 33, 600, 1);

        // Draw rows top-to-bottom: arena 7 (top) to arena 0 (bottom)
        int drawY = 34 - w.arenaRoadScrollOffset;
        for (int ai = 7; ai >= 0; ai--) {
            boolean expanded = (ai == w.arenaRoadExpandedArena);
            int h = rowH + (expanded ? expandedExtra : 0);
            if (drawY + h > 34 && drawY < 400) {
                boolean isCurrent = (ai == currentArena);
                boolean unlocked = (ai <= currentArena);
                // Row bg
                if (isCurrent) w.sr.setColor(Colors.c(25, 55, 25));
                else if (unlocked) w.sr.setColor(Colors.c(18, 18, 40));
                else w.sr.setColor(Colors.c(10, 10, 22));
                w.sr.rect(4, drawY, 592, h - 3);
                // Border for current arena
                if (isCurrent) {
                    w.sr.setColor(Colors.c(60, 220, 60));
                    w.sr.rect(4, drawY, 592, 2);
                    w.sr.rect(4, drawY + h - 5, 592, 2);
                    w.sr.rect(4, drawY, 2, h - 3);
                    w.sr.rect(594, drawY, 2, h - 3);
                }
                // Colored circle icon (fallback when no icon texture)
                com.pongus.game.ArenaConfig.Arena arena = com.pongus.game.ArenaConfig.ARENAS[ai];
                com.badlogic.gdx.graphics.Color ac = Colors.ARENA_THEMES[ai];
                if (w.arenaIconTextures[ai] == null) {
                    w.sr.setColor(unlocked ? ac : Colors.c(35, 35, 35));
                    w.sr.circle(40, drawY + rowH / 2, 18, 16);
                    w.sr.setColor(Colors.c(0, 0, 0, 120));
                    w.sr.circle(40, drawY + rowH / 2, 11, 12);
                }
                // If expanded: card pool chips (shown for locked arenas too, grayed out)
                if (expanded && arena.cardPool != null) {
                    for (int ci = 0; ci < arena.cardPool.length && ci < 6; ci++) {
                        int rarity = com.pongus.game.CardMetadata.getRarity(arena.cardPool[ci]);
                        com.badlogic.gdx.graphics.Color rc = getRarityColor(rarity);
                        if (unlocked) {
                            w.sr.setColor(rc.r * 0.3f, rc.g * 0.3f, rc.b * 0.3f, 1f);
                        } else {
                            w.sr.setColor(0.15f, 0.15f, 0.18f, 1f);
                        }
                        w.sr.rect(75 + ci * 52, drawY + rowH + 2, 48, 26);
                        if (unlocked) {
                            w.sr.setColor(rc);
                        } else {
                            w.sr.setColor(0.35f, 0.35f, 0.40f, 1f);
                        }
                        w.sr.rect(75 + ci * 52, drawY + rowH + 2, 48, 2);
                    }
                }
            }
            drawY += h;
        }
        w.sr.end();

        // === LINE PASS ===
        w.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        w.sr.setColor(Colors.c(30, 30, 60));
        w.sr.rect(0, 34, 600, 366);
        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();
        w.font.setColor(Colors.c(180, 190, 255));
        drawTextCentered("ARENA ROAD", 300, 21);
        w.font.setColor(Colors.c(120, 120, 180));
        drawTextCentered("< Back", 540, 21);

        drawY = 34 - w.arenaRoadScrollOffset;
        for (int ai = 7; ai >= 0; ai--) {
            boolean expanded = (ai == w.arenaRoadExpandedArena);
            int h = rowH + (expanded ? expandedExtra : 0);
            if (drawY + h > 34 && drawY < 400) {
                boolean isCurrent = (ai == currentArena);
                boolean unlocked = (ai <= currentArena);
                int midY = drawY + rowH / 2 - 4;
                com.pongus.game.ArenaConfig.Arena arena = com.pongus.game.ArenaConfig.ARENAS[ai];

                // Arena icon badge (or number fallback)
                com.badlogic.gdx.graphics.Texture icon = w.arenaIconTextures[ai];
                if (icon != null) {
                    int iconSize = 60;
                    int iconX = 4;
                    int iconY = drawY + (rowH - iconSize) / 2;
                    if (unlocked) {
                        w.batch.setColor(1f, 1f, 1f, 1f);
                    } else {
                        w.batch.setColor(0.4f, 0.4f, 0.4f, 1f);
                    }
                    w.batch.draw(icon, iconX, iconY, iconSize, iconSize, 0, 0, icon.getWidth(), icon.getHeight(), false, true);
                    w.batch.setColor(1f, 1f, 1f, 1f);
                } else {
                    w.font.setColor(Colors.c(255, 255, 255));
                    drawTextCentered("" + ai, 40, drawY + rowH / 2 - 4);
                }

                // Name
                w.font.setColor(unlocked ? Colors.c(240, 240, 255) : Colors.c(80, 80, 100));
                w.font.draw(w.batch, arena.name, 68, drawY + rowH / 2 + 4);

                // Trophy range
                w.font.setColor(Colors.c(200, 170, 40));
                String range = arena.minTrophies + (arena.maxTrophies > 0 ? "-" + arena.maxTrophies : "+");
                w.font.draw(w.batch, range + " trophies", 68, drawY + rowH / 2 - 10);

                // Status badge (right side)
                if (isCurrent) {
                    w.font.setColor(Colors.c(50, 230, 50));
                    drawTextCentered("YOU ARE HERE", 490, midY);
                } else if (!unlocked) {
                    w.font.setColor(Colors.c(160, 60, 60));
                    drawTextCentered("LOCKED", 490, midY);
                } else {
                    w.font.setColor(Colors.c(80, 120, 200));
                    drawTextCentered("Completed", 490, midY);
                }

                // Card pool names if expanded (locked arenas shown grayed out)
                if (expanded && arena.cardPool != null) {
                    for (int ci = 0; ci < arena.cardPool.length && ci < 6; ci++) {
                        String shortName = arena.cardPool[ci].replace("_", " ");
                        if (shortName.length() > 7) shortName = shortName.substring(0, 7);
                        if (unlocked) {
                            w.font.setColor(Colors.c(240, 240, 240));
                        } else {
                            w.font.setColor(Colors.c(100, 100, 110));
                        }
                        drawTextCentered(shortName, 75 + ci * 52 + 24, drawY + rowH + 18);
                    }
                    if (unlocked) {
                        w.font.setColor(Colors.c(100, 100, 150));
                        drawTextCentered("Tap again to close", 300, drawY + rowH + 38);
                    } else {
                        w.font.setColor(Colors.c(160, 60, 60));
                        drawTextCentered("Unlock at " + arena.minTrophies + " trophies", 300, drawY + rowH + 38);
                    }
                } else if (!expanded) {
                    w.font.setColor(Colors.c(80, 80, 130));
                    drawTextCentered("Tap to expand", 490, midY + 12);
                }
            }
            drawY += h;
        }
        w.batch.end();
    }

    // ==================== PRACTICE MODE SCREEN ====================

    public void renderPractice() {
        String[] labels = {"Easy", "Normal", "Hard", "Impossible"};
        com.badlogic.gdx.graphics.Color[] btnColors = {
            Colors.c(50, 180, 50),   // Easy = green
            Colors.c(50, 120, 220),  // Normal = blue
            Colors.c(220, 150, 30),  // Hard = orange
            Colors.c(200, 40, 40)    // Impossible = red
        };
        int[] btnX = {30, 160, 290, 420};
        int btnY = 175, btnW = 115, btnH = 50;

        // === SHAPE PASS ===
        w.sr.setProjectionMatrix(w.camera.combined);
        w.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        // Background
        w.sr.setColor(Colors.c(8, 10, 25));
        w.sr.rect(0, 0, 600, 400);
        // Top bar
        w.sr.setColor(Colors.c(20, 22, 55));
        w.sr.rect(0, 0, 600, 34);
        // Difficulty buttons
        for (int i = 0; i < 4; i++) {
            if (i == w.practiceSelectedDifficulty) {
                // Selected: bright
                w.sr.setColor(btnColors[i]);
            } else {
                // Unselected: dimmed
                w.sr.setColor(Colors.c(30, 30, 50));
            }
            w.sr.rect(btnX[i], btnY, btnW, btnH);
        }
        // Selected button border highlight
        w.sr.setColor(Colors.c(255, 255, 255, 60));
        w.sr.rect(btnX[w.practiceSelectedDifficulty], btnY, btnW, 2); // top border
        // START button
        w.sr.setColor(Colors.c(40, 180, 40));
        w.sr.rect(190, 270, 220, 50);
        // No-rewards notice background
        w.sr.setColor(Colors.c(40, 30, 10));
        w.sr.rect(100, 330, 400, 28);
        w.sr.end();

        // === LINE PASS ===
        w.sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        w.sr.setColor(Colors.c(60, 60, 120));
        for (int i = 0; i < 4; i++) w.sr.rect(btnX[i], btnY, btnW, btnH);
        w.sr.rect(190, 270, 220, 50);
        w.sr.end();

        // === TEXT PASS ===
        w.batch.setProjectionMatrix(w.camera.combined);
        w.batch.begin();
        // Title
        w.font.setColor(Colors.c(200, 200, 255));
        drawTextCentered("PRACTICE MODE", 300, 21);
        w.font.setColor(Colors.c(120, 120, 180));
        drawTextCentered("< Back", 540, 21);
        // Subtitle
        w.font.setColor(Colors.c(160, 160, 200));
        drawTextCentered("Choose AI Difficulty:", 300, 155);
        // Difficulty labels
        for (int i = 0; i < 4; i++) {
            w.font.setColor(i == w.practiceSelectedDifficulty ? Colors.c(255, 255, 255) : Colors.c(120, 120, 140));
            drawTextCentered(labels[i], btnX[i] + btnW / 2, btnY + 28);
        }
        // Description for selected difficulty
        String[] descriptions = {
            "AI moves very slowly. Good for testing cards.",
            "Standard AI speed. Balanced match.",
            "AI moves fast. A real challenge.",
            "AI at full power. Can you win?"
        };
        w.font.setColor(Colors.c(180, 180, 220));
        drawTextCentered(descriptions[w.practiceSelectedDifficulty], 300, 248);
        // START button
        w.font.setColor(Colors.c(255, 255, 255));
        drawTextCentered("PLAY PRACTICE MATCH", 300, 300);
        // No rewards notice
        w.font.setColor(Colors.c(200, 170, 60));
        drawTextCentered("No trophies or chests earned in practice", 300, 347);
        w.batch.end();
    }

}
