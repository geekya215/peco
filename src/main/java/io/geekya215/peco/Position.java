package io.geekya215.peco;

import java.util.Objects;

public final class Position {
    private Integer line;
    private Integer column;

    private Position() {
        line = 0;
        column = 0;
    }

    public static Position initialPosition() {
        return new Position();
    }

    public Position incrementLine() {
        line += 1;
        return this;
    }

    public Position incrementColumn() {
        column += 1;
        return this;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Position{" +
                "line=" + line +
                ", column=" + column +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (!Objects.equals(line, position.line)) return false;
        return Objects.equals(column, position.column);
    }

    @Override
    public int hashCode() {
        int result = line != null ? line.hashCode() : 0;
        result = 31 * result + (column != null ? column.hashCode() : 0);
        return result;
    }
}