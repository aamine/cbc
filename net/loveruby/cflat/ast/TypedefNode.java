package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;

public class TypedefNode extends TypeDefinition {
    public TypedefNode(Location loc, TypeRef ref, String name) {
        super(loc, ref, name);
    }

    public boolean isUserType() {
        return true;
    }

    public void defineIn(TypeTable table) {
        table.defineUserType(new UserTypeRef(name()), typeNode());
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("typeNode", typeNode);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
