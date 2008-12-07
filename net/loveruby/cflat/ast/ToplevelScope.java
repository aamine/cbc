package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class ToplevelScope extends Scope {
    protected Map<String, Entity> entities;
    protected List<DefinedVariable> staticLocalVariables;   // cache

    public ToplevelScope() {
        super();
        entities = new LinkedHashMap<String, Entity>();
        staticLocalVariables = null;
    }

    public boolean isToplevel() {
        return true;
    }

    public ToplevelScope toplevel() {
        return this;
    }

    public Scope parent() {
        return null;
    }

    /** Declare variable or function globally. */
    // #@@range/declareEntity{
    public void declareEntity(Entity ent) {
        if (entities.containsKey(ent.name())) {
            throw new Error("duplicated declaration: " + ent.name());
        }
        entities.put(ent.name(), ent);
    }
    // #@@}

    /** Define variable or function globally. */
    // #@@range/defineEntity{
    public void defineEntity(Entity entity) {
        Entity ent = entities.get(entity.name());
        if (ent != null && ent.isDefined()) {
            throw new Error("duplicated definition: " + entity.name() + ": " +
                            ent.location() + " and " + entity.location());
        }
        entities.put(entity.name(), entity);
    }
    // #@@}

    /** Searches and gets entity searching scopes upto ToplevelScope. */
    // #@@range/get{
    public Entity get(String name) throws SemanticException {
        Entity ent = entities.get(name);
        if (ent == null) {
            throw new SemanticException("unresolved reference: " + name);
        }
        return ent;
    }
    // #@@}

    /** Returns a list of all global variables.
     * "All global variable" means:
     *
     *    * has global scope
     *    * defined or undefined
     *    * public or private
     */
    public List<Variable> allGlobalVariables() {
        List<Variable> result = new ArrayList<Variable>();
        for (Entity ent : entities.values()) {
            if (ent instanceof Variable) {
                result.add((Variable)ent);
            }
        }
        result.addAll(staticLocalVariables());
        return result;
    }

    /** Returns the list of global variables.
     *  A global variable is a variable which has
     *  global scope and is initialized.  */
    public List<DefinedVariable> definedGlobalVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (DefinedVariable var : definedGlobalScopeVariables()) {
            if (var.hasInitializer()) {
                result.add(var);
            }
        }
        return result;
    }

    /** Returns the list of common symbols.
     *  A common symbol is a variable which has
     *  global scope and is not initialized.  */
    public List<DefinedVariable> definedCommonSymbols() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (DefinedVariable var : definedGlobalScopeVariables()) {
            if (!var.hasInitializer()) {
                result.add(var);
            }
        }
        return result;
    }

    protected List<DefinedVariable> definedGlobalScopeVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (Entity ent : entities.values()) {
            if (ent instanceof DefinedVariable) {
                result.add((DefinedVariable)ent);
            }
        }
        result.addAll(staticLocalVariables());
        return result;
    }

    protected List<DefinedVariable> staticLocalVariables() {
        if (staticLocalVariables == null) {
            staticLocalVariables = new ArrayList<DefinedVariable>();
            for (LocalScope s : children) {
                staticLocalVariables.addAll(s.staticLocalVariables());
            }
            Map<String, Integer> seqTable = new HashMap<String, Integer>();
            for (DefinedVariable var : staticLocalVariables) {
                Integer seq = seqTable.get(var.name());
                if (seq == null) {
                    var.setSequence(0);
                    seqTable.put(var.name(), 1);
                }
                else {
                    var.setSequence(seq);
                    seqTable.put(var.name(), seq + 1);
                }
            }
        }
        return staticLocalVariables;
    }

    public void checkReferences(ErrorHandler h) {
        for (Entity ent : entities.values()) {
            if (ent.isDefined() && ent.isPrivate() && !ent.isRefered()) {
                h.warn(ent.location(), "unused variable: " + ent.name());
            }
        }
        // do not check parameters
        for (LocalScope funcScope : children) {
            for (LocalScope s : funcScope.children) {
                s.checkReferences(h);
            }
        }
    }
}
