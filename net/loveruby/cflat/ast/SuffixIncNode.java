package net.loveruby.cflat.ast;

public class SuffixIncNode extends UnaryOpNode {
    public SuffixIncNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
