package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class ArefNode extends ExprNode implements LHSNode {
    protected ExprNode expr, index;

    public ArefNode(ExprNode expr, ExprNode index) {
        this.expr = expr;
        this.index = index;
    }

    public Type type() {
        return expr.type().baseType();
    }

    public ExprNode expr() {
        return expr;
    }

    public ExprNode index() {
        return index;
    }

    public boolean isAssignable() {
        return true;
    }

    public boolean isConstantAddress() {
        // FIXME
        return false;
    }

    public AsmEntity address() {
        // FIXME
        throw new Error("ArefNode#address");
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMember("index", index);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
