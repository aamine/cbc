package net.loveruby.cflat.ast;

public class RShiftAssignNode extends AbstractAssignNode {
    public RShiftAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
