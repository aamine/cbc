package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;
import java.util.*;

public class CaseNode extends StmtNode {
    protected Label beginLabel;
    protected List<ExprNode> values;
    protected BlockNode body;

    public CaseNode(Location loc, List<ExprNode> values, BlockNode body) {
        super(loc);
        this.values = values;
        this.body = body;
        this.beginLabel = new Label();
    }

    public List<ExprNode> values() {
        return values;
    }

    public void setValues(List<ExprNode> values) {
        this.values = values;
    }

    public boolean isDefault() {
        return values.isEmpty();
    }

    public BlockNode body() {
        return body;
    }

    public Label beginLabel() {
        return beginLabel;
    }

    protected void _dump(Dumper d) {
        d.printNodeList("values", values);
        d.printMember("body", body);
    }

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
