package net.loveruby.cflat.utils;
import java.util.*;

public class Cursor implements Iterator {
    protected List list;
    protected int index;

    public Cursor(List list) {
        this(list, 0);
    }

    protected Cursor(List list, int index) {
        this.list = list;
        this.index = index;
    }

    public Object clone() {
        return new Cursor(list, index);
    }

    public Cursor dup() {
        return new Cursor(list, index);
    }

    public boolean hasNext() {
        return index < list.size();
    }

    public Object next() {
        return list.get(index++);
    }

    public Object current() {
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
