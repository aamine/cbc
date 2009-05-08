package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.asm.*;
import java.util.List;
import java.io.PrintStream;

public class Dumper {
    PrintStream stream;
    private int numIndent;

    Dumper(PrintStream s) {
        stream = s;
        numIndent = 0;
    }

    public void printClass(Object obj) {
        printIndent();
        stream.println("<<" + obj.getClass().getSimpleName() + ">>");
    }

    public void printClass(Object obj, Location loc) {
        printIndent();
        stream.println("<<" + obj.getClass().getSimpleName() + ">> (" + loc + ")");
    }

    public void printMember(String name, int memb) {
        printPair(name, "" + memb);
    }

    public void printMember(String name, long memb) {
        printPair(name, "" + memb);
    }

    public void printMember(String name, boolean memb) {
        printPair(name, "" + memb);
    }

    public void printMember(String name, String memb) {
        printPair(name, memb);
    }

    public void printMember(String name, Label memb) {
        printPair(name, Integer.toHexString(memb.hashCode()));
    }

    public void printMember(String name, net.loveruby.cflat.asm.Type memb) {
        printPair(name, memb.toString());
    }

    public void printMember(String name, net.loveruby.cflat.type.Type memb) {
        printPair(name, memb.toString());
    }

    private void printPair(String name, String value) {
        printIndent();
        stream.println(name + ": " + value);
    }

    public void printMember(String name, Dumpable memb) {
        printIndent();
        if (memb == null) {
            stream.println(name + ": null");
        }
        else {
            stream.println(name + ":");
            indent();
            memb.dump(this);
            unindent();
        }
    }

    public void printMembers(String name, List<? extends Dumpable> elems) {
        printIndent();
        stream.println(name + ":");
        indent();
        for (Dumpable elem : elems) {
            elem.dump(this);
        }
        unindent();
    }

    public void printVars(String name, List<DefinedVariable> vars) {
        printIndent();
        stream.println(name + ":");
        indent();
        for (DefinedVariable var : vars) {
            printClass(var, var.location());
            printMember("name", var.name());
            printMember("isPrivate", var.isPrivate());
            printMember("type", var.type());
            printMember("initializer", var.ir());
        }
        unindent();
    }

    public void printFuncs(String name, List<DefinedFunction> funcs) {
        printIndent();
        stream.println(name + ":");
        indent();
        for (DefinedFunction f : funcs) {
            printClass(f, f.location());
            printMember("name", f.name());
            printMember("isPrivate", f.isPrivate());
            printMember("type", f.type());
            printMembers("body", f.ir());
        }
        unindent();
    }

    private void indent() { numIndent++; }
    private void unindent() { numIndent--; }

    static final private String indentString = "    ";

    private void printIndent() {
        int n = numIndent;
        while (n > 0) {
            stream.print(indentString);
            n--;
        }
    }
}
