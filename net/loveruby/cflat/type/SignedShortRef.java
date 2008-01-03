package net.loveruby.cflat.type;

public class SignedShortRef extends TypeRef {
    public SignedShortRef() {
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
