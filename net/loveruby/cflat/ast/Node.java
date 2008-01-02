package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.*;

abstract public class Node {
    public Node() {
    }

    abstract public void accept(ASTVisitor visitor);
}
