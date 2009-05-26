package net.loveruby.cflat.asm;

public class Label extends Assembly {
    protected Symbol symbol;

    public Label() {
        this(new UnnamedSymbol());
    }

    public Label(Symbol sym) {
        this.symbol = sym;
    }
    
    public Symbol symbol() {
        return symbol;
    }

    public boolean isLabel() {
        return true;
    }

    public String toSource(SymbolTable table) {
        return symbol.toSource(table) + ":";
    }

    public String dump() {
        return "(Label " + symbol.dump() + ")";
    }
}
