package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator extends Visitor {
    static public String generate(AST ast, TypeTable typeTable,
                                  ErrorHandler errorHandler) {
        Assembler as = new Assembler(typeTable.unsignedLong());
        CodeGenerator gen = new CodeGenerator(as, errorHandler);
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

        _file(ast.fileName());
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
        _data();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            dataEntry(var);
        }
    }

    /** Generates initialized entries */
    protected void dataEntry(DefinedVariable ent) {
        if (!ent.isPrivate()) {
            _globl(csymbol(ent.symbol()));
        }
        _align(ent.allocSize());
        _type(csymbol(ent.symbol()), "@object");
        _size(csymbol(ent.symbol()), ent.allocSize());
        label(csymbol(ent.symbol()));
        compileImmediate(ent.type(), ent.initializer());
    }

    /** Generates immediate values for .data section */
    protected void compileImmediate(Type type, ExprNode n) {
        // FIXME: support other constants
        IntegerLiteralNode expr = (IntegerLiteralNode)n;
        switch ((int)type.allocSize()) {
        case 1: _byte(expr.value());    break;
        case 2: _value(expr.value());   break;
        case 4: _long(expr.value());    break;
        case 8: _quad(expr.value());    break;
        default:
            throw new Error("entry size is not 1,2,4,8");
        }
    }

    /** Generates BSS entries */
    protected void compileCommonSymbols(Iterator ents) {
        while (ents.hasNext()) {
            Variable ent = (Variable)ents.next();
            if (ent.isPrivate()) {
                _local(csymbol(ent.symbol()));
            }
            _comm(csymbol(ent.symbol()), ent.allocSize(), ent.alignment());
        }
    }

    /** Generates .rodata entry (constant strings) */
    protected void compileConstants(ConstantTable table) {
        _section(".rodata");
        Iterator ents = table.entries();
        while (ents.hasNext()) {
            ConstantEntry ent = (ConstantEntry)ents.next();
            label(ent.label());
            _string(ent.value());
        }
    }

    /** Compiles all functions and generates .text section. */
    protected void compileFunctions(Iterator funcs) {
        _text();
        while (funcs.hasNext()) {
            DefinedFunction func = (DefinedFunction)funcs.next();
            compileFunction(func);
        }
    }

    /** Compiles a function. */
    protected void compileFunction(DefinedFunction func) {
        currentFunction = func;
        String symbol = csymbol(func.name());
        _globl(symbol);
        _type(symbol, "@function");
        label(symbol);
        prologue(func);
        allocateParameters(func);
        allocateLocalVariables(func);
        compile(func.body());
        epilogue(func);
        _size(symbol, ".-" + symbol);
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
        push(bp());
        mov(sp(), bp());
    }

    protected void epilogue(DefinedFunction func) {
        label(epilogueLabel(func));
        mov(bp(), sp());
        pop(bp());
        ret();
    }

    protected void jmpEpilogue() {
        jmp(new Label(epilogueLabel(currentFunction)));
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
        add(imm(stackDirection * len), sp());
    }

    protected void shrinkStack(long len) {
        sub(imm(stackDirection * len), sp());
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
            push(reg("ax"));
        }
        //if (node.function().isVararg()) {
        //    ...
        //}
        if (node.isStaticCall()) {
            if (node.function().isDefined()) {
                call(csymbol(node.function().name()));
            }
            else {
                call(tmpsymbol(node.function().name()));
            }
        }
        else {  // funcall via pointer
            // FIXME
            compile(node.expr());
            ptrcall(reg("ax"));
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
                save(var.type(), "ax", var.address());
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
        test(t, reg(regname, t), reg(regname, t));
    }

    public void visit(IfNode node) {
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        if (node.elseBody() != null) {
            jz(node.elseLabel());
            compileStmt(node.thenBody());
            jmp(node.endLabel());
            label(node.elseLabel());
            compileStmt(node.elseBody());
            label(node.endLabel());
        }
        else {
            jz(node.endLabel());
            compileStmt(node.thenBody());
            label(node.endLabel());
        }
    }

    public void visit(CondExprNode node) {
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        jz(node.elseLabel());
        compile(node.thenExpr());
        jmp(node.endLabel());
        label(node.elseLabel());
        compile(node.elseExpr());
        label(node.endLabel());
    }

    public void visit(SwitchNode node) {
        compile(node.cond());
        Type t = typeTable.signedInt();
        Iterator cases = node.cases();
        while (cases.hasNext()) {
            CaseNode caseNode = (CaseNode)cases.next();
            Iterator values = caseNode.values();
            while (values.hasNext()) {
                mov(imm(caseValue((Node)values.next())), reg("cx"));
                cmp(t, reg("cx", t), reg("ax", t));
                je(caseNode.beginLabel());
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
        label(node.beginLabel());
        compile(node.body());
    }

    public void visit(LogicalAndNode node) {
        compile(node.left());
        testCond(node.left().type(), "ax");
        jz(node.endLabel());
        compile(node.right());
        label(node.endLabel());
    }

    public void visit(LogicalOrNode node) {
        compile(node.left());
        testCond(node.left().type(), "ax");
        jnz(node.endLabel());
        compile(node.right());
        label(node.endLabel());
    }

    public void visit(WhileNode node) {
        label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        jz(node.endLabel());
        compileStmt(node.body());
        jmp(node.begLabel());
        label(node.endLabel());
    }

    public void visit(DoWhileNode node) {
        label(node.begLabel());
        compileStmt(node.body());
        label(node.continueLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        jnz(node.begLabel());
        label(node.endLabel());
    }

    public void visit(ForNode node) {
        compileStmt(node.init());
        label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), "ax");
        jz(node.endLabel());
        compileStmt(node.body());
        label(node.continueLabel());
        compileStmt(node.incr());
        jmp(node.begLabel());
        label(node.endLabel());
    }

    public void visit(BreakNode node) {
        jmp(node.targetLabel());
    }

    public void visit(ContinueNode node) {
        jmp(node.targetLabel());
    }

    public void visit(LabelNode node) {
        label(node.label());
        compileStmt(node.stmt());
    }

    public void visit(GotoNode node) {
        jmp(node.targetLabel());
    }

    //
    // Expressions
    //

    public void visit(BinaryOpNode node) {
        compile(node.right());
        push(reg("ax"));
        compile(node.left());
        pop(reg("cx"));
        compileBinaryOp(node.operator(), node.type());
    }

    protected void compileBinaryOp(String op, Type t) {
        if (op.equals("+")) {
            add(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("-")) {
            sub(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("*")) {
            imul(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("/") || op.equals("%")) {
            if (t.isSigned()) {
                cltd();
                idiv(t, reg("cx", t));
            }
            else {
                mov(imm(0), reg("dx"));
                div(t, reg("cx", t));
            }
            if (op.equals("%")) {
                mov(reg("dx"), reg("ax"));
            }
        }
        else if (op.equals("&")) {
            and(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("|")) {
            or(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals("^")) {
            xor(t, reg("cx", t), reg("ax", t));
        }
        else if (op.equals(">>")) {
            sar(t, cl(), reg("ax", t));
        }
        else if (op.equals("<<")) {
            sal(t, cl(), reg("ax", t));
        }
        else {
            // Comparison operators
            cmp(t, reg("cx", t), reg("ax", t));
            if      (op.equals("=="))   sete (al());
            else if (op.equals("!="))   setne(al());
            else if (op.equals(">"))    setg (al());
            else if (op.equals(">="))   setge(al());
            else if (op.equals("<"))    setl (al());
            else if (op.equals("<="))   setle(al());
            else {
                throw new Error("unknown binary operator: " + op);
            }
            movzb(t, al(), reg("ax", t));
        }
    }

    public void visit(UnaryOpNode node) {
        compile(node.expr());
        if (node.operator().equals("+")) {
            ;
        }
        else if (node.operator().equals("-")) {
            neg(node.expr().type(), reg("ax", node.expr().type()));
        }
        else if (node.operator().equals("~")) {
            not(node.expr().type(), reg("ax", node.expr().type()));
        }
        else if (node.operator().equals("!")) {
            testCond(node.expr().type(), "ax");
            sete(al());
            movzbl(al(), reg("ax"));
        }
    }

    public void visit(PrefixOpNode node) {
        compileIncDec(node.operator(), node.expr());
        load(node.expr().type(), node.expr().address(), "ax");
    }

    public void visit(SuffixOpNode node) {
        load(node.expr().type(), node.expr().address(), "ax");
        compileIncDec(node.operator(), node.expr());
    }

    protected void compileIncDec(String op, ExprNode e) {
        if (op.equals("++")) {
            if (e.type().isInteger()) {
                inc(e.type(), e.address());
            }
            else {
                add(imm(e.type().size()), e.address());
            }
        }
        else if (op.equals("--")) {
            if (e.type().isInteger()) {
                dec(e.type(), e.address());
            }
            else {
                sub(imm(e.type().size()), e.address());
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
            lea(node.address(), reg("ax"));
        }
        else {
            load(node.type(), node.address(), "ax");
        }
    }

    public void visit(IntegerLiteralNode node) {
        mov(node.type(), imm(node.value()), reg("ax", node.type()));
    }

    public void visit(StringLiteralNode node) {
        load(node.type(), imm(node.label()), "ax");
    }

    //
    // Assignable expressions
    //

    public void visit(AssignNode node) {
        if (node.lhs().isConstantAddress()) {
            compile(node.rhs());
            save(node.type(), "ax", node.lhs().address());
        }
        else {
            compile(node.rhs());
            push(reg("ax"));
            compileLHS(node.lhs());
            pop(reg("ax"));
            save(node.type(), "ax", addr(PTRREG));
        }
    }

    public void visit(OpAssignNode node) {
        compile(node.rhs());
        mov(reg("ax"), reg("cx"));
        load(node.type(), node.lhs().address(), "ax");
        compileBinaryOp(node.operator(), node.type());
        save(node.type(), "ax", node.lhs().address());
    }

    // FIXME: use -4(%edx,%esi,4) addressing
    public void visit(ArefNode node) {
        if (node.expr().type().isPointerAlike()) {
            compile(node.expr());
            push(reg("ax"));
        }
        else {
            compileLHS(node.expr());
            push(reg(PTRREG));
        }
        compile(node.index());
        imul(imm(node.type().size()), reg("ax"));
        pop(reg(PTRREG));
        add(reg("ax"), reg(PTRREG));
        load(node.type(), addr(PTRREG), "ax");
    }

    public void visit(MemberNode node) {
        compileLHS(node.expr());
        load(node.type(), addr2(node.offset(), PTRREG), "ax");
    }

    public void visit(PtrMemberNode node) {
        compileLHS(node.expr());
        load(node.type(), addr(PTRREG), PTRREG);
        load(node.type(), addr2(node.offset(), PTRREG), "ax");
    }

    public void visit(DereferenceNode node) {
        compile(node.expr());
        load(node.type(), addr("ax"), "ax");
    }

    public void visit(AddressNode node) {
        compileLHS(node.expr());
        mov(reg(PTRREG), reg("ax"));
    }

    static final String PTRREG = "bx";

    protected void compileLHS(Node node) {
comment("compileLHS: " + node.getClass().getName() + " {");
        if (node instanceof VariableNode) {
            // FIXME: support static variables
            VariableNode n = (VariableNode)node;
            lea(n.address(), reg(PTRREG));
        }
        else if (node instanceof ArefNode) {
            ArefNode n = (ArefNode)node;
            push(reg("ax"));
            compile(n.index());
            imul(imm(n.type().size()), reg("ax"));
            push(reg("ax"));
            if (n.expr().type().isPointerAlike()) {
                compile(n.expr());
                mov(reg("ax"), reg(PTRREG));
            }
            else {
                compileLHS(n.expr());
            }
            pop(reg("cx"));
            add(reg("cx"), reg(PTRREG));
            pop(reg("ax"));
        }
        else if (node instanceof MemberNode) {
            MemberNode n = (MemberNode)node;
            compileLHS(n.expr());
            add(imm(n.offset()), reg(PTRREG));
        }
        else if (node instanceof DereferenceNode) {
            DereferenceNode n = (DereferenceNode)node;
            push(reg("ax"));
            compile(n.expr());
            mov(reg("ax"), reg(PTRREG));
            pop(reg("ax"));
        }
        else if (node instanceof PtrMemberNode) {
            PtrMemberNode n = (PtrMemberNode)node;
            push(reg("ax"));
            compile(n.expr());
            add(imm(n.offset()), reg("ax"));
            mov(reg("ax"), reg(PTRREG));
            pop(reg("ax"));
        }
        else if (node instanceof PrefixOpNode) {
            PrefixOpNode n = (PrefixOpNode)node;
            compileLHS(n.expr());
            if (n.operator().equals("++")) {
                add(imm(n.expr().type().size()), addr(PTRREG));
                add(imm(n.expr().type().size()), reg(PTRREG));
            }
            else {
                sub(imm(n.expr().type().size()), addr(PTRREG));
                sub(imm(n.expr().type().size()), reg(PTRREG));
            }
        }
        else if (node instanceof SuffixOpNode) {
            SuffixOpNode n = (SuffixOpNode)node;
            compileLHS(n.expr());
            if (n.operator().equals("++")) {
                add(imm(n.expr().type().size()), reg(PTRREG));
            }
            else {
                sub(imm(n.expr().type().size()), reg(PTRREG));
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
comment("compileLHS: }");
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

    protected void load(Type type, AsmEntity addr, String reg) {
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

    protected void save(Type type, String reg, AsmEntity addr) {
        as.mov(type, reg(reg, type), addr);
    }

    public void comment(String str) { as.comment(str); }
    public void line(String str) { as.line(str); }
    public void _file(String name) { as._file(name); }
    public void _text() { as._text(); }
    public void _data() { as._data(); }
    public void _section(String name) { as._section(name); }
    public void _globl(String sym) { as._globl(sym); }
    public void _local(String sym) { as._local(sym); }
    public void _comm(String sym, long sz, long a) { as._comm(sym, sz, a); }
    public void _align(long n) { as._align(n); }
    public void _type(String sym, String type) { as._type(sym, type); }
    public void _size(String sym, long size) { as._size(sym, size); }
    public void _size(String sym, String size) { as._size(sym, size); }
    public void _byte(long n) { as._byte(n); }
    public void _value(long n) { as._value(n); }
    public void _long(long n) { as._long(n); }
    public void _quad(long n) { as._quad(n); }
    public void _string(String str) { as._string(str); }
    public void label(String sym) { as.label(sym); }
    public void label(Label label) { as.label(label); }

    public void jmp(Label label) { as.jmp(label); }
    public void jz(Label label) { as.jz(label); }
    public void jnz(Label label) { as.jnz(label); }
    public void je(Label label) { as.je(label); }
    public void jne(Label label) { as.jne(label); }
    public void cmp(Type t, Register a, Register b) { as.cmp(t, a, b); }
    public void sete(Register reg) { as.sete(reg); }
    public void setne(Register reg) { as.setne(reg); }
    public void setg(Register reg) { as.setg(reg); }
    public void setl(Register reg) { as.setl(reg); }
    public void setge(Register reg) { as.setge(reg); }
    public void setle(Register reg) { as.setle(reg); }
    public void test(Type type, Register a, Register b) { as.test(type, a, b); }
    public void push(Register reg) { as.push(reg); }
    public void pop(Register reg) { as.pop(reg); }
    public void call(String sym) { as.call(sym); }
    public void ptrcall(Register reg) { as.ptrcall(reg); }
    public void ret() { as.ret(); }
    public void mov(AsmEntity src, AsmEntity dest) { as.mov(src, dest); }
    public void mov(Type type, AsmEntity src, AsmEntity dest) { as.mov(type, src, dest); }
    public void movsbl(AsmEntity src, AsmEntity dest) { as.movsbl(src, dest); }
    public void movswl(AsmEntity src, AsmEntity dest) { as.movswl(src, dest); }
    public void movzb(Type type, AsmEntity src, AsmEntity dest) { as.movzb(type, src, dest); }
    public void movzbl(AsmEntity src, AsmEntity dest) { as.movzbl(src, dest); }
    public void movzwl(AsmEntity src, AsmEntity dest) { as.movzwl(src, dest); }
    public void lea(AsmEntity src, AsmEntity dest) { as.lea(src, dest); }
    public void lea(Type type, AsmEntity src, AsmEntity dest) { as.lea(type, src, dest); }
    public void neg(Type type, Register reg) { as.neg(type, reg); }
    public void inc(Type type, AsmEntity reg) { as.inc(type, reg); }
    public void dec(Type type, AsmEntity reg) { as.dec(type, reg); }
    public void add(AsmEntity diff, AsmEntity base) { as.add(diff, base); }
    public void add(Type type, AsmEntity diff, AsmEntity base) { as.add(type, diff, base); }
    public void sub(AsmEntity diff, AsmEntity base) { as.sub(diff, base); }
    public void sub(Type type, AsmEntity diff, AsmEntity base) { as.sub(type, diff, base); }
    public void imul(AsmEntity m, Register base) { as.imul(m, base); }
    public void imul(Type type, AsmEntity m, Register base) { as.imul(type, m, base); }
    public void cltd() { as.cltd(); }
    public void div(Type type, Register base) { as.div(type, base); }
    public void idiv(Type type, Register base) { as.idiv(type, base); }
    public void not(Type type, Register reg) { as.not(type, reg); }
    public void and(Type type, Register bits, Register base) { as.and(type, bits, base); }
    public void or(Type type, Register bits, Register base) { as.or(type, bits, base); }
    public void xor(Type type, Register bits, Register base) { as.xor(type, bits, base); }
    public void sar(Type type, Register n, Register base) { as.sar(type, n, base); }
    public void sal(Type type, Register n, Register base) { as.sal(type, n, base); }
    public void shr(Type type, Register n, Register base) { as.shr(type, n, base); }
    public void shl(Type type, Register n, Register base) { as.shl(type, n, base); }
}
