package net.loveruby.cflat.ast;

public class ModAssignNode extends AbstractAssignNode {
    public ModAssignNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
