package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Slot;
import java.util.*;

public class StructType extends ComplexType {
    public StructType(String name, List membs) {
        super(name, membs);
    }

    public boolean isStruct() { return true; }

    public boolean isSameType(Type other) {
        if (! other.isStruct()) return false;
        return equals(other.getStructType());
    }

    public long alignment() {
        if (members.isEmpty()) {
            return 0;
        } else {
            return ((Type)members.get(0)).alignment();
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
