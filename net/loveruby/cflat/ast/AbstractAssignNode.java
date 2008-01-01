package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class AbstractAssignNode extends Node {
    Node lhs, rhs;

    public AbstractAssignNode(Node lhs, Node rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
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

    public void setRHS(Node node) {
        this.rhs = node;
    }
}
