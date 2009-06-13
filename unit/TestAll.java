import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.*;

@RunWith(Suite.class)
@SuiteClasses({
    TestCursor.class,
    TestTextUtils.class,
    TestAsmUtils.class,
    TestListUtils.class
})
public class TestAll {
    static public void main(String[] args) {
        JUnitCore.main(TestAll.class.getName());
    }
}
