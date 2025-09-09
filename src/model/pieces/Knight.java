// ========================= src/model/pieces/Knight.java =========================
package model.pieces;


import model.board.*;
import java.util.*;


public class Knight extends Piece {
public Knight(Board b, boolean w){ super(b,w);} @Override public String getSymbol(){ return "N"; }
@Override public Piece copyFor(Board newBoard){ Knight r=new Knight(newBoard, isWhite); r.moved=this.moved; return r; }


@Override public List<Position> getPossibleMoves(){
List<Position> s = new ArrayList<>();
int[][] d={{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
for(int[] v:d){ int r=position.getRow()+v[0], c=position.getColumn()+v[1]; addIfFreeOrEnemy(s,r,c);} return s;
}
}