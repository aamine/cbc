package net.loveruby.cflat.ast;

public class SuffixDecNode extends UnaryOpNode {
    public SuffixDecNode(ExprNode n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
