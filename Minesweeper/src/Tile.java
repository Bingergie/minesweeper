public class Tile {
    private final int x;
    private final int y;
    private int value;

    private boolean isRevealed;
    private boolean isFlagged;

    public Tile(int x, int y, int value, Board board) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.isRevealed = false;
    }

    public void reveal() {
        if (isFlagged) return;
        isRevealed = true;
    }

    public void toggleFlag() {
        isFlagged = !isFlagged;
    }

    public char getState() {
        if (!isRevealed) {
            return isFlagged ? 'B' : '-';
        }
        return value < 0 ? 'X' : (char) (value + '0');
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public int getValue() {
        return value;
    }

    public char getCharValue() {
        return value < 0 ? 'X' : (char) (value + '0');
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
