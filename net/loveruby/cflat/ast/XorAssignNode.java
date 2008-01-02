package net.loveruby.cflat.ast;

public class XorAssignNode extends AbstractAssignNode {
    public XorAssignNode(ExprNode lhs, ExprNode rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
