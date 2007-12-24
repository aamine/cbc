package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import java.util.*;

abstract public class ComplexTypeDefinition extends TypeDefinition {
    protected List members;
    protected TypeNode typeNode;

    public ComplexTypeDefinition(TypeRef ref, String name, List membs) {
        super(name);
        members = membs;
        typeNode = new TypeNode(ref);
    }

    public boolean isComplexType() {
        return true;
    }

    public List members() {
        return members;
    }

    public TypeRef typeRef() {
        return typeNode.typeRef();
    }

    public TypeNode typeNode() {
        return typeNode;
    }
}
