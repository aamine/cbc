package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class ForNode extends LoopNode {
    protected ExprNode init, cond, incr;
    protected Node body;

    public ForNode(LabelPool pool,
                   ExprNode init, ExprNode cond, ExprNode incr, Node body) {
        super(pool);
        this.init = init;
        this.cond = cond;
        this.incr = incr;
        this.body = body;
    }

    public ExprNode init() {
        return init;
    }

    public ExprNode cond() {
        return cond;
    }

    public ExprNode incr() {
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
