package net.loveruby.cflat.type;

public class PointerType extends Type {
    protected long size;
    protected Type baseType;

    public PointerType(long size, Type baseType) {
        this.size = size;
        this.baseType = baseType;
    }

    public boolean isPointer() { return true; }
    public boolean isScalar() { return true; }
    public boolean isSigned() { return false; }
    public boolean isCallable() { return baseType.isFunction(); }

    public long size() {
        return size;
    }

    public Type baseType() {
        return baseType;
    }

    public boolean equals(Object other) {
        if (! (other instanceof PointerType)) return false;
        return baseType.equals(((Type)other).getPointerType().baseType);
    }

    public boolean isSameType(Type other) {
        if (!other.isPointer()) return false;
        return baseType.isSameType(other.baseType());
    }

    public boolean isCompatible(Type other) {
        if (!other.isPointer()) return false;
        if (baseType.isVoid()) {
            return true;
        }
        if (other.baseType().isVoid()) {
            return true;
        }
        return baseType.isCompatible(other.baseType());
    }

    public boolean isCastableTo(Type other) {
        return other.isPointer() || other.isInteger();
    }

    public String toString() {
        return baseType.toString() + "*";
    }
}
