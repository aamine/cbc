package net.loveruby.cflat.type;

public class UnsignedLongRef extends TypeRef {
    public UnsignedLongRef() {
    }

    public boolean isUnsignedLong() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedLongRef);
    }

    public int hashCode() {
        return 7;
    }
}
