package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

abstract public class CompositeTypeDefinition extends TypeDefinition {
    protected List<Slot> members;

    public CompositeTypeDefinition(Location loc, TypeRef ref,
                                   String name, List<Slot> membs) {
        super(loc, ref, name);
        members = membs;
    }

    public boolean isCompositeType() {
        return true;
    }

    abstract public String kind();

    public List<Slot> members() {
        return members;
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printNodeList("members", members);
    }
}
