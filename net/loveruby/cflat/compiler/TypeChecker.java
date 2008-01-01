package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class TypeChecker extends Visitor {
    static public void check(AST ast, TypeTable typeTable,
            ErrorHandler handler) throws SemanticException {
        new TypeChecker(typeTable, handler).visit(ast);
    }

    protected TypeTable typeTable;
    protected ErrorHandler handler;

    public TypeChecker(TypeTable typeTable, ErrorHandler handler) {
        this.typeTable = typeTable;
        this.handler = handler;
    }

    public void visit(AST ast) throws SemanticException {
        Iterator funcs = ast.functions();
        while (funcs.hasNext()) {
            DefinedFunction f = (DefinedFunction)funcs.next();
            resolve(f.body());
        }
        if (handler.errorOccured()) {
            throw new SemanticException("compile error");
        }
    }

    //
    // Statement Nodes
    //

    public void visit(IfNode node) {
        super.visit(node);
        checkCondExpr(node.cond());
    }

    public void visit(WhileNode node) {
        super.visit(node);
        checkCondExpr(node.cond());
    }

    public void visit(ForNode node) {
        super.visit(node);
        checkCondExpr(node.cond());
    }

    protected void checkCondExpr(Node node) {
        Type t = node.type();
        if (!t.isInteger() && !t.isPointer()) {
            notIntegerError(t);
        }
    }

    public void visit(SwitchNode node) {
        super.visit(node);
        mustBeIntegerAlike(node.cond());
    }

    //
    // Assignment Expression Nodes
    //

    public void visit(AssignNode node) {
        checkAssignment(node);
    }

    public void visit(PlusAssignNode node) {
        checkAssignment(node);
    }

    public void visit(MinusAssignNode node) {
        checkAssignment(node);
    }

    public void visit(MulAssignNode node) {
        checkAssignment(node);
    }

    public void visit(DivAssignNode node) {
        checkAssignment(node);
    }

    public void visit(ModAssignNode node) {
        checkAssignment(node);
    }

    public void visit(AndAssignNode node) {
        checkAssignment(node);
    }

    public void visit(OrAssignNode node) {
        checkAssignment(node);
    }

    public void visit(XorAssignNode node) {
        checkAssignment(node);
    }

    public void visit(LShiftAssignNode node) {
        checkAssignment(node);
    }

    public void visit(RShiftAssignNode node) {
        checkAssignment(node);
    }

    protected void checkAssignment(AbstractAssignNode node) {
        resolve(node.lhs());
        resolve(node.rhs());
        Type l = node.lhs().type();
        Type r = node.rhs().type();
        if (l.equals(r)) {
            return;
        }
        else if (r.isCastableTo(l)) {   // insert cast on RHS
            if (! r.isCompatible(l)) {
                handler.warn("incompatible cast from " +
                        r.textize() + " to " + l.textize());
            }
            node.setRHS(newCastNode(l, node.rhs()));
        }
        else {
            incompatibleTypeError(l, r);
        }
    }

    public void visit(ReturnNode node) {
        super.visit(node);
        // FIXME: check return type
    }

    //
    // Condition Expression Node
    //

    public void visit(CondExprNode node) {
        super.visit(node);
        checkCondExpr(node.cond());
        Type t = node.thenBody().type();
        Type e = node.elseBody().type();
        if (t.equals(e)) {
            return;
        }
        else if (t.isCompatible(e)) {   // insert cast on thenBody
            node.setThenBody(newCastNode(e, node.thenBody()));
        }
        else if (e.isCompatible(t)) {   // insert cast on elseBody
            node.setElseBody(newCastNode(t, node.elseBody()));
        }
        else {
            incompatibleTypeError(e, t);
        }
    }

    //
    // Binary Operator Nodes
    //

    // FIXME: ptr + int
    public void visit(PlusNode node) {
        expectsSameType(node);
    }

    // FIXME: ptr + int
    public void visit(MinusNode node) {
        expectsSameType(node);
    }

    public void visit(MulNode node) {
        expectsSameType(node);
    }

    public void visit(DivNode node) {
        expectsSameType(node);
    }

    public void visit(ModNode node) {
        expectsSameType(node);
    }

    public void visit(BitwiseAndNode node) {
        expectsSameType(node);
    }

    public void visit(BitwiseOrNode node) {
        expectsSameType(node);
    }

    public void visit(BitwiseXorNode node) {
        expectsSameType(node);
    }

    public void visit(EqNode node) {
        expectsSameType(node);
    }

    public void visit(NotEqNode node) {
        expectsSameType(node);
    }

    public void visit(LtNode node) {
        expectsSameType(node);
    }

    public void visit(LtEqNode node) {
        expectsSameType(node);
    }

    public void visit(GtNode node) {
        expectsSameType(node);
    }

    public void visit(GtEqNode node) {
        expectsSameType(node);
    }

    public void visit(LogicalAndNode node) {
        expectsSameType(node);
    }

    public void visit(LogicalOrNode node) {
        expectsSameType(node);
    }

    protected void expectsSameType(BinaryOpNode node) {
        resolve(node.left());
        resolve(node.right());
        Type r = node.right().type();
        Type l = node.left().type();
        if (r.equals(l)) {
            return;
        }
        else if (r.isCompatible(l)) {   // insert cast on right expr
            node.setRight(newCastNode(l, node.right()));
        }
        else if (l.isCompatible(r)) {   // insert cast on left expr
            node.setLeft(newCastNode(r, node.left()));
        }
        else {
            incompatibleTypeError(l, r);
        }
    }

    //
    // Unary Operator Nodes
    //

    public void visit(PrefixIncNode node) {
        expectsIntOperand(node);
    }

    public void visit(SuffixIncNode node) {
        expectsIntOperand(node);
    }

    public void visit(PrefixDecNode node) {
        expectsIntOperand(node);
    }

    public void visit(SuffixDecNode node) {
        expectsIntOperand(node);
    }

    protected void expectsIntOperand(UnaryOpNode node) {
        resolve(node.expr());
        mustBeIntegerAlike(node.expr());
    }

    public void visit(FuncallNode node) {
        super.visit(node);
        // FIXME: check types
        // FIXME: check if callable
        // FIXME: check argument types
    }

    public void visit(ArefNode node) {
        super.visit(node);
        // FIXME: check types
    }

    public void visit(MemberNode node) {
        super.visit(node);
        // FIXME: validate member here?
    }

    public void visit(PtrMemberNode node) {
        super.visit(node);
        // FIXME: validate member here?
    }

    public void visit(DereferenceNode node) {
        super.visit(node);
        if (node.type().isPointer()) return;
        notPointerError(node.type());
    }

    public void visit(AddressNode node) {
        super.visit(node);
        Type t = typeTable.pointerTo(node.expr().type());
        node.setType(t);
        // FIXME: what is "assignable"??
    }

    public void visit(CastNode node) {
        resolve(node.expr());
        if (! node.expr().type().isCastableTo(node.type())) {
            incompatibleTypeError(node.expr().type(), node.type());
        }
        else if (! node.expr().type().isCompatible(node.type())) {
            handler.warn("incompatible cast from " +
                    node.expr().type().textize() +
                    " to " + node.type().textize());
        }
    }

    //
    // Utilities
    //

    protected CastNode newCastNode(Type t, Node n) {
        return new CastNode(new TypeNode(t), n);
    }

    protected void mustBeInteger(Node node) {
        if (node.type().isInteger()) return;
        notIntegerError(node.type());
    }

    protected void mustBeIntegerAlike(Node node) {
        if (node.type().isInteger()) return;
        if (node.type().isPointer()) return;
        notIntegerError(node.type());
    }

    protected void incompatibleTypeError(Type l, Type r) {
        handler.error("incompatible type: " +
                l.textize() + " and " + r.textize());
    }

    protected void notIntegerError(Type type) {
        handler.error("non-integer argument for ++/--: " + type.textize());
    }

    protected void notPointerError(Type type) {
        handler.error("non-pointer argument: " + type.textize());
    }
}
