package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class IfNode extends StmtNode {
    protected ExprNode cond;
    protected Node thenBody;
    protected Node elseBody;
    protected Label elseLabel;
    protected Label endLabel;

    public IfNode(Location loc, ExprNode c, Node t, Node e) {
        super(loc);
        this.cond = c;
        this.thenBody = t;
        this.elseBody = e;
        this.elseLabel = new Label();
        this.endLabel = new Label();
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

    public Label elseLabel() {
        return elseLabel;
    }

    public Label endLabel() {
        return endLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("thenBody", thenBody);
        d.printMember("elseBody", elseBody);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
