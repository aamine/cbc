package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator implements ASTVisitor<Void,Void>, ELFConstants {
    // #@@range/ctor{
    protected CodeGeneratorOptions options;
    protected ErrorHandler errorHandler;
    protected LinkedList<Assembler> asStack;
    protected Assembler as;
    protected TypeTable typeTable;
    protected Label epilogue;

    public CodeGenerator(CodeGeneratorOptions options,
                         ErrorHandler errorHandler) {
        this.options = options;
        this.errorHandler = errorHandler;
        this.asStack = new LinkedList<Assembler>();
    }
    // #@@}

    /** Compiles IR and generates assembly code. */
    // #@@range/generate{
    public String generate(IR ir) {
        this.typeTable = ir.typeTable();
        pushAssembler();
        SymbolTable constSymbols = new SymbolTable(Assembler.CONST_SYMBOL_BASE);
        for (ConstantEntry ent : ir.constantTable().entries()) {
            locateConstant(ent, constSymbols);
        }
        for (Variable var : ir.allGlobalVariables()) {
            locateGlobalVariable(var);
        }
        for (Function func : ir.allFunctions()) {
            locateFunction(func);
        }
        compileIR(ir);
        return popAssembler().toSource();
    }
    // #@@}

    // #@@range/pushAssembler{
    protected void pushAssembler() {
        this.as = newAssembler();
        asStack.add(this.as);
    }
    // #@@}

    // #@@range/popAssembler{
    protected Assembler popAssembler() {
        Assembler popped = asStack.removeLast();
        this.as = asStack.isEmpty() ? null : asStack.getLast();
        return popped;
    }
    // #@@}

    // #@@range/newAssembler{
    protected Assembler newAssembler() {
        return new Assembler(typeTable.unsignedLong());
    }
    // #@@}

    // #@@range/compileIR{
    public void compileIR(IR ir) {
        _file(ir.fileName());
        // .data
        List<DefinedVariable> gvars = ir.definedGlobalVariables();
        if (!gvars.isEmpty()) {
            _data();
            for (DefinedVariable gvar : gvars) {
                dataEntry(gvar);
            }
        }
        if (!ir.constantTable().isEmpty()) {
            _section(".rodata");
            for (ConstantEntry ent : ir.constantTable()) {
                compileStringLiteral(ent);
            }
        }
        // .text
        if (ir.functionDefined()) {
            _text();
            for (DefinedFunction func : ir.definedFunctions()) {
                visit(func);
            }
        }
        // .bss
        for (DefinedVariable var : ir.definedCommonSymbols()) {
            compileCommonSymbol(var);
        }
        // others
        if (options.isPositionIndependent()) {
            PICThunk(GOTBaseReg());
        }
    }
    // #@@}

    // #@@range/locateConstant{
    protected void locateConstant(ConstantEntry ent, SymbolTable symbols) {
        ent.setSymbol(symbols.newSymbol());
        if (options.isPositionIndependent()) {
            Symbol offset = localGOTSymbol(ent.symbol());
            ent.setMemref(mem(offset, GOTBaseReg()));
        }
        else {
            ent.setMemref(mem(ent.symbol()));
            ent.setAddress(imm(ent.symbol()));
        }
    }
    // #@@}

    // #@@range/locateGlobalVariable{
    protected void locateGlobalVariable(Entity ent) {
        Symbol sym = ent.isPrivate() ? privateSymbol(ent.symbolString())
                                     : globalSymbol(ent.symbolString());
        if (options.isPositionIndependent()) {
            if (ent.isPrivate() || optimizeGvarAccess(ent)) {
                ent.setMemref(mem(localGOTSymbol(sym), GOTBaseReg()));
            }
            else {
                ent.setAddress(mem(globalGOTSymbol(sym), GOTBaseReg()));
            }
        }
        else {
            ent.setMemref(mem(sym));
        }
    }
    // #@@}

    // #@@range/locateFunction{
    protected void locateFunction(Function func) {
        func.setCallingSymbol(callingSymbol(func));
        locateGlobalVariable(func);
    }
    // #@@}

    // #@@range/callingSymbol{
    protected Symbol callingSymbol(Function func) {
        if (func.isPrivate()) {
            return privateSymbol(func.symbolString());
        }
        else {
            Symbol sym = globalSymbol(func.symbolString());
            return doesIndirectAccess(func) ? PLTSymbol(sym) : sym;
        }
    }
    // #@@}

    // condition to use indirect access (using PLT to call, GOT to refer).
    // In PIC, we do use indirect access for all global variables.
    // In PIE, we do use direct access for file-local reference.
    // #@@range/doesIndirectAccess{
    protected boolean doesIndirectAccess(Entity ent) {
        return options.isPositionIndependent() && !optimizeGvarAccess(ent);
    }
    // #@@}

    // #@@range/optimizeGvarAccess{
    protected boolean optimizeGvarAccess(Entity ent) {
        return options.isPIERequired() && ent.isDefined();
    }
    // #@@}

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
    // PIC/PIE related constants and codes
    //

    // #@@range/pic_methods{
    static protected final Symbol GOT =
            new NamedSymbol("_GLOBAL_OFFSET_TABLE_");

    protected void loadGOTBaseAddress(Register reg) {
        call(PICThunkSymbol(reg));
        add(imm(GOT), reg);
    }

    protected Register GOTBaseReg() {
        return reg("bx");
    }
    // #@@}

    // #@@range/pic_symbols{
    protected Symbol globalGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOT");
    }

    protected Symbol localGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOTOFF");
    }

    protected Symbol PLTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@PLT");
    }
    // #@@}

    // #@@range/pic_thunk_helper{
    protected Symbol PICThunkSymbol(Register reg) {
        return new NamedSymbol("__i686.get_pc_thunk." + reg.baseName());
    }

    static protected final String
    PICThunkSectionFlags = SectionFlag_allocatable
                         + SectionFlag_executable
                         + SectionFlag_sectiongroup;
    // #@@}

    /**
     * Output PIC thunk.
     * ELF section declaration format is:
     *
     *     .section NAME, FLAGS, TYPE, flag_arguments
     *
     * FLAGS, TYPE, flag_arguments are optional.
     * For "M" flag (a member of a section group),
     * following format is used:
     *
     *     .section NAME, "...M", TYPE, section_group_name, linkage
     */
    // #@@range/PICThunk{
    protected void PICThunk(Register reg) {
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
    // #@@}

    //
    // Compile Function
    //

    /* Standard IA-32 stack frame layout
     *
     * ======================= esp #3 (stack top just before function call)
     * next arg 1
     * ---------------------
     * next arg 2
     * ---------------------
     * next arg 3
     * ---------------------   esp #2 (stack top after alloca call)
     * alloca area
     * ---------------------   esp #1 (stack top just after prelude)
     * temporary
     * variables...
     * ---------------------   -16(%ebp)
     * lvar 3
     * ---------------------   -12(%ebp)
     * lvar 2
     * ---------------------   -8(%ebp)
     * lvar 1
     * ---------------------   -4(%ebp)
     * callee-saved register
     * ======================= 0(%ebp)
     * saved ebp
     * ---------------------   4(%ebp)
     * return address
     * ---------------------   8(%ebp)
     * arg 1
     * ---------------------   12(%ebp)
     * arg 2
     * ---------------------   16(%ebp)
     * arg 3
     * ...
     * ...
     * ======================= stack bottom
     */

    /*
     * Platform Dependent Stack Parameters
     */
    // #@@range/stackParams{
    static final protected boolean stackGrowsLower = true;
    static final protected long stackWordSize = 4;
    static final protected long stackAlignment = stackWordSize;
    static final protected long paramStartWord = 2;
                                    // return addr and saved bp
    // #@@}

    /** Compiles a function. */
    // #@@range/compileFunction{
    public Void visit(DefinedFunction func) {
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
        return null;
    }
    // #@@}

    // #@@range/compileFunctionBody{
    protected void compileFunctionBody(DefinedFunction func) {
        initVirtualStack();
        List<Assembly> bodyAsms = compileStmts(func);
        long maxTmpBytes = maxTmpBytes();
        AsmStatistics stats = AsmStatistics.collect(bodyAsms);
        bodyAsms = reduceLabels(bodyAsms, stats);
        List<Register> saveRegs = usedCalleeSavedRegistersWithoutBP(stats);
        long saveRegsBytes = saveRegs.size() * stackWordSize;
        long lvarBytes = allocateLocalVariables(
                func.body().scope(), saveRegsBytes);
        fixTmpOffsets(bodyAsms, saveRegsBytes + lvarBytes);

        if (options.isVerboseAsm()) {
            printStackFrameLayout(
                    saveRegsBytes, lvarBytes, maxTmpBytes,
                    func.localVariables());
        }

        initVirtualStack();
        prologue(func, saveRegs, saveRegsBytes + lvarBytes + maxTmpBytes);
        if (options.isPositionIndependent()
                && stats.doesRegisterUsed(GOTBaseReg())) {
            loadGOTBaseAddress(GOTBaseReg());
        }
        as.addAll(bodyAsms);
        epilogue(func, saveRegs, lvarBytes);
    }
    // #@@}

    protected void printStackFrameLayout(
            long saveRegsBytes, long lvarBytes, long maxTmpBytes,
            List<DefinedVariable> lvars) {
        List<MemInfo> vars = new ArrayList<MemInfo>();
        for (DefinedVariable var : lvars) {
            vars.add(new MemInfo(var.memref(), var.name()));
        }
        vars.add(new MemInfo(mem(0, bp()), "return address"));
        vars.add(new MemInfo(mem(4, bp()), "saved %ebp"));
        if (saveRegsBytes > 0) {
            vars.add(new MemInfo(mem(-saveRegsBytes, bp()),
                "saved callee-saved registers (" + saveRegsBytes + " bytes)"));
        }
        if (maxTmpBytes > 0) {
            long offset = -(saveRegsBytes + lvarBytes + maxTmpBytes);
            vars.add(new MemInfo(mem(offset, bp()),
                "tmp variables (" + maxTmpBytes + " bytes)"));
        }
        Collections.sort(vars, new Comparator<MemInfo>() {
            public int compare(MemInfo x, MemInfo y) {
                return x.mem.compareTo(y.mem);
            }
        });
        comment("---- Stack Frame Layout -----------");
        for (MemInfo info : vars) {
            comment(info.mem.toString() + ": " + info.name);
        }
        comment("-----------------------------------");
    }

    class MemInfo {
        MemoryReference mem;
        String name;

        MemInfo(MemoryReference mem, String name) {
            this.mem = mem;
            this.name = name;
        }
    }

    // #@@range/compileStmts{
    protected List<Assembly> compileStmts(DefinedFunction func) {
        pushAssembler();
        epilogue = new Label();
        for (StmtNode s : func.ir()) {
            compileStmt(s);
        }
        label(epilogue);
        return options.optimizer().optimize(popAssembler().assemblies());
    }
    // #@@}

    // #@@range/reduceLabels{
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
    // #@@}

    protected List<Register> usedCalleeSavedRegistersWithoutBP(AsmStatistics stats) {
        List<Register> result = new ArrayList<Register>();
        for (Register reg : calleeSavedRegisters()) {
            if (stats.doesRegisterUsed(reg) && !reg.equals(bp())) {
                result.add(reg);
            }
        }
        return result;
    }

    protected List<Register> calleeSavedRegistersCache = null;

    // platform dependent
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

    // #@@range/prologue{
    protected void prologue(DefinedFunction func,
                            List<Register> saveRegs,
                            long frameSize) {
        truePush(bp());
        mov(sp(), bp());
        saveRegisters(saveRegs);
        extendStack(frameSize);
    }
    // #@@}

    // #@@range/epilogue{
    protected void epilogue(DefinedFunction func,
                            List<Register> savedRegs,
                            long lvarBytes) {
        restoreRegisters(savedRegs);
        mov(bp(), sp());
        truePop(bp());
        ret();
    }
    // #@@}

    // #@@range/saveRegisters{
    protected void saveRegisters(List<Register> saveRegs) {
        for (Register reg : saveRegs) {
            push(reg);
        }
    }
    // #@@}

    // #@@range/restoreRegisters{
    protected void restoreRegisters(List<Register> savedRegs) {
        ListIterator<Register> regs = savedRegs.listIterator(savedRegs.size());
        while (regs.hasPrevious()) {
            pop(regs.previous());
        }
    }
    // #@@}

    // #@@range/allocateParameters{
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
    // #@@}

    /**
     * Allocates addresses of local variables, but offset is still
     * not determined, assign unfixed IndirectMemoryReference.
     */
    // #@@range/allocateVariablesTemp{
    protected void allocateLocalVariablesTemp(LocalScope scope) {
        for (DefinedVariable var : scope.allLocalVariables()) {
            var.setMemref(new IndirectMemoryReference(bp()));
        }
    }
    // #@@}

    /**
     * Fixes addresses of local variables.
     * Returns byte-length of the local variable area.
     * Note that numSavedRegs includes bp.
     */
    // #@@range/allocateVariables{
    protected long allocateLocalVariables(LocalScope scope, long initLen) {
        long maxLen = allocateScope(scope, initLen);
        return maxLen - initLen;
    }
    // #@@}

    // #@@range/allocateScope{
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
    // #@@}

    // #@@range/fixMemref{
    protected void fixMemref(IndirectMemoryReference memref, long offset) {
        memref.fixOffset(offset);
    }
    // #@@}

    // #@@range/extendStack{
    protected void extendStack(long len) {
        if (len > 0) {
            if (stackGrowsLower) {
                sub(imm(len), sp());
            }
            else {
                add(imm(len), sp());
            }
        }
    }
    // #@@}

    // #@@range/rewindStack{
    protected void rewindStack(long len) {
        if (len > 0) {
            if (stackGrowsLower) {
                add(imm(len), sp());
            }
            else {
                sub(imm(len), sp());
            }
        }
    }
    // #@@}

    // #@@range/virtual_stack{
    protected long stackPointer;
    protected long stackPointerMax;

    protected void initVirtualStack() {
        stackPointer = 0;
        stackPointerMax = stackPointer;
    }
    // #@@}

    // #@@range/maxTmpBytes{
    protected long maxTmpBytes() {
        return stackPointerMax;
    }
    // #@@}

    // #@@range/stackTop{
    protected IndirectMemoryReference stackTop() {
        if (stackGrowsLower) {
            return mem(-stackPointer, bp());
        }
        else {
            return mem(stackPointer - stackWordSize, bp());
        }
    }
    // #@@}

    // #@@range/push{
    protected void push(Register reg) {
        extendVirtualStack(stackWordSize);
        as.relocatableMov(reg, stackTop());
        if (options.isVerboseAsm()) {
            comment("push " + reg.name() + " -> " + stackTop());
        }
    }
    // #@@}

    // #@@range/pop{
    protected void pop(Register reg) {
        if (options.isVerboseAsm()) {
            comment("pop  " + reg.name() + " <- " + stackTop());
        }
        as.relocatableMov(stackTop(), reg);
        rewindVirtualStack(stackWordSize);
    }
    // #@@}

    // #@@range/extendVirtualStack{
    protected void extendVirtualStack(long len) {
        stackPointer += len;
        stackPointerMax = Math.max(stackPointerMax, stackPointer);
    }
    // #@@}

    // #@@range/rewindVirtualStack{
    protected void rewindVirtualStack(long len) {
        stackPointer -= len;
    }
    // #@@}

    // #@@range/fixTmpOffsets{
    protected void fixTmpOffsets(List<Assembly> asms, long offset) {
        for (Assembly asm : asms) {
            asm.fixStackOffset(offset * (stackGrowsLower ? -1 : 1));
        }
    }
    // #@@}

    /**
     * Implements cdecl function call:
     *    * All arguments are on stack.
     *    * Rewind stack by caller.
     */
    // #@@range/compile_Funcall{
    public Void visit(FuncallNode node) {
        // compile function arguments from right to left.
        ListIterator<ExprNode> args = node.finalArg();
        while (args.hasPrevious()) {
            compile(args.previous());
            truePush(reg("ax"));
        }
        // call
        if (node.isStaticCall()) {
            // call via function name
            call(node.function().callingSymbol());
        }
        else {
            // call via pointer
            compile(node.expr());
            callAbsolute(reg("ax"));
        }
        // rewind stack
        // >4 bytes arguments are not supported.
        rewindStack(node.numArgs() * stackWordSize);
        return null;
    }
    // #@@}

    // #@@range/compile_Return{
    public Void visit(ReturnNode node) {
        if (node.expr() != null) {
            compile(node.expr());
        }
        jmp(epilogue);
        return null;
    }
    // #@@}

    //
    // Statements
    //

    // #@@range/compileStmt{
    protected void compileStmt(StmtNode node) {
        if (options.isVerboseAsm()) {
            comment(node.location().numberedLine());
        }
        node.accept(this);
    }
    // #@@}

    public Void visit(ExprStmtNode node) {
        compile(node.expr());
        return null;
    }

    // #@@range/testCond{
    private void testCond(Type t, Register reg) {
        test(t, reg.forType(t), reg.forType(t));
    }
    // #@@}

    // #@@range/compile_BranchIf{
    public Void visit(BranchIfNode node) {
        compile(node.cond());
        testCond(node.cond().type(), reg("ax"));
        jnz(node.thenLabel());
        jmp(node.elseLabel());
        return null;
    }
    // #@@}

    // #@@range/compile_Switch{
    public Void visit(SwitchNode node) {
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
        return null;
    }
    // #@@}

    // #@@range/compile_Label{
    public Void visit(LabelNode node) {
        label(node.label());
        return null;
    }
    // #@@}

    // #@@range/compile_Goto{
    public Void visit(GotoNode node) {
        jmp(node.label());
        return null;
    }
    // #@@}

    public Void visit(BlockNode node) { throw new Error("BlockNode"); }
    public Void visit(IfNode node) { throw new Error("IfNode"); }
    public Void visit(CondExprNode node) { throw new Error("CondExprNode"); }
    public Void visit(CaseNode node) { throw new Error("CaseNode"); }
    public Void visit(LogicalAndNode node) { throw new Error("LogicalAndNode");}
    public Void visit(LogicalOrNode node) { throw new Error("LogicalOrNode"); }
    public Void visit(WhileNode node) { throw new Error("WhileNode"); }
    public Void visit(DoWhileNode node) { throw new Error("DoWhileNode"); }
    public Void visit(ForNode node) { throw new Error("ForNode"); }
    public Void visit(BreakNode node) { throw new Error("BreakNode"); }
    public Void visit(ContinueNode node) { throw new Error("ContinueNode"); }
    public Void visit(UndefinedFunction f) { throw new Error("UndefinedFunction"); }
    public Void visit(DefinedVariable v) { throw new Error("DefinedVariable"); }
    public Void visit(UndefinedVariable v){throw new Error("UndefinedVariable");}
    public Void visit(StructNode node) { throw new Error("StructNode"); }
    public Void visit(UnionNode node) { throw new Error("UnionNode"); }
    public Void visit(TypedefNode node) { throw new Error("TypedefNode"); }

    //
    // Expressions
    //

    // #@@range/compile{
    protected void compile(ExprNode n) {
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

    // #@@range/compile_BinaryOp{
    public Void visit(BinaryOpNode node) {
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
        return null;
    }
    // #@@}

    // #@@range/doesRequireRegister{
    protected boolean doesRequireRegister(String op) {
        return op.equals("/")
                || op.equals("%")
                || op.equals(">>")
                || op.equals("<<");
    }
    // #@@}

    // #@@range/doesSpillDX{
    protected boolean doesSpillDX(String op) {
        return op.equals("/") || op.equals("%");
    }
    // #@@}

    // spills: dx
    // #@@range/compileBinaryOp_begin{
    protected void compileBinaryOp(String op, Type t, AsmOperand right) {
        // #@@range/compileBinaryOp_arithops{
        if (op.equals("+")) {
            add(t, right, reg("ax", t));
        }
        else if (op.equals("-")) {
            sub(t, right, reg("ax", t));
        }
    // #@@range/compileBinaryOp_begin}
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
        // #@@}
        // #@@range/compileBinaryOp_bitops{
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
        // #@@}
        // #@@range/compileBinaryOp_cmpops{
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
        // #@@}
    // #@@range/compileBinaryOp_end{
    }
    // #@@}

    // #@@range/compile_UnaryOp{
    public Void visit(UnaryOpNode node) {
        compile(node.expr());
        if (node.operator().equals("+")) {
            throw new Error("unary +");
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
        return null;
    }
    // #@@}

    // spills: (none)
    // #@@range/compile_UnaryArithmetic{
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
    // #@@}

    // #@@range/compile_Cast{
    public Void visit(CastNode node) {
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
        return null;
    }
    // #@@}

    // #@@range/compile_Variable{
    public Void visit(VariableNode node) {
        loadVariable(node, reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_IntegerLiteral{
    public Void visit(IntegerLiteralNode node) {
        loadConstant(node, reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_StringLiteral{
    public Void visit(StringLiteralNode node) {
        loadConstant(node, reg("ax"));
        return null;
    }
    // #@@}

    public Void visit(PrefixOpNode node) { throw new Error("PrefixOpNode"); }
    public Void visit(SuffixOpNode node) { throw new Error("SuffixOpNode"); }
    public Void visit(ArefNode node) { throw new Error("ArefNode"); }
    public Void visit(MemberNode node) { throw new Error("MemberNode"); }
    public Void visit(PtrMemberNode node) { throw new Error("PtrMemberNode"); }
    public Void visit(SizeofExprNode node) { throw new Error("SizeofExprNode");}
    public Void visit(SizeofTypeNode node) { throw new Error("SizeofTypeNode");}
    public Void visit(AssignNode node) { throw new Error("AssignNode"); }
    public Void visit(OpAssignNode node) { throw new Error("OpAssignNode"); }

    //
    // Assignable expressions
    //

    private void compileLHS(ExprNode lhs) {
        // FIXME FIXME FIXME
        // for variables: apply loadVariableAddress
        // for *expr: remove DereferenceNode
        // otherwise: fatal error
        compile(lhs);
    }

    // #@@range/compile_Assign{
    public Void visit(AssignStmtNode node) {
        if (node.lhs().isConstantAddress() && node.lhs().memref() != null) {
            compile(node.rhs());
            save(node.lhs().type(), reg("ax"), node.lhs().memref());
        }
        else if (node.rhs().isConstant()) {
            compileLHS(node.lhs());
            mov(reg("ax"), reg("cx"));
            loadConstant(node.rhs(), reg("ax"));
            save(node.lhs().type(), reg("ax"), mem(reg("cx")));
        }
        else {
            compile(node.rhs());
            push(reg("ax"));
            compileLHS(node.lhs());
            mov(reg("ax"), reg("cx"));
            pop(reg("ax"));
            save(node.lhs().type(), reg("ax"), mem(reg("cx")));
        }
        return null;
    }
    // #@@}

    // #@@range/compile_Dereference{
    public Void visit(DereferenceNode node) {
        compile(node.expr());
        load(node.type(), mem(reg("ax")), reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_Address{
    public Void visit(AddressNode node) {
        compileLHS(node.expr());
        return null;
    }
    // #@@}

    //
    // Utilities
    //

    /**
     * Loads constant value.  You must check node by #isConstant
     * before calling this method.
     */
    // #@@range/loadConstant{
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
    // #@@}

    /**
     * Loads variable value to the register.  You must check node
     * by #isConstantAddress before calling this method.
     */
    // #@@range/loadVariable{
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
    // #@@}

    /**
     * Loads an address of the variable to the register.
     * You must check node by #isConstantAddress before
     * calling this method.
     */
    // #@@range/loadVariableAddress{
    protected void loadVariableAddress(ExprNode node, Register dest) {
        if (node.address() != null) {
            mov(node.address(), dest);
        }
        else {
            lea(node.memref(), dest);
        }
    }
    // #@@}

    //
    // x86 assembly DSL
    //

    // #@@range/dsl_regs{
    protected Register bp() { return reg("bp"); }
    protected Register sp() { return reg("sp"); }
    protected Register al() { return new Register(1, "ax"); }
    protected Register cl() { return new Register(1, "cx"); }
    // #@@}

    // #@@range/reg{
    protected Register reg(String name, Type type) {
        return new Register(name).forType(type);
    }

    protected Register reg(String name) {
        return new Register(name);
    }
    // #@@}

    // #@@range/mem{
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
    // #@@}

    // #@@range/imm{
    protected ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    protected ImmediateValue imm(Symbol sym) {
        return new ImmediateValue(sym);
    }

    protected ImmediateValue imm(Literal lit) {
        return new ImmediateValue(lit);
    }
    // #@@}

    // #@@range/load{
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
    // #@@}

    // #@@range/save{
    protected void save(Type type, Register reg, MemoryReference mem) {
        mov(type, reg.forType(type), mem);
    }
    // #@@}

    // #@@range/dsl_directives{
    public void comment(String str) { as.comment(str); }
    public void _file(String name) { as._file(name); }
    public void _text() { as._text(); }
    public void _data() { as._data(); }
    public void _section(String name) { as._section(name); }
    // #@@}
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
    protected void truePush(Register reg) { as.push(reg); }
    protected void truePop(Register reg) { as.pop(reg); }
    public void call(Symbol sym) { as.call(sym); }
    public void callAbsolute(Register reg) { as.callAbsolute(reg); }
    public void ret() { as.ret(); }
    // #@@range/dsl_ops2{
    public void mov(AsmOperand src, AsmOperand dest) { as.mov(src, dest); }
    public void mov(Type type, AsmOperand src, AsmOperand dest) { as.mov(type, src, dest); }
    public void movsx(Type from, Type to, AsmOperand src, AsmOperand dest) { as.movsx(from, to, src, dest); }
    public void movzx(Type from, Type to, AsmOperand src, AsmOperand dest) { as.movzx(from, to, src, dest); }
    // #@@}
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
    // #@@range/dsl_ops1{
    public void add(AsmOperand diff, AsmOperand base) { as.add(diff, base); }
    public void add(Type type, AsmOperand diff, AsmOperand base) { as.add(type, diff, base); }
    public void sub(AsmOperand diff, AsmOperand base) { as.sub(diff, base); }
    public void sub(Type type, AsmOperand diff, AsmOperand base) { as.sub(type, diff, base); }
    public void imul(AsmOperand m, Register base) { as.imul(m, base); }
    public void imul(Type type, AsmOperand m, Register base) { as.imul(type, m, base); }
    public void cltd() { as.cltd(); }
    public void div(Type type, Register base) { as.div(type, base); }
    public void idiv(Type type, Register base) { as.idiv(type, base); }
    // #@@}
    public void not(Type type, Register reg) { as.not(type, reg); }
    public void and(Type type, AsmOperand bits, Register base) { as.and(type, bits, base); }
    public void or(Type type, AsmOperand bits, Register base) { as.or(type, bits, base); }
    public void xor(Type type, AsmOperand bits, Register base) { as.xor(type, bits, base); }
    public void sar(Type type, Register n, Register base) { as.sar(type, n, base); }
    public void sal(Type type, Register n, Register base) { as.sal(type, n, base); }
    public void shr(Type type, Register n, Register base) { as.shr(type, n, base); }
}
