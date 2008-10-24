import net.loveruby.cflat.utils.Cursor;
import java.util.*;
import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;

public class TestCursor {
    static public void main(String[] args) {
        JUnitCore.main(TestCursor.class.getName());
    }

    @Test public void test_hasNext_0() {
        List<Integer> list = new ArrayList<Integer>();
        Cursor<Integer> it = new Cursor<Integer>(list);
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void test_hasNext_1() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        Cursor<Integer> it = new Cursor<Integer>(list);
        assertEquals("[1].hasNext #1", true, it.hasNext());
        assertEquals("[1].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void test_hasNext_2() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        Cursor<Integer> it = new Cursor<Integer>(list);
        assertEquals("[1,2].hasNext #1", true, it.hasNext());
        assertEquals("[1,2].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[2].hasNext #1", true, it.hasNext());
        assertEquals("[2].hasNext #2", true, it.hasNext());
        it.next();
        assertEquals("[].hasNext #1", false, it.hasNext());
        assertEquals("[].hasNext #2", false, it.hasNext());
    }

    @Test public void test_next() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        Cursor<Integer> it = new Cursor<Integer>(list);
        assertEquals("[1,2].next", 1, (Object)it.next());
        assertEquals("[2].next", 2, (Object)it.next());
    }

    @Test public void test_clone() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        Cursor<Integer> it = new Cursor<Integer>(list);
        it.next();
        Cursor<Integer> it2 = it.clone();
        assertEquals("clone+hasNext #1", true, (Object)it2.hasNext());
        assertEquals("clone+next #1", 2, (Object)it2.next());
    }
}
