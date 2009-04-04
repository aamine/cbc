package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class WhileNode extends LoopNode {
    protected StmtNode body;

    public WhileNode(Location loc, ExprNode cond, StmtNode body) {
        super(loc, cond);
        this.body = body;
    }

    public StmtNode body() {
        return body;
    }

    public Label continueLabel() {
        return begLabel();
    }

    protected void _dump(Dumper d) {
        d.printMember("cond", cond);
        d.printMember("body", body);
    }

    public WhileNode accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
