package net.loveruby.cflat.ast;

public class MinusNode extends BinaryOpNode {
    public MinusNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
