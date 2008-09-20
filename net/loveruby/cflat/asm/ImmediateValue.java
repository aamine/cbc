package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmOperand {
    protected Literal expr;

    public ImmediateValue(long n) {
        this.expr = new IntegerLiteral(n);
    }

    public ImmediateValue(Label label) {
        this.expr = new LabelRef(label);
    }

    public boolean equals(Object other) {
        if (!(other instanceof ImmediateValue)) return false;
        ImmediateValue imm = (ImmediateValue)other;
        return expr.equals(imm.expr);
    }

    public Literal expr() {
        return this.expr;
    }

    public String toSource() {
        return "$" + expr.toSource();
    }
}
