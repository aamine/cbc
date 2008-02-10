package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class Scope {
    protected Scope parent;
    protected List children;
    protected List entities;
    protected Map entitiesMap;
    protected long numAllEntities;

    public Scope(Scope up) {
        parent = up;
        if (up != null) up.addChild(this);
        children = new ArrayList();
        numAllEntities = -1;
        entities = new ArrayList();
        entitiesMap = new HashMap();
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
        if (entitiesMap.containsKey(ent.name())) {
            throw new Error("duplicated entity: " + ent.name());
        }
        entities.add(ent);
        entitiesMap.put(ent.name(), ent);
    }
    // #@@}

    public boolean isDefinedLocally(String name) {
        return entitiesMap.containsKey(name);
    }

    // #@@range/get{
    public Entity get(String name) throws SemanticException {
        Entity ent = (Entity)entitiesMap.get(name);
        if (ent != null) {
            return ent;
        }
        else {
            return parent.get(name);
        }
    }
    // #@@}

    // Returns all function local variables defined in this scope.
    // Does includes all nested local variables.
    // Does NOT include static local variables.
    public Iterator allVariables() {
        return allEntities().iterator();
    }

    public Iterator variables() {
        return entities();
    }

    public long numEntities() {
        return entities.size();
    }

    // Returns local variables defined in this scope itself.
    // Does NOT include nested local variables.
    // Does NOT include static local variables.
    public Iterator entities() {
        return entities.iterator();
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

    protected List allEntities() {
        List result = new ArrayList();
        Iterator cs = allChildren();
        while (cs.hasNext()) {
            Scope c = (Scope)cs.next();
            result.addAll(c.entities);
        }
        return result;
    }

    public void checkReferences(ErrorHandler h) {
        Iterator ents = entities.iterator();
        while (ents.hasNext()) {
            Entity ent = (Entity)ents.next();
            if (ent.isDefined() && ent.isPrivate() && !ent.isRefered()) {
                h.warn(ent.location(), "unused variable: " + ent.name());
            }
        }
        Iterator cs = children.iterator();
        while (cs.hasNext()) {
            Scope s = (Scope)cs.next();
            s.checkReferences(h);
        }
    }
}
