package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class IfNode extends StmtNode {
    protected ExprNode cond;
    protected StmtNode thenBody;
    protected StmtNode elseBody;
    protected Label elseLabel;
    protected Label endLabel;

    public IfNode(Location loc, ExprNode c, StmtNode t, StmtNode e) {
        super(loc);
        this.cond = c;
        this.thenBody = t;
        this.elseBody = e;
        this.elseLabel = new Label();
        this.endLabel = new Label();
    }

    public ExprNode cond() {
        return cond;
    }

    public void setCond(ExprNode cond) {
        this.cond = cond;
    }

    public StmtNode thenBody() {
        return thenBody;
    }

    public StmtNode elseBody() {
        return elseBody;
    }

    public Label elseLabel() {
        return elseLabel;
    }

    public Label endLabel() {
        return endLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("thenBody", thenBody);
        d.printMember("elseBody", elseBody);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
