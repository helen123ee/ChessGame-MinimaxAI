// ========================= src/model/pieces/Rook.java =========================
package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public String getSymbol() {
        return "R";
    }

    /** Your Piece hierarchy expects getPossibleMoves() with no parameters. */
    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        Position from = getPosition();
        if (from == null) return moves;

        // Four orthogonal rays: up, down, left, right
        addRay(moves, from, -1,  0); // up
        addRay(moves, from,  1,  0); // down
        addRay(moves, from,  0, -1); // left
        addRay(moves, from,  0,  1); // right
        return moves;
    }

    /** Required by Board.copy(): clone this piece for a different Board. */
    @Override
    public Piece copyFor(Board newBoard) {
        // If your Piece tracks "moved" state and exposes a getter, copy it here.
        // For now we just clone color/board; Board.copy() will set the new position.
        return new Rook(newBoard, this.isWhite());
    }

    private void addRay(List<Position> acc, Position from, int dRow, int dCol) {
        int r = from.getRow();
        int c = from.getColumn();

        while (true) {
            r += dRow;
            c += dCol;

            // bounds check without depending on Board.isInside
            if (r < 0 || r > 7 || c < 0 || c > 7) break;

            Position to = new Position(r, c);
            Piece occ = board.get(to); // use Board.get(Position)

            if (occ == null) {
                acc.add(to);
            } else {
                if (occ.isWhite() != this.isWhite()) {
                    acc.add(to); // capture
                }
                break; // blocked
            }
        }
    }
}