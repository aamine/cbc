package net.loveruby.cflat.ast;

public class LtNode extends BinaryOpNode {
    public LtNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
