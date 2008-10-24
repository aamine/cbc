package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.MemoryReference;
import net.loveruby.cflat.asm.ImmediateValue;
import net.loveruby.cflat.asm.IntegerLiteral;

public class IntegerLiteralNode extends LiteralNode {
    protected long value;

    public IntegerLiteralNode(Location loc, TypeRef ref, long value) {
        super(loc, ref);
        this.value = value;
    }

    public long value() {
        return value;
    }

    public ImmediateValue asmValue() {
        return new ImmediateValue(new IntegerLiteral(value));
    }

    public MemoryReference memref() {
        throw new Error("must not happen: IntegerLiteralNode#memref");
    }

    protected void _dump(Dumper d) {
        d.printMember("typeNode", typeNode);
        d.printMember("value", value);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
