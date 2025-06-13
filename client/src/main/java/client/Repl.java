package client;

import java.util.Scanner;
import model.AuthData;

public class Repl {
    private final ServerFacade facade;
    private String authToken = null;
    private boolean running = true;

    public Repl(int serverPort) {
        this.facade = new ServerFacade(serverPort);
    }

    public void run() {
        printWelcome();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                System.out.print("\nChess Login >>> ");
                String line = scanner.nextLine().trim();
                handlePrelogin(line);
            }
        }
    }

    private void printWelcome() {
        System.out.println("♔ Welcome to Chess. Sign in to start. ♔\n");
        showHelp();
    }

    private void showHelp() {
        System.out.println("Options:");
        System.out.println("Login as an existing user: \"l\", \"login\"     <USERNAME> <PASSWORD>");
        System.out.println("Register a new user: \"r\", \"register\"    <USERNAME> <PASSWORD> <EMAIL>");
        System.out.println("Exit the program: \"q\", \"quit\"");
        System.out.println("Print this message: \"h\", \"help\"");
    }

    private void handlePrelogin(String input) {
        if (input.isEmpty()) return;
        String[] parts = input.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "h", "help" -> showHelp();
            case "q", "quit" -> handleQuit();
            case "l", "login" -> handleLogin(parts);
            case "r", "register" -> handleRegister(parts);
            default -> System.out.println("Unknown command. Type \"h\" or \"help\".");
        }
    }

    private void handleQuit() {
        System.out.println("see you later");
        running = false;
    }

    private void handleLogin(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: login <username> <password>");
            return;
        }
        try {
            AuthData auth = facade.login(parts[1], parts[2]);
            authToken = auth.authToken();
            System.out.println("Logged in as " + auth.username() + "!");
            running = false;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Usage: register <username> <password> <email>");
            return;
        }
        try {
            AuthData auth = facade.register(parts[1], parts[2], parts[3]);
            authToken = auth.authToken();
            System.out.println("Registered & logged in as " + auth.username() + "!");
            running = false;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
