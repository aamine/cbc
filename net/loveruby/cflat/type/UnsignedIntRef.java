package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class UnsignedIntRef extends TypeRef {
    public UnsignedIntRef() {
        super(null);
    }

    public UnsignedIntRef(Location loc) {
        super(loc);
    }

    public boolean isUnsignedInt() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedIntRef);
    }

    public int hashCode() {
        return 6;
    }

    public String toString() {
        return "unsigned int";
    }
}
