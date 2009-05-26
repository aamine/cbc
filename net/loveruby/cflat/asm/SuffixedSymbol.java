package net.loveruby.cflat.asm;
import net.loveruby.cflat.utils.TextUtils;

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

    public void collectStatistics(Statistics stats) {
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

    public int compareTo(Literal lit) {
        return -(lit.compareTo(this));
    }

    public int cmp(IntegerLiteral i) {
        return 1;
    }

    public int cmp(NamedSymbol sym) {
        return toString().compareTo(sym.toString());
    }

    public int cmp(UnnamedSymbol sym) {
        return -1;
    }

    public int cmp(SuffixedSymbol sym) {
        return toString().compareTo(sym.toString());
    }

    public String dump() {
        return "(SuffixedSymbol " + base.dump() +
                " " + TextUtils.dumpString(suffix) + ")";
    }
}
