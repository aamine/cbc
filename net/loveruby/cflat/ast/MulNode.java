package net.loveruby.cflat.ast;

public class MulNode extends BinaryOpNode {
    public MulNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
