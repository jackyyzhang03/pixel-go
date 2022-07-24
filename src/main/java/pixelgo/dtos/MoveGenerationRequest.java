package pixelgo.dtos;

import java.util.List;

import pixelgo.game.Move;
import pixelgo.game.Player;

public record MoveGenerationRequest(Player player, List<Move> moves) {
}
