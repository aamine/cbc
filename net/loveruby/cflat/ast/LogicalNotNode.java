package net.loveruby.cflat.ast;

public class LogicalNotNode extends UnaryOpNode {
    public LogicalNotNode(ExprNode expr) {
        super(expr);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
