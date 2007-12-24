package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class ForNode extends LoopNode {
    protected Node init, cond, incr, body;

    public ForNode(LabelPool pool, Node ini, Node c, Node inc, Node b) {
        super(pool);
        init = ini;
        cond = c;
        incr = inc;
        body = b;
    }

    public Node init() {
        return init;
    }

    public Node cond() {
        return cond;
    }

    public Node incr() {
        return incr;
    }

    public Node body() {
        return body;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    protected Label continueLabel;

    public Label continueLabel() {
        if (continueLabel == null) {
            continueLabel = pool.newLabel();
        }
        return continueLabel;
    }
}
