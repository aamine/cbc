package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class LocalReferenceResolver extends Visitor {
    // #@@range/ctor{
    protected ErrorHandler errorHandler;
    protected ToplevelScope toplevel;
    protected LinkedList<Scope> scopeStack;
    protected ConstantTable constantTable;

    public LocalReferenceResolver(ErrorHandler h) {
        this.errorHandler = h;
    }
    // #@@}

    protected void resolve(Node n) {
        visitNode(n);
    }

    // #@@range/resolve{
    public void resolve(AST ast) throws SemanticException {
        toplevel = ast.scope();
        scopeStack = new LinkedList<Scope>();
        scopeStack.add(toplevel);
        constantTable = ast.constantTable();

        // #@@range/declareToplevel{
        for (Entity decl : ast.declarations()) {
            toplevel.declareEntity(decl);
        }
        for (Entity ent : ast.entities()) {
            toplevel.declareEntity(ent);
        }
        // #@@}
        // #@@range/resolveRefs{
        resolveGvarInitializers(ast.definedVariables());
        resolveFunctions(ast.definedFunctions());
        // #@@}
        toplevel.checkReferences(errorHandler);
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }
    // #@@}

    // #@@range/resolveGvarInitializers{
    protected void resolveGvarInitializers(List<DefinedVariable> gvars) {
        for (DefinedVariable gvar : gvars) {
            if (gvar.hasInitializer()) {
                resolve(gvar.initializer());
            }
        }
    }
    // #@@}

    // #@@range/resolveFunctions{
    protected void resolveFunctions(List<DefinedFunction> funcs) {
        for (DefinedFunction func : funcs) {
            pushScope(func.parameters());
            resolve(func.body());
            func.setScope(popScope());
        }
    }
    // #@@}

    // #@@range/BlockNode{
    public void visit(BlockNode node) {
        pushScope(node.variables());
        super.visit(node);
        node.setScope(popScope());
    }
    // #@@}

    // #@@range/pushScope{
    protected void pushScope(List<? extends DefinedVariable> vars) {
        LocalScope scope = new LocalScope(currentScope());
        for (DefinedVariable var : vars) {
            if (scope.isDefinedLocally(var.name())) {
                error(var, "duplicated variable in scope: " + var.name());
            }
            else {
                scope.defineVariable(var);
            }
        }
        scopeStack.addLast(scope);
    }
    // #@@}

    // #@@range/popScope{
    protected LocalScope popScope() {
        return (LocalScope)scopeStack.removeLast();
    }
    // #@@}

    // #@@range/currentScope{
    protected Scope currentScope() {
        return scopeStack.getLast();
    }
    // #@@}

    // #@@range/StringLiteralNode{
    public void visit(StringLiteralNode node) {
        node.setEntry(constantTable.intern(node.value()));
    }
    // #@@}

    // #@@range/VariableNode{
    public void visit(VariableNode node) {
        try {
            Entity ent = currentScope().get(node.name());
            ent.refered();
            node.setEntity(ent);
        }
        catch (SemanticException ex) {
            error(node, ex.getMessage());
        }
    }
    // #@@}

    protected void error(Node node, String message) {
        errorHandler.error(node.location(), message);
    }
}
