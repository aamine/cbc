package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.NamedSymbol;

public class DefinedVariable extends Variable {
    protected ExprNode initializer;
    protected long sequence;
    protected Symbol symbol;

    public DefinedVariable(boolean priv, TypeNode type,
                           String name, ExprNode init) {
        super(priv, type, name);
        initializer = init;
        sequence = -1;
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

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate);
        d.printMember("typeNode", typeNode);
        d.printMember("initializer", initializer);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
