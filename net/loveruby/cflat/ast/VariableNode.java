package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.entity.Entity;
import net.loveruby.cflat.entity.DefinedVariable;

public class VariableNode extends LHSNode {
    private Location location;
    private String name;
    private Entity entity;

    public VariableNode(Location loc, String name) {
        this.location = loc;
        this.name = name;
    }

    public VariableNode(DefinedVariable var) {
        this.entity = var;
        this.name = var.name();
    }

    public String name() {
        return name;
    }

    public boolean isResolved() {
        return (entity != null);
    }

    public Entity entity() {
        if (entity == null) {
            throw new Error("VariableNode.entity == null");
        }
        return entity;
    }

    public void setEntity(Entity ent) {
        entity = ent;
    }

    public TypeNode typeNode() {
        return entity().typeNode();
    }

    public boolean isParameter() {
        return entity().isParameter();
    }

    protected Type origType() {
        return entity().type();
    }

    public Location location() {
        return location;
    }

    protected void _dump(Dumper d) {
        if (type != null) {
            d.printMember("type", type);
        }
        d.printMember("name", name, isResolved());
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
