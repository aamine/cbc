package net.loveruby.cflat.type;

public class ArrayType extends Type {
    protected Type baseType;
    protected long length;
    protected long pointerSize;
    static final protected long undefined = -1;

    public ArrayType(Type baseType, long pointerSize) {
        this(baseType, undefined, pointerSize);
    }

    public ArrayType(Type baseType, long length, long pointerSize) {
        this.baseType = baseType;
        this.length = length;
        this.pointerSize = pointerSize;
    }

    public boolean isArray() { return true; }
    public boolean isAllocated() { return length != undefined; }
    public boolean isAllocatedArray() { return isAllocated(); }
    public boolean isUnallocatedArray() { return !isAllocated(); }
    public boolean isDereferable() { return true; }
    public boolean isPointerAlike() { return isUnallocatedArray(); }
    public boolean isScalar() { return true; }
    public boolean isSigned() { return false; }

    public Type baseType() {
        return baseType;
    }

    public long length() {
        return length;
    }

    public long size() {
        return pointerSize;
    }

    public long allocSize() {
        if (isAllocated()) {
            return baseType.allocSize() * length;
        }
        else {
            return size();
        }
    }

    public long alignment() {
        return baseType.alignment();
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

    public boolean isCompatible(Type target) {
        if (! target.isDereferable()) return false;
        if (target.baseType().isVoid()) {
            return true;
        }
        return baseType.isCompatible(target.baseType())
                && baseType.size() == target.baseType().size();
    }

    public boolean isCastableTo(Type target) {
        return target.isDereferable();
    }

    public String toString() {
        if (length < 0) {
            return baseType.toString() + "[]";
        }
        else {
            return baseType.toString() + "[" + length + "]";
        }
    }
}
