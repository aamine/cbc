package net.loveruby.cflat.ast;

public class LShiftNode extends BinaryOpNode {
    public LShiftNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
