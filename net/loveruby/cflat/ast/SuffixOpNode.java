package net.loveruby.cflat.ast;

public class SuffixOpNode extends UnaryOpNode {
    public SuffixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
