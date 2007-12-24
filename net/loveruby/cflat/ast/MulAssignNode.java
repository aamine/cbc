package net.loveruby.cflat.ast;

public class MulAssignNode extends AbstractAssignNode {
    public MulAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
