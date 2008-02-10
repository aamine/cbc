package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class Scope {
    protected Scope parent;
    protected List children;
    protected Map entities;
    protected long numAllEntities;

    public Scope(Scope up) {
        parent = up;
        if (up != null) up.addChild(this);
        children = new ArrayList();
        numAllEntities = -1;
        entities = new LinkedHashMap();
    }

    public boolean isToplevel() {
        return false;
    }

    public ToplevelScope toplevel() {
        Scope s = this;
        while (!s.isToplevel()) {
            s = s.parent;
        }
        return (ToplevelScope)s;
    }

    // Returns parent scope.
    // If this scope is a TopScope, parent() returns null.
    public Scope parent() {
        return parent;
    }

    protected void addChild(Scope s) {
        children.add(s);
    }

    // Returns a list of all child scopes.
    // Does NOT include myself.
    protected Iterator allChildren() {
        List result = new ArrayList();
        collectChildren(result);
        return result.iterator();
    }

    protected void collectChildren(List buf) {
        Iterator cs = children.iterator();
        while (cs.hasNext()) {
            Scope c = (Scope)cs.next();
            buf.add(c);
            c.collectChildren(buf);
        }
    }

    /** Declare variable or function in this scope. */
    // #@@range/declareEntity{
    public void declareEntity(Entity ent) {
        if (entities.containsKey(ent.name())) {
            throw new Error("duplicated entity: " + ent.name());
        }
        entities.put(ent.name(), ent);
    }
    // #@@}

    public boolean isDefinedLocally(String name) {
        return entities.containsKey(name);
    }

    // #@@range/get{
    public Entity get(String name) throws SemanticException {
        Entity ent = (Entity)entities.get(name);
        if (ent != null) {
            return ent;
        }
        else {
            return parent.get(name);
        }
    }
    // #@@}

    public long numEntities() {
        return entities.size();
    }

    // Returns local variables defined in this scope.
    // Does includes all nested local variables.
    // Does NOT include static local variables.
    public Iterator variables() {
        return variablesList().iterator();
    }

    protected List variablesList() {
        List result = new ArrayList();
        Iterator ents = entities.values().iterator();
        while (ents.hasNext()) {
            Entity ent = (Entity)ents.next();
            if (ent instanceof DefinedVariable) {
                DefinedVariable var = (DefinedVariable)ent;
                if (!var.isPrivate()) {
                    result.add(var);
                }
            }
        }
        return result;
    }

    public long numAllEntities() {
        if (numAllEntities < 0) {
            Iterator cs = allChildren();
            long n = 0;
            while (cs.hasNext()) {
                Scope c = (Scope)cs.next();
                n += c.numEntities();
            }
            numAllEntities = n;
        }
        return numAllEntities;
    }

    // Returns all function local variables defined in this scope.
    // Does includes all nested local variables.
    // Does NOT include static local variables.
    public Iterator allVariables() {
        return allEntities().iterator();
    }

    protected List allEntities() {
        List result = new ArrayList();
        Iterator scopes = allChildren();
        while (scopes.hasNext()) {
            Scope s = (Scope)scopes.next();
            result.addAll(s.variablesList());
        }
        return result;
    }

    public void checkReferences(ErrorHandler h) {
        Iterator ents = entities.values().iterator();
        while (ents.hasNext()) {
            Entity ent = (Entity)ents.next();
            if (ent.isDefined() && ent.isPrivate() && !ent.isRefered()) {
                h.warn(ent.location(), "unused variable: " + ent.name());
            }
        }
        Iterator scopes = children.iterator();
        while (scopes.hasNext()) {
            Scope s = (Scope)scopes.next();
            s.checkReferences(h);
        }
    }
}
