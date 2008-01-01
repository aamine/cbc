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

    public long size() {
        return size;
    }

    public boolean isInteger() {
        return true;
    }

    public boolean isNumeric() {
        return true;
    }

    public boolean isSigned() {
        return signed;
    }

    public String textize() {
        return name;
    }

    public boolean isCompatible(Type other) {
        return (other.isInteger() && other.size() <= size);
    }

    public boolean isCastableTo(Type target) {
        return (target.isInteger() && target.size() >= size)
            || (target.isPointer() && target.size() >= size);
    }
}
