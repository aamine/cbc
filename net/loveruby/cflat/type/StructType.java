package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.asm.Assembler;
import java.util.*;

public class StructType extends CompositeType {
    public StructType(String name, List membs, Location loc) {
        super(name, membs, loc);
    }

    public boolean isStruct() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isStruct()) return false;
        return equals(other.getStructType());
    }

    protected void computeOffsets() {
        long offset = 0;
        long maxAlign = 1;
        Iterator membs = members();
        while (membs.hasNext()) {
            Slot s = (Slot)membs.next();
            offset = Assembler.align(offset, s.allocSize());
            s.setOffset(offset);
            offset += s.allocSize();
            maxAlign = Math.max(maxAlign, s.alignment());
        }
        cachedSize = Assembler.align(offset, maxAlign);
        cachedAlign = maxAlign;
    }

    public String toString() {
        return "struct " + name;
    }
}
