package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.asm.Label;
import java.util.*;

public class Switch extends Stmt {
    protected Expr cond;
    protected List<Case> cases;
    protected Label defaultLabel, endLabel;

    public Switch(Location loc, Expr cond,
            List<Case> cases, Label defaultLabel, Label endLabel) {
        super(loc);
        this.cond = cond;
        this.cases = cases;
        this.defaultLabel = defaultLabel;
        this.endLabel = endLabel;
    }

    public Expr cond() {
        return cond;
    }

    public List<Case> cases() {
        return cases;
    }

    public Label defaultLabel() {
        return defaultLabel;
    }

    public Label endLabel() {
        return endLabel;
    }

    public <S,E> S accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMembers("cases", cases);
        d.printMember("defaultLabel", defaultLabel);
        d.printMember("endLabel", endLabel);
    }
}
