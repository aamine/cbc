package net.loveruby.cflat.type;

public class SignedLongLongRef extends TypeRef {
    public SignedLongLongRef() {
    }

    public boolean isSignedLongLong() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof SignedLongLongRef);
    }

    public int hashCode() {
        return -2;
    }
}
