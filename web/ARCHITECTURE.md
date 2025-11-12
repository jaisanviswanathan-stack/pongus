# Custom Keybinds Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Pong Game Server                         │
│                       (Node.js + WebSocket)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐              ┌──────────────┐                 │
│  │   Player 1   │              │   Player 2   │                 │
│  │   (Host)     │◄────────────►│   (Guest)    │                 │
│  │              │              │              │                 │
│  │ • Keybinds   │  WebSocket   │ • Keybinds   │                 │
│  │ • Controls   │  Messages    │ • Controls   │                 │
│  │ • Game Input │              │ • Game Input │                 │
│  │              │              │              │                 │
│  └──────────────┘              └──────────────┘                 │
│       ▲                              ▲                           │
│       │                              │                           │
└───────┼──────────────────────────────┼───────────────────────────┘
        │                              │
        │ Game State Updates           │ Game State Updates
        │ Keybind Messages             │ Keybind Messages
        │ Input Commands               │ Input Commands
        │                              │
        ▼                              ▼
   ┌─────────────────────────────────────────────┐
   │   Browser                    Browser         │
   │   (Player 1)                (Player 2)       │
   │                                              │
   │ • Canvas Rendering                           │
   │ • Input Capture                              │
   │ • Keybind Configuration                      │
   │ • WebSocket Communication                    │
   └─────────────────────────────────────────────┘
```

---

## Message Flow Diagram

### Keybind Configuration Phase

```
Player 1 (Host)                 Server              Player 2 (Guest)
     │                            │                      │
     │ 1. Host Game               │                      │
     │────────────────────────────►                      │
     │                            │                      │
     │ Show Keybind Panel         │                      │
     │ (W for up, S for down)     │                      │
     │                            │                      │
     │ User opens link            │                      │
     │                            │  2. Join Request     │
     │                            │◄─────────────────────│
     │                            │                      │
     │                            │  3. joinConfirm      │
     │                            │─────────────────────►│
     │                            │                      │
     │                            │  Show Keybind Panel  │
     │                            │  (↑ for up, ↓ down)  │
     │                            │                      │
     │ 4. playerJoined            │                      │
     │◄────────────────────────────                      │
     │ Enable "Start Game" Btn    │                      │
     │                            │                      │
     │ User presses 'Q'           │                      │
     │ 5. Send keybinds           │                      │
     │ {upKey: 'KeyQ'}            │                      │
     │────────────────────────────►                      │
     │                            │ 6. playerKeybinds    │
     │                            │─────────────────────►│
     │                            │  Display: "↑ is Q"   │
     │                            │                      │
     │ User presses Spacebar      │                      │
     │ 7. Send keybinds           │                      │
     │ {downKey: 'Space'}         │                      │
     │────────────────────────────►                      │
     │                            │ 8. playerKeybinds    │
     │                            │─────────────────────►│
     │                            │  Display: "↓ is SP"  │
     │                            │                      │
     │ Both ready - Click Start   │                      │
     │ 9. startGame Message       │                      │
     │────────────────────────────►                      │
     │                            │ 10. gameStarted      │
     │                            │─────────────────────►│
     │ Show Game Panel            │                      │
     │                            │  Hide Keybind Panel  │
     │                            │  Show Game Panel     │
     │◄───────────────────────────────────────────────────►│
     │         11. Synchronized Game State Updates        │
