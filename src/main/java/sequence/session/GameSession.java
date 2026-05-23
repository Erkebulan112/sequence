package sequence.session;

import lombok.Data;
import sequence.board.CellState;
import sequence.board.GameBoard;
import sequence.card.CardModel.Card;
import sequence.card.CardModel.Rank;
import sequence.card.CardModel.Suit;
import sequence.game.dto.TeamColor;

import java.util.*;

@Data
public class GameSession {

    private final String lobbyId;
    private final GameBoard board = new GameBoard();
    private final List<String> redTeam = new ArrayList<>();
    private final List<String> blueTeam = new ArrayList<>();
    private final List<String> turnOrder = new ArrayList<>();
    private final Map<String, List<Card>> playerHands = new HashMap<>();
    private final LinkedList<Card> deck = new LinkedList<>();
    private int currentPlayerIndex = 0;
    private int maxPlayers = 4;
    private CellState winnerTeam;
    private PlayerStatus status = PlayerStatus.WAITING;
    private int redSequences = 0;
    private int blueSequences = 0;

    public GameSession(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public void startNewGame() {
        status = PlayerStatus.PLAYING;
        generateDeck();
        playerHands.clear();
        turnOrder.clear();

        int teamSize = maxPlayers / 2;
        for (int i = 0; i < teamSize; i++) {
            if (i < redTeam.size()) turnOrder.add(redTeam.get(i));
            if (i < blueTeam.size()) turnOrder.add(blueTeam.get(i));
        }

        int cardsPerPlayer = (maxPlayers == 6) ? 4 : 5;

        for (String player : turnOrder) {
            List<Card> hand = new ArrayList<>();

            for (int i = 0; i < cardsPerPlayer; i++) {
                if (!deck.isEmpty()) {
                    hand.add(deck.removeFirst());
                }
            }
            playerHands.put(player, hand);
        }
        currentPlayerIndex = 0;
    }

    private void generateDeck() {
        deck.clear();
        for (int i = 0; i < 2; i++) {
            for (var suit : sequence.card.CardModel.Suit.values()) {
                if (suit == Suit.CORNER) continue;
                for (var rank : sequence.card.CardModel.Rank.values()) {
                    if (rank == Rank.NONE) continue;
                    if (rank == Rank.JACK_ONE_EYED && (suit == Suit.CLUBS || suit == Suit.DIAMONDS)) continue;
                    if (rank == Rank.JACK_TWO_EYED && (suit == Suit.HEARTS || suit == Suit.SPADES)) continue;
                    deck.add(new Card(suit, rank));
                }
            }
        }
        Collections.shuffle(deck);
    }

    public String getCurrentPlayer() {
        if (turnOrder.isEmpty()) return null;
        return turnOrder.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) return;
        currentPlayerIndex = (currentPlayerIndex + 1) % turnOrder.size();
    }

    public TeamColor getTeamOfPlayer(String player) {
        if (redTeam.contains(player)) return TeamColor.RED;
        if (blueTeam.contains(player)) return TeamColor.BLUE;
        return null;
    }

    public CellState getCellState(String player) {
        TeamColor team = getTeamOfPlayer(player);
        if (team == TeamColor.RED) return CellState.RED;
        if (team == TeamColor.BLUE) return CellState.BLUE;
        return null;
    }

    public boolean checkWin(int row, int col, CellState state) {
        if (state == null || state == CellState.EMPTY || state == CellState.CHAMELEON) return false;
        int[][] dirs = {{0,1},{1,0},{1,1},{-1,1}};
        boolean madeSequence = false;
        for (int[] d : dirs) {
            int count = 1 + count(row, col, d[0], d[1], state)
                    + count(row, col, -d[0], -d[1], state);
            if (count >= 5) { madeSequence = true; break; }
        }
        if (!madeSequence) return false;
        if (state == CellState.RED) redSequences++;
        else blueSequences++;
        return redSequences >= 2 || blueSequences >= 2;
    }

    private int count(int r, int c, int dr, int dc, CellState state) {
        int cnt = 0;
        CellState[][] boardState = board.getChips();
        r += dr; c += dc;
        while (r >= 0 && r < 10 && c >= 0 && c < 10) {
            CellState cur = boardState[r][c];
            if (cur == state || cur == CellState.CHAMELEON) {
                cnt++;
                r += dr; c += dc;
            } else break;
        }
        return cnt;
    }

    public void drawCard(String player) {
        if (deck.isEmpty()) return;
        List<Card> hand = playerHands.get(player);
        if (hand == null) return;
        hand.add(deck.removeFirst());
    }
}