package net.loveruby.cflat.ast;

public class MinusNode extends BinaryOpNode {
    public MinusNode(Node left, Node right) {
        super(left, right);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
