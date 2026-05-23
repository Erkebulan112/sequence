package sequence.game.engine;

import sequence.board.CellState;
import sequence.session.GameSession;
import sequence.card.CardModel.Card;
import sequence.card.CardModel.Rank;

public class GameEngine {

    private final GameSession game;

    public GameEngine(GameSession game) {
        this.game = game;
    }

    public boolean applyMove(Move move) {
        if (!game.getCurrentPlayer().equals(move.player())) return false;
        if (!hasCard(move.player(), move.card())) return false;

        boolean result;
        Rank rank = move.card().rank();

        if (rank == Rank.JACK_ONE_EYED) {
            result = removeOpponentChip(move);
        } else if (rank == Rank.JACK_TWO_EYED) {
            result = placeAnyWhere(move);
        } else {
            result = placeNormal(move);
        }

        if (!result) return false;
        removeCard(move.player(), move.card());
        return true;
    }

    private boolean placeNormal(Move move) {
        var boardCard = game.getBoard().getCards()[move.row()][move.col()];
        if (!boardCard.equals(move.card())) {
            return false;
        }
        return placeChip(move);
    }

    private boolean placeAnyWhere(Move move) {
        return placeChip(move);
    }

    private boolean placeChip(Move move) {
        CellState state = game.getCellState(move.player());

        return game.getBoard().placeChip(
                move.row(),
                move.col(),
                state
        );
    }

    private boolean removeOpponentChip(Move move) {
        CellState[][] chips = game.getBoard().getChips();
        CellState target = chips[move.row()][move.col()];
        if (target == null || target == CellState.EMPTY || target == CellState.CHAMELEON) return false;
        CellState myTeam = game.getCellState(move.player());
        if (target == myTeam) return false;
        game.getBoard().removeChip(move.row(), move.col());
        return true;
    }

    private boolean hasCard(String player, Card card) {
        return game.getPlayerHands()
                .getOrDefault(player, java.util.List.of())
                .contains(card);
    }

    private void removeCard(String player, Card card) {
        game.getPlayerHands().get(player).remove(card);
    }
}