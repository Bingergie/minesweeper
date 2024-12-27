import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Board {

    public enum Action {
        REVEAL,
        FLAG
    }

    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private final int mines;
    private boolean didInit;
    private boolean isGameOver;
    private boolean didWin;

    public Board(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
        this.didInit = false;
        this.isGameOver = false;
        this.didWin = false;

        tiles = new Tile[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                tiles[i][j] = new Tile(j, i, 0, this);
            }
        }
    }

    private void init(int excludeX, int excludeY) {
        setMines(excludeX, excludeY);
        setValues();
        didInit = true;
    }

    private void setMines() {
        int mines = this.mines;
        while (mines > 0) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            if (tiles[y][x].getValue() != -1) {
                tiles[y][x] = new Tile(x, y, -1, this);
                mines--;
            }
        }
    }

    private void setMines(int excludeX, int excludeY) {
        int mines = this.mines;
        while (mines > 0) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            if (x == excludeX && y == excludeY || tiles[y][x].getValue() == -1) continue;

            AtomicBoolean isNeighbor = new AtomicBoolean(false);
            forEachNeighboringTile(tiles[y][x], tile -> {
                if (tile.getX() == excludeX && tile.getY() == excludeY) {
                    isNeighbor.set(true);
                }
            });
            if (isNeighbor.get()) continue;

            tiles[y][x] = new Tile(x, y, -1, this);
            mines--;
        }
    }

    private void setValues() {
        forEachTile(tile -> {
            if (tile.getValue() < 0) {
                return;
            }
            forEachNeighboringTile(tile, neighbor -> {
                if (neighbor.getValue() < 0) {
                    tile.setValue(tile.getValue() + 1);
                }
            });
        });
    }

    public void act(int x, int y, Action action) {
        if (!didInit) {
            init(x, y);
        }
        Tile tile = tiles[y][x];
        if (tile.isRevealed()) {
            return;
        }
        switch (action) {
            case REVEAL:
                if (!tile.isFlagged()) reveal(x, y);
                break;
            case FLAG:
                toggleFlag(x, y);
                break;
        }
    }

    private void reveal(int x, int y) {
        Tile tile = tiles[y][x];
        if (tile.isRevealed()) return;
        tile.reveal();
        checkDidWin();
        if (tile.getValue() == 0) {
            forEachNeighboringTile(tile, neighbor -> reveal(neighbor.getX(), neighbor.getY()));
        } else if (didWin) {
            gameOverPhase(true);
        } else if (tile.getValue() < 0) {
            gameOverPhase(false);
        }
    }

    private void toggleFlag(int x, int y) {
        Tile tile = tiles[y][x];
        if (tile.isRevealed()) {
            return;
        }
        tile.toggleFlag();
    }

    private void checkDidWin() {
        AtomicBoolean didWin = new AtomicBoolean(true);
        forEachTile(tile -> {
            if (!tile.isRevealed() && !(tile.getValue() < 0)) didWin.set(false);
        });
        this.didWin = didWin.get();
    }

    private void gameOverPhase(boolean didWin) {
        isGameOver = true;
        if (didWin) {
            return;
        }
        forEachTile(tile -> {
            if (tile.getValue() == -1) {
                tile.reveal();
            }
        });
    }

    private void forEachTile(Consumer<Tile> consumer) {
        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                consumer.accept(tile);
            }
        }
    }

    private void forEachNeighboringTile(Tile tile, Consumer<Tile> consumer) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = j + tile.getX();
                int y = i + tile.getY();
                if (i == 0 && j == 0) continue;
                if (x < 0 || x >= width || y < 0 || y >= height) continue;
                consumer.accept(tiles[y][x]);
            }
        }
    }

    public char[][] getState() {
        char[][] state = new char[height][width];
        forEachTile(tile -> state[tile.getY()][tile.getX()] = tile.getState());
        return state;
    }

    public String getStringState(boolean includeCoords) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                char text = tiles[i][j].getState();
                string.append(text).append("  ");
            }
            string.append(includeCoords ? i + 1 : "").append("\n");
        }
        for (int j = 0; j < this.width; j++) {
            string.append(includeCoords ? j + 1 : "").append("  ");
        }
        return string.toString();
    }


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                char text = tiles[i][j].getCharValue();
                string.append(text).append("  ");
            }
            string.append(i + 1).append("\n");
        }
        for (int j = 0; j < this.width; j++) {
            string.append(j + 1).append("  ");
        }
        return string.toString();
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isDidWin() {
        return didWin;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMines() {
        return mines;
    }
}
