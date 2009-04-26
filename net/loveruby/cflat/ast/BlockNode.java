package net.loveruby.cflat.ast;
import net.loveruby.cflat.entity.DefinedVariable;
import net.loveruby.cflat.entity.LocalScope;
import java.util.*;

public class BlockNode extends StmtNode {
    protected List<DefinedVariable> variables;
    protected List<StmtNode> stmts;
    protected LocalScope scope;

    public BlockNode(Location loc, List<DefinedVariable> vars, List<StmtNode> stmts) {
        super(loc);
        this.variables = vars;
        this.stmts = stmts;
    }

    public List<DefinedVariable> variables() {
        return variables;
    }

    public List<StmtNode> stmts() {
        return stmts;
    }

    public StmtNode tailStmt() {
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

    public <S,E> S accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
