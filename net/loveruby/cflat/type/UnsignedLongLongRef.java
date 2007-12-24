package net.loveruby.cflat.type;

public class UnsignedLongLongRef extends TypeRef {
    public UnsignedLongLongRef() {
    }

    public boolean isUnsignedLongLong() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedLongLongRef);
    }

    public int hashCode() {
        return -3;
    }
}
