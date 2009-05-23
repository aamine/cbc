import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import static net.loveruby.cflat.utils.ListUtils.*;

public class TestListUtils {
    static public void main(String[] args) {
        JUnitCore.main(TestListUtils.class.getName());
    }

    @Test public void test_reverse() {
        assertEquals("reverse #1", list(), reverse(list()));
        assertEquals("reverse #2", list(1), reverse(list(1)));
        assertEquals("reverse #3", list(2,1), reverse(list(1,2)));
        assertEquals("reverse #4", list(3,2,1), reverse(list(1,2,3)));
    }

    private List<Integer> list(int... values) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i : values) {
            result.add(i);
        }
        return result;
    }
}
