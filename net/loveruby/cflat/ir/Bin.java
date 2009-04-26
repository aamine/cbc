package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.Type;

public class Bin extends Expr {
    protected Op op;
    protected Expr left, right;

    public Bin(Type type, Op op, Expr left, Expr right) {
        super(type);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public Expr left() { return left; }
    public Expr right() { return right; }
    public Op op() { return op; }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("op", op.toString());
        d.printMember("left", left);
        d.printMember("right", right);
    }
}
