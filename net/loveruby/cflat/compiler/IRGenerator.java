package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.utils.ListUtils;
import net.loveruby.cflat.exception.*;
import java.util.*;

class IRGenerator implements ASTVisitor<Void, Expr> {
    private final TypeTable typeTable;
    private final ErrorHandler errorHandler;

    // #@@range/ctor{
    public IRGenerator(TypeTable typeTable, ErrorHandler errorHandler) {
        this.typeTable = typeTable;
        this.errorHandler = errorHandler;
    }
    // #@@}

    // #@@range/generate{
    public IR generate(AST ast) throws SemanticException {
        for (DefinedVariable var : ast.definedVariables()) {
            if (var.hasInitializer()) {
                var.setIR(transformExpr(var.initializer()));
            }
        }
        for (DefinedFunction f : ast.definedFunctions()) {
            f.setIR(compileFunctionBody(f));
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("IR generation failed.");
        }
        return ast.ir();
    }
    // #@@}

    //
    // Definitions
    //

    // #@@range/compileFunctionBody{
    List<Stmt> stmts;
    LinkedList<LocalScope> scopeStack;
    LinkedList<Label> breakStack;
    LinkedList<Label> continueStack;
    Map<String, JumpEntry> jumpMap;

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
    // #@@}

    // #@@range/transformStmt_stmt{
    private void transformStmt(StmtNode node) {
        node.accept(this);
    }
    // #@@}

    // #@@range/transformStmt_expr{
    private void transformStmt(ExprNode node) {
        node.accept(this);
    }
    // #@@}

    // #@@range/transformExpr{
    private int exprNestLevel = 0;

    private Expr transformExpr(ExprNode node) {
        exprNestLevel++;
        Expr e = node.accept(this);
        exprNestLevel--;
        return e;
    }
    // #@@}

    // #@@range/isStatement{
    private boolean isStatement() {
        return (exprNestLevel == 0);
    }
    // #@@}

    // #@@range/assign{
    private void assign(Location loc, Expr lhs, Expr rhs) {
        stmts.add(new Assign(loc, addressOf(lhs), rhs));
    }
    // #@@}

    private DefinedVariable tmpVar(Type t) {
        return scopeStack.getLast().allocateTmp(t);
    }

    private void label(Label label) {
        stmts.add(new LabelStmt(null, label));
    }

    private void jump(Location loc, Label target) {
        stmts.add(new Jump(loc, target));
    }

    private void jump(Label target) {
        jump(null, target);
    }

    private void cjump(Location loc, Expr cond, Label thenLabel, Label elseLabel) {
        stmts.add(new CJump(loc, cond, thenLabel, elseLabel));
    }

    // #@@range/pushBreak{
    private void pushBreak(Label label) {
        breakStack.add(label);
    }
    // #@@}

    // #@@range/popBreak{
    private void popBreak() {
        if (breakStack.isEmpty()) {
            throw new Error("unmatched push/pop for break stack");
        }
        breakStack.removeLast();
    }
    // #@@}

    // #@@range/currentBreakTarget{
    private Label currentBreakTarget() {
        if (breakStack.isEmpty()) {
            throw new JumpError("break from out of loop");
        }
        return breakStack.getLast();
    }
    // #@@}

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

    // #@@range/If{
    public Void visit(IfNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        Expr cond = transformExpr(node.cond());
        if (node.elseBody() == null) {
            // #@@range/If_noelse{
            cjump(node.location(), cond, thenLabel, endLabel);
            label(thenLabel);
            transformStmt(node.thenBody());
            label(endLabel);
            // #@@}
        }
        else {
            // #@@range/If_withelse{
            cjump(node.location(), cond, thenLabel, elseLabel);
            label(thenLabel);
            transformStmt(node.thenBody());
            jump(endLabel);
            label(elseLabel);
            transformStmt(node.elseBody());
            label(endLabel);
            // #@@}
        }
        return null;
    }
    // #@@}

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

