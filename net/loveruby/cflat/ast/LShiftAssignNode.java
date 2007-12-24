package net.loveruby.cflat.ast;

public class LShiftAssignNode extends AbstractAssignNode {
    public LShiftAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
