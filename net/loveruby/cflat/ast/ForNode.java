package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class ForNode extends LoopNode {
    protected ExprNode init, cond, incr;
    protected Node body;
    protected Label continueLabel;

    public ForNode(Location loc, 
                   ExprNode init, ExprNode cond, ExprNode incr, Node body) {
        super(loc);
        this.init = init;
        this.cond = cond;
        this.incr = incr;
        this.body = body;
        this.continueLabel = new Label();
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

    public Label continueLabel() {
        return continueLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("init", init);
        d.printMember("cond", cond);
        d.printMember("incr", incr);
        d.printMember("body", body);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
