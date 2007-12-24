package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class WhileNode extends LoopNode {
    protected Node cond, body;

    public WhileNode(LabelPool pool, Node c, Node b) {
        super(pool);
        cond = c;
        body = b;
    }

    public Node cond() {
        return cond;
    }

    public Node body() {
        return body;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public Label continueLabel() {
        return begLabel();
    }
}
