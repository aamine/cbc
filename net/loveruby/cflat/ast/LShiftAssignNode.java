package net.loveruby.cflat.ast;

public class LShiftAssignNode extends AbstractAssignNode {
    public LShiftAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
