import net.loveruby.cflat.utils.ClonableIterator;
import java.util.*;
import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;

public class TestClonableIterator {
    static public void main(String[] args) {
        JUnitCore.main(TestClonableIterator.class.getName());
    }

    @Test public void hasNext_0() {
        List<Integer> list = new ArrayList<Integer>();
        ClonableIterator it = new ClonableIterator(list);
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void hasNext_1() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        ClonableIterator it = new ClonableIterator(list);
        assertEquals("[1].hasNext #1", true, it.hasNext());
        assertEquals("[1].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void hasNext_2() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        ClonableIterator it = new ClonableIterator(list);
        assertEquals("[1,2].hasNext #1", true, it.hasNext());
        assertEquals("[1,2].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[2].hasNext #1", true, it.hasNext());
        assertEquals("[2].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void next() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        ClonableIterator it = new ClonableIterator(list);
        assertEquals("[1,2].next", 1, it.next());
        assertEquals("[2].next", 2, it.next());
    }

    @Test public void dup() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        ClonableIterator it = new ClonableIterator(list);
        it.next();
        ClonableIterator it2 = it.dup();
        assertEquals("dup+hasNext #1", true, it2.hasNext());
        assertEquals("dup+next #1", 2, it2.next());
    }
}
