package net.loveruby.cflat.ast;

public class PrefixOpNode extends UnaryOpNode {
    public PrefixOpNode(String op, ExprNode expr) {
        super(op, expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
