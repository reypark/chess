package client;

import java.util.Scanner;

public class Repl {
    private final Scanner in = new Scanner(System.in);

    public void run() {
        printWelcome();
        while (true) {
            System.out.print("Chess Login >>> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;

            String cmd = line.split("\\s+")[0].toLowerCase();
            switch (cmd) {
                case "h", "help" -> printWelcome();
                case "q", "quit" -> {
                    System.out.println("see you later");
                    return;
                }
                default -> System.out.println("Unknown command: " + cmd);
            }
        }
    }

    private void printWelcome() {
        System.out.println("♛ Welcome to Chess. Sign in to start. ♛");
        System.out.println();
        System.out.println("Options:");
        System.out.println("Login as an existing user: \"l\", \"login\"     <USERNAME> <PASSWORD>");
        System.out.println("Register a new user: \"r\", \"register\"    <USERNAME> <PASSWORD> <EMAIL>");
        System.out.println("Exit the program: \"q\", \"quit\"");
        System.out.println("Print this message: \"h\", \"help\"");
        System.out.println();
    }
}
