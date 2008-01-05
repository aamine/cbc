package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class SignedLongRef extends TypeRef {
    public SignedLongRef() {
        super(null);
    }

    public SignedLongRef(Location loc) {
        super(loc);
    }

    public boolean isSignedLong() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof SignedLongRef);
    }

    public int hashCode() {
        return 3;
    }

    public String toString() {
        return "long";
    }
}
