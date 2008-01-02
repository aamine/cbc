package net.loveruby.cflat.ast;

public class PlusNode extends BinaryOpNode {
    public PlusNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
