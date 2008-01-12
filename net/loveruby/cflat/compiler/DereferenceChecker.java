package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class DereferenceChecker extends Visitor {
    static public void check(AST ast, ErrorHandler h)
                            throws SemanticException {
        new DereferenceChecker(h).visit(ast);
    }

    protected ErrorHandler errorHandler;

    public DereferenceChecker(ErrorHandler h) {
        this.errorHandler = h;
    }

    protected void check(Node node) {
        visitNode(node);
    }

    public void visit(AST ast) throws SemanticException {
        Iterator vars = ast.variables();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            checkVariable(var);
        }
        Iterator funcs = ast.functions();
        while (funcs.hasNext()) {
            DefinedFunction f = (DefinedFunction)funcs.next();
            check(f.body());
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }

    //
    // Statements
    //

    protected void checkVariable(DefinedVariable var) {
        if (var.hasInitializer()) {
            check(var.initializer());
        }
    }

    //
    // Assignment Expressions
    //

    public void visit(AssignNode node) {
        super.visit(node);
        checkAssignment(node);
    }

    public void visit(OpAssignNode node) {
        super.visit(node);
        checkAssignment(node);
        // check as operator
    }

    protected void checkAssignment(AbstractAssignNode node) {
        if (! node.lhs().isAssignable()) {
            error(node, "invalid lhs expression");
            return;
        }
    }

    //
    // Expressions
    //

    public void visit(FuncallNode node) {
        super.visit(node);
        if (! node.expr().isCallable()) {
            error(node, "calling object is not a function");
            return;
        }
    }

    public void visit(ArefNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            error(node, "is not indexable: " + node.expr().type());
            return;
        }
        check(node.index());
    }

    public void visit(MemberNode node) {
        super.visit(node);
        checkMemberRef(node, node.expr().type(), node.member());
    }

    public void visit(PtrMemberNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node, node.expr().type());
            return;
        }
        checkMemberRef(node, node.dereferedType(), node.member());
    }

    protected void checkMemberRef(Node node, Type t, String memb) {
        if (! t.isComplexType()) {
            error(node, "is not struct/union: " + t);
            return;
        }
        ComplexType type = t.getComplexType();
        if (! type.hasMember(memb)) {
            error(node, type.toString() + " does not have member " + memb);
            return;
        }
    }

    public void visit(DereferenceNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node, node.expr().type());
            return;
        }
    }

    public void visit(AddressNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            error(node, "invalid LHS expression for &");
        }
    }

    //
    // Utilities
    //

    protected void undereferableError(Node n, Type type) {
        error(n, "dereferencing non-pointer expression: " + type);
    }

    protected void warn(Node n, String msg) {
        errorHandler.warn(n.location(), msg);
    }

    protected void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
