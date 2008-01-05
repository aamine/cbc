package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.type.*;

public class CondExprNode extends ExprNode {
    protected ExprNode cond, thenExpr, elseExpr;
    protected LabelPool pool;
    protected Label elseLabel, endLabel;

    public CondExprNode(LabelPool pool,
                        ExprNode cond, ExprNode t, ExprNode e) {
        super();
        this.pool = pool;
        this.cond = cond;
        this.thenExpr = t;
        this.elseExpr = e;
    }

    public Type type() {
        return thenExpr.type();
    }

    public ExprNode cond() {
        return cond;
    }

    public ExprNode thenExpr() {
        return thenExpr;
    }

    public void setThenExpr(ExprNode node) {
        thenExpr = node;
    }

    public ExprNode elseExpr() {
        return elseExpr;
    }

    public void setElseExpr(ExprNode node) {
        elseExpr = node;
    }

    public Label elseLabel() {
        if (elseLabel == null) {
            elseLabel = pool.newLabel();
        }
        return elseLabel;
    }

    public Label endLabel() {
        if (endLabel == null) {
            endLabel = pool.newLabel();
        }
        return endLabel;
    }

    public Location location() {
        return cond.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("thenExpr", thenExpr);
        d.printMember("elseExpr", elseExpr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
