package net.loveruby.cflat.ast;

public class PrefixOpNode extends UnaryArithmeticOpNode {
    public PrefixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public PrefixOpNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
