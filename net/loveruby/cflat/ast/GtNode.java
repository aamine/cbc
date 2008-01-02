package net.loveruby.cflat.ast;

public class GtNode extends BinaryOpNode {
    public GtNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
