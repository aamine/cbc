package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class SwitchNode extends Node implements BreakableStmt {
    protected LabelPool pool;
    protected Node cond;
    protected List cases;
    protected Label endLabel;

    public SwitchNode(LabelPool pool, Node cond, List cases) {
        super();
        this.pool = pool;
        this.cond = cond;
        this.cases = cases;
    }

    public Node cond() {
        return cond;
    }

    public Iterator cases() {
        return cases.iterator();
    }

    public Label endLabel() {
        if (endLabel == null) {
            endLabel = pool.newLabel();
        }
        return endLabel;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
