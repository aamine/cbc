package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class CharacterLiteralNode extends LiteralNode {
    protected long value;

    public CharacterLiteralNode(Location loc, TypeRef ref, long value) {
        super(loc, ref);
        value = value;
    }

    public long value() {
        return value;
    }

    protected void _dump(Dumper d) {
        //d.printMember("typeNode", typeNode);
        d.printMember("value", value);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
