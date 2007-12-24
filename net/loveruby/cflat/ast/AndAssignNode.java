package net.loveruby.cflat.ast;

public class AndAssignNode extends AbstractAssignNode {
    public AndAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
