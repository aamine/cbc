package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

abstract public class LoopNode extends StmtNode
        implements BreakableStmt, ContinueableStmt {
    protected ExprNode cond;
    protected Label begLabel, endLabel;

    public LoopNode(Location loc, ExprNode cond) {
        super(loc);
        this.cond = cond;
        this.begLabel = new Label();
        this.endLabel = new Label();
    }

    public ExprNode cond() {
        return cond;
    }

    public void setCond(ExprNode cond) {
        this.cond = cond;
    }

    public Label begLabel() {
        return begLabel;
    }

    public Label endLabel() {
        return endLabel;
    }

    abstract public Label continueLabel();
}
