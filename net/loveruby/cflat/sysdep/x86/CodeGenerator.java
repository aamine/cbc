package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.sysdep.CodeGeneratorOptions;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.utils.AsmUtils;
import net.loveruby.cflat.utils.ErrorHandler;
import java.util.*;

public class CodeGenerator
        implements net.loveruby.cflat.sysdep.CodeGenerator,
                IRVisitor<Void,Void>,
                ELFConstants {
    // #@@range/ctor{
    private CodeGeneratorOptions options;
    private Type naturalType;
    private ErrorHandler errorHandler;

    public CodeGenerator(CodeGeneratorOptions options,
                         Type naturalType,
                         ErrorHandler errorHandler) {
        this.options = options;
        this.naturalType = naturalType;
        this.errorHandler = errorHandler;
    }
    // #@@}

    /** Compiles IR and generates assembly code. */
    // #@@range/generate{
    static final private String CONST_SYMBOL_BASE = ".LC";
    static final private String LABEL_SYMBOL_BASE = ".L";

    public String generate(IR ir) {
        SymbolTable constSymbols = new SymbolTable(CONST_SYMBOL_BASE);
        for (ConstantEntry ent : ir.constantTable().entries()) {
            locateConstant(ent, constSymbols);
        }
        for (Variable var : ir.allGlobalVariables()) {
            locateGlobalVariable(var);
        }
        for (Function func : ir.allFunctions()) {
            locateFunction(func);
        }
        AssemblyFile file = compileIR(ir);
        return file.toSource(new SymbolTable(LABEL_SYMBOL_BASE));
    }
    // #@@}

    // #@@range/newAssemblyFile{
    private AssemblyFile newAssemblyFile() {
        return new AssemblyFile(
                naturalType, STACK_WORD_SIZE, options.isVerboseAsm());
    }
    // #@@}

    // #@@range/compileIR{
    private AssemblyFile compileIR(IR ir) {
        AssemblyFile file = newAssemblyFile();
        file._file(ir.fileName());
        // .data
        List<DefinedVariable> gvars = ir.definedGlobalVariables();
        if (!gvars.isEmpty()) {
            file._data();
            for (DefinedVariable gvar : gvars) {
                dataEntry(file, gvar);
            }
        }
        if (!ir.constantTable().isEmpty()) {
            file._section(".rodata");
            for (ConstantEntry ent : ir.constantTable()) {
                compileStringLiteral(file, ent);
            }
        }
        // .text
        if (ir.functionDefined()) {
            file._text();
            for (DefinedFunction func : ir.definedFunctions()) {
                compileFunction(file, func);
            }
        }
        // .bss
        for (DefinedVariable var : ir.definedCommonSymbols()) {
            compileCommonSymbol(file, var);
        }
        // others
        if (options.isPositionIndependent()) {
            PICThunk(file, GOTBaseReg());
        }
        return file;
    }
    // #@@}

    // #@@range/locateConstant{
    private void locateConstant(ConstantEntry ent, SymbolTable syms) {
        ent.setSymbol(syms.newSymbol());
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
    private void locateGlobalVariable(Entity ent) {
        Symbol sym = symbol(ent.symbolString(), ent.isPrivate());
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
    private void locateFunction(Function func) {
        func.setCallingSymbol(callingSymbol(func));
        locateGlobalVariable(func);
    }
    // #@@}

    // #@@range/symbol{
    private Symbol symbol(String sym, boolean isPrivate) {
        return isPrivate ? privateSymbol(sym) : globalSymbol(sym);
    }
    // #@@}

    // #@@range/globalSymbol{
    private Symbol globalSymbol(String sym) {
        return new NamedSymbol(sym);
    }
    // #@@}

    // #@@range/privateSymbol{
    private Symbol privateSymbol(String sym) {
        return new NamedSymbol(sym);
    }
    // #@@}

    // #@@range/callingSymbol{
    private Symbol callingSymbol(Function func) {
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
    private boolean doesIndirectAccess(Entity ent) {
        return options.isPositionIndependent() && !optimizeGvarAccess(ent);
    }
    // #@@}

    // #@@range/optimizeGvarAccess{
    private boolean optimizeGvarAccess(Entity ent) {
        return options.isPIERequired() && ent.isDefined();
    }
    // #@@}

    /** Generates initialized entries */
    // #@@range/dataEntry{
    private void dataEntry(AssemblyFile file, DefinedVariable ent) {
        Symbol sym = globalSymbol(ent.symbolString());
        if (!ent.isPrivate()) {
            file._globl(sym);
        }
        file._align(ent.alignment());
        file._type(sym, "@object");
        file._size(sym, ent.allocSize());
        file.label(sym);
        compileImmediate(file, ent.type().allocSize(), ent.ir());
    }
    // #@@}

    /** Generates immediate values for .data section */
    // #@@range/compileImmediates{
    private void compileImmediate(AssemblyFile file, long size, Expr node) {
        if (node instanceof Int) {
            Int expr = (Int)node;
            switch ((int)size) {
            case 1: file._byte(expr.value());    break;
            case 2: file._value(expr.value());   break;
            case 4: file._long(expr.value());    break;
            case 8: file._quad(expr.value());    break;
            default:
                throw new Error("entry size must be 1,2,4,8");
            }
        }
        else if (node instanceof Str) {
            Str expr = (Str)node;
            switch ((int)size) {
            case 4: file._long(expr.symbol());   break;
            case 8: file._quad(expr.symbol());   break;
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
    private void compileCommonSymbol(AssemblyFile file, DefinedVariable var) {
        Symbol sym = globalSymbol(var.symbolString());
        if (var.isPrivate()) {
            file._local(sym);
        }
        file._comm(sym, var.allocSize(), var.alignment());
    }
    // #@@}

    /** Generates .rodata entry (constant strings) */
    // #@@range/compileStringLiteral{
    private void compileStringLiteral(AssemblyFile file, ConstantEntry ent) {
        file.label(ent.symbol());
        file._string(ent.value());
    }
    // #@@}

    //
    // PIC/PIE related constants and codes
    //

    // #@@range/pic_methods{
    static private final Symbol GOT =
            new NamedSymbol("_GLOBAL_OFFSET_TABLE_");

    private void loadGOTBaseAddress(AssemblyFile file, Register reg) {
        file.call(PICThunkSymbol(reg));
        file.add(imm(GOT), reg);
    }

    private Register GOTBaseReg() {
        return bx();
    }
    // #@@}

    // #@@range/pic_symbols{
    private Symbol globalGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOT");
    }

    private Symbol localGOTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@GOTOFF");
    }

    private Symbol PLTSymbol(Symbol base) {
        return new SuffixedSymbol(base, "@PLT");
    }
    // #@@}

    // #@@range/pic_thunk_helper{
    private Symbol PICThunkSymbol(Register reg) {
        return new NamedSymbol("__i686.get_pc_thunk." + reg.baseName());
    }

    static private final String
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
    private void PICThunk(AssemblyFile file, Register reg) {
        Symbol sym = PICThunkSymbol(reg);
        file._section(".text" + "." + sym.toSource(),
                 "\"" + PICThunkSectionFlags + "\"",
                 SectionType_bits,      // This section contains data
                 sym.toSource(),        // The name of section group
                Linkage_linkonce);      // Only 1 copy should be generated
        file._globl(sym);
        file._hidden(sym);
        file._type(sym, SymbolType_function);
        file.label(sym);
        file.mov(mem(sp()), reg);    // fetch saved EIP to the GOT base register
        file.ret();
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

    // #@@range/stackParams{
    static final private long STACK_WORD_SIZE = 4;
    // #@@}

    private long alignStack(long size) {
        return AsmUtils.align(size, STACK_WORD_SIZE);
    }

    private long stackSizeFromWordNum(long numWords) {
        return numWords * STACK_WORD_SIZE;
    }

    /** Compiles a function. */
    // #@@range/compileFunction{
    private void compileFunction(AssemblyFile file, DefinedFunction func) {
        allocateParameters(func);
        allocateLocalVariablesTemp(func.body().scope());

        Symbol sym = globalSymbol(func.name());
        if (! func.isPrivate()) {
            file._globl(sym);
        }
        file._type(sym, "@function");
        file.label(sym);
        compileFunctionBody(file, func);
        file._size(sym, ".-" + sym.toSource());
    }
    // #@@}

    // #@@range/compileFunctionBody{
    private void compileFunctionBody(
            AssemblyFile file, DefinedFunction func) {
        AssemblyFile body = compileStmts(func);
        List<Assembly> bodyAsms = optimize(body.assemblies());
        AsmStatistics stats = AsmStatistics.collect(bodyAsms);
        bodyAsms = reduceLabels(bodyAsms, stats);
        List<Register> saveRegs = usedCalleeSavedRegistersWithoutBP(stats);
        long saveRegsBytes = stackSizeFromWordNum(saveRegs.size());
        long lvarBytes = allocateLocalVariables(
                func.body().scope(), saveRegsBytes);
        fixTmpOffsets(bodyAsms, saveRegsBytes + lvarBytes);

        if (options.isVerboseAsm()) {
            printStackFrameLayout(file,
                    saveRegsBytes, lvarBytes, body.maxTmpBytes(),
                    func.localVariables());
        }

        file.initVirtualStack();
        prologue(file, func, saveRegs,
                saveRegsBytes + lvarBytes + body.maxTmpBytes());
        if (options.isPositionIndependent()
                && stats.doesRegisterUsed(GOTBaseReg())) {
            loadGOTBaseAddress(file, GOTBaseReg());
        }
        file.addAll(bodyAsms);
        epilogue(file, func, saveRegs, lvarBytes);
    }
    // #@@}

    // #@@range/compileFunctionBody{
    private List<Assembly> optimize(List<Assembly> asms) {
        if (options.optimizeLevel() < 1) {
            return asms;
        }
        return new PeepholeOptimizer().optimize(asms);
    }

    private void printStackFrameLayout(
            AssemblyFile file,
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
        file.comment("---- Stack Frame Layout -----------");
        for (MemInfo info : vars) {
            file.comment(info.mem.toString() + ": " + info.name);
        }
        file.comment("-----------------------------------");
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
    private AssemblyFile as;
    private Label epilogue;

    private AssemblyFile compileStmts(DefinedFunction func) {
        as = newAssemblyFile();
        epilogue = new Label();
        for (Stmt s : func.ir()) {
            compileStmt(s);
        }
        as.label(epilogue);
        return as;
    }
    // #@@}

    // #@@range/reduceLabels{
    private List<Assembly> reduceLabels(List<Assembly> assemblies, AsmStatistics stats) {
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

    private List<Register> usedCalleeSavedRegistersWithoutBP(AsmStatistics stats) {
        List<Register> result = new ArrayList<Register>();
        for (Register reg : calleeSavedRegisters()) {
            if (stats.doesRegisterUsed(reg) && !reg.equals(bp())) {
                result.add(reg);
            }
        }
        return result;
    }

    private List<Register> calleeSavedRegistersCache = null;

    private List<Register> calleeSavedRegisters() {
        if (calleeSavedRegistersCache == null) {
            List<Register> regs = new ArrayList<Register>();
            regs.add(bx());
            regs.add(si());
            regs.add(di());
            regs.add(bp());
            calleeSavedRegistersCache = regs;
        }
        return calleeSavedRegistersCache;
    }

    // #@@range/prologue{
    private void prologue(AssemblyFile file, DefinedFunction func,
            List<Register> saveRegs, long frameSize) {
        file.push(bp());
        file.mov(sp(), bp());
        saveRegisters(file, saveRegs);
        extendStack(file, frameSize);
    }
    // #@@}

    // #@@range/epilogue{
    private void epilogue(AssemblyFile file, DefinedFunction func,
            List<Register> savedRegs, long lvarBytes) {
        restoreRegisters(file, savedRegs);
        file.mov(bp(), sp());
        file.pop(bp());
        file.ret();
    }
    // #@@}

    // #@@range/saveRegisters{
    private void saveRegisters(AssemblyFile file, List<Register> saveRegs) {
        for (Register reg : saveRegs) {
            file.virtualPush(reg);
        }
    }
    // #@@}

    // #@@range/restoreRegisters{
    private void restoreRegisters(
            AssemblyFile file, List<Register> savedRegs) {
        ListIterator<Register> regs = savedRegs.listIterator(savedRegs.size());
        while (regs.hasPrevious()) {
            file.virtualPop(regs.previous());
        }
    }
    // #@@}

    // #@@range/allocateParameters{
    static final private long paramStartWordNum = 2;
                                    // return addr and saved bp

    private void allocateParameters(DefinedFunction func) {
        long numWords = paramStartWordNum;
        for (Parameter var : func.parameters()) {
            var.setMemref(mem(stackSizeFromWordNum(numWords), bp()));
            numWords++;
        }
    }
    // #@@}

    /**
     * Allocates addresses of local variables, but offset is still
     * not determined, assign unfixed IndirectMemoryReference.
     */
    // #@@range/allocateVariablesTemp{
    private void allocateLocalVariablesTemp(LocalScope scope) {
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
    private long allocateLocalVariables(LocalScope scope, long initLen) {
        long maxLen = allocateScope(scope, initLen);
        return maxLen - initLen;
    }
    // #@@}

    // #@@range/allocateScope{
    private long allocateScope(LocalScope scope, long parentStackLen) {
        long len = parentStackLen;
        for (DefinedVariable var : scope.localVariables()) {
            len = alignStack(len + var.allocSize());
            fixMemref((IndirectMemoryReference)var.memref(), -len);
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
    private void fixMemref(IndirectMemoryReference memref, long offset) {
        memref.fixOffset(offset);
    }
    // #@@}

    // #@@range/extendStack{
    private void extendStack(AssemblyFile file, long len) {
        if (len > 0) {
            file.sub(imm(len), sp());
        }
    }
    // #@@}

    // #@@range/rewindStack{
    private void rewindStack(AssemblyFile file, long len) {
        if (len > 0) {
            file.add(imm(len), sp());
        }
    }
    // #@@}

    // #@@range/fixTmpOffsets{
    private void fixTmpOffsets(List<Assembly> asms, long offset) {
        for (Assembly asm : asms) {
            asm.fixStackOffset(-offset);
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
            as.push(ax());
        }
        // call
        if (node.isStaticCall()) {
            // call via function name
            as.call(node.function().callingSymbol());
        }
        else {
            // call via pointer
            compile(node.expr());
            as.callAbsolute(ax());
        }
        // rewind stack
        // >4 bytes arguments are not supported.
        rewindStack(as, stackSizeFromWordNum(node.numArgs()));
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
    private void compileStmt(Stmt stmt) {
        if (options.isVerboseAsm()) {
            if (stmt.location() != null) {
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
        testCond(node.cond().type(), ax());
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
            as.mov(imm(c.value), cx());
            as.cmp(t, cx(t), ax(t));
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
    private void compile(Expr n) {
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
        if (!doesSpillRegister(node.op()) && node.right().isConstant()){
            compile(node.left());
            right = node.right().asmValue();
        }
        else if (node.right().isConstantAddress()) {
            compile(node.left());
            loadVariable(((Var)node.right()), cx());
            right = cx(node.type());
        }
        else {
            compile(node.right());
            as.virtualPush(ax());
            compile(node.left());
            as.virtualPop(cx());
            right = cx(node.type());
        }
        compileBinaryOp(node.type(), node.op(), ax(node.type()), right);
        return null;
    }
    // #@@}

    // #@@range/doesSpillRegister{
    private boolean doesSpillRegister(Op op) {
        switch (op) {
        case S_DIV:
        case U_DIV:
        case S_MOD:
        case U_MOD:
        case BIT_LSHIFT:
        case BIT_RSHIFT:
        case ARITH_RSHIFT:
            return true;
        default:
            return false;
        }
    }
    // #@@}

    // #@@range/compileBinaryOp_begin{
    private void compileBinaryOp(Type t, Op op,
            Register left, AsmOperand right) {
        // #@@range/compileBinaryOp_arithops{
        switch (op) {
        case ADD:
            as.add(t, right, left);
            break;
        case SUB:
            as.sub(t, right, left);
            break;
    // #@@range/compileBinaryOp_begin}
        case MUL:
            as.imul(t, right, left);
            break;
        case S_DIV:
        case S_MOD:
            as.cltd();
            as.idiv(t, cx(t));
            if (op == Op.S_MOD) {
                as.mov(dx(), left);
            }
            break;
        case U_DIV:
        case U_MOD:
            as.mov(imm(0), dx());
            as.div(t, cx(t));
            if (op == Op.U_MOD) {
                as.mov(dx(), left);
            }
            break;
        // #@@}
        // #@@range/compileBinaryOp_bitops{
        case BIT_AND:
            as.and(t, right, left);
            break;
        case BIT_OR:
            as.or(t, right, left);
            break;
        case BIT_XOR:
            as.xor(t, right, left);
            break;
        case BIT_LSHIFT:
            as.sal(t, cl(), left);
            break;
        case BIT_RSHIFT:
            as.shr(t, cl(), left);
            break;
        case ARITH_RSHIFT:
            as.sar(t, cl(), left);
            break;
        // #@@}
        // #@@range/compileBinaryOp_cmpops{
        default:
            // Comparison operators
            as.cmp(t, right, ax(t));
            switch (op) {
            case EQ:        as.sete (al()); break;
            case NEQ:       as.setne(al()); break;
            case S_GT:      as.setg (al()); break;
            case S_GTEQ:    as.setge(al()); break;
            case S_LT:      as.setl (al()); break;
            case S_LTEQ:    as.setle(al()); break;
            case U_GT:      as.seta (al()); break;
            case U_GTEQ:    as.setae(al()); break;
            case U_LT:      as.setb (al()); break;
            case U_LTEQ:    as.setbe(al()); break;
            default:
                throw new Error("unknown binary operator: " + op);
            }
            as.movzb(t, al(), ax(t));
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
            as.neg(node.expr().type(), ax(node.expr().type()));
            break;
        case BIT_NOT:
            as.not(node.expr().type(), ax(node.expr().type()));
            break;
        case NOT:
            testCond(node.expr().type(), ax());
            as.sete(al());
            as.movzbl(al(), ax());
            break;
        case S_CAST:
            {
                Type src = node.expr().type();
                Type dest = node.type();
                as.movsx(src, dest, ax(src), ax(dest));
            }
            break;
        case U_CAST:
            {
                Type src = node.expr().type();
                Type dest = node.type();
                as.movzx(src, dest, ax(src), ax(dest));
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
        loadVariable(node, ax());
        return null;
    }
    // #@@}

    // #@@range/compile_Int{
    public Void visit(Int node) {
        loadConstant(node, ax());
        return null;
    }
    // #@@}

    // #@@range/compile_Str{
    public Void visit(Str node) {
        loadConstant(node, ax());
        return null;
    }
    // #@@}

    //
    // Assignable expressions
    //

    // #@@range/compile_Assign{
    public Void visit(Assign node) {
        if (node.lhs().isConstantAddress() && node.lhs().memref() != null) {
            compile(node.rhs());
            save(node.lhs().type(), ax(), node.lhs().memref());
        }
        else if (node.rhs().isConstant()) {
            compile(node.lhs());
            as.mov(ax(), cx());
            loadConstant(node.rhs(), ax());
            save(node.lhs().type(), ax(), mem(cx()));
        }
        else {
            compile(node.rhs());
            as.virtualPush(ax());
            compile(node.lhs());
            as.mov(ax(), cx());
            as.virtualPop(ax());
            save(node.lhs().type(), ax(), mem(cx()));
        }
        return null;
    }
    // #@@}

    // #@@range/compile_Mem{
    public Void visit(Mem node) {
        compile(node.expr());
        load(node.type(), mem(ax()), ax());
        return null;
    }
    // #@@}

    // #@@range/compile_Addr{
    public Void visit(Addr node) {
        loadAddress(node.entity(), ax());
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
    private void loadConstant(Expr node, Register reg) {
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

    /** Loads variable value to the register. */
    // #@@range/loadVariable{
    private void loadVariable(Var var, Register dest) {
        if (var.memref() == null) {
            as.mov(var.address(), dest);
            load(var.type(), mem(dest), dest);
        }
        else {
            load(var.type(), var.memref(), dest);
        }
    }
    // #@@}

    /** Loads the address of the variable to the register. */
    // #@@range/loadAddress{
    private void loadAddress(Entity var, Register dest) {
        if (var.address() != null) {
            as.mov(var.address(), dest);
        }
        else {
            as.lea(var.memref(), dest);
        }
    }
    // #@@}

    // #@@range/reg_dsls{
    private Register ax() { return new Register(RegKind.AX, naturalType); }
    private Register bx() { return new Register(RegKind.BX, naturalType); }
    private Register cx() { return new Register(RegKind.CX, naturalType); }
    private Register dx() { return new Register(RegKind.DX, naturalType); }
    private Register si() { return new Register(RegKind.SI, naturalType); }
    private Register di() { return new Register(RegKind.DI, naturalType); }
    private Register bp() { return new Register(RegKind.BP, naturalType); }
    private Register sp() { return new Register(RegKind.SP, naturalType); }

    private Register al() { return new Register(RegKind.AX, Type.INT8); }
    private Register cl() { return new Register(RegKind.CX, Type.INT8); }

    private Register ax(Type t) { return new Register(RegKind.AX, t); }
    private Register cx(Type t) { return new Register(RegKind.CX, t); }
    // #@@}

    // #@@range/mem{
    private DirectMemoryReference mem(Symbol sym) {
        return new DirectMemoryReference(sym);
    }

    private IndirectMemoryReference mem(Register reg) {
        return new IndirectMemoryReference(0, reg);
    }

    private IndirectMemoryReference mem(long offset, Register reg) {
        return new IndirectMemoryReference(offset, reg);
    }

    private IndirectMemoryReference mem(Symbol offset, Register reg) {
        return new IndirectMemoryReference(offset, reg);
    }
    // #@@}

    // #@@range/imm{
    private ImmediateValue imm(long n) {
        return new ImmediateValue(n);
    }

    private ImmediateValue imm(Symbol sym) {
        return new ImmediateValue(sym);
    }

    private ImmediateValue imm(Literal lit) {
        return new ImmediateValue(lit);
    }
    // #@@}

    // #@@range/load{
    private void load(Type type, MemoryReference mem, Register reg) {
        as.mov(type, mem, reg.forType(type));
    }
    // #@@}

    // #@@range/save{
    private void save(Type type, Register reg, MemoryReference mem) {
        as.mov(type, reg.forType(type), mem);
    }
    // #@@}
}
