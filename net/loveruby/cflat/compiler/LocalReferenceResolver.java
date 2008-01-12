package net.loveruby.cflat.compiler;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class LocalReferenceResolver extends Visitor {
    static public void resolve(AST ast, ErrorHandler handler)
                                    throws SemanticException {
        new LocalReferenceResolver(handler).resolve(ast);
    }

    protected ErrorHandler errorHandler;
    protected ToplevelScope toplevel;
    protected LinkedList scopeStack;
    protected ConstantTable constantTable;

    public LocalReferenceResolver(ErrorHandler h) {
        this.errorHandler = h;
    }

    protected void resolve(Node n) {
        visitNode(n);
    }

    public void resolve(AST ast) throws SemanticException {
        toplevel = ast.scope();
        scopeStack = new LinkedList();
        scopeStack.add(toplevel);
        constantTable = ast.constantTable();

        declareToplevelEntities(ast.declarations());
        defineToplevelEntities(ast.entities());
        resolveGvarInitializers(ast.variables());
        resolveFunctions(ast.functions());
        toplevel.checkReferences(errorHandler);
        if (errorHandler.errorOccured()) {
            throw new SemanticException("compile failed.");
        }
    }

    protected void declareToplevelEntities(Iterator funcdecls) {
        while (funcdecls.hasNext()) {
            toplevel.declare((UndefinedFunction)funcdecls.next());
        }
    }

    protected void defineToplevelEntities(Iterator entities) {
        while (entities.hasNext()) {
            toplevel.define((Entity)entities.next());
        }
    }

    protected void resolveGvarInitializers(Iterator vars) {
        while (vars.hasNext()) {
            DefinedVariable var = (DefinedVariable)vars.next();
            if (var.hasInitializer()) {
                resolve(var.initializer());
            }
        }
    }

    protected void resolveFunctions(Iterator funcs) {
        while (funcs.hasNext()) {
            DefinedFunction func = (DefinedFunction)funcs.next();
            pushFrame(func.parameters());
            resolve(func.body());
            func.setFrame(popFrame());
        }
    }

    protected void pushFrame(Iterator params) {
        Frame frame = new Frame(toplevel);
        while (params.hasNext()) {
            Parameter param = (Parameter)params.next();
            if (frame.isDefinedLocally(param.name())) {
                error(param, "duplicated parameter: " + param.name());
            }
            else {
                frame.allocateVariable(param);
            }
        }
        scopeStack.addLast(frame);
    }

    protected Frame popFrame() {
        return (Frame)scopeStack.removeLast();
    }

    public void visit(BlockNode node) {
        pushScope(node.variables());
        super.visit(node);
        node.setScope(popScope());
    }

    protected void pushScope(Iterator vars) {
        Scope scope = new Scope(currentScope());
        while (vars.hasNext()) {
            Variable var = (Variable)vars.next();
            if (var.isPrivate()) {
                scope.allocateStaticLocalVariable(var);
            }
            else {
                scope.allocateVariable(var);
            }
        }
        scopeStack.addLast(scope);
    }

    protected Scope popScope() {
        return (Scope)scopeStack.removeLast();
    }

    protected Scope currentScope() {
        return (Scope)scopeStack.getLast();
    }

    public void visit(StringLiteralNode node) {
        node.setEntry(constantTable.intern(node.value()));
    }

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

    protected void error(Node node, String message) {
        errorHandler.error(node.location(), message);
    }
}
