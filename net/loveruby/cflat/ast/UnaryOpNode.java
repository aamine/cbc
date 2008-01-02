package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;

abstract public class UnaryOpNode extends ExprNode {
    protected ExprNode expr;

    public UnaryOpNode(ExprNode expr) {
        this.expr = expr;
    }

    public Type type() {
        return expr.type();
    }

    public ExprNode expr() {
        return expr;
    }
}
