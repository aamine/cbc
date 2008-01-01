package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

public class StructNode extends ComplexTypeDefinition {
    public StructNode(TypeRef ref, String name, List membs) {
        super(ref, name, membs);
    }

    public String type() {
        return "struct";
    }

    public TypeRef typeRef() {
        return super.typeRef();
    }

    public boolean isStruct() {
        return true;
    }

    public void defineIn(TypeTable table) {
        table.defineStruct((StructTypeRef)typeRef(), members());
    }

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }
}
