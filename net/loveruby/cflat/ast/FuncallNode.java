package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class FuncallNode extends ExprNode {
    protected ExprNode expr;
    protected List arguments;

    public FuncallNode(ExprNode expr, List arguments) {
        this.expr = expr;
        this.arguments = arguments;
    }

    public ExprNode expr() {
        return expr;
    }

    /** Returns true if this funcall is NOT a function pointer call. */
    public boolean isStaticCall() {
        return (expr instanceof VariableNode) &&
            (((VariableNode)expr).entity() instanceof Function);
    }

    /**
     * Returns a function object which is refered by expression.
     * This method expects this is static function call (isStaticCall()).
     */
    public Function function() {
        return (Function)((VariableNode)expr).entity();
    }

    /**
     * Returns a type of return value of the function which is refered
     * by expr.  This method expects expr.type().isCallable() is true.
     */
    public Type type() {
        try {
            return functionType().returnType();
        }
        catch (ClassCastException err) {
            throw new SemanticError(err.getMessage());
        }
    }

    /**
     * Returns a type of function which is refered by expr.
     * This method expects expr.type().isCallable() is true.
     */
    public FunctionType functionType() {
        return expr.type().getPointerType().baseType().getFunctionType();
    }

    public long numArgs() {
        return arguments.size();
    }

    public Iterator arguments() {
        return arguments.iterator();
    }

    // called from TypeChecker
    public void replaceArgs(List args) {
        this.arguments = args;
    }

    public ListIterator finalArg() {
        return arguments.listIterator(arguments.size());
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
