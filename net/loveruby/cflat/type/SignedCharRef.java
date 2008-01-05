package net.loveruby.cflat.type;
import net.loveruby.cflat.ast.Location;

public class SignedCharRef extends TypeRef {
    public SignedCharRef() {
        super(null);
    }

    public SignedCharRef(Location loc) {
        super(loc);
    }

    public boolean isSignedChar() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof SignedCharRef);
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "char";
    }
}
