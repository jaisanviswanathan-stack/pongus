package com.pongus.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.pongus.game.Colors.*;

import com.pongus.game.entity.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Pongus - a ping pong game
 * @author Jaisan Viswanathan
 * LibGDX port
 */
public class PongusGame extends ApplicationAdapter {

    // All game state lives in GameWorld
    GameWorld w;

    // Network adapter injected by launcher (StubNetworkAdapter on desktop, GwtNetworkAdapter on HTML)
    private final com.pongus.game.network.NetworkAdapter pendingNetwork;

    public PongusGame() {
        this.pendingNetwork = new com.pongus.game.network.StubNetworkAdapter();
    }

    public PongusGame(com.pongus.game.network.NetworkAdapter network) {
        this.pendingNetwork = network;
    }

    // Timeout before falling back to AI (seconds)
    private static final float MATCHMAKING_TIMEOUT = 8f;

    // Ability system
    com.pongus.game.ability.AbilityManager abilityManager;

    // Renderer (front-end)
    GameRenderer renderer;

    // Progression constants
    private static final int SCORE_TO_WIN = 7;
    private static final String PROFILE_PREF = "PongusProfile";

    // Fake AI usernames for online-feel matches (realistic player names used when no real opponent found)
    private static final String[] FAKE_USERNAMES = {
        "jake_pong", "mia_2007", "CosmicJoe", "layla_k", "QuickHitter",
        "SlamDunk22", "PaddleMaster", "zero_lag", "BallWizard99", "ServeAce",
        "neon_striker", "RapidFire77", "shadow_k", "dylan22", "SwiftRef",
        "MegaSmash", "PongLegend", "flux_cap", "VoidStriker", "ElitePaddle",
        "RocketRally", "tom_slammer", "Q_Bounce", "iron_paddle", "StormSurfer22",
        "NightHawkX", "ThunderClap99", "alex_plays", "PhantomHit", "ArcadeGhost"
    };

    // Ability button layout constants (match GameRenderer button layout)
    private static final int ABIL_BTN_W = 80, ABIL_BTN_H = 24, ABIL_BTN_GAP = 3, ABIL_BOTTOM_Y = 394;
    private static final int P1_ABIL_X = 5, P2_ABIL_X = 515;

