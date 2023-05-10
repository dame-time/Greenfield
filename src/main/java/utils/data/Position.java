package utils.data;

import java.util.Objects;

public final class Position<T, U> {
    private T x;
    private U y;

    public Position(T x, U y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public U getY() {
        return y;
    }

    public void setX(T x) {
        this.x = x;
    }

    public void setY(U y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Position) obj;
        return Objects.equals(this.x, that.x) &&
                Objects.equals(this.y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position[" +
                "x=" + x + ", " +
                "y=" + y + ']';
    }
}
