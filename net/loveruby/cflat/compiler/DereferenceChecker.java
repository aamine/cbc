package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class DereferenceChecker extends Visitor {
    protected ErrorHandler errorHandler;

    public DereferenceChecker(ErrorHandler h) {
        this.errorHandler = h;
    }

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

    protected void check(StmtNode node) {
        node.accept(this);
    }

    protected void check(ExprNode node) {
        node.accept(this);
    }

    //
    // Statements
    //

    public BlockNode visit(BlockNode node) {
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

    public AssignNode visit(AssignNode node) {
        super.visit(node);
        checkAssignment(node);
        return null;
    }

    public OpAssignNode visit(OpAssignNode node) {
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

    public PrefixOpNode visit(PrefixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
        return null;
    }

    public SuffixOpNode visit(SuffixOpNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node.expr(), "cannot increment/decrement");
        }
        return null;
    }

    public FuncallNode visit(FuncallNode node) {
        super.visit(node);
        if (! node.expr().isCallable()) {
            semanticError(node, "calling object is not a function");
        }
        return null;
    }

    public ArefNode visit(ArefNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            semanticError(node, "indexing non-array/pointer expression");
        }
        return null;
    }

    public MemberNode visit(MemberNode node) {
        super.visit(node);
        checkMemberRef(node, node.expr().type(), node.member());
        return null;
    }

    public PtrMemberNode visit(PtrMemberNode node) {
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

    public DereferenceNode visit(DereferenceNode node) {
        super.visit(node);
        if (! node.expr().isDereferable()) {
            undereferableError(node);
        }
        return null;
    }

    public AddressNode visit(AddressNode node) {
        super.visit(node);
        if (! node.expr().isAssignable()) {
            semanticError(node, "invalid LHS expression for &");
        }
        return null;
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