    @Override
    public void create() {
        Gdx.app.log("Pongus", "Game created");

        // Initialize shared game state
        w = new GameWorld();
        abilityManager = new com.pongus.game.ability.AbilityManager();
        renderer = new GameRenderer(w, abilityManager);

        // Wire up network adapter
        w.network = pendingNetwork;
        w.network.init("pongus", new com.pongus.game.network.NetworkCallbacks() {
            public void onReady() {
                Gdx.app.log("Pongus", "Netlib ready");
            }
            public void onPeerConnected(String peerId) {
                Gdx.app.log("Pongus", "Peer connected: " + peerId);
                // Determine host vs guest: whoever created the lobby is the host
                // The host is whoever called findMatch first (created a lobby).
                // We detect this by whether we already had a lobby created (isHost set in findMatch flow).
                Gdx.app.postRunnable(new Runnable() {
                    public void run() {
                        if (w.showingMatchmaking) {
                            startOnlineMatch();
                        }
                    }
                });
            }
            public void onMessage(String msg) {
                handleNetworkMessage(msg);
            }
            public void onDisconnected(String peerId) {
                Gdx.app.log("Pongus", "Peer disconnected");
                if (!w.showingMainMenu && w.isOnlineMatch && !w.isPaused) {
                    // Opponent quit mid-match — local player wins and gets full trophy reward
                    w.isOnlineMatch = false;
                    boolean localIsP1 = w.isHost;
                    handleMatchEnd(localIsP1);
                }
            }
        });

        // Camera: Y-down to match Swing coordinate system
        w.camera = new OrthographicCamera();
        w.camera.setToOrtho(true, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);

        // FitViewport: maintains 600:400 aspect ratio with letterboxing
        w.viewport = new FitViewport(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT, w.camera);

        // Rendering tools
        w.sr = new ShapeRenderer();
        w.batch = new SpriteBatch();

        // Default w.font (will be replaced with generated BitmapFonts later)
        w.font = new BitmapFont();
        w.font.getData().setScale(1, -1); // Flip for Y-down w.camera
        w.font.setColor(Color.WHITE);

        // For measuring text width
        w.glyphLayout = new GlyphLayout();

        // Dialog system
        w.dialog = new DialogSystem(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        w.dialog.setViewport(w.viewport);

        // Input handling: InputMultiplexer gives w.dialog priority over game input
        InputMultiplexer multiplexer = new InputMultiplexer();
        // Dialog processor first — when active it consumes all input
        multiplexer.addProcessor(w.dialog.getInputProcessor());
        // Game processor second — handles paddle movement + pause
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (w.showingArenaRoad) {
                    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE || keycode == com.badlogic.gdx.Input.Keys.BACK) {
                        w.showingArenaRoad = false;
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.DOWN || keycode == com.badlogic.gdx.Input.Keys.S) {
                        w.arenaRoadScrollOffset += 60;
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.UP || keycode == com.badlogic.gdx.Input.Keys.W) {
                        w.arenaRoadScrollOffset = Math.max(0, w.arenaRoadScrollOffset - 60);
                        return true;
                    }
                    return false;
                }
                if (w.showingPractice) {
                    if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE || keycode == com.badlogic.gdx.Input.Keys.BACK) {
                        w.showingPractice = false;
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.LEFT || keycode == com.badlogic.gdx.Input.Keys.A) {
                        w.practiceSelectedDifficulty = Math.max(0, w.practiceSelectedDifficulty - 1);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.RIGHT || keycode == com.badlogic.gdx.Input.Keys.D) {
                        w.practiceSelectedDifficulty = Math.min(3, w.practiceSelectedDifficulty + 1);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.ENTER || keycode == com.badlogic.gdx.Input.Keys.SPACE) {
                        startPracticeMatch();
                        return true;
                    }
                    return false;
                }
                if (w.showingCollection) {
                    java.util.ArrayList<String> ownedList = new java.util.ArrayList<String>(w.profile.cardCopies.keySet());
                    int maxScroll = Math.max(0, (ownedList.size() - 1) / 4 - 3);
                    if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
                        w.collectionScrollOffset = Math.min(w.collectionScrollOffset + 1, maxScroll);
                        return true;
                    }
                    if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
                        w.collectionScrollOffset = Math.max(0, w.collectionScrollOffset - 1);
                        return true;
                    }
                    if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                        w.showingCollection = false;
                        w.showingMainMenu = true;
                        if (w.collectionDeckDirty && w.profile != null) {
                            w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
                            w.collectionDeckDirty = false;
                        }
                        return true;
                    }
                }

                if (w.showingMainMenu) {
                    // On main menu, only handle save/load and ignore gameplay keys
                    if (keycode == Input.Keys.F5 && !w.dialog.isActive()) { saveGame(); return true; }
                    if (keycode == Input.Keys.F9 && !w.dialog.isActive()) { loadGame(); return true; }
                    if (keycode == Input.Keys.F8 && !w.dialog.isActive()) { deleteGame(); return true; }
                    if (keycode == Input.Keys.M && !w.dialog.isActive()) { handleCheatMenu(); return true; }
                    return false;
                }

                // ESC does nothing during a battle — you must finish the match
                // DEBUG: Test w.dialog system with T key (remove later)
                if (keycode == Input.Keys.T && !w.dialog.isActive()) {
                    testDialogSystem();
                    return true;
                }
                // Save/Load/Delete (F5/F9/F8) - only allowed on main menu, not during a match
                boolean inActiveMatch = !w.showingMainMenu && !w.showingCollection && !w.showingArenaRoad && !w.showingPractice;
                if (!inActiveMatch) {
                    if (keycode == Input.Keys.F5 && !w.dialog.isActive()) { saveGame(); return true; }
                    if (keycode == Input.Keys.F9 && !w.dialog.isActive()) { loadGame(); return true; }
                    if (keycode == Input.Keys.F8 && !w.dialog.isActive()) { deleteGame(); return true; }
                }

                // Slide deck navigation
                if (w.showingSlides) {
                    if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                        advanceSlide(); return true;
                    }
                    if (keycode == Input.Keys.ESCAPE) {
                        w.showingSlides = false; return true;
                    }
                }

                if (w.isPaused) return false;

                // Paddle movement flags (custom keybinds)
                if (keycode == w.player1UpKey) w.up1 = true;
                if (keycode == w.player1DownKey) w.down1 = true;
                if (keycode == w.player2UpKey) w.up2 = true;
                if (keycode == w.player2DownKey) w.down2 = true;

                // Cheat menu (M key) - only on main menu, not during a match
                if (keycode == Input.Keys.M && !w.dialog.isActive() && w.showingMainMenu) {
                    handleCheatMenu();
                    return true;
                }

                // Ability key activations
                handleAbilityKeyPress(keycode);
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == w.player1UpKey) w.up1 = false;
                if (keycode == w.player1DownKey) w.down1 = false;
                if (keycode == w.player2UpKey) w.up2 = false;
                if (keycode == w.player2DownKey) w.down2 = false;
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Slide deck: tap anywhere to advance
                if (w.showingSlides && !w.dialog.isActive()) {
                    advanceSlide();
                    return true;
                }
                if (w.matchResultPhase > 0) {
                    dismissMatchResult();
                    return true;
                }
                // Matchmaking cancel — any tap cancels and returns to menu
                if (w.showingMatchmaking) {
                    w.showingMatchmaking = false;
                    w.network.cancelSearch();
                    w.showingMainMenu = true;
                    return true;
                }
                // Arena road touch
                if (w.showingArenaRoad) {
                    w.tmpVec3.set(screenX, screenY, 0);
                    w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                        w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                    float vx = w.tmpVec3.x, vy = w.tmpVec3.y;

                    // Top bar tap = close
                    if (vy < 34) {
                        w.showingArenaRoad = false;
                        return true;
                    }
                    // Find tapped arena row
                    int rowH = 68;
                    int expandedExtra = 44;
                    int y = 34 - w.arenaRoadScrollOffset;
                    for (int ai = 7; ai >= 0; ai--) {
                        boolean expanded = (ai == w.arenaRoadExpandedArena);
                        int h = rowH + (expanded ? expandedExtra : 0);
                        if (vy >= y && vy < y + h) {
                            if (w.arenaRoadExpandedArena == ai) {
                                w.arenaRoadExpandedArena = -1;
                            } else {
                                w.arenaRoadExpandedArena = ai;
                            }
                            return true;
                        }
                        y += h;
                    }
                    return true;
                }
                if (w.showingPractice) {
                    w.tmpVec3.set(screenX, screenY, 0);
                    w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                        w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                    float vx = w.tmpVec3.x, vy = w.tmpVec3.y;
                    // Back button: top bar (vy < 34)
                    if (vy < 34) { w.showingPractice = false; return true; }
                    // Difficulty buttons: 4 buttons centered at y=200, each 120w x 50h
                    // Easy: x=30-150, Normal: x=160-280, Hard: x=290-410, Impossible: x=420-540
                    if (vy >= 175 && vy <= 225) {
                        if (vx >= 30 && vx < 150) { w.practiceSelectedDifficulty = 0; return true; }
                        if (vx >= 160 && vx < 280) { w.practiceSelectedDifficulty = 1; return true; }
                        if (vx >= 290 && vx < 410) { w.practiceSelectedDifficulty = 2; return true; }
                        if (vx >= 420 && vx < 540) { w.practiceSelectedDifficulty = 3; return true; }
                    }
                    // START button: centered x=190-410, y=270-320
                    if (vx >= 190 && vx <= 410 && vy >= 270 && vy <= 320) {
                        startPracticeMatch();
                        return true;
                    }
                    return true;
                }
                if (w.showingCollection) {
                    // Convert to virtual coords
                    w.tmpVec3.set(screenX, screenY, 0);
                    w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                        w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                    float vx = w.tmpVec3.x, vy = w.tmpVec3.y;

                    // Back button (x=530-590, y=5-29)
                    if (vx >= 530 && vx <= 590 && vy >= 5 && vy <= 29) {
                        w.showingCollection = false;
                        w.showingMainMenu = true;
                        if (w.collectionDeckDirty && w.profile != null) {
                            w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
                            w.collectionDeckDirty = false;
                        }
                        return true;
                    }

                    // === CARD DETAIL POPUP (intercept all touches when popup is open) ===
                    if (!w.collectionSelectedCard.isEmpty()) {
                        final int PX = 110, PY = 68, PW = 380, PH = 250;
                        final int BTN_Y = PY + PH - 44;
                        final int BTN_H = 36;
                        String sc = w.collectionSelectedCard;
                        // Close X button
                        if (vx >= PX + PW - 28 && vx <= PX + PW - 5 && vy >= PY + 5 && vy <= PY + 26) {
                            w.collectionSelectedCard = "";
                            return true;
                        }
                        // Inside popup — check button row
                        if (vx >= PX && vx <= PX + PW && vy >= PY && vy <= PY + PH) {
                            if (vy >= BTN_Y && vy <= BTN_Y + BTN_H) {
                                boolean canUpgrade = w.profile.canUpgrade(sc);
                                if (canUpgrade) {
                                    if (vx >= PX + 8 && vx <= PX + 180) {
                                        // UPGRADE button
                                        w.profile.upgradeCard(sc);
                                        w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
                                        return true;
                                    }
                                    if (vx >= PX + 188 && vx <= PX + 372) {
                                        // USE / REMOVE button (right)
                                        toggleCardInDeck(sc);
                                        return true;
                                    }
                                } else {
                                    if (vx >= PX + 8 && vx <= PX + 372) {
                                        // Full-width USE / REMOVE button
                                        toggleCardInDeck(sc);
                                        return true;
                                    }
                                }
                            }
                            return true; // consume all taps inside popup
                        }
                        // Tapped outside popup — dismiss
                        w.collectionSelectedCard = "";
                        return true;
                    }

                    // Left panel: tap deck card to open its popup; empty slots ignored
                    int slotH = 36, slotPad = 4, slotStartY = 44;
                    if (vx >= 4 && vx <= 196) {
                        for (int i = 0; i < 8; i++) {
                            int slotY = slotStartY + i * (slotH + slotPad);
                            if (slotY + slotH > 356) break;
                            if (vy >= slotY && vy <= slotY + slotH) {
                                String card = w.profile.deck[i];
                                if (card != null) w.collectionSelectedCard = card;
                                return true;
                            }
                        }
                    }

                    // Right panel: tap card to open its detail popup
                    int CARD_W = 90, CARD_H = 70, CARD_PAD = 5;
                    int gridX = 205, gridY = 38;
                    java.util.ArrayList<String> ownedList = new java.util.ArrayList<String>(w.profile.cardCopies.keySet());
                    int startIdx = w.collectionScrollOffset * 4;
                    for (int i = startIdx; i < Math.min(startIdx + 16, ownedList.size()); i++) {
                        int col = (i - startIdx) % 4;
                        int row = (i - startIdx) / 4;
                        int cx = gridX + col * (CARD_W + CARD_PAD);
                        int cy = gridY + row * (CARD_H + CARD_PAD);
                        if (vx >= cx && vx <= cx + CARD_W && vy >= cy && vy <= cy + CARD_H) {
                            w.collectionSelectedCard = ownedList.get(i);
                            return true;
                        }
                    }
                    return true; // consume all touches on collection screen
                }

                // Gameplay touch — relative drag + ability buttons
                boolean inGameplay = !w.showingMainMenu && !w.showingCollection
                    && !w.showingArenaRoad && !w.showingPractice && !w.showingCountdown;
                if (inGameplay) {
                    w.tmpVec3.set(screenX, screenY, 0);
                    w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                        w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                    float vx = w.tmpVec3.x;
                    float vy = w.tmpVec3.y;
                    // Ability buttons — only on touchscreen, only for key-press abilities.
                    // Buttons are stacked from ABIL_BOTTOM_Y upward, skipping passive abilities.
                    if (vx >= P1_ABIL_X && vx <= P1_ABIL_X + ABIL_BTN_W) {
                        int slot = 0;
                        for (int bi = 0; bi < w.player1DrawnCards.size() && slot < 4; bi++) {
                            if (!renderer.abilityManager.needsKeyPress(w.player1DrawnCards.get(bi))) continue;
                            int bY = ABIL_BOTTOM_Y - (slot + 1) * ABIL_BTN_H - slot * ABIL_BTN_GAP;
                            if (vy >= bY && vy <= bY + ABIL_BTN_H) {
                                handleAbilityKeyPress(w.player1AbilityKey);
                                return true;
                            }
                            slot++;
                        }
                    }
                    if (!w.singlePlayer && vx >= P2_ABIL_X && vx <= P2_ABIL_X + ABIL_BTN_W) {
                        int slot = 0;
                        for (int bi = 0; bi < w.player2DrawnCards.size() && slot < 4; bi++) {
                            if (!renderer.abilityManager.needsKeyPress(w.player2DrawnCards.get(bi))) continue;
                            int bY = ABIL_BOTTOM_Y - (slot + 1) * ABIL_BTN_H - slot * ABIL_BTN_GAP;
                            if (vy >= bY && vy <= bY + ABIL_BTN_H) {
                                handleAbilityKeyPress(w.player2AbilityKey);
                                return true;
                            }
                            slot++;
                        }
                    }
                    // Register drag pointer per side (left = P1, right = P2)
                    if (vx < 300 && w.p1DragPointer == -1) {
                        w.p1DragPointer = pointer;
                        w.p1LastDragY   = vy;
                        return true;
                    }
                    if (vx >= 300 && w.p2DragPointer == -1) {
                        w.p2DragPointer = pointer;
                        w.p2LastDragY   = vy;
                        return true;
                    }
                }

                if (!w.showingMainMenu || w.dialog.isActive()) return false;
                // Unproject screen coords to virtual coords
                w.tmpVec3.set(screenX, screenY, 0);
                w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                    w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                float vx = w.tmpVec3.x;
                float vy = w.tmpVec3.y;

                // Check buttons
                int btnIdx = renderer.getMenuButtonAt(vx, vy);
                if (btnIdx >= 0) {
                    handleMenuButtonClick(btnIdx);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                // Relative drag: apply delta to paddle position directly
                if (pointer != w.p1DragPointer && pointer != w.p2DragPointer) return false;
                w.tmpVec3.set(screenX, screenY, 0);
                w.camera.unproject(w.tmpVec3, w.viewport.getScreenX(), w.viewport.getScreenY(),
                    w.viewport.getScreenWidth(), w.viewport.getScreenHeight());
                float vy = w.tmpVec3.y;
                if (pointer == w.p1DragPointer && w.player1StunTimer <= 0) {
                    float delta = vy - w.p1LastDragY;
                    // Clamp delta to prevent first-frame coordinate mismatch from teleporting paddle
                    delta = Math.max(-80f, Math.min(80f, delta));
                    int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
                    w.paddle1Y = Math.max(15, Math.min(400 - p1H - 15, (int)(w.paddle1Y + delta)));
                    w.p1LastDragY = vy;
                    return true;
                }
                if (pointer == w.p2DragPointer && !w.singlePlayer && w.player2StunTimer <= 0) {
                    float delta = vy - w.p2LastDragY;
                    delta = Math.max(-80f, Math.min(80f, delta));
                    int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
                    w.paddle2Y = Math.max(15, Math.min(400 - p2H - 15, (int)(w.paddle2Y + delta)));
                    w.p2LastDragY = vy;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                // Release drag pointer — paddle stays where it stopped
                if (pointer == w.p1DragPointer) w.p1DragPointer = -1;
                if (pointer == w.p2DragPointer) w.p2DragPointer = -1;
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

        // Load player profile (or create default if first run)
        w.profile = PlayerProfile.load(Gdx.app.getPreferences(PROFILE_PREF));

        // Always start at main menu
        w.showingMainMenu = true;
        w.isPaused = true;

        // Load sprite/PNG textures (Phase 9) — gracefully skips missing PNGs
        loadTextures();
    }

    private void loadTextures() {
        // Card icons — assets/cards/<id>.png
        // Use allAbilities list since CardMetadata has no CARD_IDS static array
        String[] cardIds = w.allAbilities;
        for (String id : cardIds) {
            try {
                com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("cards/" + id + ".png"));
                w.cardTextureMap.put(id, t);
            } catch (Exception e) { /* PNG not found — shape fallback */ }
        }
        // Paddle sprites
        try { w.texPaddle1 = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("sprites/paddle_blue.png")); } catch (Exception e) {}
        try { w.texPaddle2 = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("sprites/paddle_red.png")); } catch (Exception e) {}
        // Ball sprites
        try { w.texBall      = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("sprites/ball.png")); } catch (Exception e) {}
        try { w.texBallFire  = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("sprites/ball_fire.png")); } catch (Exception e) {}
        // Chest sprites
        String[] chestTypes = {"silver", "gold", "magical", "arena"};
        for (int i = 0; i < chestTypes.length; i++) {
            try {
                w.texChests[i] = new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("sprites/chest_" + chestTypes[i] + ".png"));
            } catch (Exception e) {}
        }
        // Arena backgrounds (legacy sprites/ path)
        for (int i = 0; i < 8; i++) {
            try {
                w.texArenas[i] = new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("sprites/arena_bg_" + (i + 1) + ".png"));
            } catch (Exception e) {}
        }
        // Arena backgrounds (new backgrounds/ path)
        String[] bgNames = {"bg_training", "bg_pong_pit", "bg_shadow_realm", "bg_gravity_falls",
                            "bg_portal_fields", "bg_storm", "bg_time_rift", "bg_neon_nexus"};
        for (int i = 0; i < 8; i++) {
            try {
                w.arenaBgTextures[i] = new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("backgrounds/" + bgNames[i] + ".png"));
            } catch (Exception e) {}
        }
        // Arena badge icons
        for (int i = 0; i < 8; i++) {
            try {
                w.arenaIconTextures[i] = new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("arenas/icon_" + i + ".png"));
            } catch (Exception e) {}
        }
        // Power-up icons
        String[] puIds = {"speed", "slow", "paddle", "reverse", "giant", "shrink", "multiball",
                          "jumpscare", "dangerzone", "teleport", "fireball", "zigzag", "split",
                          "mirror", "centerwall", "invisiblewalls", "shrinkpaddles", "jackpot"};
        for (String id : puIds) {
            try {
                w.powerupTextures.put(id, new com.badlogic.gdx.graphics.Texture(
                    Gdx.files.internal("powerups/pu_" + id + ".png")));
            } catch (Exception e) {}
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        w.elapsedTime += delta;

        // Matchmaking timeout — tick while searching; fall back to AI if nobody found
        if (w.showingMatchmaking) {
            w.matchmakingTimer += delta;
            if (w.matchmakingTimer >= MATCHMAKING_TIMEOUT) {
                startAIFallback();
            }
        }

        // Toast timer countdown (always ticks so toasts fade out even when paused)
        if (w.toastP1Timer > 0) w.toastP1Timer -= Gdx.graphics.getDeltaTime();
        if (w.toastP2Timer > 0) w.toastP2Timer -= Gdx.graphics.getDeltaTime();

        // Match result animation tick (always runs when active)
        if (w.matchResultPhase > 0) {
            w.matchResultTimer += Gdx.graphics.getDeltaTime();
            // Phase 1: animate trophy count
            if (w.matchResultPhase == 1) {
                int step = w.matchResultTrophyDelta > 0 ? 1 : -1;
                if (w.matchResultTrophyDisplay != w.matchResultTrophyTarget && w.matchResultTimer > 0.05f) {
                    w.matchResultTrophyDisplay += step;
                    w.matchResultTimer = 0f;
                }
                if (w.matchResultTrophyDisplay == w.matchResultTrophyTarget) {
                    // Auto-advance after 0.5s pause
                    if (w.matchResultTimer > 0.5f) {
                        w.matchResultPhase = w.matchResultChestType.isEmpty() ?
                            (w.matchResultNewArena ? 3 : 4) : 2;
                        w.matchResultTimer = 0f;
                    }
                }
            }
            // Phases 2, 3: auto-advance after 2.5 seconds if player doesn't tap
            if (w.matchResultPhase == 2 && w.matchResultTimer > 2.5f) {
                w.matchResultPhase = w.matchResultNewArena ? 3 : 4;
                w.matchResultTimer = 0f;
            }
            if (w.matchResultPhase == 3 && w.matchResultTimer > 2.5f) {
                w.matchResultPhase = 4;
                w.matchResultTimer = 0f;
            }
            if (w.matchResultPhase == 4 && w.matchResultTimer > 1.0f) {
                w.matchResultPhase = 0;
                returnToMainMenu();
            }
        }

        // Countdown timer (ticks even while game is paused)
        if (w.showingCountdown && !w.showingMainMenu) {
            w.countdownTimer -= delta;
            if (w.countdownTimer <= 0f) {
                w.showingCountdown = false;
                w.isPaused = false;
            }
        }

        // Game update (skip if paused, w.dialog active, or main menu)
        if (!w.isPaused && !w.dialog.isActive() && !w.showingMainMenu) {
            update(delta);
        }

        // Menu animation timer (always ticks)
        if (w.showingMainMenu) w.menuAnimationTimer++;

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update w.camera/w.viewport
        w.camera.update();

        // Enable alpha blending for the entire frame
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (w.showingMatchmaking) {
            renderer.renderMatchmaking();
        } else if (w.showingArenaRoad) {
            renderer.renderArenaRoad();
        } else if (w.showingPractice) {
            renderer.renderPractice();
        } else if (w.showingCollection) {
            renderer.renderCollection();
        } else if (w.showingMainMenu) {
            renderer.renderMainMenu();
        } else {
            renderer.renderGameplay();
            if (w.showingCountdown) {
                renderer.renderCountdown();
            }
            if (w.matchResultPhase > 0) {
                renderer.renderMatchResult();
            }
        }

        // === DIALOG OVERLAY (renders its own shape + text passes) ===
        w.dialog.render(w.sr, w.batch, w.font, w.viewport);
    }


    // ==================== MENU ACTIONS ====================

    private void handleMenuButtonClick(int buttonIndex) {
        switch (buttonIndex) {
            case 0: startBattleFromMenu(); break;  // BATTLE button
            case 1: // Collection button
                w.showingMainMenu = false;
                w.showingCollection = true;
                w.collectionScrollOffset = 0;
                w.collectionSelectedCard = "";
                w.collectionSelectedSlot = -1;
                break;
            case 2: startMultiplayerFromMenu(); break;    // 2P Local sec button
            case 3: // Practice screen (Phase 10)
                w.showingPractice = true;
                w.practiceSelectedDifficulty = 1;
                break;
            case 4: showSettingsFromMenu(); break;        // Settings sec button
            case 10: handleChestOpen(0); break;
            case 11: handleChestOpen(1); break;
            case 12: handleChestOpen(2); break;
            case 13: handleChestOpen(3); break;
            case 20: // Open arena road
                w.showingArenaRoad = true;
                w.arenaRoadScrollOffset = 0;
                w.arenaRoadExpandedArena = -1;
                break;
            case 30: claimDailyChest(); break;
            case 40: openSlides(); break;
        }
    }

    private void startStoryFromMenu() {
        int selected = (w.menuSelectedLevel >= 0) ? w.menuSelectedLevel : w.storyModeLevel - 1;
        if (selected >= w.storyModeMaxLevel) selected = w.storyModeMaxLevel - 1;
        w.storyModeLevel = selected + 1;
        w.storyModeActive = true;
        w.storyPlayerScore = 0;
        w.storyBossScore = 0;

        String currentBoss = w.storyBossNames[selected];
        int scoreNeeded = w.storyLevelScoreToWin[selected];

        w.dialog.showMessage("Battle Start!",
            "=== LEVEL " + (selected + 1) + " ===\n\n" +
            "BOSS: " + currentBoss + "\n" +
            "Score " + scoreNeeded + " points to win!\n\n" +
            "Get ready!",
            new DialogSystem.MessageCallback() {
                public void onClose() {
                    startStoryLevel();
                    w.showingMainMenu = false;
                    w.isPaused = false;
                }
            });
    }

    private void startStoryLevel() {
        resetGameState();
        w.singlePlayer = true;
        w.aiDifficulty = Math.min(w.storyModeLevel, 3); // Scale difficulty with level
        w.learningAIEnabled = false;
        w.player2Name = w.storyBossNames[w.storyModeLevel - 1];
        spawnMapPowerUp();
    }

    private void openSlides() {
        w.showingSlides = true;
        w.slideIndex = 0;
    }

    private void advanceSlide() {
        if (w.slideIndex < GameRenderer.SLIDE_COUNT - 1) {
            w.slideIndex++;
        } else {
            w.showingSlides = false;
        }
    }

    private void startSinglePlayerFromMenu() {
        // Scale difficulty by trophies so the opponent feels like real matchmaking
        int trophies = (w.profile != null) ? w.profile.trophies : 0;
        if (trophies < 300) { w.aiDifficulty = 1; w.aiSpeedMultiplier = 0.60; }
        else if (trophies < 800) { w.aiDifficulty = 2; w.aiSpeedMultiplier = 1.0; }
        else { w.aiDifficulty = 3; w.aiSpeedMultiplier = 1.0; }
        w.singlePlayer = true;
        w.storyModeActive = false;
        w.learningAIEnabled = false;
        resetGameState();
        // Use the randomly chosen display name for history too
        w.player2Name = w.player2DisplayName;
        applyDeckAndSynergy();
        w.showingMainMenu = false;
        w.isPaused = true;  // stay paused during countdown
        w.showingCountdown = true;
        w.countdownTimer = 3.0f;
        spawnMapPowerUp();
    }

    /** BATTLE button handler — tries online first, falls back to AI after timeout. */
    private void startBattleFromMenu() {
        if (w.network.isAvailable()) {
            // Show searching screen and let matchmaking timer run
            w.showingMatchmaking = true;
            w.matchmakingTimer = 0f;
            w.isHost = false; // bridge sends H:1 or H:0 before onPeerConnected to set this
            int arenaIndex = (w.profile != null) ? ArenaConfig.getArenaIndex(w.profile.trophies) : 0;
            w.network.findMatch(arenaIndex);
        } else {
            startSinglePlayerFromMenu();
        }
    }

    /** Called from onPeerConnected — a real opponent was found. */
    private void startOnlineMatch() {
        w.showingMatchmaking = false;
        w.isOnlineMatch = true;
        w.singlePlayer = false;
        w.storyModeActive = false;
        w.learningAIEnabled = false;
        resetGameState();
        // Assign opponent a random realistic name so the match feels real
        w.player2Name = w.player2DisplayName;
        applyDeckAndSynergy();
        w.showingMainMenu = false;
        w.isPaused = true;
        w.showingCountdown = true;
        w.countdownTimer = 3.0f;
        spawnMapPowerUp();
    }

    /** Called by matchmaking timer when no opponent found — silently start AI. */
    private void startAIFallback() {
        w.showingMatchmaking = false;
        w.network.cancelSearch();
        w.isOnlineMatch = false;
        startSinglePlayerFromMenu();
    }

    /** Parse and apply a network message from the peer. */
    private void handleNetworkMessage(String msg) {
        if (msg == null || msg.length() < 2) return;
        char type = msg.charAt(0);
        if (type == 'P') {
            // Paddle position: "P:<y>"
            try {
                w.networkOpponentPaddleY = Float.parseFloat(msg.substring(2));
            } catch (NumberFormatException e) { /* ignore malformed */ }
        } else if (type == 'E') {
            // Match end signal from peer — end locally too if not already ended
            if (!w.showingMainMenu && w.isOnlineMatch && msg.length() >= 3) {
                boolean p1Won = msg.charAt(2) == '1';
                handleMatchEnd(p1Won);
            }
        } else if (type == 'A') {
            // Ability used: "A:1" or "A:2"
            if (msg.length() >= 3) {
                int playerN = msg.charAt(2) - '0';
                if (playerN == 1) triggerAbilityForPlayer(1);
                else              triggerAbilityForPlayer(2);
            }
        } else if (type == 'H') {
            // Host assignment: "H:0" = you are guest (P2), "H:1" = you are host (P1)
            if (msg.length() >= 3) {
                w.isHost = msg.charAt(2) == '1';
            }
        }
    }

    /** Directly trigger an ability activation for the given player (used for network sync). */
    private void triggerAbilityForPlayer(int player) {
        int key = (player == 1) ? w.player1AbilityKey : w.player2AbilityKey;
        handleAbilityKeyPress(key);
    }

    private void startPracticeMatch() {
        w.showingPractice = false;
        w.singlePlayer = true;
        w.storyModeActive = false;
        w.learningAIEnabled = false;
        w.player2Name = "AI";
        float[] speeds = {0.4f, 0.7f, 1.0f, 1.4f};
        resetGameState();
        w.aiSpeedMultiplier = speeds[Math.min(w.practiceSelectedDifficulty, 3)];
        w.practiceMode = true;
        applyDeckAndSynergy();
        w.showingMainMenu = false;
        w.isPaused = false;
        spawnMapPowerUp();
    }

    private void startMultiplayerFromMenu() {
        w.singlePlayer = false;
        w.storyModeActive = false;
        w.aiDifficulty = 1;
        w.learningAIEnabled = false;
        w.player2Name = "Player 2";

        resetGameState();
        applyDeckAndSynergy();
        w.showingMainMenu = false;
        w.isPaused = false;
        spawnMapPowerUp();
    }

    private void showSettingsFromMenu() {
        configureKeybinds();
    }

    private void claimDailyChest() {
        if (w.profile == null) return;
        long now = System.currentTimeMillis();
        if (w.profile.lastDailyChestMs != 0 && (now - w.profile.lastDailyChestMs) < 86400000L) {
            long remaining = 86400000L - (now - w.profile.lastDailyChestMs);
            long h = remaining / 3600000L;
            long m = (remaining % 3600000L) / 60000L;
            w.dialog.showMessage("Daily Chest", "Next free chest in " + h + "h " + m + "m", null);
            return;
        }
        // Open immediately — no slot needed
        int arenaIdx = ArenaConfig.getArenaIndex(w.profile.trophies);
        String[] pool = ArenaConfig.getAllUnlockedCards(arenaIdx);
        String msg = "Silver Chest OPENED!\n\nCards received:\n";
        if (pool != null && pool.length > 0) {
            for (int i = 0; i < 2; i++) {
                int ri = com.badlogic.gdx.math.MathUtils.random(pool.length - 1);
                String card = pool[ri];
                w.profile.addCardCopy(card, 1);
                msg = msg + "  + " + abilityManager.getShortName(card) + "\n";
            }
        }
        int gold = com.badlogic.gdx.math.MathUtils.random(20, 50);
        w.profile.gold += gold;
        msg = msg + "\n+" + gold + " gold!";
        w.profile.lastDailyChestMs = now;
        w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
        final String finalMsg = msg;
        w.dialog.showMessage("Daily Chest!", finalMsg, null);
    }

    private void handleChestOpen(int slotIdx) {
        if (w.profile == null) return;
        PlayerProfile.ChestSlot slot = w.profile.chestSlots[slotIdx];
        if (slot.type == null) {
            w.dialog.showMessage("Empty Slot", "This chest slot is empty.",
                new DialogSystem.MessageCallback() { public void onClose() {} });
            return;
        }
        if (!ChestSystem.isChestReady(slot)) {
            // Each tap shaves 1 second off the unlock timer
            slot.unlockTimeMs -= 1000;
            w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
            renderer.invalidateActivityCache();
            return;
        }
        int arenaIdx = ArenaConfig.getArenaIndex(w.profile.trophies);
        String[] cards = ChestSystem.openChest(w.profile, slotIdx, arenaIdx);
        String chestName = ChestSystem.getChestDisplayName(w.profile.chestSlots[slotIdx].type);
        String msg = chestName + " OPENED!\n\nCards received:\n";
        if (cards != null) {
            for (String card : cards) {
                msg = msg + "  + " + abilityManager.getShortName(card) + "\n";
            }
        }
        w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
        final String finalMsg = msg;
        w.dialog.showMessage("Chest Opened!", finalMsg,
            new DialogSystem.MessageCallback() { public void onClose() {} });
    }

    void returnToMainMenu() {
        // If quitting an active online match intentionally, count it as a loss
        if (w.isOnlineMatch && !w.showingMainMenu && w.matchResultPhase == 0 && w.profile != null) {
            w.profile.trophies = TrophySystem.calculateTrophiesAfterLoss(w.profile.trophies);
            w.profile.arenaIndex = TrophySystem.getArenaIndex(w.profile.trophies);
            w.profile.totalMatches++;
            String histEntry = "L|" + w.player2Name + "|" + TrophySystem.TROPHIES_PER_LOSS;
            w.profile.matchHistory.add(0, histEntry);
            if (w.profile.matchHistory.size() > 5) w.profile.matchHistory.remove(5);
            w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
        }
        w.showingMainMenu = true;
        w.showingCollection = false;
        w.showingArenaRoad = false;
        w.showingPractice = false;
        w.showingCountdown = false;
        w.showingMatchmaking = false;
        w.isOnlineMatch = false;
        w.isHost = false;
        w.networkOpponentPaddleY = 200f;
        w.p1DragPointer = -1;
        w.p2DragPointer = -1;
        w.isPaused = true;
        w.storyModeActive = false;
        w.menuSelectedLevel = w.storyModeLevel - 1;
        w.menuAnimationTimer = 0;
        renderer.invalidateActivityCache();
        DeckManager.resetArenaModifier(w);
        w.activeSynergyKey = "";
    }

    private boolean isCardInDeck(String card) {
        if (w.profile == null || w.profile.deck == null) return false;
        for (String d : w.profile.deck) {
            if (card.equals(d)) return true;
        }
        return false;
    }

    /** Toggle a card in/out of the deck. Adds to first empty slot; removes from all slots if already in deck. */
    private void toggleCardInDeck(String card) {
        if (w.profile == null) return;
        boolean inDeck = isCardInDeck(card);
        if (inDeck) {
            for (int j = 0; j < 8; j++) {
                if (card.equals(w.profile.deck[j])) w.profile.deck[j] = null;
            }
        } else {
            for (int j = 0; j < 8; j++) {
                if (w.profile.deck[j] == null) {
                    w.profile.deck[j] = card;
                    break;
                }
            }
        }
        w.collectionDeckDirty = true;
        w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
    }

    /** Apply synergy bonus and arena modifier for the current match. Called at match start. */
    private void applyDeckAndSynergy() {
        w.activeSynergyKey = "";
        DeckManager.resetArenaModifier(w);
        if (w.profile == null) return;
        if (DeckManager.isDeckValid(w.profile.deck)) {
            // Detect and apply synergy
            DeckManager.Synergy syn = DeckManager.detectSynergy(w.profile.deck);
            if (syn != null) {
                w.activeSynergyKey = syn.bonusKey;
                final String bannerText = DeckManager.getSynergyBannerText(syn);
                w.dialog.showMessage("Synergy Bonus!", bannerText, new DialogSystem.MessageCallback() {
                    public void onClose() {}
                });
            }
        }
        // Apply arena modifier based on player's current arena
        ArenaConfig.Arena arena = ArenaConfig.getArenaForTrophies(w.profile.trophies);
        DeckManager.applyArenaModifier(w, arena.modifierKey);
    }

    /** Check if a non-story match has ended (timer-based with mercy rule and sudden death). */
    private void checkMatchEnd() {
        if (w.dialog.isActive()) return;
        if (!w.matchTimerActive) return;
        // Mercy rule: gap of 15+ ends match immediately
        int gap = Math.abs(w.scorePlayer1 - w.scorePlayer2);
        if (gap >= 15) {
            handleMatchEnd(w.scorePlayer1 > w.scorePlayer2);
            return;
        }
        // Timer end
        if (w.matchTimer >= GameWorld.MATCH_DURATION_TICKS) {
            if (w.scorePlayer1 == w.scorePlayer2) {
                activateSuddenDeath();
            } else {
                handleMatchEnd(w.scorePlayer1 > w.scorePlayer2);
            }
        }
    }

    private void activateSuddenDeath() {
        if (w.suddenDeathMode) return; // already active
        w.suddenDeathMode = true;
        w.overtimeActive = true;
        // Reset main ball to center, slow speed, heading toward P1
        w.ballX = 292; w.ballY = 192;
        w.ballSpeed = 3.0;
        w.ballVelX = -2;
        w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
        w.ballXR = 0; w.ballYR = 0;
        // Spawn second ball at center, slow speed, heading toward P2
        w.ball2X = 292; w.ball2Y = 192;
        w.ball2Speed = 1.0;
        w.ball2VelX = 2;
        w.ball2VelY = (Math.random() > 0.5) ? 2 : -2;
        w.ball2XR = 0; w.ball2YR = 0;
        w.ball2Active = true;
    }

    /** Called when a point is scored — draws a card at thresholds 2,5,9,14,20... */
    private void onPointScored(int player) {
        if (w.profile == null) return;
        // Check threshold — only draw when score hits the next threshold
        int currentScore = (player == 1) ? w.scorePlayer1 : w.scorePlayer2;
        int drawIndex = (player == 1) ? w.player1DrawIndex : w.player2DrawIndex;
        if (drawIndex >= GameWorld.DRAW_THRESHOLDS.length) return;
        if (currentScore < GameWorld.DRAW_THRESHOLDS[drawIndex]) return;
        // Threshold reached — advance index
        if (player == 1) w.player1DrawIndex++;
        else w.player2DrawIndex++;

        String[] deck;
        if (player == 1) {
            // Player 1: draw from their profile deck
            deck = w.profile.deck;
        } else {
            // Player 2 (AI): draw from current arena card pool
            int arenaIdx = ArenaConfig.getArenaIndex(w.profile.trophies);
            deck = ArenaConfig.getAllUnlockedCards(arenaIdx);
        }
        // Build list of valid (non-null) cards
        java.util.ArrayList<String> validCards = new java.util.ArrayList<String>();
        for (String c : deck) {
            if (c != null && !c.isEmpty()) validCards.add(c);
        }
        if (validCards.isEmpty()) return;

        // Draw random card
        String card = validCards.get(com.badlogic.gdx.math.MathUtils.random(validCards.size() - 1));

        // Get current in-match level from player abilities map
        java.util.LinkedHashMap<String, Integer> abilities =
            (player == 1) ? w.player1Abilities : w.player2Abilities;
        java.util.ArrayList<String> drawnCards =
            (player == 1) ? w.player1DrawnCards : w.player2DrawnCards;

        int currentLevel = abilities.containsKey(card) ? abilities.get(card) : 0;

        if (currentLevel == 0) {
            // First draw — start at permanent level
            int permLevel = (player == 1 && w.profile != null) ? w.profile.getPermLevel(card) : 1;
            abilities.put(card, permLevel);
            drawnCards.add(card);
            // Show toast
            String name = com.pongus.game.CardMetadata.getDisplayName(card);
            if (player == 1) {
                w.toastP1Text = "+" + name + " (Lv" + permLevel + ")";
                w.toastP1Timer = 2.0f;
                w.toastP1LevelUp = false;
            } else {
                w.toastP2Text = "+" + name + " (Lv" + permLevel + ")";
                w.toastP2Timer = 2.0f;
                w.toastP2LevelUp = false;
            }
        } else {
            // Duplicate draw — level up in-match
            int newLevel = currentLevel + 1;
            abilities.put(card, newLevel);
            String name = com.pongus.game.CardMetadata.getDisplayName(card);
            if (player == 1) {
                w.toastP1Text = name + " LV UP! -> Level " + newLevel;
                w.toastP1Timer = 2.0f;
                w.toastP1LevelUp = true;
            } else {
                w.toastP2Text = name + " LV UP! -> Level " + newLevel;
                w.toastP2Timer = 2.0f;
                w.toastP2LevelUp = true;
            }
        }
    }

    /** Handle match end: update trophies, save profile, start result animation. */
    private void handleMatchEnd(boolean player1Won) {
        w.isPaused = true;
        w.matchTimerActive = false;
        // Both players notify each other of match result
        if (w.isOnlineMatch) {
            w.network.send("E:" + (player1Won ? "1" : "2"));
        }
        if (w.practiceMode) {
            // Practice: no trophies, no chest, no history
            w.practiceMode = false;
            w.matchResultPhase = 0; // no result animation
            returnToMainMenu();
            return;
        }
        if (w.profile == null) w.profile = PlayerProfile.createDefault();
        w.profile.totalMatches++;
        int oldTrophies = w.profile.trophies;
        int trophyChange;
        if (player1Won) {
            trophyChange = TrophySystem.TROPHIES_PER_WIN;
            w.profile.trophies = TrophySystem.calculateTrophiesAfterWin(w.profile.trophies);
        } else {
            trophyChange = TrophySystem.TROPHIES_PER_LOSS;
            w.profile.trophies = TrophySystem.calculateTrophiesAfterLoss(w.profile.trophies);
        }
        // Record match result in persistent history
        String histEntry = (player1Won ? "W" : "L") + "|" + w.player2Name + "|" + trophyChange;
        w.profile.matchHistory.add(0, histEntry);
        if (w.profile.matchHistory.size() > 5) w.profile.matchHistory.remove(5);
        if (w.profile.trophies > w.profile.peakTrophies) {
            w.profile.peakTrophies = w.profile.trophies;
        }
        w.profile.arenaIndex = TrophySystem.getArenaIndex(w.profile.trophies);
        if (player1Won) {
            w.profile.wins++;
            w.profile.gold += 20;
        }
        // Award a chest if there's an empty slot
        String chestType = "";
        if (player1Won && ChestSystem.hasEmptySlot(w.profile)) {
            chestType = ChestSystem.getRandomChestDrop();
            ChestSystem.addChest(w.profile, chestType);
        }
        w.profile.save(Gdx.app.getPreferences(PROFILE_PREF));
        // Invalidate activity cache so menu updates
        renderer.invalidateActivityCache();
        // Start the result animation
        w.matchResultPhase = 1;
        w.matchResultTimer = 0f;
        w.matchResultWon = player1Won;
        w.matchResultTrophyDisplay = oldTrophies;
        w.matchResultTrophyTarget = w.profile.trophies;
        w.matchResultTrophyDelta = player1Won ? TrophySystem.TROPHIES_PER_WIN : -TrophySystem.TROPHIES_PER_LOSS;
        w.matchResultChestType = chestType;
        w.matchResultNewArena = TrophySystem.didArenaChange(oldTrophies, w.profile.trophies);
        w.matchResultArenaName = ArenaConfig.getArenaForTrophies(w.profile.trophies).name;
        w.isPaused = true;
    }

    /** Advance or dismiss the match result overlay on tap/click. */
    private void dismissMatchResult() {
        if (w.matchResultPhase == 1) {
            // Skip trophy animation — jump to chest
            w.matchResultTrophyDisplay = w.matchResultTrophyTarget;
            w.matchResultPhase = w.matchResultChestType.isEmpty() ?
                (w.matchResultNewArena ? 3 : 4) : 2;
            w.matchResultTimer = 0f;
        } else if (w.matchResultPhase == 2) {
            // Skip chest — go to arena or done
            w.matchResultPhase = w.matchResultNewArena ? 3 : 4;
            w.matchResultTimer = 0f;
        } else if (w.matchResultPhase == 3) {
            // Skip arena unlock
            w.matchResultPhase = 4;
            w.matchResultTimer = 0f;
        } else if (w.matchResultPhase == 4) {
            w.matchResultPhase = 0;
            returnToMainMenu();
        }
    }

    /** Keybind configuration via w.dialog. */
    private void configureKeybinds() {
        w.dialog.showOptions("Settings", "Configure keybinds for which player?",
            new String[]{"Player 1", "Player 2", "Back"},
            new DialogSystem.OptionCallback() {
                public void onSelect(int index) {
                    if (index == 0) {
                        configurePlayerKeybinds(1);
                    } else if (index == 1) {
                        configurePlayerKeybinds(2);
                    }
                    // index == 2 or -1 = back, do nothing
                }
            });
    }

    private void configurePlayerKeybinds(final int player) {
        String pName = (player == 1) ? "Player 1" : "Player 2";
        int currentUp = (player == 1) ? w.player1UpKey : w.player2UpKey;
        int currentDown = (player == 1) ? w.player1DownKey : w.player2DownKey;
        int currentAbility = (player == 1) ? w.player1AbilityKey : w.player2AbilityKey;
        String info = pName + " Keybinds:\n" +
            "Up: " + Input.Keys.toString(currentUp) + "\n" +
            "Down: " + Input.Keys.toString(currentDown) + "\n" +
            "Ability: " + Input.Keys.toString(currentAbility);
        w.dialog.showOptions(pName + " Keybinds", info,
            new String[]{"Change Up Key", "Change Down Key", "Change Ability Key", "Done"},
            new DialogSystem.OptionCallback() {
                public void onSelect(int index) {
                    if (index == 0) {
                        captureKeybind(player, "up");
                    } else if (index == 1) {
                        captureKeybind(player, "down");
                    } else if (index == 2) {
                        captureKeybind(player, "ability");
                    }
                    // index == 3 or -1 = done
                }
            });
    }

    private void captureKeybind(final int player, final String which) {
        String pName = (player == 1) ? "Player 1" : "Player 2";
        w.dialog.captureKey("Set " + which.toUpperCase() + " Key",
            "Press a key for " + pName + "'s " + which + " control...",
            new DialogSystem.KeyCaptureCallback() {
                public void onKey(int keycode) {
                    if (player == 1) {
                        if (which.equals("up")) w.player1UpKey = keycode;
                        else if (which.equals("down")) w.player1DownKey = keycode;
                        else if (which.equals("ability")) w.player1AbilityKey = keycode;
                    } else {
                        if (which.equals("up")) w.player2UpKey = keycode;
                        else if (which.equals("down")) w.player2DownKey = keycode;
                        else if (which.equals("ability")) w.player2AbilityKey = keycode;
                    }
                    // Show updated keybinds
                    configurePlayerKeybinds(player);
                }
            });
    }

    // Rendering methods moved to GameRenderer.java

    // ==================== GAME UPDATE ====================

    /**
     * Main game update: physics, input, AI, scoring.
     * Called from render() when not paused.
     * Delta time uses the same scale as the original Swing game:
     *   w.dt = 1.0 means one normal 10ms tick.
     */
    private void update(float delta) {
        // Convert LibGDX delta (seconds) to original 10ms tick scale
        double rawDt = (delta * 1000.0) / 10.0;
        if (rawDt < 0.1) rawDt = 0.1;
        if (rawDt > 3.0) rawDt = 3.0;
        w.dt = rawDt;
        w.dti = (int) Math.ceil(w.dt);

        // Increment global timers
        w.noScoreTimer += w.dti;
        w.ballNotHitTimer += w.dti;

        // Save game state history for time loop (every 10 ticks)
        if (w.ballNotHitTimer % 10 == 0) {
            w.gameStateHistory.add(0, new GameStateSnapshot(w.ballX, w.ballY, w.ballVelX, w.ballVelY, w.paddle1Y, w.paddle2Y, w.scorePlayer1, w.scorePlayer2));
            if (w.gameStateHistory.size() > 300) w.gameStateHistory.remove(w.gameStateHistory.size() - 1);
        }

        // === TIMER UPDATES ===
        updateTimers();

        // === GUN ABILITY ===
        updateGunAbility();

        // === PROJECTILE UPDATES ===
        updateProjectiles();

        // === PADDLE MOVEMENT ===
        int paddleHeight1 = w.getPaddleHeight(1, w.shrinkPaddlesActive);
        int paddleHeight2 = w.getPaddleHeight(2, w.shrinkPaddlesActive);
        int paddleMaxY1 = GameWorld.VIRTUAL_HEIGHT - paddleHeight1;
        int paddleMaxY2 = GameWorld.VIRTUAL_HEIGHT - paddleHeight2;
        updatePaddleMovement(paddleMaxY1, paddleMaxY2, paddleHeight1, paddleHeight2);

        // === AI / PLAYER 2 / NETWORK OPPONENT MOVEMENT ===
        if (w.isOnlineMatch) {
            // Drive the opponent's paddle from the last received network position
            int clamped = (int) Math.max(15, Math.min(paddleMaxY2 - 15, w.networkOpponentPaddleY));
            if (w.isHost) w.paddle2Y = clamped;
            else          w.paddle1Y = clamped;
            // Send our own paddle position to the peer
            float myY = w.isHost ? w.paddle1Y : w.paddle2Y;
            w.network.send("P:" + myY);
        } else if (w.singlePlayer) {
            updateAI(paddleMaxY2);
        } else {
            updatePlayer2Movement(paddleMaxY2);
        }

        // === SHADOW CLONE UPDATES ===
        updateShadowClones(paddleHeight1, paddleHeight2);

        // === PORTAL/SIPHON/TIME LOOP TIMER UPDATES ===
        updateAbilityTimers();

        // === BALL MOVEMENT ===
        updateBallMovement();

        // === MAP MODIFIER EFFECTS ===
        updateMapModifiers();

        // === WALL BOUNCING ===
        updateWallBounces();

        // === PORTAL TELEPORTATION ===
        updatePortalTeleportation(paddleHeight1, paddleHeight2);

        // === PADDLE COLLISIONS (with all ability modifiers) ===
        updatePaddleCollisions(paddleHeight1, paddleHeight2);

        // === PASSIVE ABILITIES (reverse, ghost, blind, shrink, etc.) ===
        updatePassiveAbilities();

        // === SCORING ===
        updateScoring();

        // === MATCH TIMER INCREMENT ===
        if (w.matchTimerActive && !w.isPaused && !w.showingMainMenu) {
            w.matchTimer += w.dti;
        }

        // === MATCH END CHECK (non-story: timer-based with mercy rule) ===
        if (!w.storyModeActive) {
            checkMatchEnd();
        }

        // === POWER-UP SPAWNING & COLLECTION ===
        updatePowerUps();
    }

    // ==================== UPDATE SUB-METHODS ====================

    private void updateTimers() {
        // Stun timers
        if (w.player1StunTimer > 0) w.player1StunTimer = Math.max(0, w.player1StunTimer - w.dti);
        if (w.player2StunTimer > 0) w.player2StunTimer = Math.max(0, w.player2StunTimer - w.dti);
        if (w.player1StunImmunityTimer > 0) w.player1StunImmunityTimer = Math.max(0, w.player1StunImmunityTimer - w.dti);
        if (w.player2StunImmunityTimer > 0) w.player2StunImmunityTimer = Math.max(0, w.player2StunImmunityTimer - w.dti);

        // Dash timers
        if (w.player1DashActive > 0) w.player1DashActive = Math.max(0, w.player1DashActive - w.dti);
        if (w.player2DashActive > 0) w.player2DashActive = Math.max(0, w.player2DashActive - w.dti);
        if (w.player1DashTimer < 1000) w.player1DashTimer += w.dti;
        if (w.player2DashTimer < 1000) w.player2DashTimer += w.dti;

        // Hammer durations
        if (w.player1HammerDuration > 0) { w.player1HammerDuration -= w.dti; if (w.player1HammerDuration <= 0) w.player1HammerActive = false; }
        if (w.player2HammerDuration > 0) { w.player2HammerDuration -= w.dti; if (w.player2HammerDuration <= 0) w.player2HammerActive = false; }

        // Trap durations
        if (w.player1TrapDuration > 0) { w.player1TrapDuration -= w.dti; if (w.player1TrapDuration <= 0) w.player1TrapActive = false; }
        if (w.player2TrapDuration > 0) { w.player2TrapDuration -= w.dti; if (w.player2TrapDuration <= 0) w.player2TrapActive = false; }

        // Bankai aura durations
        if (w.player1BankaiAuraDuration > 0) { w.player1BankaiAuraDuration -= w.dti; if (w.player1BankaiAuraDuration <= 0) { w.player1BankaiActive = false; w.player1BankaiArmed = false; } }
        if (w.player2BankaiAuraDuration > 0) { w.player2BankaiAuraDuration -= w.dti; if (w.player2BankaiAuraDuration <= 0) { w.player2BankaiActive = false; w.player2BankaiArmed = false; } }

        // Sword swing timers
        if (w.player1SwordSwingTimer > 0) w.player1SwordSwingTimer -= w.dti;
        if (w.player2SwordSwingTimer > 0) w.player2SwordSwingTimer -= w.dti;
        if (w.player1SwordSwingCooldown > 0) w.player1SwordSwingCooldown -= w.dti;
        if (w.player2SwordSwingCooldown > 0) w.player2SwordSwingCooldown -= w.dti;

        // Portal durations
        if (w.player1PortalDuration > 0) { w.player1PortalDuration -= w.dti; if (w.player1PortalDuration <= 0) { w.player1PortalPlacementStage = 0; w.player1PortalEntranceX = null; w.player1PortalExitX = null; } }
        if (w.player2PortalDuration > 0) { w.player2PortalDuration -= w.dti; if (w.player2PortalDuration <= 0) { w.player2PortalPlacementStage = 0; w.player2PortalEntranceX = null; w.player2PortalExitX = null; } }
        if (w.portalBoostTimer > 0) w.portalBoostTimer -= w.dti;
        if (w.portalTrailTimer > 0) w.portalTrailTimer -= w.dti;

        // Shadow collision cooldowns
        if (w.player1ShadowCollisionCooldown > 0) w.player1ShadowCollisionCooldown -= w.dti;
        if (w.player2ShadowCollisionCooldown > 0) w.player2ShadowCollisionCooldown -= w.dti;

        // Haki durations
        if (w.player1HakiDuration > 0) { w.player1HakiDuration -= w.dti; if (w.player1HakiDuration <= 0) { w.player1HakiPhaseActive = false; w.player1HakiSpeedBoost = 0; } }
        if (w.player2HakiDuration > 0) { w.player2HakiDuration -= w.dti; if (w.player2HakiDuration <= 0) { w.player2HakiPhaseActive = false; w.player2HakiSpeedBoost = 0; } }

        // Increment ability cooldown timers
        int cd1 = (int)(w.dti * w.getCooldownTick(1));
        int cd2 = (int)(w.dti * w.getCooldownTick(2));
        w.player1GunTimer += cd1; w.player2GunTimer += cd2;
        w.player1HammerTimer += cd1; w.player2HammerTimer += cd2;
        w.player1PortalTimer += cd1; w.player2PortalTimer += cd2;
        w.player1TimeLoopTimer += cd1; w.player2TimeLoopTimer += cd2;
        w.player1HakiTimer += cd1; w.player2HakiTimer += cd2;
        w.player1BarrierTimer += cd1; w.player2BarrierTimer += cd2;
        w.player1TrapTimer += cd1; w.player2TrapTimer += cd2;
        w.player1WarpTimer += cd1; w.player2WarpTimer += cd2;
        w.player1BankaiTimer += cd1; w.player2BankaiTimer += cd2;
        w.player1MagnetTimer += cd1; w.player2MagnetTimer += cd2;
    }

    private void updateGunAbility() {
        // Gun cooldown: 400 ticks at lv1, -40 per level, min 150
        // Player 1 gun
        {
            int gunLevel1 = w.getEffectiveAbilityLevel(1, "gun");
            int gunCD1 = Math.max(150, w.gunCooldown - (gunLevel1 - 1) * 40);
            if (gunLevel1 > 0 && w.player1GunTimer >= gunCD1) {
                int branch = w.player1AbilityBranches.containsKey("gun") ? w.player1AbilityBranches.get("gun") : 0;
                if (branch == 1 && gunLevel1 >= 3) {
                    // Goku laser
                    int paddleH = w.getPaddleHeight(1, w.shrinkPaddlesActive);
                    w.activeLasers.add(new Laser(25, w.paddle1Y + paddleH / 2, 590, w.paddle2Y + w.getPaddleHeight(2, w.shrinkPaddlesActive) / 2, 1, 30 + (gunLevel1 - 2) * 10));
                    w.player1GunTimer = 0;
                } else if (branch == 2 && gunLevel1 >= 3) {
                    // Vegeta explosive
                    int paddleH = w.getPaddleHeight(1, w.shrinkPaddlesActive);
                    w.vegetaBullets.add(new VegetaBullet(25, w.paddle1Y + paddleH / 2, 8 + gunLevel1, (int)((Math.random() - 0.5) * 4), 1, gunLevel1));
                    w.player1GunTimer = 0;
                } else {
                    // Base gun
                    int paddleH = w.getPaddleHeight(1, w.shrinkPaddlesActive);
                    w.bullets.add(new Bullet(25, w.paddle1Y + paddleH / 2, 7 + gunLevel1, 0, 1, gunLevel1));
                    w.player1GunTimer = 0;
                }
            }
        }
        // Player 2 gun
        {
            int gunLevel2 = w.getEffectiveAbilityLevel(2, "gun");
            int gunCD2 = Math.max(150, w.gunCooldown - (gunLevel2 - 1) * 40);
            if (gunLevel2 > 0 && w.player2GunTimer >= gunCD2) {
                int branch = w.player2AbilityBranches.containsKey("gun") ? w.player2AbilityBranches.get("gun") : 0;
                if (branch == 1 && gunLevel2 >= 3) {
                    int paddleH = w.getPaddleHeight(2, w.shrinkPaddlesActive);
                    w.activeLasers.add(new Laser(575, w.paddle2Y + paddleH / 2, 10, w.paddle1Y + w.getPaddleHeight(1, w.shrinkPaddlesActive) / 2, 2, 30 + (gunLevel2 - 2) * 10));
                    w.player2GunTimer = 0;
                } else if (branch == 2 && gunLevel2 >= 3) {
                    int paddleH = w.getPaddleHeight(2, w.shrinkPaddlesActive);
                    w.vegetaBullets.add(new VegetaBullet(575, w.paddle2Y + paddleH / 2, -(8 + gunLevel2), (int)((Math.random() - 0.5) * 4), 2, gunLevel2));
                    w.player2GunTimer = 0;
                } else {
                    int paddleH = w.getPaddleHeight(2, w.shrinkPaddlesActive);
                    w.bullets.add(new Bullet(575, w.paddle2Y + paddleH / 2, -(7 + gunLevel2), 0, 2, gunLevel2));
                    w.player2GunTimer = 0;
                }
            }
        }
    }

    private void updateProjectiles() {
        // Bullets
        for (int i = w.bullets.size() - 1; i >= 0; i--) {
            Bullet b = w.bullets.get(i);
            b.x += (int)(b.velocityX * w.dt);
            b.y += (int)(b.velocityY * w.dt);
            // Check collision with opponent paddle
            int targetPadY = (b.owner == 1) ? w.paddle2Y : w.paddle1Y;
            int targetPadH = (b.owner == 1) ? w.getPaddleHeight(2, w.shrinkPaddlesActive) : w.getPaddleHeight(1, w.shrinkPaddlesActive);
            int targetX = (b.owner == 1) ? 580 : 10;
            if (Math.abs(b.x - targetX) < 15 && b.y >= targetPadY && b.y <= targetPadY + targetPadH) {
                // Hit! Stun opponent — 150 ticks base + 20 per level (1.5s–3.5s)
                int stunDur = 150 + (b.gunLevel - 1) * 20;
                if (b.owner == 1 && w.player2StunImmunityTimer == 0) w.player2StunTimer = stunDur;
                else if (b.owner == 2 && w.player1StunImmunityTimer == 0) w.player1StunTimer = stunDur;
                w.bullets.remove(i);
            } else if (b.x < -10 || b.x > 610) {
                w.bullets.remove(i);
            }
        }

        // Vegeta w.bullets
        for (int i = w.vegetaBullets.size() - 1; i >= 0; i--) {
            VegetaBullet vb = w.vegetaBullets.get(i);
            vb.x += (int)(vb.velocityX * w.dt);
            vb.y += (int)(vb.velocityY * w.dt);
            int targetPadY = (vb.owner == 1) ? w.paddle2Y : w.paddle1Y;
            int targetPadH = (vb.owner == 1) ? w.getPaddleHeight(2, w.shrinkPaddlesActive) : w.getPaddleHeight(1, w.shrinkPaddlesActive);
            int targetX = (vb.owner == 1) ? 580 : 10;
            if (Math.abs(vb.x - targetX) < 20 && vb.y >= targetPadY - 10 && vb.y <= targetPadY + targetPadH + 10) {
                // Explosion!
                int radius = 30 + vb.gunLevel * 5;
                w.explosions.add(new Explosion(vb.x, vb.y, radius, 30));
                if (vb.owner == 1 && w.player2StunImmunityTimer == 0) w.player2StunTimer = 80 + vb.gunLevel * 10;
                else if (vb.owner == 2 && w.player1StunImmunityTimer == 0) w.player1StunTimer = 80 + vb.gunLevel * 10;
                w.vegetaBullets.remove(i);
            } else if (vb.x < -10 || vb.x > 610) {
                w.vegetaBullets.remove(i);
            }
        }

        // Explosions
        for (int i = w.explosions.size() - 1; i >= 0; i--) {
            Explosion exp = w.explosions.get(i);
            exp.duration -= (float)w.dt;
            if (exp.duration <= 0) w.explosions.remove(i);
        }

        // Lasers
        for (int i = w.activeLasers.size() - 1; i >= 0; i--) {
            Laser l = w.activeLasers.get(i);
            l.remainingTime -= w.dti;
            if (l.remainingTime <= 0) {
                // Stun target
                if (l.owner == 1 && w.player2StunImmunityTimer == 0) w.player2StunTimer = 80;
                else if (l.owner == 2 && w.player1StunImmunityTimer == 0) w.player1StunTimer = 80;
                w.activeLasers.remove(i);
            }
        }

        // Frieza lasers
        for (int i = w.freezaLasers.size() - 1; i >= 0; i--) {
            FreezaLaser fl = w.freezaLasers.get(i);
            fl.remainingTime -= w.dti;
            if (fl.remainingTime <= 0) {
                w.freezaLasers.remove(i);
            }
        }

        // Jiren w.bullets
        for (int i = w.jirenBullets.size() - 1; i >= 0; i--) {
            JirenBullet jb = w.jirenBullets.get(i);
            jb.x += (int)(jb.velocityX * w.dt);
            int targetPadY = (jb.owner == 1) ? w.paddle2Y : w.paddle1Y;
            int targetPadH = (jb.owner == 1) ? w.getPaddleHeight(2, w.shrinkPaddlesActive) : w.getPaddleHeight(1, w.shrinkPaddlesActive);
            int targetX = (jb.owner == 1) ? 580 : 10;
            if (Math.abs(jb.x - targetX) < 25 && jb.y >= targetPadY - 15 && jb.y <= targetPadY + targetPadH + 15) {
                int radius = 40 + jb.level * 8;
                w.explosions.add(new Explosion(jb.x, jb.y, radius, 30, jb.owner, true, jb.level));
                if (jb.owner == 1 && w.player2StunImmunityTimer == 0) w.player2StunTimer = 100 + jb.level * 15;
                else if (jb.owner == 2 && w.player1StunImmunityTimer == 0) w.player1StunTimer = 100 + jb.level * 15;
                w.jirenBullets.remove(i);
            } else if (jb.x < -10 || jb.x > 610) {
                w.jirenBullets.remove(i);
            }
        }

    }

    private void updatePaddleMovement(int paddleMaxY1, int paddleMaxY2, int paddleHeight1, int paddleHeight2) {
        // Player 1 movement (with debuff checks)
        if (w.player1StunTimer > 0) return; // Stunned - no movement handled here

        int moveSpeed1 = w.getPlayerSpeed(1);
        // slow_opponent: player2's slow_opponent level reduces player1's speed by 15% per level, cap 50%
        int slowOppLevel1 = w.getEffectiveAbilityLevel(2, "slow_opponent");
        if (slowOppLevel1 > 0) {
            int cappedSlow1 = Math.min(slowOppLevel1, 3); // 3 levels * 15% = 45%, close to 50% cap
            double slowFactor1 = 1.0 - (cappedSlow1 * 0.15);
            if (slowFactor1 < 0.5) slowFactor1 = 0.5;
            moveSpeed1 = (int)(moveSpeed1 * slowFactor1);
            if (moveSpeed1 < 1) moveSpeed1 = 1;
        }
        boolean reversed1 = w.player1ReverseEffectTimer > 0;
        boolean actualUp1 = reversed1 ? w.down1 : w.up1;
        boolean actualDown1 = reversed1 ? w.up1 : w.down1;

        if (actualUp1 && w.paddle1Y > 0) {
            double move = moveSpeed1 * w.dt * w.player1ObsSlowFactor + w.pad1R;
            w.paddle1Y -= (int) move;
            w.pad1R = move - (int) move;
            if (w.paddle1Y < 0) w.paddle1Y = 0;
        } else if (actualDown1 && w.paddle1Y < paddleMaxY1) {
            double move = moveSpeed1 * w.dt * w.player1ObsSlowFactor + w.pad1R;
            w.paddle1Y += (int) move;
            w.pad1R = move - (int) move;
            if (w.paddle1Y > paddleMaxY1) w.paddle1Y = paddleMaxY1;
        } else {
            w.pad1R = 0;
        }
    }

    private void updatePlayer2Movement(int paddleMaxY2) {
        if (w.player2StunTimer > 0) return;

        int moveSpeed2 = w.getPlayerSpeed(2);
        // slow_opponent: player1's slow_opponent level reduces player2's speed by 15% per level, cap 50%
        int slowOppLevel2 = w.getEffectiveAbilityLevel(1, "slow_opponent");
        if (slowOppLevel2 > 0) {
            int cappedSlow2 = Math.min(slowOppLevel2, 3);
            double slowFactor2 = 1.0 - (cappedSlow2 * 0.15);
            if (slowFactor2 < 0.5) slowFactor2 = 0.5;
            moveSpeed2 = (int)(moveSpeed2 * slowFactor2);
            if (moveSpeed2 < 1) moveSpeed2 = 1;
        }
        boolean reversed2 = w.player2ReverseEffectTimer > 0;
        boolean actualUp2 = reversed2 ? w.down2 : w.up2;
        boolean actualDown2 = reversed2 ? w.up2 : w.down2;

        if (actualUp2 && w.paddle2Y > 0) {
            double move = moveSpeed2 * w.dt * w.player2ObsSlowFactor + w.pad2R;
            w.paddle2Y -= (int) move;
            w.pad2R = move - (int) move;
            if (w.paddle2Y < 0) w.paddle2Y = 0;
        } else if (actualDown2 && w.paddle2Y < paddleMaxY2) {
            double move = moveSpeed2 * w.dt * w.player2ObsSlowFactor + w.pad2R;
            w.paddle2Y += (int) move;
            w.pad2R = move - (int) move;
            if (w.paddle2Y > paddleMaxY2) w.paddle2Y = paddleMaxY2;
        } else {
            w.pad2R = 0;
        }
    }

    private void updateShadowClones(int paddleHeight1, int paddleHeight2) {
        // Shadow clone = independent small paddle that tracks the ball automatically.
        // Speed and size both scale with ability level.
        int lv1 = w.getEffectiveAbilityLevel(1, "shadow_clone");
        if (lv1 > 0) {
            int shadowH = w.getShadowSize(1);
            int speed = 1 + lv1; // lv1=2, lv2=3, lv3=4, lv4=5
            // Target: center shadow on ball
            int target = w.ballY + 7 - shadowH / 2;
            target = Math.max(0, Math.min(400 - shadowH, target));
            int diff = target - w.player1IndependentShadowY;
            if (diff > 0) w.player1IndependentShadowY += Math.min(speed, diff);
            else if (diff < 0) w.player1IndependentShadowY -= Math.min(speed, -diff);
        }
        int lv2 = w.getEffectiveAbilityLevel(2, "shadow_clone");
        if (lv2 > 0) {
            int shadowH = w.getShadowSize(2);
            int speed = 1 + lv2;
            int target = w.ballY + 7 - shadowH / 2;
            target = Math.max(0, Math.min(400 - shadowH, target));
            int diff = target - w.player2IndependentShadowY;
            if (diff > 0) w.player2IndependentShadowY += Math.min(speed, diff);
            else if (diff < 0) w.player2IndependentShadowY -= Math.min(speed, -diff);
        }
    }

    private void updateAbilityTimers() {
        // Time loop slow mo
        if (w.player1TimeLoopSlowMoTimer > 0) w.player1TimeLoopSlowMoTimer -= w.dti;
        if (w.player2TimeLoopSlowMoTimer > 0) w.player2TimeLoopSlowMoTimer -= w.dti;

        // Temporal echo
        if (w.temporalEchoActive) {
            w.echoGhostBallX += (int)(w.echoGhostBallVelX * w.dt);
            w.echoGhostBallY += (int)(w.echoGhostBallVelY * w.dt);
            if (w.echoGhostBallY <= 0 || w.echoGhostBallY >= 385) w.echoGhostBallVelY *= -1;
            // Echo scoring
            if (w.echoGhostBallX < 0) { w.scorePlayer2++; onPointScored(2); w.temporalEchoActive = false; }
            else if (w.echoGhostBallX > 600) { w.scorePlayer1++; onPointScored(1); w.temporalEchoActive = false; }
            w.temporalEchoDuration -= w.dti;
            if (w.temporalEchoDuration <= 0) w.temporalEchoActive = false;
        }


        // Magnet ball — always-on once drawn. Force = level * 0.3 (as velocity nudge per interval).
        // Cap at level 5. Active only when ball is moving toward your side.
        {
            int lvl1 = w.getEffectiveAbilityLevel(1, "magnet_ball");
            if (lvl1 > 0 && w.ballX < 300 && w.ballVelX < 0) {
                w.player1MagnetActive = true;
                int cappedLvl1 = Math.min(lvl1, 5);
                // Interval shrinks with level: lv1=10, lv2=7, lv3=5, lv4=4, lv5=3
                int interval1 = Math.max(3, 13 - cappedLvl1 * 2);
                if (w.player1MagnetTimer % interval1 == 0) {
                    int paddleH = w.getPaddleHeight(1, w.shrinkPaddlesActive);
                    int diff = (w.paddle1Y + paddleH / 2) - (w.ballY + 7);
                    int maxVelY = (int)(w.ballSpeed) + 2;
                    if (Math.abs(diff) > paddleH / 3) {
                        if (diff > 0) w.ballVelY = Math.min(w.ballVelY + 1, maxVelY);
                        else w.ballVelY = Math.max(w.ballVelY - 1, -maxVelY);
                    }
                }
            } else {
                w.player1MagnetActive = false;
            }
        }
        {
            int lvl2 = w.getEffectiveAbilityLevel(2, "magnet_ball");
            if (lvl2 > 0 && w.ballX > 300 && w.ballVelX > 0) {
                w.player2MagnetActive = true;
                int cappedLvl2 = Math.min(lvl2, 5);
                int interval2 = Math.max(3, 13 - cappedLvl2 * 2);
                if (w.player2MagnetTimer % interval2 == 0) {
                    int paddleH = w.getPaddleHeight(2, w.shrinkPaddlesActive);
                    int diff = (w.paddle2Y + paddleH / 2) - (w.ballY + 7);
                    int maxVelY = (int)(w.ballSpeed) + 2;
                    if (Math.abs(diff) > paddleH / 3) {
                        if (diff > 0) w.ballVelY = Math.min(w.ballVelY + 1, maxVelY);
                        else w.ballVelY = Math.max(w.ballVelY - 1, -maxVelY);
                    }
                }
            } else {
                w.player2MagnetActive = false;
            }
        }

        // Gravity well effect on ball
        if (w.player1GravityWellActive) {
            double dx = w.player1GravityWellX - w.ballX;
            double dy = w.player1GravityWellY - w.ballY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 150 && dist > 5) {
                w.ballVelX += (int)(dx / dist * 2);
                w.ballVelY += (int)(dy / dist * 2);
            }
        }
        if (w.player2GravityWellActive) {
            double dx = w.player2GravityWellX - w.ballX;
            double dy = w.player2GravityWellY - w.ballY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 150 && dist > 5) {
                w.ballVelX += (int)(dx / dist * 2);
                w.ballVelY += (int)(dy / dist * 2);
            }
        }
    }

    private void updateBallMovement() {
        // Time loop slow-mo modifiers
        double slowMoFactor = 1.0;
        if (w.player1TimeLoopSlowMoTimer > 0 || w.player2TimeLoopSlowMoTimer > 0) slowMoFactor = 0.3;

        int adjustedVelX = (int)(w.ballVelX * w.ballSpeedMultiplier * slowMoFactor);
        int adjustedVelY = (int)(w.ballVelY * w.ballSpeedMultiplier * slowMoFactor);

        // Flash ability speed boost
        if (w.player1FlashActive > 0 && w.ballVelX > 0) { adjustedVelX = (int)(adjustedVelX * 1.5); }
        if (w.player2FlashActive > 0 && w.ballVelX < 0) { adjustedVelX = (int)(adjustedVelX * 1.5); }

        if (!w.ballFrozenByTrap) {
            double moveX = adjustedVelX * w.dt + w.ballXR;
            double moveY = adjustedVelY * w.dt + w.ballYR;
            w.ballX += (int) moveX;
            w.ballY += (int) moveY;
            w.ballXR = moveX - (int) moveX;
            w.ballYR = moveY - (int) moveY;

            // Gravity hammer passive: applies only on the opponent's side of the court.
            // P1's hammer pulls ball down on the right half; P2's on the left half.
            int gravHammerLv1 = w.getEffectiveAbilityLevel(1, "gravity_hammer");
            int gravHammerLv2 = w.getEffectiveAbilityLevel(2, "gravity_hammer");
            int activeGravLevel = (w.ballX >= 300) ? gravHammerLv1 : gravHammerLv2;
            if (activeGravLevel > 0) {
                int cappedGravLevel = Math.min(activeGravLevel, 5);
                double gravAccel = cappedGravLevel * 0.25;
                w.ballYR += gravAccel * w.dt;
                while (w.ballYR >= 1.0) {
                    w.ballY += 1;
                    w.ballYR -= 1.0;
                }
            }
        }

        // Second ball (sudden death overtime)
        if (w.ball2Active) {
            w.ball2XR += w.ball2VelX * w.ball2Speed * w.dt;
            w.ball2YR += w.ball2VelY * w.ball2Speed * w.dt;
            int dx2 = (int)w.ball2XR; w.ball2XR -= dx2; w.ball2X += dx2;
            int dy2 = (int)w.ball2YR; w.ball2YR -= dy2; w.ball2Y += dy2;
            // Wall bounce
            if (w.ball2Y < 0) { w.ball2Y = 0; w.ball2VelY = Math.abs(w.ball2VelY); }
            if (w.ball2Y > 390) { w.ball2Y = 390; w.ball2VelY = -Math.abs(w.ball2VelY); }
            // Paddle 1 hit
            int p1H = w.getPaddleHeight(1, w.shrinkPaddlesActive);
            if (w.ball2X < 32 && w.ball2X > 0 && w.ball2Y + 14 > w.paddle1Y && w.ball2Y < w.paddle1Y + p1H) {
                w.ball2VelX = Math.abs(w.ball2VelX);
                w.ball2Speed = Math.min(w.ball2Speed + 0.3, 4.0);
            }
            // Paddle 2 hit
            int p2H = w.getPaddleHeight(2, w.shrinkPaddlesActive);
            if (w.ball2X + 14 > 568 && w.ball2X < 600 && w.ball2Y + 14 > w.paddle2Y && w.ball2Y < w.paddle2Y + p2H) {
                w.ball2VelX = -Math.abs(w.ball2VelX);
                w.ball2Speed = Math.min(w.ball2Speed + 0.3, 4.0);
            }
            // Ball2 scores
            if (w.ball2X < 0) {
                w.scorePlayer2++;
                onPointScored(2);
                w.ball2Active = false;
                handleMatchEnd(w.scorePlayer1 > w.scorePlayer2);
            } else if (w.ball2X > 600) {
                w.scorePlayer1++;
                onPointScored(1);
                w.ball2Active = false;
                handleMatchEnd(w.scorePlayer1 > w.scorePlayer2);
            }
        }
    }

    private void updateMapModifiers() {
        // Teleport effect timer
        if (w.teleportActive) { w.teleportDuration += w.dti; if (w.teleportDuration >= w.maxTeleportDuration) { w.teleportActive = false; w.teleportDuration = 0; } }
        // Fireball timer
        if (w.fireballActive) {
            w.fireballDuration += w.dti;
            if (w.fireballDuration >= w.maxFireballDuration) {
                w.fireballActive = false; w.fireballDuration = 0;
                w.ballSpeed = w.initialBallSpeed;
                double magnitude = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                if (magnitude > 0) { w.ballVelX = (int)((w.ballVelX / magnitude) * w.ballSpeed); w.ballVelY = (int)((w.ballVelY / magnitude) * w.ballSpeed); }
            }
        }
        // Zigzag timer
        if (w.zigzagActive) {
            w.zigzagDuration += w.dti; w.zigzagTimer += w.dti;
            if (w.zigzagDuration >= w.maxZigzagDuration) { w.zigzagActive = false; w.zigzagDuration = 0; w.zigzagTimer = 0; }
            if (w.zigzagTimer >= w.zigzagInterval) { w.ballVelY = (Math.random() < 0.5 ? -1 : 1) * (2 + (int)(Math.random() * 4)); w.zigzagTimer = 0; }
        }
        // Invisible walls
        if (w.invisibleWallsActive) {
            w.invisibleWallsDuration += w.dti;
            if (w.invisibleWallsDuration >= 800) { w.invisibleWallsActive = false; w.invisibleWallsDuration = 0; }
            int ballCenterY = w.ballY + 7;
            if (Math.abs(ballCenterY - w.invisibleWallY) < 10) { w.ballVelY *= -1; w.ballY = w.invisibleWallY + (w.ballVelY > 0 ? 5 : -20); }
        }
        // Shrink paddles
        if (w.shrinkPaddlesActive) { w.shrinkPaddlesDuration += w.dti; if (w.shrinkPaddlesDuration >= 800) { w.shrinkPaddlesActive = false; w.shrinkPaddlesDuration = 0; } }
        // Center wall
        if (w.centerWallActive) {
            w.centerWallDuration += w.dti;
            if (w.centerWallDuration >= 800) { w.centerWallActive = false; w.centerWallDuration = 0; }
            int ballCenterX = w.ballX + 7;
            int ballCenterY = w.ballY + 7;
            if (ballCenterX >= 295 && ballCenterX <= 305) {
                if (ballCenterY < w.centerWallGapY - w.centerWallGapSize / 2 || ballCenterY > w.centerWallGapY + w.centerWallGapSize / 2) {
                    w.ballVelX *= -1;
                    if (w.ballVelX > 0) w.ballX = 306; else w.ballX = 284;
                }
            }
        }
        // Danger zone — deflects ball at random angles while inside
        if (w.dangerZoneActive) {
            w.dangerZoneDuration += w.dti;
            if (w.dangerZoneDuration >= 800) { w.dangerZoneActive = false; w.dangerZoneDuration = 0; w.dangerZoneTime = 0; }
            int dzDx = w.ballX + 7 - w.dangerZoneCenterX;
            int dzDy = w.ballY + 7 - w.dangerZoneCenterY;
            double dzDist = Math.sqrt(dzDx * dzDx + dzDy * dzDy);
            if (dzDist < w.dangerZoneRadius) {
                w.dangerZoneTime += w.dti;
                // Every 30 ticks inside the zone, kick the ball at a weird angle
                if (w.dangerZoneTime % 30 < w.dti * 2) {
                    double speed = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                    if (speed < 3) speed = 3;
                    double angle = Math.atan2(w.ballVelY, w.ballVelX);
                    angle += (Math.random() - 0.5) * Math.PI * 1.2; // up to ±108° random kick
                    w.ballVelX = (int)(speed * Math.cos(angle));
                    w.ballVelY = (int)(speed * Math.sin(angle));
                    if (w.ballVelX == 0) w.ballVelX = (Math.random() < 0.5 ? 1 : -1);
                }
            } else {
                w.dangerZoneTime = Math.max(0, w.dangerZoneTime - w.dti);
            }
        }
        // Frozen ball (sticky trap)
        if (w.ballFrozenByTrap) {
            w.ballFreezeTimer -= w.dti;
            w.ballX = w.frozenBallX; w.ballY = w.frozenBallY;
            if (w.ballFreezeTimer <= 0) {
                w.ballFrozenByTrap = false;
                double angle = Math.random() * Math.PI * 2;
                w.ballVelX = (int)(Math.cos(angle) * 7); w.ballVelY = (int)(Math.sin(angle) * 7);
            }
        }
        // Sticky slow
        if (w.player1StickyTimer > 0 || w.player2StickyTimer > 0) {
            w.ballVelX = (int)(w.ballVelX * 0.3); w.ballVelY = (int)(w.ballVelY * 0.3);
            if (Math.abs(w.ballVelX) < 1) w.ballVelX = w.ballVelX >= 0 ? 1 : -1;
        }
        if (w.player1StickyTimer > 0) w.player1StickyTimer -= w.dti;
        if (w.player2StickyTimer > 0) w.player2StickyTimer -= w.dti;
    }

    private void updateWallBounces() {
        if (w.ballY <= 0 || w.ballY >= 385) {
            w.ballVelY *= -1;
            if (w.ballY <= 0) w.ballY = 1; else if (w.ballY >= 385) w.ballY = 384;
            // Shockwave from Thor's Hammer
            if (w.player1ShockwaveTimer > 0 && w.player2StunImmunityTimer == 0) { w.player2StunTimer = w.player1ShockwaveTimer; w.player1ShockwaveTimer = 0; }
            if (w.player2ShockwaveTimer > 0 && w.player1StunImmunityTimer == 0) { w.player1StunTimer = w.player2ShockwaveTimer; w.player2ShockwaveTimer = 0; }
        }
    }

    private void updatePortalTeleportation(int paddleHeight1, int paddleHeight2) {
        if (w.ballTeleportCooldown) {
            // Reset cooldown when ball moves away from portal
            if (w.player1PortalEntranceX != null && w.player1PortalExitX != null) {
                double d1 = Math.sqrt(Math.pow(w.ballX - w.player1PortalEntranceX, 2) + Math.pow(w.ballY - w.player1PortalEntranceY, 2));
                double d2 = Math.sqrt(Math.pow(w.ballX - w.player1PortalExitX, 2) + Math.pow(w.ballY - w.player1PortalExitY, 2));
                if (d1 > 40 && d2 > 40) w.ballTeleportCooldown = false;
            }
            if (w.player2PortalEntranceX != null && w.player2PortalExitX != null) {
                double d1 = Math.sqrt(Math.pow(w.ballX - w.player2PortalEntranceX, 2) + Math.pow(w.ballY - w.player2PortalEntranceY, 2));
                double d2 = Math.sqrt(Math.pow(w.ballX - w.player2PortalExitX, 2) + Math.pow(w.ballY - w.player2PortalExitY, 2));
                if (d1 > 40 && d2 > 40) w.ballTeleportCooldown = false;
            }
            if (w.player1PortalEntranceX == null && w.player2PortalEntranceX == null) w.ballTeleportCooldown = false;
            return;
        }
        // Player 1 portals
        if (w.player1PortalEntranceX != null && w.player1PortalExitX != null && w.player1PortalPlacementStage == 2 && w.player1PortalDuration > 0) {
            int portalRadius = 30;
            double d1 = Math.sqrt(Math.pow(w.ballX - w.player1PortalEntranceX, 2) + Math.pow(w.ballY - w.player1PortalEntranceY, 2));
            double d2 = Math.sqrt(Math.pow(w.ballX - w.player1PortalExitX, 2) + Math.pow(w.ballY - w.player1PortalExitY, 2));
            if (d1 < portalRadius) {
                w.ballX = w.player1PortalExitX; w.ballY = w.player1PortalExitY;
                int portalLevel = w.player1Abilities.containsKey("portal_pong") ? w.player1Abilities.get("portal_pong") : 1;
                int speedBoost = 2 + Math.min(portalLevel - 1, 2);
                w.ballVelX = Math.abs(w.ballVelX) + speedBoost;
                w.ballTeleportCooldown = true; w.portalBoostTimer = 30;
            } else if (d2 < portalRadius) {
                w.ballX = w.player1PortalEntranceX; w.ballY = w.player1PortalEntranceY;
                int portalLevel = w.player1Abilities.containsKey("portal_pong") ? w.player1Abilities.get("portal_pong") : 1;
                int speedBoost = 2 + Math.min(portalLevel - 1, 2);
                w.ballVelX = Math.abs(w.ballVelX) + speedBoost;
                w.ballTeleportCooldown = true; w.portalBoostTimer = 30;
            }
        }
        // Player 2 portals
        if (w.player2PortalEntranceX != null && w.player2PortalExitX != null && w.player2PortalPlacementStage == 2 && w.player2PortalDuration > 0) {
            int portalRadius = 30;
            double d1 = Math.sqrt(Math.pow(w.ballX - w.player2PortalEntranceX, 2) + Math.pow(w.ballY - w.player2PortalEntranceY, 2));
            double d2 = Math.sqrt(Math.pow(w.ballX - w.player2PortalExitX, 2) + Math.pow(w.ballY - w.player2PortalExitY, 2));
            if (d1 < portalRadius) {
                w.ballX = w.player2PortalExitX; w.ballY = w.player2PortalExitY;
                int portalLevel = w.player2Abilities.containsKey("portal_pong") ? w.player2Abilities.get("portal_pong") : 1;
                int speedBoost = 2 + Math.min(portalLevel - 1, 2);
                w.ballVelX = -(Math.abs(w.ballVelX) + speedBoost);
                w.ballTeleportCooldown = true; w.portalBoostTimer = 30;
            } else if (d2 < portalRadius) {
                w.ballX = w.player2PortalEntranceX; w.ballY = w.player2PortalEntranceY;
                int portalLevel = w.player2Abilities.containsKey("portal_pong") ? w.player2Abilities.get("portal_pong") : 1;
                int speedBoost = 2 + Math.min(portalLevel - 1, 2);
                w.ballVelX = -(Math.abs(w.ballVelX) + speedBoost);
                w.ballTeleportCooldown = true; w.portalBoostTimer = 30;
            }
        }
    }

    private void updatePaddleCollisions(int paddleHeight1, int paddleHeight2) {
        w.tmpBallRect.set(w.ballX, w.ballY, 15, 15);
        // Hitbox matches the shape core (32px wide), which the sprite body is aligned to.
        int p1W = 24;
        int p2X = 576;
        w.tmpPaddle1Rect.set(0,   w.paddle1Y, p1W,       paddleHeight1);
        w.tmpPaddle2Rect.set(p2X, w.paddle2Y, 600 - p2X, paddleHeight2);

        // Barrier collision (permanent passive — stays active, does not deactivate on hit)
        if (w.player1BarrierActive) {
            Rectangle br = new Rectangle(w.player1BarrierX, w.player1BarrierY, w.player1BarrierWidth, w.barrierHeight);
            if (w.tmpBallRect.overlaps(br)) { w.ballVelX = -w.ballVelX; }
        }
        if (w.player2BarrierActive) {
            Rectangle br = new Rectangle(w.player2BarrierX, w.player2BarrierY, w.player2BarrierWidth, w.barrierHeight);
            if (w.tmpBallRect.overlaps(br)) { w.ballVelX = -w.ballVelX; }
        }

        // Trap collision — stuns the opponent paddle, ball passes through unchanged
        if (w.player1TrapActive && !w.ballFrozenByTrap) {
            Rectangle tr = new Rectangle(w.player1TrapX - w.trapSize/2, w.player1TrapY - w.trapSize/2, w.trapSize, w.trapSize);
            if (w.tmpBallRect.overlaps(tr)) {
                int lv = w.getEffectiveAbilityLevel(1, "trap");
                int stunDur = 20 + (lv - 1) * 7; // lv1=20, lv2=27, lv3=34, lv4=41, lv5=48 (all <50 = 0.5s)
                if (w.player2StunImmunityTimer == 0) w.player2StunTimer = stunDur;
                w.player1TrapActive = false;
            }
        }
        if (w.player2TrapActive && !w.ballFrozenByTrap) {
            Rectangle tr = new Rectangle(w.player2TrapX - w.trapSize/2, w.player2TrapY - w.trapSize/2, w.trapSize, w.trapSize);
            if (w.tmpBallRect.overlaps(tr)) {
                int lv = w.getEffectiveAbilityLevel(2, "trap");
                int stunDur = 20 + (lv - 1) * 7;
                if (w.player1StunImmunityTimer == 0) w.player1StunTimer = stunDur;
                w.player2TrapActive = false;
            }
        }

        // Paddle 1 collision
        if (w.tmpBallRect.overlaps(w.tmpPaddle1Rect) && w.ballVelX < 0) {
            if (w.player2HakiPhaseActive) {
                w.player2HakiPhaseActive = false; // Ball phases through
            } else {
                w.ballSpeed += 0.3;
                w.ballNotHitTimer = 0;
                double magnitude = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                if (magnitude > 0) {
                    w.ballVelX = -(int)((w.ballVelX / magnitude) * w.ballSpeed);
                    w.ballVelY = (int)((w.ballVelY / magnitude) * w.ballSpeed);
                }
                if (w.ballVelY == 0) w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
                w.ballX = p1W + 1; // push ball flush to paddle face

                // Bankai burst (armed on activation, fires on next hit)
                if (w.player1BankaiArmed) {
                    w.ballVelX = (int)(w.ballVelX * 2.0);
                    w.ballVelY = (int)(w.ballVelY * 1.5);
                    w.player1BankaiArmed = false;
                    w.player1BankaiActive = false;
                }
                // Dash speed boost
                if (w.player1DashActive > 0) { w.ballVelX *= 2; w.ballVelY *= 2; }
                // Hammer slam
                if (w.player1HammerActive) {
                    w.ballVelY = Math.abs(w.ballVelY) + 8;
                    w.ballVelX = (int)(Math.abs(w.ballVelX) * 1.3);
                }
                // Lag spike trigger: cooldown 700 ticks at lv1, -70 per level, min 280
                if (w.getEffectiveAbilityLevel(1, "lag_spike") > 0 && w.player1LagTriggerTimer == 0) {
                    int lagLevel = w.player1Abilities.get("lag_spike");
                    int lagCD = Math.max(280, w.lagSpikeCooldown - (lagLevel - 1) * 70);
                    w.player1LagTriggerTimer = lagCD;
                }
                // Ensure minimum horizontal velocity
                if (Math.abs(w.ballVelX) < 2) w.ballVelX = w.ballVelX >= 0 ? 2 : -2;
            }
        }

        // Paddle 2 collision
        if (w.tmpBallRect.overlaps(w.tmpPaddle2Rect) && w.ballVelX > 0) {
            if (w.player1HakiPhaseActive) {
                w.player1HakiPhaseActive = false;
            } else {
                w.ballSpeed += 0.3;
                w.ballNotHitTimer = 0;
                double magnitude = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                if (magnitude > 0) {
                    w.ballVelX = -(int)((w.ballVelX / magnitude) * w.ballSpeed);
                    w.ballVelY = (int)((w.ballVelY / magnitude) * w.ballSpeed);
                }
                if (w.ballVelY == 0) w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
                w.ballX = p2X - 16; // push ball flush to paddle face

                // Bankai burst (armed on activation, fires on next hit)
                if (w.player2BankaiArmed) {
                    w.ballVelX = (int)(w.ballVelX * 2.0);
                    w.ballVelY = (int)(w.ballVelY * 1.5);
                    w.player2BankaiArmed = false;
                    w.player2BankaiActive = false;
                }
                if (w.player2DashActive > 0) { w.ballVelX *= 2; w.ballVelY *= 2; }
                if (w.player2HammerActive) {
                    w.ballVelY = Math.abs(w.ballVelY) + 8;
                    w.ballVelX = -(int)(Math.abs(w.ballVelX) * 1.3);
                }
                if (w.getEffectiveAbilityLevel(2, "lag_spike") > 0 && w.player2LagTriggerTimer == 0) {
                    int lagLevel = w.player2Abilities.get("lag_spike");
                    int lagCD2 = Math.max(280, w.lagSpikeCooldown - (lagLevel - 1) * 70);
                    w.player2LagTriggerTimer = lagCD2;
                }
                if (Math.abs(w.ballVelX) < 2) w.ballVelX = w.ballVelX >= 0 ? 2 : -2;
            }
        }

        // Lag spike trigger timers: duration always 150 ticks
        if (w.player1LagTriggerTimer > 0) { w.player1LagTriggerTimer -= w.dti; if (w.player1LagTriggerTimer <= 0) { w.player2LagEffectTimer = 150; w.player1LagTriggerTimer = 0; } }
        if (w.player2LagTriggerTimer > 0) { w.player2LagTriggerTimer -= w.dti; if (w.player2LagTriggerTimer <= 0) { w.player1LagEffectTimer = 150; w.player2LagTriggerTimer = 0; } }
        if (w.player1LagEffectTimer > 0) w.player1LagEffectTimer -= w.dti;
        if (w.player2LagEffectTimer > 0) w.player2LagEffectTimer -= w.dti;

        // Shadow clone collision — independent small paddle that catches the ball
        if (w.getEffectiveAbilityLevel(1, "shadow_clone") > 0 && w.player1ShadowCollisionCooldown <= 0
                && w.ballVelX < 0 && w.ballX < 30) {
            int shadowH = w.getShadowSize(1);
            int sy = w.player1IndependentShadowY;
            if (w.ballX + 15 > 8 && w.ballX < 22 && w.ballY + 15 > sy && w.ballY < sy + shadowH) {
                w.ballSpeed += 0.1;
                double mag1 = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                if (mag1 > 0) w.ballVelX = (int)((Math.abs(w.ballVelX) / mag1) * w.ballSpeed);
                else w.ballVelX = (int)w.ballSpeed;
                w.ballX = 22;
                if (w.ballVelY == 0) w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
                w.player1ShadowCollisionCooldown = w.shadowCollisionCooldownFrames;
            }
        }
        if (w.getEffectiveAbilityLevel(2, "shadow_clone") > 0 && w.player2ShadowCollisionCooldown <= 0
                && w.ballVelX > 0 && w.ballX > 570) {
            int shadowH = w.getShadowSize(2);
            int sy = w.player2IndependentShadowY;
            if (w.ballX + 15 > 578 && w.ballX < 592 && w.ballY + 15 > sy && w.ballY < sy + shadowH) {
                w.ballSpeed += 0.1;
                double mag2 = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
                if (mag2 > 0) w.ballVelX = -(int)((Math.abs(w.ballVelX) / mag2) * w.ballSpeed);
                else w.ballVelX = -(int)w.ballSpeed;
                w.ballX = 575;
                if (w.ballVelY == 0) w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
                w.player2ShadowCollisionCooldown = w.shadowCollisionCooldownFrames;
            }
        }

    }

    private void updatePassiveAbilities() {
        // Reverse controls: auto-fires every 1000 ticks (10s) — no Q press needed
        w.player1ReverseTimer += w.dti;
        w.player2ReverseTimer += w.dti;
        if (w.player1ReverseEffectTimer > 0) w.player1ReverseEffectTimer -= w.dti;
        if (w.player2ReverseEffectTimer > 0) w.player2ReverseEffectTimer -= w.dti;
        int revLv1 = w.getEffectiveAbilityLevel(1, "reverse_controls");
        if (revLv1 > 0 && w.player1ReverseTimer >= 2000) {
            w.player2ReverseEffectTimer = 500 + (revLv1 - 1) * 80;
            w.player1ReverseTimer = 0;
        }
        int revLv2 = w.getEffectiveAbilityLevel(2, "reverse_controls");
        if (revLv2 > 0 && w.player2ReverseTimer >= 2000) {
            w.player1ReverseEffectTimer = 500 + (revLv2 - 1) * 80;
            w.player2ReverseTimer = 0;
        }

        // Blind ability: Q-press activates 5s darkness overlay on opponent's side, 15s cooldown.
        // player1BlindTimer counts up (cooldown). player1BlindEffectTimer counts down (active duration).
        // player1BlindLevel > 0 → darken P2's side. player2BlindLevel > 0 → darken P1's side.
        {
            if (w.getEffectiveAbilityLevel(1, "blind") > 0) w.player1BlindTimer = Math.min(w.player1BlindTimer + w.dti, 2000);
            if (w.getEffectiveAbilityLevel(2, "blind") > 0) w.player2BlindTimer = Math.min(w.player2BlindTimer + w.dti, 2000);
            if (w.player1BlindEffectTimer > 0) { w.player1BlindEffectTimer -= w.dti; w.player1BlindLevel = Math.min(w.getEffectiveAbilityLevel(1, "blind"), 3); }
            else w.player1BlindLevel = 0;
            if (w.player2BlindEffectTimer > 0) { w.player2BlindEffectTimer -= w.dti; w.player2BlindLevel = Math.min(w.getEffectiveAbilityLevel(2, "blind"), 3); }
            else w.player2BlindLevel = 0;
        }

        // Shrink opponent: cooldown ticks up, effect timer counts down (set by Q-press in handleAbilityKeyPress)
        w.player1ShrinkTimer += w.dti;
        w.player2ShrinkTimer += w.dti;
        if (w.player1ShrinkEffectTimer > 0) w.player1ShrinkEffectTimer -= w.dti;
        if (w.player2ShrinkEffectTimer > 0) w.player2ShrinkEffectTimer -= w.dti;

        // Ghost ball: ball invisible every 8s (800 ticks). Duration = 2s + 0.5s*level = 200+50*level ticks
        if (w.getEffectiveAbilityLevel(1, "ghost_ball") > 0) {
            w.player1GhostTimer += w.dti;
            if (w.player1GhostTimer >= w.ghostCooldown) {
                int ghostLevel = w.player1Abilities.get("ghost_ball");
                int ghostDur = 80 + ghostLevel * 20; // 100 at lv1, 120 at lv2, 140 at lv3...
                w.player2GhostEffectTimer = ghostDur;
                w.player2GhostHasPhased = false;
                w.player1GhostTimer = 0;
            }
        }
        if (w.getEffectiveAbilityLevel(2, "ghost_ball") > 0) {
            w.player2GhostTimer += w.dti;
            if (w.player2GhostTimer >= w.ghostCooldown) {
                int ghostLevel = w.player2Abilities.get("ghost_ball");
                int ghostDur = 80 + ghostLevel * 20;
                w.player1GhostEffectTimer = ghostDur;
                w.player1GhostHasPhased = false;
                w.player2GhostTimer = 0;
            }
        }
        if (w.player1GhostEffectTimer > 0) w.player1GhostEffectTimer -= w.dti;
        if (w.player2GhostEffectTimer > 0) w.player2GhostEffectTimer -= w.dti;

        // Gravity hammer: always-on once drawn. Gravity per level = level*0.25 (lv5=1.25/tick).
        // Applied in ball movement section — gravityActive kept for renderer queries.
        if (w.getEffectiveAbilityLevel(1, "gravity_hammer") > 0 || w.getEffectiveAbilityLevel(2, "gravity_hammer") > 0) {
            w.gravityActive = true;
        }

        // Passive barrier — always active once drawn, width grows per level
        {
            int barrierLv1 = w.getEffectiveAbilityLevel(1, "barrier");
            if (barrierLv1 > 0) {
                w.player1BarrierActive = true;
                w.player1BarrierX = 45;
                w.player1BarrierY = 50;
                w.barrierHeight = 80;
                w.player1BarrierWidth = Math.min(barrierLv1 * 10, 50);
            } else {
                w.player1BarrierActive = false;
            }
            int barrierLv2 = w.getEffectiveAbilityLevel(2, "barrier");
            if (barrierLv2 > 0) {
                w.player2BarrierActive = true;
                w.player2BarrierX = 540;
                w.player2BarrierY = 50;
                w.player2BarrierWidth = Math.min(barrierLv2 * 10, 50);
            } else {
                w.player2BarrierActive = false;
            }
        }

        // Screen warp level tracking (for cooldown scaling read in handleAbilityKeyPress)
        w.player1ScreenWarpLevel = w.getEffectiveAbilityLevel(1, "screen_warp");
        w.player2ScreenWarpLevel = w.getEffectiveAbilityLevel(2, "screen_warp");
    }

    private void updateScoring() {
        if (w.ballX < 0 || w.ballX > 600) {
            w.shrinkPaddlesActive = false; w.shrinkPaddlesDuration = 0;

            int scorer = 0;
            if (w.ballX < 0) {
                // Hollow Bankai auto-reflect (once per Bankai activation)
                if (w.player1HollowActive && !w.player1HollowPhased) {
                    w.player1HollowPhased = true;
                    w.ballVelX = Math.abs(w.ballVelX) + 2;
                    w.ballX = 8;
                    return;
                }
                // Player 2 scores
                scorer = 2;
                w.scorePlayer2 += 1;
                onPointScored(2);
                w.totalPointsThisLevel2 += 1;
                w.player1ComboCount = 0;
                // Level up check (legacy counter — card draws handled by onPointScored)
                if (w.totalPointsThisLevel2 >= w.pointsToNextLevel2) {
                    w.level2++;
                    w.totalPointsThisLevel2 = 0;
                    w.pointsToNextLevel2++;
                }
            } else {
                // Player 1 scores
                scorer = 1;
                w.scorePlayer1 += 1;
                onPointScored(1);
                w.totalPointsThisLevel1 += 1;
                w.player2ComboCount = 0;
                // Level up check (legacy counter — card draws handled by onPointScored)
                if (w.totalPointsThisLevel1 >= w.pointsToNextLevel1) {
                    w.level1++;
                    w.totalPointsThisLevel1 = 0;
                    w.pointsToNextLevel1++;
                    if (w.singlePlayer) {
                        w.ballSpeedMultiplier += 0.02;
                        w.aiSpeedMultiplier += 0.01;
                        w.powerUpSpawnInterval = Math.max(200, w.powerUpSpawnInterval - 30);
                    }
                }
            }

            // Reset haki effects on score
            w.player1HakiPhaseActive = false; w.player2HakiPhaseActive = false;
            w.player1ObsAutoBlockUsed = false; w.player2ObsAutoBlockUsed = false;
            w.player1ObsSlowFactor = 1.0; w.player2ObsSlowFactor = 1.0;
            w.player1ArmamentActive = false; w.player2ArmamentActive = false;
            w.player1ObservationActive = false; w.player2ObservationActive = false;

            w.noScoreTimer = 0;
            w.ballNotHitTimer = 0;
            resetBall(scorer);
        }
    }

    private void updatePowerUps() {
        // Spawning
        w.powerUpTimer += w.dti;
        if (w.powerUpTimer > w.powerUpSpawnInterval) {
            int x = 100 + (int)(Math.random() * 400);
            int y = 50 + (int)(Math.random() * 300);
            double rand = Math.random();
            String type;
            if (rand < 0.05) type = "giant";
            else if (rand < 0.10) type = "shrink";
            else if (rand < 0.20) {
                String[] mapTypes = {"dangerzone", "invisiblewalls", "shrinkpaddles", "centerwall"};
                type = mapTypes[(int)(Math.random() * mapTypes.length)];
            } else if (rand < 0.40) type = "speed";
            else if (rand < 0.60) type = "slow";
            else if (rand < 0.80) type = "paddle";
            else type = "reverse";
            w.powerUps.add(new PowerUp(x, y, type));
            w.powerUpTimer = 0;
        }

        // Collection
        w.tmpBallRect.set(w.ballX, w.ballY, 15, 15);
        for (int i = w.powerUps.size() - 1; i >= 0; i--) {
            PowerUp pu = w.powerUps.get(i);
            Rectangle pr = new Rectangle(pu.x, pu.y, 20, 20);
            if (w.tmpBallRect.overlaps(pr)) {
                applyPowerUp(pu.type);
                w.powerUps.remove(i);
            }
        }
    }

    private void applyPowerUp(String type) {
        if ("paddle".equals(type)) {
            if (w.ballVelX < 0 && w.paddle1Y > 0) w.paddle1Y = Math.max(0, w.paddle1Y - 20);
            else if (w.paddle2Y > 0) w.paddle2Y = Math.max(0, w.paddle2Y - 20);
        } else if ("slow".equals(type)) {
            w.ballVelX = w.ballVelX > 0 ? Math.max(2, w.ballVelX - 2) : Math.min(-2, w.ballVelX + 2);
        } else if ("speed".equals(type)) {
            w.ballVelX = w.ballVelX > 0 ? w.ballVelX + 2 : w.ballVelX - 2;
        } else if ("reverse".equals(type)) {
            w.ballVelX *= -1;
        } else if ("giant".equals(type)) {
            if (w.ballVelX < 0) w.paddle1Y = Math.max(0, Math.min(340, w.ballY - 50));
            else w.paddle2Y = Math.max(0, Math.min(340, w.ballY - 50));
        } else if ("shrink".equals(type)) {
            if (w.ballVelX < 0 && w.paddle2Y < 340) w.paddle2Y = Math.min(340, w.paddle2Y + 40);
            else if (w.paddle1Y < 340) w.paddle1Y = Math.min(340, w.paddle1Y + 40);
        } else if ("dangerzone".equals(type)) {
            w.dangerZoneActive = true; w.dangerZoneDuration = 0; w.dangerZoneTime = 0;
        } else if ("teleport".equals(type)) {
            w.ballX = 100 + (int)(Math.random() * 400); w.ballY = 50 + (int)(Math.random() * 300);
            w.ballVelX = (Math.random() < 0.5 ? -1 : 1) * (3 + (int)(Math.random() * 3));
            w.ballVelY = (int)(Math.random() * 6) - 3;
            if (w.ballVelY == 0) w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
            w.teleportActive = true; w.teleportDuration = 0;
        } else if ("fireball".equals(type)) {
            w.initialBallSpeed = w.ballSpeed * 1.8; w.ballSpeed = w.initialBallSpeed;
            double mag = Math.sqrt(w.ballVelX * w.ballVelX + w.ballVelY * w.ballVelY);
            if (mag > 0) { w.ballVelX = (int)((w.ballVelX / mag) * w.ballSpeed); w.ballVelY = (int)((w.ballVelY / mag) * w.ballSpeed); }
            w.fireballActive = true; w.fireballDuration = 0;
        } else if ("zigzag".equals(type)) {
            w.zigzagActive = true; w.zigzagDuration = 0; w.zigzagTimer = 0;
        } else if ("invisiblewalls".equals(type)) {
            w.invisibleWallsActive = true; w.invisibleWallsDuration = 0; w.invisibleWallY = 150 + (int)(Math.random() * 100);
        } else if ("shrinkpaddles".equals(type)) {
            w.shrinkPaddlesActive = true; w.shrinkPaddlesDuration = 0;
        } else if ("centerwall".equals(type)) {
            w.centerWallActive = true; w.centerWallDuration = 0; w.centerWallGapY = 100 + (int)(Math.random() * 200);
        }
    }

    /** Offer ability choice - uses DialogSystem for player input */
    private void offerAbilityChoice(int player, int startingLevel) {
        // Build pool: use deck for player 1 if deck is valid, else use all abilities
        ArrayList<String> pool = new ArrayList<String>();
        if (player == 1 && w.profile != null && DeckManager.isDeckValid(w.profile.deck)) {
            for (int i = 0; i < w.profile.deck.length; i++) {
                if (w.profile.deck[i] != null) pool.add(w.profile.deck[i]);
            }
        } else {
            for (String ability : w.allAbilities) {
                pool.add(ability);
            }
        }
        // Pick 3 random abilities from pool
        ArrayList<String> available = new ArrayList<String>(pool);
        final String[] opts = new String[3];
        for (int i = 0; i < 3 && !available.isEmpty(); i++) {
            int idx = (int)(Math.random() * available.size());
            opts[i] = available.get(idx);
            available.remove(idx);
        }

        final int p = player;
        final int lvl = startingLevel;
        final ArrayList<String> finalPool = pool;
        String[] displayOptions = new String[4];
        for (int i = 0; i < 3; i++) {
            displayOptions[i] = getAbilityShortName(opts[i]);
        }
        displayOptions[3] = "Random!";

        String title = (player == 1) ? "Player 1 - Level Up!" : (w.singlePlayer ? "AI - Level Up!" : "Player 2 - Level Up!");

        if (w.singlePlayer && player == 2) {
            // AI auto-picks from the 3 offered
            int choice = (int)(Math.random() * 3);
            grantAbility(p, opts[choice], lvl);
        } else {
            w.dialog.showOptions(title,
                "Choose an ability:",
                displayOptions,
                new DialogSystem.OptionCallback() {
                    public void onSelect(int index) {
                        if (index >= 0 && index < 3) {
                            grantAbility(p, opts[index], lvl);
                        } else if (index == 3) {
                            // Random! Pick from the same pool (deck or all)
                            String randomPick = finalPool.get((int)(Math.random() * finalPool.size()));
                            grantAbility(p, randomPick, lvl);
                            w.dialog.showMessage("Random Result!",
                                "Random selection picked:\n" + getAbilityShortName(randomPick) + "!",
                                new DialogSystem.MessageCallback() {
                                    public void onClose() {}
                                });
                        }
                    }
                });
        }
    }

    private void grantAbility(int player, String ability, int level) {
        LinkedHashMap<String, Integer> abilities = (player == 1) ? w.player1Abilities : w.player2Abilities;
        int currentLevel = abilities.containsKey(ability) ? abilities.get(ability) : 0;
        abilities.put(ability, Math.max(currentLevel + 1, level));
        Gdx.app.log("Pongus", "Player " + player + " gained " + ability + " Lv" + abilities.get(ability));
    }

    /** Reset ball to center with fresh velocity after a score. */
    private void resetBall(int scorer) {
        w.ballX = 292; // center (600/2 - 8 half-ball)
        w.ballY = 192; // center (400/2 - 8 half-ball)
        w.ballSpeed = 3.0;
        // Ball goes toward the scorer so they can serve
        // scorer==1 (P1, left paddle) → negative velX; scorer==2 (P2, right paddle) → positive velX
        w.ballVelX = (scorer == 1) ? -2 : 2;
        w.ballVelY = (Math.random() > 0.5) ? 2 : -2;
        w.ballXR = 0;
        w.ballYR = 0;
    }

    // ==================== AI ====================

    /**
     * AI for Player 2. Predicts where the ball will land and moves to intercept.
     * D1=beginner (can rally, makes mistakes), D2=intermediate, D3=hard.
     */
    private void updateAI(int paddleMaxY) {
        if (w.player2StunTimer > 0) return;

        // D1: forgiving — can rally but misses frequently
        // D2: moderate — makes mistakes on harder shots
        // D3: challenging — accurate but not perfect
        double predError  = 55; // ±px landing prediction error
        int    reactDelay = 50; // ticks between re-evaluating (~500ms)
        int    deadZone   = 10;

        if (w.aiDifficulty == 2) {
            predError  = 35;
            reactDelay = 33;
            deadZone   = 7;
        } else if (w.aiDifficulty == 3) {
            predError  = 18;
            reactDelay = 18;
            deadZone   = 5;
        }

        // AI speed (aiSpeedMultiplier set per trophy tier at match start)
        int aiSpeed = (int)(w.getPlayerSpeed(2) * w.aiSpeedMultiplier);
        int slowOppLevel = w.getEffectiveAbilityLevel(1, "slow_opponent");
        if (slowOppLevel > 0) {
            double slowFactor = Math.max(0.5, 1.0 - Math.min(slowOppLevel, 3) * 0.15);
            aiSpeed = Math.max(1, (int)(aiSpeed * slowFactor));
        }

        // Re-predict when ball direction changes or timer expires
        boolean ballComing = w.ballVelX > 0;
        boolean dirChanged = ballComing != w.aiWasBallMovingToward;
        w.aiWasBallMovingToward = ballComing;

        w.aiPredictionTimer += w.dti;
        if (dirChanged || w.aiPredictionTimer >= reactDelay) {
            w.aiPredictionTimer = 0;
            if (!ballComing) {
                // Ball going away — drift toward center with natural variation
                w.aiPredictedY = 192 + (int)((Math.random() - 0.5) * 50);
            } else {
                int pred = simulateBallY();
                pred += (int)((Math.random() - 0.5) * predError * 2);
                pred  = Math.max(5, Math.min(395, pred));
                w.aiPredictedY = pred;
            }
        }

        // Reverse controls — detect transitions
        boolean isReversed = w.player2ReverseEffectTimer > 0;
        if (isReversed != w.aiWasReversed) {
            w.aiReverseAdjustTimer = 25;
            w.aiPredictionTimer = 999;
        }
        w.aiWasReversed = isReversed;
        if (w.aiReverseAdjustTimer > 0) w.aiReverseAdjustTimer -= w.dti;

        // Micro-jitter: refresh small random sway every 12-27 ticks (human idle movement)
        w.aiJitterTimer -= w.dti;
        if (w.aiJitterTimer <= 0) {
            w.aiJitterOffset = (int)((Math.random() - 0.5) * 14);
            w.aiJitterTimer  = 12 + (int)(Math.random() * 15);
        }

        int targetY = w.aiPredictedY;

        if (w.aiReverseAdjustTimer > 0) {
            // Initial confusion period: AI drifts randomly near current position
            int paddleCenter = w.paddle2Y + w.getPaddleHeight(2, w.shrinkPaddlesActive) / 2;
            targetY = paddleCenter + (int)((Math.random() - 0.5) * 40);
        } else if (isReversed) {
            // Confused by reversed controls: mostly goes to the wrong (reflected) spot
            // with heavy noise, like a player frantically trying to compensate and failing.
            int wrongTarget = 400 - w.aiPredictedY;
            targetY = wrongTarget + (int)((Math.random() - 0.5) * predError * 3);
            targetY = Math.max(5, Math.min(395, targetY));
        } else {
            // Normal: add micro-jitter so the AI looks alive rather than robotically still
            targetY += w.aiJitterOffset;
        }

        int paddleCenter2 = w.paddle2Y + w.getPaddleHeight(2, w.shrinkPaddlesActive) / 2;
        if (targetY < paddleCenter2 - deadZone) {
            w.paddle2Y = Math.max(0, w.paddle2Y - (int)(aiSpeed * w.dt));
        } else if (targetY > paddleCenter2 + deadZone) {
            w.paddle2Y = Math.min(paddleMaxY, w.paddle2Y + (int)(aiSpeed * w.dt));
        }

        // AI ability usage
        double abilityChance = w.aiDifficulty == 3 ? 0.025 : w.aiDifficulty == 2 ? 0.010 : 0.004;
        if (Math.random() < abilityChance * w.dti) {
            handleAbilityKeyPress(w.player2AbilityKey);
        }
    }

    /** Simulate ball trajectory with dt to predict landing Y at the AI paddle (x≈555). */
    private int simulateBallY() {
        double px  = w.ballX + 7;
        double py  = w.ballY + 7;
        double pvx = w.ballVelX * w.ballSpeedMultiplier;
        double pvy = w.ballVelY * w.ballSpeedMultiplier;
        double step = Math.max(0.5, w.dt); // use actual frame step
        for (int i = 0; i < 1200; i++) {
            px += pvx * step;
            py += pvy * step;
            if (py < 0)   { py = -py;       pvy = -pvy; }
            if (py > 400) { py = 800 - py;  pvy = -pvy; }
            if (px >= 555) return (int)py;
            if (px <= 0)  return 192; // ball went the other way
        }
        return (int)py;
    }

    // ==================== ABILITY HELPERS ====================

    /** @deprecated Use w.getEffectiveAbilityLevel() directly — delegates to GameWorld */
    int getEffectiveAbilityLevel(int player, String ability) {
        return w.getEffectiveAbilityLevel(player, ability);
    }

    /** @deprecated Use w.getPaddleHeight() directly — delegates to GameWorld */
    int getPaddleHeight(int player, boolean shrinkActive) {
        return w.getPaddleHeight(player, shrinkActive);
    }

    /** @deprecated Use w.getPlayerSpeed() directly — delegates to GameWorld */
    int getPlayerSpeed(int player) {
        return w.getPlayerSpeed(player);
    }

    int predictBallYPosition(int bx, int by, int bvx, int bvy, int targetX) {
        if (bvx == 0) return by;
        int steps = Math.abs((targetX - bx) / Math.max(1, Math.abs(bvx)));
        int y = by;
        int vy = bvy;
        for (int i = 0; i < steps && i < 200; i++) {
            y += vy;
            if (y <= 0) { y = -y; vy = Math.abs(vy); }
            else if (y >= 385) { y = 770 - y; vy = -Math.abs(vy); }
        }
        return Math.max(0, Math.min(385, y));
    }

    void spawnMapPowerUp() {
        String[] mapTypes = {"dangerzone", "teleport", "fireball", "zigzag", "invisiblewalls", "shrinkpaddles", "centerwall"};
        String type = mapTypes[(int)(Math.random() * mapTypes.length)];
        int x = 100 + (int)(Math.random() * 400);
        int y = 50 + (int)(Math.random() * 300);
        w.powerUps.add(new PowerUp(x, y, type));
    }

    void checkEvolvedAbilityLevelUps(int player, int pointsGained) {
        // Auto-level evolved abilities (simplified)
        // In the original, evolved abilities level up every 20 points
    }

    int getNumberOfShadows(int player) {
        int level = w.getEffectiveAbilityLevel(player, "shadow_clone");
        return Math.min(level, 3);
    }

    String getAbilityShortName(String ability) {
        return abilityManager.getShortName(ability);
    }

    String getBranchName(String ability, int branch) {
        String name = abilityManager.getBranchName(ability, branch);
        return (name != null) ? name : getAbilityShortName(ability);
    }


    // ==================== ABILITY KEY PRESS HANDLER ====================

    /**
     * Handles ability key presses for both players.
     * Called from keyDown when w.player1AbilityKey or w.player2AbilityKey is pressed.
     * Ports keyPressed ability activation logic from original PingPongGame (lines 9504-10230).
     */
    private void handleAbilityKeyPress(int keycode) {
        // Sync ability activation to peer in online matches
        if (w.isOnlineMatch) {
            int playerN = (keycode == w.player1AbilityKey) ? 1 : 2;
            w.network.send("A:" + playerN);
        }

        // === MAGNET BALL branch 2: Force Field toggle ===
        // Magnet is passive — no key press needed

        // === PORTAL PONG ===
        handlePortalAbilityKey(keycode);

        // === TIME LOOP ===
        handleTimeLoopAbilityKey(keycode);

        // === HAKI ===
        handleHakiAbilityKey(keycode);

        // === TRAP ===
        // Cooldown: 500 ticks at lv1, -60 per level, min 200
        {
            int trapLevel1 = w.getEffectiveAbilityLevel(1, "trap");
            int trapCD1 = Math.max(200, 500 - (trapLevel1 - 1) * 60);
            if (keycode == w.player1AbilityKey && trapLevel1 > 0 && w.player1TrapTimer >= trapCD1 && !w.player1TrapActive) {
                w.player1TrapActive = true;
                w.player1TrapX = 180 + (int)(Math.random() * 230);
                w.player1TrapY = 30 + (int)(Math.random() * 340);
                w.player1TrapDuration = w.trapBaseDuration + (trapLevel1 - 1) * 150;
                w.player1TrapTimer = 0;
            }
        }
        {
            int trapLevel2 = w.getEffectiveAbilityLevel(2, "trap");
            int trapCD2 = Math.max(200, 500 - (trapLevel2 - 1) * 60);
            if (keycode == w.player2AbilityKey && trapLevel2 > 0 && w.player2TrapTimer >= trapCD2 && !w.player2TrapActive) {
                w.player2TrapActive = true;
                w.player2TrapX = 180 + (int)(Math.random() * 230);
                w.player2TrapY = 30 + (int)(Math.random() * 340);
                w.player2TrapDuration = w.trapBaseDuration + (trapLevel2 - 1) * 150;
                w.player2TrapTimer = 0;
            }
        }

        // === SCREEN WARP ===
        // Cooldown: 600 ticks at lv1, -60 per level, min 240. Duration 200 ticks.
        {
            int warpLevel1 = w.getEffectiveAbilityLevel(1, "screen_warp");
            int warpCD1 = Math.max(240, 600 - (warpLevel1 - 1) * 60);
            if (keycode == w.player1AbilityKey && warpLevel1 > 0 && w.player1WarpTimer >= warpCD1) {
                int warpBranch1 = w.player1AbilityBranches.containsKey("screen_warp") ? w.player1AbilityBranches.get("screen_warp") : 0;
                if (warpBranch1 == 1 && warpLevel1 >= 3) {
                    w.player2InversionTimer = 200 + (warpLevel1 - 2) * 30;
                } else if (warpBranch1 == 2 && warpLevel1 >= 3) {
                    w.player2TunnelTimer = 200 + (warpLevel1 - 2) * 40;
                } else {
                    w.player2WarpEffectTimer = 300 + (warpLevel1 - 1) * 20;
                }
                w.player1WarpTimer = 0;
            }
        }
        {
            int warpLevel2 = w.getEffectiveAbilityLevel(2, "screen_warp");
            int warpCD2 = Math.max(240, 600 - (warpLevel2 - 1) * 60);
            if (keycode == w.player2AbilityKey && warpLevel2 > 0 && w.player2WarpTimer >= warpCD2) {
                int warpBranch2 = w.player2AbilityBranches.containsKey("screen_warp") ? w.player2AbilityBranches.get("screen_warp") : 0;
                if (warpBranch2 == 1 && warpLevel2 >= 3) {
                    w.player1InversionTimer = 200 + (warpLevel2 - 2) * 30;
                } else if (warpBranch2 == 2 && warpLevel2 >= 3) {
                    w.player1TunnelTimer = 200 + (warpLevel2 - 2) * 40;
                } else {
                    w.player1WarpEffectTimer = 300 + (warpLevel2 - 1) * 20;
                }
                w.player2WarpTimer = 0;
            }
        }

        // === BANKAI ===
        handleBankaiAbilityKey(keycode);

        // === SHRINK OPPONENT ===
        handleShrinkOpponentAbilityKey(keycode);

        // === BLIND ===
        handleBlindAbilityKey(keycode);
    }

    /** Portal Pong ability key handler */
    private void handlePortalAbilityKey(int keycode) {
        // Base cooldown: 600 ticks at lv1, -80 per level, min 200
        // Player 1
        if (keycode == w.player1AbilityKey && w.getEffectiveAbilityLevel(1, "portal_pong") > 0) {
            int portalLevel = w.player1Abilities.containsKey("portal_pong") ? w.player1Abilities.get("portal_pong") : 1;
            int portalBranch = w.player1AbilityBranches.containsKey("portal_pong") ? w.player1AbilityBranches.get("portal_pong") : 0;
            int cooldown;
            if (portalBranch == 1 && portalLevel >= 3) {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80) - (portalLevel - 2) * 20;
                if (cooldown < 200) cooldown = 200;
            } else if (portalBranch == 2 && portalLevel >= 3) {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80) - (portalLevel - 2) * 30;
                if (cooldown < 200) cooldown = 200;
            } else {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80);
            }
            if (w.player1PortalTimer >= cooldown) {
                if (w.player1PortalPlacementStage == 0) {
                    // Place entrance portal
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player1PortalEntranceX = Math.max(50, Math.min(550, w.ballX));
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalEntranceX = 50 + (int)(Math.random() * 500);
                    } else {
                        w.player1PortalEntranceX = 50;
                    }
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalEntranceY = 50 + (int)(Math.random() * 300);
                    } else {
                        w.player1PortalEntranceY = w.paddle1Y + (w.getPaddleHeight(1, w.shrinkPaddlesActive) / 2);
                    }
                    w.player1PortalPlacementStage = 1;
                } else if (w.player1PortalPlacementStage == 1) {
                    // Place exit portal
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player1PortalExitX = Math.max(50, Math.min(550, w.ballX));
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalExitX = 50 + (int)(Math.random() * 500);
                    } else {
                        w.player1PortalExitX = 50;
                    }
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalExitY = 50 + (int)(Math.random() * 300);
                    } else {
                        w.player1PortalExitY = w.paddle1Y + (w.getPaddleHeight(1, w.shrinkPaddlesActive) / 2);
                    }
                    w.player1PortalPlacementStage = 2;
                    // Activate portals with branch-specific durations
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player1PortalDuration = 500 + (portalLevel - 2) * 200;
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalDuration = 600 + (portalLevel - 2) * 100;
                    } else {
                        w.player1PortalDuration = 600;
                    }
                    w.player1PortalTimer = 0;
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player1PortalMoveTimer = 200;
                    }
                }
            }
        }
        // Player 2
        if (keycode == w.player2AbilityKey && w.getEffectiveAbilityLevel(2, "portal_pong") > 0) {
            int portalLevel = w.player2Abilities.containsKey("portal_pong") ? w.player2Abilities.get("portal_pong") : 1;
            int portalBranch = w.player2AbilityBranches.containsKey("portal_pong") ? w.player2AbilityBranches.get("portal_pong") : 0;
            int cooldown;
            if (portalBranch == 1 && portalLevel >= 3) {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80) - (portalLevel - 2) * 20;
                if (cooldown < 200) cooldown = 200;
            } else if (portalBranch == 2 && portalLevel >= 3) {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80) - (portalLevel - 2) * 30;
                if (cooldown < 200) cooldown = 200;
            } else {
                cooldown = Math.max(200, 600 - (portalLevel - 1) * 80);
            }
            if (w.player2PortalTimer >= cooldown) {
                if (w.player2PortalPlacementStage == 0) {
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player2PortalEntranceX = Math.max(50, Math.min(550, w.ballX));
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalEntranceX = 50 + (int)(Math.random() * 500);
                    } else {
                        w.player2PortalEntranceX = 550;
                    }
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalEntranceY = 50 + (int)(Math.random() * 300);
                    } else {
                        w.player2PortalEntranceY = w.paddle2Y + (w.getPaddleHeight(2, w.shrinkPaddlesActive) / 2);
                    }
                    w.player2PortalPlacementStage = 1;
                } else if (w.player2PortalPlacementStage == 1) {
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player2PortalExitX = Math.max(50, Math.min(550, w.ballX));
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalExitX = 50 + (int)(Math.random() * 500);
                    } else {
                        w.player2PortalExitX = 550;
                    }
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalExitY = 50 + (int)(Math.random() * 300);
                    } else {
                        w.player2PortalExitY = w.paddle2Y + (w.getPaddleHeight(2, w.shrinkPaddlesActive) / 2);
                    }
                    w.player2PortalPlacementStage = 2;
                    if (portalBranch == 1 && portalLevel >= 3) {
                        w.player2PortalDuration = 500 + (portalLevel - 2) * 200;
                    } else if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalDuration = 600 + (portalLevel - 2) * 100;
                    } else {
                        w.player2PortalDuration = 600;
                    }
                    w.player2PortalTimer = 0;
                    if (portalBranch == 2 && portalLevel >= 3) {
                        w.player2PortalMoveTimer = 200;
                    }
                }
            }
        }
    }


    /** Time Loop ability key handler */
    private void handleTimeLoopAbilityKey(int keycode) {
        // Player 1
        // Base cooldown: 1000 ticks at lv1, -100 per level, min 400
        if (keycode == w.player1AbilityKey && w.getEffectiveAbilityLevel(1, "time_loop") > 0 && !w.gameStateHistory.isEmpty()) {
            int timeLoopLevel = w.getEffectiveAbilityLevel(1, "time_loop");
            int timeLoopBranch = w.player1AbilityBranches.containsKey("time_loop") ? w.player1AbilityBranches.get("time_loop") : 0;
            int currentCD;
            if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100) - (timeLoopLevel - 2) * 50;
                if (currentCD < 400) currentCD = 400;
            } else if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100) - (timeLoopLevel - 2) * 80;
                if (currentCD < 400) currentCD = 400;
            } else {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100);
            }
            if (w.player1TimeLoopTimer >= currentCD) {
                GameStateSnapshot state = w.gameStateHistory.get(0);
                if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
                    // Temporal Echo - create ghost ball
                    w.temporalEchoActive = true;
                    w.echoGhostBallX = state.ballX;
                    w.echoGhostBallY = state.ballY;
                    w.echoGhostBallVelX = state.ballVelX;
                    w.echoGhostBallVelY = state.ballVelY;
                    w.temporalEchoDuration = 300 + (timeLoopLevel - 2) * 50;
                } else {
                    // Normal rewind or Chronos Rewind
                    w.ballX = state.ballX;
                    w.ballY = state.ballY;
                    w.ballVelX = state.ballVelX;
                    w.ballVelY = state.ballVelY;
                    if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
                        // Chronos Rewind - go further back
                        int historyIndex = Math.min((timeLoopLevel - 2) * 20 + 30, w.gameStateHistory.size() - 1);
                        GameStateSnapshot deeperState = w.gameStateHistory.get(Math.max(0, historyIndex));
                        w.ballX = deeperState.ballX;
                        w.ballY = deeperState.ballY;
                        w.ballVelX = deeperState.ballVelX;
                        w.ballVelY = deeperState.ballVelY;
                        w.paddle1Y = (int)(w.paddle1Y * 0.9 + state.paddle1Y * 0.1);
                        w.player1TimeLoopSlowMoTimer = 200 + (timeLoopLevel - 2) * 50;
                    } else {
                        // Base rewind - full restore
                        w.paddle1Y = state.paddle1Y;
                        w.paddle2Y = state.paddle2Y;
                        w.scorePlayer1 = state.player1Score;
                        w.scorePlayer2 = state.player2Score;
                        w.player1TimeLoopSlowMoTimer = w.timeLoopSlowMoDuration + (timeLoopLevel - 1) * 30;
                    }
                }
                w.player1TimeLoopTimer = 0;
            }
        }
        // Player 2
        if (keycode == w.player2AbilityKey && w.getEffectiveAbilityLevel(2, "time_loop") > 0 && !w.gameStateHistory.isEmpty()) {
            int timeLoopLevel = w.getEffectiveAbilityLevel(2, "time_loop");
            int timeLoopBranch = w.player2AbilityBranches.containsKey("time_loop") ? w.player2AbilityBranches.get("time_loop") : 0;
            int currentCD;
            if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100) - (timeLoopLevel - 2) * 50;
                if (currentCD < 400) currentCD = 400;
            } else if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100) - (timeLoopLevel - 2) * 80;
                if (currentCD < 400) currentCD = 400;
            } else {
                currentCD = Math.max(400, 1000 - (timeLoopLevel - 1) * 100);
            }
            if (w.player2TimeLoopTimer >= currentCD) {
                GameStateSnapshot state = w.gameStateHistory.get(0);
                if (timeLoopBranch == 2 && timeLoopLevel >= 3) {
                    w.temporalEchoActive = true;
                    w.echoGhostBallX = state.ballX;
                    w.echoGhostBallY = state.ballY;
                    w.echoGhostBallVelX = state.ballVelX;
                    w.echoGhostBallVelY = state.ballVelY;
                    w.temporalEchoDuration = 300 + (timeLoopLevel - 2) * 50;
                } else {
                    w.ballX = state.ballX;
                    w.ballY = state.ballY;
                    w.ballVelX = state.ballVelX;
                    w.ballVelY = state.ballVelY;
                    if (timeLoopBranch == 1 && timeLoopLevel >= 3) {
                        int historyIndex = Math.min((timeLoopLevel - 2) * 20 + 30, w.gameStateHistory.size() - 1);
                        GameStateSnapshot deeperState = w.gameStateHistory.get(Math.max(0, historyIndex));
                        w.ballX = deeperState.ballX;
                        w.ballY = deeperState.ballY;
                        w.ballVelX = deeperState.ballVelX;
                        w.ballVelY = deeperState.ballVelY;
                        w.paddle2Y = (int)(w.paddle2Y * 0.9 + state.paddle2Y * 0.1);
                        w.player2TimeLoopSlowMoTimer = 200 + (timeLoopLevel - 2) * 50;
                    } else {
                        w.paddle1Y = state.paddle1Y;
                        w.paddle2Y = state.paddle2Y;
                        w.scorePlayer1 = state.player1Score;
                        w.scorePlayer2 = state.player2Score;
                        w.player2TimeLoopSlowMoTimer = w.timeLoopSlowMoDuration + (timeLoopLevel - 1) * 30;
                    }
                }
                w.player2TimeLoopTimer = 0;
            }
        }
    }

    /** Haki ability key handler — simplified to invincibility + speed burst */
    private void handleHakiAbilityKey(int keycode) {
        // Player 1
        {
            int lv = w.getEffectiveAbilityLevel(1, "haki");
            int cd = Math.max(200, 600 - (lv - 1) * 50);
            if (keycode == w.player1AbilityKey && lv > 0 && w.player1HakiTimer >= cd) {
                int dur = 100 + lv * 20;
                w.player1HakiPhaseActive = true;
                w.player1HakiDuration = dur;
                w.player1HakiSpeedBoost = lv;
                w.player1HakiTimer = 0;
            }
        }
        // Player 2
        {
            int lv = w.getEffectiveAbilityLevel(2, "haki");
            int cd = Math.max(200, 600 - (lv - 1) * 50);
            if (keycode == w.player2AbilityKey && lv > 0 && w.player2HakiTimer >= cd) {
                int dur = 100 + lv * 20;
                w.player2HakiPhaseActive = true;
                w.player2HakiDuration = dur;
                w.player2HakiSpeedBoost = lv;
                w.player2HakiTimer = 0;
            }
        }
    }

    /** Bankai ability key handler — simplified to aura + burst on next hit */
    private void handleBankaiAbilityKey(int keycode) {
        // Player 1
        {
            int lv1 = w.getEffectiveAbilityLevel(1, "bankai");
            int cd1 = Math.max(300, 1000 - (lv1 - 1) * 100);
            if (keycode == w.player1AbilityKey && lv1 > 0 && w.player1BankaiTimer >= cd1) {
                w.player1BankaiActive = true;
                w.player1BankaiArmed = true;
                w.player1BankaiAuraDuration = 150;
                w.player1BankaiTimer = 0;
            }
        }
        // Player 2
        {
            int lv2 = w.getEffectiveAbilityLevel(2, "bankai");
            int cd2 = Math.max(300, 1000 - (lv2 - 1) * 100);
            if (keycode == w.player2AbilityKey && lv2 > 0 && w.player2BankaiTimer >= cd2) {
                w.player2BankaiActive = true;
                w.player2BankaiArmed = true;
                w.player2BankaiAuraDuration = 150;
                w.player2BankaiTimer = 0;
            }
        }
    }

    /** Reverse controls ability key handler — active Q-press, 5s effect, 9s cooldown */
    private void handleReverseControlsAbilityKey(int keycode) {
        // Player 1 — reverses P2 controls
        {
            int lv1 = w.getEffectiveAbilityLevel(1, "reverse_controls");
            int cd1 = Math.max(400, 900 - (lv1 - 1) * 80);
            if (keycode == w.player1AbilityKey && lv1 > 0 && w.player1ReverseTimer >= cd1) {
                int dur = 500 + (lv1 - 1) * 80;
                w.player2ReverseEffectTimer = dur;
                w.player1ReverseTimer = 0;
            }
        }
        // Player 2 — reverses P1 controls
        {
            int lv2 = w.getEffectiveAbilityLevel(2, "reverse_controls");
            int cd2 = Math.max(400, 900 - (lv2 - 1) * 80);
            if (keycode == w.player2AbilityKey && lv2 > 0 && w.player2ReverseTimer >= cd2) {
                int dur = 500 + (lv2 - 1) * 80;
                w.player1ReverseEffectTimer = dur;
                w.player2ReverseTimer = 0;
            }
        }
    }

    /** Blind ability key handler — Q-press, 5s darkness on opponent's side, 15s cooldown */
    private void handleBlindAbilityKey(int keycode) {
        // Player 1 — blinds P2's side
        {
            int lv1 = w.getEffectiveAbilityLevel(1, "blind");
            if (keycode == w.player1AbilityKey && lv1 > 0 && w.player1BlindTimer >= 2000) {
                w.player1BlindEffectTimer = 200 + (lv1 - 1) * 50;
                w.player1BlindTimer = 0;
            }
        }
        // Player 2 — blinds P1's side
        {
            int lv2 = w.getEffectiveAbilityLevel(2, "blind");
            if (keycode == w.player2AbilityKey && lv2 > 0 && w.player2BlindTimer >= 2000) {
                w.player2BlindEffectTimer = 200 + (lv2 - 1) * 50;
                w.player2BlindTimer = 0;
            }
        }
    }

    /** Shrink opponent ability key handler — active Q-press, halves paddle for 5s, 9s cooldown */
    private void handleShrinkOpponentAbilityKey(int keycode) {
        // Player 1 — shrinks P2 paddle
        {
            int lv1 = w.getEffectiveAbilityLevel(1, "shrink_opponent");
            int cd1 = Math.max(400, 900 - (lv1 - 1) * 80);
            if (keycode == w.player1AbilityKey && lv1 > 0 && w.player1ShrinkTimer >= cd1) {
                int dur = 500 + (lv1 - 1) * 80;
                w.player2ShrinkEffectTimer = dur;
                w.player1ShrinkTimer = 0;
            }
        }
        // Player 2 — shrinks P1 paddle
        {
            int lv2 = w.getEffectiveAbilityLevel(2, "shrink_opponent");
            int cd2 = Math.max(400, 900 - (lv2 - 1) * 80);
            if (keycode == w.player2AbilityKey && lv2 > 0 && w.player2ShrinkTimer >= cd2) {
                int dur = 500 + (lv2 - 1) * 80;
                w.player1ShrinkEffectTimer = dur;
                w.player2ShrinkTimer = 0;
            }
        }
    }

    // ==================== CHEAT MENU ====================

    private void handleCheatMenu() {
        w.isPaused = true;
        if (!w.cheatModeEnabled) {
            w.dialog.showInput("Cheat Mode", "Enter cheat password:", null, new DialogSystem.InputCallback() {
                public void onResult(String result) {
                    if (result != null && result.equals(w.cheatPassword)) {
                        w.cheatModeEnabled = true;
                        w.dialog.showMessage("Success!", "Cheat Mode Activated!\nPress M to open menu!", new DialogSystem.MessageCallback() {
                            public void onClose() { w.isPaused = false; }
                        });
                    } else {
                        w.dialog.showMessage("Access Denied", "Wrong password!", new DialogSystem.MessageCallback() {
                            public void onClose() { w.isPaused = false; }
                        });
                    }
                }
            });
        } else {
            w.dialog.showOptions("Cheat Menu", "What would you like to do?",
                new String[]{"Spawn Power-Up", "Give Player Ability", "Delete Player Ability"},
                new DialogSystem.OptionCallback() {
                    public void onSelect(int index) {
                        if (index == 0) {
                            // Spawn Power-Up
                            String[] powerUpTypes = {"Speed Up", "Slow Down", "Multiball", "Teleport", "Fireball",
                                "Zigzag", "Split", "Jumpscare", "Mirror", "Center Wall",
                                "Invisible Walls", "Shrink Paddles", "Danger Zone"};
                            w.dialog.showOptions("Spawn Power-Up", "Select power-up type:", powerUpTypes,
                                new DialogSystem.OptionCallback() {
                                    public void onSelect(int pIdx) {
                                        if (pIdx >= 0 && pIdx < powerUpTypes.length) {
                                            w.powerUps.add(new PowerUp(300, 200, powerUpTypes[pIdx]));
                                        }
                                        w.isPaused = false;
                                    }
                                });
                        } else if (index == 1) {
                            // Give ability
                            w.dialog.showOptions("Give Ability", "Which player?",
                                new String[]{"Player 1", "Player 2"},
                                new DialogSystem.OptionCallback() {
                                    public void onSelect(int pIdx) {
                                        final int targetPlayer = pIdx + 1;
                                        w.dialog.showOptions("Give Ability", "Select ability:",
                                            w.allAbilities,
                                            new DialogSystem.OptionCallback() {
                                                public void onSelect(int aIdx) {
                                                    if (aIdx >= 0 && aIdx < w.allAbilities.length) {
                                                        grantAbility(targetPlayer, w.allAbilities[aIdx], 1);
                                                    }
                                                    w.isPaused = false;
                                                }
                                            });
                                    }
                                });
                        } else if (index == 2) {
                            // Delete ability
                            w.dialog.showOptions("Delete Ability", "Which player?",
                                new String[]{"Player 1", "Player 2"},
                                new DialogSystem.OptionCallback() {
                                    public void onSelect(int pIdx) {
                                        final int targetPlayer = pIdx + 1;
                                        final LinkedHashMap<String, Integer> targetAbilities = (targetPlayer == 1) ? w.player1Abilities : w.player2Abilities;
                                        if (targetAbilities.isEmpty()) {
                                            w.dialog.showMessage("Delete Ability", "No abilities to delete!", new DialogSystem.MessageCallback() {
                                                public void onClose() { w.isPaused = false; }
                                            });
                                            return;
                                        }
                                        ArrayList<String> abilityNames = new ArrayList<String>(targetAbilities.keySet());
                                        String[] nameArray = abilityNames.toArray(new String[0]);
                                        w.dialog.showOptions("Delete Ability", "Select ability to remove:",
                                            nameArray,
                                            new DialogSystem.OptionCallback() {
                                                public void onSelect(int aIdx) {
                                                    if (aIdx >= 0 && aIdx < nameArray.length) {
                                                        targetAbilities.remove(nameArray[aIdx]);
                                                    }
                                                    w.isPaused = false;
                                                }
                                            });
                                    }
                                });
                        } else {
                            w.isPaused = false;
                        }
                    }
                });
        }
    }

    // ==================== RESET GAME STATE ====================

    /** Resets all game state for a new game. */
    void resetGameState() {
        w.scorePlayer1 = 0;
        w.scorePlayer2 = 0;
        w.level1 = 1;
        w.level2 = 1;
        w.pointsToNextLevel1 = 1;
        w.pointsToNextLevel2 = 1;
        w.totalPointsThisLevel1 = 0;
        w.totalPointsThisLevel2 = 0;
        w.ballX = 250; w.ballY = 150;
        w.ballVelX = 2; w.ballVelY = 2;
        w.initialBallVelX = 2; w.initialBallVelY = 2;
        w.ballSpeed = 3.0;
        w.paddle1Y = 100; w.paddle2Y = 100;
        w.player1Abilities.clear();
        w.player2Abilities.clear();
        w.player1AbilityBranches.clear();
        w.player2AbilityBranches.clear();
        w.powerUps.clear();
        w.bullets.clear();
        w.explosions.clear();
        w.activeLasers.clear();
        w.vegetaBullets.clear();
        w.freezaLasers.clear();
        w.jirenBullets.clear();
        w.getsugaProjectiles.clear();
        w.gameStateHistory.clear();
        w.player1ShadowPositions.clear();
        w.player2ShadowPositions.clear();
        w.noScoreTimer = 0;
        w.ballNotHitTimer = 0;

        // Reset all ability timers
        w.player1GunTimer = 0; w.player2GunTimer = 0;
        w.player1HammerTimer = 0; w.player2HammerTimer = 0;
        w.player1PortalTimer = 0; w.player2PortalTimer = 0;
        w.player1TimeLoopTimer = 0; w.player2TimeLoopTimer = 0;
        w.player1HakiTimer = 0; w.player2HakiTimer = 0;
        w.player1BarrierTimer = 0; w.player2BarrierTimer = 0;
        w.player1TrapTimer = 0; w.player2TrapTimer = 0;
        w.player1WarpTimer = 0; w.player2WarpTimer = 0;
        w.player1BankaiTimer = w.bankaiCooldown; w.player2BankaiTimer = w.bankaiCooldown;
        w.player1DashTimer = 0; w.player2DashTimer = 0;

        // Reset active effects
        w.player1BankaiActive = false; w.player2BankaiActive = false;
        w.player1ZangetsuActive = false; w.player2ZangetsuActive = false;
        w.player1HollowActive = false; w.player2HollowActive = false;
        w.player1HammerActive = false; w.player2HammerActive = false;
        w.player1BarrierActive = false; w.player2BarrierActive = false;
        w.player1TrapActive = false; w.player2TrapActive = false;
        w.player1ArmamentActive = false; w.player2ArmamentActive = false;
        w.player1ObservationActive = false; w.player2ObservationActive = false;
        w.player1GravityWellActive = false; w.player2GravityWellActive = false;
        w.temporalEchoActive = false;
        w.realityBreakActive = false;

        // Reset permanent bonuses
        w.player1BankaiStacks = 0; w.player2BankaiStacks = 0;
        w.player1PermanentSpeedBonus = 0; w.player2PermanentSpeedBonus = 0;
        w.player1PermanentPaddleBonus = 0; w.player2PermanentPaddleBonus = 0;
        w.player1BallSpeedMultiplier = 1.0; w.player2BallSpeedMultiplier = 1.0;
        w.player1HasBankaiImmunity = false; w.player2HasBankaiImmunity = false;
        w.player1TotalSwordSwings = 0; w.player2TotalSwordSwings = 0;

        // Reset AI prediction state
        w.aiPredictedY = 192; w.aiPredictionTimer = 999; w.aiWasBallMovingToward = false;
        w.aiWasReversed = false; w.aiReverseAdjustTimer = 0;
        w.aiJitterOffset = 0; w.aiJitterTimer = 0;

        // Reset stun/effect timers
        w.player1StunTimer = 0; w.player2StunTimer = 0;
        w.player1StunImmunityTimer = 0; w.player2StunImmunityTimer = 0;
        w.player1LagEffectTimer = 0; w.player2LagEffectTimer = 0;
        w.player1ReverseEffectTimer = 0; w.player2ReverseEffectTimer = 0;
        w.player1BlindEffectTimer = 0; w.player2BlindEffectTimer = 0;
        w.player1BlindLevel = 0; w.player2BlindLevel = 0;
        w.player1ScreenWarpLevel = 0; w.player2ScreenWarpLevel = 0;
        w.player1ShrinkEffectTimer = 0; w.player2ShrinkEffectTimer = 0;
        w.player1InversionTimer = 0; w.player2InversionTimer = 0;
        w.player1WarpEffectTimer = 0; w.player2WarpEffectTimer = 0;
        w.player1TunnelTimer = 0; w.player2TunnelTimer = 0;
        w.player1TimeLoopSlowMoTimer = 0; w.player2TimeLoopSlowMoTimer = 0;

        // Reset portal state
        w.player1PortalPlacementStage = 0; w.player2PortalPlacementStage = 0;
        w.player1PortalEntranceX = null; w.player2PortalEntranceX = null;
        w.player1PortalExitX = null; w.player2PortalExitX = null;
        w.player1PortalDuration = 0; w.player2PortalDuration = 0;
        w.portalBoostTimer = 0;
        w.ballTeleportCooldown = false;

        // Reset combo/underdog
        w.player1ComboCount = 0; w.player2ComboCount = 0;
        w.player1LastUnderdogTrigger = 0; w.player2LastUnderdogTrigger = 0;

        // Reset map effects
        w.teleportDuration = 0; w.fireballDuration = 0;
        w.zigzagDuration = 0; w.splitDuration = 0;
        w.centerWallDuration = 0; w.invisibleWallsDuration = 0;
        w.shrinkPaddlesDuration = 0; w.dangerZoneDuration = 0;

        // Reset cheat mode
        w.cheatModeEnabled = false;

        // Reset drag pointers
        w.p1DragPointer = -1;
        w.p2DragPointer = -1;
        w.networkOpponentPaddleY = 200f;

        // Reset match timer and overtime state
        w.matchTimer = 0;
        w.matchTimerActive = true;
        w.overtimeActive = false;
        w.suddenDeathMode = false;
        w.ball2Active = false;

        // Pick a random fake username for the AI opponent
        int nameIdx = com.badlogic.gdx.math.MathUtils.random(FAKE_USERNAMES.length - 1);
        w.player2DisplayName = FAKE_USERNAMES[nameIdx];

        // Reset card draw state
        w.player1DrawnCards.clear();
        w.player2DrawnCards.clear();
        w.player1Abilities.clear();
        w.player2Abilities.clear();
        w.player1DrawIndex = 0;
        w.player2DrawIndex = 0;
        w.toastP1Text = ""; w.toastP1Timer = 0f;
        w.toastP2Text = ""; w.toastP2Timer = 0f;

        // One-shot debug: give the next AI ghost_ball then never again
        if (w.singlePlayer && w.debugNextAiGhostBall) {
            w.debugNextAiGhostBall = false;
            w.player2Abilities.put("ghost_ball", 3);
            w.player2DrawnCards.add("ghost_ball");
        }

        Gdx.app.log("Game", "Game state reset");
    }

    // ==================== SAVE / LOAD / DELETE (Preferences API) ====================

    /** Save index preferences name */
    private static final String SAVE_INDEX_PREF = "PongusSaveIndex";
    /** Prefix for individual save preferences */
    private static final String SAVE_PREFIX = "PongusSave_";

    /** Returns the list of existing save names from the index. */
    private String[] getSaveNames() {
        Preferences index = Gdx.app.getPreferences(SAVE_INDEX_PREF);
        String csv = index.getString("saves", "");
        if (csv.isEmpty()) return new String[0];
        return csv.split(",");
    }

    /** Adds a save name to the index (if not already present). */
    private void addSaveToIndex(String name) {
        Preferences index = Gdx.app.getPreferences(SAVE_INDEX_PREF);
        String csv = index.getString("saves", "");
        // Check if already present
        String[] existing = csv.isEmpty() ? new String[0] : csv.split(",");
        for (String s : existing) {
            if (s.equals(name)) return; // already in index
        }
        if (csv.isEmpty()) csv = name;
        else csv = csv + "," + name;
        index.putString("saves", csv);
        index.flush();
    }

    /** Removes a save name from the index. */
    private void removeSaveFromIndex(String name) {
        Preferences index = Gdx.app.getPreferences(SAVE_INDEX_PREF);
        String csv = index.getString("saves", "");
        if (csv.isEmpty()) return;
        String[] existing = csv.split(",");
        StringBuilder sb = new StringBuilder();
        for (String s : existing) {
            if (!s.equals(name)) {
                if (sb.length() > 0) sb.append(",");
                sb.append(s);
            }
        }
        index.putString("saves", sb.toString());
        index.flush();
    }

    /** Saves the current game state. Asks for a save name via w.dialog. */
    private void saveGame() {
        w.isPaused = true;
        w.dialog.showInput("Save Game", "Enter a name for this save:", null, new DialogSystem.InputCallback() {
            public void onResult(String result) {
                if (result == null || result.trim().isEmpty()) {
                    w.dialog.showMessage("Save Cancelled", "No name provided.", new DialogSystem.MessageCallback() {
                        public void onClose() { w.isPaused = false; }
                    });
                    return;
                }

                // Clean save name — only allow alphanumeric, underscore, dash
                String saveName = "";
                for (int i = 0; i < result.length(); i++) {
                    char ch = result.charAt(i);
                    if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                        (ch >= '0' && ch <= '9') || ch == '_' || ch == '-') {
                        saveName += ch;
                    } else {
                        saveName += '_';
                    }
                }
                if (saveName.isEmpty()) saveName = "save";

                Preferences prefs = Gdx.app.getPreferences(SAVE_PREFIX + saveName);

                // Basic state
                prefs.putInteger("w.scorePlayer1", w.scorePlayer1);
                prefs.putInteger("w.scorePlayer2", w.scorePlayer2);
                prefs.putInteger("w.level1", w.level1);
                prefs.putInteger("w.level2", w.level2);
                prefs.putBoolean("w.singlePlayer", w.singlePlayer);
                prefs.putInteger("w.aiDifficulty", w.aiDifficulty);

                // Player 1 abilities
                prefs.putInteger("p1AbilityCount", w.player1Abilities.size());
                int idx = 0;
                for (String ability : w.player1Abilities.keySet()) {
                    prefs.putString("p1a_" + idx + "_name", ability);
                    prefs.putInteger("p1a_" + idx + "_level", w.player1Abilities.get(ability));
                    idx++;
                }

                // Player 1 branches
                prefs.putInteger("p1BranchCount", w.player1AbilityBranches.size());
                idx = 0;
                for (String ability : w.player1AbilityBranches.keySet()) {
                    prefs.putString("p1b_" + idx + "_name", ability);
                    prefs.putInteger("p1b_" + idx + "_branch", w.player1AbilityBranches.get(ability));
                    idx++;
                }

                // Player 2 abilities
                prefs.putInteger("p2AbilityCount", w.player2Abilities.size());
                idx = 0;
                for (String ability : w.player2Abilities.keySet()) {
                    prefs.putString("p2a_" + idx + "_name", ability);
                    prefs.putInteger("p2a_" + idx + "_level", w.player2Abilities.get(ability));
                    idx++;
                }

                // Player 2 branches
                prefs.putInteger("p2BranchCount", w.player2AbilityBranches.size());
                idx = 0;
                for (String ability : w.player2AbilityBranches.keySet()) {
                    prefs.putString("p2b_" + idx + "_name", ability);
                    prefs.putInteger("p2b_" + idx + "_branch", w.player2AbilityBranches.get(ability));
                    idx++;
                }

                // Keybinds
                prefs.putInteger("w.player1UpKey", w.player1UpKey);
                prefs.putInteger("w.player1DownKey", w.player1DownKey);
                prefs.putInteger("w.player2UpKey", w.player2UpKey);
                prefs.putInteger("w.player2DownKey", w.player2DownKey);
                prefs.putInteger("w.player1AbilityKey", w.player1AbilityKey);
                prefs.putInteger("w.player2AbilityKey", w.player2AbilityKey);

                // Permanent bankai bonuses
                prefs.putInteger("p1BankaiStacks", w.player1BankaiStacks);
                prefs.putInteger("p2BankaiStacks", w.player2BankaiStacks);
                prefs.putInteger("p1PermSpeed", w.player1PermanentSpeedBonus);
                prefs.putInteger("p2PermSpeed", w.player2PermanentSpeedBonus);
                prefs.putInteger("p1PermPaddle", w.player1PermanentPaddleBonus);
                prefs.putInteger("p2PermPaddle", w.player2PermanentPaddleBonus);
                prefs.putFloat("p1BallSpeedMult", (float) w.player1BallSpeedMultiplier);
                prefs.putFloat("p2BallSpeedMult", (float) w.player2BallSpeedMultiplier);

                prefs.flush();
                addSaveToIndex(saveName);

                final String finalName = saveName;
                w.dialog.showMessage("Save Game", "Game saved as '" + finalName + "'!", new DialogSystem.MessageCallback() {
                    public void onClose() { w.isPaused = false; }
                });
            }
        });
    }

    /** Loads a previously saved game. Shows a dropdown of available saves. */
    private void loadGame() {
        w.isPaused = true;
        final String[] saveNames = getSaveNames();
        if (saveNames.length == 0) {
            w.dialog.showMessage("Load Game", "No save files found!", new DialogSystem.MessageCallback() {
                public void onClose() { w.isPaused = false; }
            });
            return;
        }

        w.dialog.showInput("Load Game", "Select a save to load:", saveNames, new DialogSystem.InputCallback() {
            public void onResult(String selectedSave) {
                if (selectedSave == null) {
                    w.isPaused = false;
                    return;
                }

                Preferences prefs = Gdx.app.getPreferences(SAVE_PREFIX + selectedSave);
                if (!prefs.contains("w.scorePlayer1")) {
                    w.dialog.showMessage("Load Error", "Save file is empty or corrupted!", new DialogSystem.MessageCallback() {
                        public void onClose() { w.isPaused = false; }
                    });
                    return;
                }

                // Clear existing abilities
                w.player1Abilities.clear();
                w.player1AbilityBranches.clear();
                w.player2Abilities.clear();
                w.player2AbilityBranches.clear();

                // Load basic state
                w.scorePlayer1 = prefs.getInteger("w.scorePlayer1", 0);
                w.scorePlayer2 = prefs.getInteger("w.scorePlayer2", 0);
                w.level1 = prefs.getInteger("w.level1", 1);
                w.level2 = prefs.getInteger("w.level2", 1);
                w.singlePlayer = prefs.getBoolean("w.singlePlayer", true);
                w.aiDifficulty = prefs.getInteger("w.aiDifficulty", 2);

                // Load Player 1 abilities
                int p1Count = prefs.getInteger("p1AbilityCount", 0);
                for (int i = 0; i < p1Count; i++) {
                    String name = prefs.getString("p1a_" + i + "_name", "");
                    int level = prefs.getInteger("p1a_" + i + "_level", 1);
                    if (!name.isEmpty()) w.player1Abilities.put(name, level);
                }

                // Load Player 1 branches
                int p1bCount = prefs.getInteger("p1BranchCount", 0);
                for (int i = 0; i < p1bCount; i++) {
                    String name = prefs.getString("p1b_" + i + "_name", "");
                    int branch = prefs.getInteger("p1b_" + i + "_branch", 0);
                    if (!name.isEmpty()) w.player1AbilityBranches.put(name, branch);
                }

                // Load Player 2 abilities
                int p2Count = prefs.getInteger("p2AbilityCount", 0);
                for (int i = 0; i < p2Count; i++) {
                    String name = prefs.getString("p2a_" + i + "_name", "");
                    int level = prefs.getInteger("p2a_" + i + "_level", 1);
                    if (!name.isEmpty()) w.player2Abilities.put(name, level);
                }

                // Load Player 2 branches
                int p2bCount = prefs.getInteger("p2BranchCount", 0);
                for (int i = 0; i < p2bCount; i++) {
                    String name = prefs.getString("p2b_" + i + "_name", "");
                    int branch = prefs.getInteger("p2b_" + i + "_branch", 0);
                    if (!name.isEmpty()) w.player2AbilityBranches.put(name, branch);
                }

                // Load keybinds
                w.player1UpKey = prefs.getInteger("w.player1UpKey", Input.Keys.W);
                w.player1DownKey = prefs.getInteger("w.player1DownKey", Input.Keys.S);
                w.player2UpKey = prefs.getInteger("w.player2UpKey", Input.Keys.UP);
                w.player2DownKey = prefs.getInteger("w.player2DownKey", Input.Keys.DOWN);
                w.player1AbilityKey = prefs.getInteger("w.player1AbilityKey", Input.Keys.Q);
                w.player2AbilityKey = prefs.getInteger("w.player2AbilityKey", Input.Keys.SLASH);

                // Load permanent bankai bonuses
                w.player1BankaiStacks = prefs.getInteger("p1BankaiStacks", 0);
                w.player2BankaiStacks = prefs.getInteger("p2BankaiStacks", 0);
                w.player1PermanentSpeedBonus = prefs.getInteger("p1PermSpeed", 0);
                w.player2PermanentSpeedBonus = prefs.getInteger("p2PermSpeed", 0);
                w.player1PermanentPaddleBonus = prefs.getInteger("p1PermPaddle", 0);
                w.player2PermanentPaddleBonus = prefs.getInteger("p2PermPaddle", 0);
                w.player1BallSpeedMultiplier = prefs.getFloat("p1BallSpeedMult", 1.0f);
                w.player2BallSpeedMultiplier = prefs.getFloat("p2BallSpeedMult", 1.0f);
                if (w.player1BankaiStacks >= 5) w.player1HasBankaiImmunity = true;
                if (w.player2BankaiStacks >= 5) w.player2HasBankaiImmunity = true;

                // Reset ball position
                w.ballX = 250; w.ballY = 150;
                w.ballVelX = 2; w.ballVelY = 2;
                w.paddle1Y = 100; w.paddle2Y = 100;

                // Clear projectiles and active effects
                w.bullets.clear(); w.explosions.clear(); w.activeLasers.clear();
                w.vegetaBullets.clear(); w.freezaLasers.clear(); w.jirenBullets.clear();
                w.getsugaProjectiles.clear(); w.powerUps.clear();
                w.gameStateHistory.clear();

                w.dialog.showMessage("Load Game", "Game '" + selectedSave + "' loaded!", new DialogSystem.MessageCallback() {
                    public void onClose() { w.isPaused = false; }
                });
            }
        });
    }

    /** Deletes a saved game after confirmation. */
    private void deleteGame() {
        w.isPaused = true;
        final String[] saveNames = getSaveNames();
        if (saveNames.length == 0) {
            w.dialog.showMessage("Delete Game", "No save files found!", new DialogSystem.MessageCallback() {
                public void onClose() { w.isPaused = false; }
            });
            return;
        }

        w.dialog.showInput("Delete Game", "Select a save to delete:", saveNames, new DialogSystem.InputCallback() {
            public void onResult(final String selectedSave) {
                if (selectedSave == null) {
                    w.isPaused = false;
                    return;
                }

                w.dialog.showConfirm("Confirm Delete",
                    "Are you sure you want to delete '" + selectedSave + "'?\nThis cannot be undone!",
                    new DialogSystem.ConfirmCallback() {
                        public void onResult(boolean yes) {
                            if (yes) {
                                // Clear the preferences for this save
                                Preferences prefs = Gdx.app.getPreferences(SAVE_PREFIX + selectedSave);
                                prefs.clear();
                                prefs.flush();
                                removeSaveFromIndex(selectedSave);

                                w.dialog.showMessage("Delete Game", "Save '" + selectedSave + "' deleted!", new DialogSystem.MessageCallback() {
                                    public void onClose() { w.isPaused = false; }
                                });
                            } else {
                                w.isPaused = false;
                            }
                        }
                    });
            }
        });
    }

    // ==================== DEBUG: DIALOG TEST (remove later) ====================

    private int testDialogStep = 0;

    /** Cycles through all w.dialog types for testing. Press T to start. */
    private void testDialogSystem() {
        switch (testDialogStep % 5) {
            case 0:
                w.dialog.showMessage("Test Message", "This is a test w.dialog!\nPongus game test.", new DialogSystem.MessageCallback() {
                    public void onClose() {
                        Gdx.app.log("Dialog", "Message closed");
                    }
                });
                break;
            case 1:
                w.dialog.showOptions("Choose Difficulty",
                    "Select the AI difficulty level:",
                    new String[]{"Normal", "Hard", "Impossible", "Learning AI"},
                    new DialogSystem.OptionCallback() {
                        public void onSelect(int index) {
                            Gdx.app.log("Dialog", "Option selected: " + index);
                        }
                    });
                break;
            case 2:
                w.dialog.showInput("Enter Save Name",
                    "Type a name for your save file:",
                    null,
                    new DialogSystem.InputCallback() {
                        public void onResult(String result) {
                            Gdx.app.log("Dialog", "Input result: " + result);
                        }
                    });
                break;
            case 3:
                w.dialog.showInput("Load Game",
                    "Select a save file to load:",
                    new String[]{"AutoSave - Level 3", "ManualSave - Level 7", "QuickSave - Level 12"},
                    new DialogSystem.InputCallback() {
                        public void onResult(String result) {
                            Gdx.app.log("Dialog", "Dropdown result: " + result);
                        }
                    });
                break;
            case 4:
                w.dialog.showConfirm("Delete Save",
                    "Are you sure you want to permanently delete this save file?",
                    new DialogSystem.ConfirmCallback() {
                        public void onResult(boolean yes) {
                            Gdx.app.log("Dialog", "Confirm result: " + yes);
                        }
                    });
                break;
        }
        testDialogStep++;
    }

    // ==================== LIFECYCLE ====================

    @Override
    public void pause() {
        // Called when the browser tab is hidden or the page is about to unload (HTML5).
        // If the player closes the tab mid online match, record a loss for them.
        if (w.isOnlineMatch && !w.showingMainMenu && w.matchResultPhase == 0 && w.profile != null) {
            w.profile.trophies = TrophySystem.calculateTrophiesAfterLoss(w.profile.trophies);
            w.profile.arenaIndex = TrophySystem.getArenaIndex(w.profile.trophies);
            w.profile.totalMatches++;
            String histEntry = "L|" + w.player2Name + "|" + TrophySystem.TROPHIES_PER_LOSS;
            w.profile.matchHistory.add(0, histEntry);
            if (w.profile.matchHistory.size() > 5) w.profile.matchHistory.remove(5);
        }
        if (w.profile != null) {
            try { w.profile.save(Gdx.app.getPreferences(PROFILE_PREF)); }
            catch (Exception e) { Gdx.app.log("Pongus", "pause save failed: " + e.getMessage()); }
        }
    }

    @Override
    public void resize(int width, int height) {
        w.viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        w.sr.dispose();
        w.batch.dispose();
        w.font.dispose();
        // Dispose sprite textures (Phase 9)
        if (w.texPaddle1 != null) w.texPaddle1.dispose();
        if (w.texPaddle2 != null) w.texPaddle2.dispose();
        if (w.texBall != null) w.texBall.dispose();
        if (w.texBallFire != null) w.texBallFire.dispose();
        for (com.badlogic.gdx.graphics.Texture t : w.texChests) { if (t != null) t.dispose(); }
        for (com.badlogic.gdx.graphics.Texture t : w.texArenas) { if (t != null) t.dispose(); }
        for (com.badlogic.gdx.graphics.Texture t : w.arenaBgTextures) { if (t != null) t.dispose(); }
        for (com.badlogic.gdx.graphics.Texture t : w.arenaIconTextures) { if (t != null) t.dispose(); }
        for (com.badlogic.gdx.graphics.Texture t : w.cardTextureMap.values()) { if (t != null) t.dispose(); }
        for (com.badlogic.gdx.graphics.Texture t : w.powerupTextures.values()) { if (t != null) t.dispose(); }
    }
}
