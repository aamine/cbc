package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

abstract public class Function extends Entity {
    protected String symbol;
    protected AsmOperand address;

    public Function(boolean priv, TypeNode t, String name) {
        super(priv, t, name);
    }

    public boolean isFunction() { return true; }
    public boolean isInitialized() { return true; }
    abstract public boolean isDefined();
    abstract public Iterator parameters();

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

    public void setSymbol(String sym) {
        if (this.symbol != null) {
            throw new Error("must not happen: Function#symbol was set again");
        }
        this.symbol = sym;
    }

    public String symbol() {
        if (this.symbol == null) {
            throw new Error("must not happen: Function#symbol called but null");
        }
        return this.symbol;
    }

    public MemoryReference memref() {
        return null;
    }

    public void setAddress(AsmOperand addr) {
        this.address = addr;
    }

    public AsmOperand address() {
        if (address == null) {
            throw new Error("must not happen: Function.address == null");
        }
        return this.address;
    }

    public Label label() {
        // FIXME: should cache
        return new Label(symbol());
    }
}
