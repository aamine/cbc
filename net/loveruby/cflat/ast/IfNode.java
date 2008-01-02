package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class IfNode extends Node {
    protected ExprNode cond;
    protected Node thenBody, elseBody;
    protected LabelPool pool;
    protected Label elseLabel, endLabel;

    public IfNode(LabelPool lp, ExprNode c, Node t, Node e) {
        super();
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

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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
}
