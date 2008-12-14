package net.loveruby.cflat.asm;

public class NamedSymbol extends BaseSymbol {
    protected String name;

    public NamedSymbol(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String toSource() {
        return name;
    }

    public String toSource(SymbolTable table) {
        return name;
    }

    public String toString() {
        return "#" + name;
    }

    public int compareTo(Literal lit) {
        return -(lit.compareTo(this));
    }

    public int cmp(IntegerLiteral i) {
        return 1;
    }

    public int cmp(NamedSymbol sym) {
        return name.compareTo(sym.name);
    }

    public int cmp(UnnamedSymbol sym) {
        return -1;
    }

    public int cmp(SuffixedSymbol sym) {
        return toString().compareTo(sym.toString());
    }
}
