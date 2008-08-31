package net.loveruby.cflat.asm;

public class Reference extends Address {
    protected Label label;

    public Reference(String sym) {
        this(new Label(sym));
    }

    public Reference(Label label) {
        this.label = label;
    }

    public String toString() {
        return "$" + label.toString();
    }
}
