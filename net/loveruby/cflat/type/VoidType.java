package net.loveruby.cflat.type;

public class VoidType extends Type {
    public VoidType() {
    }

    public boolean isVoid() { return true; }
    public boolean isSameType(Type other) { return other.isVoid(); }

    public long size() {
        throw new Error("VoidType#size called");
    }

    public String toString() {
        return "void";
    }
}
