# Custom Keybinds Feature - Complete Overview

## 🎮 What You Can Do Now

✅ **Host (Player 1)** can customize their keyboard controls before the game starts
✅ **Guest (Player 2)** can customize their keyboard controls independently
✅ **Real-time sync** between players before game starts
✅ **Easy visual interface** - click field, press key, done
✅ **Reset button** for each keybind to go back to defaults
✅ **Default presets** based on player position (WASD for P1, Arrows for P2)

---

## 🚀 How to Use

### For the Host (Player 1)

```
1. Click "Host Game"
   ↓
2. See keybind configuration section with:
   • Move Up: W
   • Move Down: S
   ↓
3. To change a key:
   • Click on the key field
   • Field turns yellow
   • Press any key on your keyboard
   • Field saves and returns to normal
   ↓
4. To reset to defaults:
   • Click the "Reset" button next to the field
   ↓
5. Share link with Player 2
   ↓
6. Wait for Player 2 to join and configure their keys
   ↓
7. Click "Start Game"
   ↓
8. Play with your custom keybinds!
```

### For the Guest (Player 2)

```
1. Open the link from Player 1
   ↓
2. Automatically see keybind configuration:
   • Move Up: ↑ (Up Arrow)
   • Move Down: ↓ (Down Arrow)
   ↓
3. Customize the same way as Player 1:
   • Click field → Press key → Saved
   • Click Reset → Back to default
   ↓
4. Wait for Player 1 to click "Start Game"
   ↓
5. Game begins automatically
   ↓
6. Play with your custom keybinds!
```

---

## 🎯 Key Features

### Easy to Use
- **Click to customize** - No complicated menus
- **Visual feedback** - Field turns yellow while waiting for key press
- **Instant save** - Key is saved immediately after pressing
- **Clear labels** - Understand what each binding does

### Flexible Keybinds
- **Any key works** - Letters, arrows, space, modifiers, etc.
- **Custom combinations** - Both players can use different keys
- **No restrictions** - Same key can be used by both players
- **Easy reset** - One click to go back to defaults

### Real-time Sync
- **Instant relay** - Changes sent to other player immediately
- **Before game only** - Keybinds locked once game starts
- **No latency issues** - WebSocket ensures fast delivery

---

## 📋 Default Keybinds

### Player 1 (Host) Defaults
```
Move Up:   W
Move Down: S
```

### Player 2 (Guest) Defaults
```
Move Up:   ↑ (Up Arrow)
Move Down: ↓ (Down Arrow)
```

**Reason:** Classic arcade two-player layout
- Left side: WASD controls
- Right side: Arrow keys

---

## 🎨 User Interface

### Host Keybind Section (on Host Menu)
```
┌─────────────────────────────────────┐
│ ⌨️ Your Controls (Player 1)          │
├─────────────────────────────────────┤
│ Move Up:    [W]              [Reset] │
│ Move Down:  [S]              [Reset] │
├─────────────────────────────────────┤
│ ⚙️ Ready                              │
└─────────────────────────────────────┘
```

### Guest Keybind Panel (full screen)
```
┌─────────────────────────────────────┐
│         Configure Your Controls      │
├─────────────────────────────────────┤
│ Welcome Player 2!                    │
│ The host is waiting to start the     │
│ game. Customize your controls below: │
├─────────────────────────────────────┤
│ ⌨️ Your Controls (Player 2)          │
│                                     │
│ Move Up:    [↑]              [Reset] │
│ Move Down:  [↓]              [Reset] │
├─────────────────────────────────────┤
│ ✓ Ready                              │
│                                     │
│ ✓ Waiting for Host to Start Game     │
│   You can change your controls      │
│   anytime before the game starts.   │
├─────────────────────────────────────┤
│ [Leave Game]                         │
└─────────────────────────────────────┘
```

---

## 💡 Usage Tips

### Popular Key Combinations
**WASD Layout (Left Hand)**
- Up: W
- Down: S
- Alternative: Up: Q, Down: A

**Arrow Layout (Right Hand)**
- Up: ↑
- Down: ↓
- Alternative: Up: E, Down: D

