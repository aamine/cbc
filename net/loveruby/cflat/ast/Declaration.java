package net.loveruby.cflat.ast;

abstract public class Declaration {
    protected String name;

    public Declaration(String n) {
        name = n;
    }

    public String name() {
        return name;
    }

    abstract public void accept(DefinitionVisitor visitor);
}
