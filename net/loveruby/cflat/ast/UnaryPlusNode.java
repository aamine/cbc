package net.loveruby.cflat.ast;

public class UnaryPlusNode extends UnaryOpNode {
    public UnaryPlusNode(ExprNode n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
