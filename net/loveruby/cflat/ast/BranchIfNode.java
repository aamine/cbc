package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class BranchIfNode extends StmtNode {
    protected ExprNode cond;
    protected Label thenLabel;
    protected Label elseLabel;

    public BranchIfNode(Location loc, ExprNode cond,
                    Label thenLabel, Label elseLabel) {
        super(loc);
        this.cond = cond;
        this.thenLabel = thenLabel;
        this.elseLabel = elseLabel;
    }

    public ExprNode cond() {
        return cond;
    }

    public Label thenLabel() {
        return thenLabel;
    }

    public Label elseLabel() {
        return elseLabel;
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
    }
}
