package net.loveruby.cflat.ast;

public class UnaryPlusNode extends UnaryOpNode {
    public UnaryPlusNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
