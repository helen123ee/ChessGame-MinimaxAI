// ========================= src/model/board/Move.java =========================
package model.board;


import model.pieces.Piece;


public class Move {
private final Position from;
private final Position to;
private final Piece moved;
private final Piece captured;
private final boolean castleKingSide;
private final boolean castleQueenSide;
private final boolean enPassant;
private final Character promotion; // 'Q','R','B','N' ou null


public Move(Position from, Position to, Piece moved, Piece captured,
boolean castleKingSide, boolean castleQueenSide, boolean enPassant, Character promotion) {
this.from = from; this.to = to; this.moved = moved; this.captured = captured;
this.castleKingSide = castleKingSide; this.castleQueenSide = castleQueenSide;
this.enPassant = enPassant; this.promotion = promotion;
}
public Position getFrom() { return from; }
public Position getTo() { return to; }
public Piece getMoved() { return moved; }
public Piece getCaptured() { return captured; }
public boolean isCastleKingSide() { return castleKingSide; }
public boolean isCastleQueenSide() { return castleQueenSide; }
public boolean isEnPassant() { return enPassant; }
public Character getPromotion() { return promotion; }
}