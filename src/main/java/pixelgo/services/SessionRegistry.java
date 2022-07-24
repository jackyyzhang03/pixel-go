package pixelgo.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Maps WebSocket sessions to games.
 * 
 * @author jacky
 *
 */
@Component
public class SessionRegistry {
	private Map<String, UUID> gameMap = new ConcurrentHashMap<>();
	
	public UUID getGameId(String sessionId) {
		return gameMap.get(sessionId);
	}

	public void removeSession(String sessionId) {
		gameMap.remove(sessionId);
	}
	
	public void setGameId(String sessionId, UUID gameId) {
		gameMap.put(sessionId, gameId);
	}

}
