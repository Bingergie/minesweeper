import java.util.Scanner;

public class BotGame extends Game {

    private final Bot bot;

    public BotGame(int width, int height, int numMines) {
        super(width, height, numMines);
        this.bot = new Bot();
    }

    public void start() {
        System.out.println(board.getStringState(false));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            scanner.nextLine();
            Bot.Move botMove = bot.getMove(board.getState());
            board.act(botMove.x, botMove.y, botMove.action);
            if (board.isGameOver()) {
                System.out.println(board.isDidWin() ? "You win! lets gooooooo" : "Game over, u fking loose");
                System.out.println(board.getStringState(false));
                break;
            }
            System.out.println(board.getStringState(false));
        }

    }
}
