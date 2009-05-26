package net.loveruby.cflat.sysdep.x86;
import net.loveruby.cflat.asm.*;
import net.loveruby.cflat.utils.TextUtils;
import java.util.*;

public class AssemblyFile {
    private List<Assembly> assemblies;
    private Type naturalType;
    private long stackWordSize;
    private boolean verbose;
    private int commentIndentLevel;

    public AssemblyFile(
            Type naturalType, long stackWordSize, boolean verbose) {
        this.naturalType = naturalType;
        this.stackWordSize = stackWordSize;
        this.verbose = verbose;
        this.assemblies = new ArrayList<Assembly>();
        this.commentIndentLevel = 0;
        initVirtualStack();
    }

    public List<Assembly> assemblies() {
        return this.assemblies;
    }

    public void addAll(List<Assembly> assemblies) {
        this.assemblies.addAll(assemblies);
    }

    public String toSource(SymbolTable symbolTable) {
        StringBuffer buf = new StringBuffer();
        for (Assembly asm : assemblies) {
            buf.append(asm.toSource(symbolTable));
            buf.append("\n");
        }
        return buf.toString();
    }

    public void comment(String str) {
        assemblies.add(new Comment(str, commentIndentLevel));
    }

    public void indentComment() {
        commentIndentLevel++;
    }

    public void unindentComment() {
        commentIndentLevel--;
    }

    public void label(Symbol sym) {
        assemblies.add(new Label(sym));
    }

