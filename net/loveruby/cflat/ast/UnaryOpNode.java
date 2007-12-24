package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;

abstract public class UnaryOpNode extends Node {
    protected Node expr;

    public UnaryOpNode(Node n) {
        expr = n;
    }

    public Type type() {
        return expr.type();
    }

    public Node expr() {
        return expr;
    }
}
