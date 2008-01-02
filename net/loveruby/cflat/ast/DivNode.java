package net.loveruby.cflat.ast;

public class DivNode extends BinaryOpNode {
    public DivNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
