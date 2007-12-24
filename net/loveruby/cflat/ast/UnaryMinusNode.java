package net.loveruby.cflat.ast;

public class UnaryMinusNode extends UnaryOpNode {
    public UnaryMinusNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
