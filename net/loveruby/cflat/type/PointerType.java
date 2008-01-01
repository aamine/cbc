package net.loveruby.cflat.type;

public class PointerType extends Type {
    protected long size;
    protected Type base;

    public PointerType(long s, Type t) {
        size = s;
        base = t;
    }

    public long size() {
        return size;
    }

    public Type base() {
        return base;
    }

    public boolean isReferable() {
        return true;
    }

    public boolean isAllocated() {
        return true;
    }

    public boolean isInteger() {
        return true;
    }

    public boolean isNumeric() {
        return true;
    }

    public boolean isPointer() {
        return true;
    }

    public boolean equals(Object other) {
        if (! (other instanceof PointerType)) return false;
        PointerType otherptr = (PointerType)other;
        return this.base.equals(otherptr.base());
    }

    public String textize() {
        return base.textize() + "*";
    }

    public boolean isCallable() {
        return base.isFunction();
    }

    public boolean isIndexable() {
        return true;
    }
}
