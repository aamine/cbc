package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class SwitchNode extends Node implements BreakableStmt {
    protected LabelPool pool;
    protected ExprNode cond;
    protected List cases;
    protected Label endLabel;

    public SwitchNode(LabelPool pool, ExprNode cond, List cases) {
        super();
        this.pool = pool;
        this.cond = cond;
        this.cases = cases;
    }

    public ExprNode cond() {
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

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printNodeList("cases", cases);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
