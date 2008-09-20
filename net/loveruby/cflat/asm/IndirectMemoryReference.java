package net.loveruby.cflat.asm;

public class IndirectMemoryReference extends MemoryReference {
    protected long offset;
    protected Register base;
    protected boolean fixed;

    public IndirectMemoryReference(Register base) {
        this.offset = 0;
        this.base = base;
        this.fixed = false;
    }

    public IndirectMemoryReference(long offset, Register base) {
        this.offset = offset;
        this.base = base;
        this.fixed = true;
    }

    public long offset() {
        return offset;
    }

    public void fixOffset(long realOffset) {
        if (fixed) {
            throw new Error("must not happen: fixed = true");
        }
        this.offset = realOffset;
        this.fixed = true;
    }

    public Register base() {
        return base;
    }

    public void collectStatistics(AsmStatistics stats) {
        base.collectStatistics(stats);
    }

    public String toSource() {
        if (! fixed) {
            throw new Error("must not happen: writing unfixed variable");
        }
        return (offset == 0 ? "" : "" + offset) + "(" + base.toSource() + ")";
    }
}
