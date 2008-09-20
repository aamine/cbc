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

    public void visit(BlockNode node) {
        Iterator vars = node.variables();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            checkVariable(var);
        }

        Iterator stmts = node.stmts();
        while (stmts.hasNext()) {
            Node stmt = (Node)stmts.next();
            try {
                check(stmt);
            }
            catch (SemanticError err) {
                ;
            }
        }
    }

    protected void checkVariable(DefinedVariable var) {
        if (var.hasInitializer()) {
            try {
                check(var.initializer());
            }
            catch (SemanticError err) {
                ;
            }
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
            semanticError(node, "invalid lhs expression");
        }
    }

    //
    // Expressions
    //

    public void visit(PrefixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
    }

    public void visit(SuffixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
    }

    public void visit(FuncallNode node) {
        super.visit(node);
        if (! node.expr().isCallable()) {
            semanticError(node, "calling object is not a function");
        }
    }

    public void visit(ArefNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            semanticError(node, "indexing non-array/pointer expression");
        }
    }

    public void visit(MemberNode node) {
        super.visit(node);
        checkMemberRef(node, node.expr().type(), node.member());
    }

    public void visit(PtrMemberNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node);
        }
        checkMemberRef(node, node.dereferedType(), node.member());
    }

    protected void checkMemberRef(Node node, Type t, String memb) {
        if (! t.isCompositeType()) {
            semanticError(node, "accessing member `" + memb
                                + "' for non-struct/union: " + t);
        }
        CompositeType type = t.getCompositeType();
        if (! type.hasMember(memb)) {
            semanticError(node, type.toString()
                                + " does not have member: " + memb);
        }
    }

    public void visit(DereferenceNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node);
        }
    }

    public void visit(AddressNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node, "invalid LHS expression for &");
        }
    }

    //
    // Utilities
    //

    protected void undereferableError(Node n) {
        semanticError(n, "dereferencing non-pointer expression");
    }

    protected void semanticError(Node n, String msg) {
        errorHandler.error(n.location(), msg);
        throw new SemanticError("invalid expr");
    }
}
