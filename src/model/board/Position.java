// ========================= src/model/board/Position.java =========================
package model.board;


import java.util.Objects;


public class Position {
private int row;
private int column;


public Position(int row, int column) {
this.row = row; this.column = column;
}
public int getRow() { return row; }
public void setRow(int row) { this.row = row; }
public int getColumn() { return column; }
public void setColumn(int column) { this.column = column; }


public boolean isValid() { return row >= 0 && row < 8 && column >= 0 && column < 8; }


@Override public boolean equals(Object o) {
if (this == o) return true; if (!(o instanceof Position)) return false;
Position that = (Position) o; return row == that.row && column == that.column;
}
@Override public int hashCode() { return Objects.hash(row, column); }
@Override public String toString() { return (char)('a'+column) + String.valueOf(8 - row); }
}