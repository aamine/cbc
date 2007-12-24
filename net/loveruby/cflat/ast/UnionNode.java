package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class UnionNode extends ComplexTypeDefinition {
    public UnionNode(TypeRef ref, String name, List membs) {
        super(ref, name, membs);
    }

    public TypeRef typeRef() {
        return (UnionTypeRef)super.typeRef();
    }

    public boolean isUnion() {
        return true;
    }

    public void defineIn(TypeTable table) {
        table.defineUnion((UnionTypeRef)typeRef(), members());
    }

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }
}
