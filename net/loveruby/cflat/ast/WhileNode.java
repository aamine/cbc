package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.LabelPool;
import net.loveruby.cflat.asm.Label;

public class WhileNode extends LoopNode {
    protected ExprNode cond;
    protected Node body;

    public WhileNode(Location loc, LabelPool pool,
                     ExprNode cond, Node body) {
        super(loc, pool);
        this.cond = cond;
        this.body = body;
    }

    public ExprNode cond() {
        return cond;
    }

    public Node body() {
        return body;
    }

    public Label continueLabel() {
        return begLabel();
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("body", body);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
