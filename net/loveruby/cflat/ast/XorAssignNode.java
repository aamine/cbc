package net.loveruby.cflat.ast;

public class XorAssignNode extends AbstractAssignNode {
    public XorAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
