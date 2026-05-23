package sequence.board;

import lombok.Getter;
import sequence.card.CardModel;
import sequence.card.CardModel.Card;

@Getter
public class GameBoard {

    private final Card[][] cards = new Card[10][10];
    private final CellState[][] chips = new CellState[10][10];

    private static final String[][] ORIGINAL_SEQUENCE_BOARD = {
            {"CORNER",        "SIX_DIAMONDS",  "SEVEN_DIAMONDS", "EIGHT_DIAMONDS","NINE_DIAMONDS", "TEN_DIAMONDS",  "QUEEN_DIAMONDS","KING_DIAMONDS", "ACE_DIAMONDS",  "CORNER"},
            {"FIVE_DIAMONDS", "THREE_HEARTS",  "TWO_HEARTS",     "TWO_SPADES",    "THREE_SPADES",  "FOUR_SPADES",   "FIVE_SPADES",   "SIX_SPADES",   "SEVEN_SPADES",  "ACE_CLUBS"},
            {"FOUR_DIAMONDS", "FOUR_HEARTS",   "KING_DIAMONDS",  "ACE_DIAMONDS",  "ACE_HEARTS",    "KING_CLUBS",    "QUEEN_CLUBS",   "TEN_CLUBS",    "EIGHT_SPADES",  "KING_CLUBS"},
            {"THREE_DIAMONDS","FIVE_HEARTS",   "QUEEN_DIAMONDS", "TEN_HEARTS",    "NINE_HEARTS",   "EIGHT_HEARTS",  "SEVEN_HEARTS",  "NINE_CLUBS",   "NINE_SPADES",   "QUEEN_SPADES"},
            {"TWO_DIAMONDS",  "SIX_HEARTS",   "TEN_DIAMONDS",   "QUEEN_HEARTS",  "THREE_HEARTS",  "TWO_HEARTS",    "FOUR_HEARTS",   "EIGHT_CLUBS",  "TEN_SPADES",    "TEN_CLUBS"},
            {"ACE_SPADES",    "SEVEN_HEARTS", "NINE_DIAMONDS",  "KING_HEARTS",   "ACE_CLUBS",     "FIVE_HEARTS",   "SIX_HEARTS",    "SEVEN_CLUBS",  "QUEEN_CLUBS",   "NINE_CLUBS"},
            {"KING_SPADES",   "EIGHT_HEARTS", "EIGHT_DIAMONDS", "TWO_CLUBS",     "THREE_CLUBS",   "FOUR_CLUBS",    "FIVE_CLUBS",    "SIX_CLUBS",    "KING_SPADES",   "EIGHT_CLUBS"},
            {"QUEEN_SPADES",  "NINE_HEARTS",  "SEVEN_DIAMONDS", "SIX_DIAMONDS",  "FIVE_DIAMONDS", "FOUR_DIAMONDS", "THREE_DIAMONDS","TWO_DIAMONDS", "ACE_SPADES",    "SEVEN_CLUBS"},
            {"TEN_SPADES",    "TEN_HEARTS",   "QUEEN_HEARTS",   "KING_HEARTS",   "ACE_HEARTS",    "TWO_CLUBS",     "THREE_CLUBS",   "FOUR_CLUBS",   "FIVE_CLUBS",    "SIX_CLUBS"},
            {"CORNER",        "NINE_SPADES",  "EIGHT_SPADES",   "SEVEN_SPADES",  "SIX_SPADES",    "FIVE_SPADES",   "FOUR_SPADES",   "THREE_SPADES", "TWO_SPADES",    "CORNER"}
    };

    public GameBoard() {
        initialize();
    }

    private void initialize() {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                String cardStr = ORIGINAL_SEQUENCE_BOARD[r][c];

                if (cardStr.equals("CORNER")) {
                    cards[r][c] = new Card(CardModel.Suit.CORNER, CardModel.Rank.NONE);
                    chips[r][c] = CellState.CHAMELEON;
                } else {
                    chips[r][c] = CellState.EMPTY;
                    String[] parts = cardStr.split("_");
                    CardModel.Rank rank = CardModel.Rank.valueOf(parts[0]);
                    CardModel.Suit suit = CardModel.Suit.valueOf(parts[1]);

                    cards[r][c] = new Card(suit, rank);
                }
            }
        }
    }

    public synchronized boolean placeChip(int row, int col, CellState state) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) return false;
        if (chips[row][col] == CellState.CHAMELEON) return false;
        if (chips[row][col] != CellState.EMPTY) return false;
        chips[row][col] = state;
        return true;
    }

    public synchronized void removeChip(int row, int col) {
        if (chips[row][col] == CellState.CHAMELEON) return;
        chips[row][col] = CellState.EMPTY;
    }
}