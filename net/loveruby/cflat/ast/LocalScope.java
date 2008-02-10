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
            LocalScope s = (LocalScope)scopes.next();
            result.addAll(s.variablesList());
        }
        return result;
    }

    public List staticLocalVariables() {
        List result = new ArrayList();
        Iterator scopes = allChildren();
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
