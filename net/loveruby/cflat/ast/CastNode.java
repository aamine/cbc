package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class CastNode extends Node {
    protected TypeNode typeNode;
    protected Node expr;

    public CastNode(TypeNode t, Node n) {
        typeNode = t;
        expr = n;
    }

    public void resolve(TypeTable table) {
        System.err.println("FIXME: CastNode#resolve not implemented");
    }

    public Type type() {
        return typeNode.type();
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public Node expr() {
        return expr;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
