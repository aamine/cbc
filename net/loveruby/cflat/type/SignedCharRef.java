package net.loveruby.cflat.type;

public class SignedCharRef extends TypeRef {
    public SignedCharRef() {
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
}
