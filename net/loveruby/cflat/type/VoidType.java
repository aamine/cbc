package net.loveruby.cflat.type;

public class VoidType extends Type {
    public VoidType() {
    }

    public boolean isVoid() { return true; }

    public long size() {
        throw new Error("VoidType#size called");
    }

    public boolean equals(Object other) {
        return (other instanceof VoidType);
    }

    public boolean isSameType(Type other) {
        return other.isVoid();
    }

    public boolean isCompatible(Type other) {
        return other.isVoid();
    }

    public boolean isCastableTo(Type other) {
        return other.isVoid();
    }

    public String toString() {
        return "void";
    }
}
