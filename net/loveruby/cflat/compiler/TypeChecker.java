package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class TypeChecker implements ASTVisitor {
    protected TypeTable typeTable;
    protected ErrorHandler errorHandler;
    private DefinedFunction currentFunction;

    // #@@range/ctor{
    public TypeChecker(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/check_AST{
    public void check(AST ast) throws SemanticException {
        this.typeTable = ast.typeTable();
        for (DefinedVariable var : ast.definedVariables()) {
            visit(var);
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            visit(f);
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }
    // #@@}

    public DefinedFunction visit(DefinedFunction f) {
        currentFunction = f;
        checkReturnType(f);
        checkParamTypes(f);
        checkStmt(f.body());
        return null;
    }

    public UndefinedFunction visit(UndefinedFunction var) {
        throw new Error("must not happen: TypeChecker.visit:UndefinedFunction");
    }

    public DefinedVariable visit(DefinedVariable var) {
        if (isInvalidVariableType(var.type())) {
            error(var, "invalid variable type");
            return null;
        }
        if (var.hasInitializer()) {
            if (isInvalidLHSType(var.type())) {
                error(var, "invalid LHS type: " + var.type());
                return null;
            }
            var.setInitializer(implicitCast(var.type(),
                            checkExpr(var.initializer())));
        }
        return null;
    }

    public UndefinedVariable visit(UndefinedVariable var) {
        throw new Error("must not happen: TypeChecker.visit:UndefinedVariable");
    }

    protected void checkReturnType(DefinedFunction f) {
        if (isInvalidReturnType(f.returnType())) {
            error(f, "returns invalid type: " + f.returnType());
        }
    }

    protected void checkParamTypes(DefinedFunction f) {
        for (Parameter param : f.parameters()) {
            if (isInvalidParameterType(param.type())) {
                error(param, "invalid parameter type: " + param.type());
            }
        }
    }

    protected void checkStmt(StmtNode node) {
        node.accept(this);
    }

    protected ExprNode checkExpr(ExprNode node) {
        if (node == null) return null;
        return node.accept(this);
    }

    //
    // Declarations
    //

    public StructNode visit(StructNode node) {
        return null;
    }

    public UnionNode visit(UnionNode node) {
        return null;
    }

    public TypedefNode visit(TypedefNode node) {
        return null;
    }

    //
    // Statements: replaces expr and returns null.
    //

    public BlockNode visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            visit(var);
        }
        for (StmtNode n : node.stmts()) {
            checkStmt(n);
        }
        return null;
    }

    public ExprStmtNode visit(ExprStmtNode node) {
        ExprNode expr = checkExpr(node.expr());
        if (isInvalidStatementType(expr.type())) {
            error(expr, "invalid statement type: " + expr.type());
            return null;
        }
        node.setExpr(expr);
        return null;
    }

    public IfNode visit(IfNode node) {
        node.setCond(checkCond(node.cond()));
        checkStmt(node.thenBody());
        if (node.elseBody() != null) {
            checkStmt(node.elseBody());
        }
        return null;
    }

    public WhileNode visit(WhileNode node) {
        node.setCond(checkCond(node.cond()));
        checkStmt(node.body());
        return null;
    }

    public DoWhileNode visit(DoWhileNode node) {
        checkStmt(node.body());
        node.setCond(checkCond(node.cond()));
        return null;
    }

    public ForNode visit(ForNode node) {
        checkStmt(node.init());
        node.setCond(checkCond(node.cond()));
        checkStmt(node.incr());
        checkStmt(node.body());
        return null;
    }

    protected ExprNode checkCond(ExprNode cond) {
        ExprNode expr = checkExpr(cond);
        mustBeScalar(expr, "condition expression");
        return expr;
    }

    public SwitchNode visit(SwitchNode node) {
        ExprNode expr = checkExpr(node.cond());
        mustBeInteger(expr, "condition expression");
        node.setCond(expr);
        for (CaseNode n : node.cases()) {
            checkStmt(n);
        }
        return null;
    }

    public CaseNode visit(CaseNode node) {
        List<ExprNode> exprs = new ArrayList<ExprNode>();
        for (ExprNode expr : node.values()) {
            ExprNode e = checkExpr(expr);
            mustBeInteger(e, "case");
            exprs.add(e);
        }
        node.setValues(exprs);
        return null;
    }

    public ReturnNode visit(ReturnNode node) {
        if (node.expr() != null) {
            node.setExpr(checkExpr(node.expr()));
        }
        if (currentFunction.isVoid()) {
            if (node.expr() != null) {
                error(node, "returning value from void function");
            }
        }
        else {  // non-void function
            if (node.expr() == null) {
                error(node, "missing return value");
                return null;
            }
            if (node.expr().type().isVoid()) {
                error(node, "returning void");
                return null;
            }
            node.setExpr(implicitCast(currentFunction.returnType(),
                                      node.expr()));
        }
        return null;
    }

    public LabelNode visit(LabelNode node) {
        checkStmt(node.stmt());
        return null;
    }

    public GotoNode visit(GotoNode node) {
        return null;
    }

    public ContinueNode visit(ContinueNode node) {
        return null;
    }

    public BreakNode visit(BreakNode node) {
        return null;
    }

    //
    // Assignment Expressions
    //

    public AssignNode visit(AssignNode node) {
        return new AssignNode(checkLHS(node.lhs()), checkRHS(node.rhs()));
    }

    public OpAssignNode visit(OpAssignNode node) {
        ExprNode lhs = checkLHS(node.lhs());
        ExprNode rhs = checkRHS(node.rhs());
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (lhs.type().isPointer()) {
                if (! mustBeInteger(rhs, node.operator())) {
                    return node;
                }
                return new OpAssignNode(
                    lhs, node.operator(), multiplyPtrBaseSize(rhs, lhs));
            }
        }
        if (!mustBeInteger(lhs, node.operator())
                || !mustBeInteger(rhs, node.operator())) {
            return new OpAssignNode(lhs, node.operator(), rhs);
        }
        Type l = integralPromotion(lhs.type());
        Type r = integralPromotion(rhs.type());
        Type opType = usualArithmeticConversion(l, r);
        if (! opType.isCompatible(l)
                && ! isSafeIntegerCast(rhs, opType)) {
            warn(node, "incompatible implicit cast from "
                       + opType + " to " + l);
        }
        if (r.isSameType(opType)) {
            return new OpAssignNode(lhs, node.operator(), rhs);
        }
        else {   // cast RHS
            return new OpAssignNode(lhs, node.operator(), new CastNode(opType, rhs));
        }
    }

    /** allow safe implicit cast from integer literal like:
     *
     *    char c = 0;
     *
     *  "0" has a type integer, but we can cast (int)0 to (char)0 safely.
     */
    protected boolean isSafeIntegerCast(Node node, Type type) {
        if (! type.isInteger()) return false;
        IntegerType t = (IntegerType)type;
        if (! (node instanceof IntegerLiteralNode)) return false;
        IntegerLiteralNode n = (IntegerLiteralNode)node;
        return t.isInDomain(n.value());
    }

    //
    // Expressions
    //

    public CondExprNode visit(CondExprNode node) {
        ExprNode c = checkCond(node.cond());
        ExprNode t = checkExpr(node.thenExpr());
        ExprNode e = checkExpr(node.elseExpr());
        if (t.type().isSameType(e.type())) {
            node.setExprs(c, t, e);
        }
        else if (t.type().isCompatible(e.type())) {   // insert cast on thenExpr
            node.setExprs(c, new CastNode(e.type(), t), e);
        }
        else if (e.type().isCompatible(t.type())) {   // insert cast on elseExpr
            node.setExprs(c, t, new CastNode(t.type(), e));
        }
        else {
            invalidCastError(t, e.type(), t.type());
        }
        return node;
    }

    // #@@range/BinaryOpNode{
    public BinaryOpNode visit(BinaryOpNode node) {
        node.setLeft(checkExpr(node.left()));
        node.setRight(checkExpr(node.right()));
        if (node.operator().equals("+")
                || node.operator().equals("-")) {
            expectsSameIntegerOrPointerDiff(node);
            return node;
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
            return node;
        }
        else if (node.operator().equals("==")
                || node.operator().equals("!=")
                || node.operator().equals("<")
                || node.operator().equals("<=")
                || node.operator().equals(">")
                || node.operator().equals(">=")) {
            expectsComparableScalars(node);
            return node;
        }
        else {
            throw new Error("unknown binary operator: " + node.operator());
        }
    }
    // #@@}

    public LogicalAndNode visit(LogicalAndNode node) {
        node.setLeft(checkExpr(node.left()));
        node.setRight(checkExpr(node.right()));
        expectsComparableScalars(node);
        return node;
    }

    public LogicalOrNode visit(LogicalOrNode node) {
        node.setLeft(checkExpr(node.left()));
        node.setRight(checkExpr(node.right()));
        expectsComparableScalars(node);
        return node;
    }

    /**
     * For + and -, only following types of expression are valid:
     *
     *   * integer + integer
     *   * pointer + integer
     *   * integer + pointer
     *   * integer - integer
     *   * pointer - integer
     */
    protected void expectsSameIntegerOrPointerDiff(BinaryOpNode node) {
        if (node.left().type().isDereferable()) {
            if (node.left().type().baseType().isVoid()) {
                wrongTypeError(node.left(), node.operator());
                return;
            }
            mustBeInteger(node.right(), node.operator());
            node.setType(node.left().type());
            node.setRight(multiplyPtrBaseSize(node.right(), node.left()));
        }
        else if (node.right().type().isDereferable()) {
            if (node.operator().equals("-")) {
                error(node, "invalid operation integer-pointer");
                return;
            }
            if (node.right().type().baseType().isVoid()) {
                wrongTypeError(node.right(), node.operator());
                return;
            }
            mustBeInteger(node.left(), node.operator());
            node.setType(node.right().type());
            node.setLeft(multiplyPtrBaseSize(node.left(), node.right()));
        }
        else {
            expectsSameInteger(node);
        }
    }

    protected BinaryOpNode multiplyPtrBaseSize(ExprNode expr, ExprNode ptr) {
        return new BinaryOpNode(integralPromotedExpr(expr), "*", ptrBaseSize(ptr));
    }

    protected ExprNode integralPromotedExpr(ExprNode expr) {
        Type t = integralPromotion(expr.type());
        if (t.isSameType(expr.type())) {
            return expr;
        }
        else {
            return new CastNode(t, expr);
        }
    }

    protected IntegerLiteralNode ptrBaseSize(ExprNode ptr) {
        return integerLiteral(ptr.location(),
                              typeTable.ptrDiffTypeRef(),
                              ptr.type().baseType().size());
    }

    protected IntegerLiteralNode integerLiteral(Location loc, TypeRef ref, long n) {
        IntegerLiteralNode node = new IntegerLiteralNode(loc, ref, n);
        bindType(node.typeNode());
        return node;
    }

    protected void bindType(TypeNode t) {
        t.setType(typeTable.get(t.typeRef()));
    }

    // +, -, *, /, %, &, |, ^, <<, >>
    // #@@range/expectsSameInteger{
    protected void expectsSameInteger(BinaryOpNode bin) {
        if (! mustBeInteger(bin.left(), bin.operator())) return;
        if (! mustBeInteger(bin.right(), bin.operator())) return;
        arithmeticImplicitCast(bin);
    }
    // #@@}

    // ==, !=, >, >=, <, <=, &&, ||
    protected void expectsComparableScalars(BinaryOpNode bin) {
        if (! mustBeScalar(bin.left(), bin.operator())) return;
        if (! mustBeScalar(bin.right(), bin.operator())) return;
        if (bin.left().type().isDereferable()) {
            bin.setType(bin.left().type());
            bin.setRight(forcePointerType(bin.left(), bin.right()));
            return;
        }
        if (bin.right().type().isDereferable()) {
            bin.setType(bin.right().type());
            bin.setLeft(forcePointerType(bin.right(), bin.left()));
            return;
        }
        arithmeticImplicitCast(bin);
    }

    // cast slave node to master node.
    protected ExprNode forcePointerType(ExprNode master, ExprNode slave) {
        if (master.type().isCompatible(slave.type())) {
            // needs no cast
            return slave;
        }
        else {
            warn(slave, "incompatible implicit cast from "
                       + slave.type() + " to " + master.type());
            return new CastNode(master.type(), slave);
        }
    }

    // Processes usual arithmetic conversion for binary operations.
    // #@@range/arithmeticImplicitCast{
    protected void arithmeticImplicitCast(BinaryOpNode bin) {
        Type r = integralPromotion(bin.right().type());
        Type l = integralPromotion(bin.left().type());
        Type target = usualArithmeticConversion(l, r);
        bin.setType(target);
        if (! l.isSameType(target)) {
            bin.setLeft(new CastNode(target, bin.left()));
        }
        if (! r.isSameType(target)) {
            bin.setRight(new CastNode(target, bin.right()));
        }
    }
    // #@@}

    // +, -, !, ~
    public UnaryOpNode visit(UnaryOpNode un) {
        ExprNode expr = checkExpr(un.expr());
        if (un.operator().equals("!")) {
            mustBeScalar(expr, un.operator());
        }
        else {
            mustBeInteger(expr, un.operator());
        }
        return new UnaryOpNode(un.operator(), expr);
    }

    // ++x, --x
    public PrefixOpNode visit(PrefixOpNode pre) {
        pre.setExpr(checkExpr(pre.expr()));
        expectsScalarLHS(pre);
        return pre;
    }

    // x++, x--
    public SuffixOpNode visit(SuffixOpNode suf) {
        suf.setExpr(checkExpr(suf.expr()));
        expectsScalarLHS(suf);
        return suf;
    }

    protected void expectsScalarLHS(UnaryArithmeticOpNode node) {
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
            node.setAmount(1);
        }
        else if (node.expr().type().isDereferable()) {
            if (node.expr().type().baseType().isVoid()) {
                // We cannot increment/decrement void*
                wrongTypeError(node.expr(), node.operator());
                return;
            }
            node.setAmount(node.expr().type().baseType().size());
        }
        else {
            throw new Error("must not happen");
        }
    }

    /**
     * For EXPR(ARG), checks:
     *
     *   * The number of argument matches function prototype.
     *   * ARG matches function prototype.
     *   * ARG is neither a struct nor an union.
     */
    public FuncallNode visit(FuncallNode node) {
        ExprNode expr = checkExpr(node.expr());
        FunctionType type = node.functionType();
        if (! type.acceptsArgc(node.numArgs())) {
            error(node, "wrong number of argments: " + node.numArgs());
            return node;
        }

        // Check type of only mandatory parameters.
        Iterator<ExprNode> args = node.arguments().iterator();
        List<ExprNode> newArgs = new ArrayList<ExprNode>();
        for (Type param : type.paramTypes()) {
            ExprNode arg = checkRHS(args.next());
            newArgs.add(isInvalidRHSType(arg.type()) ? arg : implicitCast(param, arg));
        }
        while (args.hasNext()) {
            newArgs.add(checkRHS(args.next()));
        }

        return new FuncallNode(expr, newArgs);
    }

    public ArefNode visit(ArefNode node) {
        ExprNode expr = checkExpr(node.expr());
        ExprNode index = checkExpr(node.index());
        mustBeInteger(index, "[]");
        return new ArefNode(expr, index);
    }

    public CastNode visit(CastNode node) {
        ExprNode expr = checkExpr(node.expr());
        if (! expr.type().isCastableTo(node.type())) {
            invalidCastError(node, expr.type(), node.type());
        }
        return new CastNode(node.typeNode(), expr);
    }

    public AddressNode visit(AddressNode node) {
        node.setExpr(checkExpr(node.expr()));
        return node;
    }

    public DereferenceNode visit(DereferenceNode node) {
        return new DereferenceNode(checkExpr(node.expr()));
    }

    public MemberNode visit(MemberNode node) {
        return new MemberNode(checkExpr(node.expr()), node.member());
    }

    public PtrMemberNode visit(PtrMemberNode node) {
        return new PtrMemberNode(checkExpr(node.expr()), node.member());
    }

    public SizeofExprNode visit(SizeofExprNode node) {
        node.setExpr(checkExpr(node.expr()));
        return node;
    }

    public SizeofTypeNode visit(SizeofTypeNode node) {
        return node;
    }

    public VariableNode visit(VariableNode node) {
        return node;
    }

    public IntegerLiteralNode visit(IntegerLiteralNode node) {
        return node;
    }

    public StringLiteralNode visit(StringLiteralNode node) {
        return node;
    }

    //
    // Utilities
    //

    protected ExprNode checkLHS(ExprNode expr) {
        ExprNode lhs = checkExpr(expr);
        if (lhs.isParameter()) {
            ;   // parameter is always assignable.
        }
        else if (isInvalidLHSType(lhs.type())) {
            error(lhs, "invalid LHS expression type: " + lhs.type());
        }
        return lhs;
    }

    protected ExprNode checkRHS(ExprNode expr) {
        ExprNode rhs = checkExpr(expr);
        if (isInvalidRHSType(rhs.type())) {
            error(rhs, "invalid RHS expression type: " + rhs.type());
        }
        return rhs;
    }

    // Processes forced-implicit-cast.
    // Applied To: return expr, assignment RHS, funcall argument
    protected ExprNode implicitCast(Type targetType, ExprNode expr) {
        if (expr.type().isSameType(targetType)) {
            return expr;
        }
        else if (expr.type().isCastableTo(targetType)) {
            if (! expr.type().isCompatible(targetType)
                    && ! isSafeIntegerCast(expr, targetType)) {
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
        if (    (l.isSameType(u_int) && r.isSameType(s_long))
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

    protected boolean isInvalidStatementType(Type t) {
        return t.isStruct() || t.isUnion();
    }

    protected boolean isInvalidReturnType(Type t) {
        return t.isStruct() || t.isUnion() || t.isArray();
    }

    protected boolean isInvalidParameterType(Type t) {
        return t.isStruct() || t.isUnion() || t.isVoid()
                || t.isIncompleteArray();
    }

    protected boolean isInvalidVariableType(Type t) {
        return t.isVoid() || (t.isArray() && ! t.isAllocatedArray());
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
