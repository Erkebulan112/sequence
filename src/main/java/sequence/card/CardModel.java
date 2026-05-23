package sequence.card;

public class CardModel {

    public enum Suit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES,
        CORNER
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
        JACK_ONE_EYED,
        JACK_TWO_EYED,
        QUEEN, KING, ACE, NONE
    }

    public record Card(Suit suit, Rank rank) {
        @Override
        public String toString() {
            if (suit == Suit.CORNER) return "CORNER";
            return rank.name() + "_" + suit.name();
        }
    }
}
