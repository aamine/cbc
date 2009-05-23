package net.loveruby.cflat.ir;
import net.loveruby.cflat.entity.Function;
import net.loveruby.cflat.entity.Entity;
import net.loveruby.cflat.asm.Type;
import java.util.List;

public class Call extends Expr {
    private Expr expr;
    private List<Expr> args;

    public Call(Type type, Expr expr, List<Expr> args) {
        super(type);
        this.expr = expr;
        this.args = args;
    }

    public Expr expr() { return expr; }
    public List<Expr> args() { return args; }

    public long numArgs() {
        return args.size();
    }

    /** Returns true if this funcall is NOT a function pointer call. */
    public boolean isStaticCall() {
        return (expr.getEntityForce() instanceof Function);
    }

    /**
     * Returns a function object which is refered by expression.
     * This method expects this is static function call (isStaticCall()).
     */
    public Function function() {
        Entity ent = expr.getEntityForce();
        if (ent == null) {
            throw new Error("not a static funcall");
        }
        return (Function)ent;
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMembers("args", args);
    }
}
