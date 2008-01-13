package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator extends Visitor {
    static public String generate(AST ast, TypeTable typeTable,
            ErrorHandler errorHandler) {
        CodeGenerator gen = new CodeGenerator(new Assembler(), errorHandler);
        return gen.generateAssembly(ast, typeTable);
    }

    protected Assembler as;
    protected DefinedFunction currentFunction;
    protected TypeTable typeTable;
    protected ErrorHandler errorHandler;

    public CodeGenerator(Assembler as, ErrorHandler errorHandler) {
        this.as = as;
        this.errorHandler = errorHandler;
    }
static public void p(String s) { System.err.println(s); }

    /** Compiles "ast" and generates assembly code. */
    public String generateAssembly(AST ast, TypeTable typeTable) {
        typeTable = typeTable;
        allocateGlobalVariables(ast.globalVariables());
        allocateCommonSymbols(ast.commonSymbols());

        as._file(ast.fileName());
        // .data
        compileGlobalVariables(ast.globalVariables());
        if (!ast.constantTable().isEmpty()) {
            compileConstants(ast.constantTable());
        }
        // .text
        if (ast.functionDefined()) {
            compileFunctions(ast.functions());
        }
        // .bss
        compileCommonSymbols(ast.commonSymbols());

        return as.string();
    }

    /**
     * Sets address for...
     *   * public global variables
     *   * private global variables
     *   * static local variables
     */
    protected void allocateGlobalVariables(Iterator vars) {
        while (vars.hasNext()) {
            Variable var = (Variable)vars.next();
            var.setAddress(globalVariableAddress(var.symbol()));
        }
    }

    /**
     * Sets address for...
     *   * public common symbols
     *   * private common symbols
     */
    protected void allocateCommonSymbols(Iterator comms) {
        while (comms.hasNext()) {
            Variable var = (Variable)comms.next();
            var.setAddress(commonSymbolAddress(var.symbol()));
        }
    }

    /** Linux/IA-32 dependent */
    // FIXME: PIC
    protected AsmEntity globalVariableAddress(String sym) {
        return new Label(csymbol(sym));
    }

    /** Linux/IA-32 dependent */
    // FIXME: PIC
    protected AsmEntity commonSymbolAddress(String sym) {
        return new Label(csymbol(sym));
    }

    /** Generates static variable entries */
    protected void compileGlobalVariables(Iterator vars) {
        as._data();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            dataEntry(var);
        }
    }

    /** Generates initialized entries */
    protected void dataEntry(DefinedVariable ent) {
        if (!ent.isPrivate()) {
            as._globl(csymbol(ent.symbol()));
        }
        as._align(ent.allocSize());
        as._type(csymbol(ent.symbol()), "@object");
        as._size(csymbol(ent.symbol()), ent.allocSize());
        as.label(csymbol(ent.symbol()));
        compileImmediate(ent.type(), ent.initializer());
    }

    /** Generates immediate values for .data section */
    protected void compileImmediate(Type type, ExprNode n) {
        // FIXME: support other constants
        IntegerLiteralNode expr = (IntegerLiteralNode)n;
        switch ((int)type.allocSize()) {
        case 1: as._byte(expr.value());    break;
        case 2: as._value(expr.value());   break;
        case 4: as._long(expr.value());    break;
        case 8: as._quad(expr.value());    break;
        default:
            throw new Error("entry size is not 1,2,4,8");
        }
    }

    /** Generates BSS entries */
    protected void compileCommonSymbols(Iterator ents) {
        while (ents.hasNext()) {
            Variable ent = (Variable)ents.next();
            if (ent.isPrivate()) {
                as._local(csymbol(ent.symbol()));
            }
            as._comm(csymbol(ent.symbol()), ent.allocSize(), ent.alignment());
        }
    }

    /** Generates .rodata entry (constant strings) */
    protected void compileConstants(ConstantTable table) {
        as._section(".rodata");
        Iterator ents = table.entries();
        while (ents.hasNext()) {
            ConstantEntry ent = (ConstantEntry)ents.next();
            as.label(ent.label());
            as._string(ent.value());
        }
    }

    /** Compiles all functions and generates .text section. */
    protected void compileFunctions(Iterator funcs) {
        as._text();
        while (funcs.hasNext()) {
            DefinedFunction func = (DefinedFunction)funcs.next();
            compileFunction(func);
        }
    }

    /** Compiles a function. */
    protected void compileFunction(DefinedFunction func) {
        currentFunction = func;
        String symbol = csymbol(func.name());
        as._globl(symbol);
        as._type(symbol, "@function");
        as.label(symbol);
        prologue(func);
        allocateParameters(func);
        allocateLocalVariables(func);
        compile(func.body());
        epilogue(func);
        as._size(symbol, ".-" + symbol);
    }

    protected void compile(Node n) {
        n.accept(this);
    }

    // platform dependent
    protected String tmpsymbol(String sym) {
        return sym;
    }

    // platform dependent
    protected String csymbol(String sym) {
        return sym;
    }

    protected void prologue(DefinedFunction func) {
        as.pushq(bp());
        as.movq(sp(), bp());
    }

    protected void epilogue(DefinedFunction func) {
        as.label(epilogueLabel(func));
        as.movq(bp(), sp());
        as.popq(bp());
        as.ret();
    }

    protected void jmpEpilogue() {
        as.jmp(new Label(epilogueLabel(currentFunction)));
    }

    protected String epilogueLabel(DefinedFunction func) {
        return ".L" + func.name() + "$epilogue";
    }

    /* Standard IA-32 stack frame layout (after prologue)
     *
     * ======================= esp (stack top)
     * temporary
     * variables...
     * ---------------------   ebp-(4*3)
     * lvar 3
     * ---------------------   ebp-(4*2)
     * lvar 2
     * ---------------------   ebp-(4*1)
     * lvar 1
     * ======================= ebp
     * saved ebp
     * ---------------------   ebp+(4*1)
     * return address
     * ---------------------   ebp+(4*2)
     * arg 1
     * ---------------------   ebp+(4*3)
     * arg 2
     * ---------------------   ebp+(4*4)
     * arg 3
     * ...
     * ...
     * ======================= stack bottom
     */

    /*
     * Platform Dependent Stack Parameters
     */
    static final protected int stackDirection = -1;   // stack grows lower
    static final protected long stackWordSize = 4;
    // 1 for return address, 1 for saved bp.
    static final protected long paramStartOffset = 2;
    static final protected long usedStackWords = 0;

    protected void allocateParameters(DefinedFunction func) {
        Iterator vars = func.parameters();
        long i = paramStartOffset;
        while (vars.hasNext()) {
            Parameter var = (Parameter)vars.next();
            var.setAddress(lvarAddressByWord(i));
            i++;
        }
    }

    protected CompositeAddress lvarAddressByWord(long offset) {
        return new CompositeAddress(offset * stackWordSize, bp());
    }

    protected void allocateLocalVariables(DefinedFunction func) {
        Iterator vars = func.localVariables();
        long len = usedStackWords * stackWordSize;
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            if (stackDirection < 0) {
                len = align(len + var.allocSize(), stackWordSize);
                var.setAddress(new CompositeAddress(-len, bp()));
            }
            else {
                var.setAddress(new CompositeAddress(len, bp()));
                len = align(len + var.allocSize(), stackWordSize);
            }
        }
        if (len != 0) {
            extendStack(len);
        }
    }

    protected void extendStack(long len) {
        as.addq(imm(stackDirection * len), sp());
    }

    protected void shrinkStack(long len) {
        as.subq(imm(stackDirection * len), sp());
    }

    protected long align(long n, long alignment) {
        return (n + alignment - 1) / alignment * alignment;
    }

    /** cdecl call
     *
     *    * all arguments are on stack
     *    * rollback stack by caller
     */
    public void visit(FuncallNode node) {
        ListIterator it = node.finalArg();
        while (it.hasPrevious()) {
            ExprNode arg = (ExprNode)it.previous();
            compile(arg);
            as.pushq(reg("ax"));
        }
        //if (node.function().isVararg()) {
        //    ...
        //}
        if (node.isStaticCall()) {
            if (node.function().isDefined()) {
                as.call(csymbol(node.function().name()));
            }
            else {
                as.call(tmpsymbol(node.function().name()));
            }
        }
        else {  // funcall via pointer
            // FIXME
            compile(node.expr());
            as.ptrcall(reg("ax"));
        }
        if (node.numArgs() > 0) {
            // FIXME: >4 size arguments are not supported.
            shrinkStack(node.numArgs() * stackWordSize);
        }
    }

    public void visit(ReturnNode node) {
        if (node.expr() != null) {
            compile(node.expr());
        }
        jmpEpilogue();
    }

    //
    // Statements
    //

    public void visit(BlockNode node) {
        Iterator vars = node.scope().variables();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            if (var.initializer() != null) {
                compile(var.initializer());
                saveWords(var.type(), "ax", var.address());
            }
        }
        Iterator stmts = node.stmts();
        while (stmts.hasNext()) {
            compileStmt((Node)stmts.next());
        }
    }

    // needed?
    protected void compileStmt(Node node) {
        compile(node);
    }

    private void testCond(Type t, String regname) {
        as.test(t, reg(regname, t), reg(regname, t));
    }

    public void visit(IfNode node) {
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        if (node.elseBody() != null) {
            as.jz(node.elseLabel());
            compileStmt(node.thenBody());
            as.jmp(node.endLabel());
            as.label(node.elseLabel());
            compileStmt(node.elseBody());
            as.label(node.endLabel());
        }
        else {
            as.jz(node.endLabel());
            compileStmt(node.thenBody());
            as.label(node.endLabel());
        }
    }

    public void visit(CondExprNode node) {
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        as.jz(node.elseLabel());
        compile(node.thenExpr());
        as.jmp(node.endLabel());
        as.label(node.elseLabel());
        compile(node.elseExpr());
        as.label(node.endLabel());
    }

    public void visit(SwitchNode node) {
        compile(node.cond());
        Type t = typeTable.signedInt();
        Iterator cases = node.cases();
        while (cases.hasNext()) {
            CaseNode caseNode = (CaseNode)cases.next();
            Iterator values = caseNode.values();
            while (values.hasNext()) {
                as.movq(imm(caseValue((Node)values.next())), reg("cx"));
                as.cmp(t, reg("cx", t), reg("ax", t));
                as.je(caseNode.beginLabel());
            }
        }
        cases = node.cases();
        while (cases.hasNext()) {
            compile((CaseNode)cases.next());
        }
    }

    protected long caseValue(Node node) {
        if (!(node instanceof IntegerLiteralNode)) {
            // FIXME: use exception
            throw new Error("case accepts only integer literal");
        }
        return ((IntegerLiteralNode)node).value();
    }

    public void visit(CaseNode node) {
        as.label(node.beginLabel());
        compile(node.body());
    }

    public void visit(LogicalAndNode node) {
        compile(node.left());
        testCond(node.left().type(), "ax");
        as.jz(node.endLabel());
        compile(node.right());
        as.label(node.endLabel());
    }

    public void visit(LogicalOrNode node) {
        compile(node.left());
        testCond(node.left().type(), "ax");
        as.jnz(node.endLabel());
        compile(node.right());
        as.label(node.endLabel());
    }

    public void visit(WhileNode node) {
        as.label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        as.jz(node.endLabel());
        compileStmt(node.body());
        as.jmp(node.begLabel());
        as.label(node.endLabel());
    }

    public void visit(DoWhileNode node) {
        as.label(node.begLabel());
        compileStmt(node.body());
        as.label(node.continueLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        as.jnz(node.begLabel());
        as.label(node.endLabel());
    }

    public void visit(ForNode node) {
        compileStmt(node.init());
        as.label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        as.jz(node.endLabel());
        compileStmt(node.body());
        as.label(node.continueLabel());
        compileStmt(node.incr());
        as.jmp(node.begLabel());
        as.label(node.endLabel());
    }

    public void visit(BreakNode node) {
        as.jmp(node.targetLabel());
    }

    public void visit(ContinueNode node) {
        as.jmp(node.targetLabel());
    }

    public void visit(LabelNode node) {
        as.label(node.label());
        compileStmt(node.stmt());
    }

    public void visit(GotoNode node) {
        as.jmp(node.targetLabel());
    }

    //
    // Expressions
    //

    public void visit(BinaryOpNode node) {
        compile(node.right());
        as.pushq(reg("ax"));
        compile(node.left());
        as.popq(reg("cx"));
        compileBinaryOp(node.operator(), node.type());
    }

    protected void compileBinaryOp(String op, Type t) {
        if (op.equals("+")) {
            as.add(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("-")) {
            as.sub(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("*")) {
            as.imul(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("/")) {
            as.movq(imm(0), reg("dx"));
            as.idiv(t, reg("cx", t));
        }
        else if (op.equals("%")) {
            as.movq(imm(0), reg("dx"));
            as.idiv(t, reg("cx", t));
            as.movq(reg("dx"), reg("ax"));
        }
        else if (op.equals("&")) {
            as.and(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("|")) {
            as.or(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("^")) {
            as.xor(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals(">>")) {
            as.sar(t, cl(), reg("ax", t));
        }
        else if (op.equals("<<")) {
            as.sal(t, cl(), reg("ax", t));
        }
        else {
            // Comparison operators
            as.cmp(t, reg("cx", t), reg("ax", t));
            if      (op.equals("=="))   as.sete (al());
            else if (op.equals("!="))   as.setne(al());
            else if (op.equals(">"))    as.setg (al());
            else if (op.equals(">="))   as.setge(al());
            else if (op.equals("<"))    as.setl (al());
            else if (op.equals("<="))   as.setle(al());
            else {
                throw new Error("unknown binary operator: " + op);
            }
            as.movzb(t, al(), reg("ax", t));
        }
    }

    public void visit(UnaryOpNode node) {
        compile(node.expr());
        if (node.operator().equals("+")) {
            ;
        }
        else if (node.operator().equals("-")) {
            as.neg(node.expr().type(), reg("ax", node.expr().type()));
        }
        else if (node.operator().equals("~")) {
            as.not(node.expr().type(), reg("ax", node.expr().type()));
        }
        else if (node.operator().equals("!")) {
            testCond(node.expr().type(), "ax");
            as.sete(al());
            as.movzbl(al(), reg("ax"));
        }
    }

    public void visit(PrefixOpNode node) {
        compileIncDec(node.operator(), node.expr());
        loadWords(node.expr().type(), node.expr().address(), "ax");
    }

    public void visit(SuffixOpNode node) {
        loadWords(node.expr().type(), node.expr().address(), "ax");
        compileIncDec(node.operator(), node.expr());
    }

    protected void compileIncDec(String op, ExprNode e) {
        if (op.equals("++")) {
            if (e.type().isInteger()) {
                as.inc(e.type(), e.address());
            }
            else {
                as.addq(imm(e.type().size()), e.address());
            }
        }
        else if (op.equals("--")) {
            if (e.type().isInteger()) {
                as.dec(e.type(), e.address());
            }
            else {
                as.subq(imm(e.type().size()), e.address());
            }
        }
        else {
            throw new Error("unknown unary operator: " + op);
        }
    }

    public void visit(CastNode node) {
        // FIXME: insert cast op here
        compile(node.expr());
    }

    public void visit(VariableNode node) {
        if (node.type().isAllocatedArray()) {
            as.leaq(node.address(), reg("ax"));
        }
        else {
            loadWords(node.type(), node.address(), "ax");
        }
    }

    public void visit(IntegerLiteralNode node) {
        as.mov(node.type(), imm(node.value()), reg("ax", node.type()));
    }

    public void visit(StringLiteralNode node) {
        loadWords(node.type(), imm(node.label()), "ax");
    }

    //
    // Assignable expressions
    //

    public void visit(AssignNode node) {
        if (node.lhs().isConstantAddress()) {
            compile(node.rhs());
            saveWords(node.type(), "ax", node.lhs().address());
        }
        else {
            compile(node.rhs());
            as.pushq(reg("ax"));
            compileLHS(node.lhs());
            as.popq(reg("ax"));
            saveWords(node.type(), "ax", addr(PTRREG));
        }
    }

    public void visit(OpAssignNode node) {
        compile(node.rhs());
        as.movq(reg("ax"), reg("cx"));
        loadWords(node.type(), node.lhs().address(), "ax");
        compileBinaryOp(node.operator(), node.type());
        saveWords(node.type(), "ax", node.lhs().address());
    }

    public void visit(ArefNode node) {
        if (node.expr().type().isPointerAlike()) {
            compile(node.expr());
            as.pushq(reg("ax"));
        }
        else {
            compileLHS(node.expr());
            as.pushq(reg(PTRREG));
        }
        compile(node.index());
        as.imulq(imm(node.type().size()), reg("ax"));
        as.popq(reg(PTRREG));
        as.addq(reg("ax"), reg(PTRREG));
        loadWords(node.type(), addr(PTRREG), "ax");
    }

    public void visit(MemberNode node) {
        compileLHS(node.expr());
        loadWords(node.type(), addr2(node.offset(), PTRREG), "ax");
    }

    public void visit(PtrMemberNode node) {
        compileLHS(node.expr());
        loadWords(node.type(), addr(PTRREG), PTRREG);
        loadWords(node.type(), addr2(node.offset(), PTRREG), "ax");
    }

    public void visit(DereferenceNode node) {
        compile(node.expr());
        loadWords(node.type(), addr("ax"), "ax");
    }

    public void visit(AddressNode node) {
        compileLHS(node.expr());
        as.movq(reg(PTRREG), reg("ax"));
    }

    static final String PTRREG = "bx";

    protected void compileLHS(Node node) {
as.comment("compileLHS: " + node.getClass().getName() + " {");
        if (node instanceof VariableNode) {
            // FIXME: support static variables
            VariableNode n = (VariableNode)node;
            as.leaq(n.address(), reg(PTRREG));
        }
        else if (node instanceof ArefNode) {
            ArefNode n = (ArefNode)node;
            as.pushq(reg("ax"));
            compile(n.index());
            as.imulq(imm(n.type().size()), reg("ax"));
            as.pushq(reg("ax"));
            if (n.expr().type().isPointerAlike()) {
                compile(n.expr());
                as.movq(reg("ax"), reg(PTRREG));
            }
            else {
                compileLHS(n.expr());
            }
            as.popq(reg("cx"));
            as.addq(reg("cx"), reg(PTRREG));
            as.popq(reg("ax"));
        }
        else if (node instanceof MemberNode) {
            MemberNode n = (MemberNode)node;
            compileLHS(n.expr());
            as.addq(imm(n.offset()), reg(PTRREG));
        }
        else if (node instanceof DereferenceNode) {
            DereferenceNode n = (DereferenceNode)node;
            as.pushq(reg("ax"));
            compile(n.expr());
            as.movq(reg("ax"), reg(PTRREG));
            as.popq(reg("ax"));
        }
        else if (node instanceof PtrMemberNode) {
            PtrMemberNode n = (PtrMemberNode)node;
            as.pushq(reg("ax"));
            compile(n.expr());
            as.addq(imm(n.offset()), reg("ax"));
            as.movq(reg("ax"), reg(PTRREG));
            as.popq(reg("ax"));
        }
        else if (node instanceof PrefixOpNode) {
            PrefixOpNode n = (PrefixOpNode)node;
            compileLHS(n.expr());
            if (n.operator().equals("++")) {
                as.addq(imm(n.expr().type().size()), addr(PTRREG));
                as.addq(imm(n.expr().type().size()), reg(PTRREG));
            }
            else {
                as.subq(imm(n.expr().type().size()), addr(PTRREG));
                as.subq(imm(n.expr().type().size()), reg(PTRREG));
            }
        }
        else if (node instanceof SuffixOpNode) {
            SuffixOpNode n = (SuffixOpNode)node;
            compileLHS(n.expr());
            if (n.operator().equals("++")) {
                as.addq(imm(n.expr().type().size()), reg(PTRREG));
            }
            else {
                as.subq(imm(n.expr().type().size()), reg(PTRREG));
            }
        }
        else if (node instanceof CastNode) {
            CastNode n = (CastNode)node;
            compileLHS(n.expr());
            // FIXME: cast here
        }
        else {
            throw new Error("wrong type for compileLHS: " + node.getClass().getName());
        }
as.comment("compileLHS: }");
    }

    /*
     *  x86 assembly DSL
     */

    protected Register bp() { return reg("bp"); }
    protected Register sp() { return reg("sp"); }
    protected Register al() { return new Register(1, "ax"); }
    protected Register cl() { return new Register(1, "cx"); }

    protected Register reg(String name, Type type) {
        return Register.forType(type, name);
    }

    protected Register reg(String name) {
        return Register.widestRegister(name);
    }

    protected SimpleAddress addr(String regname) {
        return new SimpleAddress(Register.widestRegister(regname));
    }

    protected CompositeAddress addr2(long offset, String regname) {
        return new CompositeAddress(offset, Register.widestRegister(regname));
    }

    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected Reference imm(Label label) {
        return new Reference(label);
    }

    protected void loadWords(Type type, AsmEntity addr, String reg) {
        switch ((int)type.size()) {
        case 1:
            if (type.isSigned()) {  // signed char
                as.movsbl(addr, reg(reg));
            } else {                // unsigned char
                as.movzbl(addr, reg(reg));
            }
            break;
        case 2:
            if (type.isSigned()) {  // signed short
                as.movswl(addr, reg(reg));
            } else {                // unsigned short
                as.movzwl(addr, reg(reg));
            }
            break;
        default:                    // int, long, long_long
            as.mov(type, addr, reg(reg, type));
            break;
        }
    }

    protected void saveWords(Type type, String reg, AsmEntity addr) {
        as.mov(type, reg(reg, type), addr);
    }
}
