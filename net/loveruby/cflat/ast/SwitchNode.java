package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class SwitchNode extends StmtNode implements BreakableStmt {
    protected LabelPool pool;
    protected ExprNode cond;
    protected List<CaseNode> cases;
    protected Label endLabel;

    public SwitchNode(Location loc, LabelPool pool,
                      ExprNode cond, List<CaseNode> cases) {
        super(loc);
        this.pool = pool;
        this.cond = cond;
        this.cases = cases;
    }

    public ExprNode cond() {
        return cond;
    }

    public List<CaseNode> cases() {
        return cases;
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
