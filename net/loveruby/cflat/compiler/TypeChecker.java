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
    protected ErrorHandler errorHandler;

    public TypeChecker(TypeTable typeTable, ErrorHandler errorHandler) {
        this.typeTable = typeTable;
        this.errorHandler = errorHandler;
    }

    public void visit(AST ast) throws SemanticException {
        Iterator funcs = ast.functions();
        while (funcs.hasNext()) {
            DefinedFunction f = (DefinedFunction)funcs.next();
            checkReturnType(f);
            resolve(f.body());
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }

    protected void checkReturnType(Function f) {
        if (f.returnType().isArray()) {
            errorHandler.error("returns an array: " + f.name());
        }
        else if (f.returnType().isStruct()) {
            errorHandler.error("returns a struct: " + f.name());
        }
        else if (f.returnType().isUnion()) {
            errorHandler.error("returns a union: " + f.name());
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
        mustBeScalar(node.cond());
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
                errorHandler.warn("incompatible cast from " +
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
        if (node.function().isVoid()) {
            if (node.expr() != null) {
                errorHandler.error("returning value from void function");
            }
        }
        else {  // non-void function
            if (node.expr() == null) {
                errorHandler.error("missing return value");
                return;
            }
            insertImplicitCast(node);
        }
    }

    protected void insertImplicitCast(ReturnNode node) {
        Type exprType = node.expr().type();
        Type retType = node.function().returnType();
        if (exprType.equals(retType)) {   // type matches
            return;
        }
        else if (exprType.isCompatible(retType)) {
            node.setExpr(newCastNode(retType, node.expr()));
        }
        else {
            errorHandler.error("returning incompatible value: "
                               + exprType.textize());
        }
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

    public void visit(PlusNode node) {
        super.visit(node);
        expectsSameIntegerOrPointerDiff(node);
    }

    public void visit(MinusNode node) {
        super.visit(node);
        expectsSameIntegerOrPointerDiff(node);
    }

    public void visit(MulNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(DivNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(ModNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(BitwiseAndNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(BitwiseOrNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(BitwiseXorNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(LShiftNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(RShiftNode node) {
        super.visit(node);
        expectsSameInteger(node);
    }

    public void visit(EqNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(NotEqNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(LtNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(LtEqNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(GtNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(GtEqNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(LogicalAndNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(LogicalOrNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    // +, -
    protected void expectsSameIntegerOrPointerDiff(BinaryOpNode node) {
        if (node.left().type().isPointer()) {
            mustBeInteger(node.right());
            node.setType(node.left().type());
        }
        else if (node.right().type().isPointer()) {
            mustBeInteger(node.left());
            node.setType(node.right().type());
        }
        else {
            expectsSameInteger(node);
        }
    }

    // *, /, %, &, |, ^, <<, >>
    protected void expectsSameInteger(BinaryOpNode node) {
        mustBeInteger(node.left());
        mustBeInteger(node.right());
        insertImplicitCast(node);
    }

    // ==, !=, <, <=, >, >=, &&, ||
    protected void expectsComparableScalars(BinaryOpNode node) {
        mustBeScalar(node.left());
        mustBeScalar(node.right());
        insertImplicitCast(node);
    }

    protected void insertImplicitCast(BinaryOpNode node) {
        Node r = node.right();
        Node l = node.left();
        if (r.type().equals(l.type())) {
            return;
        }
        else if (r.type().isCompatible(l.type())) {
            // insert cast on right expr
            node.setRight(newCastNode(l.type(), r));
        }
        else if (l.type().isCompatible(r.type())) {
            // insert cast on left expr
            node.setLeft(newCastNode(r.type(), l));
        }
        else {
            incompatibleTypeError(l.type(), r.type());
        }
    }

    //
    // Unary Operator Nodes
    //

    public void visit(PrefixIncNode node) {
        expectsScalarOperand(node);
    }

    public void visit(SuffixIncNode node) {
        expectsScalarOperand(node);
    }

    public void visit(PrefixDecNode node) {
        expectsScalarOperand(node);
    }

    public void visit(SuffixDecNode node) {
        expectsScalarOperand(node);
    }

    protected void expectsScalarOperand(UnaryOpNode node) {
        resolve(node.expr());
        mustBeScalar(node.expr());
    }

    public void visit(FuncallNode node) {
        resolve(node.expr());
        if (! node.expr().isCallable()) {
            errorHandler.error("called object is not a function");
            return;
        }
        FunctionType type = node.functionType();
        if (! type.acceptsArgc(node.numArgs())) {
            errorHandler.error("wrong number of argments: " + node.numArgs());
            return;
        }
        // Check only mandatory parameters
        Iterator params = type.paramTypes();
        Iterator args = node.arguments();
        List newArgs = new ArrayList();
        while (params.hasNext()) {
            Type param = (Type)params.next();
            Node arg = (Node)args.next();
            resolve(arg);
            if (arg.type().equals(param)) {
                newArgs.add(arg);
            }
            else if (arg.type().isCompatible(param)) {
                newArgs.add(newCastNode(param, arg));
            }
            else {
                incompatibleTypeError(arg.type(), param);
                newArgs.add(arg);
            }
        }
        while (args.hasNext()) {
            Node arg = (Node)args.next();
            resolve(arg);
            newArgs.add(arg);
        }
        node.replaceArgs(newArgs);
    }

    public void visit(ArefNode node) {
        resolve(node.expr());
        if (! node.expr().isIndexable()) {
            errorHandler.error("is not indexable: " +
                               node.expr().type().textize());
            return;
        }
        resolve(node.index());
        mustBeInteger(node.index());
    }

    public void visit(MemberNode node) {
        resolve(node.expr());
        checkMemberRef(node.expr().type(), node.name());
    }

    public void visit(PtrMemberNode node) {
        resolve(node.expr());
        if (! node.expr().type().isPointer()) {
            notPointerError(node.type());
            return;
        }
        PointerType pt = (PointerType)node.expr().type();
        checkMemberRef(pt.base(), node.name());
    }

    protected void checkMemberRef(Type t, String memb) {
        if (! t.isComplexType()) {
            errorHandler.error("is not struct/union: " + t.textize());
            return;
        }
        ComplexType type = (ComplexType)t;
        if (! type.hasMember(memb)) {
            errorHandler.error(type.textize() +
                               " does not have member " + memb);
            return;
        }
    }

    public void visit(DereferenceNode node) {
        super.visit(node);
        if (! node.expr().type().isPointer()) {
            notPointerError(node.type());
            return;
        }
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
            errorHandler.warn("incompatible cast from " +
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

    protected void mustBeScalar(Node node) {
        if (node.type().isInteger()) return;
        if (node.type().isPointer()) return;
        notIntegerError(node.type());
    }

    protected void incompatibleTypeError(Type l, Type r) {
        errorHandler.error("incompatible type: "
                           + l.textize() + " and " + r.textize());
    }

    protected void notIntegerError(Type type) {
        errorHandler.error("non-integer argument for unary op: "
                           + type.textize());
    }

    protected void notPointerError(Type type) {
        errorHandler.error("dereferencing non-pointer expression: "
                           + type.textize());
    }
}
