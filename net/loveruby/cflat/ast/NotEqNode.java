package net.loveruby.cflat.ast;

public class NotEqNode extends BinaryOpNode {
    public NotEqNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
