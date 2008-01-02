package net.loveruby.cflat.ast;

public class ReturnNode extends Node {
    Node expr;
    Function function;

    public ReturnNode(Node e) {
        super();
        expr = e;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Node expr() {
        return this.expr;
    }

    public void setExpr(Node expr) {
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
