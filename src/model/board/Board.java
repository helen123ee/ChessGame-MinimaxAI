// ========================= src/model/board/Board.java =========================
package model.board;

import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] grid = new Piece[8][8];

    /** Verifica se a posição está dentro do tabuleiro (0..7). */
    public boolean isInside(Position p) {
        return p != null && p.isValid();
    }

    public Piece get(Position p) {
        return isInside(p) ? grid[p.getRow()][p.getColumn()] : null;
    }

    public void set(Position p, Piece piece) {
        if (!isInside(p)) return;
        grid[p.getRow()][p.getColumn()] = piece;
        if (piece != null) {
            piece.setPosition(p);
        }
    }

    /** Atalho usado no setup inicial. */
    public void placePiece(Piece piece, Position p) {
        set(p, piece);
    }

    /** Lista todas as peças de uma cor. */
    public List<Piece> pieces(boolean white) {
        List<Piece> out = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece pc = grid[r][c];
                if (pc != null && pc.isWhite() == white) out.add(pc);
            }
        }
        return out;
    }

    /** Cópia profunda do tabuleiro (clona peças para o novo Board). */
    public Board copy() {
        Board b = new Board();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    Piece cp = p.copyFor(b);
                    b.grid[r][c] = cp;
                    cp.setPosition(new Position(r, c));
                }
            }
        }
        return b;
    }
}
