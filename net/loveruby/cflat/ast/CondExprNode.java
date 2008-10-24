package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.Label;

public class CondExprNode extends ExprNode {
    protected ExprNode cond, thenExpr, elseExpr;
    protected Label elseLabel, endLabel;

    public CondExprNode(ExprNode cond, ExprNode t, ExprNode e) {
        super();
        this.cond = cond;
        this.thenExpr = t;
        this.elseExpr = e;
        this.elseLabel = new Label();
        this.endLabel = new Label();
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
        return elseLabel;
    }

    public Label endLabel() {
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
