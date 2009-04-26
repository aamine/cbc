package net.loveruby.cflat.ast;
import net.loveruby.cflat.parser.Token;
import net.loveruby.cflat.type.TypeTable;
import net.loveruby.cflat.ir.IR;
import java.util.*;

public class AST extends Node {
    protected Location source;
    protected Declarations declarations;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;
    protected TypeTable typeTable;

    public AST(Location source, Declarations declarations) {
        super();
        this.source = source;
        this.declarations = declarations;
        this.scope = new ToplevelScope();
        this.constantTable = new ConstantTable();
    }

    public Location location() {
        return source;
    }

    public CflatToken sourceTokens() {
        return source.token();
    }

    public void setTypeTable(TypeTable table) {
        if (typeTable != null) {
            throw new Error("must not happen: typeTable != null");
        }
        this.typeTable = table;
    }

    public TypeTable typeTable() {
        return this.typeTable;
    }

    public List<TypeDefinition> types() {
        List<TypeDefinition> result = new ArrayList<TypeDefinition>();
        result.addAll(declarations.defstructs());
        result.addAll(declarations.defunions());
        result.addAll(declarations.typedefs());
        return result;
    }

    public List<Entity> declarations() {
        List<Entity> result = new ArrayList<Entity>();
        result.addAll(declarations.funcdecls());
        result.addAll(declarations.vardecls());
        return result;
    }

    public List<Entity> entities() {
        List<Entity> result = new ArrayList<Entity>();
        result.addAll(declarations.defvars());
        result.addAll(declarations.defuns());
        return result;
    }

    public List<DefinedVariable> definedVariables() {
        return declarations.defvars();
    }

    public List<DefinedFunction> definedFunctions() {
        return declarations.defuns();
    }

    public ToplevelScope scope() {
        return scope;
    }

    public ConstantTable constantTable() {
        return constantTable;
    }

    public IR ir() {
        return new IR(source,
                declarations.defvars(),
                declarations.defuns(),
                declarations.funcdecls(),
                scope,
                constantTable);
    }

    protected void _dump(Dumper d) {
        d.printNodeList("variables", definedVariables());
        d.printNodeList("functions", definedFunctions());
    }
}
