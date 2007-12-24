package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

abstract public class LoopNode extends Node
        implements BreakableStmt, ContinueableStmt {
    protected LabelPool pool;
    protected Label begLabel, endLabel;

    public LoopNode(LabelPool lp) {
        super();
        pool = lp;
    }

    public Label begLabel() {
        if (begLabel == null) {
            begLabel = pool.newLabel();
        }
        return begLabel;
    }

    public Label endLabel() {
        if (endLabel == null) {
            endLabel = pool.newLabel();
        }
        return endLabel;
    }

    abstract public Label continueLabel();
}
