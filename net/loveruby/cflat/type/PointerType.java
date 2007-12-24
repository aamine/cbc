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

    //public boolean isArray() { return true; }

    public String textize() {
        return base.textize() + "*";
    }
}
