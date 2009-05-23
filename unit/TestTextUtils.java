import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;
import static net.loveruby.cflat.utils.TextUtils.*;

public class TestTextUtils {
    static public void main(String[] args) {
        JUnitCore.main(TestTextUtils.class.getName());
    }

    @Test public void test_dumpString() {
        assertEquals("dumpString #1", "\"\"", dumpString(""));
        assertEquals("dumpString #2", "\"x\"", dumpString("x"));
        assertEquals("dumpString #3", "\"xx\"", dumpString("xx"));
        assertEquals("dumpString #4", "\"x\\\"x\"", dumpString("x\"x"));
        assertEquals("dumpString #5", "\"x\\nx\"", dumpString("x\nx"));
    }

    @Test public void test_isPrintable() {
        assertEquals(true, isPrintable('a'));
        assertEquals(true, isPrintable('A'));
        assertEquals(true, isPrintable(' '));
        assertEquals(false, isPrintable('\t'));
        assertEquals(false, isPrintable('\r'));
        assertEquals(false, isPrintable('\n'));
        assertEquals(false, isPrintable('\0'));
    }
}
