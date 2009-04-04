package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class DoWhileNode extends LoopNode {
    protected StmtNode body;
    protected Label continueLabel;

    public DoWhileNode(Location loc, StmtNode body, ExprNode cond) {
        super(loc, cond);
        this.body = body;
        this.continueLabel = new Label();
    }

    public StmtNode body() {
        return body;
    }

    public Label continueLabel() {
        return continueLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("body", body);
        d.printMember("cond", cond);
    }

    public DoWhileNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
