package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class DoWhileNode extends LoopNode {
    protected Node cond, body;

    public DoWhileNode(LabelPool pool, Node b, Node c) {
        super(pool);
        body = b;
        cond = c;
    }

    public Node body() {
        return body;
    }

    public Node cond() {
        return cond;
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
