package net.loveruby.cflat.ir;
import net.loveruby.cflat.ast.Location;
import net.loveruby.cflat.ast.Variable;
import net.loveruby.cflat.ast.DefinedVariable;
import net.loveruby.cflat.ast.Function;
import net.loveruby.cflat.ast.DefinedFunction;
import net.loveruby.cflat.ast.UndefinedFunction;
import net.loveruby.cflat.ast.ConstantTable;
import net.loveruby.cflat.ast.ToplevelScope;
import net.loveruby.cflat.ast.Scope;
import net.loveruby.cflat.type.TypeTable;
import java.io.PrintStream;
import java.util.*;

public class IR {
    protected Location source;
    protected List<DefinedVariable> defvars;
    protected List<DefinedFunction> defuns;
    protected List<UndefinedFunction> funcdecls;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;
    protected TypeTable typeTable;

    public IR(Location source,
            List<DefinedVariable> defvars,
            List<DefinedFunction> defuns,
            List<UndefinedFunction> funcdecls,
            ToplevelScope scope,
            ConstantTable constantTable,
            TypeTable typeTable) {
        super();
        this.source = source;
        this.defvars = defvars;
        this.defuns = defuns;
        this.funcdecls = funcdecls;
        this.scope = scope;
        this.constantTable = constantTable;
        this.typeTable = typeTable;
    }

    public String fileName() {
        return source.sourceName();
    }

    public Location location() {
        return source;
    }

    public List<DefinedVariable> definedVariables() {
        return defvars;
    }

    public boolean functionDefined() {
        return !defuns.isEmpty();
    }

    public List<DefinedFunction> definedFunctions() {
        return defuns;
    }

    public ToplevelScope scope() {
        return scope;
    }

    public List<Function> allFunctions() {
        List<Function> result = new ArrayList<Function>();
        result.addAll(defuns);
        result.addAll(funcdecls);
        return result;
    }

    /** a list of all defined/declared global-scope variables */
    public List<Variable> allGlobalVariables() {
        return scope.allGlobalVariables();
    }

    /** a list of defined initialized global variables */
    public List<DefinedVariable> definedGlobalVariables() {
        return scope.definedGlobalVariables();
    }

    /** a list of defined uninitialized global variables */
    public List<DefinedVariable> definedCommonSymbols() {
        return scope.definedCommonSymbols();
    }

    public ConstantTable constantTable() {
        return constantTable;
    }

    public TypeTable typeTable() {
        return this.typeTable;
    }

    public void dump() {
        dump(System.err);
    }

    public void dump(PrintStream s) {
        throw new Error("FIXME: IR#dump");
    }

    protected void _dump(Dumper d) {
        d.printVariables("variables", defvars);
        d.printFunctions("functions", defuns);
    }
}
