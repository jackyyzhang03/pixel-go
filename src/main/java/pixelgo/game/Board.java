package pixelgo.game;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import pixelgo.exceptions.InvalidMoveException;

/**
 * A class for Go game boards.
 * 
 * @author jacky
 *
 */
public class Board {
	public static final char EMPTY = ' ';
	public static final char BLACK = 'B';
	public static final char WHITE = 'W';

	private final int N;
	private final Set<ZobristHash> positions;
	private ZobristHash hash;
	private ZobristHash prevHash;
	
	private char[][] board;
	private char[][] prevBoard;
	
	/**
	 * Constructs an empty Go Board with dimensions n x n.
	 * 
	 * @param n the size of the board
	 */
	public Board(int n) {
		if (n < 1)
			throw new IllegalArgumentException("Board size must be positive.");

		N = n;
		board = new char[n][n];
		prevBoard = new char[n][n];
		for (int i = 0; i < N; i++) {
			Arrays.fill(board[i], EMPTY);
			Arrays.fill(prevBoard[i], EMPTY);
		}
				
		positions = new HashSet<>();
		
		hash = new ZobristHash();
		prevHash = hash.copy();
	}

	/**
	 * Calculates the area score of both players.
	 * 
	 * @return an integer array with the black's score as first element, and white's
	 *         score as the second
	 */
	public int[] getAreaScore() {
		boolean[][] visited = new boolean[N][N];
		Queue<int[]> queue = new ArrayDeque<>();

		int blackPoints = 0;
		int whitePoints = 0;

		synchronized (this) {
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (board[i][j] == BLACK) {
						blackPoints++;
					} else if (board[i][j] == WHITE) {
						whitePoints++;
					} else if (!visited[i][j]) {
						// Use breadth-first search to determine the size of the territory and check if
						// it is neutral or controlled by black or white.
						boolean surroundedByBlack = false;
						boolean surroundedByWhite = false;

						queue.add(new int[] { i, j });
						visited[i][j] = true;

						int territorySize = 0;

						while (!queue.isEmpty()) {
							int[] pt = queue.remove();
							int r = pt[0];
							int c = pt[1];

							if (board[r][c] == EMPTY) {
								territorySize++;

								if (inBounds(r + 1) && !visited[r + 1][c]) {
									queue.add(new int[] { r + 1, c });
									visited[r + 1][c] = true;
								}
								if (inBounds(r - 1) && !visited[r - 1][c]) {
									queue.add(new int[] { r - 1, c });
									visited[r - 1][c] = true;
								}
								if (inBounds(c + 1) && !visited[r][c + 1]) {
									queue.add(new int[] { r, c + 1 });
									visited[r][c + 1] = true;
								}
								if (inBounds(c - 1) && !visited[r][c]) {
									queue.add(new int[] { r, c - 1 });
									visited[r][c - 1] = true;
								}

							} else {
								visited[r][c] = false; // Same stone may need to be visited again by another territory.

								if (board[r][c] == BLACK) {
									surroundedByBlack = true;
								}
								if (board[r][c] == WHITE) {
									surroundedByWhite = true;
								}
							}
						}

						if (surroundedByBlack && !surroundedByWhite) {
							blackPoints += territorySize;
						} else if (surroundedByWhite && !surroundedByBlack) {
							whitePoints += territorySize;
						}
					}
				}
			}
		}

		return new int[] { blackPoints, whitePoints };
	}

	/**
	 * Get the array representation of the board.
	 * 
	 * @return a copy of the array backing the board
	 */
	public synchronized char[][] getBoard() {
		return Arrays.copyOf(board, N);
	}

	/**
	 * Place a stone at the specified location.
	 * 
	 * @param player the player placing the stone
	 * @param location the location of the stone
	 * @throws InvalidMoveException
	 */
	public synchronized void placeStone(Player player, int[] location) throws InvalidMoveException {
		int r = location[0];
		int c = location[1];

		if (r >= N || c >= N) {
			throw new IllegalArgumentException("Row/column indicies must be less than board size: " + N);
		}

		deepCopy(board, prevBoard); // Store the current board state.
		prevHash.setHash(hash.getHash()); // Store the current Zobrist hash.

		try {
			if (board[r][c] != EMPTY)
				throw new InvalidMoveException(Violation.OCCUPIED_POSITION);

			char color = player == Player.BLACK ? BLACK : WHITE;
			char opponentColor = player == Player.BLACK ? WHITE : BLACK;
			
			board[r][c] = color;
			hash.toggleStone(color, r * N + c);
			
			// Try to capture opponent stones.
			tryToCaptureAll(opponentColor, r, c);

			// If player's own stones will be captured, throw an exception.
			if (tryToCapture(board[r][c], r, c)) {
				throw new InvalidMoveException(Violation.SUICIDE);
			}
			
			if (positions.contains(hash)) {
				throw new InvalidMoveException(Violation.REPEATED_POSITION);
			}
			
			positions.add(hash.copy());

		} catch (InvalidMoveException e) {
			// Revert changes.
			char[][] temp = board;
			board = prevBoard;
			prevBoard = temp;
			
			hash.setHash(prevHash.getHash());
			throw e;
		}
	}

	/**
	 * Get the size of the board.
	 * 
	 * @return the size <code>N</code> of the <code>N</code> x <code>N</code> board
	 */
	public int size() {
		return N;
	}

	/**
	 * Find if an integer index is in bounds of the board.
	 * 
	 * @param i the index
	 * @return true if the index is in bounds
	 */
	private boolean inBounds(int i) {
		return i >= 0 && i < N;
	}

	/**
	 * Call on an adjacent position to determine if it is connected.
	 * 
	 * @param r     the row index
	 * @param c     the column index
	 * @param color the color required to be connected
	 * @return true if the position is connected
	 */
	private boolean isConnected(int r, int c, char color) {
		return inBounds(r) && inBounds(c) && (board[r][c] == color || board[r][c] == EMPTY);
	}

	/**
	 * Check if a group has any liberties and clear it off the board if it does not.
	 * 
	 * @param color the color to capture
	 * @param r     the starting row index
	 * @param c     the starting column index
	 * @return true if a capture was made
	 */
	private boolean tryToCapture(char color, int r, int c) {
		if (board[r][c] != color)
			return false;

		// Breadth first search for a liberty.

		boolean[][] visited = new boolean[N][N];

		Queue<int[]> queue = new ArrayDeque<>();
		queue.add(new int[] { r, c });
		visited[r][c] = true;

		synchronized (this) {
			while (!queue.isEmpty()) {
				int[] pt = queue.remove();
				r = pt[0];
				c = pt[1];

				if (board[r][c] == EMPTY)
					return false;

				if (isConnected(r - 1, c, color) && !visited[r - 1][c]) {
					queue.add(new int[] { r - 1, c });
					visited[r - 1][c] = true;
				}
				if (isConnected(r + 1, c, color) && !visited[r + 1][c]) {
					queue.add(new int[] { r + 1, c });
					visited[r + 1][c] = true;
				}
				if (isConnected(r, c - 1, color) && !visited[r][c - 1]) {
					queue.add(new int[] { r, c - 1 });
					visited[r][c - 1] = true;
				}
				if (isConnected(r, c + 1, color) && !visited[r][c + 1]) {
					queue.add(new int[] { r, c + 1 });
					visited[r][c + 1] = true;
				}
			}

			// Clear the captured stones.
			for (r = 0; r < N; r++) {
				for (c = 0; c < N; c++) {
					if (visited[r][c]) {
						if (board[r][c] == color) {
							hash.toggleStone(color, r * N + c);
						}
						board[r][c] = EMPTY;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Convenience method to try to capture all adjacent groups to a location.
	 * 
	 * @param color the color to capture
	 * @param r     the row index
	 * @param c     the column index
	 */
	private void tryToCaptureAll(char color, int r, int c) {
		if (inBounds(r + 1))
			tryToCapture(color, r + 1, c);
		if (inBounds(r - 1))
			tryToCapture(color, r - 1, c);
		if (inBounds(c + 1))
			tryToCapture(color, r, c + 1);
		if (inBounds(c - 1))
			tryToCapture(color, r, c - 1);
	}
	
	private void deepCopy(char[][] src, char[][] des) {
		for (int i = 0; i < N; i++) {
			System.arraycopy(src[i], 0, des[i], 0, N);
		}
	}
}
