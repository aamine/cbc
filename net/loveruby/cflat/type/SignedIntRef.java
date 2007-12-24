package net.loveruby.cflat.type;

public class SignedIntRef extends TypeRef {
    public SignedIntRef() {
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
}
