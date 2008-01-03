package net.loveruby.cflat.type;

public class UnsignedShortRef extends TypeRef {
    public UnsignedShortRef() {
    }

    public boolean isUnsignedShort() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof UnsignedShortRef);
    }

    public int hashCode() {
        return 5;
    }

    public String toString() {
        return "unsigned short";
    }
}
