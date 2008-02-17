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
            checkReturnType(f);
            checkParamTypes(f);
            check(f.body());
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }

    protected void checkReturnType(DefinedFunction f) {
        if (isInvalidReturnType(f.returnType())) {
            error(f, "returns invalid type: " + f.returnType());
            return;
        }
    }

    protected void checkParamTypes(DefinedFunction f) {
        Iterator params = f.parameters();
        while (params.hasNext()) {
            Parameter param = (Parameter)params.next();
            if (isInvalidParameterType(param.type())) {
                error(param, "invalid parameter type: " + param.type());
            }
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
            Node n = (Node)stmts.next();
            check(n);
        }
    }

    protected void checkVariable(DefinedVariable var) {
        if (isInvalidVariableType(var.type())) {
            error(var, "invalid variable type");
            return;
        }
        if (var.hasInitializer()) {
            if (isInvalidLHSType(var.type())) {
                error(var, "invalid LHS type: " + var.type());
                return;
            }
            check(var.initializer());
            var.setInitializer(
                checkRHSType(var.initializer(), var.type()));
        }
    }

    public void visit(IfNode node) {
        super.visit(node);
        checkCond(node.cond());
    }

    public void visit(WhileNode node) {
        super.visit(node);
        checkCond(node.cond());
    }

    public void visit(ForNode node) {
        super.visit(node);
        checkCond(node.cond());
    }

    protected void checkCond(ExprNode cond) {
        mustBeScalarAlike(cond, "condition expression");
    }

    public void visit(SwitchNode node) {
        super.visit(node);
        mustBeInteger(node.cond(), "condition expression");
    }

    public void visit(ReturnNode node) {
        super.visit(node);
        if (node.function().isVoid()) {
            if (node.expr() != null) {
                error(node, "returning value from void function");
            }
        }
        else {  // non-void function
            if (node.expr() == null) {
                error(node, "missing return value");
                return;
            }
            if (node.expr().type().isVoid()) {
                error(node, "returning void");
                return;
            }
            insertImplicitCast(node);
        }
    }

    protected void insertImplicitCast(ReturnNode node) {
        Type exprType = node.expr().type();
        Type retType = node.function().returnType();
        if (exprType.isSameType(retType)) {
            return;
        }
        else if (exprType.isCompatible(retType)) {
            node.setExpr(newCastNode(retType, node.expr()));
        }
        else {
            error(node, "returning incompatible value: " + exprType);
        }
    }

    //
    // Assignment Expressions
    //

    public void visit(AssignNode node) {
        checkAssignment(node);
    }

    public void visit(OpAssignNode node) {
        checkAssignment(node);
        // check as operator
    }

    protected void checkAssignment(AbstractAssignNode node) {
        check(node.lhs());
        check(node.rhs());
        if (node.lhs().isParameter()) {
            // parameter is always assignable.
        }
        else if (isInvalidLHSType(node.lhs().type())) {
            error(node, "invalid lhs type");
            return;
        }
        node.setRHS(checkRHSType(node.rhs(), node.lhs().type()));
    }

    protected ExprNode checkRHSType(ExprNode rhs, Type l) {
        Type r = rhs.type();
        if (isInvalidRHSType(r)) {
            error(rhs, "invalid rhs type: " + r);
            return rhs;
        }
        if (l.isSameType(r)) {
            return rhs;
        }
        else if (r.isCastableTo(l)) {   // insert cast on RHS
            if (! r.isCompatible(l)) {
                warn(rhs, "implicit cast from " + r + " to " + l);
            }
            return newCastNode(l, rhs);
        }
        else {
            incompatibleTypeError(rhs, l, r);
            return rhs;
        }
    }

    protected boolean isInvalidReturnType(Type t) {
        return t.isStruct() || t.isUnion() || t.isArray();
    }

    protected boolean isInvalidParameterType(Type t) {
        return t.isStruct() || t.isUnion() || t.isVoid();
    }

    protected boolean isInvalidVariableType(Type t) {
        return t.isVoid();
    }

    protected boolean isInvalidLHSType(Type t) {
        // Array is OK if it is declared as a type of parameter.
        return t.isStruct() || t.isUnion() || t.isVoid() || t.isArray();
    }

    protected boolean isInvalidRHSType(Type t) {
        return t.isStruct() || t.isUnion() || t.isVoid();
    }

    //
    // Expressions
    //

    public void visit(CondExprNode node) {
        super.visit(node);
        checkCond(node.cond());
        Type t = node.thenExpr().type();
        Type e = node.elseExpr().type();
        if (t.isSameType(e)) {
            return;
        }
        else if (t.isCompatible(e)) {   // insert cast on thenBody
            node.setThenExpr(newCastNode(e, node.thenExpr()));
        }
        else if (e.isCompatible(t)) {   // insert cast on elseBody
            node.setElseExpr(newCastNode(t, node.elseExpr()));
        }
        else {
            incompatibleTypeError(node.thenExpr(), e, t);
        }
    }

    public void visit(BinaryOpNode node) {
        super.visit(node);
        if (node.operator().equals("+")
                || node.operator().equals("-")) {
            expectsSameIntegerOrPointerDiff(node);
        }
        else if (node.operator().equals("*")
                || node.operator().equals("/")
                || node.operator().equals("%")
                || node.operator().equals("&")
                || node.operator().equals("|")
                || node.operator().equals("^")
                || node.operator().equals("<<")
                || node.operator().equals(">>")) {
            expectsSameInteger(node);
        }
        else if (node.operator().equals("==")
                || node.operator().equals("!=")
                || node.operator().equals("<")
                || node.operator().equals("<=")
                || node.operator().equals(">")
                || node.operator().equals(">=")) {
            expectsComparableScalars(node);
        }
        else {
            throw new Error("unknown binary operator: " + node.operator());
        }
    }

    public void visit(LogicalAndNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    public void visit(LogicalOrNode node) {
        super.visit(node);
        expectsComparableScalars(node);
    }

    /**
     * For + and -, only following types of expression are valid:
     *
     *   * integer + integer
     *   * pointer + integer
     *   * integer + pointer
     */
    protected void expectsSameIntegerOrPointerDiff(BinaryOpNode node) {
        if (node.left().type().isPointer()) {
            mustBeInteger(node.right(), node.operator());
            node.setType(node.left().type());
        }
        else if (node.right().type().isPointer()) {
            mustBeInteger(node.left(), node.operator());
            node.setType(node.right().type());
        }
        else {
            expectsSameInteger(node);
        }
    }

    // +, -, *, /, %, &, |, ^, <<, >>
    protected void expectsSameInteger(BinaryOpNode node) {
        mustBeInteger(node.left(), node.operator());
        mustBeInteger(node.right(), node.operator());
        insertImplicitCast(node);
    }

    // ==, !=, <, <=, >, >=, &&, ||
    protected void expectsComparableScalars(BinaryOpNode node) {
        mustBeScalarAlike(node.left(), node.operator());
        mustBeScalarAlike(node.right(), node.operator());
        insertImplicitCast(node);
    }

    protected void insertImplicitCast(BinaryOpNode node) {
        ExprNode r = node.right();
        ExprNode l = node.left();
        if (r.type().isSameType(l.type())) {
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
            incompatibleTypeError(node, l.type(), r.type());
        }
    }

    // +, -, !, ~
    public void visit(UnaryOpNode node) {
        super.visit(node);
        if (node.operator().equals("!")) {
            mustBeScalarAlike(node.expr(), node.operator());
        }
        else {
            mustBeInteger(node.expr(), node.operator());
        }
    }

    // ++, --
    public void visit(PrefixOpNode node) {
        super.visit(node);
        mustBeScalar(node.expr(), node.operator());
    }

    // ++, --
    public void visit(SuffixOpNode node) {
        super.visit(node);
        mustBeScalar(node.expr(), node.operator());
    }

    /**
     * For EXPR(ARG), checks:
     *
     *   * The number of argument matches function prototype.
     *   * ARG matches function prototype.
     *   * ARG is neither a struct nor an union.
     */
    public void visit(FuncallNode node) {
        super.visit(node);
        FunctionType type = node.functionType();
        if (! type.acceptsArgc(node.numArgs())) {
            error(node, "wrong number of argments: " + node.numArgs());
            return;
        }
        // Check type of only mandatory parameters.
        Iterator params = type.paramTypes();
        Iterator args = node.arguments();
        List newArgs = new ArrayList();
        while (params.hasNext()) {
            Type param = (Type)params.next();
            ExprNode arg = (ExprNode)args.next();
            check(arg);
            newArgs.add(checkRHSType(arg, param));
        }
        while (args.hasNext()) {
            ExprNode arg = (ExprNode)args.next();
            check(arg);
            newArgs.add(arg);
        }
        node.replaceArgs(newArgs);
    }

    public void visit(ArefNode node) {
        super.visit(node);
        mustBeInteger(node.index(), "[]");
    }

    public void visit(CastNode node) {
        super.visit(node);
        if (! node.expr().type().isCastableTo(node.type())) {
            incompatibleTypeError(node, node.expr().type(), node.type());
        }
    }

    //
    // Utilities
    //

    protected CastNode newCastNode(Type t, ExprNode n) {
        return new CastNode(new TypeNode(t), n);
    }

    protected void mustBeInteger(ExprNode node, String op) {
        if (node.type().isInteger()) return;
        notIntegerError(node, node.type(), op);
    }

    protected void mustBeScalar(ExprNode node, String op) {
        if (node.type().isInteger()) return;
        if (node.type().isPointer()) return;
        notIntegerError(node, node.type(), op);
    }

    protected void mustBeScalarAlike(ExprNode node, String op) {
        if (node.type().isInteger()) return;
        if (node.type().isPointerAlike()) return;
        notIntegerError(node, node.type(), op);
    }

    protected void incompatibleTypeError(Node n, Type l, Type r) {
        error(n, "incompatible type: " + l + " and " + r);
    }

    protected void notIntegerError(Node n, Type type, String op) {
        error(n, "wrong operand type for " + op + ": " + type);
    }

    protected void warn(Node n, String msg) {
        errorHandler.warn(n.location(), msg);
    }

    protected void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
