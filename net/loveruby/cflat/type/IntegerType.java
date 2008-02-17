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
        return (target.isInteger() || target.isDereferable());
    }

    public long size() {
        return size;
    }

    public String toString() {
        return name;
    }
}
