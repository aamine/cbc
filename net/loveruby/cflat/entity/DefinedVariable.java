package net.loveruby.cflat.entity;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.ast.ExprNode;
import net.loveruby.cflat.ir.Expr;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.NamedSymbol;

public class DefinedVariable extends Variable {
    protected ExprNode initializer;
    protected Expr ir;
    protected long sequence;
    protected Symbol symbol;

    public DefinedVariable(boolean priv, TypeNode type,
                           String name, ExprNode init) {
        super(priv, type, name);
        initializer = init;
        sequence = -1;
    }

    static private long tmpSeq = 0;

    static public DefinedVariable tmp(Type t) {
        return new DefinedVariable(false,
                new TypeNode(t), "@tmp" + tmpSeq++, null);
    }

    public boolean isDefined() {
        return true;
    }

    public void setSequence(long seq) {
        this.sequence = seq;
    }

    public String symbolString() {
        return (sequence < 0) ? name : (name + "." + sequence);
    }

    public boolean hasInitializer() {
        return (initializer != null);
    }

    public boolean isInitialized() {
        return hasInitializer();
    }

    public ExprNode initializer() {
        return initializer;
    }

    public void setInitializer(ExprNode expr) {
        this.initializer = expr;
    }

    public void setIR(Expr expr) {
        this.ir = expr;
    }

    public Expr ir() { return ir; }

    protected void _dump(net.loveruby.cflat.ast.Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate);
        d.printMember("typeNode", typeNode);
        d.printMember("initializer", initializer);
    }

    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
