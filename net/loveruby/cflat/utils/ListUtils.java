package net.loveruby.cflat.utils;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

public abstract class ListUtils {
    static public <T> List<T> reverse(List<T> list) {
        List<T> result = new ArrayList<T>(list.size());
        ListIterator<T> it = list.listIterator(list.size());
        while (it.hasPrevious()) {
            result.add(it.previous());
        }
        return result;
    }
}
