package net.loveruby.cflat.asm;

public class ImmediateValue extends AsmEntity {
    protected AsmEntity entity;

    public ImmediateValue(long n) {
        this(new IntegerLiteral(n));
    }

    public ImmediateValue(Label label) {
        this(new Symbol(label));
    }

    public ImmediateValue(AsmEntity entity) {
        this.entity = entity;
    }

    public AsmEntity entity() {
        return this.entity;
    }

    public String toString() {
        return "$" + entity.toString();
    }
}
