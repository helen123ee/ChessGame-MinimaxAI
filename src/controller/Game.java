// ========================= src/controller/Game.java =========================
package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;
    private Boolean winnerWhite = null; // null enquanto não acabou

    // Casa-alvo para en passant (a casa "intermediária" após avanço de 2)
    private Position enPassantTarget = null;

    // Histórico simples (ex.: "e2e4", "O-O")
    private final List<String> history = new ArrayList<>();

    public Game() {
        this.board = new Board();
        setupPieces();
    }

    // ==== API usada pela GUI ====

    public Board board() { return board; }

    public boolean whiteToMove() { return whiteToMove; }

    public List<String> history() { return Collections.unmodifiableList(history); }

    public boolean isGameOver() { return gameOver; }

    /** Retorna true para Brancas, false para Pretas; null se o jogo não terminou. */
    public Boolean winnerWhite() { return winnerWhite; }

    /** Retorna "Brancas"/"Pretas" se houver vencedor, senão null. */
    public String winnerText() {
        if (!gameOver || winnerWhite == null) return null;
        return winnerWhite ? "Brancas" : "Pretas";
        }

    /** Retorna movimentos pseudo-legais (sem checar xeque). */
    public List<Position> legalMovesFrom(Position from) {
        Piece p = board.get(from);
        if (p == null) return List.of();
        if (p.isWhite() != whiteToMove) return List.of();
        // >>> ALTERAÇÃO: sua Piece expõe getPossibleMoves() SEM parâmetros
        return p.getPossibleMoves();
    }

    /** Verdadeiro se um peão que sai de 'from' e chega em 'to' promove. */
    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (!(p instanceof Pawn)) return false;
        if (p.isWhite()) return to.getRow() == 0;   // peão branco chegando na 8ª (topo)
        else              return to.getRow() == 7;  // peão preto chegando na 1ª (base)
    }

    /** Executa o lance (detecta roque, en passant e promoção). */
    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return; // não permite jogar após fim

        Piece p = board.get(from);
        if (p == null) return;
        if (p.isWhite() != whiteToMove) return;

        // -------- ROQUE --------
        boolean isKing = (p instanceof King);
        int dCol = Math.abs(to.getColumn() - from.getColumn());
        if (isKing && dCol == 2) {
            int row = from.getRow();

            // mover o rei
            board.set(to, p);
            board.set(from, null);

            if (to.getColumn() == 6) { // O-O (lado do rei)
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O");
            } else { // O-O-O (lado da dama)
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O-O");
            }

            p.setMoved(true);
            enPassantTarget = null; // roque limpa en passant
            // jogo não termina por roque; alterna a vez
            whiteToMove = !whiteToMove;
            return;
        }

        // -------- EN PASSANT --------
        boolean isPawn = (p instanceof Pawn);
        boolean diagonal = from.getColumn() != to.getColumn();
        boolean toIsEmpty = board.get(to) == null;
        boolean isEnPassant = isPawn && diagonal && toIsEmpty && to.equals(enPassantTarget);
        if (isEnPassant) {
            board.set(to, p);
            board.set(from, null);
            // remover peão capturado "atrás" do destino
            int dir = p.isWhite() ? 1 : -1;
            board.set(new Position(to.getRow() + dir, to.getColumn()), null);
            p.setMoved(true);
            addHistory(coord(from) + "x" + coord(to) + " e.p.");
            enPassantTarget = null; // só vale no lance imediatamente seguinte
            // alterna a vez (não há captura de rei por e.p.)
            whiteToMove = !whiteToMove;
            return;
        }

        // -------- LANCE NORMAL (com ou sem captura) --------
        Piece capturedBefore = board.get(to);

        board.set(to, p);
        board.set(from, null);
        p.setMoved(true);

        // -------- MARCA/RESSETA EN PASSANT --------
        if (isPawn && Math.abs(to.getRow() - from.getRow()) == 2) {
            int mid = (to.getRow() + from.getRow()) / 2;
            enPassantTarget = new Position(mid, from.getColumn());
        } else {
            enPassantTarget = null;
        }

        // -------- PROMOÇÃO --------
        if (promotion != null && isPawn && isPromotion(from, to)) {
            Piece np = switch (Character.toUpperCase(promotion)) {
                case 'R' -> new Rook(board, p.isWhite());
                case 'B' -> new Bishop(board, p.isWhite());
                case 'N' -> new Knight(board, p.isWhite());
                default  -> new Queen(board, p.isWhite());
            };
            np.setMoved(true);
            board.set(to, np);
        }

        // histórico simples (poderia virar SAN depois)
        addHistory(coord(from) + (capturedBefore != null ? "x" : "-") + coord(to));

        // -------- FIM DE JOGO POR CAPTURA DO REI --------
        if (capturedBefore instanceof King) {
            gameOver = true;
            winnerWhite = p.isWhite();
            return; // não alterna a vez após término
        }

        whiteToMove = !whiteToMove;
    }

    /** Indica se o lado passado está em xeque (stub por enquanto). */
    public boolean inCheck(boolean whiteSide) {
        // Implementação completa exige varrer movimentos do oponente até o rei.
        // Mantemos false para não travar a GUI. Posso implementar depois.
        return false;
    }

    /** Snapshot raso (usa Board.copy()). */
    public Game snapshot() {
        Game g = new Game();
        g.board = this.board.copy();
        g.whiteToMove = this.whiteToMove;
        g.gameOver = this.gameOver;
        g.winnerWhite = this.winnerWhite;
        g.enPassantTarget = (this.enPassantTarget == null)
                ? null
                : new Position(enPassantTarget.getRow(), enPassantTarget.getColumn());
        g.history.clear();
        g.history.addAll(this.history);
        return g;
    }

    // ==== utilidades ====

    private void addHistory(String moveStr) {
        history.add(moveStr);
    }

    private String coord(Position p) {
        // Converte (row,col) em notação "a1..h8" assumindo 0..7 de cima p/baixo
        char file = (char) ('a' + p.getColumn());
        int rank = 8 - p.getRow();
        return "" + file + rank;
    }

    /** Coloca as peças na posição inicial padrão. */
    private void setupPieces() {
        // Brancas embaixo (linhas 6 e 7)
        board.placePiece(new Rook(board, true),   new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true),  new Position(7, 3));
        board.placePiece(new King(board, true),   new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true),   new Position(7, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, true), new Position(6, c));
        }

        // Pretas em cima (linhas 0 e 1)
        board.placePiece(new Rook(board, false),   new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false),  new Position(0, 3));
        board.placePiece(new King(board, false),   new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false),   new Position(0, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, false), new Position(1, c));
        }
    }
}
