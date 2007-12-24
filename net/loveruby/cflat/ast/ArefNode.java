package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class ArefNode extends Node implements LHSNode {
    protected Node expr, index;

    public ArefNode(Node n, Node idx) {
        expr = n;
        index = idx;
    }

    public Type type() {
        return ((ArrayType)expr.type()).base();
    }

    public Node expr() {
        return expr;
    }

    public Node index() {
        return index;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isConstantAddress() {
        // FIXME
        return false;
    }

    public AsmEntity address() {
        // FIXME
        throw new Error("ArefNode#address");
    }
}
