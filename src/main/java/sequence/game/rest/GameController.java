package sequence.game.rest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import sequence.board.CellState;
import sequence.game.dto.GameDto;
import sequence.game.dto.TeamColor;
import sequence.game.engine.GameEngine;
import sequence.game.engine.Move;
import sequence.session.GameSession;
import sequence.session.PlayerStatus;
import sequence.storage.GameStorage;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameStorage gameStorage;
    private final SimpMessagingTemplate template;

    @MessageMapping("/game/play")
    public void play(Move move) {
        GameSession game = gameStorage.getGame(move.lobbyId());
        GameEngine engine = new GameEngine(game);

        synchronized (game) {
            if (game.getStatus() != PlayerStatus.PLAYING) return;
            CellState winnerState = game.getCellState(move.player());

            boolean ok = engine.applyMove(move);
            if (!ok) return;

            boolean win = game.checkWin(
                    move.row(),
                    move.col(),
                    game.getCellState(move.player())
            );

            if (win) {
                game.setStatus(PlayerStatus.FINISHED);
                game.setWinnerTeam(winnerState);
            } else {
                game.nextTurn();
                game.drawCard(move.player());
            }
        }

        broadcast(game);

        if (game.getStatus() == PlayerStatus.FINISHED) {
            CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES)
                    .execute(() -> gameStorage.removeGame(game.getLobbyId()));
        }
    }

    @MessageMapping("/game/join")
    public void join(GameDto.PlayerJoinRequest request) {
        GameSession game = gameStorage.getGame(request.lobbyId());
        synchronized (game) {
            if (game.getStatus() == PlayerStatus.WAITING) {

                if (game.getRedTeam().isEmpty() && game.getBlueTeam().isEmpty()) {
                    game.setMaxPlayers(request.maxPlayers());
                }

                if (!game.getRedTeam().contains(request.playerName())
                        && !game.getBlueTeam().contains(request.playerName())) {

                    if (game.getRedTeam().size() < game.getMaxPlayers() / 2) {
                        game.getRedTeam().add(request.playerName());
                    } else if (game.getBlueTeam().size() < game.getMaxPlayers() / 2) {
                        game.getBlueTeam().add(request.playerName());
                    }
                }
            }
        }

        if (game.getStatus() == PlayerStatus.WAITING) {
            broadcastLobby(game);
        } else {
            broadcast(game);
        }
    }

    @MessageMapping("/game/change-team")
    public void changeTeam(GameDto.ChangeTeamRequest request) {
        GameSession game = gameStorage.getGame(request.lobbyId());
        synchronized (game) {
            if (game.getStatus() != PlayerStatus.WAITING) return;

            if (request.team() == TeamColor.RED) {
                if (game.getRedTeam().size() < game.getMaxPlayers() / 2 && !game.getRedTeam().contains(request.playerName())) {
                    game.getBlueTeam().remove(request.playerName());
                    game.getRedTeam().add(request.playerName());
                }
            } else {
                if (game.getBlueTeam().size() < game.getMaxPlayers() / 2 && !game.getBlueTeam().contains(request.playerName())) {
                    game.getRedTeam().remove(request.playerName());
                    game.getBlueTeam().add(request.playerName());
                }
            }
        }
        broadcastLobby(game);
    }

    @MessageMapping("/game/start")
    public void startGame(GameDto.StartGameRequest request) {
        GameSession game = gameStorage.getGame(request.lobbyId());
        synchronized (game) {
            if (game.getStatus() != PlayerStatus.WAITING) return;
            int teamSize = game.getMaxPlayers() / 2;
            if (game.getRedTeam().size() != teamSize || game.getBlueTeam().size() != teamSize) return;
            game.startNewGame();
        }
        broadcast(game);
    }

    private void broadcastLobby(GameSession game) {
        for (String player : game.getRedTeam()) {
            sendLobbyState(game, player);
        }
        for (String player : game.getBlueTeam()) {
            sendLobbyState(game, player);
        }
    }

    private void sendLobbyState(GameSession game, String player) {
        var response = new GameDto.GameStateResponse(
                game.getLobbyId(), game.getStatus(), game.getMaxPlayers(),
                game.getRedTeam(), game.getBlueTeam(), new String[10][10],
                null, java.util.List.of(), null
        );
        template.convertAndSend("/topic/game/" + game.getLobbyId() + "/" + player, response);
    }

    private void broadcast(GameSession game) {
        CellState[][] boardChips = game.getBoard().getChips();
        String[][] chipsDto = new String[10][10];
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                chipsDto[r][c] = boardChips[r][c].name();
            }
        }

        for (String player : game.getTurnOrder()) {
            var response = new GameDto.GameStateResponse(
                    game.getLobbyId(),
                    game.getStatus(),
                    game.getMaxPlayers(),
                    game.getRedTeam(),
                    game.getBlueTeam(),
                    chipsDto,
                    game.getCurrentPlayer(),
                    game.getPlayerHands().get(player),
                    game.getStatus() == PlayerStatus.FINISHED ? game.getWinnerTeam() == CellState.RED ? "Red Team" : "Blue Team" : null
            );

            template.convertAndSend(
                    "/topic/game/" + game.getLobbyId() + "/" + player,
                    response
            );
        }
    }
}