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

    public Literal asmLiteral() {
        throw new Error("ExprNode#asmLiteral called");
    }

    public boolean isAssignable() {
        return false;
    }

    public boolean isParameter() {
        return false;
    }

    public boolean isConstantAddress() {
        return false;
    }

    public AsmOperand address() {
        throw new Error("ExprNode#address called");
    }

    public MemoryReference memref() {
        throw new Error("ExprNode#memref called");
    }
}
