package net.loveruby.cflat.ast;

public class PrefixOpNode extends UnaryArithmeticOpNode {
    public PrefixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
