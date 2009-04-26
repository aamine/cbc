package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class FuncallNode extends ExprNode {
    protected ExprNode expr;
    protected List<ExprNode> arguments;

    public FuncallNode(ExprNode expr, List<ExprNode> arguments) {
        this.expr = expr;
        this.arguments = arguments;
    }

    public ExprNode expr() {
        return expr;
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

    public List<ExprNode> arguments() {
        return arguments;
    }

    // called from TypeChecker
    public void replaceArgs(List<ExprNode> args) {
        this.arguments = args;
    }

    public ListIterator<ExprNode> finalArg() {
        return arguments.listIterator(arguments.size());
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printNodeList("arguments", arguments);
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
