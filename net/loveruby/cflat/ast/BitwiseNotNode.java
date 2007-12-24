package net.loveruby.cflat.ast;

public class BitwiseNotNode extends UnaryOpNode {
    public BitwiseNotNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
