package pixelgo.web;

import java.io.IOException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pixelgo.dtos.MoveGenerationRequest;
import pixelgo.exceptions.EngineException;
import pixelgo.game.Move;
import pixelgo.services.EngineService;

/**
 * Handle engine-related HTTP requests.
 * 
 * @author jacky
 *
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/engine")
public class EngineController {
	private final EngineService engineService;
	
	public EngineController(EngineService engineService) {
		this.engineService = engineService;
	}
	
	@PostMapping("/generate")
	public Move generateMove(@RequestBody MoveGenerationRequest request) throws IOException, EngineException {
		return engineService.generateMove(request.player(), request.moves());
	}
}
