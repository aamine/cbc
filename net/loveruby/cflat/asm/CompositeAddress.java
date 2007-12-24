package net.loveruby.cflat.asm;

public class CompositeAddress extends Address {
    long offset;
    Register base;

    public CompositeAddress(long off, Register reg) {
        offset = off;
        base = reg;
    }

    public long offset() {
        return offset;
    }

    public Register base() {
        return base;
    }

    public String toString() {
        return "" + offset + "(" + base.toString() + ")";
    }

    public AsmEntity add(long n) {
        return new CompositeAddress(offset + n, base);
    }
}
