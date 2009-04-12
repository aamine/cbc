package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class ForNode extends LoopNode {
    protected StmtNode init;
    protected StmtNode incr;
    protected StmtNode body;
    protected Label continueLabel;

    public ForNode(Location loc, 
                   ExprNode init, ExprNode cond, ExprNode incr, StmtNode body) {
        super(loc, cond);
        this.init = new ExprStmtNode(init.location(), init);
        this.incr = new ExprStmtNode(incr.location(), incr);
        this.body = body;
        this.continueLabel = new Label();
    }

    public StmtNode init() {
        return init;
    }

    public StmtNode incr() {
        return incr;
    }

    public StmtNode body() {
        return body;
    }

    public Label continueLabel() {
        return continueLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("init", init);
        d.printMember("cond", cond);
        d.printMember("incr", incr);
        d.printMember("body", body);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
