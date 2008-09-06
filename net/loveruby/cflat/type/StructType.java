package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import net.loveruby.cflat.ast.Location;
import java.util.*;

public class StructType extends ComplexType {
    public StructType(String name, List membs, Location loc) {
        super(name, membs, loc);
    }

    public boolean isStruct() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isStruct()) return false;
        return equals(other.getStructType());
    }

    public long alignment() {
        if (members.isEmpty()) {
            return 1;
        } else {
            Slot s = (Slot)members.get(0);
            return s.type().alignment();
        }
    }

    protected void computeOffsets() {
        long offset = 0;
        Iterator membs = members();
        while (membs.hasNext()) {
            Slot s = (Slot)membs.next();
            s.setOffset(offset);
            offset = align(offset + s.size());
        }
        size = offset;
    }

    public String toString() {
        return "struct " + name;
    }
}
