package net.loveruby.cflat.ast;

public class AssignNode extends AbstractAssignNode {
    public AssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
