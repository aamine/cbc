package net.loveruby.cflat.ast;

public class OrAssignNode extends AbstractAssignNode {
    public OrAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
