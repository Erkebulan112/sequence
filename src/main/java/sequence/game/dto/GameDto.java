package sequence.game.dto;

import java.util.List;
import sequence.card.CardModel.Card;
import sequence.session.PlayerStatus;

public class GameDto {

    public record PlayerJoinRequest(
            String lobbyId,
            String playerName,
            int maxPlayers
    ) {}

    public record ChangeTeamRequest(
            String lobbyId,
            String playerName,
            TeamColor team
    ) {}

    public record StartGameRequest(
            String lobbyId,
            String playerName
    ) {}

    public record GameStateResponse(
            String lobbyId,
            PlayerStatus status,
            int maxPlayers,
            List<String> redTeam,
            List<String> blueTeam,
            String[][] chips,
            String currentPlayer,
            List<Card> playerHand,
            String winner
    ) {}
}