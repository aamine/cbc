package net.loveruby.cflat.entity;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class LocalScope extends Scope {
    protected Scope parent;
    protected Map<String, DefinedVariable> variables;

    public LocalScope(Scope parent) {
        super();
        this.parent = parent;
        parent.addChild(this);
        variables = new LinkedHashMap<String, DefinedVariable>();
    }

    public boolean isToplevel() {
        return false;
    }

    public ToplevelScope toplevel() {
        return parent.toplevel();
    }

    public Scope parent() {
        return this.parent;
    }

    public List<LocalScope> children() {
        return children;
    }

    public boolean isDefinedLocally(String name) {
        return variables.containsKey(name);
    }

    /** Define variable in this scope. */
    // #@@range/defineVariable{
    public void defineVariable(DefinedVariable var) {
        if (variables.containsKey(var.name())) {
            throw new Error("duplicated variable: " + var.name());
        }
        variables.put(var.name(), var);
    }
    // #@@}

    public DefinedVariable allocateTmp(Type t) {
        DefinedVariable var = DefinedVariable.tmp(t);
        defineVariable(var);
        return var;
    }

    // #@@range/get{
    public Entity get(String name) throws SemanticException {
        DefinedVariable var = variables.get(name);
        if (var != null) {
            return var;
        }
        else {
            return parent.get(name);
        }
    }
    // #@@}

    /**
     * Returns all local variables in this scope.
     * The result DOES includes all nested local variables,
     * while it does NOT include static local variables.
     */
    public List<DefinedVariable> allLocalVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (LocalScope s : allLocalScopes()) {
            result.addAll(s.localVariables());
        }
        return result;
    }

    /**
     * Returns local variables defined in this scope.
     * Does NOT includes children's local variables.
     * Does NOT include static local variables.
     */
    public List<DefinedVariable> localVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (DefinedVariable var : variables.values()) {
            if (!var.isPrivate()) {
                result.add(var);
            }
        }
        return result;
    }

    /**
     * Returns all static local variables defined in this scope.
     */
    public List<DefinedVariable> staticLocalVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (LocalScope s : allLocalScopes()) {
            for (DefinedVariable var : s.variables.values()) {
                if (var.isPrivate()) {
                    result.add(var);
                }
            }
        }
        return result;
    }

    // Returns a list of all child scopes including this scope.
    protected List<LocalScope> allLocalScopes() {
        List<LocalScope> result = new ArrayList<LocalScope>();
        collectScope(result);
        return result;
    }

    protected void collectScope(List<LocalScope> buf) {
        buf.add(this);
        for (LocalScope s : children) {
            s.collectScope(buf);
        }
    }

    public void checkReferences(ErrorHandler h) {
        for (DefinedVariable var : variables.values()) {
            if (!var.isRefered()) {
                h.warn(var.location(), "unused variable: " + var.name());
            }
        }
        for (LocalScope c : children) {
            c.checkReferences(h);
        }
    }
}
