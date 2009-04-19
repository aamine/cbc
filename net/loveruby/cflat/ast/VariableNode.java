package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.Type;
import net.loveruby.cflat.asm.AsmOperand;
import net.loveruby.cflat.asm.MemoryReference;

public class VariableNode extends ExprNode {
    protected Location location;
    protected String name;
    protected Entity entity;

    public VariableNode(Location loc, String name) {
        super();
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

    public Type type() {
        return entity().type();
    }

    public TypeNode typeNode() {
        return entity().typeNode();
    }

    public boolean isAssignable() {
        return true;
    }

    public boolean isParameter() {
        return entity().isParameter();
    }

    public boolean shouldEvaluatedToAddress() {
        return entity().cannotLoad();
    }

    public boolean isConstantAddress() {
        return true;
    }

    public AsmOperand address() {
        return entity.address();
    }

    public MemoryReference memref() {
        return entity.memref();
    }

    public Location location() {
        return location;
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name, isResolved());
    }

    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }

    public void acceptLHS(ASTLHSVisitor visitor) {
        visitor.visitLHS(this);
    }
}
