package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.type.TypeRef;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

class Simplifier implements ASTVisitor<Void, ExprNode> {
    private ErrorHandler errorHandler;
    private TypeTable typeTable;

    // #@@range/ctor{
    public Simplifier(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/transform{
    public IR transform(AST ast) throws SemanticException {
        typeTable = ast.typeTable();
        for (DefinedVariable var : ast.definedVariables()) {
            transformInitializer(var);
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            visit(f);
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("Simplify failed.");
        }
        return ast.ir();
    }
    // #@@}

    private void transformInitializer(DefinedVariable var) {
        if (var.initializer() != null) {
            var.setInitializer(transform(var.initializer()));
        }
    }

    //
    // Definitions
    //

    private List<StmtNode> stmts;
    private LinkedList<LocalScope> scopeStack;
    private LinkedList<Label> breakStack;
    private LinkedList<Label> continueStack;
    private Map<String, JumpEntry> jumpMap;

    public Void visit(DefinedFunction f) {
        stmts = new ArrayList<StmtNode>();
        scopeStack = new LinkedList<LocalScope>();
        breakStack = new LinkedList<Label>();
        continueStack = new LinkedList<Label>();
        jumpMap = new HashMap<String, JumpEntry>();
        transform(f.body());
        checkJumpLinks(jumpMap);
        f.setIR(stmts);
        return null;
    }

    private int beforeStmt;
    private int exprNestLevel = 0;

    private void transform(StmtNode node) {
        beforeStmt = stmts.size();
        node.accept(this);
    }

    private ExprNode transform(ExprNode node) {
        exprNestLevel++;
        ExprNode e = node.accept(this);
        exprNestLevel--;
        return e;
    }

    private boolean isStatement() {
        return (exprNestLevel <= 1);
    }

    // insert node before the current statement.
    private void assignBeforeStmt(ExprNode lhs, ExprNode rhs) {
        stmts.add(beforeStmt, new AssignStmtNode(null, lhs, rhs));
        beforeStmt++;
    }

    private void addExprStmt(ExprNode expr) {
        ExprNode n = transform(expr);
        if (n != null) {
            stmts.add(new ExprStmtNode(expr.location(), n));
        }
    }

    private void label(Label id) {
        stmts.add(new LabelNode(id));
    }

    private void jump(Label target) {
        stmts.add(new GotoNode(target));
    }

    private void pushBreak(Label label) {
        breakStack.add(label);
    }

    private void popBreak() {
        if (breakStack.isEmpty()) {
            throw new Error("unmatched push/pop for break stack");
        }
        breakStack.removeLast();
    }

    private Label currentBreakTarget() {
        if (breakStack.isEmpty()) {
            throw new JumpError("break from out of loop");
        }
        return breakStack.getLast();
    }

    private void pushContinue(Label label) {
        continueStack.add(label);
    }

    private void popContinue() {
        if (continueStack.isEmpty()) {
            throw new Error("unmatched push/pop for continue stack");
        }
        continueStack.removeLast();
    }

    private Label currentContinueTarget() {
        if (continueStack.isEmpty()) {
            throw new JumpError("continue from out of loop");
        }
        return continueStack.getLast();
    }

    //
    // Statements
    //

    public Void visit(BlockNode node) {
        for (DefinedVariable var : node.variables()) {
            if (var.initializer() != null) {
                assign(var.location(), ref(var), var.initializer());
            }
        }
        scopeStack.add(node.scope());
        for (StmtNode s : node.stmts()) {
            transform(s);
        }
        scopeStack.removeLast();
        return null;
    }

    public Void visit(ExprStmtNode node) {
        addExprStmt(node.expr());
        return null;
    }

    public Void visit(IfNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        branch(transform(node.cond()),
                thenLabel,
                node.elseBody() == null ? endLabel : elseLabel);
        label(thenLabel);
        transform(node.thenBody());
        jump(endLabel);
        if (node.elseBody() != null) {
            label(elseLabel);
            transform(node.elseBody());
            jump(endLabel);
        }
        label(endLabel);
        return null;
    }

    public Void visit(SwitchNode node) {
        stmts.add(node);
        node.setCond(transform(node.cond()));
        for (CaseNode c : node.cases()) {
            label(c.beginLabel());
            transform(c.body());
            jump(node.endLabel());
        }
        return null;
    }

    public Void visit(CaseNode node) {
        throw new Error("must not happen");
    }

    public Void visit(WhileNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label endLabel = new Label();
        label(begLabel);
        branch(transform(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(begLabel);
        pushBreak(endLabel);
        transform(node.body());
        popBreak();
        popContinue();
        jump(begLabel);
        label(endLabel);
        return null;
    }

    public Void visit(DoWhileNode node) {
        Label begLabel = new Label();
        Label contLabel = new Label();  // before cond (end of body)
        Label endLabel = new Label();

        pushContinue(contLabel);
        pushBreak(endLabel);
        label(begLabel);
        transform(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        branch(transform(node.cond()), begLabel, endLabel);
        label(endLabel);
        return null;
    }

    public Void visit(ForNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label contLabel = new Label();
        Label endLabel = new Label();

        transform(node.init());
        label(begLabel);
        branch(transform(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(contLabel);
        pushBreak(endLabel);
        transform(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        transform(node.incr());
        jump(begLabel);
        label(endLabel);
        return null;
    }

    public Void visit(BreakNode node) {
        try {
            jump(currentBreakTarget());
        }
        catch (JumpError err) {
            error(node, err.getMessage());
        }
        return null;
    }

    public Void visit(ContinueNode node) {
        try {
            jump(currentContinueTarget());
        }
        catch (JumpError err) {
            error(node, err.getMessage());
        }
        return null;
    }

    public Void visit(LabelNode node) {
        try {
            label(defineLabel(node.name(), node.location()));
            if (node.stmt() != null) {
                transform(node.stmt());
            }
        }
        catch (SemanticException ex) {
            error(node, ex.getMessage());
        }
        return null;
    }

    public Void visit(GotoNode node) {
        jump(referLabel(node.target()));
        return null;
    }

    public Void visit(ReturnNode node) {
        if (node.expr() != null) {
            node.setExpr(transform(node.expr()));
        }
        stmts.add(node);
        return null;
    }

    private void branch(ExprNode cond, Label thenLabel, Label elseLabel) {
        stmts.add(new BranchIfNode(cond.location(),
                cond, thenLabel, elseLabel));
    }

    private void assign(ExprNode lhs, ExprNode rhs) {
        assign(null, lhs, rhs);
    }

    private void assign(Location loc, ExprNode lhs, ExprNode rhs) {
        stmts.add(new AssignStmtNode(loc, lhs, rhs));
    }

    private DefinedVariable tmpVar(Type t) {
        return scopeStack.getLast().allocateTmp(t);
    }

    class JumpEntry {
        public Label label;
        public long numRefered;
        public boolean isDefined;
        public Location location;

        public JumpEntry(Label label) {
            this.label = label;
            numRefered = 0;
            isDefined = false;
        }
    }

    private Label defineLabel(String name, Location loc)
                                    throws SemanticException {
        JumpEntry ent = getJumpEntry(name);
        if (ent.isDefined) {
            throw new SemanticException(
                "duplicated jump labels in " + name + "(): " + name);
        }
        ent.isDefined = true;
        ent.location = loc;
        return ent.label;
    }

    private Label referLabel(String name) {
        JumpEntry ent = getJumpEntry(name);
        ent.numRefered++;
        return ent.label;
    }

    private JumpEntry getJumpEntry(String name) {
        JumpEntry ent = jumpMap.get(name);
        if (ent == null) {
            ent = new JumpEntry(new Label());
            jumpMap.put(name, ent);
        }
        return ent;
    }

    private void checkJumpLinks(Map<String, JumpEntry> jumpMap) {
        for (Map.Entry<String, JumpEntry> ent : jumpMap.entrySet()) {
            String labelName = ent.getKey();
            JumpEntry jump = ent.getValue();
            if (!jump.isDefined) {
                errorHandler.error(jump.location,
                        "undefined label: " + labelName);
            }
            if (jump.numRefered == 0) {
                errorHandler.warn(jump.location,
                        "useless label: " + labelName);
            }
        }
    }

    //
    // Expressions (with side effects)
    //

    public ExprNode visit(CondExprNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        ExprNode cond = transform(node.cond());
        branch(cond, thenLabel, elseLabel);
        label(thenLabel);
        assign(ref(var), transform(node.thenExpr()));
        jump(endLabel);
        label(elseLabel);
        assign(ref(var), transform(node.elseExpr()));
        jump(endLabel);
        label(endLabel);
        return ref(var);
    }

    public ExprNode visit(LogicalAndNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(ref(var), transform(node.left()));
        branch(ref(var), rightLabel, endLabel);
        label(rightLabel);
        assign(ref(var), transform(node.right()));
        label(endLabel);
        return ref(var);
    }

    public ExprNode visit(LogicalOrNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(ref(var), transform(node.left()));
        branch(ref(var), endLabel, rightLabel);
        label(rightLabel);
        assign(ref(var), transform(node.right()));
        label(endLabel);
        return ref(var);
    }

    public ExprNode visit(AssignNode node) {
        if (isStatement()) {
            assign(transform(node.lhs()), transform(node.rhs()));
            return null;
        }
        else {
            DefinedVariable tmp = tmpVar(node.rhs().type());
            assignBeforeStmt(ref(tmp), transform(node.rhs()));
            assignBeforeStmt(transform(node.lhs()), ref(tmp));
            return ref(tmp);
        }
    }

    public ExprNode visit(OpAssignNode node) {
        // evaluate rhs before lhs.
        ExprNode rhs = transform(node.rhs());
        ExprNode lhs = transform(node.lhs());
        return transformOpAssign(lhs, node.operator(), rhs);
    }

    private ExprNode transformOpAssign(ExprNode lhs, String op, ExprNode rhs) {
        rhs = expandPointerArithmetic(rhs, op, lhs);
        if (isStatement()) {
            if (lhs.isConstantAddress()) {
                // lhs = lhs op rhs
                assign(lhs, binaryOp(lhs, op, rhs));
            }
            else {
                // a = &lhs, *a = *a op rhs
                ExprNode addr = addressOf(lhs);
                DefinedVariable a = tmpVar(addr.type());
                assign(ref(a), addr);
                assign(deref(a), binaryOp(deref(a), op, rhs));
            }
            return null;
        }
        else {
            // a = &lhs, *a = *a op rhs, *a
            ExprNode addr = addressOf(lhs);
            DefinedVariable a = tmpVar(addr.type());
            assignBeforeStmt(ref(a), addr);
            assignBeforeStmt(deref(a), binaryOp(deref(a), op, rhs));
            return deref(a);
        }
    }

    private ExprNode expandPointerArithmetic(ExprNode rhs, String op, ExprNode lhs) {
        if ((op.equals("+") || op.equals("-")) && lhs.type().isDereferable()) {
            return multiplyPtrBaseSize(rhs, lhs);
        }
        else {
            return rhs;
        }
    }

    // transform node into: lhs += 1 or lhs -= 1
    public ExprNode visit(PrefixOpNode node) {
        return transformOpAssign(transform(node.expr()),
                binOp(node.operator()),
                intValue(1));
    }

    public ExprNode visit(SuffixOpNode node) {
        if (isStatement()) {
            return transformOpAssign(transform(node.expr()),
                    binOp(node.operator()),
                    intValue(1));
        }
        else {
            // f(expr++) -> a = &expr, *a += 1, f(*a - 1)
            // f(expr--) -> a = &expr, *a -= 1, f(*a + 1)
            ExprNode addr = addressOf(transform(node.expr()));
            DefinedVariable tmp = tmpVar(addr.type());
            String op = binOp(node.operator());
            assignBeforeStmt(ref(tmp), addr);
            ExprNode lhs = transformOpAssign(deref(tmp), op, intValue(1));
            ExprNode rhs = expandPointerArithmetic(intValue(1), op, lhs);
            return binaryOp(lhs, invert(op), rhs);
        }
    }

    public ExprNode visit(FuncallNode node) {
        List<ExprNode> newArgs = new ArrayList<ExprNode>();
        ListIterator<ExprNode> args = node.finalArg();
        while (args.hasPrevious()) {
            newArgs.add(0, transform(args.previous()));
        }
        node.setExpr(transform(node.expr()));
        node.replaceArgs(newArgs);
        return node;
    }

    //
    // Expressions (no side effects)
    //

    // #@@range/BinaryOpNode{
    public ExprNode visit(BinaryOpNode node) {
        ExprNode left = transform(node.left());
        ExprNode right = transform(node.right());
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (left.type().isDereferable()) {
                right = multiplyPtrBaseSize(right, left);
            }
            else if (right.type().isDereferable()) {
                left = multiplyPtrBaseSize(left, right);
            }
        }
        node.setLeft(left);
        node.setRight(right);
        return node;
    }
    // #@@}

    private BinaryOpNode multiplyPtrBaseSize(ExprNode expr, ExprNode ptr) {
        return binaryOp(expr, "*", ptrBaseSize(ptr));
    }

    private IntegerLiteralNode ptrBaseSize(ExprNode ptr) {
        return ptrDiff(ptr.type().baseType().size(), ptr.location());
    }

    public ExprNode visit(UnaryOpNode node) {
        if (node.operator().equals("+")) {
            // +expr -> expr
            return transform(node.expr());
        }
        else {
            node.setExpr(transform(node.expr()));
            return node;
        }
    }

    public ExprNode visit(ArefNode node) {
        ExprNode offset = binaryOp(intValue(node.elementSize()),
                            "*", transformArrayIndex(node));
        return deref(binaryOp(transform(node.baseExpr()), "+", offset));
    }

    // For multidimension array: t[e][d][c][b][a];
    // &a[a0][b0][c0][d0][e0]
    //     = &a + edcb*a0 + edc*b0 + ed*c0 + e*d0 + e0
    //     = &a + (((((a0)*b + b0)*c + c0)*d + d0)*e + e0) * sizeof(t)
    //
    // #@@range/transformArrayIndex{
    private ExprNode transformArrayIndex(ArefNode node) {
        if (node.isMultiDimension()) {
            return binaryOp(transform(node.index()),
                    "+", binaryOp(intValue(node.length()),
                            "*", transformArrayIndex((ArefNode)node.expr())));
        }
        else {
            return transform(node.index());
        }
    }
    // #@@}

    public ExprNode visit(MemberNode node) {
        ExprNode addr = binaryOp(pointerTo(node.type()),
            addressOf(transform(node.expr())),
            "+",
            intValue(node.offset()));
        return node.shouldEvaluatedToAddress() ? addr : deref(addr);
    }

    public ExprNode visit(PtrMemberNode node) {
        ExprNode addr = binaryOp(pointerTo(node.type()),
            transform(node.expr()),
            "+",
            intValue(node.offset()));
        return node.shouldEvaluatedToAddress() ? addr : deref(addr);
    }

    public ExprNode visit(DereferenceNode node) {
        node.setExpr(transform(node.expr()));
        return node;
    }

    public ExprNode visit(AddressNode node) {
        node.setExpr(transform(node.expr()));
        return node;
    }

    public ExprNode visit(CastNode node) {
        return new CastNode(node.typeNode(), transform(node.expr()));
    }

    public ExprNode visit(SizeofExprNode node) {
        return intValue(node.expr().type().allocSize());
    }

    public ExprNode visit(SizeofTypeNode node) {
        return intValue(node.operand().allocSize());
    }

    public ExprNode visit(VariableNode node) {
        return node.shouldEvaluatedToAddress() ? addressOf(node) : node;
    }

    public ExprNode visit(IntegerLiteralNode node) {
        return node;
    }

    public ExprNode visit(StringLiteralNode node) {
        return node;
    }

    //
    // Utilities
    //

    // unary ops -> binary ops
    private String binOp(String uniOp) {
        return uniOp.equals("++") ? "+" : "-";
    }

    // invert binary ops
    private String invert(String op) {
        return op.equals("+") ? "-" : "+";
    }

    // add AddressNode on top of the expr.
    private ExprNode addressOf(ExprNode expr) {
        if (expr instanceof DereferenceNode) {
            return ((DereferenceNode)expr).expr();
        }
        else {
            AddressNode n = new AddressNode(expr);
            Type base = expr.type();
            n.setType(expr.shouldEvaluatedToAddress() ? base : pointerTo(base));
            return n;
        }
    }

    private VariableNode ref(DefinedVariable var) {
        return new VariableNode(var);
    }

    // add DereferenceNode on top of the var.
    private DereferenceNode deref(DefinedVariable var) {
        return deref(ref(var));
    }

    // add DereferenceNode on top of the expr.
    private DereferenceNode deref(ExprNode expr) {
        return new DereferenceNode(expr);
    }

    private Type pointerTo(Type t) {
        return typeTable.pointerTo(t);
    }

    private BinaryOpNode binaryOp(ExprNode left, String op, ExprNode right) {
        return new BinaryOpNode(left, op, right);
    }

    private BinaryOpNode binaryOp(Type t, ExprNode left, String op, ExprNode right) {
        return new BinaryOpNode(t, left, op, right);
    }

    private IntegerLiteralNode intValue(long n) {
        // FIXME?: location
        return ptrDiff(n, null);
    }

    private IntegerLiteralNode ptrDiff(long n, Location loc) {
        return integerLiteral(loc, typeTable.ptrDiffTypeRef(), n);
    }

    private IntegerLiteralNode integerLiteral(Location loc, TypeRef ref, long n) {
        IntegerLiteralNode node = new IntegerLiteralNode(loc, ref, n);
        bindType(node.typeNode());
        return node;
    }

    private void bindType(TypeNode t) {
        t.setType(typeTable.get(t.typeRef()));
    }

    private void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }

    //
    // must not reached
    //

    public Void visit(UndefinedFunction func) {
        throw new Error("must not happen");
    }

    public Void visit(DefinedVariable var) {
        throw new Error("must not happen");
    }

    public Void visit(UndefinedVariable func) {
        throw new Error("must not happen");
    }

    public Void visit(StructNode struct) {
        throw new Error("must not happen");
    }

    public Void visit(UnionNode union) {
        throw new Error("must not happen");
    }

    public Void visit(TypedefNode typedef) {
        throw new Error("must not happen");
    }

    public Void visit(AssignStmtNode node) {
        throw new Error("must not happen");
    }

    public Void visit(BranchIfNode node) {
        throw new Error("must not happen");
    }
}
