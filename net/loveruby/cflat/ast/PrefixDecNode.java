package net.loveruby.cflat.ast;

public class PrefixDecNode extends UnaryOpNode {
    public PrefixDecNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
