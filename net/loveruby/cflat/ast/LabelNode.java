package net.loveruby.cflat.ast;
import net.loveruby.cflat.asm.Label;

public class LabelNode extends StmtNode {
    protected String name;
    protected StmtNode stmt;
    protected Label label;

    public LabelNode(Location loc, String name, StmtNode stmt) {
        super(loc);
        this.name = name;
        this.stmt = stmt;
    }

    public String name() {
        return name;
    }

    public StmtNode stmt() {
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

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
