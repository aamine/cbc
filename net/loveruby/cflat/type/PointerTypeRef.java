package net.loveruby.cflat.type;

public class PointerTypeRef extends TypeRef {
    protected TypeRef base;

    public PointerTypeRef(TypeRef ref) {
        base = ref;
    }

    public boolean isPointer() {
        return true;
    }

    public TypeRef base() {
        return base;
    }

    public boolean equals(Object other) {
        return (other instanceof PointerTypeRef) &&
            base.equals(((PointerTypeRef)other).base);
    }

    public int hashCode() {
        return (1 << 11) & base.hashCode();
    }
}
