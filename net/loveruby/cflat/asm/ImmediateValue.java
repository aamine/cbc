package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmOperand {
    protected Literal expr;

    public ImmediateValue(long n) {
        this.expr = new IntegerLiteral(n);
    }

    public ImmediateValue(Label label) {
        this.expr = new LabelRef(label);
    }

    public Literal expr() {
        return this.expr;
    }

    public String toString() {
        return "$" + expr.toString();
    }
}