**Gamers Layout**
- Up: W
- Down: S
- *Same as WASD*

**Space + Arrows**
- Up: ↑
- Down: Space
- *One-handed control*

### Best Practices
✓ Choose keys that are comfortable for your hand position
✓ Avoid system keys (some keys may be blocked by OS)
✓ Test your keys work before game starts
✓ Make sure you remember your chosen keys
✓ Keys reset each game - customize fresh each time

### What Works
✓ All letter keys (A-Z)
✓ Number keys (0-9)
✓ Arrow keys (↑ ↓ ← →)
✓ Spacebar
✓ Enter key
✓ Shift, Ctrl, Alt (as standalone, not combos)

### What Doesn't Work
✗ Escape key (reserved for browser)
✗ Function keys (F1-F12, may be reserved)
✗ Key combinations (only single keys)
✗ Mouse buttons
✗ Gamepad buttons

---

## 🔄 Game Flow

```
CLASSIC FLOW:
Host → Join → Play

NEW FLOW WITH CUSTOM KEYBINDS:
Host Game
   ↓
Configure Keybinds (W, S)
   ↓
Share Link
   ↓
Guest Joins
   ↓
Guest Configures Keybinds (↑, ↓)
   ↓
Start Game
   ↓
Play with Custom Keybinds!
```

---

## 🛠️ Technical Details

### How Keybinds Are Stored
```
playerKeybinds = {
    upKey: 'KeyW',      // Your up key code
    downKey: 'KeyS'     // Your down key code
}
```

### How Input Works
```javascript
// When you press a key, the game checks:
if (keys[playerKeybinds.upKey]) {
    // You pressed your "up" key
    movePaddleUp();
}

if (keys[playerKeybinds.downKey]) {
    // You pressed your "down" key
    movePaddleDown();
}
```

### How Sync Works
```
Player 1 changes key → Sends to Server
Server receives → Relays to Player 2
Player 2 receives → Updates display
                ↓
Both know each other's keybinds
```

---

## ❓ FAQ

**Q: Can I change keybinds during the game?**
A: No, they're locked once the game starts. Change them before clicking "Start Game".

**Q: What if Player 2 doesn't configure keybinds?**
A: They automatically use defaults (↑ and ↓). No setup required.

**Q: Can both players use the same key?**
A: Yes, it's allowed. But it might be confusing! Better to choose different keys.

**Q: Are keybinds saved between games?**
A: No, they reset each game. You customize fresh every time.

**Q: Can I use Escape to exit the game?**
A: Escape is reserved for the browser. Use the "Exit Game" button instead.

**Q: What if a key doesn't work?**
A: Some keys may be blocked by your system or browser. Try a different key.

**Q: Can I bind multiple keys to the same action?**
A: No, only one key per action. But you can use Space, Shift, or other modifier keys.

**Q: Do keybinds work on mobile?**
A: On-screen keyboards might not have all keys. Tablet keyboards should work fully.

---

## 📚 Documentation

For more detailed information, see:
- **KEYBINDS.md** - Complete technical documentation
- **KEYBINDS_QUICK_START.md** - User-friendly quick start
- **ARCHITECTURE.md** - System architecture and data flow
- **CHANGES_SUMMARY.md** - All changes made to implement this feature

---

## 🎯 Summary

This feature adds full custom keybind support to your online Pong game:

✅ **Host** controls Player 1 (left side)
✅ **Guest** controls Player 2 (right side)
✅ **Each player** has independent keybind configuration
✅ **Easy interface** - click field, press key, done
✅ **Real-time sync** between players before game starts
✅ **No setup required** - sensible defaults for each player
✅ **Fully flexible** - supports any keyboard key

Now you and your friends can play with YOUR preferred controls!

---

## 🎮 Ready to Play?

1. Open http://localhost:3000 (or your deployed URL)
2. Click "Host Game" or "Join Game"
3. Configure your keybinds (or use defaults)
4. Start the game
5. Play with your custom controls!

Enjoy! 🎉
