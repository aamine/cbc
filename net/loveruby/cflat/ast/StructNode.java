package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class StructNode extends CompositeTypeDefinition {
    public StructNode(Location loc, TypeRef ref, String name, List membs) {
        super(loc, ref, name, membs);
    }

    public String kind() {
        return "struct";
    }

    public boolean isStruct() {
        return true;
    }

    // #@@range/definingType{
    public Type definingType() {
        return new StructType(name(), members(), location());
    }
    // #@@}

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
