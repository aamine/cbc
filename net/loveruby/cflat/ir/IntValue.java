package net.loveruby.cflat.ir;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.IntegerLiteral;
import net.loveruby.cflat.asm.ImmediateValue;
import net.loveruby.cflat.asm.MemoryReference;

public class IntValue extends Expr {
    protected long value;

    public IntValue(Type type, long value) {
        super(type);
        this.value = value;
    }

    public long value() { return value; }

    public boolean isConstant() { return true; }

    public ImmediateValue asmValue() {
        return new ImmediateValue(new IntegerLiteral(value));
    }

    public MemoryReference memref() {
        throw new Error("must not happen: IntValue#memref");
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
