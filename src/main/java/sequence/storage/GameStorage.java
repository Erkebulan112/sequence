package sequence.storage;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import sequence.session.GameSession;

@Component
public class GameStorage {

    private final ConcurrentHashMap<String, GameSession> games = new ConcurrentHashMap<>();

    public GameSession getGame(String lobbyId) {
        return games.computeIfAbsent(lobbyId, GameSession::new);
    }

    public void removeGame(String lobbyId) {
        games.remove(lobbyId);
    }
}
