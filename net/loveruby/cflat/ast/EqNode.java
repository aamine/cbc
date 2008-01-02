package net.loveruby.cflat.ast;

public class EqNode extends BinaryOpNode {
    public EqNode(ExprNode left, ExprNode right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
