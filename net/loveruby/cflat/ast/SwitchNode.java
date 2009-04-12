package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;
import java.util.*;

public class SwitchNode extends StmtNode implements BreakableStmt {
    protected ExprNode cond;
    protected List<CaseNode> cases;
    protected Label endLabel;

    public SwitchNode(Location loc, ExprNode cond, List<CaseNode> cases) {
        super(loc);
        this.cond = cond;
        this.cases = cases;
        this.endLabel = new Label();
    }

    public ExprNode cond() {
        return cond;
    }

    public void setCond(ExprNode cond) {
        this.cond = cond;
    }

    public List<CaseNode> cases() {
        return cases;
    }

    public Label endLabel() {
        return endLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printNodeList("cases", cases);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
