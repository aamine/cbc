package net.loveruby.cflat.ast;

public class ModNode extends BinaryOpNode {
    public ModNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
