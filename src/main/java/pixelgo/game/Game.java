package pixelgo.game;

import java.util.concurrent.atomic.AtomicInteger;

import pixelgo.exceptions.GameNotStartedException;
import pixelgo.exceptions.InvalidMoveException;
import pixelgo.exceptions.PlayerOutOfTurnException;

/**
 * A class for Go games.
 * 
 * @author jacky
 *
 */
public class Game {
	private Board board;
	private boolean consecutivePass;
	private Player currentPlayer;
	private boolean hasEnded;
	private boolean running;
	private AtomicInteger moveNumber;

	/**
	 * Constructs a new game with size <code>n</code>.
	 * 
	 * @param n the board size
	 */
	public Game(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Board size must be positive.");
		}

		running = false;
		board = new Board(n);
		currentPlayer = Player.BLACK;
		consecutivePass = false;
		hasEnded = false;
		moveNumber = new AtomicInteger();
	}

	/**
	 * Executes the provided move.
	 * 
	 * @param move the move
	 * @throws PlayerOutOfTurnException
	 * @throws GameNotStartedException
	 * @throws InvalidMoveException
	 */
	public synchronized void executeMove(Move move)
			throws PlayerOutOfTurnException, GameNotStartedException, InvalidMoveException {
		Player player = move.player();
		checkPreconditions(player);

		String vertex = move.vertex();
		if (vertex.equalsIgnoreCase("PASS")) {
			passTurn(player);
		} else {
			placeStone(player, vertex);
		}
	}

	/**
	 * Get the array representation of the board.
	 * 
	 * @return a copy of the array backing the board
	 */
	public synchronized char[][] getBoard() {
		return board.getBoard();
	}

	/**
	 * Get the size of the board.
	 * 
	 * @return the size <code>N</code> of the <code>N</code> x <code>N</code> board
	 */
	public int getBoardSize() {
		return board.size();
	}

	/**
	 * Get the current player.
	 * 
	 * @return the currentPlayer
	 */
	public synchronized Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Get the current move number.
	 * 
	 * @return the move number
	 */
	public synchronized int getMoveNumber() {
		return moveNumber.get();
	}

	/**
	 * Get the area score of both players.
	 * 
	 * @return an integer array with the black's score as first element, and white's
	 *         score as the second
	 */
	public synchronized int[] getPoints() {
		return board.getAreaScore();
	}

	/**
	 * Get whether or not a consecutive pass will be made.
	 * 
	 * @return true if the previous player had passed
	 */
	public boolean isConsecutivePass() {
		return consecutivePass;
	}

	/**
	 * Get whether or not the game has ended.
	 * 
	 * @return true if the game has ended
	 */
	public boolean isHasEnded() {
		return hasEnded;
	}

	/**
	 * Get whether or not the game is running.
	 * 
	 * @return true if the game is running
	 */
	public synchronized boolean isRunning() {
		return running;
	}

	/**
	 * Pause the game.
	 */
	public synchronized void pauseGame() {
		running = false;
	}

	/**
	 * Start the game.
	 */
	public synchronized void startGame() {
		running = true;
	}

	/**
	 * End the game.
	 */
	public synchronized void stopGame() {
		running = false;
		hasEnded = true;
	}

	/**
	 * Assert that it is the current player's turn and that the game is running.
	 * 
	 * @param player the player to check
	 * @throws PlayerOutOfTurnException
	 * @throws GameNotStartedException
	 */
	private void checkPreconditions(Player player) throws PlayerOutOfTurnException, GameNotStartedException {
		if (currentPlayer != player)
			throw new PlayerOutOfTurnException(player);
		if (!running)
			throw new GameNotStartedException("Game has not started.");
	}

	/**
	 * Parse the GTP command into row and column indices.
	 * 
	 * @param vertex the GTP vertex string
	 * @return an integer array with the row index as the first element and the
	 *         column index as the second
	 * @throws InvalidMoveException 
	 */
	private int[] parseVertex(String vertex) throws InvalidMoveException {

		try {
			int c = (int) Character.toUpperCase(vertex.charAt(0)) - 'A';
			int r = (int) Integer.parseInt(vertex.substring(1)) - 1; // Board is one-indexed
			if (c >= 9)
				c--; // There is no I on the board, so shift J and onwards down by one
			
			if (c < 0 || r < 0 || c >= board.size() || r >= board.size()) {
				throw new InvalidMoveException(Violation.ILLEGAL_ARGUMENT);
			}
			
			return new int[] { r, c };
		} catch (NumberFormatException e) {
			throw new InvalidMoveException(Violation.ILLEGAL_ARGUMENT);
		}
	}

	/**
	 * Pass the player's turn.
	 * 
	 * @param player the player passing
	 * @throws PlayerOutOfTurnException
	 * @throws GameNotStartedException
	 */
	private void passTurn(Player player) throws PlayerOutOfTurnException, GameNotStartedException {
		checkPreconditions(player);

		if (currentPlayer == Player.BLACK) {
			currentPlayer = Player.WHITE;
		} else {
			currentPlayer = Player.BLACK;
		}

		if (consecutivePass) {
			stopGame();
		} else {
			consecutivePass = true;
			moveNumber.incrementAndGet();
		}
	}

	/**
	 * Place a stone at the specified location.
	 * 
	 * @param player the player placing the stone
	 * @param location the location to place the stone
	 * @throws InvalidMoveException
	 * @throws PlayerOutOfTurnException
	 * @throws GameNotStartedException
	 */
	private void placeStone(Player player, int[] location)
			throws InvalidMoveException, PlayerOutOfTurnException, GameNotStartedException {
		if (currentPlayer != player) {
			throw new PlayerOutOfTurnException(player);
		}
		if (!running) {
			throw new GameNotStartedException("Game has not started.");
		}

		board.placeStone(player, location);
		currentPlayer = player.opponent;
		consecutivePass = false;
		moveNumber.incrementAndGet();
	}

	/**
	 * Place a stone at the specified location.
	 * 
	 * @param player the player placing the stone
	 * @param vertex the GTP vertex string
	 * @throws InvalidMoveException
	 * @throws PlayerOutOfTurnException
	 * @throws GameNotStartedException
	 */
	private void placeStone(Player player, String vertex)
			throws InvalidMoveException, PlayerOutOfTurnException, GameNotStartedException {
		int[] location = parseVertex(vertex);
		placeStone(player, location);
	}
}
