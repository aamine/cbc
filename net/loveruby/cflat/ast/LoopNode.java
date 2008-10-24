package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

abstract public class LoopNode extends StmtNode
        implements BreakableStmt, ContinueableStmt {
    protected Label begLabel, endLabel;

    public LoopNode(Location loc) {
        super(loc);
        this.begLabel = new Label();
        this.endLabel = new Label();
    }

    public Label begLabel() {
        return begLabel;
    }

    public Label endLabel() {
        return endLabel;
    }

    abstract public Label continueLabel();
}
