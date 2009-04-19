package net.loveruby.cflat.ast;
import java.io.*;

abstract public class Node {
    public Node() {
    }

    abstract public Location location();

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream s) {
        dump(new Dumper(s));
    }

    public void dump(Dumper d) {
        d.printClass(this, location());
        _dump(d);
    }

    abstract protected void _dump(Dumper d);
}
