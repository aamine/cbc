package net.loveruby.cflat.asm;

public class Symbol extends Literal {
    protected Label label;

    public Symbol(Label label) {
        this.label = label;
    }

    public String symbol() {
        return label.name();
    }

    public String toString() {
        return symbol();
    }
}
