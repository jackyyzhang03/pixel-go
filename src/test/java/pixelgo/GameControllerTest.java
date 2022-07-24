package pixelgo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import pixelgo.dtos.ErrorResponse;
import pixelgo.dtos.GameCommand;
import pixelgo.dtos.GameState;
import pixelgo.dtos.NewGameResponse;
import pixelgo.exceptions.GameNotFoundException;
import pixelgo.game.Board;
import pixelgo.game.Game;
import pixelgo.game.Player;
import pixelgo.services.GameContext;
import pixelgo.services.GameRegistry;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameControllerTest {
	private static class SubscriptionHandler implements StompFrameHandler {

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return null;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
		}
		
	}
	private class CreateGameStompFrameHandler implements StompFrameHandler {

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return NewGameResponse.class;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			newGameResponses.add((NewGameResponse) payload);
		}
	}

	private class GameStompFrameHandler implements StompFrameHandler {

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return GameState.class;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			gameStates.add((GameState) payload);
		}
	}

	private class ErrorStompFrameHandler implements StompFrameHandler {

		@Override
		public Type getPayloadType(StompHeaders headers) {
			return ErrorResponse.class;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			errors.add((ErrorResponse) payload);
		}
	}

	private static final String NEW_GAME_ENDPOINT = "/app/create";
	private static final String SET_GAME_ENDPOINT = "/app/join";
	private static final String READY_PLAYER_ENDPOINT = "/app/ready";
	private static final String MOVE_ENDPOINT = "/app/move";
	private static final String GAME_ENDPOINT = "/topic/state";
	private static final String ERROR_ENDPOINT = "/user/queue/errors";

	@LocalServerPort
	private int port;

	private String URL;

	private GameRegistry registry;

	private final ArrayBlockingQueue<NewGameResponse> newGameResponses;
	private final ArrayBlockingQueue<ErrorResponse> errors;
	private final ArrayBlockingQueue<GameState> gameStates;
	
	

	@Autowired
	public GameControllerTest(GameRegistry registry) {
		this.registry = registry;
		this.newGameResponses = new ArrayBlockingQueue<>(10);
		this.errors = new ArrayBlockingQueue<>(10);
		this.gameStates = new ArrayBlockingQueue<>(10);
	}

	@BeforeEach
	void setup() throws InterruptedException, ExecutionException, TimeoutException {
		URL = "ws://localhost:" + port + "/ws";
	}

	private StompSession getSession() throws InterruptedException, ExecutionException, TimeoutException {
		WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
		client.setMessageConverter(new MappingJackson2MessageConverter());
		return client.connect(URL, new StompSessionHandlerAdapter() {
		}).get(1, TimeUnit.SECONDS);
	}

	@Test
	void testCreateGame() throws InterruptedException, ExecutionException, TimeoutException, GameNotFoundException {
		StompSession session = getSession();
		session.subscribe(NEW_GAME_ENDPOINT, new CreateGameStompFrameHandler());
		NewGameResponse response = newGameResponses.poll(1, TimeUnit.SECONDS);
		UUID gameId = response.gameId();
		assertNotNull(gameId);

		GameContext context = registry.getContext(gameId);
		assertTrue(context instanceof GameContext);
		assertTrue(context.getGame() instanceof Game);
	}

	@Test
	void testHandleException() throws InterruptedException, ExecutionException, TimeoutException {
		StompSession session = getSession();
		session.subscribe(ERROR_ENDPOINT, new ErrorStompFrameHandler());

		UUID id = UUID.randomUUID();
		session.subscribe(SET_GAME_ENDPOINT + "/" + id, new CreateGameStompFrameHandler());
		ErrorResponse error = errors.poll(1, TimeUnit.SECONDS);
		assertTrue(error.message().contains("No such game exists"));
	}

	@Test
	@Disabled
	void testGetGameState() throws InterruptedException, ExecutionException, TimeoutException {
		StompSession session = getSession();
		session.subscribe(NEW_GAME_ENDPOINT, new CreateGameStompFrameHandler());
		UUID gameId = newGameResponses.poll(1, TimeUnit.SECONDS).gameId();

		session.subscribe(GAME_ENDPOINT + "/" + gameId, new GameStompFrameHandler());
		GameState state = gameStates.poll(1, TimeUnit.SECONDS);
		assertNotNull(state);
	}

	@Test
	void testProcessInput() throws InterruptedException, ExecutionException, TimeoutException {
		StompSession sessionA = getSession();
		sessionA.subscribe(NEW_GAME_ENDPOINT, new CreateGameStompFrameHandler());
		UUID gameId = newGameResponses.poll(1, TimeUnit.SECONDS).gameId();
		sessionA.subscribe(GAME_ENDPOINT + "/" + gameId, new GameStompFrameHandler());
		sessionA.subscribe(READY_PLAYER_ENDPOINT + "/" + gameId, new SubscriptionHandler());

		StompSession sessionB = getSession();
		sessionB.subscribe(SET_GAME_ENDPOINT + "/" + gameId, new CreateGameStompFrameHandler());
		sessionB.subscribe(GAME_ENDPOINT + "/" + gameId, new GameStompFrameHandler());
		sessionB.subscribe(READY_PLAYER_ENDPOINT + "/" + gameId, new SubscriptionHandler());
		
		GameCommand move = new GameCommand(0, "A1");
		sessionA.send(MOVE_ENDPOINT, move);

		GameState state = gameStates.poll(1, TimeUnit.SECONDS);

		assertNotNull(state);
		System.out.println(state);

		assertTrue(state.currentPlayer() == Player.WHITE);
		assertTrue(state.moveNumber() == 1);

		char[][] board = state.board();
		assertTrue(board.length == 19);
		assertTrue(board[0].length == 19);
		
		char[] expected = new char[19];
		Arrays.fill(expected, ' ');
		expected[0] = Board.BLACK;
		
		assertTrue(Arrays.equals(board[0], expected));
	}

	@Test
	void testSetGame() throws InterruptedException, ExecutionException, TimeoutException {
		StompSession session = getSession();
		UUID id = UUID.randomUUID();
		registry.putContext(id, new GameContext());
		session.subscribe(SET_GAME_ENDPOINT + "/" + id, new CreateGameStompFrameHandler());
		NewGameResponse response = newGameResponses.poll(1, TimeUnit.SECONDS);
		assertTrue(response.gameId().equals(id));
	}

}
