package net.loveruby.cflat.type;

public class VoidTypeRef extends TypeRef {
    public VoidTypeRef() {
    }

    public boolean isVoidTypeRef() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof VoidTypeRef);
    }

    public int hashCode() {
        return -1;
    }
}
