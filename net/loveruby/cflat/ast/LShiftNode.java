package net.loveruby.cflat.ast;

public class LShiftNode extends BinaryOpNode {
    public LShiftNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
