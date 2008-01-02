package net.loveruby.cflat.ast;

public class RShiftNode extends BinaryOpNode {
    public RShiftNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
