# Custom Keybinds Feature

## Overview

Both players can now customize their keyboard controls before the game starts!

## How It Works

### Host (Player 1)
1. Clicks "Host Game"
2. Sees keybind configuration section with **Move Up** and **Move Down**
3. Default binds: **W** for up, **S** for down
4. Can click on any keybind field to change it
5. Field turns yellow when listening for input
6. Press any key to bind it
7. Click "Reset" to go back to defaults
8. Waits for Player 2 to join

### Guest (Player 2)
1. Opens link from host
2. Joins the room
3. Automatically shown keybind configuration screen
4. Default binds: **↑ (Up Arrow)** for up, **↓ (Down Arrow)** for down
5. Can customize controls same as host
6. Once host clicks "Start Game", game begins with custom keybinds

## Features

### Key Capture
- Click on any keybind field to listen for a new key
- Field shows "... press a key ..." while waiting
- Field turns yellow/gold background during listening
- Any key press (except escape) is captured
- Keybind is instantly synced to server

### Reset Button
- Click "Reset" to go back to default keybind for that action
- Doesn't require clicking the field first
- Changes are immediately synced

### Supported Keys
- Letters: W, A, S, D, Q, E, R, T, F, G, etc.
- Arrows: ↑ (Up), ↓ (Down), ← (Left), → (Right)
- Special: Space, Enter, Shift, Ctrl, Alt
- Any other physical key on the keyboard

### Key Display Format
- Keyboard keys show as letters: "W", "S", "Q"
- Arrow keys show as symbols: "↑", "↓", "←", "→"
- Special keys show as text: "Space", "Enter", "Shift"

## Game Flow

```
Player 1 (Host)                    Player 2 (Guest)
   |                                     |
   v                                     |
Host Game                                |
   |                                     |
   |---> Customize Keybinds <-----------/
   |     (Player 1 - W/S default)   Join Game
   |                                     |
   |     Waiting for Player 2...         v
   |     Player 2 Customizes Keybinds
   |     (Player 2 - Arrows default)     |
   |<-------- Keybinds Synced -----------/
   |
   v
 [Start Game Button Enabled]
   |
   v
 Click "Start Game"
   |---> Game Starts with Custom Keybinds
   |     Both players use their chosen keys
```

## Technical Details

### Frontend (game.js)
- `listenForKey(inputId)` - Activate listening mode for a keybind
- `captureKeybind(event)` - Capture pressed key and store it
- `getKeyDisplay(keyCode)` - Convert key code to display format
- `resetKeybind(inputId, defaultKeyCode)` - Reset to default
- `playerKeybinds` - Stores current player's keybinds
- `otherPlayerKeybinds` - Stores other player's keybinds
- `handleInput()` - Uses custom keybinds for game input

### Backend (server.js)
- `if (message.type === 'keybinds')` - Receives keybind changes
- Broadcasts to other player only (not back to sender)
- No storage needed (per-game only)

### Message Format
```javascript
// Player sends keybind change:
{
    type: 'keybinds',
    keybinds: {
        upKey: 'KeyW',      // Key code
        downKey: 'KeyS'     // Key code
    }
}

// Server broadcasts to other player:
{
    type: 'playerKeybinds',
    keybinds: {
        upKey: 'KeyW',
        downKey: 'KeyS'
    }
}
```

## Default Keybinds

### Player 1 (Host)
- Move Up: **W** (KeyW)
- Move Down: **S** (KeyS)

### Player 2 (Guest)
- Move Up: **↑** (ArrowUp)
- Move Down: **↓** (ArrowDown)

Reason: WASD is common for left side, Arrows for right side

## UI Components

### Host Keybind Section
- Located on host menu screen
- Title: "⌨️ Your Controls (Player 1)"
- Two keybind rows (Move Up, Move Down)
- Reset buttons for each keybind
- Status indicator: "⚙️ Ready"

### Guest Keybind Panel
- Full screen panel shown when guest joins
- Title: "⌨️ Your Controls (Player 2)"
- Two keybind rows (Move Up, Move Down)
- Reset buttons for each keybind
- Status indicator: "✓ Ready"
- Shows waiting message for host to start

## Styling

### Keybind Input States
- **Default**: Gray background, normal text
- **Focus**: Blue border, white background
- **Listening**: Yellow/gold background, bold text, yellow border
- **Disabled**: Not applicable (always editable)

### Status Indicators
- Host: Gray box with settings icon "⚙️"
- Guest: Green box with checkmark "✓"
- Both indicate player is ready with controls configured

## Examples

### Changing a Keybind
1. Player 1 clicks on "Move Up" field
2. Field turns yellow, shows "... press a key ..."
3. Player presses "Q"
4. Field shows "Q", changes back to normal
5. Server notifies Player 2 of change
6. Player 2 sees: "Opponent moved Up key to Q"

### Using Custom Keybinds During Game
1. Player 1 uses Q to move up (was W)
2. Player 2 uses Spacebar for down (was Down Arrow)
3. Both players control their paddles with custom keys
4. Server receives input with configured keybinds

## Known Limitations

- Keybinds are per-session only (not saved)
- No duplicate key prevention (both players can use same key)
- Escape key cannot be rebound (reserved for browser)
- Game must not be started to change keybinds

## Future Enhancements

- Prevent duplicate keybinds between players
- Save keybinds to browser localStorage
- Show opponent's keybinds on screen
- Add more bindable actions (abilities, power-ups)
- Gamepad/controller support
