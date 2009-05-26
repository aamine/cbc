package net.loveruby.cflat.asm;

public class UnnamedSymbol extends BaseSymbol {
    public UnnamedSymbol() {
        super();
    }

    public String name() {
        throw new Error("unnamed symbol");
    }

    public String toSource() {
        throw new Error("UnnamedSymbol#toSource() called");
    }

    public String toSource(SymbolTable table) {
        return table.symbolString(this);
    }

    public String toString() {
        return super.toString();
    }

    public int compareTo(Literal lit) {
        return -(lit.compareTo(this));
    }

    public int cmp(IntegerLiteral i) {
        return 1;
    }

    public int cmp(NamedSymbol sym) {
        return 1;
    }

    public int cmp(UnnamedSymbol sym) {
        return toString().compareTo(sym.toString());
    }

    public int cmp(SuffixedSymbol sym) {
        return 1;
    }

    public String dump() {
        return "(UnnamedSymbol @" + Integer.toHexString(hashCode()) + ")";
    }
}