    public void label(Label label) {
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

    public void _file(String name) {
        directive(".file\t" + TextUtils.dumpString(name));
    }

    public void _text() {
        directive("\t.text");
    }

    public void _data() {
        directive("\t.data");
    }

    public void _section(String name) {
        directive("\t.section\t" + name);
    }

    public void _section(String name, String flags, String type, String group, String linkage) {
        directive("\t.section\t" + name + "," + flags + "," + type + "," + group + "," + linkage);
    }

    public void _globl(Symbol sym) {
        directive(".globl " + sym.name());
    }

    public void _local(Symbol sym) {
        directive(".local " + sym.name());
    }

    public void _hidden(Symbol sym) {
        directive("\t.hidden\t" + sym.name());
    }

    public void _comm(Symbol sym, long size, long alignment) {
        directive("\t.comm\t" + sym.name() + "," + size + "," + alignment);
    }

    public void _align(long n) {
        directive("\t.align\t" + n);
    }

    public void _type(Symbol sym, String type) {
        directive("\t.type\t" + sym.name() + "," + type);
    }

    public void _size(Symbol sym, long size) {
        _size(sym, new Long(size).toString());
    }

    public void _size(Symbol sym, String size) {
        directive("\t.size\t" + sym.name() + "," + size);
    }

    public void _byte(long val) {
        directive(".byte\t" + new IntegerLiteral((byte)val).toSource());
    }

    public void _value(long val) {
        directive(".value\t" + new IntegerLiteral((short)val).toSource());
    }

    public void _long(long val) {
        directive(".long\t" + new IntegerLiteral((int)val).toSource());
    }

    public void _quad(long val) {
        directive(".quad\t" + new IntegerLiteral(val).toSource());
    }

    public void _byte(Literal val) {
        directive(".byte\t" + val.toSource());
    }

    public void _value(Literal val) {
        directive(".value\t" + val.toSource());
    }

    public void _long(Literal val) {
        directive(".long\t" + val.toSource());
    }

    public void _quad(Literal val) {
        directive(".quad\t" + val.toSource());
    }

    public void _string(String str) {
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

    public void jmp(Label label) {
        insn("jmp", new DirectMemoryReference(label.symbol()));
    }

    public void jz(Label label) {
        insn("jz", new DirectMemoryReference(label.symbol()));
    }

    public void jnz(Label label) {
        insn("jnz", new DirectMemoryReference(label.symbol()));
    }

    public void je(Label label) {
        insn("je", new DirectMemoryReference(label.symbol()));
    }

    public void jne(Label label) {
        insn("jne", new DirectMemoryReference(label.symbol()));
    }

    public void cmp(Type t, Operand a, Register b) {
        insn(t, "cmp", a, b);
    }

    public void sete(Register reg) {
        insn("sete", reg);
    }

    public void setne(Register reg) {
        insn("setne", reg);
    }

    public void seta(Register reg) {
        insn("seta", reg);
    }

    public void setae(Register reg) {
        insn("setae", reg);
    }

    public void setb(Register reg) {
        insn("setb", reg);
    }

    public void setbe(Register reg) {
        insn("setbe", reg);
    }

    public void setg(Register reg) {
        insn("setg", reg);
    }

    public void setge(Register reg) {
        insn("setge", reg);
    }

    public void setl(Register reg) {
        insn("setl", reg);
    }

    public void setle(Register reg) {
        insn("setle", reg);
    }

    public void test(Type type, Register a, Register b) {
        insn(type, "test", a, b);
    }

    public void push(Register reg) {
        insn("push", typeSuffix(naturalType), reg);
    }

    public void pop(Register reg) {
        insn("pop", typeSuffix(naturalType), reg);
    }

    // call function by relative address
    public void call(Symbol sym) {
        insn("call", new DirectMemoryReference(sym));
    }

    // call function by absolute address
    public void callAbsolute(Register reg) {
        insn("call", new AbsoluteAddress(reg));
    }

    public void ret() {
        insn("ret");
    }

    public void mov(Operand src, Operand dest) {
        mov(naturalType, src, dest);
    }

    // for stack access
    public void relocatableMov(Operand src, Operand dest) {
        assemblies.add(new Instruction("mov", typeSuffix(naturalType), src, dest, true));
    }

    public void mov(Type type, Operand src, Operand dest) {
        insn(type, "mov", src, dest);
    }

    public void movsx(Type t1, Type t2, Operand src, Operand dest) {
        insn("movs", typeSuffix(t1, t2), src, dest);
    }

    public void movsbl(Operand src, Operand dest) {
        insn("movs", "bl", src, dest);
    }

    public void movswl(Operand src, Operand dest) {
        insn("movs", "wl", src, dest);
    }

    public void movzx(Type t1, Type t2, Operand src, Operand dest) {
        insn("movz", typeSuffix(t1, t2), src, dest);
    }

    public void movzb(Type t, Operand src, Operand dest) {
        insn("movz", "b" + typeSuffix(t), src, dest);
    }

    public void movzbl(Operand src, Operand dest) {
        insn("movz", "bl", src, dest);
    }

    public void movzwl(Operand src, Operand dest) {
        insn("movz", "wl", src, dest);
    }

    public void lea(Operand src, Operand dest) {
        lea(naturalType, src, dest);
    }

    public void lea(Type type, Operand src, Operand dest) {
        insn(type, "lea", src, dest);
    }

    public void neg(Type type, Register reg) {
        insn(type, "neg", reg);
    }

    public void inc(Type type, Operand reg) {
        insn(type, "inc", reg);
    }

    public void dec(Type type, Operand reg) {
        insn(type, "dec", reg);
    }

    public void add(Operand diff, Operand base) {
        add(naturalType, diff, base);
    }

    public void add(Type type, Operand diff, Operand base) {
        insn(type, "add", diff, base);
    }

    public void sub(Operand diff, Operand base) {
        sub(naturalType, diff, base);
    }

    public void sub(Type type, Operand diff, Operand base) {
        insn(type, "sub", diff, base);
    }

    public void imul(Operand m, Register base) {
        imul(naturalType, m, base);
    }

    public void imul(Type type, Operand m, Register base) {
        insn(type, "imul", m, base);
    }

    public void cltd() {
        insn("cltd");
    }

    public void div(Type type, Register base) {
        insn(type, "div", base);
    }

    public void idiv(Type type, Register base) {
        insn(type, "idiv", base);
    }

    public void not(Type type, Register reg) {
        insn(type, "not", reg);
    }

    public void and(Type type, Operand bits, Register base) {
        insn(type, "and", bits, base);
    }

    public void or(Type type, Operand bits, Register base) {
        insn(type, "or", bits, base);
    }

    public void xor(Type type, Operand bits, Register base) {
        insn(type, "xor", bits, base);
    }

    public void sar(Type type, Register bits, Register base) {
        insn(type, "sar", bits, base);
    }

    public void sal(Type type, Register bits, Register base) {
        insn(type, "sal", bits, base);
    }

    public void shr(Type type, Register bits, Register base) {
        insn(type, "shr", bits, base);
    }
}
