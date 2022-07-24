package pixelgo.services;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import pixelgo.exceptions.EngineException;
import pixelgo.game.Move;
import pixelgo.game.Player;

@Service
public class EngineService {
	private GTPClient client;
	
	public EngineService(GTPClient client) {
		this.client = client;
	}
	
	public Move generateMove(Player player, List<Move> moves) throws IOException, EngineException {
		client.clearBoard();
		for (Move move: moves) {
			client.playMove(move);
		}
		
		return client.generateMove(player);
	}
}
