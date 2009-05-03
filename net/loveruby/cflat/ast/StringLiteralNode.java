package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.TypeRef;
import net.loveruby.cflat.entity.ConstantEntry;

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

    public ConstantEntry entry() {
        return entry;
    }

    public void setEntry(ConstantEntry ent) {
        entry = ent;
    }

    protected void _dump(Dumper d) {
        d.printMember("value", value);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
