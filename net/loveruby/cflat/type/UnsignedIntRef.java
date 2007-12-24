package net.loveruby.cflat.type;

public class UnsignedIntRef extends TypeRef {
    public UnsignedIntRef() {
    }

    public boolean isUnsignedInt() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedIntRef);
    }

    public int hashCode() {
        return 6;
    }
}
