package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class LabelNode extends StmtNode {
    protected String name;
    protected Node stmt;
    protected Label label;

    public LabelNode(Location loc, String name, Node stmt) {
        super(loc);
        this.name = name;
        this.stmt = stmt;
    }

    public String name() {
        return name;
    }

    public Node stmt() {
        return stmt;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label label() {
        if (label == null) throw new Error("label is null");
        return label;
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("stmt", stmt);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
