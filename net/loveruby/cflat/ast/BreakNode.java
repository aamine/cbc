package net.loveruby.cflat.ast;

public class BreakNode extends StmtNode {
    public BreakNode(Location loc) {
        super(loc);
    }

    protected void _dump(Dumper d) {
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
