package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class FuncallNode extends Node {
    protected Node expr;
    protected List args;

    public FuncallNode(Node expr, List args) {
        this.expr = expr;
        this.args = args;
    }

    public Node expr() {
        return expr;
    }

    public boolean isStaticCall() {
        return (expr instanceof VariableNode) &&
            (((VariableNode)expr).entity() instanceof Function);
    }

    public Function function() {
        return (Function)((VariableNode)expr).entity();
    }

    public Type type() {
        return function().type();
    }

    public long numArgs() {
        return args.size();
    }

    public Iterator args() {
        return args.iterator();
    }

    public ListIterator finalArg() {
        return args.listIterator(args.size());
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
