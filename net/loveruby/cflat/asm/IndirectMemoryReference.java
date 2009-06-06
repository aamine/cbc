package net.loveruby.cflat.asm;

public class IndirectMemoryReference extends MemoryReference {
    Literal offset;
    Register base;
    boolean fixed;

    public IndirectMemoryReference(long offset, Register base) {
        this(new IntegerLiteral(offset), base, true);
    }

    public IndirectMemoryReference(Symbol offset, Register base) {
        this(offset, base, true);
    }

    static public IndirectMemoryReference relocatable(long offset, Register base) {
        return new IndirectMemoryReference(new IntegerLiteral(offset), base, false);
    }

    private IndirectMemoryReference(
            Literal offset, Register base, boolean fixed) {
        this.offset = offset;
        this.base = base;
        this.fixed = fixed;
    }

    public Literal offset() {
        return offset;
    }

    public void fixOffset(long diff) {
        if (fixed) {
            throw new Error("must not happen: fixed = true");
        }
        long curr = ((IntegerLiteral)offset).value;
        this.offset = new IntegerLiteral(curr + diff);
        this.fixed = true;
    }

    public Register base() {
        return base;
    }

    public void collectStatistics(Statistics stats) {
        base.collectStatistics(stats);
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

    public String dump() {
        return "(IndirectMemoryReference "
                + (fixed ? "" : "*")
                + offset.dump() + " " + base.dump() + ")";
    }
}
