package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class UnionNode extends ComplexTypeDefinition {
    public UnionNode(Location loc, TypeRef ref, String name, List membs) {
        super(loc, ref, name, membs);
    }

    public String kind() {
        return "union";
    }

    public boolean isUnion() {
        return true;
    }

    public void defineIn(TypeTable table) {
        table.defineUnion((UnionTypeRef)typeRef(), members());
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