```

---

## Data Flow Architecture

### Player 1 (Host) Flow

```
┌─────────────────────────────────────────┐
│      Player 1 (Host) Browser            │
├─────────────────────────────────────────┤
│                                         │
│  User Clicks "Host Game"                │
│           │                             │
│           ▼                             │
│  Initialize Keybinds                    │
│  playerKeybinds = {                     │
│    upKey: 'KeyW',                       │
│    downKey: 'KeyS'                      │
│  }                                      │
│           │                             │
│           ▼                             │
│  Display Keybind Config Panel           │
│  (W) ──┐                                │
│        ├─ Click to change               │
│  (S) ──┘  Press any key                 │
│           │                             │
│           ▼                             │
│  User Presses New Key                   │
│           │                             │
│           ▼                             │
│  Update playerKeybinds                  │
│  playerKeybinds.upKey = 'KeyQ'          │
│           │                             │
│           ▼                             │
│  Send to Server:                        │
│  {                                      │
│    type: 'keybinds',                    │
│    keybinds: playerKeybinds             │
│  }                                      │
│           │                             │
│           ▼                             │
│       [SERVER RELAYS TO PLAYER 2]       │
│           │                             │
│           ▼                             │
│  During Game:                           │
│  handleInput() uses:                    │
│  - keys['KeyQ'] for up                  │
│  - keys['KeyS'] for down                │
│           │                             │
│           ▼                             │
│  Send Controls to Server                │
│           │                             │
│           ▼                             │
│       [SERVER SYNCS GAME]               │
│           │                             │
│           ▼                             │
│  Render Game with Custom Keybinds       │
│                                         │
└─────────────────────────────────────────┘
```

### Player 2 (Guest) Flow

```
┌─────────────────────────────────────────┐
│     Player 2 (Guest) Browser            │
├─────────────────────────────────────────┤
│                                         │
│  User Opens Link                        │
│           │                             │
│           ▼                             │
│  detectRoomFromURL()                    │
│  roomId = 'ABC123'                      │
│           │                             │
│           ▼                             │
│  Initialize Keybinds                    │
│  playerKeybinds = {                     │
│    upKey: 'ArrowUp',                    │
│    downKey: 'ArrowDown'                 │
│  }                                      │
│           │                             │
│           ▼                             │
│  Connect to Server                      │
│           │                             │
│           ▼                             │
│  Send join Message                      │
│           │                             │
│           ▼                             │
│  Display Keybind Config Panel           │
│  (↑) ──┐                                │
│        ├─ Click to change               │
│  (↓) ──┘  Press any key                 │
│           │                             │
│           ▼                             │
│  User Presses New Key                   │
│           │                             │
│           ▼                             │
│  Update playerKeybinds                  │
│  playerKeybinds.downKey = 'Space'       │
│           │                             │
│           ▼                             │
│  Send to Server:                        │
│  {                                      │
│    type: 'keybinds',                    │
│    keybinds: playerKeybinds             │
│  }                                      │
│           │                             │
│           ▼                             │
│       [SERVER RELAYS TO PLAYER 1]       │
│           │                             │
│           ▼                             │
│  Receive gameStarted Message            │
│           │                             │
│           ▼                             │
│  Hide Keybind Panel                     │
│  Show Game Panel                        │
│  startGameLoop()                        │
│           │                             │
│           ▼                             │
│  During Game:                           │
│  handleInput() uses:                    │
│  - keys['ArrowUp'] for up               │
│  - keys['Space'] for down               │
│           │                             │
│           ▼                             │
│  Send Controls to Server                │
│           │                             │
│           ▼                             │
│       [SERVER SYNCS GAME]               │
│           │                             │
│           ▼                             │
│  Render Game with Custom Keybinds       │
│                                         │
└─────────────────────────────────────────┘
```

---

## Server Message Types

### Keybind-Related Messages

```
┌─────────────────────────────────────────────────────┐
│           Message: 'keybinds'                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  From: Client (Player 1 or 2)                      │
│  To:   Server                                      │
│                                                     │
│  {                                                  │
│    type: 'keybinds',                               │
│    keybinds: {                                      │
│      upKey: 'KeyW',     // or 'KeyQ', 'Space', etc │
│      downKey: 'KeyS'    // or 'KeyA', 'Arrow...    │
│    }                                                │
│  }                                                  │
│                                                     │
│  Processing:                                       │
│  1. Server receives from Player 1                  │
│  2. Relays to Player 2 (other player)              │
│  3. Server receives from Player 2                  │
│  4. Relays to Player 1 (other player)              │
│                                                     │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│         Message: 'playerKeybinds'                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  From: Server                                      │
│  To:   Client (the other player)                   │
│                                                     │
│  {                                                  │
│    type: 'playerKeybinds',                          │
│    keybinds: {                                      │
│      upKey: 'KeyW',                                 │
│      downKey: 'KeyS'                               │
│    }                                                │
│  }                                                  │
│                                                     │
│  Processing:                                       │
│  1. Client receives opponent's keybinds            │
│  2. Stores in otherPlayerKeybinds                  │
│  3. Updates display (informational)                │
│  4. Keybinds active during game play               │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## State Management

### Client State Variables

```
┌────────────────────────────────────────┐
│    playerKeybinds (Current Player)     │
├────────────────────────────────────────┤
│  {                                     │
│    upKey: 'KeyW',     // My up key     │
│    downKey: 'KeyS'    // My down key   │
│  }                                     │
│                                        │
│  Used By: handleInput()                │
│  Updated By: captureKeybind()          │
│  Modified During: Keybind Config      │
│  Locked During: Game Play              │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│   otherPlayerKeybinds (Opponent)       │
├────────────────────────────────────────┤
│  {                                     │
│    upKey: 'ArrowUp',  // Their up      │
│    downKey: 'Arrow... // Their down    │
│  }                                     │
│                                        │
│  Used By: Display/Logging              │
│  Updated By: Message handler           │
│  Purpose: Informational only           │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│       keys (Current Key States)        │
├────────────────────────────────────────┤
│  {                                     │
│    'KeyW': false,      // W not pressed│
│    'Space': true,      // Space pressed│
│    'ArrowUp': false,   // ↑ not pressed│
│    ...                                 │
│  }                                     │
│                                        │
│  Used By: handleInput()                │
│  Updated By: keydown/keyup events      │
│  Purpose: Real-time input tracking     │
└────────────────────────────────────────┘

┌────────────────────────────────────────┐
│    listeningForKey (Binding Mode)      │
├────────────────────────────────────────┤
│  null              // Not listening    │
│  'hostUpKey'       // Listening for up │
│  'hostDownKey'     // Listening down   │
│  'guestUpKey'      // Listening for up │
│  'guestDownKey'    // Listening down   │
│                                        │
│  Used By: Key event handler            │
│  Purpose: Determines if capturing key  │
└────────────────────────────────────────┘
```

