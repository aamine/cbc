package net.loveruby.cflat.ast;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.ir.IR;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;

public class AST extends Node {
    protected Location source;
    protected Declarations declarations;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;

    public AST(Location source, Declarations declarations) {
        super();
        this.source = source;
        this.declarations = declarations;
    }

    public Location location() {
        return source;
    }

    public List<TypeDefinition> types() {
        List<TypeDefinition> result = new ArrayList<TypeDefinition>();
        result.addAll(declarations.defstructs());
        result.addAll(declarations.defunions());
        result.addAll(declarations.typedefs());
        return result;
    }

    public List<Entity> entities() {
        List<Entity> result = new ArrayList<Entity>();
        result.addAll(declarations.funcdecls);
        result.addAll(declarations.vardecls);
        result.addAll(declarations.defvars);
        result.addAll(declarations.defuns);
        result.addAll(declarations.constants);
        return result;
    }

    public List<Entity> declarations() {
        List<Entity> result = new ArrayList<Entity>();
        result.addAll(declarations.funcdecls);
        result.addAll(declarations.vardecls);
        return result;
    }

    public List<Entity> definitions() {
        List<Entity> result = new ArrayList<Entity>();
        result.addAll(declarations.defvars);
        result.addAll(declarations.defuns);
        result.addAll(declarations.constants);
        return result;
    }

    public List<Constant> constants() {
        return declarations.constants();
    }

    public List<DefinedVariable> definedVariables() {
        return declarations.defvars();
    }

    public List<DefinedFunction> definedFunctions() {
        return declarations.defuns();
    }

    // called by LocalResolver
    public void setScope(ToplevelScope scope) {
        if (this.scope != null) {
            throw new Error("must not happen: ToplevelScope set twice");
        }
        this.scope = scope;
    }

    public ToplevelScope scope() {
        if (this.scope == null) {
            throw new Error("must not happen: AST.scope is null");
        }
        return scope;
    }

    // called by LocalResolver
    public void setConstantTable(ConstantTable table) {
        if (this.constantTable != null) {
            throw new Error("must not happen: ConstantTable set twice");
        }
        this.constantTable = table;
    }

    public ConstantTable constantTable() {
        if (this.constantTable == null) {
            throw new Error("must not happen: AST.constantTable is null");
        }
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

    public void dumpTokens(PrintStream s) {
        for (CflatToken t : source.token()) {
            printPair(t.kindName(), t.dumpedImage(), s);
        }
    }

    static final private int NUM_LEFT_COLUMNS = 24;

    private void printPair(String key, String value, PrintStream s) {
        s.print(key);
        for (int n = NUM_LEFT_COLUMNS - key.length(); n > 0; n--) {
            s.print(" ");
        }
        s.println(value);
    }

    public StmtNode getSingleMainStmt() {
        for (DefinedFunction f : definedFunctions()) {
            if (f.name().equals("main")) {
                if (f.body().stmts().isEmpty()) {
                    return null;
                }
                return f.body().stmts().get(0);
            }
        }
        return null;
    }

    public ExprNode getSingleMainExpr() {
        StmtNode stmt = getSingleMainStmt();
        if (stmt == null) {
            return null;
        }
        else if (stmt instanceof ExprStmtNode) {
            return ((ExprStmtNode)stmt).expr();
        }
        else if (stmt instanceof ReturnNode) {
            return ((ReturnNode)stmt).expr();
        }
        else {
            return null;
        }
    }

}
