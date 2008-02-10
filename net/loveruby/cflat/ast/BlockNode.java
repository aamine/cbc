package net.loveruby.cflat.ast;
import java.util.*;

public class BlockNode extends StmtNode {
    protected List variables;
    protected List stmts;
    protected LocalScope scope;

    public BlockNode(Location loc, List vars, List ss) {
        super(loc);
        variables = vars;
        stmts = ss;
    }

    public Iterator variables() {
        return variables.iterator();
    }

    public Iterator stmts() {
        return stmts.iterator();
    }

    public Node tailStmt() {
        if (stmts.isEmpty()) return null;
        return (Node)stmts.get(stmts.size() - 1);
    }

    public LocalScope scope() {
        return scope;
    }

    public void setScope(LocalScope scope) {
        this.scope = scope;
    }

    protected void _dump(Dumper d) {
        d.printNodeList("variables", variables);
        d.printNodeList("stmts", stmts);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
