package net.loveruby.cflat.entity;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.type.FunctionType;
import net.loveruby.cflat.ast.TypeNode;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.Label;
import java.util.List;

abstract public class Function extends Entity {
    protected Symbol callingSymbol;
    protected Label label;

    public Function(boolean priv, TypeNode t, String name) {
        super(priv, t, name);
    }

    public boolean isInitialized() { return true; }

    abstract public boolean isDefined();
    abstract public List<Parameter> parameters();

    public Type returnType() {
        return type().getFunctionType().returnType();
    }

    public boolean isVoid() {
        return returnType().isVoid();
    }

    public void setCallingSymbol(Symbol sym) {
        if (this.callingSymbol != null) {
            throw new Error("must not happen: Function#callingSymbol was set again");
        }
        this.callingSymbol = sym;
    }

    public Symbol callingSymbol() {
        if (this.callingSymbol == null) {
            throw new Error("must not happen: Function#callingSymbol called but null");
        }
        return this.callingSymbol;
    }

    public Label label() {
        if (label != null) {
            return label;
        }
        else {
            return label = new Label(callingSymbol());
        }
    }
}
