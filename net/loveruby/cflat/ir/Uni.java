package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.Type;

public class Uni extends Expr {
    protected Op op;
    protected Expr expr;

    public Uni(Type type, Op op, Expr expr) {
        super(type);
        this.op = op;
        this.expr = expr;
    }

    public Op op() { return op; }
    public Expr expr() { return expr; }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("op", op.toString());
        d.printMember("expr", expr);
    }
}
