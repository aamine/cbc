package net.loveruby.cflat.ast;

public class AssignNode extends AbstractAssignNode {
    public AssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
