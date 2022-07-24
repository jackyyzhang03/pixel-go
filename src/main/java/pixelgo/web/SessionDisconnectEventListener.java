package pixelgo.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import pixelgo.exceptions.GameNotFoundException;
import pixelgo.services.GameContext;
import pixelgo.services.GameRegistry;
import pixelgo.services.GameService;
import pixelgo.services.SessionRegistry;

/**
 * Cleans up after WebSocket disconnects.
 * 
 * @author jacky
 *
 */
@Component
public class SessionDisconnectEventListener {
	private static final Logger logger = LoggerFactory.getLogger(SessionDisconnectEventListener.class);
	private final GameRegistry gameRegistry;
	private final GameService gameService;
	private final SessionRegistry userRegistry;

	public SessionDisconnectEventListener(GameRegistry gameRegistry, SessionRegistry userRegistry,
			GameService gameService) {
		this.gameRegistry = gameRegistry;
		this.userRegistry = userRegistry;
		this.gameService = gameService;
	}

	@EventListener
	public void handleSessionDisconnect(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		logger.info("User {} disconnected", sessionId);
		UUID gameId = userRegistry.getGameId(sessionId);
		try {
			GameContext context = gameRegistry.getContext(gameId);
			context.removePlayer(sessionId);
			gameService.publishCurrentGameState(gameId);
		} catch (GameNotFoundException e) {
		}
		userRegistry.removeSession(sessionId);
	}
}
