package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

class DereferenceChecker extends Visitor {
    // #@@range/ctor{
    protected ErrorHandler errorHandler;

    public DereferenceChecker(ErrorHandler h) {
        this.errorHandler = h;
    }
    // #@@}

    // #@@range/check_AST{
    public void check(AST ast) throws SemanticException {
        for (DefinedVariable var : ast.definedVariables()) {
            checkVariable(var);
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            check(f.body());
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }
    // #@@}

    // #@@range/check{
    protected void check(StmtNode node) {
        node.accept(this);
    }

    protected void check(ExprNode node) {
        node.accept(this);
    }
    // #@@}

    //
    // Statements
    //

    // #@@range/BlockNode{
    public Void visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            checkVariable(var);
        }
        for (StmtNode stmt : node.stmts()) {
            try {
                check(stmt);
            }
            catch (SemanticError err) {
                ;
            }
        }
        return null;
    }
    // #@@}

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

    public Void visit(AssignNode node) {
        super.visit(node);
        checkAssignment(node);
        return null;
    }

    public Void visit(OpAssignNode node) {
        super.visit(node);
        checkAssignment(node);
        return null;
    }

    protected void checkAssignment(AbstractAssignNode node) {
        if (! node.lhs().isAssignable()) {
            semanticError(node, "invalid lhs expression");
        }
    }

    //
    // Expressions
    //

    public Void visit(PrefixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
        return null;
    }

    public Void visit(SuffixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
        return null;
    }

    public Void visit(FuncallNode node) {
        super.visit(node);
        if (! node.expr().isCallable()) {
            semanticError(node, "calling object is not a function");
        }
        return null;
    }

    public Void visit(ArefNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            semanticError(node, "indexing non-array/pointer expression");
        }
        return null;
    }

    public Void visit(MemberNode node) {
        super.visit(node);
        checkMemberRef(node, node.expr().type(), node.member());
        return null;
    }

    public Void visit(PtrMemberNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node);
        }
        checkMemberRef(node, node.dereferedType(), node.member());
        return null;
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

    // #@@range/DereferenceNode{
    public Void visit(DereferenceNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node);
        }
        return null;
    }
    // #@@}

    // #@@range/AddressNode{
    public Void visit(AddressNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node, "invalid LHS expression for &");
        }
        return null;
    }
    // #@@}

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
