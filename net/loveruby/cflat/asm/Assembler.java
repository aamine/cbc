package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.*;
import java.util.*;

public class Assembler {
    protected List list;
    protected Type naturalType;

    public Assembler(Type naturalType) {
        this.list = new ArrayList();
        this.naturalType = naturalType;
    }

    public String string() {
        StringBuffer buf = new StringBuffer();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            buf.append((String)i.next());
            buf.append("\n");
        }
        return buf.toString();
    }

    protected void op(String op) {
        list.add("\t" + op);
    }

    protected void op(String op, AsmEntity ent) {
        list.add("\t" + op + "\t" + ent.toString());
    }

    protected void op(String op, AsmEntity ent1, AsmEntity ent2) {
        list.add("\t" + op + "\t" + ent1.toString() + ", " + ent2.toString());
    }

    protected void op(String op, String arg) {
        list.add("\t" + op + "\t" + arg);
    }

    protected void op(String op, String arg1, String arg2) {
        list.add("\t" + op + "\t" + arg1 + ", " + arg2);
    }

    protected void op(String op, String arg1, String arg2, String arg3) {
        list.add("\t" + op + "\t" + arg1 + ", " + arg2 + ", " + arg3);
    }

    public void comment(String str) {
        list.add("\t# " + str);
    }

    public void line(String str) {
        list.add(str);
    }

    public void _file(String name) {
        line("\t.file\t" + TextUtils.escapeString(name));
    }

    public void _text() {
        line("\t.text");
    }

    public void _data() {
        line("\t.data");
    }

    public void _section(String name) {
        line("\t.section\t" + name);
    }

    public void _globl(String sym) {
        line(".globl " + sym);
    }

    public void _local(String sym) {
        line(".local " + sym);
    }

    public void _comm(String sym, long size, long alignment) {
        line("\t.comm\t" + sym + "," + size + "," + alignment);
    }

    public void _align(long n) {
        line("\t.align\t" + n);
    }

    public void _type(String sym, String type) {
        line("\t.type\t" + sym + ", " + type);
    }

    public void _size(String sym, long size) {
        _size(sym, new Long(size).toString());
    }

    public void _size(String sym, String size) {
        line("\t.size\t" + sym + ", " + size);
    }

    public void _byte(long n) {
        line(".byte\t" + n);
    }

    public void _value(long n) {
        line(".value\t" + n);
    }

    public void _long(long n) {
        line(".long\t" + n);
    }

    public void _long(Label label) {
        line(".long\t" + label);
    }

    public void _quad(long n) {
        line(".quad\t" + n);
    }

    public void _quad(Label label) {
        line(".quad\t" + label);
    }

    public void _string(String str) {
        line("\t.string\t" + TextUtils.escapeString(str));
    }

    public void label(String label) {
        line(label + ":");
    }

    public void label(Label label) {
        line(label.toString() + ":");
    }

    public void jmp(Label label) {
        op("jmp", label);
    }

    public void jz(Label label) {
        op("jz", label);
    }

    public void jnz(Label label) {
        op("jnz", label);
    }

    public void je(Label label) {
        op("je", label);
    }

    public void jne(Label label) {
        op("jne", label);
    }

    protected void typedOp(Type t, String op, AsmEntity a) {
        op(addSuffix(op, t), a);
    }

    protected void typedOp(Type t, String op, AsmEntity a, AsmEntity b) {
        op(addSuffix(op, t), a, b);
    }

    protected String addSuffix(String op, Type t) {
        switch ((int)t.size()) {
        case 1: return op + "b";
        case 2: return op + "w";
        case 4: return op + "l";
        default:
            throw new Error("unknown type size: " + t.size());
        }
    }

    public void cmp(Type t, Register a, Register b) {
        typedOp(t, "cmp", a, b);
    }

    public void sete(Register reg) {
        op("sete", reg);
    }

    public void setne(Register reg) {
        op("setne", reg);
    }

    public void seta(Register reg) {
        op("seta", reg);
    }

    public void setae(Register reg) {
        op("setae", reg);
    }

    public void setb(Register reg) {
        op("setb", reg);
    }

    public void setbe(Register reg) {
        op("setbe", reg);
    }

    public void setg(Register reg) {
        op("setg", reg);
    }

    public void setge(Register reg) {
        op("setge", reg);
    }

    public void setl(Register reg) {
        op("setl", reg);
    }

    public void setle(Register reg) {
        op("setle", reg);
    }

    public void test(Type type, Register a, Register b) {
        typedOp(type, "test", a, b);
    }

    public void push(Register reg) {
        push(naturalType, reg);
    }

    public void push(Type t, Register reg) {
        typedOp(t, "push", reg);
    }

    public void pop(Register reg) {
        pop(naturalType, reg);
    }

    public void pop(Type t, Register reg) {
        op("popl", reg);
    }

    // call function by relative address
    public void call(String sym) {
        op("call", sym);
    }

    // call function by absolute address
    public void callAbsolute(Register reg) {
        op("call", "*" + reg.toString());
    }

    public void ret() {
        op("ret");
    }

    public void mov(AsmEntity src, AsmEntity dest) {
        mov(naturalType, src, dest);
    }

    public void mov(Type type, AsmEntity src, AsmEntity dest) {
        typedOp(type, "mov", src, dest);
    }

    public void movsbl(AsmEntity src, AsmEntity dest) {
        op("movsbl", src, dest);
    }

    public void movswl(AsmEntity src, AsmEntity dest) {
        op("movswl", src, dest);
    }

    public void movzb(Type type, AsmEntity src, AsmEntity dest) {
        typedOp(type, "movzb", src, dest);
    }

    public void movzbl(AsmEntity src, AsmEntity dest) {
        op("movzbl", src, dest);
    }

    public void movzwl(AsmEntity src, AsmEntity dest) {
        op("movzwl", src, dest);
    }

    public void lea(AsmEntity src, AsmEntity dest) {
        lea(naturalType, src, dest);
    }

    public void lea(Type type, AsmEntity src, AsmEntity dest) {
        typedOp(type, "lea", src, dest);
    }

    public void neg(Type type, Register reg) {
        typedOp(type, "neg", reg);
    }

    public void inc(Type type, AsmEntity reg) {
        typedOp(type, "inc", reg);
    }

    public void dec(Type type, AsmEntity reg) {
        typedOp(type, "dec", reg);
    }

    public void add(AsmEntity diff, AsmEntity base) {
        add(naturalType, diff, base);
    }

    public void add(Type type, AsmEntity diff, AsmEntity base) {
        typedOp(type, "add", diff, base);
    }

    public void sub(AsmEntity diff, AsmEntity base) {
        sub(naturalType, diff, base);
    }

    public void sub(Type type, AsmEntity diff, AsmEntity base) {
        typedOp(type, "sub", diff, base);
    }

    public void imul(AsmEntity m, Register base) {
        imul(naturalType, m, base);
    }

    public void imul(Type type, AsmEntity m, Register base) {
        typedOp(type, "imul", m, base);
    }

    public void cltd() {
        op("cltd");
    }

    public void div(Type type, Register base) {
        typedOp(type, "div", base);
    }

    public void idiv(Type type, Register base) {
        typedOp(type, "idiv", base);
    }

    public void not(Type type, Register reg) {
        typedOp(type, "not", reg);
    }

    public void and(Type type, Register bits, Register base) {
        typedOp(type, "and", bits, base);
    }

    public void or(Type type, Register bits, Register base) {
        typedOp(type, "or", bits, base);
    }

    public void xor(Type type, Register bits, Register base) {
        typedOp(type, "xor", bits, base);
    }

    public void sar(Type type, Register bits, Register base) {
        typedOp(type, "sar", bits, base);
    }

    public void sal(Type type, Register bits, Register base) {
        typedOp(type, "sal", bits, base);
    }

    public void shr(Type type, Register bits, Register base) {
        typedOp(type, "shr", bits, base);
    }
}
