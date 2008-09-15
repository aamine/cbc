package net.loveruby.cflat.asm;

public class DirectAddress extends Address {
    protected Literal value;

    public DirectAddress(Label label) {
        this.value = new Symbol(label);
    }

    public DirectAddress(IntegerLiteral n) {
        this.value = n;
    }

    public Literal value() {
        return this.value;
    }

    public String toString() {
        return this.value.toString();
    }
}
