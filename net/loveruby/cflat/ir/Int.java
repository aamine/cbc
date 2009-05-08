package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.*;

public class Int extends Expr {
    protected long value;

    public Int(Type type, long value) {
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

    protected void _dump(Dumper d) {
        d.printMember("value", value);
    }
}
