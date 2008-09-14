package net.loveruby.cflat.ast;
import net.loveruby.cflat.parser.Token;
import java.util.*;

public class AST extends Node {
    protected Location source;
    protected Declarations declarations;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;

    public AST(Location source, Declarations declarations) {
        super();
        this.source = source;
        this.declarations = declarations;
        this.scope = new ToplevelScope();
        this.constantTable = new ConstantTable();
    }

    public String fileName() {
        return source.sourceName();
    }

    public Iterator sourceTokens() {
        return source.token().iterator();
    }

    public Iterator types() {
        List result = new ArrayList();
        result.addAll(declarations.defstructs());
        result.addAll(declarations.defunions());
        result.addAll(declarations.typedefs());
        return result.iterator();
    }

    public Iterator declarations() {
        List result = new ArrayList();
        result.addAll(declarations.funcdecls());
        result.addAll(declarations.vardecls());
        return result.iterator();
    }

    public Iterator entities() {
        List result = new ArrayList();
        result.addAll(declarations.defvars());
        result.addAll(declarations.defuns());
        return result.iterator();
    }

    public Iterator variables() {
        return declarations.defvars().iterator();
    }

    public boolean functionDefined() {
        return !declarations.defuns().isEmpty();
    }

    public Iterator functions() {
        return declarations.defuns().iterator();
    }

    public ToplevelScope scope() {
        return scope;
    }

    public Iterator globalVariables() {
        return scope.globalVariables().iterator();
    }

    public Iterator commonSymbols() {
        return scope.commonSymbols().iterator();
    }

    public ConstantTable constantTable() {
        return constantTable;
    }

    public Location location() {
        return source;
    }

    protected void _dump(Dumper d) {
        d.printNodeList("variables", variables());
        d.printNodeList("functions", functions());
    }

    public void accept(ASTVisitor visitor) {
        throw new Error("AST#accept");
    }
}
