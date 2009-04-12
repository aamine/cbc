package net.loveruby.cflat.ir;

public class IRTree {
    protected Location source;
    protected ToplevelScope scope;
    protected ConstantTable constantTable;
    protected Set<DefinedVariable> defvars;
    protected Set<DefinedFunction> defuns;

    public IRTree(Location source,
            ToplevelScope scope,
            ConstantTable constants,
            Set<DefinedVariable> defvars,
            Set<DefinedFunction> defuns) {
        this.source = source;
        this.scope = scope;
        this.constants = constants;
        this.defvars = defvars;
        this.defuns = defuns;
    }
}
