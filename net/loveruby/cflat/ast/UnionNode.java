package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class UnionNode extends CompositeTypeDefinition {
    public UnionNode(Location loc, TypeRef ref, String name, List<Slot> membs) {
        super(loc, ref, name, membs);
    }

    public String kind() {
        return "union";
    }

    public boolean isUnion() {
        return true;
    }

    // #@@range/definingType{
    public Type definingType() {
        return new UnionType(name(), members(), location());
    }
    // #@@}

    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
