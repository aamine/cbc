package net.loveruby.cflat.type;

public class UnsignedCharRef extends TypeRef {
    public UnsignedCharRef() {
    }

    public boolean isUnsignedChar() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedCharRef);
    }

    public int hashCode() {
        return 4;
    }
}
