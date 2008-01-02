package net.loveruby.cflat.ast;

public class ReturnNode extends Node {
    ExprNode expr;
    Function function;

    public ReturnNode(ExprNode expr) {
        super();
        this.expr = expr;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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
}
