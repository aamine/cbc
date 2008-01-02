package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class CharacterLiteralNode extends ExprNode {
    protected TypeNode typeNode;
    protected long value;

    public CharacterLiteralNode(TypeRef ref, long i) {
        typeNode = new TypeNode(ref);
        value = i;
    }

    public Type type() {
        return typeNode.type();
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public long value() {
        return value;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
