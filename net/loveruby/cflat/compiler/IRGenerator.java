package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

class IRGenerator extends ASTVisitor<Void, Expr> {
    protected ErrorHandler errorHandler;
    protected TypeTable typeTable;
    private List<Stmt> stmts;

    // #@@range/ctor{
    public IRGenerator(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    // #@@}

    protected void compile(StmtNode node) {
        if (node == null) return;
        visitStmt(node);
    }

    protected Expr compile(ExprNode node) {
        if (node == null) return null;
        return visitExpr(node);
    }

    // #@@range/check_AST{
    public IRTree compile(AST ast) throws SemanticException {
        typeTable = ast.typeTable();
        // FIXME: required?
        //for (DefinedVariable var : ast.definedVariables()) { visit(var); }
        for (DefinedFunction f : ast.definedFunctions()) {
            List<Stmt> stmts = new ArrayList<Stmt>();
            compile(f.body());
            f.setIR(stmts);
        }
        if (errorHandler.errorOccured()) {
            throw new SemanticException("IR generation failed.");
        }
        return ast;
    }
    // #@@}

    //
    // Statement
    //

    public Void visit(BlockNode node) {
        for (DefinedVariable var : node.scope().localVariables()) {
            if (var.initializer() != null) {
                stmts.add(Stmt.move(Expr.var(var), compile(var.initializer())));
            }
        }
        for (StmtNode s : node.stmts()) {
            stmts.add(compile(s));
        }
        return null;
    }

    public Void visit(ExprStmtNode node) {
        stmts.add(Stmt.stmt(compile(node.expr())));
        return null;
    }

    public Void visit(ReturnNode node) {
        stmts.add(Stmt.ret(compile(node.expr())));
        return null;
    }

    public Void visit(IfNode node) {
        stmts.add(Stmt.branch(compile(node.cond()),
                                node.thenLabel(),
                                node.elseLabel()));
        stmts.add(Stmt.label(node.thenLabel()));
        compile(node.thenBody());
        stmts.add(Stmt.label(node.elseLabel()));
        compile(node.elseBody());
        stmts.add(Stmt.label(node.endLabel()));
        return null;
    }

    // FIXME
    public Void visit(SwitchNode node) {
        throw new Error("switch not implemented yet");
    }

    public Void visit(WhileNode node) {
        stmts.add(Stmt.branch(compile(node.cond()),
                node.bodyLabel(), node.endLabel()));
        stmts.add(Stmt.label(node.begLabel()));
        compile(node.body());
        stmts.add(Stmt.branch(compile(node.cond()),
                node.begLabel(), node.endLabel()));
        stmts.add(Stmt.label(node.endLabel()));
        return null;
    }

    public Void visit(DoWhileNode node) {
        stmts.add(Stmt.label(node.begLabel()));
        compile(node.body());
        stmts.add(Stmt.branch(compile(node.cond()),
                node.begLabel(), node.endLabel()));
        stmts.add(Stmt.label(node.endLabel()));
        return null;
    }

    public Void visit(ForNode node) {
        compile(node.init());
        stmts.add(Stmt.branch(compile(node.cond()),
                node.begLabel(), node.endLabel()));
        stmts.add(node.begLabel());
        compile(node.body());
        stmts.add(node.continueLabel());
        compile(node.incr());
        stmts.add(Stmt.branch(compile(node.cond()),
                node.begLabel(), node.endLabel()));
        stmts.add(node.endLabel());
        return null;
    }

    public Void visit(BreakNode node) {
        stmts.add(Stmt.jump(currentBreakLabel()));
        return null;
    }

    public Void visit(ContinueNode node) {
        stmts.add(Stmt.jump(currentContinueLabel()));
        return null;
    }

    public Void visit(GotoNode node) {
        stmts.add(Stmt.jump(node.label()));
        return null;
    }

    public Void visit(LabelNode node) {
        stmts.add(node.label());
        compile(node.stmt());
        return null;
    }

    //
    // RHS Expression
    //

    // #@@range/BinaryOpNode{
    public Expr visit(BinaryOpNode node) {
        ExprKind op = binOp(node.operator());
        Expr left = compile(node.left());
        Expr right = compile(node.right());
        if (node.operator().equals("+") || node.operator().equals("-")) {
            if (node.left().type().isDereferable()) {
                right = multiplyPtrBaseSize(right, node.left());
            }
            else if (node.right().type().isDereferable()) {
                left = multiplyPtrBaseSize(left, node.right());
            }
        }
        return Expr.bin(op, left, right);
    }
    // #@@}

    public Expr visit(AssignNode node) {
        Expr tmp = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(tmp, compile(node.rhs())),
                Stmt.move(compileLHS(node.lhs()), tmp)
            ),
            tmp);
    }

    public Expr visit(OpAssignNode node) {
        Expr r = Expr.tmp();
        Expr l = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(r, compileOpAssignRHS(node.rhs()),
                Stmt.move(l, compileLHS(node.lhs())),
                Stmt.move(Expr.mem(l),
                        Expr.bin(binOp(node.operator()),
                            Expr.mem(l), r))
            ),
            r);
    }

    private Expr compileOpAssignRHS(String op, ExprNode rhs, ExprNode lhs) {
        if ((op.equals("+") || op.equals("-")) && lhs.type().isDereferable()) {
            return multiplyPtrBaseSize(compile(rhs), lhs);
        }
        else {
            return compile(rhs);
        }
    }

    protected Expr multiplyPtrBaseSize(Expr expr, ExprNode ptr) {
        return Expr.bin(ExprKind.MUL, expr, ptrBaseSize(ptr));
    }

    protected Expr ptrBaseSize(ExprNode ptr) {
        Type t = typeTable.ptrDiffType();
        Location loc = ptr.location();
        return Expr.const(ptr.type().baseType().size());
    }

    public Expr visit(LogicalAndNode node) {
        Expr l = Expr.tmp();
        Expr result = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(l, compile(node.left())),
                Stmt.branch(l, node.thenLabel(), node.elseLabel()),
                Stmt.label(node.thenLabel()),
                Stmt.move(result, l),
                Stmt.label(node.elseLabel()),
                Stmt.move(result, compile(node.right()))
            ),
            result);
    }

    public Expr visit(LogicalOrNode node) {
        Expr l = Expr.tmp();
        Expr result = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(l, compile(node.left())),
                Stmt.branch(l, node.elseLabel(), node.thenLabel()),
                Stmt.label(node.thenLabel()),
                Stmt.move(result, l),
                Stmt.label(node.elseLabel()),
                Stmt.move(result, compile(node.right()))
            ),
            result);
    }

    public Expr visit(CondExprNode node) {
        Expr result = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.branch(compile(node.cond()),
                        node.thenLabel(), node.elseLabel()),
                Stmt.label(node.thenLabel()),
                Stmt.move(result, compile(node.thenExpr())),
                Stmt.label(node.elseLabel()),
                Stmt.move(result, compile(node.elseExpr()))
            ),
            result);
    }

    public Expr visit(FuncallNode node) {
        List<Expr> args = new ArrayList<Expr>();
        for (ExprNode a : node.arguments()) {
            args.add(compile(a));
        }
        return Expr.call(compile(node.expr()), args);
    }

    public Expr visit(UnaryOpNode node) {
        if (node.operator().equals("+")) {
            return compile(node.expr());
        }
        else {
            return Expr.uni(uniOp(node.operator()), compile(node.expr()));
        }
    }

    public Expr visit(PrefixOpNode node) {
        Expr lhs = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(lhs, compileLHS(node.expr())),
                Stmt.move(Expr.mem(lhs),
                    Expr.bin(uniOp(node.operator()),
                            Expr.mem(lhs),
                            Expr.const(1)))
            ),
            Expr.mem(lhs));
    }

    public Expr visit(SuffixOpNode node) {
        Expr lhs = Expr.tmp();
        Expr save = Expr.tmp();
        return Expr.seq(
            Stmt.seq(
                Stmt.move(lhs, compileLHS(node.expr())),
                Stmt.move(save, Expr.mem(lhs));
                Stmt.move(Expr.mem(lhs),
                    Expr.bin(uniOp(node.operator()),
                            Expr.mem(lhs),
                            Expr.const(1)))
            ),
            Expr.mem(save));
    }

    public Expr visit(AddressNode node) {
        return compileLHS(node.expr());
    }

    public Expr visit(SizeofExprNode node) {
        return Expr.const(node.expr().type().allocSize());
    }

    public Expr visit(SizeofTypeNode node) {
        return Expr.const(node.operand().allocSize());
    }

    public Expr visit(IntegerLiteralNode node) {
        return Expr.const(node.value());
    }

    public Expr visit(StringLiteralNode node) {
        return Expr.name(node.entry());
    }

    //
    // Assignable Expression as RHS
    //

    public Expr visit(ArefNode node) {
        return Expr.mem(compileLHS(node));
    }

    public Expr visit(MemberNode node) {
        Expr a = Expr.add(Expr.const(node.offset()), compileLHS(node.expr()));
        return node.shouldEvaluatedToAddress() ? a : Expr.deref(a);
    }

    public Expr visit(PtrMemberNode node) {
        Expr addr = Expr.add(Expr.const(node.offset()), compile(node.expr()));
        return node.shouldEvaluatedToAddress() ? addr : Expr.deref(addr);
    }

    public Expr visit(DereferenceNode node) {
        return Expr.deref(compile(node.expr()));
    }

    public Expr visit(VariableNode node) {
        return Expr.var(node.variable());
    }

    //
    // Assignable Expression as LHS
    //

    public Expr compileLHS(ExprNode node) {
        node.acceptLHS(this);
    }

    public Expr visitLHS(VariableNode node) {
        return addressOf(node);
    }

    public Expr visitLHS(ArefNode node) {
        return Expr.add(
            Expr.mul(
                Expr.const(node.elementSize()),
                compileArrayIndex(node)),
            compile(node.baseExpr));
    }

    private Expr compileArrayIndex(ArefNode node) {
        if (node.isMultiDimension()) {
            return Expr.add(
                compile(node.index()),
                Expr.mul(
                    Expr.const(node.length()),
                    compileArrayIndex((ArefNode)node.expr())));
        }
        else {
            return compile(node.index());
        }
    }

    public Expr visitLHS(MemberNode node) {
        return Expr.add(
            Expr.const(node.offset()),
            compileLHS(node.expr()));
    }

    public Expr visitLHS(PtrMemberNode node) {
        return Expr.add(
            Expr.const(node.offset()),
            compile(node.expr()));
    }

    public Expr visitLHS(DereferenceNode node) {
        return compile(node.expr());
    }

    //
    // Utilities
    //

    private Expr addressOf(VariableNode node) {
    }

    private Expr addressOf(StringLiteralNode node) {
    }

    private ExprKind uniOp(String op) {
        if (op.equals("-")) return ExprKind.UMINUS;
        if (op.equals("~")) return ExprKind.NOT;
        if (op.equals("!")) return ExprKind.LNOT;
        if (op.equals("*")) return ExprKind.DEREF;
        if (op.equals("++")) return ExprKind.ADD;
        if (op.equals("--")) return ExprKind.SUB;
    }

    // FIXME: signed, arithmetic shift
    private ExprKind binOp(String op) {
        if (op.equals("+")) return ExprKind.ADD;
        if (op.equals("-")) return ExprKind.SUB;
        if (op.equals("*")) return ExprKind.MUL;
        if (op.equals("/")) return ExprKind.DIV;
        if (op.equals("%")) return ExprKind.MOD;
        if (op.equals("&")) return ExprKind.AND;
        if (op.equals("|")) return ExprKind.OR;
        if (op.equals("^")) return ExprKind.XOR;
        if (op.equals("<<")) return ExprKind.LSHIFT;
        if (op.equals(">>")) return ExprKind.RSHIFT;
        if (op.equals("==")) return ExprKind.EQ;
        if (op.equals("!=")) return ExprKind.NEQ;
        if (op.equals("<")) return ExprKind.LT;
        if (op.equals("<=")) return ExprKind.LTEQ;
        if (op.equals(">")) return ExprKind.GT;
        if (op.equals(">=")) return ExprKind.GTEQ;
    }

    private void error(Node n, String msg) {
        errorHandler.error(n.location(), msg);
    }
}
