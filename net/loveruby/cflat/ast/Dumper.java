package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.*;
import java.util.*;
import java.io.*;

public class Dumper {
    protected int nIndent;
    protected PrintStream stream;

    public Dumper(PrintStream s) {
        this.stream = s;
        this.nIndent = 0;
    }

    public void printClass(Object obj, Location loc) {
        printIndent();
        stream.println("<<" + obj.getClass().getSimpleName() + ">> (" + loc + ")");
    }

    public void printNodeList(String name, List<? extends Dumpable> nodes) {
        printIndent();
        stream.println(name + ":");
        indent();
        for (Dumpable n : nodes) {
            n.dump(this);
        }
        unindent();
    }

    public void printMember(String name, int n) {
        printPair(name, "" + n);
    }

    public void printMember(String name, long n) {
        printPair(name, "" + n);
    }

    public void printMember(String name, boolean b) {
        printPair(name, "" + b);
    }

    public void printMember(String name, TypeRef ref) {
        printPair(name, ref.toString());
    }

    public void printMember(String name, Type t) {
        printPair(name, (t == null ? "null" : t.toString()));
    }

    public void printMember(String name, String str, boolean isResolved) {
        printPair(name, TextUtils.dumpString(str)
                        + (isResolved ? " (resolved)" : ""));
    }

    public void printMember(String name, String str) {
        printMember(name, str, false);
    }

    protected void printPair(String name, String value) {
        printIndent();
        stream.println(name + ": " + value);
    }

    public void printMember(String name, TypeNode n) {
        printIndent();
        stream.println(name + ": " + n.typeRef()
                       + (n.isResolved() ? " (resolved)" : ""));
    }

    public void printMember(String name, Dumpable n) {
        printIndent();
        if (n == null) {
            stream.println(name + ": null");
        }
        else {
            stream.println(name + ":");
            indent();
            n.dump(this);
            unindent();
        }
    }

    protected void indent() { nIndent++; }
    protected void unindent() { nIndent--; }

    static final protected String indentString = "    ";

    protected void printIndent() {
        int n = nIndent;
        while (n > 0) {
            stream.print(indentString);
            n--;
        }
    }
}
