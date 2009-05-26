package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.utils.TextUtils;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;

public class AssemblyFile implements net.loveruby.cflat.sysdep.AssemblyFile {
    private final List<Assembly> assemblies;
    final Type naturalType;
    final long stackWordSize;
    final SymbolTable labelSymbols;
    final boolean verbose;
    private int commentIndentLevel;

    AssemblyFile(Type naturalType, long stackWordSize,
            SymbolTable labelSymbols, boolean verbose) {
        this.naturalType = naturalType;
        this.stackWordSize = stackWordSize;
        this.labelSymbols = labelSymbols;
        this.verbose = verbose;
        this.assemblies = new ArrayList<Assembly>();
        this.commentIndentLevel = 0;
        initVirtualStack();
    }

    List<Assembly> assemblies() {
        return this.assemblies;
    }

    void addAll(List<Assembly> assemblies) {
        this.assemblies.addAll(assemblies);
    }

    public String toSource() {
        StringBuffer buf = new StringBuffer();
        for (Assembly asm : assemblies) {
            buf.append(asm.toSource(labelSymbols));
            buf.append("\n");
        }
        return buf.toString();
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream s) {
        // FIXME
    }

    void comment(String str) {
        assemblies.add(new Comment(str, commentIndentLevel));
    }

    void indentComment() {
        commentIndentLevel++;
    }

    void unindentComment() {
        commentIndentLevel--;
    }

    void label(Symbol sym) {
        assemblies.add(new Label(sym));
    }

    void label(Label label) {
        assemblies.add(label);
    }

    protected void directive(String direc) {
        assemblies.add(new Directive(direc));
    }

    protected void insn(String op) {
        assemblies.add(new Instruction(op));
    }

    protected void insn(String op, Operand a) {
        assemblies.add(new Instruction(op, "", a));
    }

    protected void insn(String op, String suffix, Operand a) {
        assemblies.add(new Instruction(op, suffix, a));
    }

    protected void insn(Type t, String op, Operand a) {
        assemblies.add(new Instruction(op, typeSuffix(t), a));
    }

    protected void insn(String op, String suffix, Operand a, Operand b) {
        assemblies.add(new Instruction(op, suffix, a, b));
    }

    protected void insn(Type t, String op, Operand a, Operand b) {
        assemblies.add(new Instruction(op, typeSuffix(t), a, b));
    }

    protected String typeSuffix(Type t1, Type t2) {
        return typeSuffix(t1) + typeSuffix(t2);
    }

    protected String typeSuffix(Type t) {
        switch (t) {
        case INT8: return "b";
        case INT16: return "w";
        case INT32: return "l";
        case INT64: return "q";
        default:
            throw new Error("unknown register type: " + t.size());
        }
    }

    //
    // directives
    //

    void _file(String name) {
        directive(".file\t" + TextUtils.dumpString(name));
    }

    void _text() {
        directive("\t.text");
    }

    void _data() {
        directive("\t.data");
    }

    void _section(String name) {
        directive("\t.section\t" + name);
    }

    void _section(String name, String flags, String type, String group, String linkage) {
        directive("\t.section\t" + name + "," + flags + "," + type + "," + group + "," + linkage);
    }

    void _globl(Symbol sym) {
        directive(".globl " + sym.name());
    }

    void _local(Symbol sym) {
        directive(".local " + sym.name());
    }

    void _hidden(Symbol sym) {
        directive("\t.hidden\t" + sym.name());
    }

    void _comm(Symbol sym, long size, long alignment) {
        directive("\t.comm\t" + sym.name() + "," + size + "," + alignment);
    }

    void _align(long n) {
        directive("\t.align\t" + n);
    }

    void _type(Symbol sym, String type) {
        directive("\t.type\t" + sym.name() + "," + type);
    }

    void _size(Symbol sym, long size) {
        _size(sym, new Long(size).toString());
    }

    void _size(Symbol sym, String size) {
        directive("\t.size\t" + sym.name() + "," + size);
    }

    void _byte(long val) {
        directive(".byte\t" + new IntegerLiteral((byte)val).toSource());
    }

    void _value(long val) {
        directive(".value\t" + new IntegerLiteral((short)val).toSource());
    }

    void _long(long val) {
        directive(".long\t" + new IntegerLiteral((int)val).toSource());
    }

    void _quad(long val) {
        directive(".quad\t" + new IntegerLiteral(val).toSource());
    }

    void _byte(Literal val) {
        directive(".byte\t" + val.toSource());
    }

    void _value(Literal val) {
        directive(".value\t" + val.toSource());
    }

    void _long(Literal val) {
        directive(".long\t" + val.toSource());
    }

    void _quad(Literal val) {
        directive(".quad\t" + val.toSource());
    }

    void _string(String str) {
        directive("\t.string\t" + TextUtils.dumpString(str));
    }

    //
    // Virtual Stack
    //

    // #@@range/virtual_stack{
    private long stackPointer;
    private long stackPointerMax;

    void initVirtualStack() {
        stackPointer = 0;
        stackPointerMax = stackPointer;
    }
    // #@@}

    // #@@range/maxTmpBytes{
    long maxTmpBytes() {
        return stackPointerMax;
    }
    // #@@}

    // #@@range/stackTop{
    IndirectMemoryReference stackTop() {
        return new IndirectMemoryReference(-stackPointer, bp());
    }
    // #@@}

    private Register bp() {
        return new Register(RegisterClass.BP, naturalType);
    }

    // #@@range/virtualPush{
    void virtualPush(Register reg) {
        if (verbose) {
            comment("push " + reg.baseName() + " -> " + stackTop());
        }
        extendVirtualStack(stackWordSize);
        relocatableMov(reg, stackTop());
    }
    // #@@}

