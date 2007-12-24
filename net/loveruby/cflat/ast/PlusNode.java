package net.loveruby.cflat.ast;

public class PlusNode extends BinaryOpNode {
    public PlusNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
