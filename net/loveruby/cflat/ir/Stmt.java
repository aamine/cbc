package net.loveruby.cflat.ir;

public class Stmt {
    protected StmtKind kind;
    protected Expr x, y;
    protected Stmt car, cdr;
    protected Label thenDest, elseDest;

    static public Stmt move(Expr lhs, Expr rhs) {
        return new Stmt(StmtKind.MOVE, lhs, rhs, null, null);
    }

    static public Stmt branch(Expr cond, Label thenDest, elseDest) {
        return new Stmt(StmtKind.BRANCH, cond, null, null, null, thenDest, elseDest);
    }

    static public Stmt stmt(Expr expr) {
        return new Stmt(StmtKind.EXPR, expr, null, null, null, null, null);
    }

    static public Stmt ret(Expr expr) {
        return new Stmt(StmtKind.RETURN, expr, null, null, null, null, null);
    }

    static public Stmt label(Label label) {
        return new Stmt(StmtKind.LABEL, null, null, null, null, label, null);
    }

    static public Stmt seq(Stmt car, Stmt cdr) {
        return new Stmt(StmtKind.SEQ, null, null, car, cdr, null, null);
    }

    static public Stmt seq(Stmt s1, Stmt s2, Stmt s3) {
        return seq(s1, seq(s2, s3));
    }

    static public Stmt seq(Stmt s1, Stmt s2, Stmt s3, Stmt s4) {
        return seq(s1, seq(s2, seq(s3, s4)));
    }

    static public Stmt seq(Stmt s1, Stmt s2, Stmt s3, Stmt s4, Stmt s5) {
        return seq(s1, seq(s2, seq(s3, seq(s4, s5))));
    }

    static public Stmt seq(Stmt s1, Stmt s2, Stmt s3, Stmt s4, Stmt s5, Stmt s6) {
        return seq(s1, seq(s2, seq(s3, seq(s4, seq(s5, s6)))));
    }

    static public Stmt seq(Stmt s1, Stmt s2, Stmt s3, Stmt s4, Stmt s5, Stmt s6, Stmt s7) {
        return seq(s1, seq(s2, seq(s3, seq(s4, seq(s5, seq(s6, s7))))));
    }

    public Stmt(StmtKind kind, Expr x, Expr y,
            Stmt car, Stmt cdr,
            Label thenDest, Label elseDest) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.car = car;
        this.cdr = cdr;
        this.thenDest = thenDest;
        this.elseDest = elseDest;
    }

    public StmtKind kind() { return kind; }

    // as expr-statement, return
    public Expr expr() { return x; }

    // as move
    public Expr lhs() { return x; }
    public Expr rhs() { return y; }

    // as branchIf
    public Expr cond() { return x; }
    public Label thenDest() { return thenDest; }
    public Label elseDest() { return elseDest; }

    // as seq
    public Stmt car() { return car; }
    public Stmt cdr() { return cdr; }
}
