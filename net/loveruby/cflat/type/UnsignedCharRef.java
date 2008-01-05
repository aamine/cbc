package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class UnsignedCharRef extends TypeRef {
    public UnsignedCharRef() {
        super(null);
    }

    public UnsignedCharRef(Location loc) {
        super(loc);
    }

    public boolean isUnsignedChar() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedCharRef);
    }

    public int hashCode() {
        return 4;
    }

    public String toString() {
        return "unsigned char";
    }
}