    // #@@range/virtualPop{
    void virtualPop(Register reg) {
        if (verbose) {
            comment("pop  " + reg.baseName() + " <- " + stackTop());
        }
        relocatableMov(stackTop(), reg);
        rewindVirtualStack(stackWordSize);
    }
    // #@@}

    // #@@range/extendVirtualStack{
    void extendVirtualStack(long len) {
        stackPointer += len;
        stackPointerMax = Math.max(stackPointerMax, stackPointer);
    }
    // #@@}

    // #@@range/rewindVirtualStack{
    void rewindVirtualStack(long len) {
        stackPointer -= len;
    }
    // #@@}

    //
    // Instructions
    //

    void jmp(Label label) {
        insn("jmp", new DirectMemoryReference(label.symbol()));
    }

    void jz(Label label) {
        insn("jz", new DirectMemoryReference(label.symbol()));
    }

    void jnz(Label label) {
        insn("jnz", new DirectMemoryReference(label.symbol()));
    }

    void je(Label label) {
        insn("je", new DirectMemoryReference(label.symbol()));
    }

    void jne(Label label) {
        insn("jne", new DirectMemoryReference(label.symbol()));
    }

    void cmp(Type t, Operand a, Register b) {
        insn(t, "cmp", a, b);
    }

    void sete(Register reg) {
        insn("sete", reg);
    }

    void setne(Register reg) {
        insn("setne", reg);
    }

    void seta(Register reg) {
        insn("seta", reg);
    }

    void setae(Register reg) {
        insn("setae", reg);
    }

    void setb(Register reg) {
        insn("setb", reg);
    }

    void setbe(Register reg) {
        insn("setbe", reg);
    }

    void setg(Register reg) {
        insn("setg", reg);
    }

    void setge(Register reg) {
        insn("setge", reg);
    }

    void setl(Register reg) {
        insn("setl", reg);
    }

    void setle(Register reg) {
        insn("setle", reg);
    }

    void test(Type type, Register a, Register b) {
        insn(type, "test", a, b);
    }

    void push(Register reg) {
        insn("push", typeSuffix(naturalType), reg);
    }

    void pop(Register reg) {
        insn("pop", typeSuffix(naturalType), reg);
    }

    // call function by relative address
    void call(Symbol sym) {
        insn("call", new DirectMemoryReference(sym));
    }

    // call function by absolute address
    void callAbsolute(Register reg) {
        insn("call", new AbsoluteAddress(reg));
    }

    void ret() {
        insn("ret");
    }

    void mov(Operand src, Operand dest) {
        mov(naturalType, src, dest);
    }

    // for stack access
    void relocatableMov(Operand src, Operand dest) {
        assemblies.add(new Instruction("mov", typeSuffix(naturalType), src, dest, true));
    }

    void mov(Type type, Operand src, Operand dest) {
        insn(type, "mov", src, dest);
    }

    void movsx(Type t1, Type t2, Operand src, Operand dest) {
        insn("movs", typeSuffix(t1, t2), src, dest);
    }

    void movsbl(Operand src, Operand dest) {
        insn("movs", "bl", src, dest);
    }

    void movswl(Operand src, Operand dest) {
        insn("movs", "wl", src, dest);
    }

    void movzx(Type t1, Type t2, Operand src, Operand dest) {
        insn("movz", typeSuffix(t1, t2), src, dest);
    }

    void movzb(Type t, Operand src, Operand dest) {
        insn("movz", "b" + typeSuffix(t), src, dest);
    }

    void movzbl(Operand src, Operand dest) {
        insn("movz", "bl", src, dest);
    }

    void movzwl(Operand src, Operand dest) {
        insn("movz", "wl", src, dest);
    }

    void lea(Operand src, Operand dest) {
        lea(naturalType, src, dest);
    }

    void lea(Type type, Operand src, Operand dest) {
        insn(type, "lea", src, dest);
    }

    void neg(Type type, Register reg) {
        insn(type, "neg", reg);
    }

    void inc(Type type, Operand reg) {
        insn(type, "inc", reg);
    }

    void dec(Type type, Operand reg) {
        insn(type, "dec", reg);
    }

    void add(Operand diff, Operand base) {
        add(naturalType, diff, base);
    }

    void add(Type type, Operand diff, Operand base) {
        insn(type, "add", diff, base);
    }

    void sub(Operand diff, Operand base) {
        sub(naturalType, diff, base);
    }

    void sub(Type type, Operand diff, Operand base) {
        insn(type, "sub", diff, base);
    }

    void imul(Operand m, Register base) {
        imul(naturalType, m, base);
    }

    void imul(Type type, Operand m, Register base) {
        insn(type, "imul", m, base);
    }

    void cltd() {
        insn("cltd");
    }

    void div(Type type, Register base) {
        insn(type, "div", base);
    }

    void idiv(Type type, Register base) {
        insn(type, "idiv", base);
    }

    void not(Type type, Register reg) {
        insn(type, "not", reg);
    }

    void and(Type type, Operand bits, Register base) {
        insn(type, "and", bits, base);
    }

    void or(Type type, Operand bits, Register base) {
        insn(type, "or", bits, base);
    }

    void xor(Type type, Operand bits, Register base) {
        insn(type, "xor", bits, base);
    }

    void sar(Type type, Register bits, Register base) {
        insn(type, "sar", bits, base);
    }

    void sal(Type type, Register bits, Register base) {
        insn(type, "sal", bits, base);
    }

    void shr(Type type, Register bits, Register base) {
        insn(type, "shr", bits, base);
    }
}
