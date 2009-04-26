package net.loveruby.cflat.ast;
import net.loveruby.cflat.compiler.ErrorHandler;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.ir.Stmt;
import net.loveruby.cflat.asm.Label;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class DefinedFunction extends Function {
    protected Params params;
    protected BlockNode body;
    protected LocalScope scope;
    protected Label epilogueLabel;
    protected List<Stmt> ir;

    public DefinedFunction(boolean priv,
                           TypeNode type,
                           String name,
                           Params params,
                           BlockNode body) {
        super(priv, type, name);
        this.params = params;
        this.body = body;
        this.epilogueLabel = new Label();
    }

    public boolean isDefined() {
        return true;
    }

    public List<Parameter> parameters() {
        return params.parameters();
    }

    public BlockNode body() {
        return body;
    }

    public List<Stmt> ir() {
        return ir;
    }

    public void setIR(List<Stmt> ir) {
        this.ir = ir;
    }

    public void setScope(LocalScope scope) {
        this.scope = scope;
    }

    /**
     * Returns function local variables.
     * Does NOT include paramters.
     * Does NOT include static local variables.
     */
    public List<DefinedVariable> localVariables() {
        return scope.allLocalVariables();
    }

    public Label epilogueLabel() {
        return this.epilogueLabel;
    }

    protected void _dump(Dumper d) {
        d.printMember("name", name);
        d.printMember("isPrivate", isPrivate);
        d.printMember("params", params);
        d.printMember("body", body);
    }

    public <T> T accept(DeclarationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
