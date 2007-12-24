package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class AssignNode extends Node {
    Node lhs, rhs;

    public AssignNode(Node l, Node r) {
        super();
        lhs = l;
        rhs = r;
    }

    public Type type() {
        // invariant(lhs.type == rhs.type)
        return lhs.type();
    }

    public Node lhs() {
        return lhs;
    }

    public Node rhs() {
        return rhs;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
