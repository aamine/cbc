package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

abstract public class AbstractAssignNode extends ExprNode {
    ExprNode lhs, rhs;

    public AbstractAssignNode(ExprNode lhs, ExprNode rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Type type() {
        return lhs.type();
    }

    public ExprNode lhs() {
        return lhs;
    }

    public ExprNode rhs() {
        return rhs;
    }

    public void setRHS(ExprNode expr) {
        this.rhs = expr;
    }

    public Location location() {
        return lhs.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("lhs", lhs);
        d.printMember("rhs", rhs);
    }
}
