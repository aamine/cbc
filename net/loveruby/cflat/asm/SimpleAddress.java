package net.loveruby.cflat.asm;

public class SimpleAddress extends Address {
    Register base;

    public SimpleAddress(Register reg) {
        base = reg;
    }

    public Register base() {
        return base;
    }

    public String toString() {
        return "(" + base.toString() + ")";
    }

    public AsmEntity add(long n) {
        return new CompositeAddress(n, base);
    }
}
