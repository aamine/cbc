package net.loveruby.cflat.ast;

public class BitwiseAndNode extends BinaryOpNode {
    public BitwiseAndNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
