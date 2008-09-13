package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.asm.*;

public class ArefNode extends ExprNode {
    protected ExprNode expr, index;

    public ArefNode(ExprNode expr, ExprNode index) {
        this.expr = expr;
        this.index = index;
    }

    public Type type() {
        return expr.type().baseType();
    }

    public ExprNode expr() {
        return expr;
    }

    public ExprNode index() {
        return index;
    }

    public boolean isAssignable() {
        return true;
    }

    // isMultiDimension a[x][y][z] = true.
    // isMultiDimension a[x][y] = true.
    // isMultiDimension a[x] = false.
    public boolean isMultiDimension() {
        return (expr instanceof ArefNode) && !expr.type().isPointerAlike();
    }

    // Returns base expression of (multi-dimension) array.
    // e.g.  baseExpr of a[x][y][z] is a.
    public ExprNode baseExpr() {
        return isMultiDimension() ? ((ArefNode)expr).baseExpr() : expr;
    }

    // element size of this (multi-dimension) array
    public long elementSize() {
        //return baseExpr().type().baseType().allocSize();
        return type().allocSize();
    }

    public long length() {
        return ((ArrayType)expr.type()).length();
    }

    public boolean isConstantAddress() {
        return false;
    }

    public AsmEntity address() {
        throw new Error("ArefNode#address");
    }

    public Location location() {
        return expr.location();
    }

    protected void _dump(Dumper d) {
        d.printMember("expr", expr);
        d.printMember("index", index);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public void acceptLHS(ASTLHSVisitor visitor) {
        visitor.visitLHS(this);
    }
}
