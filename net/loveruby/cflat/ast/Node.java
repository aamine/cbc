package net.loveruby.cflat.ast;
import java.io.*;

abstract public class Node implements Dumpable {
    public Node() {
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream s) {
        dump(new Dumper(s));
    }

    public void dump(Dumper d) {
        d.printClass(this);
        _dump(d);
    }

    abstract void _dump(Dumper d);

    abstract public void accept(ASTVisitor visitor);
}
