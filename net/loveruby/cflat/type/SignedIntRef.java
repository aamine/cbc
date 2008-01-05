package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class SignedIntRef extends TypeRef {
    public SignedIntRef() {
        super(null);
    }

    public SignedIntRef(Location loc) {
        super(loc);
    }

    public boolean isSignedInt() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof SignedIntRef);
    }

    public int hashCode() {
        return 2;
    }

    public String toString() {
        return "int";
    }
}
