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
}
