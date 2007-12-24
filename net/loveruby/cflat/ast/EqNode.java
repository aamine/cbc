package net.loveruby.cflat.ast;

public class EqNode extends BinaryOpNode {
    public EqNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
