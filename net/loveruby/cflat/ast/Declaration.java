package net.loveruby.cflat.ast;

abstract public class Declaration implements Dumpable {
    protected String name;

    public Declaration(String n) {
        name = n;
    }

    public String name() {
        return name;
    }

    public void dump(Dumper d) {
        d.printClass(this);
        _dump(d);
    }

    abstract void _dump(Dumper d);

    abstract public void accept(DefinitionVisitor visitor);
}
