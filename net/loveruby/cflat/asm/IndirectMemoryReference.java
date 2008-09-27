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

    public IndirectMemoryReference(Label offset, Register base) {
        this.offset = new LabelRef(offset);
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

    public String toSource() {
        if (! fixed) {
            throw new Error("must not happen: writing unfixed variable");
        }
        return (offset.isZero() ? "" : offset.toSource())
                + "(" + base.toSource() + ")";
    }
}
