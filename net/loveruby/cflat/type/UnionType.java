package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import java.util.*;

public class UnionType extends ComplexType {
    public UnionType(String name, List membs) {
        super(name, membs);
    }

    public boolean isUnion() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isUnion()) return false;
        return equals(other.getUnionType());
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

    public String textize() {
        return "union " + name;
    }
}
