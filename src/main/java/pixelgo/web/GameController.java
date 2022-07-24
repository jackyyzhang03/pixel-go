package pixelgo.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import pixelgo.dtos.ErrorResponse;
import pixelgo.dtos.GameCommand;
import pixelgo.dtos.NewGameResponse;
import pixelgo.exceptions.ClientException;
import pixelgo.exceptions.GameFullException;
import pixelgo.exceptions.GameNotFoundException;
import pixelgo.game.Player;
import pixelgo.services.GameService;
import pixelgo.services.SessionRegistry;

/**
 * Handles game-related STOMP messages.
 * 
 * @author jacky
 *
 */
@Controller
public class GameController {

	private static final Logger logger = LoggerFactory.getLogger(GameController.class);
	private final GameService gameService;
	private final SessionRegistry registry;

	public GameController(GameService gameService, SessionRegistry registry) {
		this.gameService = gameService;
		this.registry = registry;
	}

	@SubscribeMapping("/create")
	public NewGameResponse createGame(SimpMessageHeaderAccessor headerAccessor) throws GameFullException {
		String sessionId = headerAccessor.getSessionId();
		logger.info("Subscribe frame recieved at \"app/game/create\" from: {}", sessionId);
		UUID gameId = gameService.createGame(sessionId);
		registry.setGameId(sessionId, gameId);

		return new NewGameResponse(gameId, Player.BLACK);
	}

	@MessageMapping("/ready/{id}")
	public void getGameState(@DestinationVariable String id, SimpMessageHeaderAccessor headerAccessor)
			throws GameNotFoundException {
		UUID gameId = convertGameId(id);
		String sessionId = headerAccessor.getSessionId();
		logger.info("Message recieved at \"/app/ready/{}\" from: {}", gameId, sessionId);
		gameService.publishCurrentGameState(gameId);
	}

	@MessageExceptionHandler
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ErrorResponse handleGameException(ClientException e) throws Exception {
		String name = e.getClass().getSimpleName();
		String message = e.getMessage();
		logger.info("{} recived by exception handler with message: {}", name, e.getMessage());
		return new ErrorResponse(name, message);
	}

	@MessageExceptionHandler
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ErrorResponse handleServiceException(Exception e) {
		logger.error("An unhandled error occured with class: {}, message: {}", e.getClass().getName(), e.getMessage());
		e.printStackTrace();
		return new ErrorResponse("UnknownServerException", "An unknown server error occured");
	}

	@SubscribeMapping("/join/{id}")
	public NewGameResponse joinGame(@DestinationVariable String id, SimpMessageHeaderAccessor headerAccessor)
			throws GameFullException, GameNotFoundException {
		UUID gameId = convertGameId(id);
		String sessionId = headerAccessor.getSessionId();
		logger.info("Subscribe frame recieved at \"/game/join/{}\" from: {}", gameId, sessionId);
		Player player = gameService.addToGame(gameId, sessionId);
		registry.setGameId(sessionId, gameId);

		return new NewGameResponse(gameId, player);
	}

	@MessageMapping("/move")
	public void processInput(@Payload GameCommand command, SimpMessageHeaderAccessor headerAccessor)
			throws ClientException {
		String sessionId = headerAccessor.getSessionId(); // Websocket session id
		logger.info("Message recieved at \"/move\" from: {}", sessionId);

		UUID gameId = registry.getGameId(sessionId);

		gameService.processMove(command, gameId, sessionId);
	}

	private UUID convertGameId(String gameId) throws GameNotFoundException {
		try {
			return UUID.fromString(gameId);
		} catch (IllegalArgumentException e) {
			throw new GameNotFoundException("Invalid game id");
		}
	}
}
