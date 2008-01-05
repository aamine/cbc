package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

abstract public class ComplexTypeDefinition extends TypeDefinition {
    protected List members;     // List<Slot>

    public ComplexTypeDefinition(Location loc,
                                 TypeRef ref, String name, List membs) {
        super(loc, ref, name);
        members = membs;
    }

    public boolean isComplexType() {
        return true;
    }

    abstract public String kind();

    public List members() {
        return members;
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printNodeList("members", members);
    }
}
