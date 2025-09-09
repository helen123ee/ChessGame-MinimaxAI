// ========================= src/model/pieces/Pawn.java =========================
package model.pieces;


import model.board.*;
import java.util.*;


public class Pawn extends Piece {
public Pawn(Board b, boolean w){ super(b,w);} @Override public String getSymbol(){ return "P"; }
@Override public Piece copyFor(Board newBoard){ Pawn r=new Pawn(newBoard, isWhite); r.moved=this.moved; return r; }


@Override public List<Position> getPossibleMoves(){
List<Position> s = new ArrayList<>();
int dir = isWhite ? -1 : 1;
Position f1 = new Position(position.getRow()+dir, position.getColumn());
if(f1.isValid() && board.get(f1)==null){ s.add(f1);
Position f2 = new Position(position.getRow()+2*dir, position.getColumn());
if(!moved && f2.isValid() && board.get(f2)==null) s.add(f2);
}
Position l = new Position(position.getRow()+dir, position.getColumn()-1);
Position r = new Position(position.getRow()+dir, position.getColumn()+1);
if(l.isValid() && board.get(l)!=null && board.get(l).isWhite()!=isWhite) s.add(l);
if(r.isValid() && board.get(r)!=null && board.get(r).isWhite()!=isWhite) s.add(r);
return s; // en passant será tratado no controller
}


@Override public List<Position> getAttacks(){
List<Position> s = new ArrayList<>(); int dir = isWhite ? -1 : 1;
Position l = new Position(position.getRow()+dir, position.getColumn()-1);
Position r = new Position(position.getRow()+dir, position.getColumn()+1);
if(l.isValid()) s.add(l); if(r.isValid()) s.add(r); return s;
}
}