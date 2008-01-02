package net.loveruby.cflat.ast;

public class BitwiseOrNode extends BinaryOpNode {
    public BitwiseOrNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
