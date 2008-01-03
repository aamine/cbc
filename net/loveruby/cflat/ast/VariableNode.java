package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.*;

public class VariableNode extends ExprNode implements LHSNode {
    protected String name;
    protected Entity entity;

    public VariableNode(String n) {
        super();
        name = n;
    }

    public String name() {
        return name;
    }

    public void setEntity(Entity ent) {
        entity = ent;
    }

    public Entity entity() {
        if (entity == null) {
            throw new Error("VariableNode.entity == null");
        }
        return entity;
    }

    public Type type() {
        return entity().type();
    }

    public TypeNode typeNode() {
        return entity().typeNode();
    }

    public boolean isAssignable() {
        return true;
    }

    // LHS node requirement
    public boolean isConstantAddress() {
        return true;
    }

    // LHS node requirement
    public AsmEntity address() {
        return entity.address();
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
