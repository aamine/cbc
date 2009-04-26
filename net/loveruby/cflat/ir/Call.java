package net.loveruby.cflat.ir;
import net.loveruby.cflat.asm.Type;
import net.loveruby.cflat.ast.Function;
import java.util.List;
import java.util.ListIterator;

public class Call extends Expr {
    protected Expr expr;
    protected List<Expr> args;

    public Call(Type type, Expr expr, List<Expr> args) {
        super(type);
        this.expr = expr;
        this.args = args;
    }

    public Expr expr() { return expr; }
    public List<Expr> arguments() { return args; }

    public long numArgs() {
        return args.size();
    }

    public ListIterator<Expr> finalArg() {
        return args.listIterator(args.size());
    }

    /** Returns true if this funcall is NOT a function pointer call. */
    public boolean isStaticCall() {
        Var var = getFunctionVariable();
        if (var == null) return false;
        return (var.entity() instanceof Function);
    }

    /**
     * Returns a function object which is refered by expression.
     * This method expects this is static function call (isStaticCall()).
     */
    public Function function() {
        Var var = getFunctionVariable();
        if (var == null) throw new Error("not a static funcall");
        return (Function)var.entity();
    }

    private Var getFunctionVariable() {
        if (expr instanceof Addr) {
            Expr e = ((Addr)expr).expr();
            if (! (e instanceof Var)) return null;
            return (Var)e;
        }
        else if (expr instanceof Var) {
            return (Var)expr;
        }
        else {
            return null;
        }
    }

    public <S,E> E accept(IRVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMembers("args", args);
    }
}
