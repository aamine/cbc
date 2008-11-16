package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.Symbol;
import net.loveruby.cflat.asm.Label;
import java.util.*;

abstract public class Function extends Entity {
    protected Symbol callingSymbol;
    protected Label label;

    public Function(boolean priv, TypeNode t, String name) {
        super(priv, t, name);
    }

    public boolean isFunction() { return true; }
    public boolean isInitialized() { return true; }
    abstract public boolean isDefined();
    abstract public List<Parameter> parameters();

    public FunctionType functionType() {
        return type().getPointerType().baseType().getFunctionType();
    }

    public Type returnType() {
        return functionType().returnType();
    }

    public boolean isVoid() {
        return returnType().isVoid();
    }

    public boolean cannotLoad() {
        return true;
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
