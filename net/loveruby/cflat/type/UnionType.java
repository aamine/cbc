package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.asm.Assembler;
import java.util.*;

public class UnionType extends ComplexType {
    public UnionType(String name, List membs, Location loc) {
        super(name, membs, loc);
    }

    public boolean isUnion() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isUnion()) return false;
        return equals(other.getUnionType());
    }

    protected void computeOffsets() {
        long maxSize = 0;
        long maxAlign = 1;
        Iterator membs = members.iterator();
        while (membs.hasNext()) {
            Slot s = (Slot)membs.next();
            s.setOffset(0);
            maxSize = Math.max(maxSize, s.allocSize());
            maxAlign = Math.max(maxAlign, s.alignment());
        }
        cachedSize = Assembler.align(maxSize, maxAlign);
        cachedAlign = maxAlign;
    }

    public String toString() {
        return "union " + name;
    }
}
