package net.loveruby.cflat.type;

public class IntegerType extends Type {
    protected long size;
    protected boolean signed;
    protected String name;

    public IntegerType(long sz, boolean sign, String nm) {
        size = sz;
        signed = sign;
        name = nm;
    }

    public boolean isInteger() { return true; }
    public boolean isSigned() { return signed; }

    public boolean isCompatible(Type other) {
        return (other.isInteger() && size <= other.size());
    }

    public boolean isCastableTo(Type target) {
        return (target.isInteger() || target.isPointer());
    }

    public boolean isSameType(Type other) {
        if (! other.isInteger()) return false;
        return equals(other.getIntegerType());
    }

    public long size() {
        return size;
    }

    public String textize() {
        return name;
    }
}
