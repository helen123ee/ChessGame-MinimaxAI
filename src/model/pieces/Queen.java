// ========================= src/model/pieces/Queen.java =========================
package model.pieces;


import model.board.*;
import java.util.*;


public class Queen extends Piece {
public Queen(Board b, boolean w){ super(b,w);} @Override public String getSymbol(){ return "Q"; }
@Override public Piece copyFor(Board newBoard){ Queen q=new Queen(newBoard, isWhite); q.moved=this.moved; return q; }


@Override public List<Position> getPossibleMoves(){
List<Position> s = new ArrayList<>();
ray(s,-1,0); ray(s,1,0); ray(s,0,-1); ray(s,0,1);
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