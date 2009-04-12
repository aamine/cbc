package net.loveruby.cflat.ast;

public class PrefixOpNode extends UnaryArithmeticOpNode {
    public PrefixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
