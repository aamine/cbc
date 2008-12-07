package net.loveruby.cflat.asm;

public class SuffixedSymbol implements Symbol {
    protected Symbol base;
    protected String suffix;

    public SuffixedSymbol(Symbol base, String suffix) {
        this.base = base;
        this.suffix = suffix;
    }

    public boolean isZero() {
        return false;
    }

    public void collectStatistics(AsmStatistics stats) {
        base.collectStatistics(stats);
    }

    public Literal plus(long n) {
        throw new Error("must not happen: SuffixedSymbol.plus called");
    }

    public String name() {
        return base.name();
    }

    public String toSource() {
        return base.toSource() + suffix;
    }

    public String toSource(SymbolTable table) {
        return base.toSource(table) + suffix;
    }

    public String toString() {
        return base.toString() + suffix;
    }
}
