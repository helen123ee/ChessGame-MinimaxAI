// ========================= src/model/pieces/King.java =========================
package model.pieces;


import model.board.*;
import java.util.*;


public class King extends Piece {
public King(Board b, boolean w){ super(b,w); }
@Override public String getSymbol(){ return "K"; }
@Override public Piece copyFor(Board newBoard){ King k = new King(newBoard, isWhite); k.moved=this.moved; return k; }


@Override public List<Position> getPossibleMoves(){
List<Position> s = new ArrayList<>();
for(int dr=-1; dr<=1; dr++) for(int dc=-1; dc<=1; dc++) if(dr!=0||dc!=0){
int r=position.getRow()+dr, c=position.getColumn()+dc;
addIfFreeOrEnemy(s, r, c);
}
// Roques tratados no controller.Game
return s;
}
}