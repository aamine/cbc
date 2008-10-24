package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator
                extends Visitor implements ASTLHSVisitor, ELFConstants {
    // #@@range/ctor{
    protected CodeGeneratorOptions options;
    protected ErrorHandler errorHandler;
    protected LinkedList<Assembler> asStack;
    protected Assembler as;
    protected TypeTable typeTable;
    protected DefinedFunction currentFunction;

    public CodeGenerator(CodeGeneratorOptions options,
                         ErrorHandler errorHandler) {
        this.options = options;
        this.errorHandler = errorHandler;
        this.asStack = new LinkedList<Assembler>();
    }
    // #@@}

    /** Compiles "ast" and generates assembly code. */
    // #@@range/generate
    public String generate(AST ast) {
        this.typeTable = ast.typeTable();
        pushAssembler();
        SymbolTable constSymbols = new SymbolTable(Assembler.CONST_SYMBOL_BASE);
        for (ConstantEntry ent : ast.constantTable().entries()) {
            locateConstant(ent, constSymbols);
        }
        for (Variable var : ast.allGlobalVariables()) {
            locateGlobalVariable(var);
        }
        for (Function func : ast.allFunctions()) {
            locateFunction(func);
        }
        compileAST(ast);
        return popAssembler().toSource();
    }

    protected void pushAssembler() {
        this.as = newAssembler();
        asStack.add(this.as);
    }

    protected Assembler popAssembler() {
        Assembler popped = asStack.removeLast();
        this.as = asStack.isEmpty() ? null : asStack.getLast();
        return popped;
    }

    protected Assembler newAssembler() {
        return new Assembler(typeTable.unsignedLong());
    }

    public void compileAST(AST ast) {
        _file(ast.fileName());
        // .data
        _data();
        for (DefinedVariable gvar : ast.definedGlobalVariables()) {
            dataEntry(gvar);
        }
        if (!ast.constantTable().isEmpty()) {
            _section(".rodata");
            for (ConstantEntry ent : ast.constantTable()) {
                compileStringLiteral(ent);
            }
        }
        // .text
        if (ast.functionDefined()) {
            _text();
            for (DefinedFunction func : ast.definedFunctions()) {
                compileFunction(func);
            }
        }
        // .bss
        for (DefinedVariable var : ast.definedCommonSymbols()) {
            compileCommonSymbol(var);
        }
        // others
        if (options.isPICRequired()) {
            PICThunk(GOTBaseReg());
        }
    }
    // #@@}

    protected void locateConstant(ConstantEntry ent, SymbolTable symbols) {
        ent.setSymbol(symbols.newSymbol());
        if (options.isPICRequired()) {
            Symbol offset = localGOTSymbol(ent.symbol());
            ent.setMemref(mem(offset, GOTBaseReg()));
        }
        else {
            ent.setMemref(mem(ent.symbol()));
            ent.setAddress(imm(ent.symbol()));
        }
    }

    protected void locateGlobalVariable(Variable var) {
        MemoryReference mem;
        if (options.isPICRequired()) {
            if (var.isPrivate()) {
                mem = mem(localGOTSymbol(var.symbol()), GOTBaseReg());
                var.setMemref(mem);
            }
            else {
                mem = mem(globalGOTSymbol(var.symbol()), GOTBaseReg());
                var.setAddress(mem);
            }
        }
        else {
            mem = mem(globalSymbol(var.symbolString()));
            var.setMemref(mem);
        }
    }

    protected void locateFunction(Function func) {
        func.setSymbol(functionSymbol(func));
        func.setAddress(functionAddress(func));
    }

    protected Symbol functionSymbol(Function func) {
        if (func.isDefined()) {
            return globalSymbol(func.name());
        }
        else {
            Symbol sym = privateSymbol(func.name());
            return options.isPICRequired() ? PLTSymbol(sym) : sym;
        }
    }

    protected AsmOperand functionAddress(Function func) {
        Symbol sym = new NamedSymbol(func.name());
        if (options.isPICRequired()) {
            if (func.isPrivate()) {
                return mem(localGOTSymbol(sym), GOTBaseReg());
            }
            else {
                return mem(globalGOTSymbol(sym), GOTBaseReg());
            }
        }
        else {
            return imm(sym);
        }
    }

    /** Generates initialized entries */
    // #@@range/dataEntry{
    protected void dataEntry(DefinedVariable ent) {
        Symbol sym = globalSymbol(ent.symbolString());
        if (!ent.isPrivate()) {
            _globl(sym);
        }
        _align(ent.alignment());
        _type(sym, "@object");
        _size(sym, ent.allocSize());
        label(sym);
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
            case 4: _long(expr.symbol());   break;
            case 8: _quad(expr.symbol());   break;
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
    // #@@range/compileCommonSymbol{
    protected void compileCommonSymbol(DefinedVariable var) {
        Symbol sym = globalSymbol(var.symbolString());
        if (var.isPrivate()) {
            _local(sym);
        }
        _comm(sym, var.allocSize(), var.alignment());
    }
    // #@@}

    /** Generates .rodata entry (constant strings) */
    // #@@range/compileStringLiteral{
    protected void compileStringLiteral(ConstantEntry ent) {
        label(ent.symbol());
        _string(ent.value());
    }
    // #@@}

    // #@@range/globalSymbol{
    // platform dependent
    protected Symbol globalSymbol(String sym) {
        return new NamedSymbol(sym);
    }
    // #@@}

    // #@@range/privateSymbol{
    // platform dependent
    protected Symbol privateSymbol(String sym) {
        return new NamedSymbol(sym);
    }
    // #@@}

    //
    // PIC related constants and codes
    //

    static protected final Symbol GOT = new NamedSymbol("_GLOBAL_OFFSET_TABLE_");

    protected void loadGOTBaseAddress(Register reg) {
        call(PICThunkSymbol(reg));
        add(imm(GOT), reg);
    }

    protected Symbol PICThunkSymbol(Register reg) {
        return new NamedSymbol("__i686.get_pc_thunk." + reg.baseName());
    }

    static protected final String
    PICThunkSectionFlags = SectionFlag_allocatable
                         + SectionFlag_executable
                         + SectionFlag_sectiongroup;

    protected void PICThunk(Register reg) {
        // ELF section declaration; format:
        //
        //     .section NAME, FLAGS, TYPE, flag_arguments
        //
        // FLAGS, TYPE, flag_arguments are optional.
        // For "M" flag (a member of a section group),
        // format is:
        //
        //     .section NAME, "...M", TYPE, section_group_name, linkage
        //
        Symbol sym = PICThunkSymbol(reg);
        _section(".text" + "." + sym.toSource(),
                 "\"" + PICThunkSectionFlags + "\"",
                 SectionType_bits,      // This section contains data
                 sym.toSource(),        // The name of section group
                Linkage_linkonce);      // Only 1 copy should be generated
        _globl(sym);
        _hidden(sym);
        _type(sym, SymbolType_function);
        label(sym);
        mov(mem(sp()), reg);    // fetch saved EIP to the GOT base register
        ret();
    }

    protected Register GOTBaseReg() {
        return reg("bx");
    }

    protected Symbol globalGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOT");
    }

    protected Symbol localGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOTOFF");
    }

    protected Symbol PLTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@PLT");
    }

    //
    // Compile Function
    //

    /** Compiles a function. */
    // #@@range/compileFunction{
    protected void compileFunction(DefinedFunction func) {
        allocateParameters(func);
        allocateLocalVariablesTemp(func.body().scope());

        Symbol sym = globalSymbol(func.name());
        if (! func.isPrivate()) {
            _globl(sym);
        }
        _type(sym, "@function");
        label(sym);
        compileFunctionBody(func);
        _size(sym, ".-" + sym.toSource());
    }
    // #@@}

    protected void compileFunctionBody(DefinedFunction func) {
        List<Assembly> bodyAsms = compileStmts(func);
        AsmStatistics stats = AsmStatistics.collect(bodyAsms);
        bodyAsms = reduceLabels(bodyAsms, stats);
        List<Register> saveRegs = usedCalleeSavedRegisters(stats);
        long lvarBytes = allocateLocalVariables(func.body().scope(),
                                                saveRegs.size());
        prologue(func, saveRegs, lvarBytes);
        if (options.isPICRequired() && stats.doesRegisterUsed(GOTBaseReg())) {
            loadGOTBaseAddress(GOTBaseReg());
        }
        as.addAll(bodyAsms);
        epilogue(func, saveRegs, lvarBytes);
    }

    protected List<Assembly> compileStmts(DefinedFunction func) {
        pushAssembler();
        currentFunction = func;
        compile(func.body());
        label(func.epilogueLabel());
        currentFunction = null;
        return options.optimizer().optimize(popAssembler().assemblies());
    }

    protected List<Assembly> reduceLabels(List<Assembly> assemblies, AsmStatistics stats) {
        List<Assembly> result = new ArrayList<Assembly>();
        for (Assembly asm : assemblies) {
            if (asm.isLabel() && ! stats.doesSymbolUsed((Label)asm)) {
                ;
            }
            else {
                result.add(asm);
            }
        }
        return result;
    }

    // #@@range/compile{
    protected void compile(Node n) {
        if (options.isVerboseAsm()) {
            comment(n.getClass().getSimpleName() + " {");
            as.indentComment();
        }
        n.accept(this);
        if (options.isVerboseAsm()) {
            as.unindentComment();
            comment("}");
        }
    }
    // #@@}

    protected List<Register> usedCalleeSavedRegisters(AsmStatistics stats) {
        List<Register> result = new ArrayList<Register>();
        for (Register reg : calleeSavedRegisters()) {
            if (stats.doesRegisterUsed(reg)) {
                result.add(reg);
            }
        }
        return result;
    }

    protected List<Register> calleeSavedRegistersCache = null;

    protected List<Register> calleeSavedRegisters() {
        if (calleeSavedRegistersCache == null) {
            List<Register> regs = new ArrayList<Register>();
            regs.add(reg("bx"));
            regs.add(reg("si"));
            regs.add(reg("di"));
            regs.add(reg("bp"));
            calleeSavedRegistersCache = regs;
        }
        return calleeSavedRegistersCache;
    }

    /* Standard IA-32 stack frame layout (after prologue)
     *
     * ======================= esp (stack top)
     * temporary
     * variables...
     * ---------------------   ebp-(4*4)
     * lvar 3
     * ---------------------   ebp-(4*3)
     * lvar 2
     * ---------------------   ebp-(4*2)
     * lvar 1
     * ---------------------   ebp-(4*1)
     * callee-saved register
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

    // #@@range/prologue{
    protected void prologue(DefinedFunction func,
                            List<Register> saveRegs,
                            long lvarBytes) {
        push(bp());
        mov(sp(), bp());
        saveRegisters(saveRegs);
        extendStack(lvarBytes);
        if (options.isVerboseAsm()) {
            for (DefinedVariable var : func.localVariables()) {
                comment("mem " + var.memref() + ": " + var.name());
            }
        }
    }
    // #@@}

    // #@@range/epilogue{
    protected void epilogue(DefinedFunction func,
                            List<Register> savedRegs,
                            long lvarBytes) {
        shrinkStack(lvarBytes);
        restoreRegisters(savedRegs);
        mov(bp(), sp());
        pop(bp());
        ret();
    }
    // #@@}

    protected void saveRegisters(List<Register> saveRegs) {
        for (Register reg : saveRegs) {
            if (! reg.equals(bp())) {   // bp is already saved.
                push(reg);
            }
        }
    }

    protected void restoreRegisters(List<Register> savedRegs) {
        ListIterator<Register> regs = savedRegs.listIterator(savedRegs.size());
        while (regs.hasPrevious()) {
            Register reg = regs.previous();
            if (! reg.equals(bp())) {   // bp is going to be restored.
                pop(reg);
            }
        }
    }

    protected void allocateParameters(DefinedFunction func) {
        long word = paramStartWord;
        for (Parameter var : func.parameters()) {
            if (stackGrowsLower) {
                var.setMemref(mem(word * stackWordSize, bp()));
            }
            else {
                throw new Error("unsupported stack layout");
            }
            word++;
        }
    }

    /**
     * Allocates addresses of local variables, but offset is still
     * not determined, assign unfixed IndirectMemoryReference.
     */
    protected void allocateLocalVariablesTemp(LocalScope scope) {
        for (DefinedVariable var : scope.allLocalVariables()) {
            var.setMemref(new IndirectMemoryReference(bp()));
        }
    }

    /**
     * Fixes addresses of local variables.
     * Returns byte-length of the local variable area.
     * Note that numSavedRegs includes bp.
     */
    protected long allocateLocalVariables(LocalScope scope, long numSavedRegs) {
        long initLen = (numSavedRegs - 1) * stackWordSize;
        long maxLen = allocateScope(scope, initLen);
        return maxLen - initLen;
    }

    protected long allocateScope(LocalScope scope, long parentStackLen) {
        long len = parentStackLen;
        for (DefinedVariable var : scope.localVariables()) {
            if (stackGrowsLower) {
                len = Assembler.align(len + var.allocSize(), stackAlignment);
                fixMemref((IndirectMemoryReference)var.memref(), -len);
            }
            else {
                fixMemref((IndirectMemoryReference)var.memref(), len);
                len = Assembler.align(len + var.allocSize(), stackAlignment);
            }
        }
        // Allocate local variables in child scopes.
        // We allocate child scopes in the same area (overrapped).
        long maxLen = len;
        for (LocalScope s : scope.children()) {
            long childLen = allocateScope(s, len);
            maxLen = Math.max(maxLen, childLen);
        }
        return maxLen;
    }

    protected void fixMemref(IndirectMemoryReference memref, long offset) {
        memref.fixOffset(offset);
    }

    protected void extendStack(long len) {
        if (len > 0) {
            add(imm(len * (stackGrowsLower ? -1 : 1)), sp());
        }
    }

    protected void shrinkStack(long len) {
        if (len > 0) {
            add(imm(len * (stackGrowsLower ? 1 : -1)), sp());
        }
    }

    /**
     * Implements cdecl function call:
     *    * All arguments are on stack.
     *    * Rewind stack by caller.
     */
    public void visit(FuncallNode node) {
        // compile function arguments from right to left.
        ListIterator<ExprNode> args = node.finalArg();
        while (args.hasPrevious()) {
            compile(args.previous());
            push(reg("ax"));
        }
        // call
        if (node.isStaticCall()) {
            // call via function name
            call(node.function().symbol());
        }
        else {
            // call via pointer
            compile(node.expr());
            callAbsolute(reg("ax"));
        }
        // rewind stack
        // >4 bytes arguments are not supported.
        shrinkStack(node.numArgs() * stackWordSize);
    }

    public void visit(ReturnNode node) {
        if (node.expr() != null) {
            compile(node.expr());
        }
        jmp(currentFunction.epilogueLabel());
    }

    //
    // Statements
    //

    public void visit(BlockNode node) {
        for (DefinedVariable var : node.scope().localVariables()) {
            if (var.initializer() != null) {
                compile(var.initializer());
                save(var.type(), reg("ax"), var.memref());
            }
        }
        for (Node stmt : node.stmts()) {
            compileStmt(stmt);
        }
    }

    protected void compileStmt(Node node) {
        if (options.isVerboseAsm()) {
            comment(node.location().numberedLine());
        }
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
        for (CaseNode cn : node.cases()) {
            if (! cn.isDefault()) {
                for (ExprNode ex : cn.values()) {
                    IntegerLiteralNode ival = (IntegerLiteralNode)ex;
                    mov(imm(ival.value()), reg("cx"));
                    cmp(t, reg("cx", t), reg("ax", t));
                    je(cn.beginLabel());
                }
            }
            else {
                jmp(cn.beginLabel());
            }
        }
        jmp(node.endLabel());
        for (CaseNode n : node.cases()) {
            compile(n);
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
        AsmOperand right = null;
        if (!doesRequireRegister(node.operator()) && node.right().isConstant()){
            compile(node.left());
            right = node.right().asmValue();
        }
        else if (node.right().isConstantAddress()) {
            compile(node.left());
            loadVariable(node.right(), reg("cx"));
            right = reg("cx", node.type());
        }
        else {
            compile(node.right());
            push(reg("ax"));
            compile(node.left());
            pop(reg("cx"));
            right = reg("cx", node.type());
        }
        compileBinaryOp(node.operator(), node.type(), right);
    }

    protected boolean doesRequireRegister(String op) {
        return op.equals("/")
                || op.equals("%")
                || op.equals(">>")
                || op.equals("<<");
    }

    protected boolean doesSpillDX(String op) {
        return op.equals("/") || op.equals("%");
    }

    // spills: dx
    protected void compileBinaryOp(String op, Type t, AsmOperand right) {
        if (op.equals("+")) {
            add(t, right, reg("ax", t));
        }
        else if (op.equals("-")) {
            sub(t, right, reg("ax", t));
        }
        else if (op.equals("*")) {
            imul(t, right, reg("ax", t));
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
            and(t, right, reg("ax", t));
        }
        else if (op.equals("|")) {
            or(t, right, reg("ax", t));
        }
        else if (op.equals("^")) {
            xor(t, right, reg("ax", t));
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
            cmp(t, right, reg("ax", t));
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
            load(node.expr().type(), node.expr().memref(), reg("ax"));
            compileUnaryArithmetic(node, reg("ax"));
            save(node.expr().type(), reg("ax"), node.expr().memref());
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
            load(node.expr().type(), node.expr().memref(), reg("ax"));
            mov(reg("ax"), reg("cx"));
            compileUnaryArithmetic(node, reg("cx"));
            save(node.expr().type(), reg("cx"), node.expr().memref());
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
                                          AsmOperand dest) {
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
        // We need not execute downcast because we can cast big value
        // to small value by just cutting off higer bits.
        if (node.isEffectiveCast()) {
            Type src = node.expr().type();
            Type dest = node.type();
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
        loadVariable(node, reg("ax"));
    }

    public void visit(IntegerLiteralNode node) {
        loadConstant(node, reg("ax"));
    }

    public void visit(StringLiteralNode node) {
        loadConstant(node, reg("ax"));
    }

    //
    // Assignable expressions
    //

    public void visit(AssignNode node) {
        if (node.lhs().isConstantAddress() && node.lhs().memref() != null) {
            compile(node.rhs());
            save(node.type(), reg("ax"), node.lhs().memref());
        }
        else if (node.rhs().isConstant()) {
            compileLHS(node.lhs());
            mov(reg("ax"), reg("cx"));
            loadConstant(node.rhs(), reg("ax"));
            save(node.type(), reg("ax"), mem(reg("cx")));
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
        if (node.lhs().isConstantAddress() && node.lhs().memref() != null) {
            // const += ANY
            compile(node.rhs());
            mov(reg("ax"), reg("cx"));
            load(node.type(), node.lhs().memref(), reg("ax"));
            compileBinaryOp(node.operator(), node.type(), reg("cx"));
            save(node.type(), reg("ax"), node.lhs().memref());
        }
        else if (node.rhs().isConstant() && !doesRequireRegister(node.operator())) {
            // ANY += const
            compileLHS(node.lhs());
            mov(reg("ax"), reg("cx"));
            load(node.type(), mem(reg("cx")), reg("ax"));
            AsmOperand rhs = node.rhs().asmValue();
            compileBinaryOp(node.operator(), node.type(), rhs);
            save(node.type(), reg("ax"), mem(reg("cx")));
        }
        else if (node.rhs().isConstantAddress()) {
            // ANY += var
            compileLHS(node.lhs());
            push(reg("ax"));
            load(node.type(), mem(reg("ax")), reg("ax"));
            loadVariable(node.rhs(), reg("cx"));
            compileBinaryOp(node.operator(), node.type(), reg("cx"));
            pop(reg("cx"));
            save(node.type(), reg("ax"), mem(reg("cx")));
        }
        else {
            // ANY += ANY
            // no optimization
            compile(node.rhs());
            push(reg("ax"));
            compileLHS(node.lhs());
            Register lhs = doesSpillDX(node.operator()) ? reg("si") : reg("dx");
            mov(reg("ax"), lhs);
            load(node.type(), mem(lhs), reg("ax"));
            pop(reg("cx"));
            compileBinaryOp(node.operator(), node.type(), reg("cx"));
            save(node.type(), reg("ax"), mem(lhs));
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
        if (options.isVerboseAsm()) {
            comment("compileLHS: " + node.getClass().getSimpleName() + " {");
            as.indentComment();
        }
        node.acceptLHS(this);
        if (options.isVerboseAsm()) {
            as.unindentComment();
            comment("compileLHS: }");
        }
    }

    public void visitLHS(VariableNode node) {
        loadVariableAddress(node, reg("ax"));
    }

    public void visitLHS(ArefNode node) {
        compileArrayIndex(node);
        imul(imm(node.elementSize()), reg("ax"));
        push(reg("ax"));
        if (node.baseExpr().type().isPointerAlike()) {
            compile(node.baseExpr());
        }
        else {
            compileLHS(node.baseExpr());
        }
        pop(reg("cx"));
        add(reg("cx"), reg("ax"));
    }

    protected void compileArrayIndex(ArefNode node) {
        compile(node.index());
        if (node.isMultiDimension()) {
            push(reg("ax"));
            compileArrayIndex((ArefNode)node.expr());
            imul(imm(node.length()), reg("ax"));
            pop(reg("cx"));
            add(reg("cx"), reg("ax"));
        }
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

    //
    // Utilities
    //

    /**
     * Loads constant value.  You must check node by #isConstant
     * before calling this method.
     */
    protected void loadConstant(ExprNode node, Register reg) {
        if (node.asmValue() != null) {
            mov(node.asmValue(), reg);
        }
        else if (node.memref() != null) {
            lea(node.memref(), reg);
        }
        else {
            throw new Error("must not happen: constant has no asm value");
        }
    }

    /**
     * Loads variable value to the register.  You must check node
     * by #isConstantAddress before calling this method.
     */
    protected void loadVariable(ExprNode node, Register dest) {
        if (node.shouldEvaluatedToAddress()) {
            // "int[4] a; a" implies &a
            // "x = puts" implies &puts
            loadVariableAddress(node, dest);
        }
        else if (node.memref() == null) {
            mov(node.address(), dest);
            load(node.type(), mem(dest), dest);
        }
        else {
            // regular variable
            load(node.type(), node.memref(), dest);
        }
    }

    /**
     * Loads an address of the variable to the register.
     * You must check node by #isConstantAddress before
     * calling this method.
     */
    protected void loadVariableAddress(ExprNode node, Register dest) {
        if (node.address() != null) {
            mov(node.address(), dest);
        }
        else {
            lea(node.memref(), dest);
        }
    }

    //
    // x86 assembly DSL
    //

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

    protected DirectMemoryReference mem(Symbol sym) {
        return new DirectMemoryReference(sym);
    }

    protected IndirectMemoryReference mem(Register reg) {
        return new IndirectMemoryReference(0, reg);
    }

    protected IndirectMemoryReference mem(long offset, Register reg) {
        return new IndirectMemoryReference(offset, reg);
    }

    protected IndirectMemoryReference mem(Symbol offset, Register reg) {
        return new IndirectMemoryReference(offset, reg);
    }

    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected ImmediateValue imm(Symbol sym) {
        return new ImmediateValue(sym);
    }

    protected ImmediateValue imm(Literal lit) {
        return new ImmediateValue(lit);
    }

    protected void load(Type type, MemoryReference mem, Register reg) {
        switch ((int)type.size()) {
        case 1:
            if (type.isSigned()) {  // signed char
                movsbl(mem, reg);
            } else {                // unsigned char
                movzbl(mem, reg);
            }
            break;
        case 2:
            if (type.isSigned()) {  // signed short
                movswl(mem, reg);
            } else {                // unsigned short
                movzwl(mem, reg);
            }
            break;
        default:                    // int, long, long_long
            mov(type, mem, reg.forType(type));
            break;
        }
    }

    protected void save(Type type, Register reg, MemoryReference mem) {
        mov(type, reg.forType(type), mem);
    }

    public void comment(String str) { as.comment(str); }
    public void _file(String name) { as._file(name); }
    public void _text() { as._text(); }
    public void _data() { as._data(); }
    public void _section(String name) { as._section(name); }
    public void _section(String name, String flags, String type, String group, String linkage) { as._section(name, flags, type, group, linkage); }
    public void _globl(Symbol sym) { as._globl(sym); }
    public void _local(Symbol sym) { as._local(sym); }
    public void _hidden(Symbol sym) { as._hidden(sym); }
    public void _comm(Symbol sym, long sz, long a) { as._comm(sym, sz, a); }
    public void _align(long n) { as._align(n); }
    public void _type(Symbol sym, String type) { as._type(sym, type); }
    public void _size(Symbol sym, long size) { as._size(sym, size); }
    public void _size(Symbol sym, String size) { as._size(sym, size); }
    public void _byte(long n) { as._byte(new IntegerLiteral(n)); }
    public void _value(long n) { as._value(new IntegerLiteral(n)); }
    public void _long(long n) { as._long(new IntegerLiteral(n)); }
    public void _long(Symbol sym) { as._long(sym); }
    public void _quad(long n) { as._quad(new IntegerLiteral(n)); }
    public void _quad(Symbol sym) { as._quad(sym); }
    public void _string(String str) { as._string(str); }
    public void label(Symbol sym) { as.label(sym); }
    public void label(Label label) { as.label(label); }

    public void jmp(Label label) { as.jmp(label); }
    public void jz(Label label) { as.jz(label); }
    public void jnz(Label label) { as.jnz(label); }
    public void je(Label label) { as.je(label); }
    public void jne(Label label) { as.jne(label); }
    public void cmp(Type t, AsmOperand a, Register b) { as.cmp(t, a, b); }
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
    public void call(Symbol sym) { as.call(sym); }
    public void callAbsolute(Register reg) { as.callAbsolute(reg); }
    public void ret() { as.ret(); }
    public void mov(AsmOperand src, AsmOperand dest) { as.mov(src, dest); }
    public void mov(Type type, AsmOperand src, AsmOperand dest) { as.mov(type, src, dest); }
    public void movsx(Type from, Type to, AsmOperand src, AsmOperand dest) { as.movsx(from, to, src, dest); }
    public void movzx(Type from, Type to, AsmOperand src, AsmOperand dest) { as.movzx(from, to, src, dest); }
    public void movsbl(AsmOperand src, AsmOperand dest) { as.movsbl(src, dest); }
    public void movswl(AsmOperand src, AsmOperand dest) { as.movswl(src, dest); }
    public void movzb(Type type, AsmOperand src, AsmOperand dest) { as.movzb(type, src, dest); }
    public void movzbl(AsmOperand src, AsmOperand dest) { as.movzbl(src, dest); }
    public void movzwl(AsmOperand src, AsmOperand dest) { as.movzwl(src, dest); }
    public void lea(AsmOperand src, AsmOperand dest) { as.lea(src, dest); }
    public void lea(Type type, AsmOperand src, AsmOperand dest) { as.lea(type, src, dest); }
    public void neg(Type type, Register reg) { as.neg(type, reg); }
    public void inc(Type type, AsmOperand reg) { as.inc(type, reg); }
    public void dec(Type type, AsmOperand reg) { as.dec(type, reg); }
    public void add(AsmOperand diff, AsmOperand base) { as.add(diff, base); }
    public void add(Type type, AsmOperand diff, AsmOperand base) { as.add(type, diff, base); }
    public void sub(AsmOperand diff, AsmOperand base) { as.sub(diff, base); }
    public void sub(Type type, AsmOperand diff, AsmOperand base) { as.sub(type, diff, base); }
    public void imul(AsmOperand m, Register base) { as.imul(m, base); }
    public void imul(Type type, AsmOperand m, Register base) { as.imul(type, m, base); }
    public void cltd() { as.cltd(); }
    public void div(Type type, Register base) { as.div(type, base); }
    public void idiv(Type type, Register base) { as.idiv(type, base); }
    public void not(Type type, Register reg) { as.not(type, reg); }
    public void and(Type type, AsmOperand bits, Register base) { as.and(type, bits, base); }
    public void or(Type type, AsmOperand bits, Register base) { as.or(type, bits, base); }
    public void xor(Type type, AsmOperand bits, Register base) { as.xor(type, bits, base); }
    public void sar(Type type, Register n, Register base) { as.sar(type, n, base); }
    public void sal(Type type, Register n, Register base) { as.sal(type, n, base); }
    public void shr(Type type, Register n, Register base) { as.shr(type, n, base); }
}
