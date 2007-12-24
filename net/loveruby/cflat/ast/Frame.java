package net.loveruby.cflat.ast;
import java.util.*;

public class Frame extends Scope {
    public Frame(ToplevelScope up) {
        super(up);
    }

    public void allocatePrivateVariable(Variable var) {
        throw new Error("Frame#allocatePrivateVariable called");
    }

    public long numLocalVariables() {
        return bodyScope().numAllEntities();
    }

    public Iterator localVariables() {
        return bodyScope().entities();
    }

    private Scope bodyScope() {
        return (Scope)children.get(0);
    }
}
