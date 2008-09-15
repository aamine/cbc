package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmOperand {
    protected AsmOperand entity;

    public ImmediateValue(long n) {
        this(new IntegerLiteral(n));
    }

    public ImmediateValue(Label label) {
        this(new Symbol(label));
    }

    public ImmediateValue(AsmOperand entity) {
        this.entity = entity;
    }

    public AsmOperand entity() {
        return this.entity;
    }

    public String toString() {
        return "$" + entity.toString();
    }
}
