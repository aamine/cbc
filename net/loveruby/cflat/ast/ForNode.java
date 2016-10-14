package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class ForNode extends StmtNode {
    protected StmtNode init;
    protected ExprNode cond;
    protected StmtNode incr;
    protected StmtNode body;

    public ForNode(Location loc, 
                   ExprNode init, ExprNode cond, ExprNode incr, StmtNode body) {
        super(loc);
        if (init != null) {
            this.init = new ExprStmtNode(init.location(), init);
        } else {
            this.init = null;
        }
        if (cond != null) {
            this.cond = cond;
        } else {
            this.cond = new IntegerLiteralNode(null, IntegerTypeRef.intRef(), 1);
        }
        if (incr != null) {
            this.incr = new ExprStmtNode(incr.location(), incr);
        } else {
            this.incr = null;
        }
        this.body = body;
    }

    public StmtNode init() {
        return init;
    }

    public ExprNode cond() {
        return cond;
    }

    public StmtNode incr() {
        return incr;
    }

    public StmtNode body() {
        return body;
    }

    protected void _dump(Dumper d) {
        d.printMember("init", init);
        d.printMember("cond", cond);
        d.printMember("incr", incr);
        d.printMember("body", body);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
