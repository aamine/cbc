package net.loveruby.cflat.type;

public class ArrayType extends Type {
    protected Type base;
    protected long length;

    public ArrayType(Type t) {
        base = t;
        length = -1;
    }

    public ArrayType(Type t, long len) {
        base = t;
        length = len;
    }

    public Type base() {
        return base;
    }

    public long length() {
        return length;
    }

    public long size() {
        // platform dependent: take alignment into account
        return base.size() * length;
    }

    public boolean isReferable() {
        return true;
    }

    public boolean isAllocated() {
        return (length >= 0);
    }

    public boolean isArray() {
        return true;
    }

    public String textize() {
        if (length < 0) {
            return base.textize() + "[]";
        } else {
            return base.textize() + "[" + length + "]";
        }
    }
}
