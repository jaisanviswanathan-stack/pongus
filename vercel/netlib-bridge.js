import { Network } from 'https://cdn.jsdelivr.net/npm/@poki/netlib/dist/netlib.js';

window.PongusNet = {
  _net: null,
  _ready: false,
  _searching: false,
  _connected: false,   // true once a peer is connected — prevents double-join
  _isCreator: false,   // true if we called create(), false if we called join()
  _currentArena: -1,
  _pendingArena: -1,

  // Callbacks wired by GwtNetworkAdapter.init()
  _onReady: null,
  _onPeerConnected: null,
  _onMessage: null,
  _onDisconnected: null,

  init: function(gameId) {
    var self = this;
    this._net = new Network(gameId);

    this._net.on('ready', function() {
      self._ready = true;
      if (self._onReady) self._onReady();
      // If a search was queued before ready fired, kick it off now
      if (self._searching && self._pendingArena >= 0) {
        self._startSearch(self._pendingArena);
        self._pendingArena = -1;
      }
    });

    this._net.on('connected', function(peer) {
      if (self._connected) return; // already paired — ignore duplicate events
      self._connected = true;
      self._searching = false;

      // Tell Java which role this player has BEFORE signaling peer connected,
      // so w.isHost is correct when startOnlineMatch() runs.
      // H:1 = I am host (P1/creator), H:0 = I am guest (P2/joiner)
      var role = self._isCreator ? '1' : '0';
      if (self._onMessage) self._onMessage('H:' + role);
      if (self._onPeerConnected) self._onPeerConnected(peer.id);
    });

    this._net.on('message', function(peer, channel, data) {
      if (self._onMessage) self._onMessage(String(data));
    });

    this._net.on('disconnected', function(peer) {
      self._connected = false;
      if (self._onDisconnected) self._onDisconnected(peer.id);
    });

    // Lobby list event — fired when available lobbies are updated
    this._net.on('lobby', function(lobbies) {
      if (!self._searching || self._connected) return; // stop if already paired
      // Find an open lobby in the same arena tier
      var match = null;
      for (var i = 0; i < lobbies.length; i++) {
        var meta = lobbies[i].metadata;
        if (meta && meta.arena === self._currentArena) {
          match = lobbies[i];
          break;
        }
      }
      if (match) {
        // Join an existing game — we become the guest (P2)
        self._isCreator = false;
        self._net.join(match.id);
      }
      // No match: keep waiting in our own lobby (we're the host)
    });
  },

  findMatch: function(arenaIndex) {
    this._searching = true;
    this._connected = false;
    this._currentArena = arenaIndex;
    if (!this._ready) {
      this._pendingArena = arenaIndex;
      return;
    }
    this._startSearch(arenaIndex);
  },

  _startSearch: function(arenaIndex) {
    this._isCreator = true; // assume we're creating until a lobby event makes us join
    this._net.create({ arena: arenaIndex });
  },

  cancelSearch: function() {
    this._searching = false;
    this._connected = false;
    this._pendingArena = -1;
  },

  send: function(msg) {
    if (this._net && this._connected) this._net.broadcast('unreliable', msg);
  },
};
