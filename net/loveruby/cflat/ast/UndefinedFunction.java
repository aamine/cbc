package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class UndefinedFunction extends Function {
    protected Params params;

    public UndefinedFunction(TypeNode t, String name, Params ps) {
        super(false, t, name);
        params = ps;
    }

    public Iterator parameters() {
        return params.parameters();
    }

    public boolean isDefined() {
        return false;
    }

    public boolean isFunction() {
        return true;
    }

    public void defineIn(ToplevelScope scope) {
        throw new Error("UndefinedFunction#defineIn");
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate());
        d.printMember("typeNode", typeNode);
        d.printMember("params", params);
    }

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }
}
