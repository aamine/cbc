package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.exception.*;

abstract public class ExprNode extends Node {
    public ExprNode() {
        super();
    }

    abstract public Type type();

    public boolean isCallable() {
        try {
            return type().isCallable();
        }
        catch (SemanticError err) {
            return false;
        }
    }

    public boolean isDereferable() {
        try {
            return type().isDereferable();
        }
        catch (SemanticError err) {
            return false;
        }
    }

    public boolean isConstant() {
        return false;
    }

    public boolean isAssignable() {
        return false;
    }

    public boolean isParameter() {
        return false;
    }

    public boolean isConstantAddress() {
        throw new Error("ExprNode#isConstantAddress called");
    }

    public AsmEntity address() {
        throw new Error("ExprNode#address called");
    }

    public Address memref() {
        throw new Error("ExprNode#memref called");
    }
}
