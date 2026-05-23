package sequence.game.engine;

import sequence.card.CardModel.Card;

public record Move(
        String lobbyId,
        String player,
        int row,
        int col,
        Card card,
        String actionType
) {}