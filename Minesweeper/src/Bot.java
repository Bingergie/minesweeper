import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Bot {

    private static class Tile {
        public final int x;
        public final int y;
        public final char value;

        public Tile(int x, int y, char value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    private static class Coordinate {
        public final int x;
        public final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Move {
        public final int x;
        public final int y;
        public final Board.Action action;

        public Move(int x, int y, Board.Action action) {
            this.x = x;
            this.y = y;
            this.action = action;
        }
    }

    private Tile[][] boardState;
    private float[][] weightMap;
    private final List<Move> movesBuffer;

    public Bot() {
        boardState = null;
        movesBuffer = new ArrayList<>();
    }

    // only for debugging
    public Move getMove(char[][] boardState, boolean noBuffer) {
        return generateMoves().get(0);
    }

    public Move getMove(char[][] boardState) {
        this.boardState = new Tile[boardState.length][boardState[0].length];
        for (int i = 0; i < boardState.length; i++) {
            for (int j = 0; j < boardState[0].length; j++) {
                this.boardState[i][j] = new Tile(j, i, boardState[i][j]);
            }
        }

        if (movesBuffer.size() > 0) {
            return movesBuffer.remove(0);
        }

        // if there are no moves in the buffer, generate new moves
        movesBuffer.addAll(generateMoves());
        if (movesBuffer.size() == 0) {
            System.out.println("No moves generated, generating random move");
            // if there are still no moves in the buffer, generate a random move
            movesBuffer.add(new Move((int) (Math.random() * boardState[0].length), (int) (Math.random() * boardState.length), Board.Action.REVEAL));
        }
        return getMove(boardState);
    }

    private List<Move> generateMoves() {
        List<Move> moves = new ArrayList<>();

        forEachTile(tile -> {
            if (tile.value != '-' && tile.value != 'B' && tile.value != '0') {
                // if the tile is already revealed, check if the number of flags around it is equal to the number on the tile
                AtomicInteger flagCount = new AtomicInteger(0);
                AtomicInteger hiddenNeighbors = new AtomicInteger(0);
                forEachNeighboringTile(tile, neighbor -> {
                    if (neighbor.value == 'B') {
                        flagCount.getAndIncrement();
                    }
                    if (neighbor.value == '-') {
                        hiddenNeighbors.getAndIncrement();
                    }
                });
                // if the number of flags around the tile is equal to the number on the tile, reveal all the tiles around it
                if (flagCount.get() == tile.value - '0' && hiddenNeighbors.get() > 0) {
                    forEachNeighboringTile(tile, neighbor -> {
                        if (neighbor.value == '-') {
                            moves.add(new Move(neighbor.x, neighbor.y, Board.Action.REVEAL));
                        }
                    });
                } else {
                    // if the number of flags and hidden neighbors around the tile is equal to the number on the tile, flag all the tiles around it
                    if (flagCount.get() + hiddenNeighbors.get() == tile.value - '0') {
                        forEachNeighboringTile(tile, neighbor -> {
                            if (neighbor.value == '-') {
                                moves.add(new Move(neighbor.x, neighbor.y, Board.Action.FLAG));
                            }
                        });
                    }
                }
            }
        });

        // find and remove duplicates in the buffer
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            for (int j = i + 1; j < moves.size(); j++) {
                Move otherMove = moves.get(j);
                if (move.x == otherMove.x && move.y == otherMove.y) {
                    moves.remove(j);
                    j--;
                }
            }
        }
        // if there are no moves in the buffer, do magic
        if (moves.size() == 0) {
            System.out.println("doing magic");
            // generate a weight for each tile
            weightMap = new float[boardState.length][boardState[0].length];
            forEachTile(tile -> {
                if (tile.value == '0') {
                    weightMap[tile.y][tile.x] = -1;
                } else if (tile.value != '-' && tile.value != 'B') {
                    AtomicReference<Float> totalWeight = new AtomicReference<>((float) (tile.value - '0'));
                    List<Coordinate> hiddenNeighbors = new ArrayList<>();
                    forEachNeighboringTile(tile, neighbor -> {
                        if (neighbor.value == '-' || neighbor.value == 'B') {
                            if (weightMap[neighbor.y][neighbor.x] == 0) hiddenNeighbors.add(new Coordinate(neighbor.x, neighbor.y));
                            totalWeight.updateAndGet(v -> v - weightMap[neighbor.y][neighbor.x]);
                        }
                    });
                    // equally distribute the remaining weight of the tile to all the hidden neighbors
                    float weight = totalWeight.get() / hiddenNeighbors.size();
                    hiddenNeighbors.forEach(coordinate -> {
                        weightMap[coordinate.y][coordinate.x] += weight;
                        float tileWeight = weightMap[coordinate.y][coordinate.x];
                        if (tileWeight >= 1) {
                            moves.add(new Move(coordinate.x, coordinate.y, Board.Action.FLAG));
                        } else if (tileWeight <= 0 && tileWeight > -1) {
                            moves.add(new Move(coordinate.x, coordinate.y, Board.Action.REVEAL));
                        }
                    });
                }
            });
            printWeightMap();
        }

        return moves;
    }

    private void printWeightMap() {
        for (int i = 0; i < weightMap.length; i++) {
            for (int j = 0; j < weightMap[0].length; j++) {
                System.out.print(weightMap[i][j] + " ");
            }
            System.out.println();
        }
    }

    private void generateWeightMap() {
        weightMap = new float[boardState.length][boardState[0].length];
        forEachTile(tile -> {
            if (tile.value == '-' || tile.value == 'B' || tile.value == '0') {
                weightMap[tile.y][tile.x] = -1;
            } else {
                AtomicReference<Float> totalWeight = new AtomicReference<>((float) (tile.value - '0'));
                List<Coordinate> hiddenNeighbors = new ArrayList<>();
                forEachNeighboringTile(tile, neighbor -> {
                    if (neighbor.value == '-') {
                        hiddenNeighbors.add(new Coordinate(neighbor.x, neighbor.y));
                        totalWeight.updateAndGet(v -> v - weightMap[neighbor.y][neighbor.x]);
                    }
                });
                if (hiddenNeighbors.size() > 0) {
                    // equally distribute the remaining weight of the tile to all the hidden neighbors
                    float weight = totalWeight.get() / hiddenNeighbors.size();
                    hiddenNeighbors.forEach(coordinate -> weightMap[coordinate.y][coordinate.x] += weight);
                }
            }
        });
    }

    private void forEachTile(Consumer<Tile> consumer) {
        for (Tile[] row : boardState) {
            for (Tile tile : row) {
                consumer.accept(tile);
            }
        }
    }

    private void forEachNeighboringTile(Tile tile, Consumer<Tile> consumer) {
        for (int i = -1; i <= 1; i++) {
            if (tile.y + i < 0 || tile.y + i >= boardState.length) continue;
            for (int j = -1; j <= 1; j++) {
                if (tile.x + j < 0 || tile.x + j >= boardState[0].length) continue;
                if (i == 0 && j == 0) continue;
                consumer.accept(boardState[tile.y + i][tile.x + j]);
            }
        }
    }
}
