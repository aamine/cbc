package net.loveruby.cflat.ast;
import net.loveruby.cflat.parser.Token;
import net.loveruby.cflat.type.TypeTable;
import java.util.*;

public class AST extends Node {
    protected String fileName;
    protected Declarations declarations;
    protected Token firstToken;

    protected ToplevelScope scope;
    protected TypeTable typeTable;
    protected ConstantTable constantTable;

    public AST(String fname, Declarations decls, Token t) {
        super();
        fileName = fname;
        declarations = decls;
        firstToken = t;
        scope = new ToplevelScope();
        typeTable = TypeTable.ilp32();  // FIXME: platform dependent
        constantTable = new ConstantTable();
    }

    public String fileName() {
        return fileName;
    }

    public Iterator types() {
        List result = new ArrayList();
        result.addAll(declarations.defstructs());
        result.addAll(declarations.defunions());
        result.addAll(declarations.typedefs());
        return result.iterator();
    }

    public Iterator entities() {
        List result = new ArrayList();
        result.addAll(declarations.defvars());
        result.addAll(declarations.defuns());
        return result.iterator();
    }

    public Iterator globalVariables() {
        return scope.globalVariables().iterator();
    }

    public Iterator commonSymbols() {
        return scope.commonSymbols().iterator();
    }

    public boolean functionDefined() {
        return !declarations.defuns().isEmpty();
    }

    public Iterator functions() {
        return declarations.defuns().iterator();
    }

    public void declare(UndefinedFunction f) {
        declarations.funcdecls().add(f);
    }

    public Iterator declarations() {
        List result = new ArrayList();
        //result.addAll(declarations.defvars());
        //result.addAll(declarations.defuns());
        result.addAll(declarations.funcdecls());
        return result.iterator();
    }

    public ToplevelScope scope() {
        return scope;
    }

    public TypeTable typeTable() {
        return typeTable;
    }

    public ConstantTable constantTable() {
        return constantTable;
    }

    public void dump(String prefix) {
        System.out.println("FIXME: dump AST");
    }

    public void accept(ASTVisitor visitor) {
        throw new Error("AST#accept");
    }
}
