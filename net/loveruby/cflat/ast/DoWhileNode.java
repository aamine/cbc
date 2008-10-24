package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.LabelPool;
import net.loveruby.cflat.asm.Label;

public class DoWhileNode extends LoopNode {
    protected ExprNode cond;
    protected Node body;

    public DoWhileNode(Location loc, LabelPool pool,
                       Node body, ExprNode cond) {
        super(loc, pool);
        this.body = body;
        this.cond = cond;
    }

    public Node body() {
        return body;
    }

    public ExprNode cond() {
        return cond;
    }

    protected Label continueLabel;

    public Label continueLabel() {
        if (continueLabel == null) {
            continueLabel = pool.newLabel();
        }
        return continueLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("body", body);
        d.printMember("cond", cond);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
