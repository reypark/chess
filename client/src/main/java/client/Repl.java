package client;

import java.util.Arrays;
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
            while (authToken == null) {
                System.out.print("\nPre-login >>> ");
                String line = scanner.nextLine().trim();
                handlePrelogin(line);
            }
            postLoginLoop(scanner);
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

    private void postLoginLoop(Scanner scanner) {
        System.out.println("\n You're in.");
        showPostLoginHelp();
        while (true) {
            System.out.print("\nChess >>> ");
            String line = scanner.nextLine().trim();
            if (handlePostlogin(line)) {
                break;
            }
        }
    }

    private void showPostLoginHelp() {
        System.out.println("Create game: \"c\", \"create\"    <GAME_NAME>");
        System.out.println("List games: \"l\", \"list\"");
        System.out.println("Join game: \"p\", \"play\"    <NUMBER> <WHITE|BLACK>");
        System.out.println("Observe game: \"o\", \"observe\"   <NUMBER>");
        System.out.println("Logout: \"x\", \"logout\"");
        System.out.println("Quit: \"q\", \"quit\"");
        System.out.println("Help: \"h\", \"help\"");
    }

    private boolean handlePostlogin(String input) {
        if (input.isEmpty()) return false;
        String[] parts = input.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "c", "create" -> handleCreateGame(parts);
            case "l", "list" -> handleListGames();
            case "j", "join" -> handleJoinGame(parts);
            case "o", "observe" -> handleObserveGame(parts);
            case "x", "logout" -> { handleLogout(); return true; }
            case "q", "quit"   -> handleQuit();
            case "h", "help" -> showPostLoginHelp();
            default -> System.out.println("Unknown command. Type \"h\" for help.");
        }
        return false;
    }
}
