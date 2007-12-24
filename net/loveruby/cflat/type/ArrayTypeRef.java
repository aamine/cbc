package net.loveruby.cflat.type;

public class ArrayTypeRef extends TypeRef {
    protected TypeRef base;
    protected long length;
    static final protected long undefined = -1;

    public ArrayTypeRef(TypeRef base) {
        this(base, undefined);
    }

    public ArrayTypeRef(TypeRef ref, long len) {
        if (len < 0) throw new Error("negative array length");
        base = ref;
        length = len;
    }

    public boolean isArrayTypeRef() {
        return true;
    }

    public boolean equals(Object other) {
        return (other instanceof ArrayTypeRef) &&
            (length == ((ArrayTypeRef)other).length);
    }

    public int hashCode() {
        return (1 << 10) & (int)length;
    }

    public TypeRef base() {
        return base;
    }

    public long length() {
        return length;
    }

    public boolean isLengthUndefined() {
        return (length == undefined);
    }
}
