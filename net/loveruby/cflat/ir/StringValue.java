package net.loveruby.cflat.ir;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.ast.ConstantEntry;
import net.loveruby.cflat.asm.*;

public class StringValue extends Expr {
    protected ConstantEntry entry;

    public StringValue(Type type, ConstantEntry entry) {
        super(type);
        this.entry = entry;
    }

    public ConstantEntry entry() { return entry; }

    public Symbol symbol() {
        return entry.symbol();
    }

    public boolean isConstant() { return true; }

    public MemoryReference memref() {
        return entry.memref();
    }

    public AsmOperand address() {
        return entry.address();
    }

    public ImmediateValue asmValue() {
        return entry.address();
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("entry", entry.toString());
    }
}
