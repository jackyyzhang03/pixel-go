package pixelgo.services;

import java.util.concurrent.atomic.AtomicReference;

import pixelgo.game.Game;
import pixelgo.game.Player;

public class GameContext {
	private final AtomicReference<String> blackSessionId = new AtomicReference<>();
	private final Game game = new Game(19);

	private final AtomicReference<String> whiteSessionId = new AtomicReference<>();

	public String getBlackSessionId() {
		return blackSessionId.get();
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer(String sessionId) {
		if (sessionId.equals(blackSessionId.get())) {
			return Player.BLACK;
		} else if (sessionId.equals(whiteSessionId.get())) {
			return Player.WHITE;
		}

		return null;
	}

	public String getWhiteSessionId() {
		return whiteSessionId.get();
	}

	public void removePlayer(String sessionId) {
		if (blackSessionId.compareAndSet(sessionId, null) || whiteSessionId.compareAndSet(sessionId, null)) {
			game.stopGame();
		}
	}

	public int setBlackSessionId(String sessionId) {
		if (!blackSessionId.compareAndSet(null, sessionId)) {
			return 0;
		} else if (whiteSessionId.get() != null) {
			game.startGame();
			return 2;
		}

		return 1;
	}


	public int setWhiteSessionId(String sessionId) {
		if (!whiteSessionId.compareAndSet(null, sessionId)) {
			return 0;
		} else if (blackSessionId.get() != null) {
			game.startGame();
			return 2;
		}

		return 1;
	}
}
