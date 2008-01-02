package net.loveruby.cflat.ast;

public class MinusAssignNode extends AbstractAssignNode {
    public MinusAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
