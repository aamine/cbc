package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class StringLiteralNode extends LiteralNode {
    protected String value;
    protected ConstantEntry entry;

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

    public Label label() {
        checkEntry();
        return entry.label();
    }

    public Literal asmLiteral() {
        return new LabelRef(label());
    }

    protected long id() {
        checkEntry();
        return entry.id();
    }

    protected void checkEntry() {
        if (entry == null)
            throw new Error("StringLiteralNode#entry not resolved");
    }

    protected void _dump(Dumper d) {
        //d.printMember("typeNode", typeNode);
        d.printMember("value", value);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
