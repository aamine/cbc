package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class UnsignedShortRef extends TypeRef {
    public UnsignedShortRef() {
        super(null);
    }

    public UnsignedShortRef(Location loc) {
        super(loc);
    }

    public boolean isUnsignedShort() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedShortRef);
    }

    public int hashCode() {
        return 5;
    }

    public String toString() {
        return "unsigned short";
    }
}
