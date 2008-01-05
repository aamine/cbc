package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class SignedShortRef extends TypeRef {
    public SignedShortRef() {
        super(null);
    }

    public SignedShortRef(Location loc) {
        super(loc);
    }

    public boolean isSignedShort() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof SignedShortRef);
    }

    public int hashCode() {
        return 1;
    }

    public String toString() {
        return "short";
    }
}