    // #@@range/While{
    public Void visit(WhileNode node) {
        Label begLabel = new Label();
        Label bodyLabel = new Label();
        Label endLabel = new Label();

        label(begLabel);
        cjump(node.location(),
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
    // #@@}

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
        cjump(node.location(), transformExpr(node.cond()), begLabel, endLabel);
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
        cjump(node.location(),
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

    // #@@range/Break{
    public Void visit(BreakNode node) {
        try {
            jump(node.location(), currentBreakTarget());
        }
        catch (JumpError err) {
            error(node, err.getMessage());
        }
        return null;
    }
    // #@@}

    public Void visit(ContinueNode node) {
        try {
            jump(node.location(), currentContinueTarget());
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
        jump(node.location(), referLabel(node.target()));
        return null;
    }

    public Void visit(ReturnNode node) {
        stmts.add(new Return(node.location(),
                node.expr() == null ? null : transformExpr(node.expr())));
        return null;
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
    // Expressions (with branches)
    //

    public Expr visit(CondExprNode node) {
        Label thenLabel = new Label();
        Label elseLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        Expr cond = transformExpr(node.cond());
        cjump(node.location(), cond, thenLabel, elseLabel);
        label(thenLabel);
        assign(node.thenExpr().location(),
                ref(var), transformExpr(node.thenExpr()));
        jump(endLabel);
        label(elseLabel);
        assign(node.elseExpr().location(),
                ref(var), transformExpr(node.elseExpr()));
        jump(endLabel);
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    public Expr visit(LogicalAndNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(node.left().location(),
                ref(var), transformExpr(node.left()));
        cjump(node.location(), ref(var), rightLabel, endLabel);
        label(rightLabel);
        assign(node.right().location(),
                ref(var), transformExpr(node.right()));
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    public Expr visit(LogicalOrNode node) {
        Label rightLabel = new Label();
        Label endLabel = new Label();
        DefinedVariable var = tmpVar(node.type());

        assign(node.left().location(),
                ref(var), transformExpr(node.left()));
        cjump(node.location(), ref(var), endLabel, rightLabel);
        label(rightLabel);
        assign(node.right().location(),
                ref(var), transformExpr(node.right()));
        label(endLabel);
        return isStatement() ? null : ref(var);
    }

    //
    // Expressions (with side effects)
    //

    // #@@range/Assign{
    public Expr visit(AssignNode node) {
        Location lloc = node.lhs().location();
        Location rloc = node.rhs().location();
        if (isStatement()) {
            // Evaluate RHS before LHS.
            // #@@range/Assign_stmt{
            Expr rhs = transformExpr(node.rhs());
            assign(lloc, transformExpr(node.lhs()), rhs);
            // #@@}
            return null;
        }
        else {
            // lhs = rhs -> tmp = rhs, lhs = tmp, tmp
            // #@@range/Assign_expr{
            DefinedVariable tmp = tmpVar(node.rhs().type());
            assign(rloc, ref(tmp), transformExpr(node.rhs()));
            assign(lloc, transformExpr(node.lhs()), ref(tmp));
            return ref(tmp);
            // #@@}
        }
    }
    // #@@}

    // #@@range/OpAssign{
    public Expr visit(OpAssignNode node) {
        // Evaluate RHS before LHS.
        Expr rhs = transformExpr(node.rhs());
        Expr lhs = transformExpr(node.lhs());
        Type t = node.lhs().type();
        Op op = Op.internBinary(node.operator(), t.isSigned());
        return transformOpAssign(node.location(), op, t, lhs, rhs);
    }
    // #@@}

    public Expr visit(PrefixOpNode node) {
        // ++expr -> expr += 1
        Type t = node.expr().type();
        return transformOpAssign(node.location(),
                binOp(node.operator()), t,
                transformExpr(node.expr()), imm(t, 1));
    }

    // #@@range/SuffixOp{
    public Expr visit(SuffixOpNode node) {
        // #@@range/SuffixOp_init{
        Expr expr = transformExpr(node.expr());
        Type t = node.expr().type();
        Op op = binOp(node.operator());
        Location loc = node.location();
        // #@@}

        if (isStatement()) {
            // expr++; -> expr += 1;
            transformOpAssign(loc, op, t, expr, imm(t, 1));
            return null;
        }
        else if (expr.isVar()) {
            // cont(expr++) -> v = expr; expr = v + 1; cont(v)
            DefinedVariable v = tmpVar(t);
            assign(loc, ref(v), expr);
            assign(loc, expr, bin(op, t, ref(v), imm(t, 1)));
            return ref(v);
        }
        else {
            // cont(expr++) -> a = &expr; v = *a; *a = *a + 1; cont(v)
            // #@@range/SuffixOp_expr{
            DefinedVariable a = tmpVar(pointerTo(t));
            DefinedVariable v = tmpVar(t);
            assign(loc, ref(a), addressOf(expr));
            assign(loc, ref(v), mem(a));
            assign(loc, mem(a), bin(op, t, mem(a), imm(t, 1)));
            return ref(v);
            // #@@}
        }
    }
    // #@@}

    // #@@range/transformOpAssign{
    private Expr transformOpAssign(Location loc,
            Op op, Type lhsType, Expr lhs, Expr rhs) {
        if (lhs.isVar()) {
            // cont(lhs += rhs) -> lhs = lhs + rhs; cont(lhs)
            assign(loc, lhs, bin(op, lhsType, lhs, rhs));
            return isStatement() ? null : lhs;
        }
        else {
            // cont(lhs += rhs) -> a = &lhs; *a = *a + rhs; cont(*a)
            DefinedVariable a = tmpVar(pointerTo(lhsType));
            assign(loc, ref(a), addressOf(lhs));
            assign(loc, mem(a), bin(op, lhsType, mem(a), rhs));
            return isStatement() ? null : mem(a);
        }
    }
    // #@@}

    // #@@range/bin{
    private Bin bin(Op op, Type leftType, Expr left, Expr right) {
        if (isPointerArithmetic(op, leftType)) {
            return new Bin(left.type(), op, left,
                    new Bin(right.type(), Op.MUL,
                            right, ptrBaseSize(leftType)));
        }
        else {
            return new Bin(left.type(), op, left, right);
        }
    }
    // #@@}

    // #@@range/Funcall{
    public Expr visit(FuncallNode node) {
        List<Expr> args = new ArrayList<Expr>();
        for (ExprNode arg : ListUtils.reverse(node.args())) {
            args.add(0, transformExpr(arg));
        }
        Expr call = new Call(asmType(node.type()),
                transformExpr(node.expr()), args);
        if (isStatement()) {
            stmts.add(new ExprStmt(node.location(), call));
            return null;
        }
        else {
            DefinedVariable tmp = tmpVar(node.type());
            assign(node.location(), ref(tmp), call);
            return ref(tmp);
        }
    }
    // #@@}

    //
    // Expressions (no side effects)
    //

    // #@@range/BinaryOp{
    public Expr visit(BinaryOpNode node) {
        // #@@range/BinaryOp_init_1{
        Expr right = transformExpr(node.right());
        Expr left = transformExpr(node.left());
        Op op = Op.internBinary(node.operator(), node.type().isSigned());
        Type t = node.type();
        // #@@}
        Type r = node.right().type();
        Type l = node.left().type();

        // #@@range/BinaryOp_ptr{
        if (isPointerDiff(op, l, r)) {
            // ptr - ptr -> (ptr - ptr) / ptrBaseSize
            Expr tmp = new Bin(asmType(t), op, left, right);
            return new Bin(asmType(t), Op.S_DIV, tmp, ptrBaseSize(l));
        }
        else if (isPointerArithmetic(op, l)) {
            // ptr + int -> ptr + (int * ptrBaseSize)
            return new Bin(asmType(t), op,
                    left,
                    new Bin(asmType(r), Op.MUL, right, ptrBaseSize(l)));
        }
        else if (isPointerArithmetic(op, r)) {
            // int + ptr -> (int * ptrBaseSize) + ptr
            return new Bin(asmType(t), op,
                    new Bin(asmType(l), Op.MUL, left, ptrBaseSize(r)),
                    right);
        }
        // #@@}
        else {
            // int + int
            // #@@range/BinaryOp_int{
            return new Bin(asmType(t), op, left, right);
            // #@@}
        }
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

    // #@@range/Aref{
    public Expr visit(ArefNode node) {
        Expr expr = transformExpr(node.baseExpr());
        Expr offset = new Bin(ptrdiff_t(), Op.MUL,
                size(node.elementSize()), transformIndex(node));
        Bin addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        return mem(addr, node.type());
    }
    // #@@}

    // For multidimension array: t[e][d][c][b][a] ary;
    // &ary[a0][b0][c0][d0][e0]
    //     = &ary + edcb*a0 + edc*b0 + ed*c0 + e*d0 + e0
    //     = &ary + (((((a0)*b + b0)*c + c0)*d + d0)*e + e0) * sizeof(t)
    //
    private Expr transformIndex(ArefNode node) {
        if (node.isMultiDimension()) {
            return new Bin(int_t(), Op.ADD,
                    transformExpr(node.index()),
                    new Bin(int_t(), Op.MUL,
                            new Int(int_t(), node.length()),
                            transformIndex((ArefNode)node.expr())));
        }
        else {
            return transformExpr(node.index());
        }
    }

    // #@@range/Member{
    public Expr visit(MemberNode node) {
        Expr expr = addressOf(transformExpr(node.expr()));
        Expr offset = ptrdiff(node.offset());
        Expr addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        // #@@range/Member_ret{
        return node.isLoadable() ? mem(addr, node.type()) : addr;
        // #@@}
    }
    // #@@}

    // #@@range/PtrMember{
    public Expr visit(PtrMemberNode node) {
        Expr expr = transformExpr(node.expr());
        Expr offset = ptrdiff(node.offset());
        Expr addr = new Bin(ptr_t(), Op.ADD, expr, offset);
        return node.isLoadable() ? mem(addr, node.type()) : addr;
    }
    // #@@}

    // #@@range/Dereference{
    public Expr visit(DereferenceNode node) {
        Expr addr = transformExpr(node.expr());
        return node.isLoadable() ? mem(addr, node.type()) : addr;
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
        return new Int(size_t(), node.expr().allocSize());
    }

    public Expr visit(SizeofTypeNode node) {
        return new Int(size_t(), node.operand().allocSize());
    }

    public Expr visit(VariableNode node) {
        if (node.entity().isConstant()) {
            return transformExpr(node.entity().value());
        }
        Var var = ref(node.entity());
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

    private boolean isPointerDiff(Op op, Type l, Type r) {
        return op == Op.SUB && l.isPointer() && r.isPointer();
    }

    private boolean isPointerArithmetic(Op op, Type operandType) {
        switch (op) {
        case ADD:
        case SUB:
            return operandType.isPointer();
        default:
            return false;
        }
    }

    private Expr ptrBaseSize(Type t) {
        return new Int(ptrdiff_t(), t.baseType().size());
    }

    // unary ops -> binary ops
    private Op binOp(String uniOp) {
        return uniOp.equals("++") ? Op.ADD : Op.SUB;
    }

    // #@@range/addressOf{
    private Expr addressOf(Expr expr) {
        return expr.addressNode(ptr_t());
    }
    // #@@}

    // #@@range/ref{
    private Var ref(Entity ent) {
        return new Var(varType(ent.type()), ent);
    }
    // #@@}

    // mem(ent) -> (Mem (Var ent))
    private Mem mem(Entity ent) {
        return new Mem(asmType(ent.type().baseType()), ref(ent));
    }

    // mem(expr) -> (Mem expr)
    // #@@range/mem{
    private Mem mem(Expr expr, Type t) {
        return new Mem(asmType(t), expr);
    }
    // #@@}

    // #@@range/ptrdiff{
    private Int ptrdiff(long n) {
        return new Int(ptrdiff_t(), n);
    }
    // #@@}

    // #@@range/size{
    private Int size(long n) {
        return new Int(size_t(), n);
    }
    // #@@}

    // #@@range/imm{
    private Int imm(Type operandType, long n) {
        if (operandType.isPointer()) {
            return new Int(ptrdiff_t(), n);
        }
        else {
            return new Int(int_t(), n);
        }
    }
    // #@@}

    private Type pointerTo(Type t) {
        return typeTable.pointerTo(t);
    }

    private net.loveruby.cflat.asm.Type asmType(Type t) {
        if (t.isVoid()) return int_t();
        return net.loveruby.cflat.asm.Type.get(t.size());
    }

    private net.loveruby.cflat.asm.Type varType(Type t) {
        if (! t.isScalar()) {
            return null;
        }
        return net.loveruby.cflat.asm.Type.get(t.size());
    }

    private net.loveruby.cflat.asm.Type int_t() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.intSize());
    }

    private net.loveruby.cflat.asm.Type size_t() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.longSize());
    }

    private net.loveruby.cflat.asm.Type ptr_t() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.pointerSize());
    }

    private net.loveruby.cflat.asm.Type ptrdiff_t() {
        return net.loveruby.cflat.asm.Type.get((int)typeTable.longSize());
    }

    private void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
