package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.*;
import java.util.*;

public class CaseNode extends StmtNode {
    protected LabelPool pool;
    protected Label beginLabel;
    protected List values;      // List<Node>
    protected BlockNode body;

    public CaseNode(Location loc, LabelPool pool, List values, BlockNode body) {
        super(loc);
        this.pool = pool;
        this.values = values;
        this.body = body;
        this.beginLabel = null;
    }

    public Iterator values() {
        return values.iterator();
    }

    public boolean isDefault() {
        return values.isEmpty();
    }

    public BlockNode body() {
        return body;
    }

    public Label beginLabel() {
        if (beginLabel == null) {
            beginLabel = pool.newLabel();
        }
        return beginLabel;
    }

    protected void _dump(Dumper d) {
        d.printNodeList("values", values);
        d.printMember("body", body);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
