package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.exception.*;

abstract public class ExprNode extends Node {
    public ExprNode() {
        super();
    }

    abstract public Type type();
    protected Type origType() { return type(); }

    public long allocSize() { return type().allocSize(); }

    public boolean isConstant() { return false; }
    public boolean isParameter() { return false; }

    public boolean isLvalue() { return false; }
    public boolean isAssignable() { return false; }
    public boolean isLoadable() { return false; }

    public boolean isCallable() {
        try {
            return type().isCallable();
        }
        catch (SemanticError err) {
            return false;
        }
    }

    // #@@range/isPointer{
    public boolean isPointer() {
        try {
            return type().isPointer();
        }
        catch (SemanticError err) {
            return false;
        }
    }
    // #@@}

    abstract public <S,E> E accept(ASTVisitor<S,E> visitor);
}
