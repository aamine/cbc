package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.sysdep.CodeGeneratorOptions;
import net.loveruby.cflat.ir.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.utils.AsmUtils;
import net.loveruby.cflat.utils.ListUtils;
import net.loveruby.cflat.utils.ErrorHandler;
import java.util.*;

public class CodeGenerator implements net.loveruby.cflat.sysdep.CodeGenerator,
        IRVisitor<Void,Void>, ELFConstants {
    // #@@range/ctor{
    final CodeGeneratorOptions options;
    final Type naturalType;
    final ErrorHandler errorHandler;

    public CodeGenerator(CodeGeneratorOptions options,
            Type naturalType, ErrorHandler errorHandler) {
        this.options = options;
        this.naturalType = naturalType;
        this.errorHandler = errorHandler;
    }
    // #@@}

    /** Compiles IR and generates assembly code. */
    // #@@range/generate{
    public AssemblyCode generate(IR ir) {
        locateSymbols(ir);
        return generateAssemblyCode(ir);
    }
    // #@@}

    static final String LABEL_SYMBOL_BASE = ".L";
    static final String CONST_SYMBOL_BASE = ".LC";

    //
    // locateSymbols
    //

    // #@@range/locateSymbols{
    private void locateSymbols(IR ir) {
        SymbolTable constSymbols = new SymbolTable(CONST_SYMBOL_BASE);
        for (ConstantEntry ent : ir.constantTable().entries()) {
            locateStringLiteral(ent, constSymbols);
        }
        for (Variable var : ir.allGlobalVariables()) {
            locateGlobalVariable(var);
        }
        for (Function func : ir.allFunctions()) {
            locateFunction(func);
        }
    }
    // #@@}

    // #@@range/locateStringLiteral{
    private void locateStringLiteral(ConstantEntry ent, SymbolTable syms) {
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
            ent.setAddress(imm(sym));
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
            return shouldUsePLT(func) ? PLTSymbol(sym) : sym;
        }
    }
    // #@@}

    // #@@range/shouldUsePLT{
    private boolean shouldUsePLT(Entity ent) {
        return options.isPositionIndependent() && !optimizeGvarAccess(ent);
    }
    // #@@}

    // #@@range/optimizeGvarAccess{
    private boolean optimizeGvarAccess(Entity ent) {
        return options.isPIERequired() && ent.isDefined();
    }
    // #@@}

    //
    // generateAssemblyCode
    //

    // #@@range/generateAssemblyCode{
    private AssemblyCode generateAssemblyCode(IR ir) {
        AssemblyCode file = newAssemblyCode();
        file._file(ir.fileName());
        if (ir.isGlobalVariableDefined()) {
            generateDataSection(file, ir.definedGlobalVariables());
        }
        if (ir.isStringLiteralDefined()) {
            generateReadOnlyDataSection(file, ir.constantTable());
        }
        if (ir.isFunctionDefined()) {
            generateTextSection(file, ir.definedFunctions());
        }
        // #@@range/generateAssemblyCode_last{
        if (ir.isCommonSymbolDefined()) {
            generateCommonSymbols(file, ir.definedCommonSymbols());
        }
        if (options.isPositionIndependent()) {
            PICThunk(file, GOTBaseReg());
        }
        return file;
        // #@@}
    }
    // #@@}

    // #@@range/newAssemblyCode{
    private AssemblyCode newAssemblyCode() {
        return new AssemblyCode(
                naturalType, STACK_WORD_SIZE,
                new SymbolTable(LABEL_SYMBOL_BASE),
                options.isVerboseAsm());
    }
    // #@@}

    /** Generates initialized entries */
    // #@@range/generateDataSection{
    private void generateDataSection(AssemblyCode file,
                                    List<DefinedVariable> gvars) {
        file._data();
        for (DefinedVariable var : gvars) {
            Symbol sym = globalSymbol(var.symbolString());
            if (!var.isPrivate()) {
                file._globl(sym);
            }
            file._align(var.alignment());
            file._type(sym, "@object");
            file._size(sym, var.allocSize());
            file.label(sym);
            generateImmediate(file, var.type().allocSize(), var.ir());
        }
    }
    // #@@}

    /** Generates immediate values for .data section */
    // #@@range/generateImmediate{
    private void generateImmediate(AssemblyCode file, long size, Expr node) {
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

    /** Generates .rodata entries (constant strings) */
    // #@@range/generateReadOnlyDataSection{
    private void generateReadOnlyDataSection(AssemblyCode file,
                                    ConstantTable constants) {
        file._section(".rodata");
        for (ConstantEntry ent : constants) {
            file.label(ent.symbol());
            file._string(ent.value());
        }
    }
    // #@@}

    // #@@range/generateTextSection{
    private void generateTextSection(AssemblyCode file,
                                    List<DefinedFunction> functions) {
        file._text();
        for (DefinedFunction func : functions) {
            Symbol sym = globalSymbol(func.name());
            if (! func.isPrivate()) {
                file._globl(sym);
            }
            file._type(sym, "@function");
            file.label(sym);
            compileFunctionBody(file, func);
            file._size(sym, ".-" + sym.toSource());
        }
    }
    // #@@}

    /** Generates BSS entries */
    // #@@range/generateCommonSymbols{
    private void generateCommonSymbols(AssemblyCode file,
                                    List<DefinedVariable> variables) {
        for (DefinedVariable var : variables) {
            Symbol sym = globalSymbol(var.symbolString());
            if (var.isPrivate()) {
                file._local(sym);
            }
            file._comm(sym, var.allocSize(), var.alignment());
        }
    }
    // #@@}

    //
    // PIC/PIE related constants and codes
    //

    // #@@range/loadGOT{
    static private final Symbol GOT = new NamedSymbol("_GLOBAL_OFFSET_TABLE_");

    private void loadGOTBaseAddress(AssemblyCode file, Register reg) {
        file.call(PICThunkSymbol(reg));
        file.add(imm(GOT), reg);
    }
    // #@@}

    private Register GOTBaseReg() {
        return bx();
    }

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

    // #@@range/PICThunkSymbol{
    private Symbol PICThunkSymbol(Register reg) {
        return new NamedSymbol("__i686.get_pc_thunk." + reg.baseName());
    }
    // #@@}

    static private final String
    PICThunkSectionFlags = SectionFlag_allocatable
                         + SectionFlag_executable
                         + SectionFlag_sectiongroup;

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
    private void PICThunk(AssemblyCode file, Register reg) {
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

    // #@@range/alignStack{
    private long alignStack(long size) {
        return AsmUtils.align(size, STACK_WORD_SIZE);
    }
    // #@@}

    // #@@range/stackSizeFromWordNum{
    private long stackSizeFromWordNum(long numWords) {
        return numWords * STACK_WORD_SIZE;
    }
    // #@@}

    // #@@range/StackFrameInfo{
    class StackFrameInfo {
        List<Register> saveRegs;
        long lvarSize;
        long tempSize;

        long saveRegsSize() { return saveRegs.size() * STACK_WORD_SIZE; }
        long lvarOffset() { return saveRegsSize(); }
        long tempOffset() { return saveRegsSize() + lvarSize; }
        long frameSize() { return saveRegsSize() + lvarSize + tempSize; }
    }
    // #@@}

    // #@@range/compileFunctionBody{
    private void compileFunctionBody(AssemblyCode file, DefinedFunction func) {
        StackFrameInfo frame = new StackFrameInfo();
        // #@@range/cfb_locate{
        locateParameters(func.parameters());
        frame.lvarSize = locateLocalVariables(func.lvarScope());
        // #@@}

        // #@@range/cfb_offset{
        AssemblyCode body = optimize(compileStmts(func));
        frame.saveRegs = usedCalleeSaveRegisters(body);
        frame.tempSize = body.virtualStack.maxSize();

        fixLocalVariableOffsets(func.lvarScope(), frame.lvarOffset());
        fixTempVariableOffsets(body, frame.tempOffset());
        // #@@}

        if (options.isVerboseAsm()) {
            printStackFrameLayout(file, frame, func.localVariables());
        }
        // #@@range/cfb_gen{
        generateFunctionBody(file, body, frame);
        // #@@}
    }
    // #@@}

    // #@@range/optimize{
    private AssemblyCode optimize(AssemblyCode body) {
        if (options.optimizeLevel() < 1) {
            return body;
        }
        body.apply(PeepholeOptimizer.defaultSet());
        body.reduceLabels();
        return body;
    }
    // #@@}

    private void printStackFrameLayout(AssemblyCode file,
            StackFrameInfo frame, List<DefinedVariable> lvars) {
        List<MemInfo> vars = new ArrayList<MemInfo>();
        for (DefinedVariable var : lvars) {
            vars.add(new MemInfo(var.memref(), var.name()));
        }
        vars.add(new MemInfo(mem(0, bp()), "return address"));
        vars.add(new MemInfo(mem(4, bp()), "saved %ebp"));
        if (frame.saveRegsSize() > 0) {
            vars.add(new MemInfo(mem(-frame.saveRegsSize(), bp()),
                "saved callee-saved registers (" + frame.saveRegsSize() + " bytes)"));
        }
        if (frame.tempSize > 0) {
            vars.add(new MemInfo(mem(-frame.frameSize(), bp()),
                "tmp variables (" + frame.tempSize + " bytes)"));
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
    private AssemblyCode as;
    private Label epilogue;

    private AssemblyCode compileStmts(DefinedFunction func) {
        as = newAssemblyCode();
        epilogue = new Label();
        for (Stmt s : func.ir()) {
            compileStmt(s);
        }
        as.label(epilogue);
        return as;
    }
    // #@@}

    // does NOT include BP
    // #@@range/usedCalleeSaveRegisters{
    private List<Register> usedCalleeSaveRegisters(AssemblyCode body) {
        List<Register> result = new ArrayList<Register>();
        for (Register reg : calleeSaveRegisters()) {
            if (body.doesUses(reg)) {
                result.add(reg);
            }
        }
        result.remove(bp());
        return result;
    }
    // #@@}

    static final RegisterClass[] CALLEE_SAVE_REGISTERS = {
        RegisterClass.BX, RegisterClass.BP,
        RegisterClass.SI, RegisterClass.DI
    };

    private List<Register> calleeSaveRegistersCache = null;

    private List<Register> calleeSaveRegisters() {
        if (calleeSaveRegistersCache == null) {
            List<Register> regs = new ArrayList<Register>();
            for (RegisterClass c : CALLEE_SAVE_REGISTERS) {
                regs.add(new Register(c, naturalType));
            }
            calleeSaveRegistersCache = regs;
        }
        return calleeSaveRegistersCache;
    }

    // #@@range/generateFunctionBody{
    private void generateFunctionBody(AssemblyCode file,
            AssemblyCode body, StackFrameInfo frame) {
        file.virtualStack.reset();
        prologue(file, frame.saveRegs, frame.frameSize());
        if (options.isPositionIndependent() && body.doesUses(GOTBaseReg())) {
            loadGOTBaseAddress(file, GOTBaseReg());
        }
        file.addAll(body.assemblies());
        epilogue(file, frame.saveRegs);
        file.virtualStack.fixOffset(0);
    }
    // #@@}

    // #@@range/prologue{
    private void prologue(AssemblyCode file,
            List<Register> saveRegs, long frameSize) {
        file.push(bp());
        file.mov(sp(), bp());
        for (Register reg : saveRegs) {
            file.virtualPush(reg);
        }
        extendStack(file, frameSize);
    }
    // #@@}

    // #@@range/epilogue{
    private void epilogue(AssemblyCode file, List<Register> savedRegs) {
        for (Register reg : ListUtils.reverse(savedRegs)) {
            file.virtualPop(reg);
        }
        file.mov(bp(), sp());
        file.pop(bp());
        file.ret();
    }
    // #@@}

    // #@@range/locateParameters{
    static final private long PARAM_START_WORD = 2;
                                    // return addr and saved bp

    private void locateParameters(List<Parameter> params) {
        long numWords = PARAM_START_WORD;
        for (Parameter var : params) {
            var.setMemref(mem(stackSizeFromWordNum(numWords), bp()));
            numWords++;
        }
    }
    // #@@}

    /**
     * Allocates addresses of local variables, but offset is still
     * not determined, assign unfixed IndirectMemoryReference.
     */
    // #@@range/locateLocalVariables{
    private long locateLocalVariables(LocalScope scope) {
        return locateLocalVariables(scope, 0);
    }

    private long locateLocalVariables(LocalScope scope, long parentStackLen) {
        // #@@range/locateLocalVariables_loc{
        long len = parentStackLen;
        for (DefinedVariable var : scope.localVariables()) {
            len = alignStack(len + var.allocSize());
            var.setMemref(relocatableMem(-len, bp()));
        }
        // #@@}

        // #@@range/locateLocalVariables_child{
        long maxLen = len;
        for (LocalScope s : scope.children()) {
            long childLen = locateLocalVariables(s, len);
            maxLen = Math.max(maxLen, childLen);
        }
        return maxLen;
        // #@@}
    }
    // #@@}

    // #@@range/relocatableMem{
    private IndirectMemoryReference relocatableMem(long offset, Register base) {
        return IndirectMemoryReference.relocatable(offset, base);
    }
    // #@@}

    // #@@range/fixLocalVariableOffsets{
    private void fixLocalVariableOffsets(LocalScope scope, long len) {
        for (DefinedVariable var : scope.allLocalVariables()) {
            var.memref().fixOffset(-len);
        }
    }
    // #@@}

    // #@@range/fixTempVariableOffsets{
    private void fixTempVariableOffsets(AssemblyCode asm, long len) {
        asm.virtualStack.fixOffset(-len);
    }
    // #@@}

    // #@@range/extendStack{
    private void extendStack(AssemblyCode file, long len) {
        if (len > 0) {
            file.sub(imm(len), sp());
        }
    }
    // #@@}

    // #@@range/rewindStack{
    private void rewindStack(AssemblyCode file, long len) {
        if (len > 0) {
            file.add(imm(len), sp());
        }
    }
    // #@@}

    /**
     * Implements cdecl function call:
     *    * All arguments are on stack.
     *    * Caller rewinds stack pointer.
     */
    // #@@range/Call{
    public Void visit(Call node) {
        for (Expr arg : ListUtils.reverse(node.args())) {
            compile(arg);
            as.push(ax());
        }
        if (node.isStaticCall()) {
            as.call(node.function().callingSymbol());
        }
        else {
            compile(node.expr());
            as.callAbsolute(ax());
        }
        // >4 bytes arguments are not supported.
        rewindStack(as, stackSizeFromWordNum(node.numArgs()));
        return null;
    }
    // #@@}

    // #@@range/Return{
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

    // #@@range/ExprStmt{
    public Void visit(ExprStmt stmt) {
        compile(stmt.expr());
        return null;
    }
    // #@@}

    // #@@range/LabelStmt{
    public Void visit(LabelStmt node) {
        as.label(node.label());
        return null;
    }
    // #@@}

    // #@@range/Jump{
    public Void visit(Jump node) {
        as.jmp(node.label());
        return null;
    }
    // #@@}

    // #@@range/CJump{
    public Void visit(CJump node) {
        compile(node.cond());
        Type t = node.cond().type();
        as.test(ax(t), ax(t));
        as.jnz(node.thenLabel());
        as.jmp(node.elseLabel());
        return null;
    }
    // #@@}

    public Void visit(Switch node) {
        compile(node.cond());
        Type t = node.cond().type();
        for (Case c : node.cases()) {
            as.mov(imm(c.value), cx());
            as.cmp(cx(t), ax(t));
            as.je(c.label);
        }
        as.jmp(node.defaultLabel());
        return null;
    }

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

    // #@@range/Bin{
    public Void visit(Bin node) {
        // #@@range/Bin_init{
        Op op = node.op();
        Type t = node.type();
        // #@@}
        if (node.right().isConstant() && !doesRequireRegisterOperand(op)) {
            // #@@range/Bin_const{
            compile(node.left());
            compileBinaryOp(op, ax(t), node.right().asmValue());
            // #@@}
        }
        else if (node.right().isConstant()) {
            compile(node.left());
            loadConstant(node.right(), cx());
            compileBinaryOp(op, ax(t), cx(t));
        }
        else if (node.right().isVar()) {
            compile(node.left());
            loadVariable((Var)node.right(), cx(t));
            compileBinaryOp(op, ax(t), cx(t));
        }
        else if (node.right().isAddr()) {
            compile(node.left());
            loadAddress(node.right().getEntityForce(), cx(t));
            compileBinaryOp(op, ax(t), cx(t));
        }
        else if (node.left().isConstant()
                || node.left().isVar()
                || node.left().isAddr()) {
            compile(node.right());
            as.mov(ax(), cx());
            compile(node.left());
            compileBinaryOp(op, ax(t), cx(t));
        }
        else {
            // #@@range/Bin_generic{
            compile(node.right());
            as.virtualPush(ax());
            compile(node.left());
            as.virtualPop(cx());
            compileBinaryOp(op, ax(t), cx(t));
            // #@@}
        }
        return null;
    }
    // #@@}

    // #@@range/doesRequireRegisterOperand{
    private boolean doesRequireRegisterOperand(Op op) {
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
    private void compileBinaryOp(Op op, Register left, Operand right) {
        // #@@range/compileBinaryOp_arithops{
        switch (op) {
        case ADD:
            as.add(right, left);
            break;
        case SUB:
            as.sub(right, left);
            break;
    // #@@range/compileBinaryOp_begin}
        case MUL:
            as.imul(right, left);
            break;
            // #@@range/compileBinaryOp_sdiv{
        case S_DIV:
        case S_MOD:
            as.cltd();
            as.idiv(cx(left.type));
            if (op == Op.S_MOD) {
                as.mov(dx(), left);
            }
            // #@@}
            break;
        case U_DIV:
        case U_MOD:
            as.mov(imm(0), dx());
            as.div(cx(left.type));
            if (op == Op.U_MOD) {
                as.mov(dx(), left);
            }
            break;
        // #@@}
        // #@@range/compileBinaryOp_bitops{
        case BIT_AND:
            as.and(right, left);
            break;
        case BIT_OR:
            as.or(right, left);
            break;
        case BIT_XOR:
            as.xor(right, left);
            break;
        case BIT_LSHIFT:
            as.sal(cl(), left);
            break;
        case BIT_RSHIFT:
            as.shr(cl(), left);
            break;
        case ARITH_RSHIFT:
            as.sar(cl(), left);
            break;
        // #@@}
        // #@@range/compileBinaryOp_cmpops{
        default:
            // Comparison operators
            as.cmp(right, ax(left.type));
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
            as.movzx(al(), left);
        }
        // #@@}
    }

    // #@@range/Uni{
    public Void visit(Uni node) {
        Type src = node.expr().type();
        Type dest = node.type();

        compile(node.expr());
        switch (node.op()) {
        case UMINUS:
            as.neg(ax(src));
            break;
        case BIT_NOT:
            as.not(ax(src));
            break;
        case NOT:
            // #@@range/Uni_not{
            as.test(ax(src), ax(src));
            as.sete(al());
            as.movzx(al(), ax(dest));
            // #@@}
            break;
        case S_CAST:
            as.movsx(ax(src), ax(dest));
            break;
        case U_CAST:
            as.movzx(ax(src), ax(dest));
            break;
        default:
            throw new Error("unknown unary operator: " + node.op());
        }
        return null;
    }
    // #@@}

    // #@@range/Var{
    public Void visit(Var node) {
        loadVariable(node, ax());
        return null;
    }
    // #@@}

    // #@@range/Int{
    public Void visit(Int node) {
        as.mov(imm(node.value()), ax());
        return null;
    }
    // #@@}

    // #@@range/Str{
    public Void visit(Str node) {
        loadConstant(node, ax());
        return null;
    }
    // #@@}

    //
    // Assignable expressions
    //

    // #@@range/Assign{
    public Void visit(Assign node) {
        if (node.lhs().isAddr() && node.lhs().memref() != null) {
            compile(node.rhs());
            store(ax(node.lhs().type()), node.lhs().memref());
        }
        else if (node.rhs().isConstant()) {
            compile(node.lhs());
            as.mov(ax(), cx());
            loadConstant(node.rhs(), ax());
            store(ax(node.lhs().type()), mem(cx()));
        }
        else {
            compile(node.rhs());
            as.virtualPush(ax());
            compile(node.lhs());
            as.mov(ax(), cx());
            as.virtualPop(ax());
            store(ax(node.lhs().type()), mem(cx()));
        }
        return null;
    }
    // #@@}

    // #@@range/Mem{
    public Void visit(Mem node) {
        compile(node.expr());
        load(mem(ax()), ax(node.type()));
        return null;
    }
    // #@@}

    // #@@range/Addr{
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

    /** Loads variable content to the register. */
    // #@@range/loadVariable{
    private void loadVariable(Var var, Register dest) {
        if (var.memref() == null) {
            Register a = dest.forType(naturalType);
            as.mov(var.address(), a);
            load(mem(a), dest.forType(var.type()));
        }
        else {
            load(var.memref(), dest.forType(var.type()));
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

    // #@@range/reg_dsls2{
    private Register ax() { return ax(naturalType); }
    private Register al() { return ax(Type.INT8); }
    private Register bx() { return bx(naturalType); }
    // #@@}
    private Register cx() { return cx(naturalType); }
    private Register cl() { return cx(Type.INT8); }
    private Register dx() { return dx(naturalType); }

    // #@@range/reg_dsls1{
    private Register ax(Type t) {
        return new Register(RegisterClass.AX, t);
    }

    private Register bx(Type t) {
        return new Register(RegisterClass.BX, t);
    }
    // #@@}

    private Register cx(Type t) {
        return new Register(RegisterClass.CX, t);
    }

    private Register dx(Type t) {
        return new Register(RegisterClass.DX, t);
    }

    private Register si() {
        return new Register(RegisterClass.SI, naturalType);
    }

    private Register di() {
        return new Register(RegisterClass.DI, naturalType);
    }

    private Register bp() {
        return new Register(RegisterClass.BP, naturalType);
    }

    private Register sp() {
        return new Register(RegisterClass.SP, naturalType);
    }

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
    private void load(MemoryReference mem, Register reg) {
        as.mov(mem, reg);
    }
    // #@@}

    // #@@range/store{
    private void store(Register reg, MemoryReference mem) {
        as.mov(reg, mem);
    }
    // #@@}
}
