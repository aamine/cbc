package net.loveruby.cflat.ast;

public class ReturnNode extends StmtNode {
    protected ExprNode expr;
    protected Function function;

    public ReturnNode(Location loc, ExprNode expr) {
        super(loc);
        this.expr = expr;
    }

    public ExprNode expr() {
        return this.expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    public void setFunction(Function f) {
        if (this.function != null)
            throw new Error("setFunction called twice");
        this.function = f;
    }

    public Function function() {
        if (this.function == null)
            throw new Error("ReturnNode#function called before setFunction");
        return this.function;
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
