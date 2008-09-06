package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator extends Visitor implements ASTLHSVisitor {
    // #@@range/generate
    static public String generate(AST ast, TypeTable typeTable,
                                  ErrorHandler errorHandler) {
        Assembler as = new Assembler(typeTable.unsignedLong());
        CodeGenerator gen = new CodeGenerator(as, errorHandler);
        return gen.generateAssembly(ast, typeTable);
    }
    // #@@}

    // #@@range/ctor{
    protected Assembler as;
    protected ErrorHandler errorHandler;
    protected TypeTable typeTable;
    protected DefinedFunction currentFunction;

    public CodeGenerator(Assembler as, ErrorHandler errorHandler) {
        this.as = as;
        this.errorHandler = errorHandler;
    }
    // #@@}

    /** Compiles "ast" and generates assembly code. */
    // #@@range/generateAssembly
    public String generateAssembly(AST ast, TypeTable typeTable) {
        this.typeTable = typeTable;
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
    // #@@}

    /**
     * Sets address for...
     *   * public global variables
     *   * private global variables
     *   * static local variables
     */
    // #@@range/allocateGlobalVariable
    protected void allocateGlobalVariables(Iterator vars) {
        while (vars.hasNext()) {
            Variable var = (Variable)vars.next();
            var.setAddress(globalVariableAddress(var.symbol()));
        }
    }
    // #@@}

    /**
     * Sets address for...
     *   * public common symbols
     *   * private common symbols
     */
    // #@@range/allocateCommonSymbols
    protected void allocateCommonSymbols(Iterator comms) {
        while (comms.hasNext()) {
            Variable var = (Variable)comms.next();
            var.setAddress(commonSymbolAddress(var.symbol()));
        }
    }
    // #@@}

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
    // #@@range/compileGlobalVariables{
    protected void compileGlobalVariables(Iterator vars) {
        _data();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            dataEntry(var);
        }
    }
    // #@@}

    /** Generates initialized entries */
    // #@@range/dataEntry{
    protected void dataEntry(DefinedVariable ent) {
        if (!ent.isPrivate()) {
            _globl(csymbol(ent.symbol()));
        }
        _align(ent.alignment());
        _type(csymbol(ent.symbol()), "@object");
        _size(csymbol(ent.symbol()), ent.allocSize());
        label(csymbol(ent.symbol()));
        compileImmediate(ent.type(), ent.initializer());
    }
    // #@@}

    /** Generates immediate values for .data section */
    // #@@range/compileImmediates{
    protected void compileImmediate(Type type, ExprNode node) {
        if (node instanceof IntegerLiteralNode) {
            IntegerLiteralNode expr = (IntegerLiteralNode)node;
            switch ((int)type.allocSize()) {
            case 1: _byte(expr.value());    break;
            case 2: _value(expr.value());   break;
            case 4: _long(expr.value());    break;
            case 8: _quad(expr.value());    break;
            default:
                throw new Error("entry size must be 1,2,4,8");
            }
        }
        else if (node instanceof StringLiteralNode) {
            StringLiteralNode expr = (StringLiteralNode)node;
            switch ((int)type.allocSize()) {
            case 4: _long(expr.label());   break;
            case 8: _quad(expr.label());   break;
            default:
                throw new Error("pointer size must be 4,8");
            }
        }
        else {
            throw new Error("unknown literal node type" + node.getClass());
        }
    }
    // #@@}

    /** Generates BSS entries */
    // #@@range/compileCommonSymbols{
    protected void compileCommonSymbols(Iterator ents) {
        while (ents.hasNext()) {
            Variable ent = (Variable)ents.next();
            if (ent.isPrivate()) {
                _local(csymbol(ent.symbol()));
            }
            _comm(csymbol(ent.symbol()), ent.allocSize(), ent.alignment());
        }
    }
    // #@@}

    /** Generates .rodata entry (constant strings) */
    // #@@range/compileConstants{
    protected void compileConstants(ConstantTable table) {
        _section(".rodata");
        Iterator ents = table.entries();
        while (ents.hasNext()) {
            ConstantEntry ent = (ConstantEntry)ents.next();
            label(ent.label());
            _string(ent.value());
        }
    }
    // #@@}

    /** Compiles all functions and generates .text section. */
    // #@@range/compileFunctions{
    protected void compileFunctions(Iterator funcs) {
        _text();
        while (funcs.hasNext()) {
            DefinedFunction func = (DefinedFunction)funcs.next();
            compileFunction(func);
        }
    }
    // #@@}

    /** Compiles a function. */
    // #@@range/compileFunction{
    protected void compileFunction(DefinedFunction func) {
        long numSavedRegs = 0;  // 1 for PIC
        allocateParameters(func);
        long lvarBytes = allocateLocalVariables(func, numSavedRegs);

        currentFunction = func;
        String symbol = csymbol(func.name());
        if (! func.isPrivate()) {
            _globl(symbol);
        }
        _type(symbol, "@function");
        label(symbol);
        prologue(func, lvarBytes);
        compile(func.body());
        epilogue(func, lvarBytes);
        _size(symbol, ".-" + symbol);
    }
    // #@@}

    // #@@range/compile{
    protected void compile(Node n) {
        n.accept(this);
    }
    // #@@}

    // #@@range/tmpsymbol{
    // platform dependent
    protected String tmpsymbol(String sym) {
        return sym;
    }
    // #@@}

    // #@@range/csymbol{
    // platform dependent
    protected String csymbol(String sym) {
        return sym;
    }
    // #@@}

    // #@@range/prologue{
    protected void prologue(DefinedFunction func, long lvarBytes) {
        push(bp());
        mov(sp(), bp());
        if (lvarBytes > 0) {
            extendStack(lvarBytes);
        }
    }
    // #@@}

    // #@@range/epilogue{
    protected void epilogue(DefinedFunction func, long lvarBytes) {
        label(epilogueLabel(func));
        if (lvarBytes > 0) {
            shrinkStack(lvarBytes);
        }
        mov(bp(), sp());
        pop(bp());
        ret();
    }
    // #@@}

    // #@@range/jmpEpilogue{
    protected void jmpEpilogue() {
        jmp(new Label(epilogueLabel(currentFunction)));
    }
    // #@@}

    // #@@range/epilogueLabel{
    protected String epilogueLabel(DefinedFunction func) {
        return ".L" + func.name() + "$epilogue";
    }
    // #@@}

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
    static final protected boolean stackGrowsLower = true;
    static final protected long stackWordSize = 4;
    static final protected long stackAlignment = stackWordSize;
    // +1 for return address, +1 for saved bp
    static final protected long paramStartWord = 2;

    protected void allocateParameters(DefinedFunction func) {
        Iterator vars = func.parameters();
        long word = paramStartWord;
        while (vars.hasNext()) {
            Parameter var = (Parameter)vars.next();
            if (stackGrowsLower) {
                var.setAddress(mem(word * stackWordSize, bp()));
            }
            else {
                throw new Error("unsupported stack layout");
            }
            word++;
        }
    }

    // Fixes addresses of local variables.
    // Returns byte-length of the local variable area.
    protected long allocateLocalVariables(DefinedFunction func,
                                          long numSavedRegs) {
        long initLen = numSavedRegs * stackWordSize;
        long maxLen = allocateScope(func.body().scope(), initLen);
        return maxLen - initLen;
    }

    protected long allocateScope(LocalScope scope, long parentStackLen) {
        long len = parentStackLen;
        Iterator vars = scope.variables();
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            if (stackGrowsLower) {
                len = Assembler.align(len + var.allocSize(), stackAlignment);
                var.setAddress(mem(-len, bp()));
            }
            else {
                var.setAddress(mem(len, bp()));
                len = Assembler.align(len + var.allocSize(), stackAlignment);
            }
        }
        // Allocate local variables in child scopes.
        // We allocate child scopes in the same area (overrapped).
        long maxLen = len;
        Iterator scopes = scope.children();
        while (scopes.hasNext()) {
            LocalScope s = (LocalScope)scopes.next();
            long childLen = allocateScope(s, len);
            maxLen = Math.max(maxLen, childLen);
        }
        return maxLen;
    }

    protected void extendStack(long len) {
        add(imm(len * (stackGrowsLower ? -1 : 1)), sp());
    }

    protected void shrinkStack(long len) {
        add(imm(len * (stackGrowsLower ? 1 : -1)), sp());
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
        if (node.isStaticCall()) {
            if (node.function().isDefined()) {
                call(csymbol(node.function().name()));
            }
            else {
                call(tmpsymbol(node.function().name()));
            }
        }
        else {  // function call via pointer
            compile(node.expr());
            callAbsolute(reg("ax"));
        }
        if (node.numArgs() > 0) {
            // >4 bytes arguments are not supported.
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
                save(var.type(), reg("ax"), var.address());
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

    private void testCond(Type t, Register reg) {
        test(t, reg.forType(t), reg.forType(t));
    }

    public void visit(IfNode node) {
        compile(node.cond());
        testCond(node.cond().type(), reg("ax"));
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
        testCond(node.cond().type(), reg("ax"));
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
            if (! caseNode.isDefault()) {
                Iterator values = caseNode.values();
                while (values.hasNext()) {
                    IntegerLiteralNode ival = (IntegerLiteralNode)values.next();
                    mov(imm(ival.value()), reg("cx"));
                    cmp(t, reg("cx", t), reg("ax", t));
                    je(caseNode.beginLabel());
                }
            }
            else {
                jmp(caseNode.beginLabel());
            }
        }
        jmp(node.endLabel());
        cases = node.cases();
        while (cases.hasNext()) {
            compile((CaseNode)cases.next());
        }
        label(node.endLabel());
    }

    public void visit(CaseNode node) {
        label(node.beginLabel());
        compile(node.body());
    }

    public void visit(LogicalAndNode node) {
        compile(node.left());
        testCond(node.left().type(), reg("ax"));
        jz(node.endLabel());
        compile(node.right());
        label(node.endLabel());
    }

    public void visit(LogicalOrNode node) {
        compile(node.left());
        testCond(node.left().type(), reg("ax"));
        jnz(node.endLabel());
        compile(node.right());
        label(node.endLabel());
    }

    public void visit(WhileNode node) {
        label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), reg("ax"));
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
        testCond(node.cond().type(), reg("ax"));
        jnz(node.begLabel());
        label(node.endLabel());
    }

    public void visit(ForNode node) {
        compileStmt(node.init());
        label(node.begLabel());
        compile(node.cond());
        testCond(node.cond().type(), reg("ax"));
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

    // spills: dx
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
            if (t.isSigned()) {
                sar(t, cl(), reg("ax", t));
            }
            else {
                shr(t, cl(), reg("ax", t));
            }
        }
        else if (op.equals("<<")) {
            sal(t, cl(), reg("ax", t));
        }
        else {
            // Comparison operators
            cmp(t, reg("cx", t), reg("ax", t));
            if (!t.isPointer() && t.isSigned()) {
                if      (op.equals("=="))   sete (al());
                else if (op.equals("!="))   setne(al());
                else if (op.equals(">"))    setg (al());
                else if (op.equals(">="))   setge(al());
                else if (op.equals("<"))    setl (al());
                else if (op.equals("<="))   setle(al());
                else {
                    throw new Error("unknown binary operator: " + op);
                }
            }
            else {
                if      (op.equals("=="))   sete (al());
                else if (op.equals("!="))   setne(al());
                else if (op.equals(">"))    seta (al());
                else if (op.equals(">="))   setae(al());
                else if (op.equals("<"))    setb (al());
                else if (op.equals("<="))   setbe(al());
                else {
                    throw new Error("unknown binary operator: " + op);
                }
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
            testCond(node.expr().type(), reg("ax"));
            sete(al());
            movzbl(al(), reg("ax"));
        }
    }

    public void visit(PrefixOpNode node) {
        if (node.expr().isConstantAddress()) {
            load(node.expr().type(), node.expr().address(), reg("ax"));
            compileUnaryArithmetic(node, reg("ax"));
            save(node.expr().type(), reg("ax"), node.expr().address());
        }
        else {
            compileLHS(node.expr());
            mov(reg("ax"), reg("cx"));
            load(node.expr().type(), mem(reg("cx")), reg("ax"));
            compileUnaryArithmetic(node, reg("ax"));
            save(node.expr().type(), reg("ax"), mem(reg("cx")));
        }
    }

    public void visit(SuffixOpNode node) {
        if (node.expr().isConstantAddress()) {
            load(node.expr().type(), node.expr().address(), reg("ax"));
            mov(reg("ax"), reg("cx"));
            compileUnaryArithmetic(node, reg("cx"));
            save(node.expr().type(), reg("cx"), node.expr().address());
        }
        else {
            compileLHS(node.expr());
            mov(reg("ax"), reg("cx"));
            load(node.expr().type(), mem(reg("cx")), reg("ax"));
            mov(reg("ax"), reg("dx"));
            compileUnaryArithmetic(node, reg("dx"));
            save(node.expr().type(), reg("dx"), mem(reg("cx")));
        }
    }

    // spills: (none)
    protected void compileUnaryArithmetic(UnaryArithmeticOpNode node,
                                          AsmEntity dest) {
        if (node.operator().equals("++")) {
            add(imm(node.amount()), dest);
        }
        else if (node.operator().equals("--")) {
            sub(imm(node.amount()), dest);
        }
        else {
            throw new Error("unknown unary operator: " + node.operator());
        }
    }

    public void visit(CastNode node) {
        compile(node.expr());
        Type src = node.expr().type();
        Type dest = node.type();
        // We need not execute downcast because we can cast big value
        // to small value by just cutting off higer bits.
        if (dest.size() > src.size()) {
            if (src.isSigned()) {
                movsx(src, dest,
                      reg("ax").forType(src), reg("ax").forType(dest));
            }
            else {
                movzx(src, dest,
                      reg("ax").forType(src), reg("ax").forType(dest));
            }
        }
    }

    public void visit(SizeofExprNode node) {
        long val = node.expr().type().allocSize();
        mov(node.type(), imm(val), reg("ax", node.type()));
    }

    public void visit(SizeofTypeNode node) {
        long val = node.operand().allocSize();
        mov(node.type(), imm(val), reg("ax", node.type()));
    }

    public void visit(VariableNode node) {
        if (node.type().isAllocatedArray()) {
            lea(node.address(), reg("ax"));
        }
        else {
            load(node.type(), node.address(), reg("ax"));
        }
    }

    public void visit(IntegerLiteralNode node) {
        mov(node.type(), imm(node.value()), reg("ax", node.type()));
    }

    public void visit(StringLiteralNode node) {
        load(node.type(), imm(node.label()), reg("ax"));
    }

    //
    // Assignable expressions
    //

    public void visit(AssignNode node) {
        if (node.lhs().isConstantAddress()) {
            compile(node.rhs());
            save(node.type(), reg("ax"), node.lhs().address());
        }
        else {
            compile(node.rhs());
            push(reg("ax"));
            compileLHS(node.lhs());
            mov(reg("ax"), reg("cx"));
            pop(reg("ax"));
            save(node.type(), reg("ax"), mem(reg("cx")));
        }
    }

    public void visit(OpAssignNode node) {
        compile(node.rhs());
        if (node.lhs().isConstantAddress()) {
            mov(reg("ax"), reg("cx"));
            load(node.type(), node.lhs().address(), reg("ax"));
            compileBinaryOp(node.operator(), node.type());
            save(node.type(), reg("ax"), node.lhs().address());
        }
        else {
            push(reg("ax"));
            compileLHS(node.lhs());
            mov(reg("ax"), reg("dx"));
            load(node.type(), mem(reg("dx")), reg("ax"));
            pop(reg("cx"));
            push(reg("dx"));
            compileBinaryOp(node.operator(), node.type());
            pop(reg("dx"));
            save(node.type(), reg("ax"), mem(reg("dx")));
        }
    }

    public void visit(ArefNode node) {
        compileLHS(node);
        load(node.type(), mem(reg("ax")), reg("ax"));
    }

    public void visit(MemberNode node) {
        compileLHS(node.expr());
        load(node.type(), mem(node.offset(), reg("ax")), reg("ax"));
    }

    public void visit(PtrMemberNode node) {
        compile(node.expr());
        load(node.type(), mem(node.offset(), reg("ax")), reg("ax"));
    }

    public void visit(DereferenceNode node) {
        compile(node.expr());
        load(node.type(), mem(reg("ax")), reg("ax"));
    }

    public void visit(AddressNode node) {
        compileLHS(node.expr());
    }

    protected void compileLHS(Node node) {
        comment("compileLHS: " + node.getClass().getSimpleName() + " {");
        node.acceptLHS(this);
        comment("compileLHS: }");
    }

    public void visitLHS(VariableNode node) {
        lea(node.address(), reg("ax"));
    }

    public void visitLHS(ArefNode node) {
        compile(node.index());
        imul(imm(node.type().size()), reg("ax"));
        push(reg("ax"));
        if (node.expr().type().isPointerAlike()) {
            compile(node.expr());
        }
        else {
            compileLHS(node.expr());
        }
        pop(reg("cx"));
        add(reg("cx"), reg("ax"));
    }

    public void visitLHS(MemberNode node) {
        compileLHS(node.expr());
        add(imm(node.offset()), reg("ax"));
    }

    public void visitLHS(DereferenceNode node) {
        compile(node.expr());
    }

    public void visitLHS(PtrMemberNode node) {
        compile(node.expr());
        add(imm(node.offset()), reg("ax"));
    }

    /*
     *  x86 assembly DSL
     */

    protected Register bp() { return reg("bp"); }
    protected Register sp() { return reg("sp"); }
    protected Register al() { return new Register(1, "ax"); }
    protected Register cl() { return new Register(1, "cx"); }

    protected Register reg(String name, Type type) {
        return new Register(name).forType(type);
    }

    protected Register reg(String name) {
        return new Register(name);
    }

    protected SimpleAddress mem(Register reg) {
        return new SimpleAddress(reg);
    }

    protected CompositeAddress mem(long offset, Register reg) {
        return new CompositeAddress(offset, reg);
    }

    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected Reference imm(Label label) {
        return new Reference(label);
    }

    protected void load(Type type, AsmEntity addr, Register reg) {
        switch ((int)type.size()) {
        case 1:
            if (type.isSigned()) {  // signed char
                movsbl(addr, reg);
            } else {                // unsigned char
                movzbl(addr, reg);
            }
            break;
        case 2:
            if (type.isSigned()) {  // signed short
                movswl(addr, reg);
            } else {                // unsigned short
                movzwl(addr, reg);
            }
            break;
        default:                    // int, long, long_long
            mov(type, addr, reg.forType(type));
            break;
        }
    }

    protected void save(Type type, Register reg, AsmEntity addr) {
        mov(type, reg.forType(type), addr);
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
    public void _long(Label label) { as._long(label); }
    public void _quad(long n) { as._quad(n); }
    public void _quad(Label label) { as._quad(label); }
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
    public void seta(Register reg) { as.seta(reg); }
    public void setae(Register reg) { as.setae(reg); }
    public void setb(Register reg) { as.setb(reg); }
    public void setbe(Register reg) { as.setbe(reg); }
    public void setg(Register reg) { as.setg(reg); }
    public void setge(Register reg) { as.setge(reg); }
    public void setl(Register reg) { as.setl(reg); }
    public void setle(Register reg) { as.setle(reg); }
    public void test(Type type, Register a, Register b) { as.test(type, a, b); }
    public void push(Register reg) { as.push(reg); }
    public void pop(Register reg) { as.pop(reg); }
    public void call(String sym) { as.call(sym); }
    public void callAbsolute(Register reg) { as.callAbsolute(reg); }
    public void ret() { as.ret(); }
    public void mov(AsmEntity src, AsmEntity dest) { as.mov(src, dest); }
    public void mov(Type type, AsmEntity src, AsmEntity dest) { as.mov(type, src, dest); }
    public void movsx(Type from, Type to, AsmEntity src, AsmEntity dest) { as.movsx(from, to, src, dest); }
    public void movzx(Type from, Type to, AsmEntity src, AsmEntity dest) { as.movzx(from, to, src, dest); }
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
}
