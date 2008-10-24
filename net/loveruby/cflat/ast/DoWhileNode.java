package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class DoWhileNode extends LoopNode {
    protected ExprNode cond;
    protected Node body;
    protected Label continueLabel;

    public DoWhileNode(Location loc, Node body, ExprNode cond) {
        super(loc);
        this.body = body;
        this.cond = cond;
        this.continueLabel = new Label();
    }

    public Node body() {
        return body;
    }

    public ExprNode cond() {
        return cond;
    }

    public Label continueLabel() {
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
