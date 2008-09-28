package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

abstract public class Scope {
    protected List<LocalScope> children;

    public Scope() {
        children = new ArrayList<LocalScope>();
    }

    abstract public boolean isToplevel();
    abstract public ToplevelScope toplevel();
    abstract public Scope parent();

    protected void addChild(LocalScope s) {
        children.add(s);
    }

    abstract public Entity get(String name) throws SemanticException;
}
