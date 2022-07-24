package pixelgo.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.springframework.stereotype.Component;

import pixelgo.exceptions.EngineException;
import pixelgo.game.Move;
import pixelgo.game.Player;

/**
 * A class for interfacing with the KataGo GTP engine.
 * 
 * @author jacky
 *
 */
@Component
public class GTPClient {	
	private BufferedReader in;
	private BufferedWriter out;
	
	public GTPClient() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("katago.exe", "gtp");
		Process process = processBuilder.start();
		processBuilder.redirectErrorStream(true);
		
		in = process.inputReader();
		out = process.outputWriter();
	}
	
	public void clearBoard() throws IOException, EngineException {
		executeCommand("clear_board");
	}
	
	public Move generateMove(Player player) throws IOException, EngineException {
		String result = executeCommand("genmove " + player);
		return new Move(player, result);
	}
	
	public void playMove(Move move) throws IOException, EngineException {
		String player = move.player() == Player.BLACK ? "B" : "W";
		executeCommand("play " + player + " " + move.vertex());
	}
	
	private synchronized String executeCommand(String command) throws IOException, EngineException {
		out.append(command + "\n");
		out.flush();
		
		String res;
		while ((res = in.readLine().trim()).equals("")); // Discard blank lines.
		
		if (res.charAt(0) == '?') {
			String message = res.substring(1).trim();
			while ((res = in.readLine().trim()).equals("")); // Discard blank lines.
			throw new EngineException(message);
		}

		return res.substring(1).trim();
	}
}
