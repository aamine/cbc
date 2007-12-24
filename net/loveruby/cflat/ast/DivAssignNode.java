package net.loveruby.cflat.ast;

public class DivAssignNode extends AbstractAssignNode {
    public DivAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
