package net.loveruby.cflat.ast;
import java.util.*;

public class Declarations {
    protected Set defvars, vardecls, defuns, funcdecls;
    protected Set defstructs, defunions, typedefs;

    public Declarations() {
        defvars = new LinkedHashSet();
        vardecls = new LinkedHashSet();
        defuns = new LinkedHashSet();
        funcdecls = new LinkedHashSet();
        defstructs = new LinkedHashSet();
        defunions = new LinkedHashSet();
        typedefs = new LinkedHashSet();
    }

    public void add(Declarations decls) {
        updateSet(vardecls, decls.vardecls());
        updateSet(funcdecls, decls.funcdecls());
        updateSet(defstructs, decls.defstructs());
        updateSet(defunions, decls.defunions());
        updateSet(typedefs, decls.typedefs());
    }

    protected void updateSet(Set s, List items) {
        Iterator i = items.iterator();
        while (i.hasNext()) {
            s.add(i.next());
        }
    }

    public void addDefvar(DefinedVariable var) {
        defvars.add(var);
    }

    public void addDefvars(List vars) {
        updateSet(defvars, vars);
    }

    public List defvars() {
        return new ArrayList(defvars);
    }

    public void addVardecl(UndefinedVariable var) {
        vardecls.add(var);
    }

    public List vardecls() {
        return new ArrayList(vardecls);
    }

    public void addDefun(Function func) {
        defuns.add(func);
    }

    public List defuns() {
        return new ArrayList(defuns);
    }

    public void addFuncdecl(UndefinedFunction func) {
        funcdecls.add(func);
    }

    public List funcdecls() {
        return new ArrayList(funcdecls);
    }

    public void addDefstruct(StructNode n) {
        defstructs.add(n);
    }

    public List defstructs() {
        return new ArrayList(defstructs);
    }

    public void addDefunion(UnionNode n) {
        defunions.add(n);
    }

    public List defunions() {
        return new ArrayList(defunions);
    }

    public void addTypedef(TypedefNode n) {
        typedefs.add(n);
    }

    public List typedefs() {
        return new ArrayList(typedefs);
    }
}
