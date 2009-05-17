package net.loveruby.cflat.ast;
import net.loveruby.cflat.entity.*;
import java.util.*;

public class Declarations {
    Set<DefinedVariable> defvars = new LinkedHashSet<DefinedVariable>();
    Set<UndefinedVariable> vardecls = new LinkedHashSet<UndefinedVariable>();
    Set<DefinedFunction> defuns = new LinkedHashSet<DefinedFunction>();
    Set<UndefinedFunction> funcdecls = new LinkedHashSet<UndefinedFunction>();
    Set<Constant> constants = new LinkedHashSet<Constant>();
    Set<StructNode> defstructs = new LinkedHashSet<StructNode>();
    Set<UnionNode> defunions = new LinkedHashSet<UnionNode>();
    Set<TypedefNode> typedefs = new LinkedHashSet<TypedefNode>();

    public void add(Declarations decls) {
        defvars.addAll(decls.defvars);
        vardecls.addAll(decls.vardecls);
        funcdecls.addAll(decls.funcdecls);
        constants.addAll(decls.constants);
        defstructs.addAll(decls.defstructs);
        defunions.addAll(decls.defunions);
        typedefs.addAll(decls.typedefs);
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

    public void addConstant(Constant c) {
        constants.add(c);
    }

    public List<Constant> constants() {
        return new ArrayList<Constant>(constants);
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
