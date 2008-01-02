package net.loveruby.cflat.ast;

public class BitwiseXorNode extends BinaryOpNode {
    public BitwiseXorNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
