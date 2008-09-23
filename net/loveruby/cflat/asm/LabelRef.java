package net.loveruby.cflat.asm;

public class LabelRef extends Literal {
    protected Label label;

    public LabelRef(Label label) {
        this.label = label;
    }

    public Label label() {
        return label;
    }

    public String symbol() {
        return label.name();
    }

    public String toSource() {
        return symbol();
    }
}
