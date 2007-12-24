package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class LogicalNotNode extends Node {
    Node expr;

    public LogicalNotNode(Node n) {
        super();
        expr = n;
    }

    public Type type() {
        return expr.type();
    }

    public Node expr() {
        return expr;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
