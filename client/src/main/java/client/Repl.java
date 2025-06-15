package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import model.AuthData;
import model.GameData;
import ui.BoardRenderer;
import chess.ChessGame;


public class Repl {
    private final ServerFacade facade;
    private String authToken = null;
    private boolean running = true;
    private List<GameData> lastGames = new ArrayList<>();


    public Repl(int serverPort) {
        this.facade = new ServerFacade(serverPort);
    }

    public void run() {
        facade.clearDatabase();
        printWelcome();
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                if (authToken == null) {
                    System.out.print("\nPre-login >>> ");
                    String line = scanner.nextLine().trim();
                    handlePrelogin(line);
                } else {
                    System.out.print("\nChess >>> ");
                    String line = scanner.nextLine().trim();
                    boolean justLoggedOut = handlePostlogin(line);
                    if (justLoggedOut) {
                        printWelcome();
                    }
                }
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
        System.out.println("See you later.");
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
            System.out.println("Logged in as " + auth.username());
            System.out.println("\nYou're in.");
            showPostLoginHelp();
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
            System.out.println("Registered and logged in as " + auth.username());
            System.out.println("\nYou're in.");
            showPostLoginHelp();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void showPostLoginHelp() {
        System.out.println("Create game: \"c\", \"create\"    <GAME_NAME>");
        System.out.println("List games: \"l\", \"list\"");
        System.out.println("Join game: \"j\", \"join\"    <NUMBER> <WHITE|BLACK>");
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

    private void handleCreateGame(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: create <game name>");
            return;
        }
        String gameName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        try {
            int gameId = facade.createGame(authToken, gameName);
            System.out.println("Created game \"" + gameName + "\" (ID " + gameId + ")");
            lastGames = facade.listGames(authToken);
        } catch (Exception e) {
            System.out.println("Error creating game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            lastGames = facade.listGames(authToken);
            if (lastGames.isEmpty()) {
                System.out.println("No games available.");
            } else {
                for (int i = 0; i < lastGames.size(); i++) {
                    GameData g = lastGames.get(i);
                    String white = g.whiteUsername()   != null ? g.whiteUsername() : "-";
                    String black = g.blackUsername()   != null ? g.blackUsername() : "-";
                    System.out.printf(
                            "%2d)  %-20s  [%s vs %s]%n",
                            i+1, g.gameName(), white, black
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing games: " + e.getMessage());
        }
    }

    private void handleJoinGame(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: play <number> <WHITE|BLACK>");
            return;
        }

        try {
            int idx = Integer.parseInt(parts[1]) - 1;
            if (idx < 0 || idx >= lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            String color = parts[2].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK.");
                return;
            }

            GameData selected = lastGames.get(idx);
            facade.joinGame(selected.gameID(), color, authToken);
            System.out.println("Joined game as " + color + ".");

            GameData state = facade.getGameState(authToken, selected.gameID());

            ChessGame.TeamColor pov = color.equals("WHITE")
                    ? ChessGame.TeamColor.WHITE
                    : ChessGame.TeamColor.BLACK;
            BoardRenderer.draw(state.game(), pov);

            running = false;

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error joining game: " + e.getMessage());
        }
    }

    private void handleObserveGame(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: observe <number>");
            return;
        }

        try {
            int idx = Integer.parseInt(parts[1]) - 1;
            if (idx < 0 || idx >= lastGames.size()) {
                System.out.println("Invalid game number.");
                return;
            }

            GameData selected = lastGames.get(idx);
            System.out.println("Observing game \"" + selected.gameName() + "\"…");
            GameData state = facade.getGameState(authToken, selected.gameID());
            BoardRenderer.draw(state.game(), ChessGame.TeamColor.WHITE);

            running = false;

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            facade.logout(authToken);
            System.out.println("Logged out successfully.");
            printWelcome();
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
        authToken = null;
    }
}
