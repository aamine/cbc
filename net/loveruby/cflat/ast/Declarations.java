package net.loveruby.cflat.ast;
import java.util.*;

public class Declarations {
    protected Set<DefinedVariable> defvars;
    protected Set<UndefinedVariable> vardecls;
    protected Set<DefinedFunction> defuns;
    protected Set<UndefinedFunction> funcdecls;
    protected Set<StructNode> defstructs;
    protected Set<UnionNode> defunions;
    protected Set<TypedefNode> typedefs;

    public Declarations() {
        defvars = new LinkedHashSet<DefinedVariable>();
        vardecls = new LinkedHashSet<UndefinedVariable>();
        defuns = new LinkedHashSet<DefinedFunction>();
        funcdecls = new LinkedHashSet<UndefinedFunction>();
        defstructs = new LinkedHashSet<StructNode>();
        defunions = new LinkedHashSet<UnionNode>();
        typedefs = new LinkedHashSet<TypedefNode>();
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

    public void addDefvars(List<DefinedVariable> vars) {
        defvars.addAll(vars);
    }

    public List<DefinedVariable> defvars() {
        return new ArrayList<DefinedVariable>(defvars);
    }

    public void addVardecl(UndefinedVariable var) {
        vardecls.add(var);
    }

    public List<UndefinedVariable> vardecls() {
        return new ArrayList<UndefinedVariable>(vardecls);
    }

    public void addDefun(DefinedFunction func) {
        defuns.add(func);
    }

    public List<DefinedFunction> defuns() {
        return new ArrayList<DefinedFunction>(defuns);
    }

    public void addFuncdecl(UndefinedFunction func) {
        funcdecls.add(func);
    }

    public List<UndefinedFunction> funcdecls() {
        return new ArrayList<UndefinedFunction>(funcdecls);
    }

    public void addDefstruct(StructNode n) {
        defstructs.add(n);
    }

    public List<StructNode> defstructs() {
        return new ArrayList<StructNode>(defstructs);
    }

    public void addDefunion(UnionNode n) {
        defunions.add(n);
    }

    public List<UnionNode> defunions() {
        return new ArrayList<UnionNode>(defunions);
    }

    public void addTypedef(TypedefNode n) {
        typedefs.add(n);
    }

    public List<TypedefNode> typedefs() {
        return new ArrayList<TypedefNode>(typedefs);
    }
}
