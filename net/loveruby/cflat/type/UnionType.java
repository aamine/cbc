package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import java.util.*;

public class UnionType extends ComplexType {
    public UnionType(String name, List membs) {
        super(name, membs);
    }

    public long alignmemt() {
        // platform depedent
        return 1;
    }

    protected void computeOffsets() {
        long max = 0;
        Iterator membs = members.iterator();
        while (membs.hasNext()) {
            Slot s = (Slot)membs.next();
            s.setOffset(0);
            if (align(s.size()) > max) {
                max = align(s.size());
            }
        }
        size = max;
    }

    public boolean isUnion() {
        return true;
    }

    public String textize() {
        return "union " + name;
    }
}
