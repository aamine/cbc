package net.loveruby.cflat.type;

public class IntegerType extends Type {
    protected long size;
    protected boolean isSigned;
    protected String name;

    public IntegerType(long size, boolean isSigned, String name) {
        super();
        this.size = size;
        this.isSigned = isSigned;
        this.name = name;
    }

    public boolean isInteger() { return true; }
    public boolean isSigned() { return isSigned; }
    public boolean isScalar() { return true; }

    public long minValue() {
        return isSigned ? (long)-Math.pow(2, size * 8 - 1) : 0;
    }

    public long maxValue() {
        return isSigned ? (long)Math.pow(2, size * 8 - 1) - 1
                        : (long)Math.pow(2, size * 8) - 1;
    }

    public boolean isInDomain(long i) {
        return (minValue() <= i && i <= maxValue());
    }

    // Use default #equals
    //public boolean equals(Object other)

    public boolean isSameType(Type other) {
        if (! other.isInteger()) return false;
        return equals(other.getIntegerType());
    }

    public boolean isCompatible(Type other) {
        return (other.isInteger() && size <= other.size());
    }

    public boolean isCastableTo(Type target) {
        return (target.isInteger() || target.isPointer());
    }

    public long size() {
        return size;
    }

    public String toString() {
        return name;
    }
}
