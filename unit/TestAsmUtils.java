import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;
import static net.loveruby.cflat.utils.AsmUtils.*;

public class TestAsmUtils {
    static public void main(String[] args) {
        JUnitCore.main(TestAsmUtils.class.getName());
    }

    @Test public void test_align() {
        assertEquals(0, align(0, 4));
        assertEquals(4, align(1, 4));
        assertEquals(4, align(2, 4));
        assertEquals(4, align(3, 4));
        assertEquals(4, align(4, 4));
        assertEquals(8, align(5, 4));
        assertEquals(8, align(6, 4));
        assertEquals(8, align(7, 4));
        assertEquals(8, align(8, 4));
        assertEquals(12, align(9, 4));

        assertEquals(0, align(0, 8));
        assertEquals(8, align(1, 8));
        assertEquals(8, align(2, 8));
        assertEquals(8, align(7, 8));
        assertEquals(8, align(8, 8));
        assertEquals(16, align(9, 8));
        assertEquals(16, align(16, 8));
        assertEquals(24, align(17, 8));

        assertEquals(0, align(0, 16));
        assertEquals(16, align(1, 16));
        assertEquals(16, align(16, 16));
        assertEquals(32, align(17, 16));
    }
}
