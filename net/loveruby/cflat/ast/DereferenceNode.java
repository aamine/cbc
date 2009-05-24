package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class DereferenceNode extends LHSNode {
    private ExprNode expr;

    public DereferenceNode(ExprNode expr) {
        this.expr = expr;
    }

    protected Type origType() {
        return expr.type().baseType();
    }

    public ExprNode expr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        if (type != null) {
            d.printMember("type", type);
        }
        d.printMember("expr", expr);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
