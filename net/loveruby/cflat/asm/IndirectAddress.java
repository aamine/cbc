package net.loveruby.cflat.asm;

public class IndirectAddress extends Address {
    protected long offset;
    protected Register base;

    public IndirectAddress(Register base) {
        this(0, base);
    }

    public IndirectAddress(long offset, Register base) {
        this.offset = offset;
        this.base = base;
    }

    public long offset() {
        return offset;
    }

    public Register base() {
        return base;
    }

    public String toString() {
        return (offset == 0 ? "" : "" + offset) + "(" + base.toString() + ")";
    }
}
