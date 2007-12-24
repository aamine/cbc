package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;

abstract public class BinaryOpNode extends Node {
    protected Node left, right;

    public BinaryOpNode(Node l, Node r) {
        super();
        left = l;
        right = r;
    }

    public Type type() {
        return left.type();
    }

    public Node left() {
        return left;
    }

    public Node right() {
        return right;
    }
}
