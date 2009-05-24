package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.entity.*;
import net.loveruby.cflat.utils.ErrorHandler;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class LocalResolver extends Visitor {
    // #@@range/ctor{
    private final LinkedList<Scope> scopeStack;
    private final ConstantTable constantTable;
    private final ErrorHandler errorHandler;

    public LocalResolver(ErrorHandler h) {
        this.errorHandler = h;
        this.scopeStack = new LinkedList<Scope>();
        this.constantTable = new ConstantTable();
    }
    // #@@}

    private void resolve(StmtNode n) {
        n.accept(this);
    }

    private void resolve(ExprNode n) {
        n.accept(this);
    }

    // #@@range/resolve{
    public void resolve(AST ast) throws SemanticException {
        ToplevelScope toplevel = new ToplevelScope();
        scopeStack.add(toplevel);

        // #@@range/declareToplevel{
        for (Entity decl : ast.declarations()) {
            toplevel.declareEntity(decl);
        }
        for (Entity ent : ast.definitions()) {
            toplevel.defineEntity(ent);
        }
        // #@@}
        // #@@range/resolveRefs{
        resolveGvarInitializers(ast.definedVariables());
        resolveConstantValues(ast.constants());
        resolveFunctions(ast.definedFunctions());
        // #@@}
        toplevel.checkReferences(errorHandler);
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }

        ast.setScope(toplevel);
        ast.setConstantTable(constantTable);
    }
    // #@@}

    // #@@range/resolveGvarInitializers{
    private void resolveGvarInitializers(List<DefinedVariable> gvars) {
        for (DefinedVariable gvar : gvars) {
            if (gvar.hasInitializer()) {
                resolve(gvar.initializer());
            }
        }
    }
    // #@@}

    private void resolveConstantValues(List<Constant> consts) {
        for (Constant c : consts) {
            resolve(c.value());
        }
    }

    // #@@range/resolveFunctions{
    private void resolveFunctions(List<DefinedFunction> funcs) {
        for (DefinedFunction func : funcs) {
            pushScope(func.parameters());
            resolve(func.body());
            func.setScope(popScope());
        }
    }
    // #@@}

    // #@@range/BlockNode{
    public Void visit(BlockNode node) {
        pushScope(node.variables());
        super.visit(node);
        node.setScope(popScope());
        return null;
    }
    // #@@}

    // #@@range/pushScope{
    private void pushScope(List<? extends DefinedVariable> vars) {
        LocalScope scope = new LocalScope(currentScope());
        for (DefinedVariable var : vars) {
            if (scope.isDefinedLocally(var.name())) {
                error(var.location(),
                    "duplicated variable in scope: " + var.name());
            }
            else {
                scope.defineVariable(var);
            }
        }
        scopeStack.addLast(scope);
    }
    // #@@}

    // #@@range/popScope{
    private LocalScope popScope() {
        return (LocalScope)scopeStack.removeLast();
    }
    // #@@}

    // #@@range/currentScope{
    private Scope currentScope() {
        return scopeStack.getLast();
    }
    // #@@}

    // #@@range/StringLiteralNode{
    public Void visit(StringLiteralNode node) {
        node.setEntry(constantTable.intern(node.value()));
        return null;
    }
    // #@@}

    // #@@range/VariableNode{
    public Void visit(VariableNode node) {
        try {
            Entity ent = currentScope().get(node.name());
            ent.refered();
            node.setEntity(ent);
        }
        catch (SemanticException ex) {
            error(node, ex.getMessage());
        }
        return null;
    }
    // #@@}

    private void error(Node node, String message) {
        errorHandler.error(node.location(), message);
    }

    private void error(Location loc, String message) {
        errorHandler.error(loc, message);
    }
}
