package pixelgo.dtos;

import pixelgo.game.Player;

public record GameState(char[][] board, int moveNumber, Player currentPlayer, int numPlayers, boolean consecutivePass,
		boolean running) {
}
