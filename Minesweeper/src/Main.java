import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter 'play' or 'bot': ");
        String input = scanner.nextLine();

        if (input.equals("play")) {
            Game game = new Game(9, 9, 10);
            game.start();
        } else if (input.equals("bot")) {
            BotGame botGame = new BotGame(9, 9, 10);
            botGame.start();
        } else {
            fku();
        }
    }
    private static void fku() {
        System.out.println("Invalid input fk u");
    }
}