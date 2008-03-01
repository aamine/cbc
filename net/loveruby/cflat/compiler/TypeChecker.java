package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class TypeChecker extends Visitor {
    protected TypeTable typeTable;
    protected ErrorHandler errorHandler;

    // #@@range/ctor{
    public TypeChecker(TypeTable typeTable, ErrorHandler errorHandler) {
        this.typeTable = typeTable;
        this.errorHandler = errorHandler;
    }
    // #@@}

    protected void check(Node node) {
        visitNode(node);
    }

    // #@@range/check_AST{
    public void check(AST ast) throws SemanticException {
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
    // #@@}

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
            var.setInitializer(implicitCast(var.type(), var.initializer()));
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
        mustBeScalar(cond, "condition expression");
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
            node.setExpr(implicitCast(node.function().returnType(),
                                      node.expr()));
        }
    }

    //
    // Assignment Expressions
    //

    public void visit(AssignNode node) {
        super.visit(node);
        if (! checkLHS(node.lhs())) return;
        if (! checkRHS(node.rhs())) return;
        node.setRHS(implicitCast(node.lhs().type(), node.rhs()));
    }

    public void visit(OpAssignNode node) {
        super.visit(node);
        if (! checkLHS(node.lhs())) return;
        if (! checkRHS(node.rhs())) return;
        if (node.operator().equals("+")
                || node.operator().equals("-")) {
            if (node.lhs().type().isPointer()) {
                if (! mustBeInteger(node.rhs(), node.operator())) return;
                Type t = integralPromotion(node.rhs().type());
                if (! t.isSameType(node.rhs().type())) {
                    node.setRHS(new CastNode(t, node.rhs()));
                }
                return;
            }
        }
        if (! mustBeInteger(node.lhs(), node.operator())) return;
        if (! mustBeInteger(node.rhs(), node.operator())) return;
        Type l = integralPromotion(node.lhs().type());
        Type r = integralPromotion(node.rhs().type());
        Type opType = usualArithmeticConversion(l, r);
        if (! opType.isCompatible(l)) {
            warn(node, "incompatible implicit cast from "
                       + opType + " to " + l);
        }
        if (! r.isSameType(opType)) {
            // cast RHS
            node.setRHS(new CastNode(opType, node.rhs()));
        }
    }

    protected boolean checkLHS(ExprNode lhs) {
        if (lhs.isParameter()) {
            // parameter is always assignable.
            return true;
        }
        else if (isInvalidLHSType(lhs.type())) {
            error(lhs, "invalid LHS expression type: " + lhs.type());
            return false;
        }
        return true;
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
            node.setThenExpr(new CastNode(e, node.thenExpr()));
        }
        else if (e.isCompatible(t)) {   // insert cast on elseBody
            node.setElseExpr(new CastNode(t, node.elseExpr()));
        }
        else {
            invalidCastError(node.thenExpr(), e, t);
        }
    }

    // #@@range/BinaryOpNode{
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
    // #@@}

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
    // #@@range/expectsSameInteger{
    protected void expectsSameInteger(BinaryOpNode node) {
        if (! node.left().type().isInteger()) {
            wrongTypeError(node.left(), node.operator());
            return;
        }
        if (! node.right().type().isInteger()) {
            wrongTypeError(node.right(), node.operator());
            return;
        }
        arithmeticImplicitCast(node);
    }
    // #@@}

    // ==, !=, <, <=, >, >=, &&, ||
    protected void expectsComparableScalars(BinaryOpNode node) {
        if (! node.left().type().isScalar()) {
            wrongTypeError(node.left(), node.operator());
            return;
        }
        if (! node.right().type().isScalar()) {
            wrongTypeError(node.right(), node.operator());
            return;
        }
        arithmeticImplicitCast(node);
    }

    // Processes usual arithmetic conversion for binary operations.
    // #@@range/arithmeticImplicitCast{
    protected void arithmeticImplicitCast(BinaryOpNode node) {
        Type r = integralPromotion(node.right().type());
        Type l = integralPromotion(node.left().type());
        Type target = usualArithmeticConversion(l, r);
        if (! l.isSameType(target)) {
            // insert cast on left expr
            node.setLeft(new CastNode(target, node.left()));
        }
        if (! r.isSameType(target)) {
            // insert cast on right expr
            node.setRight(new CastNode(target, node.right()));
        }
        node.setType(target);
    }
    // #@@}

    // +, -, !, ~
    public void visit(UnaryOpNode node) {
        super.visit(node);
        if (node.operator().equals("!")) {
            mustBeScalar(node.expr(), node.operator());
        }
        else {
            mustBeInteger(node.expr(), node.operator());
        }
    }

    // ++x, --x
    public void visit(PrefixOpNode node) {
        super.visit(node);
        expectsScalarLHS(node);
    }

    // x++, x--
    public void visit(SuffixOpNode node) {
        super.visit(node);
        expectsScalarLHS(node);
    }

    protected void expectsScalarLHS(UnaryOpNode node) {
        if (node.expr().isParameter()) {
            // parameter is always a scalar.
        }
        else if (node.expr().type().isArray()) {
            // We cannot modify non-parameter array.
            wrongTypeError(node.expr(), node.operator());
            return;
        }
        else {
            mustBeScalar(node.expr(), node.operator());
        }
        if (node.expr().type().isInteger()) {
            Type opType = integralPromotion(node.expr().type());
            if (! node.expr().type().isSameType(opType)) {
                node.setOpType(opType);
            }
        }
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
            newArgs.add(checkRHS(arg) ? implicitCast(param, arg) : arg);
        }
        while (args.hasNext()) {
            ExprNode arg = (ExprNode)args.next();
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
            invalidCastError(node, node.expr().type(), node.type());
        }
    }

    //
    // Utilities
    //

    protected boolean checkRHS(ExprNode rhs) {
        if (isInvalidRHSType(rhs.type())) {
            error(rhs, "invalid RHS expression type: " + rhs.type());
            return false;
        }
        return true;
    }

    // Processes forced-implicit-cast.
    // Applied To: return expr, assignment RHS, funcall argument
    protected ExprNode implicitCast(Type targetType, ExprNode expr) {
        if (expr.type().isSameType(targetType)) {
            return expr;
        }
        else if (expr.type().isCastableTo(targetType)) {
            if (! expr.type().isCompatible(targetType)) {
                warn(expr, "incompatible implicit cast from "
                           + expr.type() + " to " + targetType);
            }
            return new CastNode(targetType, expr);
        }
        else {
            invalidCastError(expr, expr.type(), targetType);
            return expr;
        }
    }

    // Process integral promotion (integers only).
    // #@@range/integralPromotion{
    protected Type integralPromotion(Type t) {
        if (!t.isInteger()) {
            throw new Error("integralPromotion for " + t);
        }
        Type intType = typeTable.signedInt();
        if (t.size() < intType.size()) {
            return intType;
        }
        else {
            return t;
        }
    }
    // #@@}

    // Usual arithmetic conversion for ILP32 platform (integers only).
    // Size of l, r >= sizeof(int).
    // #@@range/usualArithmeticConversion{
    protected Type usualArithmeticConversion(Type l, Type r) {
        Type s_int = typeTable.signedInt();
        Type u_int = typeTable.unsignedInt();
        Type s_long = typeTable.signedLong();
        Type u_long = typeTable.unsignedLong();
        if (       (l.isSameType(u_int) && r.isSameType(s_long))
                || (r.isSameType(u_int) && l.isSameType(s_long))) {
            return u_long;
        }
        else if (l.isSameType(u_long) || r.isSameType(u_long)) {
            return u_long;
        }
        else if (l.isSameType(s_long) || r.isSameType(s_long)) {
            return s_long;
        }
        else if (l.isSameType(u_int)  || r.isSameType(u_int)) {
            return u_int;
        }
        else {
            return s_int;
        }
    }
    // #@@}

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

    protected boolean mustBeInteger(ExprNode expr, String op) {
        if (! expr.type().isInteger()) {
            wrongTypeError(expr, op);
            return false;
        }
        return true;
    }

    protected boolean mustBeScalar(ExprNode expr, String op) {
        if (! expr.type().isScalar()) {
            wrongTypeError(expr, op);
            return false;
        }
        return true;
    }

    protected void invalidCastError(Node n, Type l, Type r) {
        error(n, "invalid cast from " + l + " to " + r);
    }

    protected void wrongTypeError(ExprNode expr, String op) {
        error(expr, "wrong operand type for " + op + ": " + expr.type());
    }

    protected void warn(Node n, String msg) {
        errorHandler.warn(n.location(), msg);
    }

    protected void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
