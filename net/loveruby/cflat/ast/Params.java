package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.TypeTable;
import java.util.*;

abstract public class Params extends Node {
    public Params() {
        super();
    }

    abstract public Iterator parameters();
    abstract public boolean isVararg();
    abstract public int argc();
    abstract public int minArgc();
    abstract public boolean equals(Object other);
    abstract public Params internTypes(TypeTable table);
    abstract public Params typeRefs();

    protected void _dump(Dumper d) {
        d.printNodeList("parameters", parameters());
    }

    public void accept(ASTVisitor visitor) {
        throw new Error("do not use Params#accept");
    }
}
