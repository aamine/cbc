package net.loveruby.cflat.ast;
import java.util.*;

public class BlockNode extends StmtNode {
    protected List<DefinedVariable> variables;
    protected List<Node> stmts;
    protected LocalScope scope;

    public BlockNode(Location loc, List<DefinedVariable> vars, List<Node> stmts) {
        super(loc);
        this.variables = vars;
        this.stmts = stmts;
    }

    public List<DefinedVariable> variables() {
        return variables;
    }

    public List<Node> stmts() {
        return stmts;
    }

    public Node tailStmt() {
        if (stmts.isEmpty()) return null;
        return stmts.get(stmts.size() - 1);
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
