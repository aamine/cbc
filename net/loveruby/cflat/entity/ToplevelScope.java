package net.loveruby.cflat.entity;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.utils.ErrorHandler;
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
    public void declareEntity(Entity entity) throws SemanticException {
        Entity e = entities.get(entity.name());
        if (e != null) {
            throw new SemanticException("duplicated declaration: " +
                    entity.name() + ": " +
                    e.location() + " and " + entity.location());
        }
        entities.put(entity.name(), entity);
    }
    // #@@}

    /** Define variable or function globally. */
    // #@@range/defineEntity{
    public void defineEntity(Entity entity) throws SemanticException {
        Entity e = entities.get(entity.name());
        if (e != null && e.isDefined()) {
            throw new SemanticException("duplicated definition: " +
                    entity.name() + ": " +
                    e.location() + " and " + entity.location());
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

    public List<DefinedVariable> definedGlobalScopeVariables() {
        List<DefinedVariable> result = new ArrayList<DefinedVariable>();
        for (Entity ent : entities.values()) {
            if (ent instanceof DefinedVariable) {
                result.add((DefinedVariable)ent);
            }
        }
        result.addAll(staticLocalVariables());
        return result;
    }

    public List<DefinedVariable> staticLocalVariables() {
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
            if (ent.isDefined()
                    && ent.isPrivate()
                    && !ent.isConstant()
                    && !ent.isRefered()) {
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
