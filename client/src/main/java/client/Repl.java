package client;

import java.util.Scanner;

public class Repl {
    private enum State {LOGGED_OUT, LOGGED_IN}
    private State state = State.LOGGED_OUT;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        printWelcome();

        while (running) {
            System.out.print("[" + state + "] >>> ");
            String line = scanner.nextLine().trim().toLowerCase();

            switch (line) {
                case "help":
                    printHelp();
                    break;
                case "quit":
                    running = false;
                    break;
            }
        }

        scanner.close();
    }

    private void printWelcome() {
        System.out.println("Welcome to 240 Chess. Type Help to get started.");
    }

    private void printHelp() {
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("quit - playing chess");
        System.out.println("help - with possible commands");
    }
}
