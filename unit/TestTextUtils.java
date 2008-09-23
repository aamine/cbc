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
    }

    @Test public void test_isPrintable() {
    }
}
