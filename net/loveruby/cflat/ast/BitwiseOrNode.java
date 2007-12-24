package net.loveruby.cflat.ast;

public class BitwiseOrNode extends BinaryOpNode {
    public BitwiseOrNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
