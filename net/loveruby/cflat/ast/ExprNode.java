package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
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

    // #@@range/isDereferable{
    public boolean isDereferable() {
        try {
            return type().isDereferable();
        }
        catch (SemanticError err) {
            return false;
        }
    }
    // #@@}

    public boolean isAssignable() {
        return false;
    }

    public boolean isParameter() {
        return false;
    }

    public boolean shouldEvaluatedToAddress() {
        return type().isArray();
    }

    // used by IRGenerator
    public boolean isConstantAddress() {
        return false;
    }

    abstract public <S,E> E accept(ASTVisitor<S,E> visitor);
}
