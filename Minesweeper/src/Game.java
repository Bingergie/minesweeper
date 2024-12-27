import java.util.Objects;
import java.util.Scanner;

public class Game {
    protected final Board board;

    public Game(int width, int height, int numMines) {
        this.board = new Board(width, height, numMines);
    }

    public void start() {
        System.out.println(board.getStringState(true));
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter 'r' for reveal or 'f' for flag: ");
            String input = scanner.nextLine();

            if (Objects.equals(input, "r") || Objects.equals(input, "f")) {
                try {
                    int[] inputCoords = getInput();
                    int y = inputCoords[1];
                    int x = inputCoords[0];
                    if (Objects.equals(input, "r")) {
                        board.act(x, y, Board.Action.REVEAL);
                    } else {
                        board.act(x, y, Board.Action.FLAG);
                    }
                } catch (Exception e) {
                    fku();
                }
            } else {
                fku();
            }

            System.out.println(board.getStringState(true));
            if (board.isGameOver()) {
                System.out.println(board.isDidWin() ? "You win! lets gooooooo" : "Game over, u fking loose");
                break;
            }
        }
    }

    private int[] getInput() {
        int x = 0;
        int y = 0;
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter x: ");
            x = scanner.nextInt() - 1;
            System.out.print("Enter y: ");
            y = scanner.nextInt() - 1;
        } catch (Exception e) {
            fku();
        }
        return new int[]{x, y};
    }

    private static void fku() {
        System.out.println("Invalid input fk u");
    }
}
