package net.loveruby.cflat.ast;

public class AssignStmtNode extends StmtNode {
    protected ExprNode lhs, rhs;

    public AssignStmtNode(ExprNode lhs, ExprNode rhs) {
        super(null);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public ExprNode lhs() {
        return lhs;
    }

    public ExprNode rhs() {
        return rhs;
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("lhs", lhs);
        d.printMember("rhs", rhs);
    }
}
