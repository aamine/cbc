package net.loveruby.cflat.ast;

public class PlusAssignNode extends AbstractAssignNode {
    public PlusAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
