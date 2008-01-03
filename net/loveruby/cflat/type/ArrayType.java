package net.loveruby.cflat.type;

public class ArrayType extends Type {
    protected Type baseType;
    protected long length;
    static final protected long undefined = -1;

    public ArrayType(Type baseType) {
        this.baseType = baseType;
        length = undefined;
    }

    public ArrayType(Type baseType, long length) {
        this.baseType = baseType;
        this.length = length;
    }

    public boolean isArray() { return true; }
    public boolean isAllocated() { return length != undefined; }
    public boolean isAllocatedArray() { return isAllocated(); }
    public boolean isDereferable() { return true; }

    public Type baseType() {
        return baseType;
    }

    public long length() {
        return length;
    }

    public long size() {
        return 4;       // FIXME: get from TypeTable
    }

    public long allocSize() {
        // platform dependent: take alignment into account
        return baseType.size() * length;
    }

    public boolean equals(Object other) {
        if (! (other instanceof ArrayType)) return false;
        ArrayType type = (ArrayType)other;
        return (baseType.equals(type.baseType) && length == type.length);
    }

    public boolean isSameType(Type other) {
        // length is not important
        if (! other.isDereferable()) return false;
        return baseType.isSameType(other.baseType());
    }

    public String textize() {
        if (length < 0) {
            return baseType.textize() + "[]";
        }
        else {
            return baseType.textize() + "[" + length + "]";
        }
    }
}
