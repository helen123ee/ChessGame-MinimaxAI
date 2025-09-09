// ========================= src/model/pieces/Bishop.java =========================
package model.pieces;


import model.board.*;
import java.util.*;


public class Bishop extends Piece {
public Bishop(Board b, boolean w){ super(b,w);} @Override public String getSymbol(){ return "B"; }
@Override public Piece copyFor(Board newBoard){ Bishop r=new Bishop(newBoard, isWhite); r.moved=this.moved; return r; }


@Override public List<Position> getPossibleMoves(){
List<Position> s = new ArrayList<>();
ray(s,-1,-1); ray(s,-1,1); ray(s,1,-1); ray(s,1,1);
return s;
}
private void ray(List<Position> s, int dr, int dc){
int r=position.getRow()+dr, c=position.getColumn()+dc;
while(new Position(r,c).isValid()){
Position p=new Position(r,c); var q=board.get(p); s.add(p);
if(q!=null){ if(q.isWhite()==this.isWhite) s.remove(s.size()-1); break; }
r+=dr; c+=dc;
}
}
}