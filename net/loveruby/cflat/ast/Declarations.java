package net.loveruby.cflat.ast;
import java.util.*;

public class Declarations {
    protected List defvars, vardecls, defuns, funcdecls;
    protected List defstructs, defunions, typedefs;

    public Declarations() {
        defvars = new ArrayList();
        vardecls = new ArrayList();
        defuns = new ArrayList();
        funcdecls = new ArrayList();
        defstructs = new ArrayList();
        defunions = new ArrayList();
        typedefs = new ArrayList();
    }

    public void add(Declarations decls) {
        vardecls.addAll(decls.vardecls());
        funcdecls.addAll(decls.funcdecls());
        defstructs.addAll(decls.defstructs());
        defunions.addAll(decls.defunions());
        typedefs.addAll(decls.typedefs());
    }

    public void addDefvar(DefinedVariable var) {
        defvars.add(var);
    }

    public void addDefvars(List vars) {
        defvars.addAll(vars);
    }

    public List defvars() {
        return defvars;
    }

    public void addVardecl(UndefinedVariable var) {
        vardecls.add(var);
    }

    public List vardecls() {
        return vardecls;
    }

    public void addDefun(Function func) {
        defuns.add(func);
    }

    public List defuns() {
        return defuns;
    }

    public void addFuncdecl(UndefinedFunction func) {
        funcdecls.add(func);
    }

    public List funcdecls() {
        return funcdecls;
    }

    public void addDefstruct(StructNode n) {
        defstructs.add(n);
    }

    public List defstructs() {
        return defstructs;
    }

    public void addDefunion(UnionNode n) {
        defunions.add(n);
    }

    public List defunions() {
        return defunions;
    }

    public void addTypedef(TypedefNode n) {
        typedefs.add(n);
    }

    public List typedefs() {
        return typedefs;
    }
}
