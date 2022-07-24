package pixelgo.dtos;

import java.util.UUID;

import pixelgo.game.Player;

public record NewGameResponse(UUID gameId, Player player) {
}
