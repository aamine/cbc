package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class TypedefNode extends TypeDefinition {
    protected TypeNode typeNode;

    public TypedefNode(TypeNode t, String name) {
        super(name);
        typeNode = t;
    }

    public boolean isUserType() {
        return true;
    }

    public TypeRef typeRef() {
        return typeNode.typeRef();
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public Type type() {
        return typeNode.type();
    }

    public void defineIn(TypeTable table) {
        table.defineUserType(new UserTypeRef(name()), typeNode());
    }

    public void accept(DefinitionVisitor visitor) {
        visitor.visit(this);
    }
}
