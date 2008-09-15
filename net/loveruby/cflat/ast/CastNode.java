package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class CastNode extends ExprNode {
    protected TypeNode typeNode;
    protected ExprNode expr;

    public CastNode(Type t, ExprNode expr) {
        this(new TypeNode(t), expr);
    }

    public CastNode(TypeNode t, ExprNode expr) {
        this.typeNode = t;
        this.expr = expr;
    }

    public Type type() {
        return typeNode.type();
    }

    public TypeNode typeNode() {
        return typeNode;
    }

    public ExprNode expr() {
        return expr;
    }

    public boolean isConstant() {
        return expr.isConstant();
    }

    public boolean isConstantAddress() {
        return expr.isConstantAddress();
    }

    public MemoryReference memref() {
        return expr.memref();
    }

    public AsmEntity address() {
        return expr.address();
    }

    public Location location() {
        return typeNode.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("typeNode", typeNode);
        d.printMember("expr", expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
