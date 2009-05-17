package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

class IRGenerator implements ASTVisitor<Void, Expr> {
    private ErrorHandler errorHandler;
    private TypeTable typeTable;

    // #@@range/ctor{
    public IRGenerator(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/generate{
    public IR generate(AST ast, TypeTable typeTable)
                            throws SemanticException {
        this.typeTable = typeTable;
        for (DefinedVariable var : ast.definedVariables()) {
            if (var.hasInitializer()) {
                var.setIR(transformExpr(var.initializer()));
            }
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            f.setIR(compileFunctionBody(f));
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("Simplify failed.");
        }
        return ast.ir();
    }
    // #@@}

    //
    // Definitions
    //

    private List<Stmt> stmts;
    private LinkedList<LocalScope> scopeStack;
    private LinkedList<Label> breakStack;
    private LinkedList<Label> continueStack;
    private Map<String, JumpEntry> jumpMap;

    public List<Stmt> compileFunctionBody(DefinedFunction f) {
        stmts = new ArrayList<Stmt>();
        scopeStack = new LinkedList<LocalScope>();
        breakStack = new LinkedList<Label>();
        continueStack = new LinkedList<Label>();
        jumpMap = new HashMap<String, JumpEntry>();
        transformStmt(f.body());
        checkJumpLinks(jumpMap);
        return stmts;
    }

    private int exprNestLevel = 0;

    private void transformStmt(StmtNode node) {
        node.accept(this);
    }

    private Expr transformExpr(ExprNode node) {
        exprNestLevel++;
        Expr e = node.accept(this);
        exprNestLevel--;
        return e;
    }

    private void transformStmt(ExprNode node) {
        node.accept(this);
    }

    private boolean isStatement() {
        return (exprNestLevel == 0);
    }

    private void label(Label label) {
        stmts.add(new LabelStmt(null, label));
    }

    private void jump(Label target) {
        stmts.add(new Jump(null, target));
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
        scopeStack.add(node.scope());
        for (DefinedVariable var : node.variables()) {
            if (var.hasInitializer()) {
                if (var.isPrivate()) {
                    // static variables
                    var.setIR(transformExpr(var.initializer()));
                }
                else {
                    assign(var.location(),
                        ref(var), transformExpr(var.initializer()));
                }
            }
        }
        for (StmtNode s : node.stmts()) {
            transformStmt(s);
        }
        scopeStack.removeLast();
        return null;
    }

    public Void visit(ExprStmtNode node) {
        // do not use transformStmt here, to receive compiled tree.
        Expr e = node.expr().accept(this);
        if (e != null) {
            //stmts.add(new ExprStmt(node.expr().location(), e));
            errorHandler.warn(node.location(), "useless expression");
        }
        return null;
    }

    public Void visit(IfNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();

        branch(node.location(),
                transformExpr(node.cond()),
                thenLabel,
                node.elseBody() == null ? endLabel : elseLabel);
        label(thenLabel);
        transformStmt(node.thenBody());
        jump(endLabel);
        if (node.elseBody() != null) {
            label(elseLabel);
            transformStmt(node.elseBody());
            jump(endLabel);
        }
        label(endLabel);
        return null;
    }

    public Void visit(SwitchNode node) {
        List<Case> cases = new ArrayList<Case>();
        Label endLabel = new Label();
        Label defaultLabel = endLabel;

        Expr cond = transformExpr(node.cond());
        for (CaseNode c : node.cases()) {
            if (c.isDefault()) {
                defaultLabel = c.label();
            }
            else {
                for (ExprNode val : c.values()) {
                    Expr v = transformExpr(val);
                    cases.add(new Case(((Int)v).value(), c.label()));
                }
            }
        }
        stmts.add(new Switch(node.location(), cond, cases, defaultLabel, endLabel));
        pushBreak(endLabel);
        for (CaseNode c : node.cases()) {
            label(c.label());
            transformStmt(c.body());
        }
        popBreak();
        label(endLabel);
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
        branch(node.location(),
                transformExpr(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(begLabel);
        pushBreak(endLabel);
        transformStmt(node.body());
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
        transformStmt(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        branch(node.location(), transformExpr(node.cond()), begLabel, endLabel);
        label(endLabel);
        return null;
    }

    public Void visit(ForNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label contLabel = new Label();
        Label endLabel = new Label();

        transformStmt(node.init());
        label(begLabel);
        branch(node.location(),
                transformExpr(node.cond()), bodyLabel, endLabel);
        label(bodyLabel);
        pushContinue(contLabel);
        pushBreak(endLabel);
        transformStmt(node.body());
        popBreak();
        popContinue();
        label(contLabel);
        transformStmt(node.incr());
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
            stmts.add(new LabelStmt(node.location(),
                    defineLabel(node.name(), node.location())));
            if (node.stmt() != null) {
                transformStmt(node.stmt());
            }
        }
        catch (SemanticException ex) {
            error(node, ex.getMessage());
        }
        return null;
    }

    public Void visit(GotoNode node) {
        stmts.add(new Jump(node.location(), referLabel(node.target())));
        return null;
    }

    public Void visit(ReturnNode node) {
        stmts.add(new Return(node.location(),
                node.expr() == null ? null : transformExpr(node.expr())));
        return null;
    }

    private void branch(Location loc, Expr cond, Label thenLabel, Label elseLabel) {
        stmts.add(new BranchIf(loc, cond, thenLabel, elseLabel));
    }

    private void assign(Expr lhs, Expr rhs) {
        assign(null, lhs, rhs);
    }

    private void assign(Location loc, Expr lhs, Expr rhs) {
        stmts.add(new Assign(loc, addressOf(lhs), rhs));
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

    public Expr visit(CondExprNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        Expr cond = transformExpr(node.cond());
        branch(node.location(), cond, thenLabel, elseLabel);
        label(thenLabel);
        assign(ref(var), transformExpr(node.thenExpr()));
        jump(endLabel);
        label(elseLabel);
        assign(ref(var), transformExpr(node.elseExpr()));
        jump(endLabel);
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    public Expr visit(LogicalAndNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(ref(var), transformExpr(node.left()));
        branch(node.location(), ref(var), rightLabel, endLabel);
        label(rightLabel);
        assign(ref(var), transformExpr(node.right()));
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    public Expr visit(LogicalOrNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(ref(var), transformExpr(node.left()));
        branch(node.location(), ref(var), endLabel, rightLabel);
        label(rightLabel);
        assign(ref(var), transformExpr(node.right()));
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    public Expr visit(AssignNode node) {
        if (isStatement()) {
            // Evaluate RHS before LHS.
            Expr rhs = transformExpr(node.rhs());
            assign(transformExpr(node.lhs()), rhs);
            return null;
        }
        else {
            // lhs = rhs -> tmp = rhs, lhs = tmp, tmp
            DefinedVariable tmp = tmpVar(node.rhs().type());
            assign(ref(tmp), transformExpr(node.rhs()));
            assign(transformExpr(node.lhs()), ref(tmp));
            return ref(tmp);
        }
    }

    public Expr visit(OpAssignNode node) {
        // Evaluate RHS before LHS.
        Expr rhs = transformExpr(node.rhs());
        Expr lhs = transformExpr(node.lhs());
        return transformOpAssign(lhs,
                Op.internBinary(node.operator(), node.rhs().type().isSigned()),
                rhs,
                node.lhs().type());
    }

    private Expr transformOpAssign(Expr lhs, Op op, Expr _rhs, Type lhsType) {
        Expr rhs = expandPointerArithmetic(_rhs, op, lhsType);
        if (lhs.isConstantAddress()) {
            // lhs = lhs op rhs, lhs
            assign(lhs, new Bin(lhs.type(), op, lhs, rhs));
            return isStatement() ? null : lhs;
        }
        else {
            // a = &lhs, *a = *a op rhs, *a
            Expr addr = addressOf(lhs);
            DefinedVariable a = tmpVar(pointerTo(lhsType));
            assign(ref(a), addr);
            assign(deref(a), new Bin(lhs.type(), op, deref(a), rhs));
            return isStatement() ? null : deref(a);
        }
    }

    private Expr expandPointerArithmetic(Expr rhs, Op op, Type lhsType) {
        switch (op) {
        case ADD:
        case SUB:
            if (lhsType.isPointer()) {
                return new Bin(rhs.type(), Op.MUL,
                    rhs, ptrDiff(lhsType.baseType().size()));
            }
        }
        return rhs;
    }

    // transform node into: lhs += 1 or lhs -= 1
    public Expr visit(PrefixOpNode node) {
        return transformOpAssign(transformExpr(node.expr()),
                binOp(node.operator()),
                intValue(1),
                node.expr().type());
    }

    public Expr visit(SuffixOpNode node) {
        Expr lhs = transformExpr(node.expr());
        Op op = binOp(node.operator());
        if (isStatement()) {
            // expr++; -> expr += 1;
            transformOpAssign(lhs, op, intValue(1), node.expr().type());
            return null;
        }
        else if (lhs.isConstantAddress()) {
            // f(expr++) -> v = expr; expr = expr + 1, f(v)
            DefinedVariable v = tmpVar(node.expr().type());
            assign(ref(v), lhs);
            Expr rhs = expandPointerArithmetic(intValue(1),
                    op, node.expr().type());
            assign(lhs, new Bin(lhs.type(), op, lhs, rhs));
            return ref(v);
        }
        else {
            // f(expr++) -> a = &expr, v = *a; *a = *a + 1, f(v)
            Expr addr = addressOf(lhs);
            DefinedVariable a = tmpVar(pointerTo(node.expr().type()));
            DefinedVariable v = tmpVar(node.expr().type());
            assign(ref(a), addr);
            assign(ref(v), deref(a));
            assign(deref(a),
                new Bin(lhs.type(), op,
                    deref(a),
                    expandPointerArithmetic(intValue(1),
                            op, node.expr().type())));
            return ref(v);
        }
    }

    public Expr visit(FuncallNode node) {
        List<Expr> newArgs = new ArrayList<Expr>();
        ListIterator<ExprNode> args = node.finalArg();
        while (args.hasPrevious()) {
            newArgs.add(0, transformExpr(args.previous()));
        }
        if (isStatement()) {
            stmts.add(
                new ExprStmt(node.location(),
                    new Call(asmType(node.type()),
                            transformExpr(node.expr()),
                            newArgs)));
            return null;
        }
        else {
            DefinedVariable tmp = tmpVar(node.type());
            assign(node.location(),
                ref(tmp),
                new Call(asmType(node.type()),
                        transformExpr(node.expr()),
                        newArgs));
            return ref(tmp);
        }
    }

    //
    // Expressions (no side effects)
    //

    // #@@range/BinaryOp{
    public Expr visit(BinaryOpNode node) {
        Expr right = transformExpr(node.right());
        Expr left = transformExpr(node.left());
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (node.left().isPointer() && node.right().isPointer()) {
                Expr tmp = new Bin(asmType(node.type()),
                        Op.internBinary(node.operator(),
                                        node.type().isSigned()),
                        left, right);
                return new Bin(ptrDiffType(), Op.S_DIV,
                        tmp, ptrDiff(node.left().baseSize()));
            }
            else if (node.left().isPointer()) {
                right = new Bin(ptrDiffType(), Op.MUL,
                        right, ptrDiff(node.left().baseSize()));
            }
            else if (node.right().isPointer()) {
                left = new Bin(ptrDiffType(), Op.MUL,
                        left, ptrDiff(node.right().baseSize()));
            }
        }
        return new Bin(asmType(node.type()),
                Op.internBinary(node.operator(), node.type().isSigned()),
                left, right);
    }
    // #@@}

    // #@@range/UnaryOp{
    public Expr visit(UnaryOpNode node) {
        if (node.operator().equals("+")) {
            // +expr -> expr
            return transformExpr(node.expr());
        }
        else {
            return new Uni(asmType(node.type()),
                    Op.internUnary(node.operator()),
                    transformExpr(node.expr()));
        }
    }
    // #@@}

    public Expr visit(ArefNode node) {
        Expr offset = new Bin(signedInt(), Op.MUL,
                intValue(node.elementSize()), transformArrayIndex(node));
        return deref(
            new Bin(pointer(), Op.ADD,
                    transformExpr(node.baseExpr()), offset),
            node.type());
    }

    // For multidimension array: t[e][d][c][b][a];
    // &a[a0][b0][c0][d0][e0]
    //     = &a + edcb*a0 + edc*b0 + ed*c0 + e*d0 + e0
    //     = &a + (((((a0)*b + b0)*c + c0)*d + d0)*e + e0) * sizeof(t)
    //
    private Expr transformArrayIndex(ArefNode node) {
        if (node.isMultiDimension()) {
            return new Bin(signedInt(), Op.ADD,
                    transformExpr(node.index()),
                    new Bin(signedInt(), Op.MUL,
                            intValue(node.length()),
                            transformArrayIndex((ArefNode)node.expr())));
        }
        else {
            return transformExpr(node.index());
        }
    }

    // #@@range/Member{
    public Expr visit(MemberNode node) {
        Expr addr = new Bin(pointer(), Op.ADD,
            addressOf(transformExpr(node.expr())),
            intValue(node.offset()));
        return node.isLoadable() ? deref(addr, node.type()) : addr;
    }
    // #@@}

    public Expr visit(PtrMemberNode node) {
        Expr addr = new Bin(pointer(), Op.ADD,
            transformExpr(node.expr()),
            intValue(node.offset()));
        return node.isLoadable() ? deref(addr, node.type()) : addr;
    }

    // #@@range/Dereference{
    public Expr visit(DereferenceNode node) {
        return new Mem(asmType(node.type()), transformExpr(node.expr()));
    }
    // #@@}

    public Expr visit(AddressNode node) {
        Expr e = transformExpr(node.expr());
        return node.expr().isLoadable() ? addressOf(e) : e;
    }

    public Expr visit(CastNode node) {
        if (node.isEffectiveCast()) {
            return new Uni(asmType(node.type()),
                    node.expr().type().isSigned() ? Op.S_CAST : Op.U_CAST,
                    transformExpr(node.expr()));
        }
        else if (isStatement()) {
            transformStmt(node.expr());
            return null;
        }
        else {
            return transformExpr(node.expr());
        }
    }

    public Expr visit(SizeofExprNode node) {
        return intValue(node.expr().allocSize());
    }

    public Expr visit(SizeofTypeNode node) {
        return intValue(node.operand().allocSize());
    }

    public Expr visit(VariableNode node) {
        if (node.entity().isConstant()) {
            return transformExpr(node.entity().value());
        }
        Var var = new Var(varType(node.type()), node.entity());
        return node.isLoadable() ? var : addressOf(var);
    }

    public Expr visit(IntegerLiteralNode node) {
        return new Int(asmType(node.type()), node.value());
    }

    public Expr visit(StringLiteralNode node) {
        return new Str(asmType(node.type()), node.entry());
    }

    //
    // Utilities
    //

    // unary ops -> binary ops
    private Op binOp(String uniOp) {
        return uniOp.equals("++") ? Op.ADD : Op.SUB;
    }

    // #@@range/addressOf{
    private Expr addressOf(Expr expr) {
        return expr.addressNode(pointer());
    }
    // #@@}

    private Var ref(DefinedVariable var) {
        return new Var(varType(var.type()), var);
    }

    // add DereferenceNode on top of the var.
    private Mem deref(DefinedVariable var) {
        return deref(ref(var), var.type().baseType());
    }

    // add DereferenceNode on top of the expr.
    private Mem deref(Expr expr, Type t) {
        return new Mem(asmType(t), expr);
    }

    // #@@range/intValue{
    private Int intValue(long n) {
        return new Int(signedInt(), n);
    }
    // #@@}

    private Int ptrDiff(long n) {
        return new Int(ptrDiffType(), n);
    }

    private Type pointerTo(Type t) {
        return typeTable.pointerTo(t);
    }

    private net.loveruby.cflat.asm.Type asmType(Type t) {
        if (t.isVoid()) return signedInt();
        return net.loveruby.cflat.asm.Type.get(t.size());
    }

    private net.loveruby.cflat.asm.Type varType(Type t) {
        if (t.size() == 0 || t.size() > typeTable.maxIntSize()) {
            return null;
        }
        return net.loveruby.cflat.asm.Type.get(t.size());
    }

    private net.loveruby.cflat.asm.Type signedInt() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.intSize());
    }

    private net.loveruby.cflat.asm.Type pointer() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.pointerSize());
    }

    private net.loveruby.cflat.asm.Type ptrDiffType() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.pointerSize());
    }

    private void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
