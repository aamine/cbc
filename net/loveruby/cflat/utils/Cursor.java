package net.loveruby.cflat.utils;
import java.util.*;

public class Cursor<T> implements Iterator {
    protected List<T> list;
    protected int index;

    public Cursor(List<T> list) {
        this(list, 0);
    }

    protected Cursor(List<T> list, int index) {
        this.list = list;
        this.index = index;
    }

    public Cursor<T> clone() {
        return new Cursor<T>(list, index);
    }

    public boolean hasNext() {
        return index < list.size();
    }

    public T next() {
        return list.get(index++);
    }

    public T current() {
        if (index == 0) {
            throw new Error("must not happen: Cursor#current");
        }
        return list.get(index - 1);
    }

    public void remove() {
        list.remove(index);
    }

    public String toString() {
        return "#<Cursor list=" + list + " index=" + index + ">";
    }
}
