package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmOperand {
    protected Literal expr;

    public ImmediateValue(long n) {
        this(new IntegerLiteral(n));
    }

    public ImmediateValue(Label label) {
        this(new LabelRef(label));
    }

    public ImmediateValue(Literal expr) {
        this.expr = expr;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ImmediateValue)) return false;
        ImmediateValue imm = (ImmediateValue)other;
        return expr.equals(imm.expr);
    }

    public Literal expr() {
        return this.expr;
    }

    public void collectStatistics(AsmStatistics stats) {
        // does nothing
    }

    public String toSource() {
        return "$" + expr.toSource();
    }
}
