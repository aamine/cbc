package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class LocalScope extends Scope {
    protected long numAllEntities;

    public LocalScope(Scope up) {
        super(up);
        numAllEntities = -1;
    }

    public boolean isToplevel() {
        return false;
    }

    public Iterator children() {
        return children.iterator();
    }

    /**
     * Returns local variables defined in this scope.
     * Does NOT includes children's local variables.
     * Does NOT include static local variables.
     */
    public Iterator variables() {
        return lvarList().iterator();
    }

    /**
     * Returns local variables defined in this scope.
     * Does NOT includes children's local variables.
     * Does NOT include static local variables.
     */
    protected List lvarList() {
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

    /**
     * Returns the number of all entities in this scope.
     * Result includes the number of children's entities.
     */
    public long numAllEntities() {
        if (numAllEntities < 0) {
            Iterator cs = allScopes().iterator();
            long n = 0;
            while (cs.hasNext()) {
                Scope c = (Scope)cs.next();
                n += c.numEntities();
            }
            numAllEntities = n;
        }
        return numAllEntities;
    }

    /**
     * Returns all local variables in this scope.
     * The result DOES includes all nested local variables,
     * while it does NOT include static local variables.
     */
    public Iterator allLocalVariables() {
        return allLocalVariablesList().iterator();
    }

    // List<DefinedVariable>
    protected List allLocalVariablesList() {
        List result = new ArrayList();
        Iterator scopes = allScopes().iterator();
        while (scopes.hasNext()) {
            LocalScope s = (LocalScope)scopes.next();
            result.addAll(s.lvarList());
        }
        return result;
    }

    /**
     * Returns all static local variables defined in this scope.
     */
    public List staticLocalVariables() {
        List result = new ArrayList();
        Iterator scopes = allScopes().iterator();
        while (scopes.hasNext()) {
            LocalScope s = (LocalScope)scopes.next();
            Iterator vars = s.entities.values().iterator();
            while (vars.hasNext()) {
                DefinedVariable var = (DefinedVariable)vars.next();
                if (var.isPrivate()) {
                    result.add(var);
                }
            }
        }
        return result;
    }
}
