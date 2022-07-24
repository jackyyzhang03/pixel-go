package pixelgo.game;

import java.util.Random;

/**
 * A class for generating and comparing Zobrist hashes.
 * 
 * @author jacky
 *
 */
public class ZobristHash {
	private static int MAX_SIZE = 19;

	private static long[] blackBitstrings;
	private static long[] whiteBitstrings;

	/**
	 * Generate random bit strings for each possible position.
	 */
	static {
		Random random = new Random();
		blackBitstrings = new long[MAX_SIZE * MAX_SIZE];
		whiteBitstrings = new long[MAX_SIZE * MAX_SIZE];
		for (int i = 0; i < MAX_SIZE * MAX_SIZE; i++) {
			blackBitstrings[i] = random.nextLong();
			whiteBitstrings[i] = random.nextLong();
		}
	}

	/**
	 * A 64 bit hash stored in a long.
	 */
	private long hash;

	/**
	 * Creates a new Zobrist hash object with a zero hash representing an empty
	 * board.
	 */
	public ZobristHash() {
		this(0);
	}

	/**
	 * Creates a new Zobrist hash object from the specified hash.
	 * 
	 * @param hash
	 */
	public ZobristHash(long hash) {
		this.hash = hash;
	}

	/**
	 * Get a copy of the instance
	 * 
	 * @return a copy of the instance
	 */
	public ZobristHash copy() {
		return new ZobristHash(hash);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ZobristHash && ((ZobristHash) o).hash == hash;
	}

	/**
	 * Returns the long representation of the hash.
	 * 
	 * @return the hash
	 */
	public long getHash() {
		return hash;
	}

	@Override
	public int hashCode() {
		return (int) (hash ^ (hash >>> 32));
	}

	/**
	 * Set the hash of the object.
	 * 
	 * @param hash the hash
	 */
	public void setHash(long hash) {
		this.hash = hash;
	}

	/**
	 * Toggle the presence of a stone.
	 * 
	 * @param color the color of the stone
	 * @param i     the "flattened" index
	 */
	public void toggleStone(char color, int i) {
		if (color == Board.BLACK) {
			hash ^= blackBitstrings[i];
		} else if (color == Board.WHITE) {
			hash ^= whiteBitstrings[i];
		} else {
			throw new IllegalArgumentException("Invalid stone color");
		}
	}

	@Override
	public String toString() {
		return String.valueOf(hash);
	}
}
