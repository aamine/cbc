package net.loveruby.cflat.ast;

public class RShiftNode extends BinaryOpNode {
    public RShiftNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
