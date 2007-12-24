package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.TypeTable;
import java.util.*;

abstract public class Params extends Node {
    public void accept(ASTVisitor visitor) {
        throw new Error("do not use Params#accept");
    }

    abstract public Iterator parameters();
    abstract public boolean isVararg();
    abstract public boolean equals(Object other);
    abstract public Params internTypes(TypeTable table);
    abstract public Params typeRefs();
}
