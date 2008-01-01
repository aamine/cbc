package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.*;

abstract public class Node {
    public Node() {
    }

    abstract public void accept(ASTVisitor visitor);

    public Type type() {
        throw new Error("Node#type called");
    }

    public AsmEntity address() {
        throw new Error("Node#address");
    }

    public boolean isCallable() {
        return type().isCallable();
    }

    public boolean isIndexable() {
        return type().isIndexable();
    }
}
