package net.loveruby.cflat.type;

public class SignedLongRef extends TypeRef {
    public SignedLongRef() {
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
}
