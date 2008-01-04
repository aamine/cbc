package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class CastNode extends ExprNode {
    protected TypeNode typeNode;
    protected ExprNode expr;

    public CastNode(TypeNode t, ExprNode expr) {
        this.typeNode = t;
        this.expr = expr;
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

    public ExprNode expr() {
        return expr;
    }

    public boolean isAssignable() {
        return expr.isAssignable();
    }

    protected void _dump(Dumper d) {
        d.printMember("typeNode", typeNode);
        d.printMember("expr", expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
