package net.loveruby.cflat.ast;

public class LtNode extends BinaryOpNode {
    public LtNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
