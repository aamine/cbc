package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class FuncallNode extends Node {
    protected Node expr;
    protected List arguments;

    public FuncallNode(Node expr, List arguments) {
        this.expr = expr;
        this.arguments = arguments;
    }

    public Node expr() {
        return expr;
    }

    public boolean isStaticCall() {
        return (expr instanceof VariableNode) &&
            (((VariableNode)expr).entity() instanceof Function);
    }

    // This method expects static function
    public Function function() {
        return (Function)((VariableNode)expr).entity();
    }

    public Type type() {
        return functionType().returnType();
    }

    public FunctionType functionType() {
        Type t = expr().type();
        if (! (t instanceof PointerType)) {
            throw new Error("calling non-function");
        }
        Type f = ((PointerType)t).base();
        if (! (f instanceof FunctionType)) {
            throw new Error("calling non-function");
        }
        return (FunctionType)f;
    }

    public long numArgs() {
        return arguments.size();
    }

    public Iterator arguments() {
        return arguments.iterator();
    }

    /** called from TypeChecker */
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
