package net.loveruby.cflat.type;

public abstract class Type {
    static final public long sizeUnknown = -1;

    public long alignment() {
        return size();
    }

    public abstract long size();

    public boolean isReferable() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }

    public boolean isSigned() {
        throw new Error("#isSigned for non-integer type");
    }

    public boolean isNumeric() {
        return false;
    }

    public boolean isPointer() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isStruct() {
        return false;
    }

    public boolean isUnion() {
        return false;
    }

    public boolean isUserType() {
        return false;
    }

    public boolean isFunction() {
        return false;
    }

    public abstract String textize();

    public boolean isCompatible(Type other) {
        return false;
    }

    public boolean isCastableTo(Type target) {
        return equals(target);
    }
}
