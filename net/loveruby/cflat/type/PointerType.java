package net.loveruby.cflat.type;

public class PointerType extends Type {
    protected long size;
    protected Type baseType;

    public PointerType(long size, Type baseType) {
        this.size = size;
        this.baseType = baseType;
    }

    public boolean isPointer() { return true; }
    public boolean isPointerAlike() { return true; }
    public boolean isDereferable() { return true; }
    public boolean isCallable() { return baseType.isFunction(); }

    public long size() {
        return size;
    }

    public Type baseType() {
        return baseType;
    }

    public boolean equals(Object other) {
        if (! (other instanceof Type)) return false;
        Type t = (Type)other;
        if (! t.isPointer()) return false;
        return baseType.equals(t.getPointerType().baseType);
    }

    public boolean isSameType(Type other) {
        if (! other.isDereferable()) return false;
        return baseType.isSameType(other.baseType());
    }

    public String toString() {
        return baseType.toString() + "*";
    }
}
