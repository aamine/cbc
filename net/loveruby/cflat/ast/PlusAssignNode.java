package net.loveruby.cflat.ast;

public class PlusAssignNode extends AbstractAssignNode {
    public PlusAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
