import chess.*;
import dataaccess.DataAccessException;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        try {
            Server server = new Server();
            int port = server.run(8080);
            System.out.println("Listening on port " + port);
        } catch (DataAccessException dae) {
            System.err.println("FATAL: unable to initialize database: " + dae.getMessage());
            dae.printStackTrace();
            System.exit(1);
        }
    }
}