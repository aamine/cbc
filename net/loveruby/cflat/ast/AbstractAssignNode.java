package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class AbstractAssignNode extends Node {
    Node lhs, rhs;

    public AbstractAssignNode(Node l, Node r) {
        super();
        lhs = l;
        rhs = r;
    }

    public Type type() {
        return lhs.type();
    }

    public Node lhs() {
        return lhs;
    }

    public Node rhs() {
        return rhs;
    }
}
