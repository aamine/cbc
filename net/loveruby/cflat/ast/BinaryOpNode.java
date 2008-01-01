package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;

abstract public class BinaryOpNode extends Node {
    protected Node left, right;
    protected Type type;

    public BinaryOpNode(Node l, Node r) {
        super();
        left = l;
        right = r;
    }

    public Type type() {
        return (type != null) ? type : left.type();
    }

    public void setType(Type type) {
        if (this.type != null)
            throw new Error("BinaryOp#setType called twice");
        this.type = type;
    }

    public Node left() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node right() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }
}