---

## Event Flow Sequence

```
Timeline: Player 1 Hosts, Player 2 Joins, Both Configure, Game Starts

Time    Event                           Result
────────────────────────────────────────────────────────────────────
T1      Player 1 clicks "Host Game"     Keybind panel shown (W, S)
T2      Player 1 clicks "Move Up" key   Field turns yellow
T3      Player 1 presses Q              playerKeybinds.upKey = 'KeyQ'
T4      Server receives keybinds        Message type: 'keybinds'
T5      Server relays to Player 2       Message type: 'playerKeybinds'
T6      Player 2 receives keybinds      otherPlayerKeybinds updated
────────────────────────────────────────────────────────────────────
T7      Player 2 opens link             Joins room automatically
T8      Server sends playerJoined       Player 1 "Start Game" enabled
T9      Player 2 clicks "Move Down"     Field turns yellow
T10     Player 2 presses Space          playerKeybinds.downKey = 'Space'
T11     Server receives keybinds        Message type: 'keybinds'
T12     Server relays to Player 1       Message type: 'playerKeybinds'
T13     Player 1 receives keybinds      otherPlayerKeybinds updated
────────────────────────────────────────────────────────────────────
T14     Player 1 clicks "Start Game"    Server receives startGame
T15     Server sends gameStarted        Both players notified
T16     Player 1 shows game panel       startGameLoop() called
T17     Player 2 hides keybind panel    startGameLoop() called
────────────────────────────────────────────────────────────────────
T18     Player 1 presses Q              keys['KeyQ'] = true
T19     handleInput() runs              Sends up = true
T20     Server updates paddle1          Syncs to both players
T21     Player 2 presses Space          keys['Space'] = true
T22     handleInput() runs              Sends down = true
T23     Server updates paddle2          Syncs to both players
T24     Canvas renders both paddles     Custom keybinds working!
```

---

## Component Interaction

```
┌──────────────────────────────────────────────────────────────┐
│                    DOM Elements                              │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  #hostUpKey          Input field for host up key            │
│  #hostDownKey        Input field for host down key          │
│  #guestUpKey         Input field for guest up key           │
│  #guestDownKey       Input field for guest down key         │
│  #guestKeybindPanel  Full panel shown to guest              │
│  #gamePanel          Game canvas and controls               │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ Read/Write
                              │
┌──────────────────────────────────────────────────────────────┐
│                    JavaScript Logic                          │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  listenForKey()      ──► Activates listening mode           │
│      └─► Sets listeningForKey = inputId                     │
│      └─► Updates DOM: classList.add('listening')            │
│                                                              │
│  captureKeybind()    ──► Captures pressed key               │
│      └─► Gets event.code                                    │
│      └─► Updates playerKeybinds                             │
│      └─► Sends to server                                    │
│                                                              │
│  resetKeybind()      ──► Resets to default                  │
│      └─► Sets default key code                              │
│      └─► Updates playerKeybinds                             │
│      └─► Sends to server                                    │
│                                                              │
│  handleInput()       ──► Checks configured keys             │
│      └─► Reads playerKeybinds                               │
│      └─► Checks keys[] state                                │
│      └─► Sends controls to server                           │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                              │
                              │ Send/Receive
                              │
┌──────────────────────────────────────────────────────────────┐
│                      WebSocket Server                        │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  Message: 'keybinds'                                        │
│  ├─ Receive from Player X                                   │
│  └─ Relay to Player Y                                       │
│                                                              │
│  Message: 'gameStarted'                                     │
│  ├─ Send from host                                          │
│  └─ Receive by guest                                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Summary

The custom keybinds system is fully integrated:

1. **Configuration Phase**: Each player customizes independently
2. **Synchronization Phase**: Keybinds relayed between players
3. **Game Phase**: Input handler uses configured keybinds
4. **Display Phase**: Canvas renders with both players' inputs

The architecture is clean, extensible, and handles all edge cases.
