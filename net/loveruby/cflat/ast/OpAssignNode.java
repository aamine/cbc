package net.loveruby.cflat.ast;

public class OpAssignNode extends AbstractAssignNode {
    protected String operator;

    public OpAssignNode(ExprNode lhs, String op, ExprNode rhs) {
        super(lhs, rhs);
        this.operator = op;
    }

    public String operator() {
        return operator;
    }

    public OpAssignNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
