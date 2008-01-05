package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class IfNode extends StmtNode {
    protected ExprNode cond;
    protected Node thenBody, elseBody;
    protected LabelPool pool;
    protected Label elseLabel, endLabel;

    public IfNode(Location loc, LabelPool lp, ExprNode c, Node t, Node e) {
        super(loc);
        pool = lp;
        cond = c;
        thenBody = t;
        elseBody = e;
    }

    public ExprNode cond() {
        return cond;
    }

    public Node thenBody() {
        return thenBody;
    }

    public Node elseBody() {
        return elseBody;
    }

    public Label elseLabel() {
        if (elseLabel == null) {
            elseLabel = pool.newLabel();
        }
        return elseLabel;
    }

    public Label endLabel() {
        if (endLabel == null) {
            endLabel = pool.newLabel();
        }
        return endLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("thenBody", thenBody);
        d.printMember("elseBody", elseBody);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
