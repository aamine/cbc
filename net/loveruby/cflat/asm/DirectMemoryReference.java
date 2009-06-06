package net.loveruby.cflat.asm;

public class DirectMemoryReference extends MemoryReference {
    protected Literal value;

    public DirectMemoryReference(Literal val) {
        this.value = val;
    }

    public Literal value() {
        return this.value;
    }

    public void collectStatistics(Statistics stats) {
        value.collectStatistics(stats);
    }

    public void fixOffset(long diff) {
        throw new Error("DirectMemoryReference#fixOffset");
    }

    public String toString() {
        return toSource(SymbolTable.dummy());
    }

    public String toSource(SymbolTable table) {
        return this.value.toSource(table);
    }

    public int compareTo(MemoryReference mem) {
        return -(mem.cmp(this));
    }

    protected int cmp(IndirectMemoryReference mem) {
        return 1;
    }

    protected int cmp(DirectMemoryReference mem) {
        return value.compareTo(mem.value);
    }

    public String dump() {
        return "(DirectMemoryReference " + value.dump() + ")";
    }
}
