package pixelgo.services;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.springframework.stereotype.Component;

import pixelgo.exceptions.GameNotFoundException;

@Component
public class GameRegistry {
	private Map<UUID, GameContext> contextMap = Collections.synchronizedMap(new WeakHashMap<>());
	
	public GameContext getContext(UUID id) throws GameNotFoundException {
		GameContext context = contextMap.get(id);
		if (context == null) throw new GameNotFoundException("No such game exists with id: " + id); 
		return context;
	}
	
	public UUID newContext(GameContext context) {
		UUID id = UUID.randomUUID();
		contextMap.put(id, context);
		return id;
	}

	public void putContext(UUID id, GameContext context) {
		contextMap.put(id, context);
	}
	
	public void removeContext(UUID id) {
		contextMap.remove(id);
	}
}
