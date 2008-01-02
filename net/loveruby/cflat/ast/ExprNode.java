package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.*;

abstract public class ExprNode extends Node {
    public ExprNode() {
        super();
    }

    abstract public Type type();

    public boolean isCallable() {
        return type().isCallable();
    }

    public boolean isIndexable() {
        return type().isIndexable();
    }

    public boolean isAssignable() {
        return false;
    }

    public AsmEntity address() {
        throw new Error("ExprNode#address called");
    }
}
