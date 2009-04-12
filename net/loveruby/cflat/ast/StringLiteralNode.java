package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.AsmOperand;
import net.loveruby.cflat.asm.ImmediateValue;
import net.loveruby.cflat.asm.MemoryReference;

public class StringLiteralNode extends LiteralNode {
    protected String value;
    protected ConstantEntry entry;
    protected ImmediateValue asmValue;
    protected MemoryReference memref;

    public StringLiteralNode(Location loc, TypeRef ref, String value) {
        super(loc, ref);
        this.value = value;
    }

    public String value() {
        return value;
    }

    public void setEntry(ConstantEntry ent) {
        entry = ent;
    }

    public Symbol symbol() {
        checkEntry();
        return entry.symbol();
    }

    public MemoryReference memref() {
        return entry.memref();
    }

    public AsmOperand address() {
        return entry.address();
    }

    public ImmediateValue asmValue() {
        return entry.address();
    }

    protected void checkEntry() {
        if (entry == null)
            throw new Error("StringLiteralNode#entry not resolved");
    }

    protected void _dump(Dumper d) {
        //d.printMember("typeNode", typeNode);
        d.printMember("value", value);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
