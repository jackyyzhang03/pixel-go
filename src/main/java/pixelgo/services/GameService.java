package pixelgo.services;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import pixelgo.dtos.GameCommand;
import pixelgo.dtos.GameResult;
import pixelgo.dtos.GameState;
import pixelgo.exceptions.ClientOutOfSyncException;
import pixelgo.exceptions.GameFullException;
import pixelgo.exceptions.GameNotFoundException;
import pixelgo.exceptions.GameNotStartedException;
import pixelgo.exceptions.InvalidMoveException;
import pixelgo.exceptions.PlayerOutOfTurnException;
import pixelgo.game.Game;
import pixelgo.game.Move;
import pixelgo.game.Player;

@Service
public class GameService {
	private static final Logger logger = LoggerFactory.getLogger(GameService.class);
	private final GameRegistry gameRegistry;
	private final SimpMessagingTemplate messagingTemplate;

	public GameService(GameRegistry gameRegistry, SimpMessagingTemplate messagingTemplate) {
		this.gameRegistry = gameRegistry;
		this.messagingTemplate = messagingTemplate;
	}

	public Player addToGame(UUID gameId, String sessionId) throws GameFullException, GameNotFoundException {
		GameContext context = gameRegistry.getContext(gameId);
		if (context == null)
			throw new GameNotFoundException("No such game exists with id: " + gameId);
		logger.info("User [{}] added to game [{}]", sessionId);

		int setBlackResult = context.setBlackSessionId(sessionId);
		int setWhiteResult = context.setWhiteSessionId(sessionId);

		if (setBlackResult == 0 && setWhiteResult == 0) {
			throw new GameFullException("Game is already full");
		} else if (setBlackResult == 2 || setWhiteResult == 2) {
			publishCurrentGameState(gameId);
			logger.info("Game [{}] started", gameId);
		}

		if (setBlackResult != 0) {
			return Player.BLACK;
		}

		return Player.WHITE;
	}

	public UUID createGame(String sessionId) throws GameFullException {
		GameContext context = new GameContext();
		context.setBlackSessionId(sessionId);
		UUID gameId = gameRegistry.newContext(context);
		logger.info("New game created with id: " + gameId);
		return gameId;
	}

	public void processMove(GameCommand command, UUID gameId, String sessionId)
			throws GameNotStartedException, GameNotFoundException, ClientOutOfSyncException, InvalidMoveException, PlayerOutOfTurnException {
		GameContext context = gameRegistry.getContext(gameId);
		Game game = context.getGame();
		
		Player player = context.getPlayer(sessionId);

		if (!player.equals(game.getCurrentPlayer()) || command.moveNumber() != game.getMoveNumber()) {
			throw new ClientOutOfSyncException("User [" + sessionId + "] is out of sync with the server.");
		}

		Move move = new Move(player, command.vertex());
		game.executeMove(move);
		if (game.isHasEnded()) {
			messagingTemplate.convertAndSend("/topic/result/" + gameId, getGameResults(game));
		}

		publishCurrentGameState(gameId);
	}

	public void publishCurrentGameState(UUID gameId) throws GameNotFoundException {
		GameState state = getCurrentGameState(gameRegistry.getContext(gameId));
		messagingTemplate.convertAndSend("/topic/state/" + gameId, state);
		logger.info("New game state sent to \"/topic/state/{}\"", gameId);
	}

	private GameState getCurrentGameState(GameContext context) {
		Game game = context.getGame();
		int numPlayers = 0;

		if (context.getBlackSessionId() != null)
			numPlayers++;
		if (context.getWhiteSessionId() != null)
			numPlayers++;
		return new GameState(game.getBoard(), game.getMoveNumber(), game.getCurrentPlayer(), numPlayers, game.isConsecutivePass(), game.isRunning());
	}

	private GameResult getGameResults(Game game) {
		int[] points = game.getPoints();
		int blackPoints = points[0];
		int whitePoints = points[1];

		return new GameResult(blackPoints, whitePoints);
	}
}
