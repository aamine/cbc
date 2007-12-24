package net.loveruby.cflat.type;

public class VoidType extends Type {
    public VoidType() {
    }

    public boolean isVoid() {
        return true;
    }

    public long size() {
        // FIXME???: use exception
        // This error is caused by user error??
        throw new Error("VoidType#size called");
    }

    public String textize() {
        return "void";
    }
}
