package net.loveruby.cflat.entity;
import net.loveruby.cflat.ast.TypeNode;

public class Parameter extends DefinedVariable {
    public Parameter(TypeNode type, String name) {
        super(false, type, name, null);
    }

    public boolean isParameter() {
        return true;
    }

    protected void _dump(net.loveruby.cflat.ast.Dumper d) {
        d.printMember("name", name);
        d.printMember("typeNode", typeNode);
    }
}
