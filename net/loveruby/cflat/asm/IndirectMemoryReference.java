package net.loveruby.cflat.asm;

public class IndirectMemoryReference extends MemoryReference {
    protected Literal offset;
    protected Register base;
    protected boolean fixed;

    public IndirectMemoryReference(Register base) {
        this.offset = new IntegerLiteral(0);
        this.base = base;
        this.fixed = false;
    }

    public IndirectMemoryReference(long offset, Register base) {
        this.offset = new IntegerLiteral(offset);
        this.base = base;
        this.fixed = true;
    }

    public IndirectMemoryReference(Symbol offset, Register base) {
        this.offset = offset;
        this.base = base;
        this.fixed = true;
    }

    public Literal offset() {
        return offset;
    }

    public void fixOffset(long realOffset) {
        if (fixed) {
            throw new Error("must not happen: fixed = true");
        }
        this.offset = new IntegerLiteral(realOffset);
        this.fixed = true;
    }

    public Register base() {
        return base;
    }

    public void collectStatistics(AsmStatistics stats) {
        base.collectStatistics(stats);
    }

    public void fixStackOffset(long diff) {
        offset = offset.plus(diff);
    }

    public String toString() {
        return toSource(SymbolTable.dummy());
    }

    public String toSource(SymbolTable table) {
        if (! fixed) {
            throw new Error("must not happen: writing unfixed variable");
        }
        return (offset.isZero() ? "" : offset.toSource(table))
                + "(" + base.toSource(table) + ")";
    }

    public int compareTo(MemoryReference mem) {
        return -(mem.cmp(this));
    }

    protected int cmp(DirectMemoryReference mem) {
        return -1;
    }

    protected int cmp(IndirectMemoryReference mem) {
        return offset.compareTo(mem.offset);
    }
}
