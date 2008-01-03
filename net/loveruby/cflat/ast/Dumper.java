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

    public void printClass(Object obj) {
        printIndent();
        stream.println(className(obj));
    }

    protected String className(Object obj) {
        String[] ids = obj.getClass().getName().split("\\.");
        return "<<" + ids[ids.length - 1] + ">>";
    }

    public void printNodeList(String name, List list) {
        printNodeList(name, list.iterator());
    }

    public void printNodeList(String name, Iterator list) {
        printIndent();
        stream.println(name + ":");
        indent();
        while (list.hasNext()) {
            Dumpable i = (Dumpable)list.next();
            i.dump(this);
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
        printPair(name, (t == null ? "null" : t.textize()));
    }

    public void printMember(String name, String str) {
        printPair(name, TextUtils.escapeString(str));
    }

    protected void printPair(String name, String value) {
        printIndent();
        stream.println(name + ": " + value);
    }

    public void printMember(String name, TypeNode n) {
        printIndent();
        stream.println(name + ": " + n.typeRef());
    }

    public void printMember(String name, Node n) {
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
