package net.loveruby.cflat.ast;
import java.util.*;

public class Frame extends Scope {
    public Frame(ToplevelScope up) {
        super(up);
    }

    public List staticLocalVariables() {
        List result = new ArrayList();
        Iterator scopes = allChildren();
        while (scopes.hasNext()) {
            Scope s = (Scope)scopes.next();
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

    public long numLocalVariables() {
        return bodyScope().numAllEntities();
    }

    public Iterator localVariables() {
        return bodyScope().variables();
    }

    private Scope bodyScope() {
        return (Scope)children.get(0);
    }
}
