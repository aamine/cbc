package net.loveruby.cflat.asm;

public class IndirectMemoryReference extends MemoryReference {
    protected long offset;
    protected Register base;

    public IndirectMemoryReference(Register base) {
        this(0, base);
    }

    public IndirectMemoryReference(long offset, Register base) {
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
