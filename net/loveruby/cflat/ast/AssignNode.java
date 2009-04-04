package net.loveruby.cflat.ast;

public class AssignNode extends AbstractAssignNode {
    public AssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public AssignNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
