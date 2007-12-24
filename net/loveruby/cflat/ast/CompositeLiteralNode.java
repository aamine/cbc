package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class CompositeLiteralNode extends Node {
    protected List members;
    protected Type type;

    public CompositeLiteralNode(List membs) {
        this.members = membs;
    }

    public Iterator members() {
        return members.iterator();
    }

    public Type type() {
        if (type == null) {
            throw new Error("CompositeLiteralNode#type == null");
        }
        return type;
    }

    public void setType(Type t) {
        if (type != null) {
            throw new Error("CompositeLiteralNode#setType called twice");
        }
        type = t;
    }

    public void accept(ASTVisitor visitor) {
        throw new Error("CompositeLiteralNode#accept called");
    }
}
