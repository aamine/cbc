package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;

public class WhileNode extends LoopNode {
    protected ExprNode cond;
    protected Node body;

    public WhileNode(LabelPool pool, ExprNode cond, Node body) {
        super(pool);
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
