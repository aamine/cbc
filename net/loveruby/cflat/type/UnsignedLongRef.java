package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class UnsignedLongRef extends TypeRef {
    public UnsignedLongRef() {
        super(null);
    }

    public UnsignedLongRef(Location loc) {
        super(loc);
    }

    public boolean isUnsignedLong() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedLongRef);
    }

    public int hashCode() {
        return 7;
    }

    public String toString() {
        return "unsigned long";
    }
}
