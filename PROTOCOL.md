Quake 1D communication protocol
===============================

Transport
---------
The transport protocol used is line-based TCP on port 1996. The server accepts UNIX-style line endings (\n) and windows-style line endings (\r\n). When reading from the socket, make sure that you don't limit the size of your lines, as some packages (e.g. the transmission of the gamstate) may end up rather large.

Format
------
All communication between the server and the clients is done via JSON (Description and a wide range of implementations at http://www.json.org/ ).

Errors
------
Error messages sent by the server are meant for human- not machine readability. The only guarantees made about them is that they are the only messages by the server that are not valid JSON.

Handshake
---------
A connecting AI must send a handshake within 10 seconds of connecting, otherwise the connection is dropped. It has the following form:
```json
{"message":"connect",
 "color":int
}
```
color is the decimal representation of your desired player color. You might write it internally as 0x12ad85 but json only supports decimal numbers.

To connect as an observer, send:
```json
{"message":"connect"}
```

Note that your connection will be rejected if the color is too close to that of an already connected player.

If the handshake is successful, the server will respond with
```json
{"message":"connect",
 "status":true
}
```

Gamestate
---------
Due to the compact nature of the game, the map is always transmitted in its entirety every turn.

```json
{"message":"gamestate",
 "width":int,
 "offset":int,
 "items":[PLAYER, PLAYER, ..., BULLET, BULLET]
}
```

```json
{"message":"player",
 "color":int,
 "x":int,
 "facingRight":boolean,
 "health":int,
 "score":int
}
```

```json
{"message":"bullet",
 "x":int,
 "shooter":int //color of the shooter,
 "facingRight":boolean
}
```

Gameover
--------
This message indicates that the round is finished.

```json
{"message":"gameover",
 "players":[REDUCEDPLAYER, ...]
}
```

The elements of players are sorted by score from highest to lowest and take the following form:
```json
{"color":int,
 "score":int
}
```

Player action
-------------
To take an action on your next turn, send

```json
{"message":"action",
 "type":"MOVE/TURN/SHOOT/JUMP/IDLE"
}
```
