package net.loveruby.cflat.ast;

public class AddressNode extends UnaryOpNode {
    public AddressNode(Node n) {
        super(n);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
