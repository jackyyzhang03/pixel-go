package pixelgo.exceptions;

import pixelgo.game.Player;

/**
 * This exception is thrown when a player attempts to make a move when it is not
 * their turn.
 * 
 * @author jacky
 */
public class PlayerOutOfTurnException extends ClientException {
	
	private static final long serialVersionUID = 1L;

	private Player player;

	public PlayerOutOfTurnException(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
