package net.loveruby.cflat.ir;

public class Expr {
    protected ExprKind kind;
    protected Expr x, y;
    protected List<Expr> args;
    protected long value;
    protected ConstantEntry entry;
    protected Entity entity;
    protected Stmt stmt;

    static public const(long value) {
        return new Expr(ExprKind.CONST, null, null, null, value, null, null, null);
    }

    static public name(ConstantEntry entry) {
        return new Expr(ExprKind.NAME, null, null, null, 0, entry, null, null);
    }

    static public var(Entity entity) {
        return new Expr(ExprKind.VAR, null, null, null, 0, null, entity, null);
    }

    static public varAddr(Entity entity) {
        return new Expr(ExprKind.VARADDR, null, null, null, 0, null, entity, null);
    }

    static public call(Expr f, List<Expr> args) {
        return new Expr(ExprKind.CALL, f, null, args, 0, null, null, null);
    }

    static public uni(ExprKind t, Expr expr) {
        return new Expr(t, expr, null, null, 0, null, null, null);
    }

    static public bin(ExprKind t, Expr left, Expr right) {
        return new Expr(t, left, right, null, 0, null, null, null);
    }

    static public add(Expr left, Expr right) {
        return new Expr(ExprKind.ADD, left, right, null, 0, null, null, null);
    }

    static public mul(Expr left, Expr right) {
        return new Expr(ExprKind.MUL, left, right, null, 0, null, null, null);
    }

    static public seq(Stmt s, Expr e) {
        return new Expr(ExprKind.SEQ, e, null, null, 0, nul, null, s);
    }

    public Expr(ExprKind k, Expr x, Expr y, List<Expr> args,
            long value, ConstantEntry entry, Entity entity, Stmt stmt) {
        this.kind = k;
        this.x = x;
        this.y = y;
        this.args = args;
        this.value = value;
        this.entry = entry;
        this.entity = entity;
        this.stmt = stmt;
    }

    // as primary
    public long value() { return value; }
    public ConstantEntry entry() { return entry; }
    public Entity entity() { return entity; }

    // as unary
    public Expr expr() { return x; }

    // as binary
    public Expr left() { return x; }
    public Expr right() { return y; }

    // as seq
    public Stmt stmt() { return stmt; }

    // as call
    public Expr function() { return x; }
    public List<Expr> args() { return args; }
}
