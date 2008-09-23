package net.loveruby.cflat.utils;
import java.util.*;

public class ClonableIterator implements Iterator {
    protected List list;
    protected int index;

    public ClonableIterator(List list) {
        this(list, 0);
    }

    protected ClonableIterator(List list, int index) {
        this.list = list;
        this.index = index;
    }

    public Object clone() {
        return new ClonableIterator(list, index);
    }

    public ClonableIterator dup() {
        return new ClonableIterator(list, index);
    }

    public boolean hasNext() {
        return index < list.size();
    }

    public Object next() {
        return list.get(index++);
    }

    public void remove() {
        list.remove(index);
    }

    public String toString() {
        return "#<ClonableIterator list=" + list + " index=" + index + ">";
    }
}
