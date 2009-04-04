package net.loveruby.cflat.ast;

public class SuffixOpNode extends UnaryArithmeticOpNode {
    public SuffixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public SuffixOpNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
