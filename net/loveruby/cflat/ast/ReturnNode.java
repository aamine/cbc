package net.loveruby.cflat.ast;

public class ReturnNode extends Node {
    Node expr;

    public ReturnNode(Node e) {
        super();
        expr = e;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Node expr() {
        return expr;
    }
}
