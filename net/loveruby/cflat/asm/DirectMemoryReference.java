package net.loveruby.cflat.asm;

public class DirectMemoryReference extends MemoryReference {
    protected Literal value;

    public DirectMemoryReference(Literal val) {
        this.value = val;
    }

    public Literal value() {
        return this.value;
    }

    public void collectStatistics(AsmStatistics stats) {
        value.collectStatistics(stats);
    }

    public String toString() {
        return toSource(SymbolTable.dummy());
    }

    public String toSource(SymbolTable table) {
        return this.value.toSource(table);
    }
}
