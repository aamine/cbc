package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

abstract public class Function extends Entity {
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

    public MemoryReference memref() {
        return null;
    }

    public AsmEntity address() {
        return new ImmediateValue(new Label(name()));
    }
}
