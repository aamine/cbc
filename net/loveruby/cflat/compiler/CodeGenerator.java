package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CodeGenerator implements IRVisitor<Void,Void>, ELFConstants {
    // #@@range/ctor{
    protected CodeGeneratorOptions options;
    protected ErrorHandler errorHandler;
    protected LinkedList<Assembler> asStack;
    protected Assembler as;
    protected Type naturalType;
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
        this.naturalType = ir.naturalType();
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
        this.as = new Assembler(naturalType);
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

    // #@@range/compileIR{
    public void compileIR(IR ir) {
        as._file(ir.fileName());
        // .data
        List<DefinedVariable> gvars = ir.definedGlobalVariables();
        if (!gvars.isEmpty()) {
            as._data();
            for (DefinedVariable gvar : gvars) {
                dataEntry(gvar);
            }
        }
        if (!ir.constantTable().isEmpty()) {
            as._section(".rodata");
            for (ConstantEntry ent : ir.constantTable()) {
                compileStringLiteral(ent);
            }
        }
        // .text
        if (ir.functionDefined()) {
            as._text();
            for (DefinedFunction func : ir.definedFunctions()) {
                compileFunction(func);
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
            as._globl(sym);
        }
        as._align(ent.alignment());
        as._type(sym, "@object");
        as._size(sym, ent.allocSize());
        as.label(sym);
        compileImmediate(ent.type().allocSize(), ent.ir());
    }
    // #@@}

    /** Generates immediate values for .data section */
    // #@@range/compileImmediates{
    protected void compileImmediate(long size, Expr node) {
        if (node instanceof IntValue) {
            IntValue expr = (IntValue)node;
            switch ((int)size) {
            case 1: as._byte(expr.value());    break;
            case 2: as._value(expr.value());   break;
            case 4: as._long(expr.value());    break;
            case 8: as._quad(expr.value());    break;
            default:
                throw new Error("entry size must be 1,2,4,8");
            }
        }
        else if (node instanceof StringValue) {
            StringValue expr = (StringValue)node;
            switch ((int)size) {
            case 4: as._long(expr.symbol());   break;
            case 8: as._quad(expr.symbol());   break;
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
            as._local(sym);
        }
        as._comm(sym, var.allocSize(), var.alignment());
    }
    // #@@}

    /** Generates .rodata entry (constant strings) */
    // #@@range/compileStringLiteral{
    protected void compileStringLiteral(ConstantEntry ent) {
        as.label(ent.symbol());
        as._string(ent.value());
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
        as.call(PICThunkSymbol(reg));
        as.add(imm(GOT), reg);
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
        as._section(".text" + "." + sym.toSource(),
                 "\"" + PICThunkSectionFlags + "\"",
                 SectionType_bits,      // This section contains data
                 sym.toSource(),        // The name of section group
                Linkage_linkonce);      // Only 1 copy should be generated
        as._globl(sym);
        as._hidden(sym);
        as._type(sym, SymbolType_function);
        as.label(sym);
        as.mov(mem(sp()), reg);    // fetch saved EIP to the GOT base register
        as.ret();
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
    public void compileFunction(DefinedFunction func) {
        allocateParameters(func);
        allocateLocalVariablesTemp(func.body().scope());

        Symbol sym = globalSymbol(func.name());
        if (! func.isPrivate()) {
            as._globl(sym);
        }
        as._type(sym, "@function");
        as.label(sym);
        compileFunctionBody(func);
        as._size(sym, ".-" + sym.toSource());
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
        as.comment("---- Stack Frame Layout -----------");
        for (MemInfo info : vars) {
            as.comment(info.mem.toString() + ": " + info.name);
        }
        as.comment("-----------------------------------");
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
        for (Stmt s : func.ir()) {
            compileStmt(s);
        }
        as.label(epilogue);
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
        as.push(bp());
        as.mov(sp(), bp());
        saveRegisters(saveRegs);
        extendStack(frameSize);
    }
    // #@@}

    // #@@range/epilogue{
    protected void epilogue(DefinedFunction func,
                            List<Register> savedRegs,
                            long lvarBytes) {
        restoreRegisters(savedRegs);
        as.mov(bp(), sp());
        as.pop(bp());
        as.ret();
    }
    // #@@}

    // #@@range/saveRegisters{
    protected void saveRegisters(List<Register> saveRegs) {
        for (Register reg : saveRegs) {
            virtualPush(reg);
        }
    }
    // #@@}

    // #@@range/restoreRegisters{
    protected void restoreRegisters(List<Register> savedRegs) {
        ListIterator<Register> regs = savedRegs.listIterator(savedRegs.size());
        while (regs.hasPrevious()) {
            virtualPop(regs.previous());
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
                as.sub(imm(len), sp());
            }
            else {
                as.add(imm(len), sp());
            }
        }
    }
    // #@@}

    // #@@range/rewindStack{
    protected void rewindStack(long len) {
        if (len > 0) {
            if (stackGrowsLower) {
                as.add(imm(len), sp());
            }
            else {
                as.sub(imm(len), sp());
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

    // #@@range/virtualPush{
    protected void virtualPush(Register reg) {
        extendVirtualStack(stackWordSize);
        as.relocatableMov(reg, stackTop());
        if (options.isVerboseAsm()) {
            as.comment("push " + reg.name() + " -> " + stackTop());
        }
    }
    // #@@}

    // #@@range/virtualPop{
    protected void virtualPop(Register reg) {
        if (options.isVerboseAsm()) {
            as.comment("pop  " + reg.name() + " <- " + stackTop());
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
    public Void visit(Call node) {
        // compile function arguments from right to left.
        ListIterator<Expr> args = node.finalArg();
        while (args.hasPrevious()) {
            compile(args.previous());
            as.push(reg("ax"));
        }
        // call
        if (node.isStaticCall()) {
            // call via function name
            as.call(node.function().callingSymbol());
        }
        else {
            // call via pointer
            compile(node.expr());
            as.callAbsolute(reg("ax"));
        }
        // rewind stack
        // >4 bytes arguments are not supported.
        rewindStack(node.numArgs() * stackWordSize);
        return null;
    }
    // #@@}

    // #@@range/compile_Return{
    public Void visit(Return node) {
        if (node.expr() != null) {
            compile(node.expr());
        }
        as.jmp(epilogue);
        return null;
    }
    // #@@}

    //
    // Statements
    //

    // #@@range/compileStmt{
    protected void compileStmt(Stmt stmt) {
        if (options.isVerboseAsm()) {
            if (stmt.location() == null) {
                as.comment("(null)");
            }
            else {
                as.comment(stmt.location().numberedLine());
            }
        }
        stmt.accept(this);
    }
    // #@@}

    public Void visit(ExprStmt stmt) {
        compile(stmt.expr());
        return null;
    }

    // #@@range/testCond{
    private void testCond(Type t, Register reg) {
        as.test(t, reg.forType(t), reg.forType(t));
    }
    // #@@}

    // #@@range/compile_BranchIf{
    public Void visit(BranchIf node) {
        compile(node.cond());
        testCond(node.cond().type(), reg("ax"));
        as.jnz(node.thenLabel());
        as.jmp(node.elseLabel());
        return null;
    }
    // #@@}

    // #@@range/compile_Switch{
    public Void visit(Switch node) {
        compile(node.cond());
        Type t = node.cond().type();
        for (Case c : node.cases()) {
            as.mov(imm(c.value), reg("cx"));
            as.cmp(t, reg("cx", t), reg("ax", t));
            as.je(c.label);
        }
        as.jmp(node.defaultLabel());
        return null;
    }
    // #@@}

    // #@@range/compile_LabelStmt{
    public Void visit(LabelStmt node) {
        as.label(node.label());
        return null;
    }
    // #@@}

    // #@@range/compile_Jump{
    public Void visit(Jump node) {
        as.jmp(node.label());
        return null;
    }
    // #@@}

    //
    // Expressions
    //

    // #@@range/compile{
    protected void compile(Expr n) {
        if (options.isVerboseAsm()) {
            as.comment(n.getClass().getSimpleName() + " {");
            as.indentComment();
        }
        n.accept(this);
        if (options.isVerboseAsm()) {
            as.unindentComment();
            as.comment("}");
        }
    }
    // #@@}

    // #@@range/compile_Bin{
    public Void visit(Bin node) {
        AsmOperand right = null;
        if (!doesRequireRegister(node.op()) && node.right().isConstant()){
            compile(node.left());
            right = node.right().asmValue();
        }
        else if (node.right().isConstantAddress()) {
            compile(node.left());
            loadVariable((Var)node.right(), reg("cx"));
            right = reg("cx", node.type());
        }
        else {
            compile(node.right());
            virtualPush(reg("ax"));
            compile(node.left());
            virtualPop(reg("cx"));
            right = reg("cx", node.type());
        }
        compileBinaryOp(node.op(), node.type(), right);
        return null;
    }
    // #@@}

    // #@@range/doesRequireRegister{
    protected boolean doesRequireRegister(Op op) {
        switch (op) {
        case DIV:
        case MOD:
        case LSHIFT:
        case RSHIFT:
            return true;
        default:
            return false;
        }
    }
    // #@@}

    // #@@range/doesSpillDX{
    protected boolean doesSpillDX(Op op) {
        switch (op) {
        case DIV:
        case MOD:
            return true;
        default:
            return false;
        }
    }
    // #@@}

    // spills: dx
    // #@@range/compileBinaryOp_begin{
    protected void compileBinaryOp(Op op, Type t, AsmOperand right) {
        // #@@range/compileBinaryOp_arithops{
        switch (op) {
        case ADD:
            as.add(t, right, reg("ax", t));
            break;
        case SUB:
            as.sub(t, right, reg("ax", t));
            break;
    // #@@range/compileBinaryOp_begin}
        case MUL:
            as.imul(t, right, reg("ax", t));
            break;
        case DIV:
        case MOD:
            if (t.isSigned()) {
                as.cltd();
                as.idiv(t, reg("cx", t));
            }
            else {
                as.mov(imm(0), reg("dx"));
                as.div(t, reg("cx", t));
            }
            if (op == Op.MOD) {
                as.mov(reg("dx"), reg("ax"));
            }
            break;
        // #@@}
        // #@@range/compileBinaryOp_bitops{
        case BIT_AND:
            as.and(t, right, reg("ax", t));
            break;
        case BIT_OR:
            as.or(t, right, reg("ax", t));
            break;
        case BIT_XOR:
            as.xor(t, right, reg("ax", t));
            break;
        case RSHIFT:
            if (t.isSigned()) {
                as.sar(t, cl(), reg("ax", t));
            }
            else {
                as.shr(t, cl(), reg("ax", t));
            }
            break;
        case LSHIFT:
            as.sal(t, cl(), reg("ax", t));
            break;
        // #@@}
        // #@@range/compileBinaryOp_cmpops{
        default:
            // Comparison operators
            as.cmp(t, right, reg("ax", t));
            if (t.isSigned()) {
                switch (op) {
                case EQ:        as.sete (al()); break;
                case NEQ:       as.setne(al()); break;
                case GT:        as.setg (al()); break;
                case GTEQ:      as.setge(al()); break;
                case LT:        as.setl (al()); break;
                case LTEQ:      as.setle(al()); break;
                default:
                    throw new Error("unknown binary operator: " + op);
                }
            }
            else {
                switch (op) {
                case EQ:        as.sete (al()); break;
                case NEQ:       as.setne(al()); break;
                case GT:        as.seta (al()); break;
                case GTEQ:      as.setae(al()); break;
                case LT:        as.setb (al()); break;
                case LTEQ:      as.setbe(al()); break;
                default:
                    throw new Error("unknown binary operator: " + op);
                }
            }
            as.movzb(t, al(), reg("ax", t));
        }
        // #@@}
    // #@@range/compileBinaryOp_end{
    }
    // #@@}

    // #@@range/compile_Uni{
    public Void visit(Uni node) {
        compile(node.expr());
        switch (node.op()) {
        case UMINUS:
            as.neg(node.expr().type(), reg("ax", node.expr().type()));
            break;
        case BIT_NOT:
            as.not(node.expr().type(), reg("ax", node.expr().type()));
            break;
        case NOT:
            testCond(node.expr().type(), reg("ax"));
            as.sete(al());
            as.movzbl(al(), reg("ax"));
            break;
        case CAST:
            Type src = node.expr().type();
            Type dest = node.type();
            if (src.isSigned()) {
                as.movsx(src, dest,
                      reg("ax").forType(src), reg("ax").forType(dest));
            }
            else {
                as.movzx(src, dest,
                      reg("ax").forType(src), reg("ax").forType(dest));
            }
            break;
        default:
            throw new Error("unknown unary operator: " + node.op());
        }
        return null;
    }
    // #@@}

    // #@@range/compile_Var{
    public Void visit(Var node) {
        loadVariable(node, reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_IntValue{
    public Void visit(IntValue node) {
        loadConstant(node, reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_StringValue{
    public Void visit(StringValue node) {
        loadConstant(node, reg("ax"));
        return null;
    }
    // #@@}

    //
    // Assignable expressions
    //

    private void compileLHS(Expr lhs) {
        if (lhs instanceof Var) {
            // for variables: apply loadVariableAddress
            loadVariableAddress((Var)lhs, reg("ax"));
        }
        else if (lhs instanceof Mem) {
            // for *expr: remove Mem
            compile(((Mem)lhs).expr());
        }
        else {
            // otherwise: fatal error
            throw new Error("must not happen: " + lhs.getClass());
        }
    }

    // #@@range/compile_Assign{
    public Void visit(Assign node) {
        if (node.lhs().isConstantAddress() && node.lhs().memref() != null) {
            compile(node.rhs());
            save(node.lhs().type(), reg("ax"), node.lhs().memref());
        }
        else if (node.rhs().isConstant()) {
            compileLHS(node.lhs());
            as.mov(reg("ax"), reg("cx"));
            loadConstant(node.rhs(), reg("ax"));
            save(node.lhs().type(), reg("ax"), mem(reg("cx")));
        }
        else {
            compile(node.rhs());
            virtualPush(reg("ax"));
            compileLHS(node.lhs());
            as.mov(reg("ax"), reg("cx"));
            virtualPop(reg("ax"));
            save(node.lhs().type(), reg("ax"), mem(reg("cx")));
        }
        return null;
    }
    // #@@}

    // #@@range/compile_Mem{
    public Void visit(Mem node) {
        compile(node.expr());
        load(node.type(), mem(reg("ax")), reg("ax"));
        return null;
    }
    // #@@}

    // #@@range/compile_Addr{
    public Void visit(Addr node) {
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
    protected void loadConstant(Expr node, Register reg) {
        if (node.asmValue() != null) {
            as.mov(node.asmValue(), reg);
        }
        else if (node.memref() != null) {
            as.lea(node.memref(), reg);
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
    protected void loadVariable(Var node, Register dest) {
        if (node.memref() == null) {
            as.mov(node.address(), dest);
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
    protected void loadVariableAddress(Expr node, Register dest) {
        if (node.address() != null) {
            as.mov(node.address(), dest);
        }
        else {
            as.lea(node.memref(), dest);
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
        switch (type.size()) {
        case 1:
            if (type.isSigned()) {  // signed char
                as.movsbl(mem, reg);
            } else {                // unsigned char
                as.movzbl(mem, reg);
            }
            break;
        case 2:
            if (type.isSigned()) {  // signed short
                as.movswl(mem, reg);
            } else {                // unsigned short
                as.movzwl(mem, reg);
            }
            break;
        case 4:
        case 8:                     // int, long, long_long
            as.mov(type, mem, reg.forType(type));
            break;
        default:
            throw new Error("unloadable value size: " + type.size());
        }
    }
    // #@@}

    // #@@range/save{
    protected void save(Type type, Register reg, MemoryReference mem) {
        as.mov(type, reg.forType(type), mem);
    }
    // #@@}
}
