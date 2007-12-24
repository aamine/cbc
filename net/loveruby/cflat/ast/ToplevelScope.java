package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class ToplevelScope extends Scope {
    protected List privateEntities;
    protected Map sequenceTable;

    public ToplevelScope() {
        super(null);
        privateEntities = new ArrayList();
        sequenceTable = new HashMap();
    }

    public boolean isToplevel() {
        return true;
    }

    /*public void defineType(TypeDefinition t) {
        System.err.println("FIXME: defineType");
    }*/

    // Declare external symbols
    public void declare(Entity entity) {
        addEntity(entity);
    }

    public void define(Entity entity) {
        entity.defineIn(this);
    }

    public void defineFunction(DefinedFunction f) {
        if (f.isPrivate()) {
            addPrivateEntity(f);
        }
        else {
            addEntity(f);
        }
    }

    // Allocate public global variable or common symbol
    public void allocateVariable(Variable var) {
        addEntity(var);
        var.toplevelDefinition();
    }

    // Allocate private global variable or common symbol
    public void allocatePrivateVariable(Variable var) {
        addPrivateEntity(var);
        var.toplevelDefinition();
    }

    // Allocate static local variable
    public void allocateStaticLocalVariable(Variable var) {
        addPrivateEntity(var);
        Long seq = (Long)sequenceTable.get(var.name());
        if (seq == null) {
            var.setSequence(0);
            sequenceTable.put(var.name(), new Long(1));
        }
        else {
            var.setSequence(seq.longValue());
            sequenceTable.put(var.name(), new Long(seq.longValue() + 1));
        }
    }

    protected void addPrivateEntity(Entity ent) {
        super.addPrivateEntity(ent);
        privateEntities.add(ent);
    }

    public Entity get(String name) throws SemanticException {
        Entity ent;
        ent = (Entity)privateEntitiesMap.get(name);
        if (ent != null) return ent;
        ent = (Entity)entitiesMap.get(name);
        if (ent != null) return ent;
        throw new SemanticException("unresolved reference: " + name);
    }

    public Iterator allVariables() {
        throw new Error("TopScope#allVariables called");
    /*
        List result = allEntities();
        Iterator ents = privateEntitiesMap.values().iterator();
        while (ents.hasNext()) {
            Entity ent = (Entity)ents.next();
            if (ent.isVariable()) {
                result.add(ent);
            }
        }
        return result.iterator();
    */
    }

    public List globalVariables() {
        List result = new ArrayList();
        List src = new ArrayList();
        src.addAll(entities);
        src.addAll(privateEntities);
        Iterator ents = src.iterator();
        while (ents.hasNext()) {
            Object ent = ents.next();
            if (ent instanceof DefinedVariable) {
                DefinedVariable var = (DefinedVariable)ent;
                if (var.isInitialized()) {
                    result.add(var);
                }
            }
        }
        return result;
    }

    public List commonSymbols() {
        List result = new ArrayList();
        List src = new ArrayList();
        src.addAll(entities);
        src.addAll(privateEntities);
        Iterator ents = src.iterator();
        while (ents.hasNext()) {
            Entity ent = (Entity)ents.next();
            if (ent.isVariable() && !ent.isInitialized()) {
                result.add(ent);
            }
        }
        return result;
    }
}
